package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrismCuttleModelTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
    private static final int SIZE = 128;

    @Test
    void runtimeAtlasesMatchTheEditableModel() throws IOException {
        JsonObject model = readProject();
        assertEquals(SIZE, model.getAsJsonObject("resolution").get("width").getAsInt());
        assertEquals(SIZE, model.getAsJsonObject("resolution").get("height").getAsInt());
        var expected = new HashSet<>(Set.of("deep", "sand", "coral", "kelp", "flash"));
        for (var entry : model.getAsJsonArray("textures")) {
            JsonObject texture = entry.getAsJsonObject();
            String name = texture.get("name").getAsString();
            assertTrue(expected.remove(name.substring("model_prism_cuttle_".length(), name.length() - 4)), name);
            String embedded = texture.get("source").getAsString().split(",", 2)[1];
            Path runtime = ASSETS.resolve("textures/entity/prism_cuttle").resolve(name);
            assertArrayEquals(Files.readAllBytes(runtime), Base64.getDecoder().decode(embedded), name);
        }
        assertEquals(Set.of(), expected);
    }

    @Test
    void bakedModelUsesEveryAuthoredFaceRectangle() throws IOException {
        var expected = new HashSet<List<Integer>>();
        for (var element : readProject().getAsJsonArray("elements")) {
            for (var face : element.getAsJsonObject().getAsJsonObject("faces").asMap().values()) {
                var uv = face.getAsJsonObject().getAsJsonArray("uv");
                assertTrue(expected.add(List.of(uv.get(0).getAsInt(), uv.get(1).getAsInt(),
                        uv.get(2).getAsInt(), uv.get(3).getAsInt())));
            }
        }
        var capture = new FaceCapture();
        new PrismCuttleModel().root().render(new PoseStack(), capture, 0, 0);
        assertEquals(expected.size() * 4, capture.vertices);
        assertEquals(expected, capture.faces);
    }

    @Test
    void swimmingMovesFinPanelsOutOfPhaseAndCurlsTheJointedArms() {
        var model = new PrismCuttleModel();
        var fin = model.root().getChild("mantle").getChild("right_fin");
        var front = fin.getChild("right_shoulder_root");
        var rear = fin.getChild("right_tip_web");
        var arm = model.root().getChild("head").getChild("arms").getChild("right_upper_outer");
        var curl = arm.getChild("right_upper_outer_curl");
        float frontRest = front.zRot;
        float rearRest = rear.zRot;
        float curlRest = curl.xRot;

        model.pose(4.0F, 0.8F, 35.0F, 0.0F);
        assertTrue(front.zRot > frontRest && rear.zRot < rearRest);
        assertNotEquals(curlRest, curl.xRot);
        assertEquals(24.0F * Mth.DEG_TO_RAD, model.root().xRot, 0.0001F);
    }

    @Test
    void flashFlaresTheMantleAndFeedingTentaclesThenResetsToSwim() {
        var model = new PrismCuttleModel();
        var mantle = model.root().getChild("mantle");
        var feeder = model.root().getChild("head").getChild("arms").getChild("right_feeding_tentacle");
        var club = feeder.getChild("right_tentacle_club");
        model.pose(8.0F, 0.0F, 0.0F, 0.0F);
        float neutral = mantle.yScale;
        float feederYaw = feeder.yRot;
        float clubPitch = club.xRot;
        model.pose(8.0F, 0.0F, 0.0F, 18.0F);
        float peak = mantle.yScale;
        assertTrue(peak > neutral + 0.1F);
        assertTrue(feeder.yRot < feederYaw && club.xRot > clubPitch);
        model.pose(8.0F, 0.0F, 0.0F, 3.0F);
        assertTrue(mantle.yScale > neutral && mantle.yScale < peak);
        model.pose(8.0F, 0.0F, 0.0F, 0.0F);
        assertEquals(neutral, mantle.yScale);
        assertEquals(feederYaw, feeder.yRot);
        assertEquals(clubPitch, club.xRot);
    }

    private static JsonObject readProject() throws IOException {
        Path project = ASSETS.resolve("models/entity/bbmodel/PrismCuttleModel.bbmodel");
        return JsonParser.parseString(Files.readString(project)).getAsJsonObject();
    }

    private static final class FaceCapture implements VertexConsumer {
        final Set<List<Integer>> faces = new HashSet<>();
        int vertices;
        int left;
        int top;

        @Override
        public VertexConsumer setUv(float u, float v) {
            int x = Math.round(u * SIZE);
            int y = Math.round(v * SIZE);
            if (vertices % 4 == 0) {
                left = x;
                top = y;
            } else if (vertices % 4 == 2) {
                faces.add(List.of(left, top, x, y));
            }
            vertices++;
            return this;
        }

        @Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
    }
}
