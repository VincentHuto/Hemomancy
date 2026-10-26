package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HemorathModelTest {
    @Test
    void overloadMovesTheIndividualRibsBeforeTheFrameSlumps() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var hips = root.getChild("root").getChild("hips");
        var rib = hips.getChild("chest").getChild("ribcage").getChild("left_rib_3");
        model.pose(0, false, false, 0, 20, 0, 0, 0, 0);
        float rest = rib.zRot, restY = hips.y;
        model.pose(HemorathEntity.VISUAL_OVERLOAD, false, false, 20, 20, 0, 0, 0, 0);
        assertTrue(Math.abs(rib.zRot - rest) > .06F, "Blood pressure should prise the ribs apart");
        model.pose(HemorathEntity.VISUAL_OVERLOAD, false, false, 1, 40, 0, 0, 0, 0);
        assertTrue(hips.y > restY + 3, "Exsanguination should collapse the skeleton into its vestments");
    }

    @Test
    void openRibcageAndHeldStaffFollowTheArticulatedSkeleton() {
        var hips = HemorathModel.createBodyLayer().bakeRoot().getChild("root").getChild("hips");
        var chest = hips.getChild("chest");
        for (var cube : chest.getChild("ribcage").cubes)
            assertTrue(cube.maxX - cube.minX <= 1, "Only the narrow sternum may fill the center of the rib cage");
        for (String side : new String[]{"left", "right"}) {
            assertNotNull(chest.getChild("ribcage").getChild(side + "_rib_0"));
            assertNotNull(chest.getChild(side + "_arm").getChild(side + "_forearm").getChild(side + "_hand"));
            assertNotNull(chest.getChild(side + "_shoulder_chain"));
        }
        assertNotNull(chest.getChild("head").getChild("face_cords"));
        assertNotNull(chest.getChild("halo").getChild("halo_segment_0"));
        assertNotNull(chest.getChild("left_arm").getChild("left_forearm").getChild("left_hand")
                .getChild("staff").getChild("staff_crown"));
    }

    @Test
    void combatPosesKeepTheStaffUprightAndReturnToTheSameIdle() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        model.pose(0, false, false, 0, 45, 0, 0, 10, 5);
        var expected = root.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList();
        var hips = root.getChild("root").getChild("hips");
        var chest = hips.getChild("chest");
        var arm = chest.getChild("left_arm");
        var forearm = arm.getChild("left_forearm");
        var hand = forearm.getChild("left_hand");
        var staff = hand.getChild("staff");
        for (int visual = 0; visual <= HemorathEntity.VISUAL_OVERLOAD; visual++)
            for (int tick = 0; tick <= 60; tick += 5) {
                model.pose(visual, visual == HemorathEntity.VISUAL_COLLAPSE_WINDUP, tick % 2 == 0,
                        tick, tick * 2.7F, tick, .5F, 10, 5);
                PoseStack pose = new PoseStack();
                for (var part : new ModelPart[]{hips, chest, arm, forearm, hand, staff}) part.translateAndRotate(pose);
                Vector3f direction = pose.last().pose().transformDirection(new Vector3f(0, 1, 0));
                assertEquals(1, direction.y, .0001F, "Staff tipped in visual state " + visual);
                assertEquals(0, direction.x, .0001F);
                assertEquals(0, direction.z, .0001F);
            }
        model.pose(0, false, false, 0, 45, 0, 0, 10, 5);
        assertEquals(expected, root.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList());
    }

    @Test
    void neutralStaffFootReachesTheFloorWhileTheHandKeepsItsGrip() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var hips = root.getChild("root").getChild("hips");
        var chest = hips.getChild("chest");
        var arm = chest.getChild("left_arm");
        var forearm = arm.getChild("left_forearm");
        var hand = forearm.getChild("left_hand");
        var staff = hand.getChild("staff");
        PoseStack pose = new PoseStack();
        for (var part : new ModelPart[]{hips, chest, arm, forearm, hand, staff}) part.translateAndRotate(pose);
        float end = staff.cubes.stream().map(c -> c.maxY).max(Float::compare).orElseThrow();
        var foot = pose.last().pose().transformPosition(new Vector3f(0, end / 16, 0)).mul(16);
        assertEquals(24, foot.y, .2F);
    }

    @Test
    void everyExportedFaceMatchesTheBakedUvRectangle() {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/HemorathModel.bbmodel");
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
        HemorathModel.createBodyLayer().bakeRoot().render(new PoseStack(), collector, 0, 0, -1);
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
            minU = Math.min(minU, u * width); maxU = Math.max(maxU, u * width);
            minV = Math.min(minV, v * height); maxV = Math.max(maxV, v * height);
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
    void allSavedGeometryUsesQuarterUnitsAndHasRealThickness() {
        HemorathModel.createBodyLayer().bakeRoot().getAllParts().forEach(part -> {
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
    void embeddedAtlasMatchesRuntimeAndGlowLeavesBoneAndClothDark() throws Exception {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/HemorathModel.bbmodel");
        var basePath = ResourceReader.path("assets/hemomancy/textures/entity/boss/hemorath/hemorath.png");
        String embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(Files.readAllBytes(basePath), Base64.getDecoder().decode(embedded.split(",", 2)[1]));
        assertEquals(project.getAsJsonArray("elements").size(), HemorathModel.createBodyLayer().bakeRoot()
                .getAllParts().mapToInt(p -> p.cubes.size()).sum());
        var base = ImageIO.read(basePath.toFile());
        var glow = ImageIO.read(ResourceReader.path(
                "assets/hemomancy/textures/entity/boss/hemorath/hemorath_overload.png").toFile());
        assertEquals(base.getWidth(), glow.getWidth());
        assertEquals(base.getHeight(), glow.getHeight());
        int dark = 0, lit = 0;
        for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
            if ((glow.getRGB(x, y) >>> 24) != 0) {
                assertNotEquals(0, base.getRGB(x, y) >>> 24);
                lit++;
            } else if ((base.getRGB(x, y) >>> 24) != 0) dark++;
        }
        assertTrue(lit > 0 && dark > lit * 3, "Only blood channels and iron fissures should glow");
    }
}
