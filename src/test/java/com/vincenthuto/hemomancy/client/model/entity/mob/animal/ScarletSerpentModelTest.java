package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScarletSerpentModelTest {
    @Test
    void risesFlaresAndOpensTheJawWithoutLeavingTheNextRenderInAStrikePose() {
        ScarletSerpentModel model = new ScarletSerpentModel();
        ModelPart serpent = model.root().getChild("scarlet_serpent");
        ModelPart neck = serpent.getChild("raised_neck");
        ModelPart jaw = neck.getChild("head").getChild("lower_jaw");
        ModelPart collar = neck.getChild("hood_collar");
        ModelPart left = collar.getChild("left_hood_hinge");
        ModelPart right = collar.getChild("right_hood_hinge");
        ModelPart body = serpent.getChild("body_chain").getChild("body_01").getChild("body_02");

        model.pose(0, 0, 0, 0, 0, 0, 0);
        float lowNeck = neck.xRot;
        float closedJaw = jaw.xRot;
        float extendedBody = body.yRot;
        assertTrue(left.yRot < -1 && right.yRot > 1);

        model.pose(0, 0, 0, 0, 0, 1, 0);
        float warningJaw = jaw.xRot;
        assertEquals(0, left.yRot, .001F);
        assertEquals(0, right.yRot, .001F);
        assertTrue(neck.xRot < lowNeck);
        assertTrue(warningJaw > closedJaw);
        assertTrue(body.yRot > extendedBody + .4F);

        model.pose(0, 0, 0, 0, 0, 1, 1);
        assertTrue(jaw.xRot > warningJaw);
        assertTrue(neck.xRot > .15F);

        model.pose(0, 0, 0, 0, 0, 0, 0);
        assertEquals(lowNeck, neck.xRot, .001F);
        assertEquals(closedJaw, jaw.xRot, .001F);
        assertEquals(extendedBody, body.yRot, .001F);
    }
}
