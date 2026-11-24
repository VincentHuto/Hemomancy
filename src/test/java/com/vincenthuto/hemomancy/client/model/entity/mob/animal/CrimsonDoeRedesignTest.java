package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrimsonDoeRedesignTest {
    @Test
    void rendererUsesTheVisceralAtlas() throws Exception {
        var renderer = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/entity/mob/animal/CrimsonDoeRenderer.java"));
        assertTrue(renderer.contains("textures/entity/crimson_doe/model_crimson_doe_redesign.png"));
    }

    private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
    private static final Path TEXTURES = ASSETS.resolve("textures/entity/crimson_doe");

    @Test
    void runtimeMeshEmitsEveryAuthoredUvFace() throws Exception {
        var project = JsonParser.parseString(Files.readString(
                ASSETS.resolve("models/entity/bbmodel/CrimsonDoeRedesign.bbmodel"))).getAsJsonObject();
        var atlas = ImageIO.read(TEXTURES.resolve("model_crimson_doe_redesign.png").toFile());
        var glow = ImageIO.read(TEXTURES.resolve("model_crimson_doe_redesign_glow.png").toFile());
        assertEquals(atlas.getWidth(), glow.getWidth());
        assertEquals(atlas.getHeight(), glow.getHeight());
        int litPixels = 0;
        for (int y = 0; y < atlas.getHeight(); y++) {
            for (int x = 0; x < atlas.getWidth(); x++) {
                if (glow.getRGB(x, y) >>> 24 != 0) {
                    assertEquals(255, atlas.getRGB(x, y) >>> 24);
                    litPixels++;
                }
            }
        }
        assertTrue(litPixels > 0);
        Set<UvFace> expected = new HashSet<>();
        for (var element : project.getAsJsonArray("elements")) {
            for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
                expected.add(new UvFace(uv.get(0).getAsInt(), uv.get(1).getAsInt(),
                        uv.get(2).getAsInt(), uv.get(3).getAsInt()));
            }
        }
        var capture = new UvCapture();
        CrimsonDoeMesh.createRoot().render(new PoseStack(), capture, 0, 0, -1);
        assertEquals(expected.size() * 4, capture.vertices);
        assertEquals(expected, capture.faces);
    }

    @Test
    void hoofArticulationAndFlankBreathingResetAcrossEntities() {
        var model = new CrimsonDoeModel();
        var body = model.root().getChild("body");
        var hoof = body.getChild("left_hind_leg").getChild("left_hind_gaskin")
                .getChild("left_hind_cannon").getChild("left_hind_hoof");
        var flank = body.getChild("exposed_flank");
        var vein = body.getChild("neck").getChild("raised_jugular");
        float restHoof = hoof.xRot;

        model.pose(0, 0, 2.5F, 0, 0, 0);
        assertTrue(flank.xScale > 1);
        assertTrue(vein.xScale > 1);
        model.pose(1.2F, 1, 18, 0, 0, 0);
        assertNotEquals(restHoof, hoof.xRot);
        assertEquals(1, flank.xScale);
        assertEquals(1, vein.xScale);
        model.pose(0, 0, 18, 0, 0, 0);
        assertEquals(restHoof, hoof.xRot);
    }

    private record UvFace(int left, int top, int right, int bottom) {}

    private static final class UvCapture implements VertexConsumer {
        final Set<UvFace> faces = new HashSet<>();
        int vertices;
        private int left;
        private int top;

        @Override
        public VertexConsumer setUv(float u, float v) {
            int x = Math.round(u * CrimsonDoeMesh.TEXTURE_WIDTH);
            int y = Math.round(v * CrimsonDoeMesh.TEXTURE_HEIGHT);
            if (vertices % 4 == 0) {
                left = x;
                top = y;
            } else if (vertices % 4 == 2) {
                faces.add(new UvFace(left, top, x, y));
            }
            vertices++;
            return this;
        }

        @Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }
}
