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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.optaplanner.core.impl.score.buildin.simple.SimpleScoreDefinition;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.common.persistence.AbstractSolutionDao;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionExporter;
import org.optaplanner.examples.common.persistence.AbstractTxtSolutionImporter;
import org.optaplanner.examples.common.persistence.XStreamSolutionDao;
import org.optaplannerdelirium.pss.domain.Allocation;
import org.optaplannerdelirium.pss.domain.AnchorAllocation;
import org.optaplannerdelirium.pss.domain.Present;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Rotation;
import org.optaplannerdelirium.pss.domain.Sleigh;

/**
 * The default XML format is too verbose and throws {@link OutOfMemoryError}.
 */
public class PssDao extends AbstractSolutionDao {

    public PssDao() {
        super("pss");
    }

    @Override
    public String getFileExtension() {
        return "planner.csv";
    }

    @Override
    public Solution readSolution(File inputFile) {
        // TODO DRY duplicated from AbstractTxtSolutionImporter
        Solution solution;
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-8"));
            AbstractTxtSolutionImporter.TxtInputBuilder txtInputBuilder = new SleighInputBuilder();
            txtInputBuilder.setInputFile(inputFile);
            txtInputBuilder.setBufferedReader(bufferedReader);
            try {
                solution = txtInputBuilder.readSolution();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Exception in inputFile (" + inputFile + ")", e);
            } catch (IllegalStateException e) {
                throw new IllegalStateException("Exception in inputFile (" + inputFile + ")", e);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the file (" + inputFile.getName() + ").", e);
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
        logger.info("Opened: {}", inputFile);
        return solution;
    }

    @Override
    public void writeSolution(Solution solution, File outputFile) {
        // TODO DRY duplicated from AbstractTxtSolutionExporter
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
            AbstractTxtSolutionExporter.TxtOutputBuilder txtOutputBuilder = new SleighOutputBuilder();
            txtOutputBuilder.setBufferedWriter(bufferedWriter);
            txtOutputBuilder.setSolution(solution);
            txtOutputBuilder.writeSolution();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not write the file (" + outputFile.getName() + ").", e);
        } finally {
            IOUtils.closeQuietly(bufferedWriter);
        }
        logger.info("Saved: {}", outputFile);
    }

    public class SleighInputBuilder extends AbstractTxtSolutionImporter.TxtInputBuilder {

        private Sleigh sleigh;

        public Solution readSolution() throws IOException {
            sleigh = new Sleigh();
            sleigh.setId(0L);
            readConstantLine("SLEIGH");
            int presentSize = readIntegerValue("PresentSize:");
            SimpleScoreDefinition scoreDefinition = new SimpleScoreDefinition();
            sleigh.setScore(scoreDefinition.parseScore(readStringValue("Score:")));
            Map<Long, Allocation> allocationMap = new HashMap<Long, Allocation>(presentSize);
            readConstantLine("PRESENTS");
            AnchorAllocation anchorAllocation = new AnchorAllocation();
            anchorAllocation.setId(0L);
            sleigh.setAnchorAllocation(anchorAllocation);
            allocationMap.put(anchorAllocation.getId(), anchorAllocation);
            readConstantLine("id,a,b,c,locked,rotation,previousAllocation,calculatedX,calculatedY,calculatedZ");
            List<Present> presentList = new ArrayList<Present>(presentSize);
            List<PresentAllocation> presentAllocationList = new ArrayList<PresentAllocation>(presentSize);
            Map<PresentAllocation, Long> previousAllocationMap = new HashMap<PresentAllocation, Long>(presentSize);
            for (int i = 0; i < presentSize; i++) {
                String line = bufferedReader.readLine();
                String[] lineTokens = splitBy(line, ",", "comma", 10, true, false);
                Present present = new Present();
                present.setId(Long.parseLong(lineTokens[0]));
                present.setA(Integer.parseInt(lineTokens[1]));
                present.setB(Integer.parseInt(lineTokens[2]));
                present.setC(Integer.parseInt(lineTokens[3]));
                presentList.add(present);
                PresentAllocation presentAllocation = new PresentAllocation();
                presentAllocation.setId(present.getId());
                presentAllocation.setPresent(present);
                presentAllocation.setLocked(Boolean.valueOf(lineTokens[4]));
                String rotationString = lineTokens[5];
                presentAllocation.setRotation(rotationString.equals("null") ? null : Rotation.valueOf(rotationString));
                String previousAllocationString = lineTokens[6];
                if (!previousAllocationString.equals("null")) {
                    // Delay id resolution until all presentAllocations are created
                    previousAllocationMap.put(presentAllocation, Long.parseLong(previousAllocationString));
                }
                presentAllocation.setCalculatedX(Integer.parseInt(lineTokens[7]));
                presentAllocation.setCalculatedY(Integer.parseInt(lineTokens[8]));
                presentAllocation.setCalculatedZ(Integer.parseInt(lineTokens[9]));
                presentAllocationList.add(presentAllocation);
                allocationMap.put(presentAllocation.getId(), presentAllocation);
            }
            for (Map.Entry<PresentAllocation, Long> entry : previousAllocationMap.entrySet()) {
                PresentAllocation presentAllocation = entry.getKey();
                Allocation previousAllocation = allocationMap.get(entry.getValue());
                if (previousAllocation == null) {
                    throw new IllegalStateException("The previousAllocation for presentAllocation ("
                            + presentAllocation + ") cannot be null.");
                }
                presentAllocation.setPreviousAllocation(previousAllocation);
            }
            sleigh.setPresentList(presentList);
            sleigh.setPresentAllocationList(presentAllocationList);
            return sleigh;
        }

    }

