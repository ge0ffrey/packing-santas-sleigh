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
import org.optaplanner.core.impl.score.director.simple.SimpleScoreCalculator;
import org.optaplannerdelirium.pss.domain.AnchorAllocation;
import org.optaplannerdelirium.pss.domain.PresentAllocation;
import org.optaplannerdelirium.pss.domain.Sleigh;

public class PssScoreCalculator implements SimpleScoreCalculator<Sleigh> {

    private final static boolean assertMode = false;

    public static final int SLEIGH_X = 1000;
    public static final int SLEIGH_Y = 1000;

    public SimpleScore calculateScore(Sleigh sleigh) {
        AnchorAllocation anchorAllocation = sleigh.getAnchorAllocation();
        PresentAllocation presentAllocation = anchorAllocation.getNextPresentAllocation();
        Point[][] ground = new Point[SLEIGH_X][SLEIGH_Y];
        for (int i = 0; i < SLEIGH_X; i++) {
            for (int j = 0; j < SLEIGH_Y; j++) {
                ground[i][j] = new Point(i, j);
            }
        }
        SortedSet<Point> cornerSet = new TreeSet<Point>();
        Point gridCorner = ground[0][0];
        addCorner(cornerSet, gridCorner);
int count = 0;
        while (presentAllocation != null) {
            int z = findMinimalZ(cornerSet);
            Point winner = null;
            while (winner == null) {
                z++;
                for (Point corner : cornerSet) {
                    if (corner.z > z) {
                        continue;
                    }
                    if (fits(ground, presentAllocation, corner.x, corner.y, z)) {
                        winner = corner;
                        break;
                    }
                }
            }
            place(ground, cornerSet, presentAllocation, winner.x, winner.y, z);
            if (assertMode) {
                validateGround(ground, cornerSet);
            }
System.out.println(count + " winner found.");
            presentAllocation = presentAllocation.getNextPresentAllocation();
count++;
        }
        return SimpleScore.valueOf(-2 * findMaximalZ(cornerSet));
    }

    private void validateGround(Point[][] ground, SortedSet<Point> cornerSet) {
        for (Point[] points : ground) {
            for (Point point : points) {
                if (point.cornerMark && !cornerSet.contains(point)) {
                    throw new IllegalStateException("Point " + point + " : " + point.isCorner(ground));
                }
            }
        }
    }

    protected int findMinimalZ(SortedSet<Point> cornerSet) {
        int min = Integer.MAX_VALUE;
        for (Point corner : cornerSet) {
            if (corner.z < min) {
                min = corner.z;
            }
        }
        return min;
    }

    protected int findMaximalZ(SortedSet<Point> cornerSet) {
        int max = Integer.MIN_VALUE;
        for (Point corner : cornerSet) {
            if (corner.z > max) {
                max = corner.z;
            }
        }
        return max;
    }

    protected boolean fits(Point[][] ground, PresentAllocation presentAllocation, int xStart, int yStart, int z) {
        if (ground[xStart][yStart].z > z) {
            return false;
        }
        int xEnd = xStart + presentAllocation.getXLength();
        if (xEnd > ground.length) {
            return false;
        }
        int yEnd = yStart + presentAllocation.getYLength();
        if (yEnd > ground[xStart].length) {
            return false;
        }
        // Quick false of the start corner outwards
        if (!fitsYLine(ground, xStart, yStart, z, yEnd)) {
            return false;
        }
        if (!fitsXLine(ground, xStart, yStart, z, xEnd)) {
            return false;
        }
        // Remaining false of the sides inwards
        for (int x = xStart + 1; x < xEnd; x++) {
            if (!fitsYLine(ground, x, yStart, z, yEnd)) {
                return false;
            }
        }
        for (int y = yStart + 1; y < yEnd; y++) {
            if (!fitsXLine(ground, xStart, y, z, xEnd)) {
                return false;
            }
        }
        return true;
    }

    protected boolean fitsXLine(Point[][] ground, int x, int y, int z, int xEnd) {
        Point point = ground[x][y];
        while (true) {
            if (point.x >= point.xSpaceEnd) { // TODO uncomment me
                throw new IllegalStateException();
            }
            if (xEnd <= point.xSpaceEnd) {
                return true;
            }
            // OutOfBoundsException only if ground is corrupted
            point = ground[point.xSpaceEnd][y];
            if (point.z > z) {
                return false;
            }
        }
    }

    protected boolean fitsYLine(Point[][] ground, int x, int y, int z, int yEnd) {
        Point point = ground[x][y];
        while (true) {
            if (point.y >= point.ySpaceEnd) { // TODO uncomment me
                throw new IllegalStateException();
            }
            if (yEnd <= point.ySpaceEnd) {
                return true;
            }
            // OutOfBoundsException only if ground is corrupted
            point = ground[x][point.ySpaceEnd];
            if (point.z > z) {
                return false;
            }
        }
    }

