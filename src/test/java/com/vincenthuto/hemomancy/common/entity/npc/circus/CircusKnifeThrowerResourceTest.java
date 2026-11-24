package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.client.model.entity.npc.CircusKnifeThrowerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusKnifeThrowerResourceTest {
    @Test
    void costumePropsFollowTheirAnimatedJoints() {
        var model = new CircusKnifeThrowerModel();
        assertEquals(12F, model.rightLeg.getInitialPose().y);
        assertEquals(12F, model.leftLeg.getInitialPose().y);
        assertTrue(hasGeometry(model.leftArm.getChild("left_elbow").getChild("left_hand").getChild("knife_fan")));
        assertTrue(hasGeometry(model.head.getChild("side_cowl")));
    }

    private static boolean hasGeometry(ModelPart part) {
        return part.getAllParts().anyMatch(child -> !child.isEmpty());
    }
}
