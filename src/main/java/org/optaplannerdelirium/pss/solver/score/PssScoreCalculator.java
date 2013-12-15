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

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.optaplanner.core.api.score.buildin.simple.SimpleScore;
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;
import org.optaplannerdelirium.pss.domain.AnchorAllocation;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Sleigh;

public class PssScoreCalculator implements SimpleScoreCalculator<Sleigh> {

    public static final int SLEIGH_X = 1000;
    public static final int SLEIGH_Y = 1000;

    public SimpleScore calculateScore(Sleigh sleigh) {
        AnchorAllocation anchorAllocation = sleigh.getAnchorAllocation();
        PresentAllocation presentAllocation = anchorAllocation.getNextPresentAllocation();
        int[][] ground = new int[SLEIGH_X][SLEIGH_Y];
        SortedSet<Corner> corners = new TreeSet<Corner>();
        corners.add(new Corner(0, 0, 0));
        while (presentAllocation != null) {
            for (Iterator<Corner> it = corners.iterator(); it.hasNext(); ) {
                Corner corner = it.next();
                if (fits(presentAllocation, corner, ground)) {
                    int xEnd = corner.x + presentAllocation.getXLength();
                    int yEnd = corner.y + presentAllocation.getYLength();
                    int zEnd = corner.z + presentAllocation.getZLength();
                    for (int x = corner.x; x < xEnd; x++) {
                        for (int y = corner.y; y < yEnd; y++) {
                            ground[x][y] = zEnd;
                        }
                    }
                    it.remove();
                    // todo add new corners

                    break;
                }
            }
            presentAllocation = presentAllocation.getNextPresentAllocation();
        }
        int maxZ = corners.last().z;
        return SimpleScore.valueOf(-2 * maxZ);
    }

    private boolean fits(PresentAllocation presentAllocation, Corner corner, int[][] ground) {
        int xEnd = corner.x + presentAllocation.getXLength();
        if (xEnd > SLEIGH_X) {
            return false;
        }
        int yEnd = corner.y + presentAllocation.getYLength();
        if (yEnd > SLEIGH_Y) {
            return false;
        }
        for (int x = corner.x; x < xEnd; x++) {
            for (int y = corner.y; y < yEnd; y++) {
                if (ground[x][y] > corner.z) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class Corner implements Comparable<Corner> {

        public int x;
        public int y;
        public int z;

        private Corner(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public int compareTo(Corner other) {
            if (z < other.z) {
                return -1;
            } else if (z > other.z) {
                return 1;
            } else {
                if (y < other.y) {
                    return -1;
                } else if (y > other.y) {
                    return 1;
                } else {
                    if (y < other.y) {
                        return -1;
                    } else if (y > other.y) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        }

        @Override
        public int hashCode() {
            return z + y *2000 + x * 2000000;
        }

        @Override
        public boolean equals(Object o) {
            Corner other = (Corner) o;
            return z == other.z && y == other.y && x == other.x;
        }
    }

}
