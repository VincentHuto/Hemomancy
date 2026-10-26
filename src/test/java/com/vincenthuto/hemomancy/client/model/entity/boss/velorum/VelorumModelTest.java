package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

class VelorumModelTest {
    @Test void aMartyrsBowAndDeathRemainVisibleDuringTheSharedCast() {
        var root=VelorumModel.createBodyLayer().bakeRoot();var model=new VelorumModel<>(root);
        var chest=root.getChild("root").getChild("hips").getChild("chest");
        var phase=new com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumVisualRules.Phase(100,15,160,19,80,17,30);
        model.pose(phase,30,0,0,0,0,0);float cast=chest.xRot;
        model.pose(phase,30,0,0,0,0,1);assertTrue(chest.xRot>cast+.3F);
    }
    @Test
    void everyCombatPoseResetsAndKeepsStaffSleevesAndClawIceVertical() {
        var root = VelorumModel.createBodyLayer().bakeRoot();
        var model = new VelorumModel<>(root);
        model.pose(0, false, 0, 45, 10, 5);
        var expected = root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.xScale, p.yScale, p.zScale)).toList();
        var hips = root.getChild("root").getChild("hips");
        var chest = hips.getChild("chest");
        for (int visual = 0; visual <= 3; visual++) for (int tick = 0; tick <= 24; tick += 3) {
            model.pose(visual, tick % 2 == 0, tick, tick * 2.7F, 10, 5);
            for (String side : new String[]{"left", "right"}) {
                var arm = chest.getChild(side + "_arm");
                var fore = arm.getChild(side + "_forearm");
                var hand = fore.getChild(side + "_hand");
                PoseStack pose = new PoseStack();
                for (var part : new ModelPart[]{hips, chest, arm, fore}) part.translateAndRotate(pose);
                pose.pushPose();
                fore.getChild(side + "_sleeve").translateAndRotate(pose);
                assertVertical(pose);
                pose.popPose();
                hand.translateAndRotate(pose);
                hand.getChild(side.equals("left") ? "staff" : "claw_ice").translateAndRotate(pose);
                assertVertical(pose);
            }
        }
        model.pose(0, false, 0, 45, 10, 5);
        assertEquals(expected, root.getAllParts().map(p -> List.of(p.xRot, p.yRot, p.zRot, p.xScale, p.yScale, p.zScale)).toList());
    }

    private static void assertVertical(PoseStack pose) {
        var down = pose.last().pose().transformDirection(new Vector3f(0, 1, 0));
        assertEquals(1, down.y, .0001F);
        assertEquals(0, down.x, .0001F);
        assertEquals(0, down.z, .0001F);
    }

    @Test
    void authoredStaffFootReachesTheFloor() {
        var hips = VelorumModel.createBodyLayer().bakeRoot().getChild("root").getChild("hips");
        var chest = hips.getChild("chest");
        var arm = chest.getChild("left_arm");
        var fore = arm.getChild("left_forearm");
        var hand = fore.getChild("left_hand");
        var staff = hand.getChild("staff");
        PoseStack pose = new PoseStack();
        for (var part : new ModelPart[]{hips, chest, arm, fore, hand, staff}) part.translateAndRotate(pose);
        float bottom = staff.cubes.stream().map(c -> c.maxY).max(Float::compare).orElseThrow();
        var foot = pose.last().pose().transformPosition(new Vector3f(0, bottom / 16, 0)).mul(16);
        assertEquals(24, foot.y, .2F);
    }

    @Test
    void exportedFaceUvsMatchTheBakedJavaModel() {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/VelorumModel.bbmodel");
        int width = project.getAsJsonObject("resolution").get("width").getAsInt();
        int height = project.getAsJsonObject("resolution").get("height").getAsInt();
        var expected = new ArrayList<String>();
        for (var element : project.getAsJsonArray("elements"))
            for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
                expected.add(rectangle(uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                        uv.get(2).getAsFloat(), uv.get(3).getAsFloat()));
            }
        var collector = new UvCollector(width, height);
        VelorumModel.createBodyLayer().bakeRoot().render(new PoseStack(), collector, 0, 0, -1);
        Collections.sort(expected);
        Collections.sort(collector.faces);
        assertEquals(expected, collector.faces);
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
    void veilHaloAndLanternsFrameAnArticulatedClaw() {
        var chest = VelorumModel.createBodyLayer().bakeRoot().getChild("root").getChild("hips").getChild("chest");
        var head = chest.getChild("head");
        assertTrue(head.getChild("face_veil").getAllParts().mapToInt(p -> p.cubes.size()).sum() >= 8);
        assertNotNull(head.getChild("halo").getChild("halo_segment_0"));
        var claw = chest.getChild("right_arm").getChild("right_forearm").getChild("right_hand");
        assertNotNull(claw.getChild("right_finger_0").getChild("right_finger_0_tip"));
        var staff = chest.getChild("left_arm").getChild("left_forearm").getChild("left_hand").getChild("staff");
        assertNotNull(staff.getChild("staff_lantern").getChild("staff_lantern_crystal"));
        assertNotNull(staff.getChild("pendant_chain").getChild("pendant_lantern"));
        assertNotNull(chest.getChild("side_chain").getChild("side_lantern"));
    }

    @Test
    void savedGeometryUsesTheQuarterUnitGridAndRealThickness() {
        VelorumModel.createBodyLayer().bakeRoot().getAllParts().forEach(part -> {
            for (float c : new float[]{part.x, part.y, part.z}) assertEquals(Math.round(c * 4), c * 4, .0001F);
            for (var cube : part.cubes) {
                for (float c : new float[]{cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ})
                    assertEquals(Math.round(c * 4), c * 4, .0001F);
                assertTrue(cube.maxX - cube.minX >= .25F);
                assertTrue(cube.maxY - cube.minY >= .25F);
                assertTrue(cube.maxZ - cube.minZ >= .25F);
            }
        });
    }

    @Test
    void embeddedTextureMatchesRuntimeAndGlowStaysOnVisibleSurfaces() throws Exception {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/VelorumModel.bbmodel");
        var path = ResourceReader.path("assets/hemomancy/textures/entity/boss/velorum/velorum.png");
        var embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(Files.readAllBytes(path), Base64.getDecoder().decode(embedded.split(",", 2)[1]));
        assertEquals(project.getAsJsonArray("elements").size(), VelorumModel.createBodyLayer().bakeRoot()
                .getAllParts().mapToInt(p -> p.cubes.size()).sum());
        var base = ImageIO.read(path.toFile());
        var glow = ImageIO.read(ResourceReader.path("assets/hemomancy/textures/entity/boss/velorum/velorum_glow.png").toFile());
        assertEquals(base.getWidth(), glow.getWidth());
        assertEquals(base.getHeight(), glow.getHeight());
        int lit = 0, dark = 0;
        for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
            if ((glow.getRGB(x, y) >>> 24) != 0) {
                assertNotEquals(0, base.getRGB(x, y) >>> 24);
                lit++;
            } else if ((base.getRGB(x, y) >>> 24) != 0) dark++;
        }
        assertTrue(lit > 0 && dark > lit * 3, "Shadow robes must frame the smaller luminous frost details");
    }
}
