package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChoirKeeperModelTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");

    @Test
    void runtimeTextureMatchesTheLowDensityBlockbenchAtlas() throws Exception {
        var png = ASSETS.resolve("textures/entity/choir_keeper_java.png");
        var texture = ImageIO.read(png.toFile());
        assertEquals(128, texture.getWidth());
        assertEquals(128, texture.getHeight());
        var embedded = project().getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(Files.readAllBytes(png), Base64.getDecoder().decode(embedded.split(",", 2)[1]));
    }

    @Test
    void javaMeshPreservesAllAuthoredFaceUvsIncludingSharedAndMirroredFaces() throws Exception {
        Map<Face, Integer> authored = new HashMap<>();
        for (var cube : project().getAsJsonArray("elements")) {
            for (var face : cube.getAsJsonObject().getAsJsonObject("faces").asMap().values()) {
                var uv = face.getAsJsonObject().getAsJsonArray("uv");
                authored.merge(new Face(uv.get(0).getAsInt(), uv.get(1).getAsInt(),
                        uv.get(2).getAsInt(), uv.get(3).getAsInt()), 1, Integer::sum);
            }
        }

        var capture = capture(new ChoirKeeperModel());
        assertEquals(81 * 6 * 4, capture.vertices);
        assertEquals(authored, capture.faces);
    }

    @Test
    void flightSpreadsTheWingsAndLayersTheFanBeforeReturningToPerch() {
        var model = new ChoirKeeperModel();
        var body = model.root().getChild("body");
        var tail = body.getChild("tail_display");
        var outer = tail.getChild("outer_train").getChild("outer_feather_01");
        var inner = tail.getChild("inner_train").getChild("inner_feather_06");
        var left = body.getChild("left_wing");
        var right = body.getChild("right_wing");
        var primaries = left.getChild("left_primaries");
        var head = body.getChild("head");

        model.pose(0, 0, 0, 0, 0, 0, 0);
        var perch = capture(model);
        float foldedTail = tail.xRot;
        float foldedFeather = outer.zRot;
        assertTrue(foldedTail < -1);

        model.pose(1, 0, 0, 0, 0, 0, 0);
        var flight = capture(model);
        float wrist = primaries.zRot;
        assertTrue(flight.width() > perch.width() * 1.3F);
        assertEquals(-left.zRot, right.zRot, .0001F);
        model.pose(1, 0, 0, 0, 4, 0, 0);
        assertTrue(Math.abs(primaries.zRot - wrist) > .2F && primaries.zRot != left.zRot);

        model.pose(1, 1, .05F, 5, 4, 20, 15);
        var fan = capture(model);
        assertEquals(68 * Mth.DEG_TO_RAD, outer.zRot, .0001F);
        assertEquals(-55 * Mth.DEG_TO_RAD, inner.zRot, .0001F);
        assertTrue(fan.width() > flight.width() * 1.3F);
        assertTrue(head.yRot > 0);

        model.pose(0, 0, 0, 0, 0, 0, 0);
        assertEquals(foldedTail, tail.xRot);
        assertEquals(foldedFeather, outer.zRot);
        assertEquals(perch.width(), capture(model).width());
        assertEquals(0, head.yRot);
    }

    private static Capture capture(ChoirKeeperModel model) {
        var capture = new Capture();
        model.root().render(new PoseStack(), capture, 0, 0, -1);
        return capture;
    }

    private static JsonObject project() throws Exception {
        return JsonParser.parseString(Files.readString(
                ASSETS.resolve("models/entity/bbmodel/ChoirKeeperModel.bbmodel"))).getAsJsonObject();
    }

    private record Face(int u, int v, int endU, int endV) {}

    private static final class Capture implements VertexConsumer {
        private final Map<Face, Integer> faces = new HashMap<>();
        private int vertices;
        private int u;
        private int v;
        private float minX = Float.POSITIVE_INFINITY;
        private float maxX = Float.NEGATIVE_INFINITY;

        float width() {
            return maxX - minX;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            int px = Math.round(u * ChoirKeeperMesh.TEXTURE_WIDTH);
            int py = Math.round(v * ChoirKeeperMesh.TEXTURE_HEIGHT);
            if (vertices % 4 == 0) {
                this.u = px;
                this.v = py;
            } else if (vertices % 4 == 2) {
                faces.merge(new Face(this.u, this.v, px, py), 1, Integer::sum);
            }
            vertices++;
            return this;
        }

        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
