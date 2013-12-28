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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.config.heuristic.policy.HeuristicConfigPolicy;
import org.optaplanner.core.config.phase.SolverPhaseConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.phase.custom.CustomSolverPhase;
import org.optaplanner.core.impl.phase.custom.CustomSolverPhaseCommand;
import org.optaplanner.core.impl.phase.custom.DefaultCustomSolverPhase;
import org.optaplanner.core.impl.termination.Termination;

@XStreamAlias("lockingPartitionerConfig")
public class LockingPartitionerConfig extends SolverPhaseConfig {

    // Warning: all fields are null (and not defaulted) because they can be inherited
    // and also because the input config file should match the output config file

    private Long from = null;
    private Long to = null;

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public CustomSolverPhase buildSolverPhase(int phaseIndex, HeuristicConfigPolicy solverConfigPolicy,
            Termination solverTermination) {
        HeuristicConfigPolicy phaseConfigPolicy = solverConfigPolicy.createPhaseConfigPolicy();
        DefaultCustomSolverPhase customSolverPhase = new DefaultCustomSolverPhase();
        configureSolverPhase(customSolverPhase, phaseIndex, phaseConfigPolicy, solverTermination);
        LockingPartitioner lockingPartitioner = new LockingPartitioner(from, to);
        List<CustomSolverPhaseCommand> customSolverPhaseCommandList
                = Collections.<CustomSolverPhaseCommand>singletonList(lockingPartitioner);
        customSolverPhase.setCustomSolverPhaseCommandList(customSolverPhaseCommandList);
        customSolverPhase.setForceUpdateBestSolution(true);
        return customSolverPhase;
    }

    public void inherit(LockingPartitionerConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        from = ConfigUtils.inheritOverwritableProperty(from,
                inheritedConfig.getFrom());
        to = ConfigUtils.inheritOverwritableProperty(to,
                inheritedConfig.getTo());
    }


}