    protected void place(Point[][] ground, SortedSet<Point> cornerSet, PresentAllocation presentAllocation, int xStart, int yStart, int zStart) {
        int xEnd = xStart + presentAllocation.getXLength();
        int yEnd = yStart + presentAllocation.getYLength();
        int zEnd = zStart + presentAllocation.getZLength();

        for (int x = xStart; x < xEnd; x++) {
            int ySpaceEnd = findYPlaceEnd(ground, x, yEnd, zEnd);
            for (int y = yStart; y < yEnd; y++) {
                Point point = ground[x][y];
                point.z = zEnd;
                point.ySpaceEnd = ySpaceEnd;
                if (point.cornerMark) {
                    removeCorner(cornerSet, point);
                }
            }
            backwardsCorrectY(ground, cornerSet, x, yStart, zEnd);
        }
        for (int y = yStart; y < yEnd; y++) {
            int xSpaceEnd = findXPlaceEnd(ground, xEnd, y, zEnd);
            for (int x = xStart; x < xEnd; x++) {
                ground[x][y].xSpaceEnd = xSpaceEnd;
            }
            backwardsCorrectX(ground, cornerSet, xStart, y, zEnd);
        }
        // Add corners
        Point basePoint = ground[xStart][yStart];
        if (basePoint.isCorner(ground)) {
            addCorner(cornerSet, basePoint);
        }
        if (yEnd < ground[0].length) {
            for (int x = xEnd - 1; x >= 0; x--) {
                Point point = ground[x][yEnd];
                if (point.z >= zEnd && x < xStart) {
                    break;
                }
                if (!point.cornerMark && point.isCorner(ground)) {
                    addCorner(cornerSet, point);
                }
            }
        }
        if (xEnd < ground.length) {
            for (int y = yEnd - 1; y >= 0; y--) {
                Point point = ground[xEnd][y];
                if (point.z >= zEnd && y < yStart) {
                    break;
                }
                if (!point.cornerMark && point.isCorner(ground)) {
                    addCorner(cornerSet, point);
                }
            }
        }
    }

    private void backwardsCorrectX(Point[][] ground, SortedSet<Point> cornerSet, int xEnd, int y, int zEnd) {
        for (int x = xEnd - 1; x >= 0; x--) {
            Point point = ground[x][y];
            if (point.z >= zEnd) {
                break;
            }
            if (point.xSpaceEnd > xEnd) {
                point.xSpaceEnd = xEnd;
            }
            if (point.cornerMark && !point.isCornerY(ground)) {
                removeCorner(cornerSet, point);
            }
        }
    }

    private void backwardsCorrectY(Point[][] ground, SortedSet<Point> cornerSet, int x, int yEnd, int zEnd) {
        for (int y = yEnd - 1; y >= 0; y--) {
            Point point = ground[x][y];
            if (point.z >= zEnd) {
                break;
            }
            if (point.ySpaceEnd > yEnd) {
                point.ySpaceEnd = yEnd;
            }
            if (point.cornerMark && !point.isCornerX(ground)) {
                removeCorner(cornerSet, point);
            }
        }
    }

    protected int findXPlaceEnd(Point[][] ground, int x, int y, int z) {
        int xSpaceEnd = x;
        while (xSpaceEnd < ground.length) {
            Point point = ground[xSpaceEnd][y];
            if (point.z > z) {
                break;
            }
            xSpaceEnd = point.xSpaceEnd;
        }
        return xSpaceEnd;
    }

    protected int findYPlaceEnd(Point[][] ground, int x, int y, int z) {
        int ySpaceEnd = y;
        while (ySpaceEnd < ground[x].length) {
            Point point = ground[x][ySpaceEnd];
            if (point.z > z) {
                break;
            }
            ySpaceEnd = point.ySpaceEnd;
        }
        return ySpaceEnd;
    }

    private void addCorner(SortedSet<Point> cornerSet, Point point) {
        point.cornerMark = true;
        boolean added = cornerSet.add(point);
        if (!added) {
            throw new IllegalStateException("Point (" + point + ") not added.");
        }
    }

    private void removeCorner(SortedSet<Point> cornerSet, Point point) {
        point.cornerMark = false;
        boolean removed = cornerSet.remove(point);
        if (!removed) {
            throw new IllegalStateException("Point (" + point + ") not removed.");
        }
    }

    protected void printGround(Point[][] ground) {
        System.out.println("z");
        for (int y = 0; y < ground.length; y++) {
            for (int x = 0; x < ground[y].length; x++) {
                System.out.print(ground[x][y].z + "\t");
            }
            System.out.println("");
        }
        System.out.println("xSpaceEnd");
        for (int y = 0; y < ground.length; y++) {
            for (int x = 0; x < ground[y].length; x++) {
                System.out.print(ground[x][y].xSpaceEnd + "\t");
            }
            System.out.println("");
        }
        System.out.println("ySpaceEnd");
        for (int y = 0; y < ground.length; y++) {
            for (int x = 0; x < ground[y].length; x++) {
                System.out.print(ground[x][y].ySpaceEnd + "\t");
            }
            System.out.println("");
        }
    }

    protected static class Point implements Comparable<Point> {

        public int x;
        public int y;

        // Changes
        public int z;
        public int xSpaceEnd;
        public int ySpaceEnd;
        public boolean cornerMark;

        protected Point(int x, int y) {
            this.x = x;
            this.y = y;
            z = 0;
            xSpaceEnd = SLEIGH_X;
            ySpaceEnd = SLEIGH_Y;
            cornerMark = false;
        }

        @Override
        public int compareTo(Point other) {
            if (y < other.y) {
                return -1;
            } else if (y > other.y) {
                return 1;
            } else {
                if (x < other.x) {
                    return -1;
                } else if (x > other.x) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }

        public boolean isCorner(Point[][] ground) {
            return isCornerX(ground) && isCornerY(ground);
        }

        public boolean isCornerX(Point[][] ground) {
            if (x == 0) {
                return true;
            }
            Point previousPoint = ground[x - 1][y];
            return previousPoint.z > z || previousPoint.ySpaceEnd < ySpaceEnd;
        }

        public boolean isCornerY(Point[][] ground) {
            if (y == 0) {
                return true;
            }
            Point previousPoint = ground[x][y - 1];
            return previousPoint.z > z || previousPoint.xSpaceEnd < xSpaceEnd;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

}
