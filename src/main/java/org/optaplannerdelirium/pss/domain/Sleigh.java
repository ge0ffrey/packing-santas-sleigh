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

package org.optaplannerdelirium.pss.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@PlanningSolution
public class Sleigh extends AbstractPersistable implements Solution<SimpleScore> {

    private List<Present> presentList;
    private AnchorAllocation anchorAllocation;

    private List<PresentAllocation> presentAllocationList;

    private SimpleScore score;

    public List<Present> getPresentList() {
        return presentList;
    }

    public void setPresentList(List<Present> presentList) {
        this.presentList = presentList;
    }

    public AnchorAllocation getAnchorAllocation() {
        return anchorAllocation;
    }

    public void setAnchorAllocation(AnchorAllocation anchorAllocation) {
        this.anchorAllocation = anchorAllocation;
    }

    @ValueRangeProvider(id = "anchorAllocationRange")
    public List<AnchorAllocation> getAnchorList() {
        return Collections.singletonList(anchorAllocation);
    }

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "presentAllocationRange")
    public List<PresentAllocation> getPresentAllocationList() {
        return presentAllocationList;
    }

    @ValueRangeProvider(id = "rotationRange")
    public List<Rotation> getRotationList() {
        return Arrays.asList(Rotation.values());
    }

    public void setPresentAllocationList(List<PresentAllocation> presentAllocationList) {
        this.presentAllocationList = presentAllocationList;
    }

    public SimpleScore getScore() {
        return score;
    }

    public void setScore(SimpleScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * Not used
     * @return never null
     */
    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(presentList);
        facts.add(anchorAllocation);
        // Do not add the planning entity's (processList) because that will be done automatically
        return facts;
    }

}
