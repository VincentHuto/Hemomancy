package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import java.nio.file.Files;
import java.util.Base64;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanguineRamModelTest {
    @Test
    void runtimeAtlasMatchesEditableProjectAndAllFourHornsFollowTheHead() throws Exception {
        var project = JsonParser.parseString(Files.readString(
                RepoPaths.ASSETS.resolve("models/entity/bbmodel/SanguineRamModel.bbmodel"))).getAsJsonObject();
        String embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1)),
                Files.readAllBytes(RepoPaths.ASSETS.resolve("textures/entity/sanguine_ram.png")));

        var head = new SanguineRamModel().root().getChild("body").getChild("head");
        for (String side : new String[]{"left", "right"}) {
            assertTrue(head.hasChild("straight_horn_" + side));
            assertTrue(head.hasChild("curved_horn_" + side));
        }
    }

    @Test
    void diagonalStrideAndHeadTrackingResetForTheNextEntity() {
        var model = new SanguineRamModel();
        var root = model.root();
        var head = root.getChild("body").getChild("head");
        var front = root.getChild("front_leg_left");
        var diagonal = root.getChild("hind_leg_right");
        var opposite = root.getChild("front_leg_right");
        float standingHeight = root.y;

        model.pose(0, 1, 20, 0, 35, -20);
        assertTrue(front.xRot > 0);
        assertEquals(front.xRot, diagonal.xRot, 0.0001F);
        assertEquals(-front.xRot, opposite.xRot, 0.0001F);
        assertTrue(head.yRot > 0);
        assertTrue(head.xRot < 0);
        assertTrue(root.y > standingHeight, "The ram must lower its hips as the legs swing out");

        model.pose(0, 0, 20, 0, 0, 0);
        assertEquals(0, front.xRot);
        assertEquals(0, diagonal.xRot);
        assertEquals(0, head.yRot);
        assertEquals(standingHeight, root.y);

        var body = root.getChild("body");
        float relaxedWidth = body.xScale;
        model.pose(0, 0, 32, 0, 0, 0);
        assertNotEquals(relaxedWidth, body.xScale);
    }
}
