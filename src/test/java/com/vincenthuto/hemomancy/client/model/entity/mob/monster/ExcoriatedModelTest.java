package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExcoriatedModelTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");

    @Test
    void installedAtlasMatchesTheEditableAshHunter() throws Exception {
        var installed = ASSETS.resolve("models/entity/bbmodel/excoriated.bbmodel");
        var project = JsonParser.parseString(Files.readString(installed)).getAsJsonObject();
        assertEquals("Phlegethontic Ash Hunter", project.get("name").getAsString());
        String embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1)),
                Files.readAllBytes(ASSETS.resolve("textures/entity/excoriated/ash_hunter.png")));
    }

    @Test
    void compiledRigKeepsTheBowOnTheLeftAndCollapsesToOneStretchableStringPerLimb() {
        var model = new ExcoriatedModel();
        var root = model.root();
        assertEquals(198, root.getAllParts().mapToInt(part -> part.cubes.size()).sum());
        var body = root.getChild("horse_body");
        var torso = body.getChild("human_torso");
        assertTrue(torso.getChild("left_arm").x > 0);
        var bow = torso.getChild("left_arm").getChild("left_forearm").getChild("fused_bow");
        var string = bow.getChild("bowstring");
        assertTrue(string.hasChild("upper_string"));
        assertTrue(string.hasChild("lower_string"));
    }

    @Test
    void drawReachesTheAuthoredAimAndReleaseHidesTheHarpoon() {
        var model = new ExcoriatedModel();
        var body = model.root().getChild("horse_body");
        var torso = body.getChild("human_torso");
        var left = torso.getChild("left_arm");
        var forearm = left.getChild("left_forearm");
        var bow = forearm.getChild("fused_bow");
        var arrow = bow.getChild("harpoon");
        var right = torso.getChild("right_arm");
        var rightForearm = right.getChild("right_forearm");
        var hand = rightForearm.getChild("right_hand");

        model.pose(0, 0, 0, 0, 0, ExcoriatedEntity.IDLE, 0, false, false, 0);
        Vector3f idle = point(left, forearm, bow);
        assertFalse(arrow.visible);

        Vector3f previous = idle;
        for (int i = 0; i <= 80; i++) {
            model.pose(0, 0, 0, 0, 0, ExcoriatedEntity.DRAW, i / 4F, false, false, 0);
            Vector3f anchor = point(left, forearm, bow);
            assertTrue(anchor.x > 0 && anchor.isFinite());
            assertTrue(anchor.distance(previous) < 1.75F, "bow jumped at frame " + i);
            previous = anchor;
        }
        assertTrue(previous.y < idle.y - 6);
        assertTrue(arrow.visible);
        assertTrue(point(left, forearm, bow, arrow).distance(point(right, rightForearm, hand)) < 1.25F);

        var string = bow.getChild("bowstring");
        for (String name : new String[]{"upper_string", "lower_string"}) {
            var segment = string.getChild(name);
            assertTrue(segment.yScale > 0);
            assertEquals(0, point(new Vector3f(0, .5F, 0), segment).distance(point(arrow)), .01F);
        }
        model.pose(0, 0, 0, 0, 0, ExcoriatedEntity.FIRE, 0, false, false, 0);
        assertFalse(arrow.visible);
        model.pose(0, 0, 0, 0, 0, ExcoriatedEntity.FIRE, 8, false, false, 0);
        assertTrue(point(left, forearm, bow).distance(idle) < .001F);
    }

    @Test
    void wadingBendsTheHocksAndRearLeadsIntoBreath() {
        var model = new ExcoriatedModel();
        var body = model.root().getChild("horse_body");
        var left = body.getChild("front_left");
        var right = body.getChild("front_right");
        model.pose(1.5F, 1, 0, 0, 0, ExcoriatedEntity.IDLE, 0, false, false, 0);
        assertEquals(left.xRot, -right.xRot, .001F);
        var knee = left.getChild("front_left_lower");
        float dry = knee.xRot;
        model.pose(1.5F, 1, 0, 0, 0, ExcoriatedEntity.IDLE, 0, true, false, 0);
        assertTrue(knee.xRot < dry);
        model.pose(0, 0, 0, 0, 0, ExcoriatedEntity.REAR, 20, false, false, 0);
        float raised = body.xRot;
        assertTrue(raised < 0);
        model.pose(0, 0, 0, 0, 0, ExcoriatedEntity.BREATH, 0, false, false, 0);
        assertEquals(raised, body.xRot);
        model.pose(0, 0, 0, 0, 0, ExcoriatedEntity.BREATH, 12, false, false, 0);
        assertEquals(0F, body.xRot, .001F);
    }

    private static Vector3f point(ModelPart... chain) {
        return point(new Vector3f(), chain);
    }

    private static Vector3f point(Vector3f offset, ModelPart... chain) {
        var pose = new PoseStack();
        for (var part : chain) part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(offset.div(16)).mul(16);
    }
}
