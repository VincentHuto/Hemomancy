package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChalybeateSnailModelTest {
    @Test
    void rendersEveryAuthoredFaceAtItsAtlasCoordinates() throws Exception {
        var project = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/models/entity/bbmodel/ChalybeateScleritesSnail.bbmodel")));
        Map<Uv, Integer> expected = new HashMap<>();
        for (var cube : project.getAsJsonObject().getAsJsonArray("elements")) {
            for (var face : cube.getAsJsonObject().getAsJsonObject("faces").asMap().values()) {
                var uv = face.getAsJsonObject().getAsJsonArray("uv");
                expected.merge(new Uv(uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                        uv.get(2).getAsFloat(), uv.get(3).getAsFloat()), 1, Integer::sum);
            }
        }
        var model = new ChalybeateSnailModel();
        var actual = capture(model.root());
        assertEquals(expected.values().stream().mapToInt(Integer::intValue).sum() * 4, actual.vertices);
        assertEquals(expected, actual.faces);
    }

    @Test
    void withdrawalKeepsTheArmoredFootVisibleAndFitsItUnderTheShell() {
        var model = new ChalybeateSnailModel();
        var foot = model.root().getChild("creeping_foot");
        var shell = model.root().getChild("coiled_shell");
        model.pose(4.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        var extended = capture(model.root());

        model.pose(4.0F, 0.0F, 0.0F, 0.0F, 0.5F);
        var halfway = capture(model.root());
        model.pose(4.0F, 0.0F, 0.0F, 0.0F, 1.0F);
        var withdrawn = capture(model.root());
        var withdrawnFoot = capture(foot);
        var shellBounds = capture(shell);

        assertTrue(extended.length() > halfway.length() && halfway.length() > withdrawn.length());
        assertTrue(withdrawn.height() < extended.height());
        assertEquals(extended.vertices, withdrawn.vertices);
        assertTrue(withdrawnFoot.minZ >= shellBounds.minZ && withdrawnFoot.maxZ <= shellBounds.maxZ);
        assertEquals(24.0F / 16.0F, withdrawn.maxY, 0.005F);

        model.pose(4.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        assertEquals(extended.length(), capture(model.root()).length());
    }

    @Test
    void crawlAndClimbMoveTheFootAndScleritesWhileRestOnlyMovesTheFeelers() {
        var model = new ChalybeateSnailModel();
        var foot = model.root().getChild("creeping_foot");
        var head = foot.getChild("head");
        var tentacle = head.getChild("left_cephalic_tentacle");
        var skirt = foot.getChild("iron_sulfide_sclerites").getChild("left_sclerite_skirt");
        model.pose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        float stationaryTentacle = tentacle.xRot;
        model.pose(5.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        assertTrue(tentacle.xRot > stationaryTentacle);
        assertEquals(1.0F, foot.zScale);
        assertEquals(0.0F, skirt.zRot);
        model.pose(5.0F, 0.2F, 0.0F, 0.0F, 0.0F);
        assertTrue(foot.zScale > 1.0F && skirt.zRot > 0.0F);
        model.pose(5.0F, 0.0F, 1.0F, 20.0F, 0.0F);
        assertTrue(foot.zScale > 1.0F && skirt.zRot > 0.0F);
        assertEquals(0.0F, head.yRot);
    }

    private static Capture capture(ModelPart part) {
        var capture = new Capture();
        part.render(new PoseStack(), capture, 0, 0, -1);
        return capture;
    }

    private record Uv(float left, float top, float right, float bottom) {}

    private static final class Capture implements VertexConsumer {
        private final Map<Uv, Integer> faces = new HashMap<>();
        private int vertices;
        private float left;
        private float top;
        private float minY = Float.POSITIVE_INFINITY;
        private float maxY = Float.NEGATIVE_INFINITY;
        private float minZ = Float.POSITIVE_INFINITY;
        private float maxZ = Float.NEGATIVE_INFINITY;

        float length() { return maxZ - minZ; }
        float height() { return maxY - minY; }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
            return this;
        }

        @Override public VertexConsumer setUv(float u, float v) {
            float x = u * 64;
            float y = v * 64;
            if (vertices % 4 == 0) {
                left = x;
                top = y;
            } else if (vertices % 4 == 2) {
                faces.merge(new Uv(left, top, x, y), 1, Integer::sum);
            }
            vertices++;
            return this;
        }

        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
    }
}
