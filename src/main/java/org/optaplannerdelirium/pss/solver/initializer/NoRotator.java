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

package org.optaplannerdelirium.pss.solver.initializer;

import org.optaplanner.core.impl.phase.custom.CustomSolverPhaseCommand;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Rotation;
import org.optaplannerdelirium.pss.domain.Sleigh;
import org.optaplannerdelirium.pss.domain.solver.MovablePresentAllocationSelectionFilter;

public class NoRotator implements CustomSolverPhaseCommand {

    private MovablePresentAllocationSelectionFilter filter = new MovablePresentAllocationSelectionFilter();

    @Override
    public void changeWorkingSolution(ScoreDirector scoreDirector) {
        Sleigh sleigh = (Sleigh) scoreDirector.getWorkingSolution();
        for (PresentAllocation presentAllocation : sleigh.getPresentAllocationList()) {
            if (filter.accept(scoreDirector, presentAllocation)) {
                scoreDirector.beforeVariableChanged(presentAllocation, "rotation");
                presentAllocation.setRotation(Rotation.AXBYCZ);
                scoreDirector.afterVariableChanged(presentAllocation, "rotation");
            }
        }
    }

}
