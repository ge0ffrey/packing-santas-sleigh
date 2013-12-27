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

package org.optaplannerdelirium.pss.solver.partitioner;

import org.optaplanner.core.impl.phase.custom.CustomSolverPhaseCommand;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Rotation;
import org.optaplannerdelirium.pss.domain.Sleigh;
import org.optaplannerdelirium.pss.domain.solver.MovablePresentAllocationSelectionFilter;

public class LockingPartitioner implements CustomSolverPhaseCommand {

    private long from = 0;
    private long to = 100;

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    @Override
    public void changeWorkingSolution(ScoreDirector scoreDirector) {
        Sleigh sleigh = (Sleigh) scoreDirector.getWorkingSolution();
        for (PresentAllocation presentAllocation : sleigh.getPresentAllocationList()) {
            Long id = presentAllocation.getId();
            // Weird logic because the id's start from 1 instead of 0.
            boolean locked = !(id > from && id <= to);
            // Locked is not a variable
            // HACK: we don't call problemFactChanged to avoid unneeded code execution ...
            // scoreDirector.beforeProblemFactChanged(presentAllocation);
            presentAllocation.setLocked(locked);
            // scoreDirector.beforeProblemFactChanged(presentAllocation);
        }
    }

}
