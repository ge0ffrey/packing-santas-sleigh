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

        PssScoreCalculator.Point[][] ground = createGroundFromTransposedZ(new int[][]{
                {1, 0, 0, 2, 0},
                {1, 0, 0, 1, 0},
                {0, 0, 0, 3, 3},
                {0, 0, 1, 4, 3},
                {0, 0, 0, 1, 2},
        });
        scoreCalculator.printGround(ground);

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

    private PssScoreCalculator.Point[][] createGroundFromTransposedZ(int[][] zGroundTransposed) {
        int[][] zGround = new int[zGroundTransposed[0].length][zGroundTransposed.length];
        // Undo transpose
        for (int y = 0; y < zGroundTransposed.length; y++) {
            for (int x = 0; x < zGroundTransposed[y].length; x++) {
                zGround[x][y] = zGroundTransposed[y][x];
            }
        }

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

}
