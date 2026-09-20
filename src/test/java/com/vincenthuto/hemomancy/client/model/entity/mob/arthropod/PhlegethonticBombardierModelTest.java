package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhlegethonticBombardierModelTest {
    @Test
    void latestBlockbenchGeometryAndArticulationHierarchyAreBaked() {
        ModelPart whole = PhlegethonticBombardierModel.createBodyLayer().bakeRoot().getChild("whole");
        ModelPart head = whole.getChild("head");
        ModelPart body = whole.getChild("body");
        ModelPart tail = body.getChild("tail");
        ModelPart tail4 = tail.getChild("tail2").getChild("tail3").getChild("tail4");

        assertAll(
                () -> assertEquals(59, whole.getAllParts().mapToInt(part -> part.cubes.size()).sum()),
                () -> assertEquals(3, tail4.cubes.size(),
                        "the supplied export simplifies the terminal abdomen to three cubes"),
                () -> assertNotNull(head.getChild("antennaL")),
                () -> assertNotNull(head.getChild("antennaR")),
                () -> assertNotNull(body.getChild("wingL")),
                () -> assertNotNull(body.getChild("wingR")),
                () -> assertNotNull(tail.getChild("tail2").getChild("tail3").getChild("tail4")),
                () -> assertNotNull(body.getChild("lLegF").getChild("flLeg2").getChild("flLeg3")),
                () -> assertNotNull(body.getChild("rLegB").getChild("flLeg12").getChild("flLeg13")));
    }

    @Test
    void bothEyesRenderMirroredGeometryAndTextureOnOppositeSidesOfHead() {
        ModelPart head = PhlegethonticBombardierModel.createBodyLayer().bakeRoot()
                .getChild("whole").getChild("head");
        assertTrue(head.hasChild("eyeL2"), "the latest Blockbench export includes the second eye");
        var left = new EyeVertices();
        var right = new EyeVertices();
        head.getChild("eyeL").render(new PoseStack(), left, 0, 0, -1);
        head.getChild("eyeL2").render(new PoseStack(), right, 0, 0, -1);

        assertEquals(24, left.vertices.size());
        assertEquals(24, right.vertices.size());
        for (EyeVertex vertex : left.vertices) {
            assertTrue(vertex.position.x > 0, "left eye must be on the positive-X side of the head");
            Vector3f mirrored = new Vector3f(vertex.position).mul(-1, 1, 1);
            assertTrue(right.vertices.stream().anyMatch(other ->
                            other.position.distance(mirrored) < .0001F
                                    && Math.abs(other.u - vertex.u) < .0001F
                                    && Math.abs(other.v - vertex.v) < .0001F),
                    "second eye must mirror the position and keep its matching texture coordinates");
            assertTrue(vertex.u >= 44F / 128 && vertex.u <= 50F / 128
                    && vertex.v >= 66F / 128 && vertex.v <= 70F / 128,
                    "both eyes must use the authored eye island on the native 128x128 atlas");
        }
    }

    @Test
    void blockbenchRootAndBodyPivotsArePreserved() {
        ModelPart whole = PhlegethonticBombardierModel.createBodyLayer().bakeRoot().getChild("whole");
        ModelPart head = whole.getChild("head");
        ModelPart body = whole.getChild("body");

        assertAll(
                () -> assertEquals(21.1238F, whole.y, .0001F),
                () -> assertEquals(-4.9992F, whole.z, .0001F),
                () -> assertEquals(-1.8762F, head.y, .0001F),
                () -> assertEquals(-2.9992F, head.z, .0001F),
                () -> assertEquals(1.8762F, body.y, .0001F),
                () -> assertEquals(2.9992F, body.z, .0001F));
    }

    @Test
    void sprayPoseCurlsNozzleOverBackAndFlaresWingsUpAndOut() {
        ModelPart root = PhlegethonticBombardierModel.createBodyLayer().bakeRoot();
        PhlegethonticBombardierModel model = new PhlegethonticBombardierModel(root);
        ModelPart whole = root.getChild("whole");
        ModelPart body = whole.getChild("body");
        ModelPart tail = body.getChild("tail");
        ModelPart tail2 = tail.getChild("tail2");
        ModelPart tail3 = tail2.getChild("tail3");
        ModelPart tail4 = tail3.getChild("tail4");
        ModelPart wingL = body.getChild("wingL");
        ModelPart wingR = body.getChild("wingR");

        KeyframeAnimations.animate(model, PhlegethonticBombardierAnimations.ABDOMEN_FIRE,
                800L, 1.0F, new Vector3f());

        float bodyHeight = origin(whole, body).y;
        float nozzleHeight = origin(whole, body, tail, tail2, tail3, tail4).y;
        assertAll(
                () -> assertTrue(tail.xRot > .6F && tail2.xRot > .5F && tail3.xRot > .4F,
                        "the abdomen must curl upward"),
                () -> assertTrue(tail4.xRot < 0.0F
                                && tail.xRot + tail2.xRot + tail3.xRot + tail4.xRot
                                < Math.toRadians(105),
                        "the terminal segment must counter-rotate instead of folding straight down"),
                () -> assertTrue(nozzleHeight < bodyHeight - 8.0F,
                        "spray nozzle must rise over the shell"),
                () -> assertTrue(wingL.xRot > .5F && wingR.xRot > .5F,
                        "both wings must lift during the spray"),
                () -> assertTrue(wingL.zRot > .55F && wingR.zRot < -.55F,
                        "wings must flare away from the body"));
    }

    @Test
    void windupAlsoKeepsTerminalSegmentAlignedWithRaisedTail() {
        ModelPart root = PhlegethonticBombardierModel.createBodyLayer().bakeRoot();
        PhlegethonticBombardierModel model = new PhlegethonticBombardierModel(root);
        ModelPart tail = root.getChild("whole").getChild("body").getChild("tail");
        ModelPart tail2 = tail.getChild("tail2");
        ModelPart tail3 = tail2.getChild("tail3");
        ModelPart tail4 = tail3.getChild("tail4");

        KeyframeAnimations.animate(model, PhlegethonticBombardierAnimations.ABDOMEN_WINDUP,
                1500L, 1.0F, new Vector3f());

        float terminalAngle = tail.xRot + tail2.xRot + tail3.xRot + tail4.xRot;
        assertTrue(tail4.xRot < 0.0F && terminalAngle < Math.toRadians(105),
                "wind-up must counter-rotate the terminal segment as the abdomen rises");
    }

    @Test
    void liveAimPitchRotatesTheNozzleInTheSameDirectionAsTheTarget() {
        assertEquals(Math.toRadians(18), PhlegethonticBombardierModel.nozzlePitchOffset(18), .0001,
                "a target above the port must raise the nozzle");
        assertEquals(Math.toRadians(-18), PhlegethonticBombardierModel.nozzlePitchOffset(-18), .0001,
                "a target below the port must lower rather than invert the nozzle");
    }

    @Test
    void everyLoopReturnsToItsStartingPoseWithoutASeam() {
        assertLoopSeam(PhlegethonticBombardierAnimations.IDLE, 2000L, "body", "tail");
        assertLoopSeam(PhlegethonticBombardierAnimations.WALK, 1000L, "body", "lLegF");
        assertLoopSeam(PhlegethonticBombardierAnimations.WALK, 1000L, "body", "rLegM");
        assertLoopSeam(PhlegethonticBombardierAnimations.GRAZE, 1000L, "head");
        assertLoopSeam(PhlegethonticBombardierAnimations.VENT_COOLDOWN, 1000L,
                "body", "tail", "tail2", "tail3", "tail4");
    }

    @Test
    void walkingWeightFadesContinuouslyThroughTheOldCutoff() {
        float below = PhlegethonticBombardierModel.walkBlend(.019F);
        float above = PhlegethonticBombardierModel.walkBlend(.021F);
        assertTrue(below > 0.0F, "walking must fade rather than switch off below the old cutoff");
        assertTrue(above > below);
        assertTrue(above - below < .01F, "crossing the old cutoff must not produce a visible jump");
        assertEquals(0.0F, PhlegethonticBombardierModel.walkBlend(0.0F), .0001F);
        assertEquals(1.0F, PhlegethonticBombardierModel.walkBlend(1.0F), .0001F);
    }

    private static void assertLoopSeam(net.minecraft.client.animation.AnimationDefinition animation,
                                       long durationMillis, String... path) {
        Vector3f start = sampledRotation(animation, 0L, path);
        Vector3f after = sampledRotation(animation, 1L, path);
        Vector3f end = sampledRotation(animation, durationMillis - 1L, path);
        assertTrue(start.distance(end) < .01F,
                "loop seam for " + String.join("/", path) + " must return to its starting rotation");
        Vector3f velocityBefore = new Vector3f(start).sub(end);
        Vector3f velocityAfter = new Vector3f(after).sub(start);
        assertTrue(velocityBefore.distance(velocityAfter) < .002F,
                "loop seam for " + String.join("/", path) + " must preserve its direction and speed");
    }

    private static Vector3f sampledRotation(net.minecraft.client.animation.AnimationDefinition animation,
                                            long timeMillis, String... path) {
        ModelPart root = PhlegethonticBombardierModel.createBodyLayer().bakeRoot();
        PhlegethonticBombardierModel model = new PhlegethonticBombardierModel(root);
        ModelPart part = root.getChild("whole");
        for (String child : path) part = part.getChild(child);
        KeyframeAnimations.animate(model, animation, timeMillis, 1.0F, new Vector3f());
        return new Vector3f(part.xRot, part.yRot, part.zRot);
    }

    private static Vector3f origin(ModelPart... chain) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : chain) part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(new Vector3f()).mul(16.0F);
    }

    private record EyeVertex(Vector3f position, float u, float v) {}

    private static final class EyeVertices implements VertexConsumer {
        private final List<EyeVertex> vertices = new ArrayList<>();
        private Vector3f position;

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            position = new Vector3f(x, y, z).mul(16);
            return this;
        }
        @Override public VertexConsumer setUv(float u, float v) {
            vertices.add(new EyeVertex(position, u, v));
            return this;
        }
        @Override public VertexConsumer setColor(int red, int green, int blue, int alpha) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
