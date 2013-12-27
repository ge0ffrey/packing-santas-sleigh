/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplannerdelirium.pss.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.optaplannerdelirium.pss.domain.Allocation;
import org.optaplannerdelirium.pss.domain.AnchorAllocation;
import org.optaplannerdelirium.pss.domain.Present;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Rotation;
import org.optaplannerdelirium.pss.domain.Sleigh;

public class PssImporter extends AbstractTxtSolutionImporter {

    private static final String INPUT_FILE_SUFFIX = "csv";

    public static void main(String[] args) {
        new PssImporter().convertAll();
    }

    public PssImporter() {
        super(new PssDao());
    }

    @Override
    public String getInputFileSuffix() {
        return INPUT_FILE_SUFFIX;
    }

    public TxtInputBuilder createTxtInputBuilder() {
        return new PssInputBuilder();
    }

    public class PssInputBuilder extends TxtInputBuilder {

        private Sleigh sleigh;

        public Solution readSolution() throws IOException {
            sleigh = new Sleigh();
            sleigh.setId(0L);
            readPresentList();
            createAnchorAllocation();
            createPresentAllocationList();
            assignPresentAllocationList();
            logger.info("Sleigh {} has {} presents.",
                    getInputId(),
                    sleigh.getPresentList().size());
//            BigInteger possibleSolutionSize = factorial(sleigh.getPresentList().size());
//            logger.info("Sleigh {} has {} presents with a search space of {}.",
//                    getInputId(),
//                    sleigh.getPresentList().size(),
//                    getFlooredPossibleSolutionSize(possibleSolutionSize));
            return sleigh;
        }

        private void readPresentList() throws IOException {
            readConstantLine("PresentId,Dimension1,Dimension2,Dimension3");
            List<Present> presentList = new ArrayList<Present>(1000000);
            String line = bufferedReader.readLine();
            while (line != null && !line.trim().isEmpty()) {
                String[] lineTokens = splitBy(line, ",", "comma", 4, true, false);
                Present present = new Present();
                present.setId(Long.parseLong(lineTokens[0]));
                present.setA(Integer.parseInt(lineTokens[1]));
                present.setB(Integer.parseInt(lineTokens[2]));
                present.setC(Integer.parseInt(lineTokens[3]));
                presentList.add(present);
                line = bufferedReader.readLine();
            }
            sleigh.setPresentList(presentList);
        }

        private void createAnchorAllocation() {
            AnchorAllocation anchorAllocation = new AnchorAllocation();
            anchorAllocation.setId(0L);
            sleigh.setAnchorAllocation(anchorAllocation);
        }

        private void createPresentAllocationList() {
            List<Present> presentList = sleigh.getPresentList();
            List<PresentAllocation> presentAllocationList = new ArrayList<PresentAllocation>(presentList.size());
            for (Present present : presentList) {
                PresentAllocation presentAllocation = new PresentAllocation();
                presentAllocation.setId(present.getId());
                presentAllocation.setPresent(present);
                presentAllocationList.add(presentAllocation);
            }
            sleigh.setPresentAllocationList(presentAllocationList);
        }

        private void assignPresentAllocationList() {
            Allocation previousAllocation = sleigh.getAnchorAllocation();
            for (PresentAllocation presentAllocation : sleigh.getPresentAllocationList()) {
                presentAllocation.setRotation(null);
                presentAllocation.setPreviousAllocation(previousAllocation);
                previousAllocation.setNextPresentAllocation(presentAllocation);
                previousAllocation = presentAllocation;
            }
        }

    }

}
