package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.client.model.entity.npc.CircusFireEaterModel;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusFireEaterResourceTest {
    @Test
    void costumePropsFollowTheirAnimatedJoints() {
        var model = new CircusFireEaterModel();
        assertEquals(12F, model.rightLeg.getInitialPose().y);
        assertEquals(12F, model.leftLeg.getInitialPose().y);
        assertTrue(hasGeometry(model.body.getChild("chest").getChild("gorget")));
        assertTrue(hasGeometry(model.rightArm.getChild("right_elbow").getChild("right_hand").getChild("firebrand")));
        assertTrue(hasGeometry(model.head.getChild("cowl_tail")));
    }

    private static boolean hasGeometry(ModelPart part) {
        return part.getAllParts().anyMatch(child -> !child.isEmpty());
    }
}
