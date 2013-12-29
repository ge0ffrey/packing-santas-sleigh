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

class Point implements Comparable<Point> {

    public int x;
    public int y;

    // Changes
    public int z;
    public int xSpaceEnd;
    public int ySpaceEnd;
    public boolean cornerMark;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        z = 0;
        xSpaceEnd = PssSimpleScoreCalculator.SLEIGH_X;
        ySpaceEnd = PssSimpleScoreCalculator.SLEIGH_Y;
        cornerMark = false;
    }

    public Point(Point lockedPoint) {
        x = lockedPoint.x;
        y = lockedPoint.y;
        z = lockedPoint.z;
        xSpaceEnd = lockedPoint.xSpaceEnd;
        ySpaceEnd = lockedPoint.ySpaceEnd;
        cornerMark = lockedPoint.cornerMark;
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
        int previousX = x - 1;
        int searchY = y;
        while (searchY < ground[previousX].length) {
            Point searchPoint = ground[previousX][searchY];
            if (searchPoint.z > ground[x][searchY].z && searchPoint.z > z) {
                return true;
            }
            searchY = searchPoint.ySpaceEnd;
        }
        return false;
    }

    public boolean isCornerY(Point[][] ground) {
        if (y == 0) {
            return true;
        }
        int previousY = y - 1;
        int searchX = x;
        while (searchX < ground.length) {
            Point searchPoint = ground[searchX][previousY];
            if (searchPoint.z > ground[searchX][y].z && searchPoint.z > z) {
                return true;
            }
            searchX = searchPoint.xSpaceEnd;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    protected static void printGround(Point[][] ground) {
        printGround(ground, 0, 0, ground.length, ground[0].length);
    }

    protected static void printGround(Point[][] ground, int xStart, int xEnd, int yStart, int yEnd) {
        System.out.println("       z, xSpaceEnd, ySpaceEnd");
        System.out.print("       ");
        for (int x = xStart; x < xEnd; x++) {
            printNumber(x);
        }
        System.out.print("       ");
        for (int x = xStart; x < xEnd; x++) {
            printNumber(x);
        }
        System.out.print("       ");
        for (int x = xStart; x < xEnd; x++) {
            printNumber(x);
        }
        System.out.println("");
        System.out.println("       ----");
        for (int y = yEnd - 1; y >= yStart; y--) {
            printNumber(y);
            System.out.print(":");
            for (int x = xStart; x < xEnd; x++) {
                printNumber(ground[x][y].z);
            }
            System.out.print("       ");
            for (int x = xStart; x < xEnd; x++) {
                printNumber(ground[x][y].xSpaceEnd);
            }
            System.out.print("       ");
            for (int x = xStart; x < xEnd; x++) {
                printNumber(ground[x][y].ySpaceEnd);
            }
            System.out.println("");
        }
    }

    private static void printNumber(int number) {
        String s = Integer.toString(number);
        System.out.print(s);
        for (int i = s.length(); i < 6; i++) {
            System.out.print(" ");
        }
    }

}
