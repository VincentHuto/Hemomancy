package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OutpouringAnchorsTest {
    @Test void yawZeroFacesSouthWithTheRightHandWest() {
        double[] out = new double[3];
        double[] at = OutpouringAnchors.toWorld(new double[]{1, 0, 0}, new double[]{0, 0, 1}, 1, 0, 0, 0, 0, 1, false, out);
        assertEquals(-1, at[0], 1e-9, "a player facing +z has their right hand at -x");
        assertEquals(1, out[2], 1e-9, "forward is +z at yaw 0");
    }

    @Test void yawNinetyFacesWest() {
        double[] out = new double[3];
        OutpouringAnchors.toWorld(new double[]{0, 0, 0}, new double[]{0, 0, 1}, 1, 0, 0, 0, 90, 1, false, out);
        assertEquals(-1, out[0], 1e-9);
    }

    @Test void crouchLowersAndScaleShrinks() {
        double[] out = new double[3];
        double[] standing = OutpouringAnchors.toWorld(new double[]{0, 1.5, 0}, new double[]{0, 0, 1}, 1, 0, 64, 0, 0, 1, false, out);
        double[] crouched = OutpouringAnchors.toWorld(new double[]{0, 1.5, 0}, new double[]{0, 0, 1}, 1, 0, 64, 0, 0, 1, true, out);
        double[] small = OutpouringAnchors.toWorld(new double[]{0, 1.5, 0}, new double[]{0, 0, 1}, 1, 0, 64, 0, 0, .5, false, out);
        assertEquals(65.5, standing[1], 1e-9);
        assertEquals(65.2, crouched[1], 1e-9);
        assertEquals(64.75, small[1], 1e-9);
    }

    @Test void sectionsHaveTheirExits() {
        assertEquals(2, OutpouringAnchors.local(EnumVeinSections.HEAD, 0).length / 3, "eyes only at Humilis");
        assertEquals(5, OutpouringAnchors.local(EnumVeinSections.HEAD, 2).length / 3, "eyes, nose and ears from Summa");
        assertEquals(1, OutpouringAnchors.local(EnumVeinSections.HEART, 4).length / 3);
        assertEquals(4, OutpouringAnchors.local(EnumVeinSections.BODY, 4).length / 3);
        assertEquals(4, OutpouringAnchors.local(EnumVeinSections.LEGS, 4).length / 3);
        double[] heart = OutpouringAnchors.local(EnumVeinSections.HEART, 2);
        assertTrue(heart[1] > 1 && heart[2] > 0, "the heart opens on the front of the chest");
    }

    @Test void legBloodRunsDown() {
        double[] outward = OutpouringAnchors.outward(EnumVeinSections.LEGS, 4);
        for (int i = 0; i < outward.length / 3; i++) assertTrue(outward[i * 3 + 1] < 0);
    }
}
