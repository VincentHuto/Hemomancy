package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeState;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeModelTest {
    private static final Path ASSETS = ResourceReader.path("assets/hemomancy");

    @Test
    void fracturingWavesTravelThroughIndividualPrimaryFeathers() {
        var root = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(root);
        var outer = model.root().getChild("body").getChild("left_wing").getChild("left_wing_outer");
        model.pose(SeraphaeState.FRACTURING, 30, 45, 0, 0);
        float first = outer.getChild("left_primary_0").xRot;
        float last = outer.getChild("left_primary_6").xRot;
        assertTrue(Math.abs(first - last) > .04F, "Feather tips must lag the wing root");
        model.pose(SeraphaeState.FRACTURING, 30, 50, 0, 0);
        assertTrue(Math.abs(first - outer.getChild("left_primary_0").xRot) > .025F);
    }

    @Test
    void featheredWingsHaveSeparateShoulderAndOuterWingJoints() {
        ModelPart body = SeraphaeModel.createBodyLayer().bakeRoot().getChild("root").getChild("body");
        for (String side : new String[]{"left", "right"}) {
            ModelPart wing = body.getChild(side + "_wing");
            ModelPart outer = wing.getChild(side + "_wing_outer");
            assertFalse(wing.getChild(side + "_inner_feather_0").cubes.isEmpty());
            assertFalse(outer.getChild(side + "_primary_0").cubes.isEmpty());
            assertFalse(outer.getChild(side + "_primary_6").cubes.isEmpty());
            assertTrue(outer.getAllParts().mapToInt(part -> part.cubes.size()).sum() >= 20);
        }
    }

    @Test
    void leavingCondensingRestoresTheWholeStablePoseAndDispersalCanRecover() {
        var layer = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(layer);
        model.pose(SeraphaeState.STABLE, 160, 45, 10, 5);
        var expected = layer.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList();
        model.pose(SeraphaeState.CONDENSING, 0, 45, 10, 5);
        assertNotEquals(expected, layer.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList());
        model.pose(SeraphaeState.DISPERSED, 20, 45, 10, 5);
        assertFalse(model.root().visible);
        model.pose(SeraphaeState.STABLE, 160, 45, 10, 5);
        assertTrue(model.root().visible);
        assertEquals(expected, layer.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList());
    }

    @Test
    void wristChainsHangDownThroughEveryContainmentPose() {
        var layer = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(layer);
        var body = model.root().getChild("body");
        for (var state : SeraphaeState.values()) for (int tick = 0; tick <= 100; tick += 5) {
            model.pose(state, tick, tick * 2.3F, 12, 5);
            for (String side : new String[]{"left", "right"}) {
                var arm = body.getChild(side + "_arm");
                var forearm = arm.getChild(side + "_forearm");
                var chain = forearm.getChild(side + "_chain");
                PoseStack pose = new PoseStack();
                for (var part : new ModelPart[]{model.root(), body, arm, forearm, chain})
                    part.translateAndRotate(pose);
                Vector3f down = pose.last().pose().transformDirection(new Vector3f(0, 1, 0));
                assertEquals(1, down.y, .002F, state + ": " + side + " tick=" + tick + " down=" + down);
                assertTrue(Math.abs(down.x) < .05F && Math.abs(down.z) < .05F);
            }
        }
    }

    @Test
    void everyBlockbenchFaceUsesTheSameUvRectangleAsTheBakedJavaModel() throws Exception {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/SeraphaeModel.bbmodel");
        int width = project.getAsJsonObject("resolution").get("width").getAsInt();
        int height = project.getAsJsonObject("resolution").get("height").getAsInt();
        var exported = new ArrayList<String>();
        for (var element : project.getAsJsonArray("elements"))
            for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
                exported.add(rectangle(uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                        uv.get(2).getAsFloat(), uv.get(3).getAsFloat()));
            }
        var collector = new UvCollector(width, height);
        SeraphaeModel.createBodyLayer().bakeRoot().render(new PoseStack(), collector, 0, 0, -1);
        Collections.sort(exported);
        Collections.sort(collector.faces);
        assertEquals(exported, collector.faces);
    }

    private static String rectangle(float u0, float v0, float u1, float v1) {
        return Math.round(Math.min(u0, u1) * 4) + ":" + Math.round(Math.min(v0, v1) * 4) + ":"
                + Math.round(Math.max(u0, u1) * 4) + ":" + Math.round(Math.max(v0, v1) * 4);
    }

    private static final class UvCollector implements VertexConsumer {
        final ArrayList<String> faces = new ArrayList<>();
        final int width, height;
        int vertices;
        float minU = Float.POSITIVE_INFINITY, minV = Float.POSITIVE_INFINITY;
        float maxU = Float.NEGATIVE_INFINITY, maxV = Float.NEGATIVE_INFINITY;

        UvCollector(int width, int height) { this.width = width; this.height = height; }

        @Override public VertexConsumer addVertex(float x, float y, float z) { return this; }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
        @Override public VertexConsumer setUv(float u, float v) {
            minU = Math.min(minU, u * width);
            maxU = Math.max(maxU, u * width);
            minV = Math.min(minV, v * height);
            maxV = Math.max(maxV, v * height);
            if (++vertices % 4 == 0) {
                faces.add(rectangle(minU, minV, maxU, maxV));
                minU = minV = Float.POSITIVE_INFINITY;
                maxU = maxV = Float.NEGATIVE_INFINITY;
            }
            return this;
        }
        @Override public VertexConsumer setUv1(int u, int v) { return this; }
        @Override public VertexConsumer setUv2(int u, int v) { return this; }
        @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
    }

    @Test
    void restraintsAndArticulatedLimbsFollowTheTorso() {
        ModelPart root = SeraphaeModel.createBodyLayer().bakeRoot().getChild("root");
        ModelPart body = root.getChild("body");
        assertNotNull(body.getChild("head").getChild("veil"));
        assertNotNull(body.getChild("halo").getChild("halo_segment_0"));
        for (String side : new String[]{"left", "right"}) {
            ModelPart forearm = body.getChild(side + "_arm").getChild(side + "_forearm");
            assertNotNull(forearm.getChild(side + "_hand"));
            assertNotNull(forearm.getChild(side + "_chain"));
            assertNotNull(root.getChild(side + "_leg").getChild(side + "_shin"));
        }
    }

    @Test
    void authoredCubesAndPivotsStayOnTheQuarterUnitGrid() {
        var root = SeraphaeModel.createBodyLayer().bakeRoot();
        root.getAllParts().forEach(part -> {
            for (float coordinate : new float[]{part.x, part.y, part.z})
                assertEquals(Math.round(coordinate * 4), coordinate * 4, .0001F);
            for (var cube : part.cubes) {
                for (float coordinate : new float[]{cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ})
                    assertEquals(Math.round(coordinate * 4), coordinate * 4, .0001F);
                assertTrue(cube.maxX - cube.minX >= .25F);
                assertTrue(cube.maxY - cube.minY >= .25F);
                assertTrue(cube.maxZ - cube.minZ >= .25F);
            }
        });
    }

    @Test
    void blockbenchEmbedsTheInstalledAtlasAndMatchesTheBakedCubeCount() throws Exception {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/SeraphaeModel.bbmodel");
        byte[] atlasBytes = Files.readAllBytes(ASSETS.resolve("textures/entity/boss/seraphae/seraphae.png"));
        String embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(atlasBytes, Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1)));
        assertEquals(project.getAsJsonArray("elements").size(), SeraphaeModel.createBodyLayer().bakeRoot()
                .getAllParts().mapToInt(part -> part.cubes.size()).sum());
        var base = ImageIO.read(ASSETS.resolve("textures/entity/boss/seraphae/seraphae.png").toFile());
        var glow = ImageIO.read(ASSETS.resolve("textures/entity/boss/seraphae/seraphae_glow.png").toFile());
        assertEquals(base.getWidth(), glow.getWidth());
        assertEquals(base.getHeight(), glow.getHeight());
        int lit = 0, dark = 0;
        for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
            if ((glow.getRGB(x, y) >>> 24) != 0) {
                assertNotEquals(0, base.getRGB(x, y) >>> 24, "Glow must stay on visible surfaces");
                lit++;
            } else if ((base.getRGB(x, y) >>> 24) != 0) dark++;
        }
        assertTrue(lit > 0);
        assertTrue(dark > lit, "Robes and restraints must remain dark around the luminous vessels");
    }
}
