package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsteophageModelTest {
    @Test
    void bakedMeshEmitsTheAuthoredFractionalUvRectangles() throws Exception {
        var project = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/models/entity/bbmodel/Osteophage.bbmodel"))).getAsJsonObject();
        Set<Uv> expected = new HashSet<>();
        for (var element : project.getAsJsonArray("elements")) {
            for (var face : element.getAsJsonObject().getAsJsonObject("faces").asMap().values()) {
                var uv = face.getAsJsonObject().getAsJsonArray("uv");
                assertTrue(expected.add(new Uv(uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                        uv.get(2).getAsFloat(), uv.get(3).getAsFloat())));
            }
        }
        var capture = capture(new OsteophageModel());
        assertEquals(expected.size() * 4, capture.vertexCount);
        assertEquals(expected, capture.faces);
    }

    @Test
    void flightArticulatesAllThreeWingJointsAndResetsToTheFoldedPose() {
        var model = new OsteophageModel();
        var body = model.root().getChild("body");
        var left = body.getChild("wing_L");
        var wrist = left.getChild("forewing_L").getChild("handwing_L");
        var right = body.getChild("wing_R");
        var jaw = body.getChild("neck").getChild("head").getChild("jaw");
        model.pose(0, Vec3.ZERO, 0, 0, 0, 0, 0);
        float foldedWidth = capture(model).width();
        float foldedShoulder = left.yRot;
        float foldedWrist = wrist.yRot;

        model.pose(1, new Vec3(.25, .1, 0), 0, 0, 0, 0, 0);
        float spreadWidth = capture(model).width();
        float wristStroke = wrist.zRot;
        float levelPitch = body.xRot;
        model.pose(1, new Vec3(.25, .1, 0), 0, 4, 0, 0, 0);
        assertTrue(Math.abs(wristStroke - wrist.zRot) > .1F);
        assertTrue(spreadWidth > foldedWidth * 1.8F, "The grounded silhouette must fold tightly");
        assertEquals(-left.zRot, right.zRot, .0001F);

        model.pose(1, new Vec3(.25, -.4, 0), 0, 0, 0, 0, 1);
        assertTrue(body.xRot > levelPitch + .25F);
        assertTrue(capture(model).width() < spreadWidth);
        assertTrue(jaw.xRot > .25F);

        model.pose(0, Vec3.ZERO, 0, 0, 0, 0, 0);
        assertEquals(foldedShoulder, left.yRot);
        assertEquals(foldedWrist, wrist.yRot);
        assertEquals(foldedWidth, capture(model).width());
        assertEquals(0, jaw.xRot);
    }

    private static Capture capture(OsteophageModel model) {
        var capture = new Capture();
        model.root().render(new PoseStack(), capture, 0, 0, -1);
        return capture;
    }

    private record Uv(float left, float top, float right, float bottom) {}

    private static final class Capture implements VertexConsumer {
        private final Set<Uv> faces = new HashSet<>();
        private final Vec3[] positions = new Vec3[4];
        private int vertexCount;
        private float left;
        private float top;
        private float minX = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;

        float width() {
            return maxX - minX;
        }

        @Override public VertexConsumer addVertex(float x, float y, float z) {
            positions[vertexCount % 4] = new Vec3(x, y, z);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            return this;
        }

        @Override public VertexConsumer setUv(float u, float v) {
            float x = u * OsteophageMesh.TEXTURE_WIDTH;
            float y = v * OsteophageMesh.TEXTURE_HEIGHT;
            if (vertexCount % 4 == 0) {
                left = x;
                top = y;
            } else if (vertexCount % 4 == 2) {
                assertTrue(faces.add(new Uv(left, top, x, y)));
            }
            vertexCount++;
            return this;
        }

        @Override public VertexConsumer setNormal(float x, float y, float z) {
            if (vertexCount % 4 == 0) {
                Vec3 normal = positions[1].subtract(positions[0]).cross(positions[2].subtract(positions[0]));
                assertTrue(normal.dot(new Vec3(x, y, z)) > 0, "Face winding must match its normal");
            }
            return this;
        }

        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
    }
}
