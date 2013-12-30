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

package org.optaplannerdelirium.pss.solver.score;

import java.util.SortedSet;
import java.util.TreeSet;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator;
import org.optaplannerdelirium.pss.domain.Allocation;
import org.optaplannerdelirium.pss.domain.AnchorAllocation;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Sleigh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PssIncrementalScoreCalculator extends AbstractIncrementalScoreCalculator<Sleigh> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected static final int SLEIGH_X = PssSimpleScoreCalculator.SLEIGH_X;
    protected static final int SLEIGH_Y = PssSimpleScoreCalculator.SLEIGH_Y;

    protected final PssSimpleScoreCalculator simpleScoreCalculator = new PssSimpleScoreCalculator();

    protected Point[][] lockedGround;
    protected Allocation lastLockedAllocation;

    @Override
    public void resetWorkingSolution(Sleigh sleigh) {
        lockedGround = new Point[SLEIGH_X][SLEIGH_Y];
        for (int i = 0; i < SLEIGH_X; i++) {
            for (int j = 0; j < SLEIGH_Y; j++) {
                lockedGround[i][j] = new Point(i, j);
            }
        }
        lastLockedAllocation = sleigh.getAnchorAllocation();
        int state = 0;
        AnchorAllocation anchorAllocation = sleigh.getAnchorAllocation();
        PresentAllocation presentAllocation = anchorAllocation.getNextPresentAllocation();
        while (presentAllocation != null) {
            if (presentAllocation.isLocked() && presentAllocation.getRotation() != null) {
                // Closed
                if (state > 0) {
                    throw new IllegalStateException("Invalid state (" + state
                            + ") for presentAllocation (" + presentAllocation + ").");
                }
                state = 0;
                int xStart = presentAllocation.getCalculatedX();
                int yStart = presentAllocation.getCalculatedY();
                int zStart = presentAllocation.getCalculatedZ();
                int xEnd = xStart + presentAllocation.getXLength();
                int yEnd = yStart + presentAllocation.getYLength();
                int zEnd = zStart + presentAllocation.getZLength();
                for (int x = xStart; x < xEnd; x++) {
                    for (int y = yStart; y < yEnd; y++) {
                        Point point = lockedGround[x][y];
                        if (zEnd > point.z) {
                            point.z = zEnd;
                        }
                    }
                }
                lastLockedAllocation = presentAllocation;
            } else if (!presentAllocation.isLocked()) {
                // Open
                if (state > 1) {
                    throw new IllegalStateException("Invalid state (" + state
                            + ") for presentAllocation (" + presentAllocation + ").");
                }
                state = 1;
                // Do nothing
            } else {
                // Not yet open
                state = 2;
                // Do nothing
            }
            presentAllocation = presentAllocation.getNextPresentAllocation();
        }
        // Refresh spaceEnds
        for (int y = SLEIGH_Y - 1; y >= 0; y--) {
            lockedGround[SLEIGH_X - 1][y].xSpaceEnd = SLEIGH_X;
            for (int x = SLEIGH_X - 2; x >= 0; x--) {
                Point point = lockedGround[x][y];
                Point followingPoint = lockedGround[x + 1][y];
                while (true) {
                    if (point.z < followingPoint.z) {
                        point.xSpaceEnd = followingPoint.x;
                        break;
                    } else if (point.z == followingPoint.z || followingPoint.xSpaceEnd >= SLEIGH_X) {
                        point.xSpaceEnd = followingPoint.xSpaceEnd;
                        break;
                    }
                    followingPoint = lockedGround[followingPoint.xSpaceEnd][y];
                }
            }
        }
        for (int x = SLEIGH_X - 1; x >= 0; x--) {
            lockedGround[x][SLEIGH_Y - 1].ySpaceEnd = SLEIGH_Y;
            for (int y = SLEIGH_Y - 2; y >= 0; y--) {
                Point point = lockedGround[x][y];
                Point followingPoint = lockedGround[x][y + 1];
                while (true) {
                    if (point.z < followingPoint.z) {
                        point.ySpaceEnd = followingPoint.y;
                        break;
                    } else if (point.z == followingPoint.z || followingPoint.ySpaceEnd >= SLEIGH_Y) {
                        point.ySpaceEnd = followingPoint.ySpaceEnd;
                        break;
                    }
                    followingPoint = lockedGround[x][followingPoint.ySpaceEnd];
                }
            }
        }
        // Refresh cornerMark
        for (Point[] points : lockedGround) {
            for (Point point : points) {
                point.cornerMark = point.isCorner(lockedGround);
            }
        }
    }

    @Override
    public void beforeEntityAdded(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterEntityAdded(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeVariableChanged(Object entity, String variableName) {
        PresentAllocation presentAllocation = (PresentAllocation) entity;
        if (presentAllocation.isLocked()) {
            throw new IllegalArgumentException("The presentAllocation (" + presentAllocation + ") is locked.");
        }
    }

    @Override
    public void afterVariableChanged(Object entity, String variableName) {
        // Do nothing
    }

    @Override
    public void beforeEntityRemoved(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterEntityRemoved(Object entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleScore calculateScore() {
        Point[][] ground = new Point[SLEIGH_X][SLEIGH_Y];
        SortedSet<Point> cornerSet = new TreeSet<Point>();
        for (int x = 0; x < SLEIGH_X; x++) {
            for (int y = 0; y < SLEIGH_Y; y++) {
                Point point = new Point(lockedGround[x][y]);
                ground[x][y] = point;
                if (point.cornerMark) {
                    boolean added = cornerSet.add(point);
                    if (!added) {
                        throw new IllegalStateException("Point (" + point + ") not added.");
                    }
                }
            }
        }
        return simpleScoreCalculator.calculateScore(ground, cornerSet, lastLockedAllocation);
    }

}
