package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumOpeningRules;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Velorum's visual pass 2 animation (2026-11-06): the tracks rise out of the stance and settle back into it, the planted
 * heel stays on the floor without skating, each strike runs faster through its commit than through its wind-up, the
 * crooked hand never falls before his body fails, the Endless Hour holds absolutely still, the Long Night hands its
 * pose to the Guttering Lantern, and the cloth heaps on the floor instead of passing through it. Revision 2026-11-06:
 * the six new tracks (Arrested Pulse, Cruor Javelins, Veins of Frost, the Shadow Step, Gloam Cuts, the Eclipse) do the
 * same; the Step sinks him upright and brings him back up hood first; the Eclipse holds flat but for its three head
 * turns, puts his light out, and hands its pose to the Gutter as the Long Night does.
 */
class VelorumAnimationTest {
	private static final float FLOOR = 24.0F;

	private record Rigged(ModelPart root, VelorumModel<?> model) {
		static Rigged of() {
			ModelPart root = VelorumModel.createBodyLayer().bakeRoot();
			return new Rigged(root, new VelorumModel<>(root));
		}

		ModelPart part(String... path) {
			ModelPart part = root.getChild("root");
			for (String name : path) part = part.getChild(name);
			return part;
		}
	}

	private static int phaseOf(VelorumMove move) {
		return switch (move) {
			case ENDLESS_HOUR, HOLLOW_TOLL, ECLIPSE -> 3;
			case RECALL, CALL_TO_PRAYER, PROCESSION, SPLINTER_CROWN, CRUOR_JAVELINS, VEINS_OF_FROST, UMBRAL_STEP,
					GLOAM_CUTS -> 2;
			default -> 1;
		};
	}

	private static VelorumAnimState.Frame frame(VelorumMove move, float t) {
		var f = VelorumAnimState.Frame.move(move, t);
		return new VelorumAnimState.Frame(f.move(), f.moveTime(), 1.0F, 0, -1.0F, 0.0F, 0, -1.0F, 0, 0.0F, -1.0F,
				phaseOf(move), 0.0F, f.hour(), 0);
	}

	/** Every frame this test walks: each move on its authored clock, the three openings, the ending. */
	private static List<List<VelorumAnimState.Frame>> tracks() {
		List<List<VelorumAnimState.Frame>> tracks = new ArrayList<>();
		for (VelorumMove move : VelorumMove.values()) {
			List<VelorumAnimState.Frame> frames = new ArrayList<>();
			for (float t = 0; t <= move.authored().total(); t += 0.25F) frames.add(frame(move, t));
			tracks.add(frames);
		}
		for (int[] opening : new int[][]{{VelorumOpeningRules.GUTTER_ID, 60}, {VelorumOpeningRules.OVERHELD_ID, 50},
				{VelorumOpeningRules.BOW_ID, 70}, {VelorumOpeningRules.BOW_ID, 40}, {VelorumOpeningRules.BOW_ID, 100}}) {
			List<VelorumAnimState.Frame> frames = new ArrayList<>();
			for (float t = 0; t <= opening[1]; t += 0.25F) frames.add(VelorumAnimState.Frame.opening(opening[0], t, opening[1]));
			tracks.add(frames);
		}
		List<VelorumAnimState.Frame> ending = new ArrayList<>();
		for (float t = 0; t <= 200; t += 0.25F) ending.add(VelorumAnimState.Frame.ending(t));
		tracks.add(ending);
		return tracks;
	}

	@Test
	void everyMoveRisesOutOfTheStanceAndSettlesBackIntoIt() {
		for (VelorumMove move : VelorumMove.values()) {
			int phase = phaseOf(move);
			float[] rest = VelorumModel.channels(new VelorumAnimState.Frame(0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, phase, 0,
					false, 0), 0.0F);
			float[] start = VelorumModel.channels(frame(move, 0.0F), 0.0F);
			for (int i = 0; i < VelorumModel.CHANNELS; i++) {
				if (i == VelorumModel.CLENCH || i == VelorumModel.HEAD_TURN || i == VelorumModel.HEAD) continue;
				assertEquals(rest[i], start[i], 1.0E-4F, move + " starts at the stance, channel " + i);
			}
			if (move == VelorumMove.LONG_NIGHT || move == VelorumMove.ECLIPSE) continue;
			float[] end = VelorumModel.channels(frame(move, move.authored().total()), 0.0F);
			for (int i = 0; i < VelorumModel.CHANNELS; i++) {
				if (i == VelorumModel.CLENCH || i == VelorumModel.HEAD_TURN || i == VelorumModel.HEAD) continue;
				assertEquals(rest[i], end[i], 1.0E-3F, move + " settles back into the stance, channel " + i);
			}
		}
	}

