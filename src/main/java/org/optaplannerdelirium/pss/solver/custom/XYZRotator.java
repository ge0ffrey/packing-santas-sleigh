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

package org.optaplannerdelirium.pss.solver.custom;

import org.optaplanner.core.impl.phase.custom.CustomSolverPhaseCommand;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Rotation;
import org.optaplannerdelirium.pss.domain.Sleigh;

public class XYZRotator implements CustomSolverPhaseCommand {

    @Override
    public void changeWorkingSolution(ScoreDirector scoreDirector) {
        Sleigh sleigh = (Sleigh) scoreDirector.getWorkingSolution();
        for (PresentAllocation presentAllocation : sleigh.getPresentAllocationList()) {
            int a = presentAllocation.getPresent().getA();
            int b = presentAllocation.getPresent().getB();
            int c = presentAllocation.getPresent().getC();
            Rotation rotation;
            if (a >= b && b >= c) {
                rotation = Rotation.AXBYCZ;
            } else if (a >= c && c >= b) {
                rotation = Rotation.AXBZCY;
            } else if (b >= a && a >= c) {
                rotation = Rotation.AYBXCZ;
            } else if (c >= a && a >= b) {
                rotation = Rotation.AYBZCX;
            } else if (b >= c && c >= a) {
                rotation = Rotation.AZBXCY;
            } else if (c >= b && b >= a) {
                rotation = Rotation.AZBYCX;
            } else {
                throw new IllegalStateException("Impossible");
            }
            scoreDirector.beforeVariableChanged(presentAllocation, "rotation");
            presentAllocation.setRotation(rotation);
            scoreDirector.afterVariableChanged(presentAllocation, "rotation");
        }
    }

}
