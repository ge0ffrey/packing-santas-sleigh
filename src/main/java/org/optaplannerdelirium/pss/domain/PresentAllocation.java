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
    private Rotation rotation;
    private Allocation previousAllocation;

    // Shadow variables
    private PresentAllocation nextPresentAllocation;

    // HACK: shadow variables set by ScoreCalculator
    private int calculatedX;
    private int calculatedY;
    private int calculatedZ;

    public Present getPresent() {
        return present;
    }

    public void setPresent(Present present) {
        this.present = present;
    }

    @PlanningVariable(valueRangeProviderRefs = {"rotationRange"})
    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
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

    public int getXLength() {
        switch (rotation) {
            case AXBYCZ:
            case AXBZCY:
                return present.getA();
            case AYBXCZ:
            case AZBXCY:
                return present.getB();
            case AYBZCX:
            case AZBYCX:
                return present.getC();
            default:
                throw new IllegalStateException("The rotation (" + rotation + ") is not implemented.");
        }
    }

    public int getYLength() {
        switch (rotation) {
            case AYBXCZ:
            case AYBZCX:
                return present.getA();
            case AXBYCZ:
            case AZBYCX:
                return present.getB();
            case AXBZCY:
            case AZBXCY:
                return present.getC();
            default:
                throw new IllegalStateException("The rotation (" + rotation + ") is not implemented.");
        }
    }

    public int getZLength() {
        switch (rotation) {
            case AZBXCY:
            case AZBYCX:
                return present.getA();
            case AXBZCY:
            case AYBZCX:
                return present.getB();
            case AXBYCZ:
            case AYBXCZ:
                return present.getC();
            default:
                throw new IllegalStateException("The rotation (" + rotation + ") is not implemented.");
        }
    }

    public int getCalculatedX() {
        return calculatedX;
    }

    public void setCalculatedX(int calculatedX) {
        this.calculatedX = calculatedX;
    }

    public int getCalculatedY() {
        return calculatedY;
    }

    public void setCalculatedY(int calculatedY) {
        this.calculatedY = calculatedY;
    }

    public int getCalculatedZ() {
        return calculatedZ;
    }

    public void setCalculatedZ(int calculatedZ) {
        this.calculatedZ = calculatedZ;
    }

    @Override
    public String toString() {
        return present + "(after "
                + (previousAllocation == null ? "null" : previousAllocation instanceof PresentAllocation
                ? ((PresentAllocation) previousAllocation).getPresent() : "anchor")
                + ")";
    }

}