	@Test
	void thePlantedHeelStaysOnTheFloorAndNeverSkates() {
		Rigged rig = Rigged.of();
		for (List<VelorumAnimState.Frame> track : tracks()) {
			Vector3f last = null;
			boolean lastPlanted = false;
			for (VelorumAnimState.Frame f : track) {
				float[] a = VelorumModel.channels(f, 0.0F);
				rig.model().pose(f, 0.0F, 0.0F, 0.0F);
				Vector3f heel = rig.model().heel().mul(16.0F);
				boolean planted = a[VelorumModel.STAFF_PLANT] > 0.99F;
				String at = f.track() + " at " + f.trackTime();
				assertTrue(heel.y <= FLOOR + 0.6F, "the heel never sinks into the floor: " + at);
				if (planted) assertEquals(FLOOR, heel.y, 0.6F, "a planted heel stands on the floor: " + at);
				if (planted && lastPlanted) assertTrue(heel.distance(last) < 0.35F, "a planted heel does not skate: " + at);
				last = heel;
				lastPlanted = planted;
			}
		}
	}

	@Test
	void eachStrikeRunsFasterThroughItsCommitThanThroughItsWindup() {
		Rigged rig = Rigged.of();
		strike(rig, VelorumMove.STAFF_TOLL, () -> rig.model().heel());
		strike(rig, VelorumMove.LANTERN_SWEEP, () -> rig.model().lanterns()[0]);
		strike(rig, VelorumMove.SET_IN_PLACE, () -> rig.model().claw());
		strike(rig, VelorumMove.CRUOR_RELEASE, () -> rig.model().claw());
		strike(rig, VelorumMove.ARRESTED_PULSE, () -> rig.model().claw());
		strike(rig, VelorumMove.CRUOR_JAVELINS, () -> rig.model().claw());
		strike(rig, VelorumMove.VEINS_OF_FROST, () -> rig.model().claw());
		strike(rig, VelorumMove.GLOAM_CUTS, () -> rig.model().claw());
		strike(rig, VelorumMove.HOLLOW_TOLL, () -> rig.model().heel());
		strike(rig, VelorumMove.HANGING_LANTERN, () -> rig.model().lanterns()[2]);
	}

	private static void strike(Rigged rig, VelorumMove move, java.util.function.Supplier<Vector3f> limb) {
		int commit = move.authored().windup();
		float windup = 0.0F, fastest = 0.0F;
		Vector3f last = null;
		for (float t = 0; t <= commit + 3.0F; t += 0.25F) {
			rig.model().pose(frame(move, t), 0.0F, 0.0F, 0.0F);
			Vector3f at = limb.get().mul(16.0F);
			if (last != null) {
				float speed = at.distance(last) / 0.25F;
				if (t <= commit - 1.0F) windup += at.distance(last);
				if (t >= commit - 0.5F) fastest = Math.max(fastest, speed);
			}
			last = at;
		}
		float mean = windup / (commit - 1.0F);
		assertTrue(fastest >= 2.5F * mean, move + ": through the commit at " + fastest + ", the wind-up at " + mean);
	}

	@Test
	void theCrookedHandNeverFallsBeforeHisBodyFails() {
		Rigged rig = Rigged.of();
		List<List<VelorumAnimState.Frame>> tracks = tracks();
		for (List<VelorumAnimState.Frame> track : tracks.subList(0, tracks.size() - 1)) {
			for (VelorumAnimState.Frame f : track) {
				rig.model().pose(f, 0.0F, 0.0F, 0.0F);
				assertTrue(rig.model().claw().y * 16.0F < 12.0F, "the claw stays above his waist: " + f.track() + " at "
						+ f.trackTime());
			}
		}
		rig.model().pose(VelorumAnimState.Frame.REST, 0.0F, 0.0F, 0.0F);
		float raised = rig.model().claw().y;
		rig.model().pose(VelorumAnimState.Frame.ending(40.0F), 0.0F, 0.0F, 0.0F);
		assertTrue(rig.model().claw().y > raised + 0.75F, "it lowers for the first time as his body fails");
	}