    public class SleighOutputBuilder extends AbstractTxtSolutionExporter.TxtOutputBuilder {

        private Sleigh sleigh;

        public void setSolution(Solution solution) {
            sleigh = (Sleigh) solution;
        }

        public void writeSolution() throws IOException {
            List<PresentAllocation> presentAllocationList = sleigh.getPresentAllocationList();
            bufferedWriter.write("SLEIGH\n");
            bufferedWriter.write("PresentSize: " + presentAllocationList.size() +"\n");
            bufferedWriter.write("Score: " + sleigh.getScore().toString() +"\n");
            bufferedWriter.write("PRESENTS\n");
            bufferedWriter.write("id,a,b,c,locked,rotation,previousAllocation,calculatedX,calculatedY,calculatedZ\n");
            for (PresentAllocation presentAllocation : presentAllocationList) {
                Present present = presentAllocation.getPresent();
                bufferedWriter.write(Long.toString(present.getId()));
                bufferedWriter.write(",");
                if (!present.getId().equals(presentAllocation.getId())) {
                    throw new IllegalStateException("The present id (" + present.getId()
                            + " differs from the presentAllocation id (" + presentAllocation.getId() + ").");
                }
                bufferedWriter.write(Integer.toString(present.getA()));
                bufferedWriter.write(",");
                bufferedWriter.write(Integer.toString(present.getB()));
                bufferedWriter.write(",");
                bufferedWriter.write(Integer.toString(present.getC()));
                bufferedWriter.write(",");
                bufferedWriter.write(Boolean.toString(presentAllocation.isLocked()));
                bufferedWriter.write(",");
                Rotation rotation = presentAllocation.getRotation();
                bufferedWriter.write(rotation == null ? "null" : rotation.name());
                bufferedWriter.write(",");
                Allocation previousAllocation = presentAllocation.getPreviousAllocation();
                bufferedWriter.write((previousAllocation == null ? "null" : Long.toString(previousAllocation.getId())));
                bufferedWriter.write(",");
                bufferedWriter.write(Integer.toString(presentAllocation.getCalculatedX()));
                bufferedWriter.write(",");
                bufferedWriter.write(Integer.toString(presentAllocation.getCalculatedY()));
                bufferedWriter.write(",");
                bufferedWriter.write(Integer.toString(presentAllocation.getCalculatedZ()));
                bufferedWriter.write("\n");
            }
        }

    }

}
