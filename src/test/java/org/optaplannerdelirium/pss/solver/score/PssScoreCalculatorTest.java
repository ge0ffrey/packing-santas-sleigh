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

import org.junit.Test;
import org.optaplannerdelirium.pss.domain.PresentAllocation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PssScoreCalculatorTest {

    @Test
    public void fits() {
        PssScoreCalculator scoreCalculator = new PssScoreCalculator();
        PresentAllocation presentAllocation = mock(PresentAllocation.class);
        when(presentAllocation.getXLength()).thenReturn(2);
        when(presentAllocation.getYLength()).thenReturn(3);
        when(presentAllocation.getZLength()).thenReturn(7);

        PssScoreCalculator.Point[][] ground = createGround(transpose(new int[][]{
                {1, 0, 0, 2, 0},
                {1, 0, 0, 1, 0},
                {0, 0, 0, 3, 3},
                {0, 0, 1, 4, 3},
                {0, 0, 0, 1, 2},
        }));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 0, 0, 0));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 0, 0, 1));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 0, 0, 2));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 0, 1, 0));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 0, 2, 0));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 0, 3, 0));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 0, 4, 0));


        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 1, 0, 0));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 1, 0, 1));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 1, 0, 2));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 1, 1, 0));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 1, 1, 1));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 1, 2, 0));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 1, 2, 1));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 1, 3, 0));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 1, 3, 1));


        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 2, 0, 0));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 2, 0, 1));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 2, 0, 2));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 2, 0, 3));


        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 0, 0));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 0, 1));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 0, 2));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 3, 0, 3));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 3, 0, 4));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 1, 0));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 1, 1));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 1, 2));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 1, 3));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 3, 1, 4));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 2, 3));
        assertEquals(true, scoreCalculator.fits(ground, presentAllocation, 3, 2, 4));

        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 3, 3));
        assertEquals(false, scoreCalculator.fits(ground, presentAllocation, 3, 3, 4));
    }

    @Test
    public void place_0_0() {
        PssScoreCalculator scoreCalculator = new PssScoreCalculator();
        PresentAllocation presentAllocation = mock(PresentAllocation.class);
        when(presentAllocation.getXLength()).thenReturn(2);
        when(presentAllocation.getYLength()).thenReturn(3);
        when(presentAllocation.getZLength()).thenReturn(7);

        PssScoreCalculator.Point[][] ground = createGround(transpose(new int[][]{
                {1, 0, 0, 2, 0},
                {1, 0, 0, 1, 0},
                {0, 0, 0, 3, 3},
                {0, 0, 1, 4, 3},
                {0, 0, 0, 1, 2},
        }));
        SortedSet<PssScoreCalculator.Point> cornerSet = mock(SortedSet.class);
        scoreCalculator.place(ground, cornerSet, presentAllocation, 0, 0, 1);
        assertPlacement(transpose(new int[][]{
                {8, 8, 0, 2, 0},
                {8, 8, 0, 1, 0},
                {8, 8, 0, 3, 3},
                {0, 0, 1, 4, 3},
                {0, 0, 0, 1, 2},
        }), transpose(new int[][]{
                {5, 5, 3, 5, 5},
                {5, 5, 3, 5, 5},
                {5, 5, 3, 5, 5},
                {2, 2, 3, 5, 5},
                {3, 3, 3, 4, 5},
        }), transpose(new int[][]{
                {5, 5, 3, 2, 2},
                {5, 5, 3, 2, 2},
                {5, 5, 3, 3, 5},
                {5, 5, 5, 5, 5},
                {5, 5, 5, 5, 5},
        }), ground);
    }

    @Test
    public void place_1_0() {
        PssScoreCalculator scoreCalculator = new PssScoreCalculator();
        PresentAllocation presentAllocation = mock(PresentAllocation.class);
        when(presentAllocation.getXLength()).thenReturn(2);
        when(presentAllocation.getYLength()).thenReturn(3);
        when(presentAllocation.getZLength()).thenReturn(7);

        PssScoreCalculator.Point[][] ground = createGround(transpose(new int[][]{
                {1, 0, 0, 2, 0},
                {9, 0, 0, 9, 0},
                {0, 0, 0, 3, 3},
                {0, 0, 1, 4, 3},
                {0, 0, 0, 1, 2},
        }));
        SortedSet<PssScoreCalculator.Point> cornerSet = mock(SortedSet.class);
        scoreCalculator.place(ground, cornerSet, presentAllocation, 1, 0, 0);
        assertPlacement(transpose(new int[][]{
                {1, 7, 7, 2, 0},
                {9, 7, 7, 9, 0},
                {0, 7, 7, 3, 3},
                {0, 0, 1, 4, 3},
                {0, 0, 0, 1, 2},
        }), transpose(new int[][]{
                {1, 5, 5, 5, 5},
                {5, 3, 3, 5, 5},
                {1, 5, 5, 5, 5},
                {2, 2, 3, 5, 5},
                {3, 3, 3, 4, 5},
        }), transpose(new int[][]{
                {1, 5, 5, 1, 2},
                {5, 5, 5, 5, 2},
                {5, 5, 5, 3, 5},
                {5, 5, 5, 5, 5},
                {5, 5, 5, 5, 5},
        }), ground);
    }

    @Test
    public void place_3_2() {
        PssScoreCalculator scoreCalculator = new PssScoreCalculator();
        PresentAllocation presentAllocation = mock(PresentAllocation.class);
        when(presentAllocation.getXLength()).thenReturn(2);
        when(presentAllocation.getYLength()).thenReturn(3);
        when(presentAllocation.getZLength()).thenReturn(7);

        PssScoreCalculator.Point[][] ground = createGround(transpose(new int[][]{
                {1, 0, 0, 2, 1},
                {9, 0, 0, 9, 0},
                {9, 0, 1, 3, 3},
                {1, 0, 2, 4, 3},
                {0, 0, 0, 1, 2},
        }));
        SortedSet<PssScoreCalculator.Point> cornerSet = mock(SortedSet.class);
        scoreCalculator.place(ground, cornerSet, presentAllocation, 3, 2, 4);
        assertPlacement(transpose(new int[][]{
                {1, 0, 0, 2, 1},
                {9, 0, 0, 9, 0},
                {9, 0, 1, 11, 11},
                {1, 0, 2, 11, 11},
                {0, 0, 0, 11, 11},
        }), transpose(new int[][]{
                {3, 3, 3, 5, 5},
                {5, 3, 3, 5, 5},
                {3, 2, 3, 5, 5},
                {2, 2, 3, 5, 5},
                {3, 3, 3, 5, 5},
        }), transpose(new int[][]{
                {1, 5, 2, 1, 2},
                {5, 5, 2, 2, 2},
                {5, 5, 3, 5, 5},
                {5, 5, 5, 5, 5},
                {5, 5, 5, 5, 5},
        }), ground);
    }

    private void assertPlacement(int[][] zGround, int[][] xSpaceEndGround, int[][] ySpaceEndGround,
            PssScoreCalculator.Point[][] ground) {
        for (int x = 0; x < ground.length; x++) {
            for (int y = 0; y < ground[x].length; y++) {
                assertEquals(zGround[x][y], ground[x][y].z);
                assertEquals(xSpaceEndGround[x][y], ground[x][y].xSpaceEnd);
                assertEquals(ySpaceEndGround[x][y], ground[x][y].ySpaceEnd);
            }
        }
    }

    private PssScoreCalculator.Point[][] createGround(int[][] zGround) {

        PssScoreCalculator.Point[][] ground = new PssScoreCalculator.Point[zGround.length][zGround[0].length];
        for (int x = 0; x < zGround.length; x++) {
            for (int y = 0; y < zGround[x].length; y++) {
                PssScoreCalculator.Point point = new PssScoreCalculator.Point(x, y);
                point.z = zGround[x][y];
                point.xSpaceEnd = x + 1;
                while(point.xSpaceEnd < zGround.length && zGround[point.xSpaceEnd][y] <= point.z) {
                    point.xSpaceEnd++;
                }
                point.ySpaceEnd = y + 1;
                while(point.ySpaceEnd < zGround[x].length && zGround[x][point.ySpaceEnd] <= point.z) {
                    point.ySpaceEnd++;
                }
                ground[x][y] = point;
            }
        }
        return ground;
    }

    private int[][] transpose(int[][] original) {
        int[][] transposed = new int[original[0].length][original.length];
        for (int i = 0; i < original.length; i++) {
            for (int j = 0; j < original[i].length; j++) {
                transposed[j][i] = original[i][j];
            }
        }
        return transposed;
    }

}
