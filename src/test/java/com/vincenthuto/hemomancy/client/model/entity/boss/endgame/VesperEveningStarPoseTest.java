package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperEveningStarPoseTest {
	@Test
	void reParentingKeepsTheRestPoseWhereItWasAuthored() {
		ModelPart whole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");
		ModelPart body = whole.getChild("body");
		// The torso has no rest rotation, so each limb's pivot is just the sum of the two offsets: it must land on the
		// pivot it had when it hung straight from whole.
		assertPivot(body, body.getChild("head"), -0.0219F, -9.942F, 0.9744F);
		assertPivot(body, body.getChild("rightArm"), -6.8969F, -3.867F, 0.5994F);
		assertPivot(body, body.getChild("leftArm"), 6.6031F, -3.867F, 0.5994F);
	}

	@Test
	void restingBootsSitOnTheFloor() {
		ModelPart whole = VesperTheEveningStarModel.createBodyLayer().bakeRoot().getChild("whole");
		float sole = whole.y + whole.getChild("leftLeg").y + VesperEveningStarPose.soleDrop(0.0F, 0.0F, 0.0F);
		assertEquals(VesperEveningStarPose.FLOOR_Y, sole, 1.0F);
	}

	@Test
	void foldingTheLegLiftsTheBootByTheRequestedAmount() {
		for (float lift : new float[] { 1.0F, 4.0F, 8.0F }) {
			float fold = VesperEveningStarPose.foldFor(0.0F, 0.0F, 0.0F, lift);
			float lifted = VesperEveningStarPose.soleDrop(-fold, 2.0F * fold, 0.0F);
			assertEquals(VesperEveningStarPose.soleDrop(0.0F, 0.0F, 0.0F) - lift, lifted, 0.05F, "lift " + lift);
			// The boot stays roughly under the hip instead of kicking out.
			assertTrue(Math.abs(VesperEveningStarPose.soleReach(-fold, 2.0F * fold)
					- VesperEveningStarPose.soleReach(0.0F, 0.0F)) < 4.0F);
		}
		assertEquals(0.0F, VesperEveningStarPose.foldFor(0.0F, 0.0F, 0.0F, -2.0F));
	}

	@Test
	void levelFloorLetsTheLowestBootTouch() {
		float[] floors = { 24.0F, 24.0F };
		// A crouched pose with both soles 3 above the floor settles 3 down; a lunge with one sole into it rises.
		assertEquals(3.0F, VesperEveningStarPose.bodyShift(new float[] { 21.0F, 21.0F }, floors), 1.0E-4F);
		assertEquals(-2.0F, VesperEveningStarPose.bodyShift(new float[] { 26.0F, 22.0F }, floors), 1.0E-4F);
		// A swinging boot never drags the hips down to it.
		assertEquals(0.0F, VesperEveningStarPose.bodyShift(new float[] { 24.0F, 19.0F }, floors), 1.0E-4F);
	}

	@Test
	void unevenFloorSinksTheHipsALittleSoTheLowerBootReaches() {
		// Left boot over a step 8 units lower: the hips sink by the capped amount.
		float shift = VesperEveningStarPose.bodyShift(new float[] { 24.0F, 24.0F }, new float[] { 32.0F, 24.0F });
		assertEquals(VesperEveningStarPose.TERRAIN_SINK, shift, 1.0E-4F);
		assertTrue(VesperEveningStarPose.bodyShift(new float[] { 24.0F, 24.0F }, new float[] { 26.0F, 24.0F }) < 2.1F);
	}

	@Test
	void wrappedSpinsEndWhereTheyStarted() {
		assertEquals(0.0F, VesperEveningStarPose.wrap(3.0F * Mth.TWO_PI), 1.0E-4F);
		assertEquals(0.0F, VesperEveningStarPose.wrap(-2.0F * Mth.TWO_PI), 1.0E-4F);
		assertEquals(-Mth.HALF_PI, VesperEveningStarPose.wrap(1.5F * Mth.PI), 1.0E-4F);
	}

	@Test
	void gaitSwingsAndLiftsOnOneCycle() {
		assertEquals(0.0F, VesperEveningStarPose.legSwing(0.0F), 1.0E-4F);
		assertEquals(1.0F, VesperEveningStarPose.legSwing(0.25F), 1.0E-4F);
		// The knee lifts while the thigh swings forward and is down while it pushes back.
		assertEquals(1.0F, VesperEveningStarPose.legLift(0.0F), 1.0E-4F);
		assertEquals(0.0F, VesperEveningStarPose.legLift(0.5F), 1.0E-4F);
	}

	@Test
	void harvestBeatsAreSeparateSegmentsAndLeapsLeaveTheGround() {
		assertNotEquals(VesperEveningStarPose.segment(VesperWeaponAction.SICKLE_HARVEST, 10.0F),
				VesperEveningStarPose.segment(VesperWeaponAction.SICKLE_HARVEST, 20.0F));
		assertEquals(0, VesperEveningStarPose.segment(VesperWeaponAction.CROSSCUT, 20.0F));
		assertTrue(VesperEveningStarPose.airbornePose(VesperWeaponAction.LEAPING_CLEAVE));
		assertFalse(VesperEveningStarPose.airbornePose(VesperWeaponAction.CROSSCUT));
	}

	private static void assertPivot(ModelPart parent, ModelPart child, float x, float y, float z) {
		assertEquals(x, parent.x + child.x, 1.0E-3F, "x pivot");
		assertEquals(y, parent.y + child.y, 1.0E-3F, "y pivot");
		assertEquals(z, parent.z + child.z, 1.0E-3F, "z pivot");
	}
}
