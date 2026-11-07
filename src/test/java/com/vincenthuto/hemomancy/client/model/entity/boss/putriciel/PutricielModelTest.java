package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage;
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

class PutricielModelTest {
    private static PutricielModel<?> model(ModelPart root) {
        return new PutricielModel<>(root);
    }

    private static PutricielAnimState state(PutricielMove move, float t, PutricielStage stage, int opening, float sinceAccepted) {
        var state = PutricielAnimState.detached();
        state.set(move == null ? 0 : move.id(), move == null ? -1 : t, stage, 40, -1, opening, opening == 0 ? -1 : 10, .4F, 2, sinceAccepted);
        return state;
    }

    private static PutricielStage stage(PutricielMove move) {
        return move != null && move.fire() ? PutricielStage.BURNING : PutricielStage.ROT;
    }

    /** One frame for a detached state: the stance, the gait and the track, laid on the rig (no blend, no springs). */
    private static float[] pose(PutricielModel<?> model, PutricielAnimState state, float stride, float walking) {
        float[] a = PutricielModel.frame(state, 40, stride, walking);
        model.pose(a, state, 40, 10, 5);
        return a;
    }

    private static float[] still() {
        return new float[PutricielModel.CHANNELS];
    }

    private static List<List<Float>> snapshot(ModelPart root) {
        return root.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot, p.xScale, p.yScale, p.zScale)).toList();
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

    /** A move's authored length (its base timeline; a kneeling beat held to its 50-tick opening). */
    private static float length(PutricielMove move) {
        return PutricielTracks.authoredTimeline(move, SaintMoveInputs.NONE).total();
    }

    @Test
    void anAcceptedOfferingRecoilsTheChest() {
        var root = PutricielModel.createBodyLayer().bakeRoot();
        var model = model(root);
        var chest = root.getChild("root").getChild("hips").getChild("chest");
        pose(model, state(PutricielMove.OFFERING, 30, PutricielStage.BURNING, PutricielOpenings.OFFERING_ID, 1000), 0, 0);
        float kneeling = chest.xRot;
        var recoiling = state(PutricielMove.OFFERING, 30, PutricielStage.BURNING, PutricielOpenings.OFFERING_ID, 2);
        assertEquals(PutricielTracks.RECOIL, recoiling.track().id());
        pose(model, recoiling, 0, 0);
        assertTrue(kneeling - chest.xRot > .2F, "the chest is thrown back beneath an accepted offering");
    }

    @Test
    void theFireStraightensTheHunch() {
        var root = PutricielModel.createBodyLayer().bakeRoot();
        var model = model(root);
        var chest = root.getChild("root").getChild("hips").getChild("chest");
        pose(model, state(null, 0, PutricielStage.ROT, 0, 1000), 0, 0);
        float rot = chest.xRot;
        pose(model, state(null, 0, PutricielStage.BURNING, 0, 1000), 0, 0);
        assertTrue(rot - chest.xRot > .2F, "18 degrees hunched in the rot, 2 in the fire");
    }

    @Test
    void theOfferingHoldsThePalmWhereTheServerAimsTheAbsolution() {
        var root = PutricielModel.createBodyLayer().bakeRoot();
        var model = model(root);
        pose(model, state(PutricielMove.OFFERING, 30, PutricielStage.BURNING, PutricielOpenings.OFFERING_ID, 1000), 0, 0);
        Vector3f palm = model.palm();
        // the renderer's offset at scale 2: model -z is ahead, y is measured down from 1.501
        float ahead = -palm.z * 2, up = (1.501F - palm.y) * 2;
        assertEquals(0.7F, ahead, .15F, "PutricielEntity.PALM_FORWARD");
        assertEquals(2.45F, up, .15F, "PutricielEntity.PALM_UP");
        assertEquals(0.0F, -palm.x * 2, .2F, "the palm is held at the body's centre line");
    }

    @Test
    void everyTrackKeepsTheStaffOneRigidPoleThePalmUpAndReturnsToTheSameIdle() {
        var root = PutricielModel.createBodyLayer().bakeRoot();
        var model = model(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        var chest = hips.getChild("chest");
        var arm = chest.getChild("left_arm");
        var forearm = arm.getChild("left_forearm");
        var hand = forearm.getChild("left_hand");
        var staff = hand.getChild("staff");
        var rightArm = chest.getChild("right_arm");
        var rightForearm = rightArm.getChild("right_forearm");
        var rightHand = rightForearm.getChild("right_hand");
        pose(model, state(null, 0, PutricielStage.ROT, 0, 1000), 0, 0);
        var expected = snapshot(root);
        List<PutricielAnimState> states = new ArrayList<>();
        for (PutricielMove move : PutricielMove.values())
            for (int tick = 0; tick <= 150; tick += 5) states.add(state(move, tick, stage(move), 0, 1000));
        for (int opening = 1; opening <= 6; opening++)
            for (int tick = 0; tick <= 60; tick += 6) {
                var s = PutricielAnimState.detached();
                s.set(0, -1, opening == PutricielOpenings.OFFERING_ID ? PutricielStage.BURNING : PutricielStage.ROT, 40, -1,
                        opening, tick, .4F, 2, 1000);
                states.add(s);
            }
        for (int tick = 0; tick <= 100; tick += 5) {
            var ending = PutricielAnimState.detached();
            ending.set(0, -1, PutricielStage.BURNING, 40, tick, 0, -1, 0, 5, 1000);
            states.add(ending);
            var rising = state(null, 0, PutricielStage.ROT, 0, 1000);
            rising.setRise(Math.min(59, tick));
            states.add(rising);
            states.add(state(PutricielMove.OFFERING, 30, PutricielStage.BURNING, PutricielOpenings.OFFERING_ID, tick / 6.0F));
        }
        for (var state : states) {
            for (float walking : new float[]{0, .5F}) {
                float[] a = pose(model, state, 17 * walking, walking);
                for (var part : root.getAllParts().toList())
                    assertTrue(Float.isFinite(part.xRot) && Float.isFinite(part.yRot) && Float.isFinite(part.zRot)
                            && Float.isFinite(part.x) && Float.isFinite(part.y) && Float.isFinite(part.z), "finite at " + state.track());
                float plant = Math.max(0, Math.min(1, a[PutricielModel.STAFF_PLANT]));
                Vector3f axis = direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff);
                assertEquals(1, axis.length(), 1e-4F, "the staff is one rigid pole");
                if (plant >= 1) {
                    Vector3f target = new Vector3f(model.staffFootRest).add(a[PutricielModel.STAFF_FOOT_X],
                            Math.min(1.5F, a[PutricielModel.STAFF_FOOT_Y]), a[PutricielModel.STAFF_FOOT_Z]);
                    var pivot = staff.getInitialPose();
                    Vector3f grip = at(new Vector3f(pivot.x, pivot.y, pivot.z), top, hips, chest, arm, forearm, hand);
                    assertEquals(1, axis.dot(new Vector3f(target).sub(grip).normalize()), 1e-3F,
                            "a planted staff points from the grip to its foot at " + state.track());
                    if (Math.abs(target.distance(grip) - model.staffLength) < PutricielModel.GRIP_SLIDE - .1F) {
                        Vector3f foot = at(new Vector3f(0, model.staffLength, 0), top, hips, chest, arm, forearm, hand, staff);
                        assertEquals(0, foot.distance(target), .05F, "the grip slides so the planted foot stays put");
                    }
                } else if (plant <= 0) {
                    Vector3f free = PutricielModel.freeAxis(a[PutricielModel.STAFF_TILT], a[PutricielModel.STAFF_AZ]);
                    assertEquals(1, axis.dot(free), 1e-3F, "a lifted staff points where its move says at " + state.track());
                }
                if (Math.abs(a[PutricielModel.PALM]) < 1e-4F)
                    assertEquals(-1, direction(new Vector3f(0, -1, 0), top, hips, chest, rightArm, rightForearm, rightHand).y,
                            1e-3F, "the offering palm stays up at " + state.track());
            }
        }
        pose(model, state(null, 0, PutricielStage.ROT, 0, 1000), 0, 0);
        assertEquals(expected, snapshot(root));
    }

    @Test
    void atRestTheStaffStandsPlumbOnTheFloorAndTheCensersHangPlumb() {
        var root = PutricielModel.createBodyLayer().bakeRoot();
        var model = model(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        var chest = hips.getChild("chest");
        var arm = chest.getChild("left_arm");
        var forearm = arm.getChild("left_forearm");
        var hand = forearm.getChild("left_hand");
        var staff = hand.getChild("staff");
        var head = staff.getChild("staff_head");
        var rest = state(null, 0, PutricielStage.ROT, 0, 1000);
        float[] a = still();
        a[PutricielModel.STAFF_PLANT] = 1;
        model.pose(a, rest, 40, 0, 0);
        assertEquals(24, at(new Vector3f(0, model.staffLength, 0), top, hips, chest, arm, forearm, hand, staff).y, .3F);
        assertEquals(1, direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff).y, 1e-3F);
        for (String side : new String[]{"left", "right"})
            assertEquals(1, direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff, head,
                    head.getChild("staff_chain_" + side)).y, 1e-3F, side + " censer hangs plumb");
        // a bowed, leaning body: the censers still hang true
        a[PutricielModel.BOW] = .6F;
        a[PutricielModel.LEAN] = .2F;
        model.pose(a, rest, 40, 0, 0);
        assertEquals(1, direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff, head,
                head.getChild("staff_chain_left")).y, 1e-3F);
        // swung out and flung: the chain follows the staff's head outward
        a[PutricielModel.STAFF_PLANT] = 0;
        a[PutricielModel.STAFF_TILT] = 1.2F;
        a[PutricielModel.CENSER_FLING] = 1.0F;
        model.pose(a, rest, 40, 0, 0);
        Vector3f chain = direction(new Vector3f(0, 1, 0), top, hips, chest, arm, forearm, hand, staff, head,
                head.getChild("staff_chain_right"));
        assertTrue(chain.z < -.5F, "a flung censer swings out ahead along the staff (" + chain + ")");
    }

    private static float lowestContact(ModelPart top, ModelPart hips) {
        float lowest = -Float.MAX_VALUE;
        for (String side : new String[]{"left", "right"}) {
            var thigh = hips.getChild(side + "_thigh");
            var shin = thigh.getChild(side + "_shin");
            var foot = shin.getChild(side + "_foot");
            lowest = Math.max(lowest, at(new Vector3f(0, .25F, 0), top, hips, thigh, shin, foot).y);
            lowest = Math.max(lowest, at(new Vector3f(), top, hips, thigh, shin).y + PutricielGait.KNEE_PAD);
        }
        return lowest;
    }

    @Test
    void theKneesFoldAndTheHipsSinkUntilASoleOrAKneeMeetsTheFloor() {
        var root = PutricielModel.createBodyLayer().bakeRoot();
        var model = model(root);
        var top = root.getChild("root");
        var hips = top.getChild("hips");
        var rest = state(null, 0, PutricielStage.ROT, 0, 1000);
        model.pose(still(), rest, 40, 0, 0);
        float floor = lowestContact(top, hips), restHips = hips.y;
        assertEquals(24, floor, .3F, "at rest the soles are on the floor");
        for (int channel : new int[]{PutricielModel.CROUCH, PutricielModel.KNEEL, PutricielModel.KNEEL_BOTH}) {
            float[] a = still();
            a[channel] = channel == PutricielModel.CROUCH ? 2.0F : 1.0F;
            model.pose(a, rest, 40, 0, 0);
            assertTrue(hips.y > restHips + 1, "it lowers the body (" + channel + ")");
            assertEquals(floor, lowestContact(top, hips), .12F, "a sole or a knee stays on the floor (" + channel + ")");
        }
        float[] stepping = still();
        stepping[PutricielModel.STEP_L] = -.4F;
        stepping[PutricielModel.LIFT_L] = 1;
        model.pose(stepping, rest, 40, 0, 0);
        assertEquals(floor, lowestContact(top, hips), .12F, "the planted foot holds him while the other swings");
    }

    @Test
    void everyMoveRisesOutOfTheStanceAndSettlesBackIntoIt() {
        for (var move : PutricielMove.values()) {
            float length = length(move);
            for (float t : new float[]{0, length}) {
                float[] a = still();
                PutricielModel.move(a, move, t, length, 5);
                for (int i = 0; i < PutricielModel.SECONDARY; i++)
                    assertEquals(0, a[i], 1e-3F, move + " at " + t + ": channel " + i + " at rest");
            }
        }
    }

    @Test
    void everyStrikeCommitsFastAndFollowsThroughPastTheLine() {
        Object[][] strikes = {{PutricielMove.CENSER_SWING, PutricielModel.STAFF_AZ},
                {PutricielMove.CENSER_BACKSWING, PutricielModel.STAFF_AZ}, {PutricielMove.CENSER_DROP, PutricielModel.STAFF_TILT},
                {PutricielMove.OFFERING_GRASP, PutricielModel.R_ARM_X}, {PutricielMove.ROT_BREATH, PutricielModel.HEAD},
                {PutricielMove.SAC_SWELL, PutricielModel.BOW}, {PutricielMove.ROOT_SNARE, PutricielModel.BOW},
                {PutricielMove.IGNITION, PutricielModel.SPREAD}, {PutricielMove.PURGING_SWEEP, PutricielModel.STAFF_TILT},
                {PutricielMove.CENSER_CAST, PutricielModel.STAFF_TILT}, {PutricielMove.CAUTERISING_LANE, PutricielModel.R_ARM_Y},
                {PutricielMove.PYRE_HEART, PutricielModel.SPREAD}, {PutricielMove.PYRE_LINES, PutricielModel.FINGERS}};
        for (Object[] strike : strikes) {
            var move = (PutricielMove) strike[0];
            int channel = (int) strike[1];
            float commit = PutricielTracks.authoredTimeline(move, SaintMoveInputs.NONE).windup();
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

    private static float value(PutricielMove move, int channel, float t) {
        float[] a = still();
        PutricielModel.move(a, move, t, length(move), 5);
        return a[channel];
    }

    private static float speed(PutricielMove move, int channel, float t) {
        return Math.abs(value(move, channel, t + .05F) - value(move, channel, t - .05F)) / .1F;
    }

    @Test
    void theStanceMovesFromTheRotToTheFireWithoutAStep() {
        float[] last = still();
        PutricielModel.stance(last, 0, .5F, 40, 0, 0, 0, 1);
        for (int i = 1; i <= 100; i++) {
            float[] next = still();
            PutricielModel.stance(next, i / 100F, .5F, 40, 0, 0, 0, 1);
            for (int c = 0; c < PutricielModel.SECONDARY; c++)
                assertEquals(last[c], next[c], .05F, "channel " + c + " at " + i + "%");
            last = next;
        }
        float[] rot = still(), fire = still();
        PutricielModel.stance(rot, 0, .9F, 40, 0, 0, 0, 1);
        PutricielModel.stance(fire, 1, .9F, 40, 0, 0, 0, 1);
        assertTrue(rot[PutricielModel.CROUCH] > fire[PutricielModel.CROUCH], "the rot sinks onto soft knees");
        assertTrue(rot[PutricielModel.SHAKE] > 0, "a full Kindling trembles in the rot");
    }

    @Test
    void theShambleLurchesOnTheLeadFootAndTheStrideWalksEvenly() {
        float[] lurch = still(), even = still();
        float stride = 4.0F * PutricielGait.SHAMBLE_BLOCKS * (PutricielGait.LEAD_END + .08F);
        PutricielModel.gait(lurch, 0, stride, .6F, 0, 0, 0);
        assertTrue(lurch[PutricielModel.STEP_L] < -.3F, "the lead foot is out ahead");
        assertTrue(lurch[PutricielModel.BOW] > .05F, "the weight is thrown over it");
        float q = 4.0F * PutricielGait.STRIDE_BLOCKS * .25F;
        PutricielModel.gait(even, 1, q, .6F, 0, 0, 0);
        assertTrue(even[PutricielModel.LIFT_L] > .5F, "the fire's knee lifts through the swing");
        assertEquals(0, even[PutricielModel.LIFT_R], 1e-4F);
    }

    @Test
    void exportedFaceUvsMatchTheBakedJavaModel() {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/PutricielModel.bbmodel");
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
        PutricielModel.createBodyLayer().bakeRoot().render(new PoseStack(), collector, 0, 0, -1);
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
    void theRigCarriesLegsTheFaceTheSeamsEightSacsAndFiveNodules() {
        var hips = PutricielModel.createBodyLayer().bakeRoot().getChild("root").getChild("hips");
        var chest = hips.getChild("chest");
        for (String side : new String[]{"left", "right"}) {
            assertNotNull(hips.getChild(side + "_thigh").getChild(side + "_shin").getChild(side + "_foot"));
        }
        assertNotNull(chest.getChild("head").getChild("purifier_face"));
        assertNotNull(chest.getChild("head").getChild("face_vines"));
        assertNotNull(chest.getChild("eschar_seams"));
        assertNotNull(chest.getChild("spore_cluster").getChild("spore_3"));
        assertNotNull(chest.getChild("back_colony").getChild("back_spore_0"));
        assertNotNull(chest.getChild("back_colony").getChild("back_spore_1"));
        var hand = chest.getChild("right_arm").getChild("right_forearm").getChild("right_hand");
        for (int i = 0; i < 5; i++) assertNotNull(hand.getChild("palm_nodule_" + i));
        assertNotNull(hand.getChild("offering_drips"));
        var staff = chest.getChild("left_arm").getChild("left_forearm").getChild("left_hand").getChild("staff");
        assertNotNull(staff.getChild("staff_foot"));
        assertNotNull(staff.getChild("staff_head").getChild("staff_chain_left").getChild("censer_left"));
        assertNotNull(staff.getChild("staff_head").getChild("staff_chain_right").getChild("censer_right"));
    }

    @Test
    void savedGeometryUsesTheQuarterUnitGridAndHalfAUnitAtLeast() {
        PutricielModel.createBodyLayer().bakeRoot().getAllParts().forEach(part -> {
            for (float c : new float[]{part.x, part.y, part.z}) assertEquals(Math.round(c * 4), c * 4, .0001F);
            for (var cube : part.cubes) {
                for (float c : new float[]{cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ})
                    assertEquals(Math.round(c * 4), c * 4, .0001F);
                assertTrue(cube.maxX - cube.minX >= .5F);
                assertTrue(cube.maxY - cube.minY >= .5F);
                assertTrue(cube.maxZ - cube.minZ >= .5F);
            }
        });
    }

    @Test
    void sixteenPixelsPerBlockAtRenderScale() {
        // 2 texture pixels a model unit (putriciel.py kit.SCALE), 16 units a block, drawn at PutricielVisualRules.SCALE
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/PutricielModel.bbmodel");
        int width = project.getAsJsonObject("resolution").get("width").getAsInt();
        assertEquals(16.0F, 2 * 16 / com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielVisualRules.SCALE, 1.0E-6F);
        assertTrue(width >= 256);
    }

    @Test
    void embeddedTextureMatchesRuntimeAndGlowStaysOnVisibleSurfaces() throws Exception {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/PutricielModel.bbmodel");
        var path = ResourceReader.path("assets/hemomancy/textures/entity/boss/putriciel/putriciel.png");
        var embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(Files.readAllBytes(path), Base64.getDecoder().decode(embedded.split(",", 2)[1]));
        assertEquals(project.getAsJsonArray("elements").size(), PutricielModel.createBodyLayer().bakeRoot()
                .getAllParts().mapToInt(p -> p.cubes.size()).sum());
        var base = ImageIO.read(path.toFile());
        var glow = ImageIO.read(ResourceReader.path("assets/hemomancy/textures/entity/boss/putriciel/putriciel_glow.png").toFile());
        assertEquals(base.getWidth(), glow.getWidth());
        assertEquals(base.getHeight(), glow.getHeight());
        int lit = 0, dark = 0;
        for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
            if ((glow.getRGB(x, y) >>> 24) != 0) {
                assertNotEquals(0, base.getRGB(x, y) >>> 24);
                lit++;
            } else if ((base.getRGB(x, y) >>> 24) != 0) dark++;
        }
        assertTrue(lit > 0 && dark > lit * 3, "Cloth and fungal shelves must frame the smaller luminous spores");
    }

    // ── the purifier on fire (revision 2026-11-06, R1) and the colour pass (R3) ──

    private static Vector3f[][] restAnchors(PutricielModel<?> model) {
        Vector3f[] points = new Vector3f[PutricielFireAnchors.COUNT], normals = new Vector3f[PutricielFireAnchors.COUNT];
        model.pose(still(), state(null, 0, PutricielStage.ROT, 0, 1000), 40, 0, 0);
        model.fireAnchors(points, normals);
        return new Vector3f[][]{points, normals};
    }

    @Test
    void seventyFireAnchorsStandOnTheirRegionsPartsWithUnitNormals() {
        var model = model(PutricielModel.createBodyLayer().bakeRoot());
        Vector3f[][] anchors = restAnchors(model);
        assertEquals(70, PutricielFireAnchors.COUNT, "R1.1's seventy anchors");
        int[] perRegion = new int[PutricielFireAnchors.Region.values().length];
        java.util.Set<Integer> orders = new java.util.HashSet<>();
        for (int i = 0; i < PutricielFireAnchors.COUNT; i++) {
            var region = PutricielFireAnchors.Region.byOrdinal(PutricielModel.ANCHOR_REGIONS[i]);
            perRegion[region.ordinal()]++;
            String part = PutricielModel.ANCHOR_PARTS[i];
            var partRegion = PutricielFireAnchors.regionOf(part);
            if (partRegion == null) assertTrue(part.equals("back_colony") || part.equals("eschar_seams"), part + " has no region");
            else assertEquals(region, partRegion, part + "'s anchor burns in its part's region");
            assertEquals(1.0F, anchors[1][i].length(), 1.0E-3F, part + "'s normal is a unit vector");
            assertTrue(Float.isFinite(anchors[0][i].x + anchors[0][i].y + anchors[0][i].z), part + " is posed");
            assertTrue(orders.add(PutricielModel.ANCHOR_ORDERS[i]), "each anchor has its own kindle order");
            float size = PutricielFireAnchors.size(i);
            assertTrue(size >= 0.0F && size <= 1.0F, part + "'s size sits in its region's tongue range");
        }
        assertArrayEquals(new int[]{12, 2, 19, 15, 9, 6, 7}, perRegion, "staff, censers, back, shoulders, crown, chest, hem");
        for (int order = 1; order < PutricielFireAnchors.COUNT; order++)
            assertTrue(PutricielFireAnchors.kindleTick(order) >= PutricielFireAnchors.kindleTick(order - 1), "kindle order is by tick");
        assertEquals(16.0F, PutricielFireAnchors.kindleTick(0), 1.0E-4F, "the chest tears and the grip wrap catch first, at 16");
        assertTrue(PutricielFireAnchors.kindleTick(PutricielFireAnchors.COUNT - 1) <= 29.0F, "the crown's last spire by 29");
    }

    @Test
    void theStaffBurnsAlongItsShaftFromTheGripWrapToTheHead() {
        var model = model(PutricielModel.createBodyLayer().bakeRoot());
        Vector3f[][] anchors = restAnchors(model);
        Vector3f foot = model.staffFootPoint(), head = model.staffHeadPoint();
        Vector3f axis = new Vector3f(head).sub(foot);
        float length = axis.length();
        axis.normalize();
        int grip = -1;
        for (int i = 0; i < PutricielFireAnchors.COUNT; i++) if (PutricielModel.ANCHOR_PARTS[i].equals("staff_wrap_2")) grip = i;
        assertTrue(grip >= 0, "the grip wrap carries an anchor");
        float gripAt = new Vector3f(anchors[0][grip]).sub(foot).dot(axis);
        List<float[]> above = new ArrayList<>();
        for (int i = 0; i < PutricielFireAnchors.COUNT; i++) {
            String part = PutricielModel.ANCHOR_PARTS[i];
            if (!part.startsWith("staff_wrap_") && !part.equals("staff_head")) continue;
            float along = new Vector3f(anchors[0][i]).sub(foot).dot(axis);
            assertTrue(along > -0.05F && along < length + 0.6F, part + " stands on the staff, between its foot and its head");
            if (along >= gripAt - 1.0E-3F) above.add(new float[]{along, PutricielModel.ANCHOR_KINDLE[i]});
        }
        assertTrue(above.size() >= 7, "three wraps, three prongs and the head bar's ends above the grip");
        above.sort((a, b) -> Float.compare(a[0], b[0]));
        for (int i = 1; i < above.size(); i++)
            assertTrue(above.get(i)[1] >= above.get(i - 1)[1] - 0.6F, "the fire climbs the shaft from the grip wrap to the head");
        assertEquals(16.0F, PutricielModel.ANCHOR_KINDLE[grip], 1.0E-4F, "the grip wrap catches first");
    }

    @Test
    void theStoleTheStaffWrapsAndTheMossExist() {
        var chest = PutricielModel.createBodyLayer().bakeRoot().getChild("root").getChild("hips").getChild("chest");
        assertNotNull(chest.getChild("stole_left"));
        assertNotNull(chest.getChild("stole_right"));
        var staff = chest.getChild("left_arm").getChild("left_forearm").getChild("left_hand").getChild("staff");
        for (int i = 0; i < 4; i++) assertFalse(staff.getChild("staff_wrap_" + i).cubes.isEmpty(), "wrap " + i);
        assertTrue(chest.getChild("back_colony").getChild("back_shelf_0").cubes.size() > 4, "a moss tuft at the shelf's root");
    }

    private static PutricielBodyFireRules.Moment ignition(float t) {
        return PutricielBodyFireRules.Moment.of(PutricielStage.IGNITING, t, 1.0F).moving(PutricielMove.IGNITION, t, 30, 6);
    }

    @Test
    void theGrowthsBlackenInKindleOrderAndStandAtOneByTheEmberingsSixtieth() {
        for (int i = 0; i < PutricielModel.GROWTH_COUNT; i++) {
            String part = PutricielModel.GROWTH_PARTS[i];
            assertEquals(1.0F, PutricielFireAnchors.growth(i, PutricielBodyFireRules.Moment.of(PutricielStage.ROT, 100, .5F)), 1.0E-5F, part);
            assertEquals(0.0F, PutricielFireAnchors.charAlpha(i, PutricielBodyFireRules.Moment.of(PutricielStage.ROT, 100, .5F)), 1.0E-5F, part);
            assertEquals(1.0F, PutricielFireAnchors.growth(i, ignition(9)), 1.0E-4F, part + " untouched before the first embers");
            assertEquals(0.35F, PutricielFireAnchors.growth(i, ignition(30)), 1.0E-3F, part + " burnt by the commit");
            var embering = PutricielBodyFireRules.Moment.of(PutricielStage.EMBERING, 60, 0);
            assertEquals(1.0F, PutricielFireAnchors.growth(i, embering), 1.0E-3F, part + " regrown by the Embering's tick 60");
            assertEquals(0.0F, PutricielFireAnchors.charAlpha(i, embering), 1.0E-3F, part + " clear by the Embering's tick 60");
            assertEquals(PutricielFireAnchors.kindleOrder(part), PutricielModel.GROWTH_ORDERS[i]);
        }
        // the chest's sacs before the back's root shelf, the root shelf before the outermost
        int sac = PutricielFireAnchors.kindleOrder("spore_0"), rootShelf = PutricielFireAnchors.kindleOrder("back_shelf_4"),
                outer = PutricielFireAnchors.kindleOrder("back_shelf_10");
        assertTrue(sac < rootShelf && rootShelf < outer, "kindle order " + sac + " < " + rootShelf + " < " + outer);
        int iRoot = PutricielFireAnchors.growthIndex("back_shelf_4"), iOuter = PutricielFireAnchors.growthIndex("back_shelf_10");
        assertTrue(PutricielFireAnchors.growth(iRoot, ignition(21)) < PutricielFireAnchors.growth(iOuter, ignition(21)) - 0.3F,
                "the fire reaches the colony's root before its outer shelves");
        var burnt = PutricielBodyFireRules.Moment.of(PutricielStage.BURNING, 400, 0);
        assertEquals(0.2F, PutricielFireAnchors.growth(iOuter, burnt), 1.0E-3F, "the outermost shelf eaten to 0.2 by the burn-down");
        assertEquals(0.35F, PutricielFireAnchors.growth(iRoot, burnt), 1.0E-3F);
        // the Embering: the root shelf regrows before the outer one, the knuckle before the shoulder
        var early = PutricielBodyFireRules.Moment.of(PutricielStage.EMBERING, 12, 0);
        assertTrue(PutricielFireAnchors.growth(iRoot, early) > PutricielFireAnchors.growth(iOuter, early), "regrows from the root outward");
        assertTrue(PutricielFireAnchors.regrowRank("knuckle_growth") < PutricielFireAnchors.regrowRank("right_shoulder_shelf_0"),
                "the arms regrow from their tips inward");
    }

    @Test
    void theModelBurnsItsGrowthsByKindleOrder() {
        var root = PutricielModel.createBodyLayer().bakeRoot();
        var model = model(root);
        var colony = root.getChild("root").getChild("hips").getChild("chest").getChild("back_colony");
        var ignition = state(PutricielMove.IGNITION, 21, PutricielStage.IGNITING, 0, 1000);
        pose(model, ignition, 0, 0);
        assertTrue(colony.getChild("back_shelf_4").xScale < colony.getChild("back_shelf_10").xScale - 0.3F,
                "the root shelf has burnt while the outermost waits for the fire");
        var embering = PutricielAnimState.detached();
        embering.set(0, -1, PutricielStage.EMBERING, 60, -1, 0, -1, 0, 2, 1000);
        model.pose(still(), embering, 40, 0, 0);
        for (int i = 0; i < 11; i++) assertEquals(1.0F, colony.getChild("back_shelf_" + i).xScale, 1.0E-3F, "shelf " + i + " regrown");
    }

    @Test
    void theFireAnchorsCarryTheirVelocityByRenderAge() {
        var state = PutricielAnimState.detached();
        int n = PutricielFireAnchors.COUNT;
        Vec3[] points = new Vec3[n], normals = new Vec3[n];
        for (int i = 0; i < n; i++) {
            points[i] = new Vec3(0, 1 + i * 0.01, 0);
            normals[i] = new Vec3(0, 1, 0);
        }
        state.recordFireAnchors(points, normals, Vec3.ZERO, 10);
        assertEquals(n, state.fireAnchors().size());
        assertEquals(0.0D, state.fireAnchors().get(0).speed(), 1.0E-9D, "at rest on the first frame");
        for (int frame = 1; frame <= 20; frame++) state.recordFireAnchors(points, normals, new Vec3(0.25 * frame, 0, 0), 10 + frame);
        assertEquals(0.25D, state.fireAnchors().get(5).velocity().x, 0.01D, "a quarter block a tick, from the render-age step");
        state.recordFireAnchors(points, normals, new Vec3(30, 0, 0), 5);
        assertEquals(0.0D, state.fireAnchors().get(5).speed(), 1.0E-9D, "a rewind starts again at rest");
        assertEquals(PutricielFireAnchors.Region.byOrdinal(PutricielModel.ANCHOR_REGIONS[3]), state.fireAnchors().get(3).region());
    }
}
