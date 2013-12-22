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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
        Point[][] ground = new Point[SLEIGH_X][SLEIGH_Y];
        for (int i = 0; i < SLEIGH_X; i++) {
            for (int j = 0; j < SLEIGH_Y; j++) {
                ground[i][j] = new Point(i, j);
            }
        }
        List<Point> cornerList = new ArrayList<Point>(SLEIGH_X * SLEIGH_Y);
        cornerList.add(ground[0][0]);
        while (presentAllocation != null) {
            for (Iterator<Point> it = cornerList.iterator(); it.hasNext(); ) {
                Point corner = it.next();


//                ggg
//                if (fits(presentAllocation, corner, ground)) {
//                    int xEnd = corner.x + presentAllocation.getXLength();
//                    int yEnd = corner.y + presentAllocation.getYLength();
//                    int zEnd = corner.z + presentAllocation.getZLength();
//                    for (int x = corner.x; x < xEnd; x++) {
//                        for (int y = corner.y; y < yEnd; y++) {
//                            ground[x][y] = zEnd;
//                        }
//                    }
//                    it.remove();
//                    cornerList.add(new Point(corner.x, corner.y, zEnd));
//                    // todo add new cornerList
//
//                    break;
//                }
            }
            Collections.sort(cornerList);
            presentAllocation = presentAllocation.getNextPresentAllocation();
        }
        int maxZ = cornerList.listIterator().previous().z;
        return SimpleScore.valueOf(-2 * maxZ);
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

    protected void place(Point[][] ground, PresentAllocation presentAllocation, int xStart, int yStart, int zStart) {
        int xEnd = xStart + presentAllocation.getXLength();
        int yEnd = yStart + presentAllocation.getYLength();
        int zEnd = zStart + presentAllocation.getZLength();

        for (int x = xStart; x < xEnd; x++) {
            int ySpaceEnd = findYPlaceEnd(ground, x, yEnd, zEnd);
            for (int y = yStart; y < yEnd; y++) {
                ground[x][y].z = zEnd;
                ground[x][y].ySpaceEnd = ySpaceEnd;
            }
            backwardsCorrectY(ground, x, yStart, zEnd);
        }
        for (int y = yStart; y < yEnd; y++) {
            int xSpaceEnd = findXPlaceEnd(ground, xEnd, y, zEnd);
            for (int x = xStart; x < xEnd; x++) {
                ground[x][y].xSpaceEnd = xSpaceEnd;
            }
            backwardsCorrectX(ground, xStart, y, zEnd);
        }
    }

    private void backwardsCorrectX(Point[][] ground, int xEnd, int y, int zEnd) {
        // TODO unoptimal
        for (int x = 0; x < xEnd; x++) {
            Point point = ground[x][y];
            if (point.xSpaceEnd > xEnd && point.z < zEnd) {
                point.xSpaceEnd = xEnd;
            }
        }
    }

    private void backwardsCorrectY(Point[][] ground, int x, int yEnd, int zEnd) {
        // TODO unoptimal
        for (int y = 0; y < yEnd; y++) {
            Point point = ground[x][y];
            if (point.ySpaceEnd > yEnd && point.z < zEnd) {
                point.ySpaceEnd = yEnd;
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

        protected Point(int x, int y) {
            this.x = x;
            this.y = y;
            z = 0;
            xSpaceEnd = SLEIGH_X;
            ySpaceEnd = SLEIGH_Y;
        }

        @Override
        public int compareTo(Point other) {
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

//        @Override
//        public int hashCode() {
//            return z + y *2000 + x * 2000000;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            Point other = (Point) o;
//            return z == other.z && y == other.y && x == other.x;
//        }
    }

}
