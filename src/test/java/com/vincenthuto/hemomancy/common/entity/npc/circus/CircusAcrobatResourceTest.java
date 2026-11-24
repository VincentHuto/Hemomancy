package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.client.model.entity.npc.CircusAcrobatModel;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusAcrobatResourceTest {
    @Test
    void costumePropsFollowTheirAnimatedJoints() {
        var model = new CircusAcrobatModel();
        assertEquals(12F, model.rightLeg.getInitialPose().y);
        assertEquals(12F, model.leftLeg.getInitialPose().y);
        assertTrue(hasGeometry(model.body.getChild("sash")));
        assertTrue(hasGeometry(model.head.getChild("right_ribbon")));
        assertTrue(hasGeometry(model.head.getChild("left_ribbon")));
    }

    private static boolean hasGeometry(ModelPart part) {
        return part.getAllParts().anyMatch(child -> !child.isEmpty());
    }
}
