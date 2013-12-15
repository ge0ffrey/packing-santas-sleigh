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

package org.optaplannerdelirium.pss.domain;

import org.junit.Assert;
import org.junit.Test;

public class PresentAllocationTest {

    @Test
    public void AXBYCZ() {
        Present present = new Present();
        present.setA(3);
        present.setB(5);
        present.setC(7);
        PresentAllocation presentAllocation = new PresentAllocation();
        presentAllocation.setPresent(present);
        presentAllocation.setRotation(Rotation.AXBYCZ);
        Assert.assertEquals(3, presentAllocation.getX());
        Assert.assertEquals(5, presentAllocation.getY());
        Assert.assertEquals(7, presentAllocation.getZ());
    }

    @Test
    public void AXBZCY() {
        Present present = new Present();
        present.setA(3);
        present.setB(5);
        present.setC(7);
        PresentAllocation presentAllocation = new PresentAllocation();
        presentAllocation.setPresent(present);
        presentAllocation.setRotation(Rotation.AXBZCY);
        Assert.assertEquals(3, presentAllocation.getX());
        Assert.assertEquals(7, presentAllocation.getY());
        Assert.assertEquals(5, presentAllocation.getZ());
    }

    @Test
    public void AYBXCZ() {
        Present present = new Present();
        present.setA(3);
        present.setB(5);
        present.setC(7);
        PresentAllocation presentAllocation = new PresentAllocation();
        presentAllocation.setPresent(present);
        presentAllocation.setRotation(Rotation.AYBXCZ);
        Assert.assertEquals(5, presentAllocation.getX());
        Assert.assertEquals(3, presentAllocation.getY());
        Assert.assertEquals(7, presentAllocation.getZ());
    }

    @Test
    public void AYBZCX() {
        Present present = new Present();
        present.setA(3);
        present.setB(5);
        present.setC(7);
        PresentAllocation presentAllocation = new PresentAllocation();
        presentAllocation.setPresent(present);
        presentAllocation.setRotation(Rotation.AYBZCX);
        Assert.assertEquals(7, presentAllocation.getX());
        Assert.assertEquals(3, presentAllocation.getY());
        Assert.assertEquals(5, presentAllocation.getZ());
    }

    @Test
    public void AZBXCY() {
        Present present = new Present();
        present.setA(3);
        present.setB(5);
        present.setC(7);
        PresentAllocation presentAllocation = new PresentAllocation();
        presentAllocation.setPresent(present);
        presentAllocation.setRotation(Rotation.AZBXCY);
        Assert.assertEquals(5, presentAllocation.getX());
        Assert.assertEquals(7, presentAllocation.getY());
        Assert.assertEquals(3, presentAllocation.getZ());
    }

    @Test
    public void AZBYCX() {
        Present present = new Present();
        present.setA(3);
        present.setB(5);
        present.setC(7);
        PresentAllocation presentAllocation = new PresentAllocation();
        presentAllocation.setPresent(present);
        presentAllocation.setRotation(Rotation.AZBYCX);
        Assert.assertEquals(7, presentAllocation.getX());
        Assert.assertEquals(5, presentAllocation.getY());
        Assert.assertEquals(3, presentAllocation.getZ());
    }

}
