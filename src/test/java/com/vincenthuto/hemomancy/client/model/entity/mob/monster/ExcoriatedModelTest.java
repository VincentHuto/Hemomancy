package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExcoriatedModelTest {

    @Test
    void vesselGlowExcludesTheHaunches() {
        var model = new ExcoriatedModel(ExcoriatedModel.createBodyLayer().bakeRoot());
        var vertices = new CountingVertexConsumer();

        model.renderVessels(new PoseStack(), vertices, 0, 0, 0xFFFFFFFF);

        assertEquals(24, vertices.count);
    }

    @Test
    void latestBlockbenchExportGeometryIsBaked() {
        var body = ExcoriatedModel.createBodyLayer().bakeRoot()
                .getChild("root").getChild("horse_body");
        var frontLeftHoof = body.getChild("front_left")
                .getChild("front_left_lower").getChild("front_left_hoof");
        var abdomen = body.getChild("human_torso").getChild("human_abdomen_r1").cubes.getFirst();

        assertAll(
                () -> assertEquals(59, body.getAllParts().mapToInt(part -> part.cubes.size()).sum()),
                () -> assertEquals(8.9F, frontLeftHoof.y),
                () -> assertEquals(-1F, frontLeftHoof.z),
                () -> assertEquals(3F, abdomen.maxZ - abdomen.minZ));
    }

    @Test
    void blockbenchExportPivotsArePreserved() {
        var body = ExcoriatedModel.createBodyLayer().bakeRoot()
                .getChild("root").getChild("horse_body");
        var frontLeft = body.getChild("front_left");
        var frontRight = body.getChild("front_right");
        var hindLeft = body.getChild("hind_left");
        var hindRight = body.getChild("hind_right");
        var torso = body.getChild("human_torso");

        assertAll(
                () -> assertEquals(4F, frontLeft.x),
                () -> assertEquals(.5F, frontLeft.getChild("front_left_lower").x),
                () -> assertEquals(-4F, frontRight.x),
                () -> assertEquals(-.5F, frontRight.getChild("front_right_lower").x),
                () -> assertEquals(3.5F, hindLeft.x),
                () -> assertEquals(-3.5F, hindRight.x),
                () -> assertEquals(4F, torso.getChild("left_arm").x),
                () -> assertEquals(-4F, torso.getChild("right_arm").x));
    }

    @Test
    void approvedTextureAndExportedCubesAreInstalled() throws Exception {
        Path assets = Path.of("src/main/resources/assets/hemomancy");
        Path texturePath = assets.resolve("textures/entity/excoriated/excoriated.png");
        assertTrue(Files.isRegularFile(texturePath));
        var texture = ImageIO.read(texturePath.toFile());
        assertEquals(256, texture.getWidth());
        assertEquals(256, texture.getHeight());
        var layer = ExcoriatedModel.createBodyLayer().bakeRoot();
        assertEquals(59, layer.getAllParts().mapToInt(part -> part.cubes.size()).sum());
        assertDoesNotThrow(() -> new ExcoriatedModel(layer));
    }

    @Test
    void drawingHandFollowsStringWithoutForearmCrossingChest() {
        var layer = ExcoriatedModel.createBodyLayer().bakeRoot();
        var model = new ExcoriatedModel(layer);
        var torso = layer.getChild("root").getChild("horse_body").getChild("human_torso");
        var right = torso.getChild("right_arm");
        var forearm = right.getChild("right_forearm");
        var left = torso.getChild("left_arm");
        var leftForearm = left.getChild("left_forearm");
        var bow = leftForearm.getChild("fused_bow");
        var string = bow.getChild("bowstring");
        float chestFront = zBounds(torso.getChild("human_ribcage_r1"))[0];
        for (int frame = 0; frame <= 40; frame++) {
            layer.getAllParts().forEach(ModelPart::resetPose);
            model.animateDraw(frame / 40F);
            Vector3f hand = origin(right, forearm, forearm.getChild("right_hand"));
            Vector3f nock = origin(left, leftForearm, bow, string, string.getChild("arrow"));
            assertEquals(.35F, hand.x - nock.x, .002F, "hand X at frame " + frame);
            assertEquals(0, hand.y - nock.y, .002F, "hand Y at frame " + frame);
            assertEquals(1, hand.z - nock.z, .002F, "hand Z at frame " + frame);
            assertTrue(zBounds(right, forearm, forearm.getChild("right_drawing_forearm_r1"))[1] < chestFront,
                    "Forearm must remain in front of the ribcage at frame " + frame);
        }
    }

    private static Vector3f origin(ModelPart... chain) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : chain) part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(new Vector3f()).mul(16);
    }

    @Test
    void idleArmsHangOutsideTheBodyWithHandAndBowNearTheGround() {
        var layer = ExcoriatedModel.createBodyLayer().bakeRoot();
        var model = new ExcoriatedModel(layer);
        var root = layer.getChild("root");
        var body = root.getChild("horse_body");
        var torso = body.getChild("human_torso");
        var left = torso.getChild("left_arm");
        var leftFore = left.getChild("left_forearm");
        var right = torso.getChild("right_arm");
        var rightFore = right.getChild("right_forearm");
        for (int age = 0; age <= 200; age += 5) {
            layer.getAllParts().forEach(ModelPart::resetPose);
            model.animateArms(0, 0, age, age * .6F, .5F);
            Vector3f hand = origin(root, body, torso, right, rightFore, rightFore.getChild("right_hand"));
            Vector3f bow = origin(root, body, torso, left, leftFore, leftFore.getChild("fused_bow"));
            assertTrue(hand.x < -6 && bow.x > 6,
                    "Resting limbs must stay outside the horse's flanks: hand=" + hand + ", bow=" + bow);
            assertTrue(hand.y > 17 && hand.y < 25, "Hanging hand must reach ground height: " + hand);
            assertTrue(bow.y > 16 && bow.y < 24, "Bow must drag close to the ground: " + bow);
            var bowPart = leftFore.getChild("fused_bow");
            float tipHeight = axisBounds(1, root, body, torso, left, leftFore, bowPart, bowPart.getChild("bow_lower_3_r1"))[1];
            assertTrue(tipHeight > 22 && tipHeight < 24.8F, "Trailing bow tip must skim the ground: " + tipHeight);
            assertEquals(1, left.xScale * left.getChild("left_upper_arm_r1").xScale, .0001F);
            assertEquals(1, left.xScale * leftFore.getChild("fused_bow").xScale, .0001F);
        }
    }

    @Test
    void raiseAndLowerTransitionsJoinIdleAndAimingWithoutSnapping() {
        assertEquals(0, ExcoriatedModel.armReadiness(0, 0));
        assertEquals(0, ExcoriatedModel.armReadiness(1, 0));
        assertEquals(1, ExcoriatedModel.armReadiness(1, 8));
        assertEquals(1, ExcoriatedModel.armReadiness(2, 0));
        assertEquals(0, ExcoriatedModel.armReadiness(2, 8));
        var layer = ExcoriatedModel.createBodyLayer().bakeRoot();
        var model = new ExcoriatedModel(layer);
        var torso = layer.getChild("root").getChild("horse_body").getChild("human_torso");
        var left = torso.getChild("left_arm");
        var fore = left.getChild("left_forearm");
        Vector3f previous = null;
        for (int frame = 0; frame <= 160; frame++) {
            layer.getAllParts().forEach(ModelPart::resetPose);
            float time = frame / 20F;
            model.animateArms(ExcoriatedModel.armReadiness(1, time), time / 20F, 0, 0, 0);
            Vector3f bow = origin(left, fore, fore.getChild("fused_bow"));
            assertTrue(bow.isFinite());
            if (previous != null) assertTrue(previous.distance(bow) < 1.5F, "Bow jumped between transition frames");
            previous = bow;
        }
        assertEquals(1, left.xScale);
        assertEquals(0, left.xRot, .0001F);
        assertEquals(0, fore.xRot, .0001F);
    }

    private static float[] zBounds(ModelPart... chain) {
        return axisBounds(2, chain);
    }

    private static float[] axisBounds(int axis, ModelPart... chain) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : chain) part.translateAndRotate(pose);
        float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
        for (var cube : chain[chain.length - 1].cubes) {
            for (float x : new float[]{cube.minX, cube.maxX})
                for (float y : new float[]{cube.minY, cube.maxY})
                    for (float z : new float[]{cube.minZ, cube.maxZ}) {
                        float transformed = pose.last().pose().transformPosition(new Vector3f(x, y, z).div(16)).get(axis) * 16;
                        min = Math.min(min, transformed);
                        max = Math.max(max, transformed);
                    }
        }
        return new float[]{min, max};
    }

    private static final class CountingVertexConsumer implements VertexConsumer {
        private int count;

        @Override public VertexConsumer addVertex(float x, float y, float z) { count++; return this; }
        @Override public VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
        @Override public VertexConsumer setUv(float u, float v) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
