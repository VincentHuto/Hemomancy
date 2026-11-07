package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeOpenings;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeRestraintState;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeVisualRules;
import com.vincenthuto.hemomancy.support.ResourceReader;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SeraphaeModelTest {
    private static final Path ASSETS = ResourceReader.path("assets/hemomancy");
    private static final String[] TRUNKS = {"upper", "middle", "lower"};

    @Test
    void texelDensityIsSixteenPerBlockAtHerRenderScale() {
        assertEquals(16.0F, SeraphaeVisualRules.TEXELS * 16.0F / SeraphaeVisualRules.MODEL_SCALE, 1.0E-4F);
    }

    @Test
    void eachWingIsANerveTreeOfThreeJointedTrunksWithBranchesAndTips() {
        ModelPart body = SeraphaeModel.createBodyLayer().bakeRoot().getChild("root").getChild("body");
        for (String side : new String[]{"left", "right"}) {
            ModelPart wing = body.getChild(side + "_wing");
            assertTrue(wing.hasChild(side + "_wing_shackle"), side + " wing-root locator");
            for (String trunk : TRUNKS) {
                String stem = side + "_trunk_" + trunk;
                ModelPart segment = wing.getChild(stem + "_0");
                int segments = 1;
                while (segment.hasChild(stem + "_" + segments)) {
                    assertTrue(segment.hasChild(stem + "_" + (segments - 1) + "_branch_a"));
                    assertTrue(segment.hasChild(stem + "_" + (segments - 1) + "_branch_b"));
                    segment = segment.getChild(stem + "_" + segments);
                    segments++;
                }
                int expected = side.equals("right") && trunk.equals("lower") ? 4 : 3;
                assertEquals(expected, segments, stem + " segments (the right lower trunk keeps its kink)");
                ModelPart tip = segment.getChild(stem + "_tip");
                for (int k = 0; k < 4; k++) assertFalse(tip.getChild(stem + "_tip_" + k).cubes.isEmpty(), "dendrite " + k);
            }
        }
        float left = length(body.getChild("left_wing"), "left_trunk_middle");
        float right = length(body.getChild("right_wing"), "right_trunk_middle");
        assertEquals(3.0F, left - right, 1.0E-4F, "the right wing's middle trunk is three shorter");
    }

    private static float length(ModelPart wing, String stem) {
        float total = 0;
        ModelPart segment = wing.getChild(stem + "_0");
        for (int i = 1; ; i++) {
            total += segment.cubes.get(0).maxY - segment.cubes.get(0).minY;
            if (!segment.hasChild(stem + "_" + i)) return total;
            segment = segment.getChild(stem + "_" + i);
        }
    }

    @Test
    void shackleLocatorsExistAndFollowTheirLimbs() {
        var layer = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(layer);
        model.pose(new SeraphaePose(), 0, 0, 0, 0, 1);
        Vector3f rest = model.wristShackle(true);
        model.pose(new SeraphaePose().set(SeraphaePose.ARM_L_RAISE, 1.5F), 0, 0, 0, 0, 1);
        Vector3f raised = model.wristShackle(true);
        assertTrue(rest.distance(raised) > 0.2F, "the wrist iron moves with the raised arm");
        assertNotNull(model.waistPoint());
        assertNotNull(model.wingShackle(false));
    }

    @Test
    void nerveTipsLagTheRootOnTheirWave() {
        var layer = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(layer);
        ModelPart wing = layer.getChild("root").getChild("body").getChild("left_wing");
        model.pose(new SeraphaePose(), 45, 0, 0, 0, 7);
        float root = wing.getChild("left_trunk_middle_0").xRot;
        float next = wing.getChild("left_trunk_middle_0").getChild("left_trunk_middle_1").xRot;
        assertNotEquals(root, next, 1.0E-4F, "segments move on a delayed wave, not as one rigid sheet");
    }

    @Test
    void theSamePoseTwiceRestoresEveryJoint() {
        var layer = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(layer);
        model.pose(new SeraphaePose(), 40, 10, 5, 0, 3);
        var expected = layer.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList();
        SeraphaePose busy = new SeraphaePose().set(SeraphaePose.WRISTS_CROSS, 1).set(SeraphaePose.JITTER, 1)
                .set(SeraphaePose.WING_L_SPREAD, 1).set(SeraphaePose.KNEEL, 0.6F);
        model.pose(busy, 40, 10, 5, 0, 3);
        assertNotEquals(expected, layer.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList());
        model.pose(new SeraphaePose(), 40, 10, 5, 0, 3);
        assertEquals(expected, layer.getAllParts().map(p -> List.of(p.x, p.y, p.z, p.xRot, p.yRot, p.zRot)).toList());
    }

    @Test
    void wristChainsShowOnlyWhileSlippedAndHangDown() {
        var layer = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(layer);
        var body = model.root().getChild("body");
        int[] states = new int[SeraphaeRestraintState.SLOTS];
        states[SeraphaeRestraint.LEFT_WRIST.ordinal()] = SeraphaeRestraintState.SLIPPED;
        states[SeraphaeRestraint.RIGHT_WRIST.ordinal()] = SeraphaeRestraintState.HELD;
        int packed = SeraphaeRestraintState.pack(states);
        for (int tick = 0; tick <= 100; tick += 5) {
            SeraphaePose pose = new SeraphaePose().set(SeraphaePose.ARM_L_RAISE, tick / 60.0F)
                    .set(SeraphaePose.LEAN, tick / 300.0F);
            model.pose(pose, tick * 2.3F, 12, 5, packed, 2);
            var arm = body.getChild("left_arm");
            var forearm = arm.getChild("left_forearm");
            var chain = forearm.getChild("left_chain");
            assertTrue(chain.visible);
            assertFalse(body.getChild("right_arm").getChild("right_forearm").getChild("right_chain").visible);
            PoseStack stack = new PoseStack();
            for (var part : new ModelPart[]{model.root(), body, arm, forearm, chain}) part.translateAndRotate(stack);
            Vector3f down = stack.last().pose().transformDirection(new Vector3f(0, 1, 0));
            assertEquals(1, down.y, .002F, "tick=" + tick + " down=" + down);
        }
    }

    @Test
    void cubesAndPivotsStayOnTheHalfUnitGrid() {
        var root = SeraphaeModel.createBodyLayer().bakeRoot();
        root.getAllParts().forEach(part -> {
            for (float coordinate : new float[]{part.x, part.y, part.z})
                assertEquals(Math.round(coordinate * 2), coordinate * 2, .0001F);
            for (var cube : part.cubes) {
                for (float coordinate : new float[]{cube.minX, cube.minY, cube.minZ, cube.maxX, cube.maxY, cube.maxZ})
                    assertEquals(Math.round(coordinate * 2), coordinate * 2, .0001F);
                assertTrue(cube.maxX - cube.minX >= .5F);
                assertTrue(cube.maxY - cube.minY >= .5F);
                assertTrue(cube.maxZ - cube.minZ >= .5F);
            }
        });
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

    @Test
    void blockbenchEmbedsTheAtlasAndTheMasksStayOnVisibleSurfaces() throws Exception {
        var project = ResourceReader.asset("models/entity/bbmodel/bosses/SeraphaeModel.bbmodel");
        byte[] atlasBytes = Files.readAllBytes(ASSETS.resolve("textures/entity/boss/seraphae/seraphae.png"));
        String embedded = project.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
        assertArrayEquals(atlasBytes, Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1)));
        assertEquals(project.getAsJsonArray("elements").size(), SeraphaeModel.createBodyLayer().bakeRoot()
                .getAllParts().mapToInt(part -> part.cubes.size()).sum());
        var base = ImageIO.read(ASSETS.resolve("textures/entity/boss/seraphae/seraphae.png").toFile());
        for (String mask : new String[]{"seraphae_vessels.png", "seraphae_nerves.png"}) {
            var glow = ImageIO.read(ASSETS.resolve("textures/entity/boss/seraphae/" + mask).toFile());
            assertEquals(base.getWidth(), glow.getWidth());
            assertEquals(base.getHeight(), glow.getHeight());
            int lit = 0, dark = 0;
            for (int y = 0; y < base.getHeight(); y++) for (int x = 0; x < base.getWidth(); x++) {
                if ((glow.getRGB(x, y) >>> 24) != 0) {
                    assertNotEquals(0, base.getRGB(x, y) >>> 24, mask + " must stay on visible surfaces");
                    lit++;
                } else if ((base.getRGB(x, y) >>> 24) != 0) dark++;
            }
            assertTrue(lit > 0, mask);
            assertTrue(dark > lit, mask + ": robes and restraints stay dark round the light");
        }
    }

    @Test
    void nervePartsCoverBothWings() {
        var model = new SeraphaeModel<>(SeraphaeModel.createBodyLayer().bakeRoot());
        var nerves = model.nerveParts();
        assertTrue(nerves.stream().anyMatch(n -> n.side() == 0) && nerves.stream().anyMatch(n -> n.side() == 1));
        for (int place = 0; place < 3; place++) {
            final int p = place;
            assertTrue(nerves.stream().anyMatch(n -> n.place() == p && n.position() == 3), "tip of trunk " + place);
        }
    }

    // ── visual pass 2: the animation ─────────────────────────────────

    private static final SaintMoveInputs INPUTS = new SaintMoveInputs(3, SeraphaeMove.laneVariant(6.0D, false), 0, 1);

    private static float[] still() {
        return new float[SeraphaePose.COUNT];
    }

    private static float value(SeraphaeMove move, int channel, float t) {
        float[] a = still();
        SeraphaeModel.move(a, move, t, 0, 0.0F);
        return a[channel];
    }

    private static float speed(SeraphaeMove move, int channel, float t) {
        return Math.abs(value(move, channel, t + .05F) - value(move, channel, t - .05F)) / .1F;
    }

    @Test
    void everyMoveRisesOutOfTheIdleAndSettlesBackIntoIt() {
        for (var move : SeraphaeMove.values()) {
            int length = SeraphaeModel.length(move);
            BossMoveTimeline timeline = move.timeline(INPUTS);
            assertEquals(timeline.windup() + timeline.active() + timeline.recovery(), length, move + " length");
            for (int variant = 0; variant < 2; variant++) {
                for (float aim : new float[]{-2.5F, -0.6F, 0.0F, 0.8F, 3.0F}) {
                    float[] start = still(), end = still();
                    SeraphaeModel.move(start, move, 0, variant, aim);
                    SeraphaeModel.move(end, move, length, variant, aim);
                    for (int i = 0; i < SeraphaePose.SECONDARY; i++) {
                        assertEquals(0, start[i], 1e-4F, move + " starts with channel " + i + " at rest");
                        assertEquals(0, end[i], 1e-4F, move + " ends with channel " + i + " at rest");
                    }
                }
            }
        }
    }

    @Test
    void everyTrackStaysFiniteAndBounded() {
        for (var move : SeraphaeMove.values()) {
            for (float t = 0; t <= SeraphaeModel.length(move); t += .25F) {
                float[] a = still();
                SeraphaeModel.move(a, move, t, 1, -0.9F);
                for (int i = 0; i < SeraphaePose.SECONDARY; i++) {
                    assertTrue(Float.isFinite(a[i]), move + " channel " + i + " at " + t);
                    assertTrue(Math.abs(a[i]) < 6.0F, move + " channel " + i + " at " + t + " = " + a[i]);
                }
            }
        }
        for (float t = 0; t < 140; t += .5F) {
            float[] a = still();
            SeraphaeModel.hold(a, t);
            SeraphaeModel.ending(a, t);
            SeraphaeModel.rise(a, Math.min(60, t));
            SeraphaeModel.card(a, 2, Math.min(50, t));
            SeraphaeModel.card(a, 3, Math.min(50, t));
            for (int id = 1; id <= 4; id++)
                SeraphaeModel.opening(a, id, Math.min(70, t), 40, SeraphaeRestraint.byId((int) t));
            for (float v : a) assertTrue(Float.isFinite(v), "set pieces at " + t);
        }
    }

    @Test
    void everyStrikeCommitsFastAndFollowsThroughPastTheLine() {
        Object[][] strikes = {{SeraphaeMove.LAYING_ON_OF_HANDS, SeraphaePose.LEAN},
                {SeraphaeMove.STRAINING_REACH, SeraphaePose.TRAIL},
                {SeraphaeMove.NERVE_LASH, SeraphaePose.TRUNK_SWEEP + SeraphaePose.trunkCh(true, 2)},
                {SeraphaeMove.COUNTER_LASH, SeraphaePose.WING_L_RAISE}, {SeraphaeMove.AXON_LANCE, SeraphaePose.LEAN},
                {SeraphaeMove.WING_GRASP, SeraphaePose.TRUNK_CURL + SeraphaePose.trunkCh(true, 1)},
                {SeraphaeMove.CONVULSION, SeraphaePose.ARCH}, {SeraphaeMove.CONVULSION_WIDE, SeraphaePose.ARCH},
                {SeraphaeMove.CHAIN_WRENCH, SeraphaePose.TWIST}, {SeraphaeMove.FRACTURE, SeraphaePose.ARCH},
                {SeraphaeMove.HEALED_GROUND, SeraphaePose.LEAN}, {SeraphaeMove.UNCLOSING_EYE, SeraphaePose.ARCH},
                {SeraphaeMove.EXTRACTION, SeraphaePose.ARCH}, {SeraphaeMove.RADIANCE_FLOOD, SeraphaePose.ARCH}};
        for (Object[] strike : strikes) {
            var move = (SeraphaeMove) strike[0];
            int channel = (int) strike[1];
            float commit = move.timeline(INPUTS).windup();
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

    @Test
    void theEyeTurnsHerThroughItsSweepAtTheBeamsOwnRate() {
        for (int variant = 0; variant < 2; variant++) {
            var inputs = new SaintMoveInputs(3, variant, 0, 1);
            var sweep = (BossFootprint.Sweep) ((BossFootprint.Occluded) SeraphaeMove.UNCLOSING_EYE.footprint(inputs)).shape();
            int windup = SeraphaeMove.UNCLOSING_EYE.timeline(inputs).windup();
            for (float age = 0; age <= 40; age += 2.5F) {
                float[] a = still();
                SeraphaeModel.move(a, SeraphaeMove.UNCLOSING_EYE, windup + age, variant, 0.0F);
                assertEquals((float) Math.toRadians(sweep.angle(age)), a[SeraphaePose.TURN], 1e-3F,
                        "variant " + variant + " at active " + age + ": her chest faces the beam");
            }
        }
    }

    @Test
    void theConvulsionTurnsHerWithItsSpokes() {
        for (var move : new SeraphaeMove[]{SeraphaeMove.CONVULSION, SeraphaeMove.CONVULSION_WIDE}) {
            var spokes = (BossFootprint.Annulus) move.footprint(INPUTS);
            var timeline = move.timeline(INPUTS);
            for (float age = 0; age <= timeline.active(); age += 1.5F) {
                float[] a = still();
                SeraphaeModel.move(a, move, timeline.windup() + age, 0, 0.0F);
                assertEquals((float) Math.toRadians(spokes.arcAngle(age, 0)), a[SeraphaePose.TURN], 1e-3F,
                        move + " at active " + age + ": she turns with the spokes");
            }
        }
    }

    @Test
    void theLashesWindAwayThenWhipThroughWithTheirOwnWing() {
        for (int variant = 0; variant < 2; variant++) {
            boolean left = variant == 0;
            int lower = SeraphaePose.TRUNK_SWEEP + SeraphaePose.trunkCh(left, 2);
            int other = SeraphaePose.TRUNK_SWEEP + SeraphaePose.trunkCh(!left, 2);
            float[] coil = still(), whip = still();
            SeraphaeModel.move(coil, SeraphaeMove.NERVE_LASH, 13, variant, 0.0F);
            SeraphaeModel.move(whip, SeraphaeMove.NERVE_LASH, 19, variant, 0.0F);
            assertTrue(coil[lower] < -0.5F && whip[lower] > 1.0F, "the lashing trunk draws back, then cracks forward");
            assertEquals(0, whip[other], 1e-4F, "only the lashing wing whips");
            float sign = left ? 1 : -1;
            assertTrue(coil[SeraphaePose.TURN] * sign < 0 && whip[SeraphaePose.TURN] * sign > 0.5F,
                    "she winds away from the strike, then turns her whole body through it");
        }
    }

    @Test
    void theHoldClosesHerHandsAndWrapsHerWingsRoundThePatient() {
        float[] a = still();
        SeraphaeModel.hold(a, 20);
        assertTrue(a[SeraphaePose.HAND_L] < -0.5F && a[SeraphaePose.HAND_R] < -0.5F, "hands closed on the patient");
        assertTrue(a[SeraphaePose.WING_L_SWEEP] > 0.8F && a[SeraphaePose.WING_R_SWEEP] > 0.8F, "wings forward round them");
        assertTrue(a[SeraphaePose.LEAN] > 0.3F && a[SeraphaePose.LIFT] < -1.0F, "she bends in and sinks to them");
        float[] early = still(), late = still();
        SeraphaeModel.hold(early, 10.5F);
        SeraphaeModel.hold(late, 50.5F);
        assertTrue(early[SeraphaePose.HAND_L] > late[SeraphaePose.HAND_L], "the squeeze grows more urgent");
    }

    @Test
    void theRecoilFlingsHerOpenThenGathersOverTheOpeningsLength() {
        for (int length : new int[]{SeraphaeOpenings.RECOIL_TICKS, SeraphaeOpenings.RECOIL_REPOSE_TICKS,
                SeraphaeOpenings.STRAINING_RECOIL_TICKS}) {
            float[] snap = still(), end = still();
            SeraphaeModel.opening(snap, SeraphaeOpenings.RECOIL_ID, 4, length, null);
            SeraphaeModel.opening(end, SeraphaeOpenings.RECOIL_ID, length, length, null);
            assertTrue(snap[SeraphaePose.ARM_L_RAISE] < -0.8F, "arms flung behind");
            assertTrue(snap[SeraphaePose.WING_L_SWEEP] > 0.7F, "wings thrown forward");
            for (int i = 0; i < SeraphaePose.SECONDARY; i++) assertEquals(0, end[i], 1e-4F, "gathered by " + length);
        }
    }

    @Test
    void thePalmsPlantOnTheFloorAndTheLegsAndGownNeverPassThroughIt() {
        var layer = SeraphaeModel.createBodyLayer().bakeRoot();
        var model = new SeraphaeModel<>(layer);
        float[] slam = still();
        SeraphaeModel.move(slam, SeraphaeMove.HEALED_GROUND, 30, 0, 0.0F);
        model.pose(slam, 0, 0, 0, 0, 1);
        float palm = Math.max(model.palm(true), model.palm(false));
        float slamRoot = model.root().y;
        float[] rest = still();
        model.pose(rest, 0, 0, 0, 0, 1);
        float restPalm = Math.max(model.palm(true), model.palm(false));
        float planted = slamRoot - model.root().y;
        // she drives her palms down toward the floor and kneels into it as far as PLANT_REACH, never flat in the floor
        // (polish round 1 sank her 1.5 blocks to reach it, lying her torso along the floor)
        assertTrue(palm > restPalm + 2.0F, "her palms are driven down: " + palm + " from " + restPalm);
        assertTrue(palm <= SeraphaeHover.FLOOR + 0.05F, "her palm never passes through the floor: " + palm);
        assertTrue(planted > 0.5F * SeraphaeModel.PLANT_REACH && planted <= SeraphaeHover.BLOCK,
                "the plant sinks her " + planted / SeraphaeHover.BLOCK + " blocks");
        for (float sink : new float[]{0, 2, 5, 9}) {
            float[] a = still();
            a[SeraphaePose.LIFT] = -sink;
            model.pose(a, 0, 0, 0, 0, 1);
            ModelPart body = model.root().getChild("body");
            for (String side : new String[]{"left", "right"}) {
                ModelPart leg = model.root().getChild(side + "_leg");
                Vector3f sole = model.point(new Vector3f(0, 5.5F / 16F, 0), model.root(), leg, leg.getChild(side + "_shin"));
                assertTrue(sole.y * 16 <= SeraphaeHover.FLOOR, side + " sole over the floor at sink " + sink + ": " + sole.y * 16);
            }
            for (int i = 0; i < 6; i++) {
                ModelPart panel = body.getChild("robe_panel_" + i);
                float length = 0;
                for (var cube : panel.cubes) length = Math.max(length, cube.maxY);
                Vector3f hem = model.point(new Vector3f(0, length / 16F, 0), model.root(), body, panel);
                assertTrue(hem.y * 16 <= SeraphaeHover.FLOOR + 0.3F, "panel " + i + " hem at sink " + sink + ": " + hem.y * 16);
            }
        }
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
}
