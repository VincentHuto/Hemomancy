package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarbedUrchinModelTest {
    @Test
    void fullPuffExtendsTheAuthoredSpinesAndRestoresTheirBakedPose() {
        BarbedUrchinModel model = new BarbedUrchinModel();
        ModelPart body = model.root().getChild("barbed_urchin");
        ModelPart hinge = body.getChild("equatorial_spines").getChild("equatorial_spine_01")
                .getChild("equatorial_spine_01_spine_hinge");

        model.pose(0, 0, 0, 0);
        float restingReach = hinge.z;
        float restingPitch = hinge.xRot;
        assertTrue(restingReach < hinge.getInitialPose().z);
        assertTrue(restingPitch > hinge.getInitialPose().xRot);

        model.pose(1, 0, 0, 0);
        assertTrue(hinge.z > restingReach);
        assertTrue(hinge.z < hinge.getInitialPose().z);

        model.pose(2, 0, 0, 0);
        assertEquals(hinge.getInitialPose().z, hinge.z, 0.001F);
        assertEquals(hinge.getInitialPose().xRot, hinge.xRot, 0.001F);

        model.pose(0, 0, 0, 0);
        assertEquals(restingReach, hinge.z, 0.001F);
        assertEquals(restingPitch, hinge.xRot, 0.001F);
    }
}
