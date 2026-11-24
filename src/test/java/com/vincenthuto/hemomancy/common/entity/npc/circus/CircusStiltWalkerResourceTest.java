package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.client.model.entity.npc.CircusStiltWalkerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusStiltWalkerResourceTest {
    @Test
    void costumePropsFollowTheirAnimatedJoints() {
        var model = new CircusStiltWalkerModel();
        assertEquals(-9F, model.rightLeg.getInitialPose().y);
        assertEquals(-9F, model.leftLeg.getInitialPose().y);
        assertTrue(hasGeometry(model.rightLeg.getChild("right_calf").getChild("right_ankle")));
        assertTrue(hasGeometry(model.leftLeg.getChild("left_calf").getChild("left_ankle")));
        assertTrue(hasGeometry(model.head.getChild("top_hat").getChild("feather")));
    }

    private static boolean hasGeometry(ModelPart part) {
        return part.getAllParts().anyMatch(child -> !child.isEmpty());
    }
}
