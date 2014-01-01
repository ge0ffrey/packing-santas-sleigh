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

package org.optaplannerdelirium.pss.app;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.localsearch.LocalSearchSolverPhaseConfig;
import org.optaplanner.core.config.phase.SolverPhaseConfig;
import org.optaplanner.core.config.phase.custom.CustomSolverPhaseConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.XmlSolverFactory;
import org.optaplanner.core.config.termination.TerminationConfig;
import org.optaplanner.core.impl.phase.custom.CustomSolverPhaseCommand;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.common.app.LoggingMain;
import org.optaplannerdelirium.pss.domain.Sleigh;
import org.optaplannerdelirium.pss.persistence.PssDao;
import org.optaplannerdelirium.pss.persistence.PssExporter;
import org.optaplannerdelirium.pss.solver.partitioner.LockingPartitionerConfig;

public class PssPartitionedApp extends LoggingMain {

    public static final String SOLVER_CONFIG
            = "/org/optaplannerdelirium/pss/solver/pssPartitionedSolverConfig.xml";

    public static void main(String[] args) {
        new PssPartitionedApp().run();
    }

    private int availableTimeInMinutes = 3;
    private int partitionOffsetIncrement = 500;
    private int partitionJoinCount = 2;

    private PssDao pssDao;
    private File unsolvedFile;

    public PssPartitionedApp() {
        pssDao = new PssDao();
        unsolvedFile = new File(pssDao.getDataDir(), "unsolved/subset_10k_presents.planner.csv");
    }

    public void run() {
        Sleigh unsolvedSleigh = (Sleigh) pssDao.readSolution(unsolvedFile);

        Solver solver = buildSolver(unsolvedSleigh.getPresentList().size());
        solver.setPlanningProblem(unsolvedSleigh);
        solver.solve();

        Solution bestSolution = solver.getBestSolution();
        writeBestSolution(bestSolution);
        logger.info("Finished partitioned solver with partitionOffsetIncrement ({}) and partitionJoinCount ({})"
                + " in availableTimeInMinutes ({}).",
                partitionOffsetIncrement, partitionJoinCount, availableTimeInMinutes);
    }

    private Solver buildSolver(int presentSize) {
        XmlSolverFactory solverFactory = new XmlSolverFactory();
        solverFactory.addXstreamAnnotations(LockingPartitionerConfig.class);
        solverFactory.configure(SOLVER_CONFIG);
        SolverConfig solverConfig = solverFactory.getSolverConfig();

        if (presentSize % partitionOffsetIncrement != 0
                || partitionOffsetIncrement <= 0
                || partitionJoinCount <= 0) {
            throw new IllegalStateException("Invalid partitionOffsetIncrement (" + partitionOffsetIncrement
                    + ") or partitionBufferCount (" + partitionJoinCount + ").");
        }
        int partitionCount = (presentSize / partitionOffsetIncrement) + (partitionJoinCount - 1);
        long availableTimeMillis = (long) availableTimeInMinutes * 60000L;
        // Note: actually it should be less as the non local search phases take time too
        long availableTimeMillisPerPartition = availableTimeMillis / (long) partitionCount;
        logger.info("Starting partitioned solver with availableTimeMillisPerPartition ({}),"
                + " partitionOffsetIncrement ({}) and partitionJoinCount ({})"
                + " in availableTimeInMinutes ({}).",
                availableTimeMillisPerPartition, partitionOffsetIncrement, partitionJoinCount, availableTimeInMinutes);

        List<SolverPhaseConfig> oldSolverPhaseConfigList = solverConfig.getSolverPhaseConfigList();
        if (oldSolverPhaseConfigList.size() != 2) {
            throw new IllegalStateException("Invalid oldSolverPhaseConfigList size ("
                    + oldSolverPhaseConfigList.size() + ").");
        }
        LockingPartitionerConfig oldLockingPartitionerConfig = (LockingPartitionerConfig) oldSolverPhaseConfigList.get(0);
        LocalSearchSolverPhaseConfig oldLocalSearchConfig = (LocalSearchSolverPhaseConfig) oldSolverPhaseConfigList.get(1);
        TerminationConfig oldTerminationConfig = oldLocalSearchConfig.getTerminationConfig();
        if (oldTerminationConfig == null) {
            oldTerminationConfig = new TerminationConfig();
            oldLocalSearchConfig.setTerminationConfig(oldTerminationConfig);
        }
        oldTerminationConfig.setMaximumTimeMillisSpend(availableTimeMillisPerPartition);
        List<SolverPhaseConfig> newSolverPhaseConfigList = new ArrayList<SolverPhaseConfig>(partitionCount * 2 + 1);
        for (int partitionIndex = 0; partitionIndex < partitionCount; partitionIndex++) {
            LockingPartitionerConfig newLockingPartitionerConfig = new LockingPartitionerConfig();
            newLockingPartitionerConfig.inherit(oldLockingPartitionerConfig);
            int from = (partitionIndex - partitionJoinCount + 1) * partitionOffsetIncrement;
            int to = from + (partitionJoinCount * partitionOffsetIncrement);
            newLockingPartitionerConfig.setFrom((long) Math.max(0, from));
            newLockingPartitionerConfig.setTo((long) Math.min(presentSize, to));
            newSolverPhaseConfigList.add(newLockingPartitionerConfig);
            LocalSearchSolverPhaseConfig newLocalSearchConfig = new LocalSearchSolverPhaseConfig();
            newLocalSearchConfig.inherit(oldLocalSearchConfig);
            newSolverPhaseConfigList.add(oldLocalSearchConfig);
        }
        solverConfig.setSolverPhaseConfigList(newSolverPhaseConfigList);

        return solverFactory.buildSolver();
    }

    private void writeBestSolution(Solution bestSolution) {
        // String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        String filename = FilenameUtils.getBaseName(FilenameUtils.getBaseName(unsolvedFile.getName()))
                + "_" + availableTimeInMinutes + "m_" + partitionOffsetIncrement + "-" + partitionJoinCount
                + "_score" + bestSolution.getScore().toString().replace("/", "_");
        File solvedFile = new File(pssDao.getDataDir(), "solved/" + filename + "." + pssDao.getFileExtension());
        pssDao.writeSolution(bestSolution, solvedFile);
        PssExporter pssExporter = new PssExporter();
        File exportFile = new File(pssDao.getDataDir(), "export/" + filename + ".sol.csv");
        pssExporter.writeSolution(bestSolution, exportFile);
    }

}
