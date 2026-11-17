package com.vincenthuto.hemomancy.client.render.world.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules.Stage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

/** The final review's findings on the phantom show, each pinned (2026-11-16). */
class PhantomReviewFixesTest {
	private static final Vec3 VIEWER = new Vec3(0.5D, 2.6D, 0.5D);

	/** Finding 2: a phantom between the eye and a creature fades; one behind a creature is hidden by depth anyway. */
	@Test
	void aPhantomInFrontOfACreatureFades() {
		Vec3 camera = new Vec3(0, 2, 0);
		AABB creature = new AABB(-0.3, 0, 7.7, 0.3, 1.8, 8.3);
		assertTrue(PhantomShowRules.covers(camera, new Vec3(0, 1.5, 4), creature), "phantom between eye and creature");
		assertFalse(PhantomShowRules.covers(camera, new Vec3(0, 1.5, 12), creature), "phantom behind the creature");
		assertTrue(PhantomShowRules.covers(camera, new Vec3(0.6, 1.0, 8.6), creature), "phantom touching it");
		assertFalse(PhantomShowRules.covers(camera, new Vec3(6, 1.5, 4), creature), "phantom off to the side");
	}

	/** Finding 3: a stage change cross-fades whole casts; the weight scales every figure's alpha. */
	@Test
	void weightScalesEveryFigure() {
		ShowMarks marks = marks(40);
		var look = PhantomShowRules.look(Stage.ATTUNED, false);
		var full = PhantomFigures.plan(marks, look, 100.0F, VIEWER, pos -> null, 1.0F);
		var half = PhantomFigures.plan(marks, look, 100.0F, VIEWER, pos -> null, 0.5F);
		assertEquals(full.size(), half.size());
		for (int i = 0; i < full.size(); i++)
			assertEquals(full.get(i).alpha() * 0.5F, half.get(i).alpha(), 1.0E-5F);
		assertTrue(PhantomFigures.plan(marks, look, 100.0F, VIEWER, pos -> null, 0.0F).isEmpty());
	}

	/** Finding 4: the ring act fades out and in at an act change, and applause eases. */
	@Test
	void actChangesAndApplauseEase() {
		ShowMarks marks = marks(0);
		var look = PhantomShowRules.look(Stage.ATTUNED, false);
		float boundary = PhantomFigures.ACT * 3.0F;
		float atEdge = ringAlpha(PhantomFigures.plan(marks, look, boundary + 0.5F, VIEWER, pos -> null, 1.0F));
		float mid = ringAlpha(PhantomFigures.plan(marks, look, boundary + PhantomFigures.ACT / 2, VIEWER, pos -> null, 1.0F));
		assertTrue(atEdge < mid * 0.25F, "edge " + atEdge + " mid " + mid);
		var still = PhantomPose.seated(5L, 40.0F, 0.0F, 0.0F);
		var half = PhantomPose.seated(5L, 40.0F, 0.0F, 0.5F);
		var full = PhantomPose.seated(5L, 40.0F, 0.0F, 1.0F);
		assertTrue(half.rightArmX() < still.rightArmX() && half.rightArmX() > full.rightArmX());
	}

	/** Finding 5: which trapeze a performer takes does not depend on where the player stands. */
	@Test
	void performersKeepTheirTrapezeWhereverThePlayerWalks() {
		List<BlockPos> a = List.of(new BlockPos(3, 9, 40), new BlockPos(60, 9, 40), new BlockPos(30, 9, 4));
		List<BlockPos> b = List.of(new BlockPos(30, 9, 4), new BlockPos(3, 9, 40), new BlockPos(60, 9, 40));
		assertEquals(ShowMarks.stableOrder(a), ShowMarks.stableOrder(b));
	}

	/** Finding 6: the audience is capped per stage and culled past the cue range. */
	@Test
	void theAudienceIsCappedAndCulled() {
		ShowMarks marks = marks(400);
		var attuned = PhantomFigures.plan(marks, PhantomShowRules.look(Stage.ATTUNED, false), 10.0F, VIEWER,
				pos -> null, 1.0F);
		long seated = attuned.stream().filter(f -> f.kind() != PhantomFigures.Kind.SWINGING && !f.hidesFromGaze()).count();
		assertTrue(seated <= PhantomFigures.cap(PhantomShowRules.look(Stage.ATTUNED, false)) + 1, "seated " + seated);
		for (var figure : attuned) assertTrue(figure.at().distanceTo(VIEWER) <= 28.0D + 1.0D, "far " + figure.at());
	}

	private static float ringAlpha(List<PhantomFigures.Figure> cast) {
		return (float) cast.stream().filter(f -> f.kind() == PhantomFigures.Kind.STANDING && !f.hidesFromGaze())
				.mapToDouble(PhantomFigures.Figure::alpha).max().orElse(0.0D);
	}

	/** A ring of radius 6 at the origin and {@code count} seats on a grid out to 60 blocks. */
	private static ShowMarks marks(int count) {
		List<ShowMarks.Seat> seats = new ArrayList<>();
		for (int i = 0; i < count; i++) seats.add(new ShowMarks.Seat(new BlockPos(i % 20 * 3 - 30, 1, i / 20 * 3 - 30), 0.0F, false));
		List<BlockPos> curb = new ArrayList<>();
		for (int i = 0; i < 16; i++) {
			double a = i * Math.PI / 8.0D;
			curb.add(new BlockPos((int) Math.round(6 * Math.cos(a)), 1, (int) Math.round(6 * Math.sin(a))));
		}
		return new ShowMarks(seats, List.of(), List.of(), List.of(), ShowMarks.ring(curb));
	}
}
