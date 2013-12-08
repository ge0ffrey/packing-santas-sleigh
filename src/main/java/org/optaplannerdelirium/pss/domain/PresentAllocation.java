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

package org.optaplannerdelirium.pss.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@PlanningEntity()
@XStreamAlias("PssPresentAllocation")
public class PresentAllocation extends AbstractPersistable implements Allocation {

    private Present present;

    // Planning variables: changes during planning, between score calculations.
    private Allocation previousAllocation;

    // Shadow variables
    private PresentAllocation nextPresentAllocation;

    public Present getPresent() {
        return present;
    }

    public void setPresent(Present present) {
        this.present = present;
    }

    @PlanningVariable(chained = true, valueRangeProviderRefs = {"anchorAllocationRange", "presentAllocationRange"})
    public Allocation getPreviousAllocation() {
        return previousAllocation;
    }

    public void setPreviousAllocation(Allocation previousAllocation) {
        this.previousAllocation = previousAllocation;
    }

    public PresentAllocation getNextPresentAllocation() {
        return nextPresentAllocation;
    }

    public void setNextPresentAllocation(PresentAllocation nextPresentAllocation) {
        this.nextPresentAllocation = nextPresentAllocation;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public String toString() {
        return present + "(after "
                + (previousAllocation == null ? "null" : previousAllocation instanceof PresentAllocation
                ? ((PresentAllocation) previousAllocation).getPresent() : "anchor")
                + ")";
    }

}
