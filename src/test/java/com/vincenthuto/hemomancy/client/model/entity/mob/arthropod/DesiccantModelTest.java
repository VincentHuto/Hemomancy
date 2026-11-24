package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesiccantModelTest {
    @Test
    void redesignedRigHasEightLegsAndAnArticulatedTelson() {
        var body = DesiccantModel.createBodyLayer().bakeRoot().getChild("desiccant");
        for (String side : new String[]{"left", "right"}) {
            for (int leg = 1; leg <= 4; leg++) {
                assertTrue(body.hasChild(side + "_leg_" + leg));
            }
        }

        var tail = body.getChild("metasoma");
        for (int segment = 1; segment <= 5; segment++) {
            tail = tail.getChild("tail_articulation_" + segment);
        }
        assertTrue(tail.getChild("telson").hasChild("venom_bulb"));
    }

    @Test
    void stingAndWalkingResetWhenTheModelIsReused() {
        var root = DesiccantModel.createBodyLayer().bakeRoot();
        var body = root.getChild("desiccant");
        var model = new DesiccantModel(root);
        var left = body.getChild("left_leg_1");
        var right = body.getChild("right_leg_1");
        var tail = body.getChild("metasoma");
        for (int segment = 1; segment <= 5; segment++) {
            tail = tail.getChild("tail_articulation_" + segment);
        }
        var telson = tail.getChild("telson");
        var bulb = telson.getChild("venom_bulb");

        model.pose(0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.3F);
        assertTrue(left.yRot * right.yRot > 0);
        assertTrue(Math.abs(left.zRot) > Math.abs(right.zRot));
        assertTrue(tail.xRot > 0.10F);
        assertTrue(telson.xRot > 0.0F);
        assertTrue(bulb.xScale > 1.0F);
        assertEquals(1.0F, telson.xScale);

        model.pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        assertEquals(0.0F, left.yRot, 0.0001F);
        assertEquals(0.0F, left.zRot, 0.0001F);
        assertEquals(0.0F, telson.xRot, 0.0001F);
        assertEquals(1.0F, bulb.xScale);
        assertEquals(1.0F, bulb.yScale);
        assertEquals(1.0F, bulb.zScale);
    }
}
