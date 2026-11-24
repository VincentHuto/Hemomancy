package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.support.RepoPaths;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HematicBurrowerModelTest {
    @Test
    void installedTextureMatchesApprovedArt() throws Exception {
        assertArrayEquals(Files.readAllBytes(RepoPaths.ROOT.resolve("docs/art/hematic_burrower/hematic_burrower.png")),
                Files.readAllBytes(RepoPaths.ASSETS.resolve("textures/entity/hematic_burrower/model_hematic_burrower.png")));
    }

    @Test
    void bakedRigScurriesOnDiagonalPairsAndRakesWithAlternatingForepaws() {
        var model = new HematicBurrowerModel(HematicBurrowerModel.createBodyLayer().bakeRoot());
        var root = model.root();
        var left = root.getChild("left_forepaw");
        var right = root.getChild("right_forepaw");
        var rightHind = root.getChild("right_hindpaw");
        var head = root.getChild("body").getChild("head");
        float restingZ = left.z;

        model.pose(0, 1, 0, 20, 0, false);
        assertTrue(left.xRot > 0 && rightHind.xRot > 0);
        assertEquals(0, right.xRot);
        assertTrue(head.yRot > 0);

        model.pose(0, 0, 0, 20, 0, true);
        assertTrue(left.z < restingZ && right.z > restingZ);
        assertTrue(head.xRot > 0);
        assertEquals(0, head.yRot);

        model.pose(0, 0, (float) Math.PI / 1.05F, 0, 0, true);
        assertTrue(right.z < restingZ && left.z > restingZ);

        model.pose(0, 0, 0, 0, 0, false);
        assertEquals(restingZ, left.z);
        assertEquals(0, left.xRot);
        assertEquals(0, head.xRot);
    }
}

