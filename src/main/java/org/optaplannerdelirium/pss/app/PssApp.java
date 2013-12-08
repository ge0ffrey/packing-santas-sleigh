/*
 * Copyright 2010 JBoss Inc
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

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.solver.XmlSolverFactory;
import org.optaplanner.examples.common.app.CommonApp;
import org.optaplanner.examples.common.persistence.AbstractSolutionImporter;
import org.optaplanner.examples.common.persistence.SolutionDao;
import org.optaplanner.examples.common.swingui.SolutionPanel;
import org.optaplannerdelirium.pss.persistence.PssDao;
import org.optaplannerdelirium.pss.persistence.PssImporter;
import org.optaplannerdelirium.pss.swingui.PssPanel;

public class PssApp extends CommonApp {

    public static final String SOLVER_CONFIG
            = "/org/optaplannerdelirium/pss/solver/pssSolverConfig.xml";

    public static void main(String[] args) {
        fixateLookAndFeel();
        new PssApp().init();
    }

    public PssApp() {
        super("Packing Santa's Sleigh",
                "http://www.kaggle.com/c/packing-santas-sleigh",
                null);
    }

    @Override
    protected Solver createSolver() {
        XmlSolverFactory solverFactory = new XmlSolverFactory(SOLVER_CONFIG);
        return solverFactory.buildSolver();
    }

    @Override
    protected SolutionPanel createSolutionPanel() {
        return new PssPanel();
    }

    @Override
    protected SolutionDao createSolutionDao() {
        return new PssDao();
    }

    @Override
    protected AbstractSolutionImporter createSolutionImporter() {
        return new PssImporter();
    }

}