	@Test
	void theEndlessHourHoldsAbsolutelyStill() {
		VelorumAnimState.Frame early = frame(VelorumMove.ENDLESS_HOUR, 40.0F);
		VelorumAnimState.Frame late = frame(VelorumMove.ENDLESS_HOUR, 220.0F);
		float[] a = VelorumModel.channels(early, 40.0F), b = VelorumModel.channels(late, 220.0F);
		for (int i = 0; i < VelorumModel.CHANNELS; i++) {
			if (i == VelorumModel.CLENCH) continue;
			assertEquals(a[i], b[i], 1.0E-4F, "nothing moves through the hold, channel " + i);
		}
		assertTrue(a[VelorumModel.CALM] <= 0.0F, "his secondary motion is damped to nothing");
		assertTrue(a[VelorumModel.BREATH] <= 0.0F, "his breath stops");
		assertTrue(a[VelorumModel.LOOK] <= 0.0F, "not even the head follows");
	}

	@Test
	void theLongNightHandsItsPoseToTheGutteringLantern() {
		float[] night = VelorumModel.channels(frame(VelorumMove.LONG_NIGHT, VelorumMove.LONG_NIGHT.authored().total()), 0.0F);
		float[] gutter = VelorumModel.channels(VelorumAnimState.Frame.opening(VelorumOpeningRules.GUTTER_ID, 0.0F, 60), 0.0F);
		for (int i : new int[]{VelorumModel.L_ARM_X, VelorumModel.L_ARM_Z, VelorumModel.STAFF_X, VelorumModel.STAFF_PLANT,
				VelorumModel.HEAD_TURN, VelorumModel.BOW, VelorumModel.CORE}) {
			assertEquals(gutter[i], night[i], 0.06F, "the Gutter picks up where the Long Night left him, channel " + i);
		}
	}

	@Test
	void theUnbrokenEclipseHandsItsPoseToTheGutteringLantern() {
		VelorumAnimState.Frame end = frame(VelorumMove.ECLIPSE, VelorumMove.ECLIPSE.authored().total());
		float[] eclipse = VelorumModel.channels(end, 0.0F);
		VelorumAnimState.Frame g = VelorumAnimState.Frame.opening(VelorumOpeningRules.GUTTER_ID, 0.0F, 20);
		float[] gutter = VelorumModel.channels(new VelorumAnimState.Frame(0, -1, 0, 0, -1, 0, g.opening(), 0.0F, 20, 1.0F, -1,
				3, 0, false, 0), 0.0F);
		for (int i : new int[]{VelorumModel.L_ARM_X, VelorumModel.L_ARM_Z, VelorumModel.STAFF_X, VelorumModel.STAFF_PLANT,
				VelorumModel.HEAD_TURN, VelorumModel.HEAD, VelorumModel.BOW, VelorumModel.CORE, VelorumModel.R_ARM_X,
				VelorumModel.R_FORE_Z, VelorumModel.FINGERS, VelorumModel.CLENCH, VelorumModel.SINK, VelorumModel.LOOK}) {
			assertEquals(gutter[i], eclipse[i], 0.06F, "the Gutter picks up where the Eclipse left him, channel " + i);
		}
	}

	@Test
	void theEclipseHoldsFlatButForItsThreeHeadTurnsAndPutsHisLightOut() {
		float[] held = VelorumModel.channels(frame(VelorumMove.ECLIPSE, 60.0F), 60.0F);
		for (float t = 60.0F; t <= 193.0F; t += 0.5F) {
			float[] a = VelorumModel.channels(frame(VelorumMove.ECLIPSE, t), t);
			for (int i = 0; i < VelorumModel.CHANNELS; i++) {
				if (i == VelorumModel.HEAD_TURN) continue;
				assertEquals(held[i], a[i], 1.0E-4F, "nothing moves through the hold but the head, channel " + i + " at " + t);
			}
			boolean beat = Math.abs(t - 87.0F) < 7.0F || Math.abs(t - 137.0F) < 7.0F || Math.abs(t - 187.0F) < 7.0F;
			if (!beat) assertEquals(held[VelorumModel.HEAD_TURN], a[VelorumModel.HEAD_TURN], 1.0E-4F, "the head still at " + t);
		}
		for (float beat : new float[]{84.0F, 134.0F, 184.0F}) {
			float turned = VelorumModel.channels(frame(VelorumMove.ECLIPSE, beat), beat)[VelorumModel.HEAD_TURN];
			assertEquals(10.0F * net.minecraft.util.Mth.DEG_TO_RAD, Math.abs(turned - held[VelorumModel.HEAD_TURN]), 0.01F,
					"the head turns ten degrees toward the reaching shapes at " + beat);
		}
		assertTrue(held[VelorumModel.CALM] <= 0.0F && held[VelorumModel.BREATH] <= 0.0F, "utterly still");
		Rigged rig = Rigged.of();
		rig.model().pose(frame(VelorumMove.ECLIPSE, 100.0F), 0.0F, 0.0F, 0.0F);
		ModelPart core = rig.part("hips", "chest", "left_arm", "left_forearm", "left_hand", "staff", "staff_lantern",
				"staff_lantern_crystal", "staff_lantern_core");
		assertFalse(core.visible, "the lantern is out");
		rig.model().pose(VelorumAnimState.Frame.REST, 0.0F, 0.0F, 0.0F);
		assertTrue(core.visible, "and lit at rest");
	}

