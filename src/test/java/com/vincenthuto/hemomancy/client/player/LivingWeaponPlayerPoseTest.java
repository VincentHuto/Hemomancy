package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.player.LivingWeaponPlayerPose.Stroke;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivingWeaponPlayerPoseTest {
	/** A point up the held weapon, in hand space before the item renderer. */
	private static final Vector3f TIP = new Vector3f(0, .6F, 0);
	private static final Vector3f GRIP = new Vector3f();

	@Test
	void everyStrokeLeavesAndReturnsToTheRestingGripWithoutSnapping() {
		for (Stroke stroke : Stroke.values()) {
			Matrix4f rest = hand(stroke, 0, true);
			assertTrue(rest.equals(hand(stroke, 1, true), .00001F), stroke + " must end at rest");
			assertTrue(rest.equals(hand(stroke, .999F, true), .001F), stroke + " must not snap on the last frame");
			assertTrue(!rest.equals(hand(stroke, stroke.contact, true), .05F), stroke + " must actually move");
			assertEquals(0, arm(stroke, 0).weight(), stroke + " starts from the vanilla pose");
			assertEquals(0, arm(stroke, 1).weight(), stroke + " hands the arm back to vanilla");
		}
	}

	@Test
	void framesNeverJump() {
		for (Stroke stroke : Stroke.values()) {
			Matrix4f previous = hand(stroke, 0, true);
			var previousArm = arm(stroke, 0);
			for (int i = 1; i <= 4000; i++) {
				float progress = i / 4000.0F;
				Matrix4f current = hand(stroke, progress, true);
				assertTrue(point(previous, TIP).distance(point(current, TIP)) < .05F, stroke + " first-person jump at " + progress);
				var currentArm = arm(stroke, progress);
				assertTrue(Math.abs(currentArm.armXRot() - previousArm.armXRot()) < .05F, stroke + " arm jump at " + progress);
				assertTrue(Math.abs(currentArm.bodyYRot() - previousArm.bodyYRot()) < .05F, stroke + " torso jump at " + progress);
				assertTrue(Math.abs(currentArm.weight() - previousArm.weight()) < .01F, stroke + " blend jump at " + progress);
				previous = current;
				previousArm = currentArm;
			}
		}
	}

	@Test
	void heavyBlowsLiftThenLandAtSpeed() {
		for (Stroke stroke : new Stroke[]{Stroke.AXE, Stroke.FLAIL}) {
			float rest = tipY(stroke, 0);
			float apex = tipY(stroke, stroke.contact - .10F);
			float contact = tipY(stroke, stroke.contact);
			assertTrue(apex > rest + .1F, stroke + " must lift before the blow");
			assertTrue(apex > contact + .3F, stroke + " must come down hard");
			float early = tipY(stroke, stroke.contact - .09F) - tipY(stroke, stroke.contact - .08F);
			float late = tipY(stroke, stroke.contact - .02F) - tipY(stroke, stroke.contact - .01F);
			assertTrue(late > early * 2, stroke + " must accelerate into the blow");
			var overhead = arm(stroke, stroke.contact - .10F);
			var landed = arm(stroke, stroke.contact);
			assertTrue(overhead.armXRot() < -2.4F && landed.armXRot() > -.5F, stroke + " third person lifts overhead and drops");
			assertTrue(overhead.bodyYRot() > 0 && landed.bodyYRot() < 0,
					stroke + " torso winds the weapon shoulder back, then turns through the blow");
		}
		assertTrue(tipY(Stroke.AXE, .64F) > tipY(Stroke.AXE, .54F), "The axe wrenches itself free after biting");
	}

	@Test
	void thrustsDrawBackThenDriveForward() {
		for (Stroke stroke : new Stroke[]{Stroke.SPEAR, Stroke.LANCEA, Stroke.TORCH}) {
			float rest = gripZ(stroke, 0);
			float contact = gripZ(stroke, stroke.contact);
			assertTrue(contact < rest - .3F, stroke + " must reach out past the resting grip");
			assertTrue(arm(stroke, stroke.contact).armXRot() < -1.2F, stroke + " third person extends the arm forward");
		}
		assertTrue(gripZ(Stroke.SPEAR, .22F) > gripZ(Stroke.SPEAR, 0), "The spear draws back along its shaft first");
		assertTrue(arm(Stroke.LANCEA, .24F).armXRot() < -2.4F, "The lancea is raised overhand before it drives");
	}

	@Test
	void bladeComboCutsDownThenAnswersRisingTheOtherWay() {
		Vector3f foreStart = tipDir(Stroke.BLADE_FOREHAND, .20F), foreEnd = tipDir(Stroke.BLADE_FOREHAND, .40F);
		Vector3f backStart = tipDir(Stroke.BLADE_BACKHAND, .20F), backEnd = tipDir(Stroke.BLADE_BACKHAND, .40F);
		assertTrue(foreEnd.y < foreStart.y && foreEnd.x < foreStart.x, "The forehand cuts down and across");
		assertTrue(backEnd.y > backStart.y && backEnd.x > backStart.x, "The backhand rises back the other way");
		assertTrue(arm(Stroke.BLADE_FOREHAND, .20F).armXRot() < -2.0F, "The forehand is cocked high");
		assertTrue(arm(Stroke.BLADE_BACKHAND, .20F).armYRot() < 0, "The backhand winds across the body");
	}

	@Test
	void rakesAlternateDirection() {
		float inward = tipDir(Stroke.RAKE_INWARD, .36F).x - tipDir(Stroke.RAKE_INWARD, .18F).x;
		float outward = tipDir(Stroke.RAKE_OUTWARD, .36F).x - tipDir(Stroke.RAKE_OUTWARD, .18F).x;
		assertTrue(inward < 0 && outward > 0, "Consecutive rakes tear in opposite directions");
		assertTrue(tipY(Stroke.RAKE_INWARD, .36F) < tipY(Stroke.RAKE_INWARD, .18F), "Both rakes tear downward");
		assertTrue(tipY(Stroke.RAKE_OUTWARD, .36F) < tipY(Stroke.RAKE_OUTWARD, .18F));
	}

	@Test
	void sickleHooksInwardAndPullsTheCutBack() {
		assertTrue(tipDir(Stroke.SICKLE, .44F).x < tipDir(Stroke.SICKLE, .22F).x - .5F, "The reap sweeps across the body");
		assertTrue(gripZ(Stroke.SICKLE, .44F) > gripZ(Stroke.SICKLE, .22F) + .15F, "The hook draws back toward the reaper");
		var wide = arm(Stroke.SICKLE, .22F);
		var hooked = arm(Stroke.SICKLE, .44F);
		assertTrue(wide.armYRot() > .5F && hooked.armYRot() < -.5F, "Third person reaches out wide, then hooks inward");
	}

	@Test
	void leftHandMirrorsEveryStroke() {
		for (Stroke stroke : Stroke.values()) {
			for (float progress : new float[]{.15F, .3F, .42F, .6F, .85F}) {
				Matrix4f right = hand(stroke, progress, true);
				Matrix4f left = hand(stroke, progress, false);
				assertEquals(-right.m30(), left.m30(), .00001F);
				assertEquals(right.m31(), left.m31(), .00001F);
				assertEquals(right.m32(), left.m32(), .00001F);
				var rightArm = LivingWeaponPlayerPose.thirdPerson(stroke, progress, true);
				var leftArm = LivingWeaponPlayerPose.thirdPerson(stroke, progress, false);
				assertEquals(rightArm.armXRot(), leftArm.armXRot(), .00001F);
				assertEquals(-rightArm.armYRot(), leftArm.armYRot(), .00001F);
				assertEquals(-rightArm.armZRot(), leftArm.armZRot(), .00001F);
				assertEquals(-rightArm.bodyYRot(), leftArm.bodyYRot(), .00001F);
				assertEquals(-rightArm.offYRot(), leftArm.offYRot(), .00001F);
			}
		}
	}

	private static Matrix4f hand(Stroke stroke, float progress, boolean right) {
		PoseStack poses = new PoseStack();
		LivingWeaponPlayerPose.firstPerson(poses, stroke, progress, 0, right);
		return new Matrix4f(poses.last().pose());
	}

	private static UnstainedWeaponPlayerPose.ArmPose arm(Stroke stroke, float progress) {
		return LivingWeaponPlayerPose.thirdPerson(stroke, progress, true);
	}

	private static Vector3f point(Matrix4f matrix, Vector3f local) {
		return new Vector3f(local).mulPosition(matrix);
	}

	private static float tipY(Stroke stroke, float progress) {
		return point(hand(stroke, progress, true), TIP).y;
	}

	private static float gripZ(Stroke stroke, float progress) {
		return point(hand(stroke, progress, true), GRIP).z;
	}

	private static Vector3f tipDir(Stroke stroke, float progress) {
		return new Vector3f(0, 1, 0).mulDirection(hand(stroke, progress, true));
	}
}
