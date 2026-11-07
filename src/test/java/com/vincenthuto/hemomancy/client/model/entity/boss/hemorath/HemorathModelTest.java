package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathMove;
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
    private static float[] still() {
        return new float[HemorathModel.CHANNELS];
    }

    @Test
    void ribsPartWithTheMeterAndTheEndingFoldsHimIntoHisVestments() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var hips = root.getChild("root").getChild("hips");
        var rib = hips.getChild("chest").getChild("ribcage").getChild("left_rib_3");
        model.pose(still(), 0, 0, 1000, 0, 20, 0, 0, 0, 0);
        float rest = rib.zRot, restY = hips.y;
        model.pose(still(), 1, 0, 1000, 0, 20, 0, 0, 0, 0);
        assertTrue(Math.abs(rib.zRot - rest) > .06F, "A full meter should prise the ribs apart");
        float[] fold = still();
        HemorathModel.ending(fold, 110);
        model.pose(fold, 0, 0, 1000, 0, 40, 0, 0, 0, 0);
        assertTrue(hips.y > restY + 3, "The ending should fold the skeleton into its vestments");
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
            assertNotNull(chest.getChild(side + "_arm").getChild(side + "_arm_vessel"));
        }
        assertNotNull(chest.getChild("head").getChild("face_cords").getChild("face_cords_left"));
        assertNotNull(chest.getChild("head").getChild("face_cords").getChild("face_cords_right"));
        assertNotNull(chest.getChild("halo").getChild("halo_segment_0"));
        assertNotNull(chest.getChild("halo").getChild("halo_spike_7"));
        assertNotNull(chest.getChild("halo").getChild("halo_chain_left").getChild("halo_chain_left_pendant"));
        assertNotNull(chest.getChild("left_shoulder_chain").getChild("left_shoulder_chain_pendant"));
        for (String stage : new String[]{"vessel_cords", "vessel_lobes", "vessel_mass"}) assertNotNull(chest.getChild(stage));
        var rightHand = chest.getChild("right_arm").getChild("right_forearm").getChild("right_hand");
        assertNotNull(rightHand.getChild("claw_chain"));
        assertNotNull(chest.getChild("right_arm").getChild("right_forearm").getChild("spike_patch_0"));
        assertNotNull(chest.getChild("head").getChild("spike_patch_8"));
        assertNotNull(chest.getChild("left_arm").getChild("left_forearm").getChild("left_hand")
                .getChild("staff").getChild("staff_crown"));
    }

    @Test
    void theRestPoseIsHollowAndTheVesselFillsFromInside() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var chest = root.getChild("root").getChild("hips").getChild("chest");
        model.pose(still(), 0, 0, 1000, 0, 20, 0, 0, 0, 0);
        assertFalse(chest.getChild("vessel_lobes").visible, "The hollow rest hides the lobes");
        assertFalse(chest.getChild("vessel_mass").visible, "The hollow rest hides the heart mass");
        assertTrue(chest.getChild("vessel_cords").visible, "Dried cords always show");
        assertFalse(chest.getChild("right_arm").getChild("right_forearm").getChild("spike_patch_0").visible);
        model.pose(still(), .8F, 4, 1000, 0, 20, 0, 0, 0, 0);
        var lobes = chest.getChild("vessel_lobes");
        assertTrue(lobes.visible && chest.getChild("vessel_mass").visible);
        assertTrue(lobes.xScale >= .8F && lobes.xScale <= 1.15F);
        assertTrue(chest.getChild("right_arm").getChild("right_forearm").getChild("spike_patch_0").visible);
    }

    /** A stance at meter fraction {@code f} with nothing playing over it. */
    private static float[] rest(float f) {
        float[] a = still();
        HemorathModel.stance(a, f, 40, 0, 0, 1);
        return a;
    }

    private static List<List<Float>> snapshot(ModelPart root) {
        return root.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList();
    }

    private static Vector3f at(Vector3f point, ModelPart... chain) {
        PoseStack pose = new PoseStack();
        for (var part : chain) part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(point.div(16)).mul(16);
    }

    private static Vector3f direction(Vector3f axis, ModelPart... chain) {
        PoseStack pose = new PoseStack();
        for (var part : chain) part.translateAndRotate(pose);
        return pose.last().pose().transformDirection(axis);
    }

    private static int total(HemorathMove move) {
        return move.timeline(new SaintMoveInputs(1, 0, 0, 1)).total();
    }

    @Test
    void everyTrackKeepsTheStaffOneRigidPoleAndReturnsToTheSameIdle() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        var chest = hips.getChild("chest");
        var arm = chest.getChild("left_arm");
        var forearm = arm.getChild("left_forearm");
        var hand = forearm.getChild("left_hand");
        var staff = hand.getChild("staff");
        model.pose(rest(0), 0, 0, 1000, 0, 45, 0, 0, 10, 5);
        var expected = snapshot(root);
        List<float[]> poses = new ArrayList<>();
        for (var move : HemorathMove.values())
            for (int tick = 0; tick <= 150; tick += 5) {
                float[] a = rest(.5F);
                HemorathModel.move(a, move, tick, 0);
                poses.add(a);
            }
        for (int opening = 1; opening <= 8; opening++)
            for (int tick = 0; tick <= 50; tick += 10) {
                float[] a = rest(.8F);
                HemorathModel.opening(a, opening, tick);
                poses.add(a);
            }
        for (int tick = 0; tick <= 240; tick += 10) {
            float[] a = rest(.2F), b = rest(.9F), c = rest(.9F), d = rest(tick / 240F);
            HemorathModel.ending(a, Math.min(125, tick));
            HemorathModel.vesselEmpties(b, Math.min(50, tick));
            HemorathModel.lastThirst(c, tick);
            HemorathModel.stalk(d, tick / 240F, tick * .8F, .7F, 0, .3F, tick, 0);
            poses.add(a);
            poses.add(b);
            poses.add(c);
            poses.add(d);
        }
        for (float[] a : poses) {
            model.pose(a, .9F, 9, 2, 5, 33, 2, .5F, 10, 5);
            float plant = Math.max(0, Math.min(1, a[HemorathModel.STAFF_PLANT]));
            Vector3f foot = at(new Vector3f(0, model.staffLength, 0), top, hips, chest, arm, forearm, hand, staff);
            Vector3f axis = direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff);
            assertEquals(1, axis.length(), 1e-4F, "the staff is one rigid pole");
            if (plant >= 1) {
                Vector3f target = new Vector3f(model.staffFootRest).add(a[HemorathModel.STAFF_FOOT_X],
                        Math.min(1.5F, a[HemorathModel.STAFF_FOOT_Y]), a[HemorathModel.STAFF_FOOT_Z]);
                var pivot = staff.getInitialPose();
                Vector3f grip = at(new Vector3f(pivot.x, pivot.y, pivot.z), top, hips, chest, arm, forearm, hand);
                Vector3f toward = new Vector3f(target).sub(grip).normalize();
                assertEquals(1, axis.dot(toward), 1e-3F, "a planted staff points from the grip to its foot");
                if (Math.abs(target.distance(grip) - model.staffLength) < HemorathModel.GRIP_SLIDE - .1F)
                    assertEquals(0, foot.distance(target), .05F, "the grip slides so the planted foot stays put");
            } else if (plant <= 0) {
                Vector3f free = new Vector3f(0, 1, 0).rotateX(a[HemorathModel.STAFF_X]).rotateZ(a[HemorathModel.STAFF_Z]);
                assertEquals(1, axis.dot(free), 1e-3F, "a lifted staff holds the angle its move gives it");
            }
        }
        model.pose(rest(0), 0, 0, 1000, 0, 45, 0, 0, 10, 5);
        assertEquals(expected, snapshot(root));
    }

    @Test
    void atRestTheStaffStandsPlumbOnTheFloor() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        var chest = hips.getChild("chest");
        var arm = chest.getChild("left_arm");
        var forearm = arm.getChild("left_forearm");
        var hand = forearm.getChild("left_hand");
        var staff = hand.getChild("staff");
        model.pose(rest(.5F), .5F, 0, 1000, 0, 20, 0, 0, 0, 0);
        assertEquals(24, at(new Vector3f(0, model.staffLength, 0), top, hips, chest, arm, forearm, hand, staff).y, .3F);
        model.pose(still(), 0, 0, 1000, 0, 20, 0, 0, 0, 0);
        assertEquals(1, direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff).y, 1e-3F);
    }

    @Test
    void theKneesFoldAndTheHipsSinkUntilTheSolesMeetTheFloor() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        model.pose(still(), 0, 0, 1000, 0, 20, 0, 0, 0, 0);
        float restSole = sole(top, hips), restHips = hips.y;
        float[] crouched = still();
        crouched[HemorathModel.CROUCH] = 1.2F;
        model.pose(crouched, 0, 0, 1000, 0, 20, 0, 0, 0, 0);
        assertTrue(hips.y > restHips + 1, "a crouch lowers him");
        assertEquals(restSole, sole(top, hips), .05F, "the lower sole stays on the floor");
        float[] stepping = still();
        stepping[HemorathModel.STEP_L] = -.4F;
        stepping[HemorathModel.LIFT_L] = 1;
        model.pose(stepping, 0, 0, 1000, 0, 20, 0, 0, 0, 0);
        assertEquals(restSole, sole(top, hips), .05F, "the planted foot holds him while the other swings");
    }

    private static float sole(ModelPart top, ModelPart hips) {
        float lowest = -Float.MAX_VALUE;
        for (String side : new String[]{"left", "right"}) {
            var leg = hips.getChild(side + "_leg");
            lowest = Math.max(lowest, at(new Vector3f(0, 6.25F, 0), top, hips, leg, leg.getChild(side + "_shin")).y);
        }
        return lowest;
    }

    @Test
    void theHangingChainsStayPlumbWhenHeBowsAndTheStaffSwings() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        var chest = hips.getChild("chest");
        float[] a = rest(.5F);
        a[HemorathModel.BOW] += .6F;
        a[HemorathModel.LEAN] += .2F;
        a[HemorathModel.HIP_SWAY] += .1F;
        model.pose(a, .5F, 0, 1000, 0, 20, 0, 0, 0, 0);
        for (String name : new String[]{"left_shoulder_chain", "right_shoulder_chain_front"})
            assertEquals(1, direction(new Vector3f(0, 1, 0), top, hips, chest, chest.getChild(name)).y, 3e-3F,
                    name + " hangs plumb but for its idle sway");
        var halo = chest.getChild("halo");
        assertEquals(1, direction(new Vector3f(0, 1, 0), top, hips, chest, halo, halo.getChild("halo_chain_left")).y,
                3e-3F);
        a[HemorathModel.STAFF_PLANT] = 0;
        a[HemorathModel.STAFF_X] = 1.2F;
        model.pose(a, .5F, 0, 1000, 0, 20, 0, 0, 0, 0);
        var arm = chest.getChild("left_arm");
        var forearm = arm.getChild("left_forearm");
        var hand = forearm.getChild("left_hand");
        var staff = hand.getChild("staff");
        var crown = staff.getChild("staff_crown");
        assertEquals(1, direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff, crown,
                crown.getChild("staff_chain_right")).y, 3e-3F, "the crown chains hang plumb off a swung staff");
    }

    @Test
    void theLiftedHaloRidesTheClaw() {
        var root = HemorathModel.createBodyLayer().bakeRoot();
        var model = new HemorathModel<>(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        var chest = hips.getChild("chest");
        float[] a = rest(.5F);
        a[HemorathModel.R_ARM_X] -= 2.6F;
        a[HemorathModel.HALO_HELD] = 1;
        model.pose(a, .5F, 0, 1000, 0, 20, 0, 0, 0, 0);
        var arm = chest.getChild("right_arm");
        var forearm = arm.getChild("right_forearm");
        Vector3f grip = at(new Vector3f(0, 3, -.25F), top, hips, chest, arm, forearm, forearm.getChild("right_hand"));
        var halo = chest.getChild("halo");
        Vector3f centre = at(new Vector3f(), top, hips, chest, halo);
        assertEquals(6.25F, centre.distance(grip), .05F, "the claw holds the ring by its lower edge");
        assertTrue(centre.y < grip.y, "the ring stands above the claw");
    }

    @Test
    void everyMoveRisesOutOfTheStanceAndSettlesBackIntoIt() {
        for (var move : HemorathMove.values()) {
            if (move == HemorathMove.LAST_THIRST) continue;
            float[] end = still();
            HemorathModel.move(end, move, total(move), 0);
            for (int i = 0; i < HemorathModel.SECONDARY; i++)
                assertEquals(0, end[i], 1e-4F, move + " ends with channel " + i + " at rest");
            // the Rupture carries on from the Pulse's fold and the Crown Fall from the caught halo
            if (move == HemorathMove.MARROW_RUPTURE || move == HemorathMove.CROWN_FALL) continue;
            float[] start = still();
            HemorathModel.move(start, move, 0, 0);
            for (int i = 0; i < HemorathModel.SECONDARY; i++)
                assertEquals(0, start[i], 1e-4F, move + " starts with channel " + i + " at rest");
        }
    }

    @Test
    void everyStrikeCommitsFastAndFollowsThroughPastTheLine() {
        Object[][] strikes = {{HemorathMove.RAKING_CLAW, HemorathModel.R_ARM_X},
                {HemorathMove.BACKHAND, HemorathModel.R_ARM_Y}, {HemorathMove.STAFF_HEEL, HemorathModel.STAFF_X},
                {HemorathMove.STAFF_FALL, HemorathModel.STAFF_X}, {HemorathMove.IRON_RAKE, HemorathModel.R_ARM_X},
                {HemorathMove.CHAINED_DEBT, HemorathModel.R_ARM_X}, {HemorathMove.TITHE_RECKONING, HemorathModel.R_ARM_X},
                {HemorathMove.SPIKED_HALO, HemorathModel.R_ARM_Y}, {HemorathMove.CROWN_FALL, HemorathModel.BOW},
                {HemorathMove.IRON_THICKET, HemorathModel.R_ARM_X}, {HemorathMove.EXSANGUINATING_TIDE, HemorathModel.BOW},
                {HemorathMove.RIB_BURST, HemorathModel.RIBS}, {HemorathMove.EMPTY_PULSE, HemorathModel.RIBS}};
        for (Object[] strike : strikes) {
            var move = (HemorathMove) strike[0];
            int channel = (int) strike[1];
            float commit = move.baseWindup();
            float windup = 0, swing = 0;
            for (float t = 0; t < commit - 4; t += .1F) windup = Math.max(windup, speed(move, channel, t));
            for (float t = commit - 1.5F; t < commit + 3; t += .1F) swing = Math.max(swing, speed(move, channel, t));
            assertTrue(swing > windup * 2.5F, move + ": the strike is far faster than the wind-up (" + swing + " against "
                    + windup + ")");
            float before = value(move, channel, commit - 1), furthest = 0, when = commit;
            for (float t = commit; t < commit + 8; t += .1F) {
                float gone = Math.abs(value(move, channel, t) - before);
                if (gone > furthest) {
                    furthest = gone;
                    when = t;
                }
            }
            assertTrue(when >= commit + 1.4F, move + ": the swing carries on past the commit (" + when + ")");
        }
    }

    private static float value(HemorathMove move, int channel, float t) {
        float[] a = still();
        HemorathModel.move(a, move, t, 0);
        return a[channel];
    }

    private static float speed(HemorathMove move, int channel, float t) {
        return Math.abs(value(move, channel, t + .05F) - value(move, channel, t - .05F)) / .1F;
    }

    @Test
    void theStanceSagsAndSwellsWithoutASingleStep() {
        float[] last = rest(0);
        for (int i = 1; i <= 100; i++) {
            float[] next = rest(i / 100F);
            for (int c = 0; c < HemorathModel.SECONDARY; c++)
                assertEquals(last[c], next[c], .08F, "channel " + c + " at " + i + "%");
            last = next;
        }
        float[] hollow = rest(0), full = rest(1);
        assertTrue(hollow[HemorathModel.BOW] > full[HemorathModel.BOW], "hollow, he hunches");
        assertTrue(full[HemorathModel.RIBS] > hollow[HemorathModel.RIBS], "full, his ribs are held wide");
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
        assertTrue(lit > 0 && dark > lit * 3, "Only vessel core lines and spike tips should glow");
    }
}
