/*
 * Copyright 2013 JBoss Inc
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

import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionExporter;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Sleigh;

public class PssExporter extends AbstractTxtSolutionExporter {

    private static final String OUTPUT_FILE_SUFFIX = "sol.csv";

    public static void main(String[] args) {
        new PssExporter().convertAll();
    }

    public PssExporter() {
        super(new PssDao());
    }

    @Override
    protected String getOutputFileSuffix() {
        return OUTPUT_FILE_SUFFIX;
    }

    public TxtOutputBuilder createTxtOutputBuilder() {
        return new PssOutputBuilder();
    }

    public class PssOutputBuilder extends TxtOutputBuilder {

        private Sleigh sleigh;

        public void setSolution(Solution solution) {
            sleigh = (Sleigh) solution;
        }

        public void writeSolution() throws IOException {
            int maxZ = findMaxZ();
            bufferedWriter.write("PresentId,x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4,x5,y5,z5,x6,y6,z6,x7,y7,z7,x8,y8,z8\n");
            for (PresentAllocation presentAllocation : sleigh.getPresentAllocationList()) {
                bufferedWriter.write(Long.toString(presentAllocation.getPresent().getId()));
                // Origin is 1, 1, 1 (not 0, 0, 0)
                int x = presentAllocation.getCalculatedX() + 1;
                int y = presentAllocation.getCalculatedY() + 1;
                int z = maxZ - presentAllocation.getZLength() - presentAllocation.getCalculatedZ() + 1;
                int xLength = presentAllocation.getXLength() - 1;
                int yLength = presentAllocation.getYLength() - 1;
                int zLength = presentAllocation.getZLength() - 1;
                writePoint(x, y, z);
                writePoint(x + xLength, y, z);
                writePoint(x, y + yLength, z);
                writePoint(x + xLength, y + yLength, z);
                writePoint(x, y, z + zLength);
                writePoint(x + xLength, y, z + zLength);
                writePoint(x, y + yLength, z + zLength);
                writePoint(x + xLength , y + yLength, z + zLength);
                bufferedWriter.write("\n");
            }
        }

        private int findMaxZ() {
            int maxZ = 0;
            for (PresentAllocation presentAllocation : sleigh.getPresentAllocationList()) {
                int endZ = presentAllocation.getCalculatedZ() + presentAllocation.getZLength();
                if (endZ > maxZ) {
                    maxZ = endZ;
                }
            }
            return maxZ;
        }

        public void writePoint(int x, int y, int z) throws IOException {
            bufferedWriter.write(",");
            bufferedWriter.write(Integer.toString(x));
            bufferedWriter.write(",");
            bufferedWriter.write(Integer.toString(y));
            bufferedWriter.write(",");
            bufferedWriter.write(Integer.toString(z));
        }

    }

}