	@Test
	void theShadowStepSinksHimUprightAndBringsHimUpHoodFirst() {
		assertEquals(0.0F, VelorumModel.stepUnder(6.0F));
		assertEquals(VelorumModel.STEP_DEPTH, VelorumModel.stepUnder(23.999F), 0.01F, "all the way under at the commit");
		assertTrue(VelorumModel.stepUnder(12.0F) < VelorumModel.STEP_DEPTH * 0.1F, "slowly at first");
		assertTrue(VelorumModel.stepUnder(25.0F) < 2.3F, "the hood is out by the first tick of the rise");
		assertTrue(VelorumModel.stepUnder(28.0F) <= 1.3F, "the shoulders by four");
		assertEquals(0.0F, VelorumModel.stepUnder(34.0F), "the hem by ten");
		for (float t = 8.0F; t <= 34.0F; t += 0.25F) {
			float[] a = VelorumModel.channels(frame(VelorumMove.UMBRAL_STEP, t), 0.0F);
			assertTrue(Math.abs(a[VelorumModel.BOW] + a[VelorumModel.HIP_X]) < 0.1F, "upright as he sinks, at " + t);
			assertEquals(VelorumModel.stepUnder(t) * 16.0F, a[VelorumModel.UNDER], 1.0E-3F, "the cloth's floor rises with him");
			assertTrue(a[VelorumModel.STAFF_PLANT] < 1.0F || VelorumModel.stepUnder(t) == 0.0F, "the staff goes under with him");
		}
		assertEquals(0.0F, VelorumModel.cardUnder(-1.0F));
		assertTrue(VelorumModel.cardUnder(23.0F) > VelorumModel.STEP_DEPTH * 0.9F, "the phase-3 card sinks him too");
	}

	@Test
	void theClothHeapsOnTheFloorInsteadOfPassingThroughIt() {
		Rigged rig = Rigged.of();
		for (VelorumAnimState.Frame f : List.of(VelorumAnimState.Frame.opening(VelorumOpeningRules.BOW_ID, 30.0F, 70),
				VelorumAnimState.Frame.ending(120.0F), frame(VelorumMove.HOLLOW_TOLL, 25.5F))) {
			rig.model().pose(f, 0.0F, 0.0F, 0.0F);
			for (int i = 0; i < 16; i++) {
				float tip = tip(12.0F + (i % 3) * 0.5F, rig.part("hips"), rig.part("hips", "robe_panel_" + i));
				assertTrue(tip <= FLOOR + 0.05F, "robe panel " + i + " rests on the floor at " + f.track());
			}
			for (int i = 0; i < 9; i++) {
				float tip = tip(12.0F, rig.part("hips"), rig.part("hips", "chest"), rig.part("hips", "chest", "mantle_panel_" + i),
						rig.part("hips", "chest", "mantle_panel_" + i, "mantle_panel_" + i + "_train"));
				assertTrue(tip <= FLOOR + 0.05F, "mantle train " + i + " rests on the floor at " + f.track());
			}
		}
	}

	private static float tip(float length, ModelPart... chain) {
		PoseStack pose = new PoseStack();
		for (ModelPart part : chain) part.translateAndRotate(pose);
		return pose.last().pose().transformPosition(new Vector3f(0, length / 16.0F, 0)).y * 16.0F;
	}

	@Test
	void theSplinterCrownsIciclesFallAndGrowBack() {
		Rigged rig = Rigged.of();
		ModelPart icicle = rig.part("hips", "chest", "head", "halo", "halo_chain_0", "halo_icicle_0");
		rig.model().pose(frame(VelorumMove.SPLINTER_CROWN, 30.0F), 0.0F, 0.0F, 0.0F);
		assertFalse(icicle.visible, "snapped free at the commit");
		rig.model().pose(frame(VelorumMove.SPLINTER_CROWN, 44.0F), 0.0F, 0.0F, 0.0F);
		assertTrue(icicle.visible && icicle.yScale > 1.0F, "growing back, overshooting before it sets");
		rig.model().pose(frame(VelorumMove.SPLINTER_CROWN, 50.0F), 0.0F, 0.0F, 0.0F);
		assertEquals(1.0F, icicle.yScale, 1.0E-3F);
	}
}
