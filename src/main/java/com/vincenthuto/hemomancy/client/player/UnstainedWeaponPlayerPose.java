package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.client.player.SwingCurve.Ease;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

/**
 * Keyframed attack motions for the Unstained weapons. Curves are authored for the right hand and mirrored
 * for the left; every channel starts and ends at the resting grip so swings never snap. Torso curves are
 * positive while the weapon shoulder winds back, as in vanilla's attack twist.
 *
 * <ul>
 * <li>Dagger: a low coil, then an accelerating upward thrust under the guard that twists in the gap.</li>
 * <li>Glaive: a coiled backswing, then one broad horizontal cut that passes the front at full speed.</li>
 * <li>Warhammer: a two-handed overhead lift, a held apex, then a slam that lands at speed and rebounds.</li>
 * </ul>
 */
public final class UnstainedWeaponPlayerPose {
	/** Vanilla's resting held-item arm pitch, so third-person curves begin and end where the arm already is. */
	private static final float HELD_X = -Mth.PI / 10.0F;

	/** Swing progress at which each weapon visibly makes contact. */
	public static final float DAGGER_CONTACT = .34F;
	public static final float GLAIVE_CONTACT = .41F;
	public static final float HAMMER_APEX = .32F;
	public static final float HAMMER_CONTACT = .42F;

	// ---- Absolution Dagger: coil .00-.18, thrust .18-.34, twist .34-.48, recover .48-1.
	private static final SwingCurve DAGGER_ARM_X = SwingCurve.from(HELD_X).to(.18F, .30F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, -1.12F, Ease.IN).to(.48F, -1.06F, Ease.OUT).to(1, HELD_X, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_ARM_Y = SwingCurve.from(0).to(.18F, .26F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, .08F, Ease.IN).to(.48F, .10F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_ARM_Z = SwingCurve.from(0).to(.18F, .10F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, .20F, Ease.IN).to(.48F, .24F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_BODY = SwingCurve.from(0).to(.18F, .16F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, -.12F, Ease.IN).to(.48F, -.10F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	// The free hand rises as a guard during the coil, then chambers back as the body turns into the thrust.
	private static final SwingCurve DAGGER_OFF_X = SwingCurve.from(0).to(.18F, -.62F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, .28F, Ease.IN).to(.48F, .22F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_OFF_Y = SwingCurve.from(0).to(.18F, .32F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, 0, Ease.IN).to(1, 0, Ease.SMOOTH).build();

	private static final SwingCurve DAGGER_FP_X = SwingCurve.from(0).to(.18F, .06F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, -.32F, Ease.IN).to(.48F, -.30F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_FP_Y = SwingCurve.from(0).to(.18F, -.36F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, .34F, Ease.IN).to(.48F, .32F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_FP_Z = SwingCurve.from(0).to(.18F, .06F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, -.18F, Ease.IN).to(.48F, -.22F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_FP_PITCH = SwingCurve.from(0).to(.18F, -1.95F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, -.70F, Ease.IN).to(.48F, -.74F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_FP_ROLL = SwingCurve.from(0).to(.18F, -.20F, Ease.SMOOTH)
			.to(DAGGER_CONTACT, .52F, Ease.IN).to(.48F, .56F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	// Once the point is in, the blade turns about its own axis: the chink is pried, not just pierced.
	private static final SwingCurve DAGGER_FP_TWIST = SwingCurve.from(0).to(DAGGER_CONTACT, 0, Ease.LINEAR)
			.to(.48F, .55F, Ease.OUT).to(.62F, .45F, Ease.SMOOTH).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve DAGGER_WEIGHT = SwingCurve.from(0).to(.08F, 1, Ease.OUT)
			.to(.90F, 1, Ease.LINEAR).to(1, 0, Ease.SMOOTH).build();

	// ---- Silthmere Glaive: coil .00-.22, cut .22-.60 (front at .41), recover .60-1.
	private static final SwingCurve GLAIVE_SWEEP = SwingCurve.from(0).to(.22F, 1.35F, Ease.SMOOTH)
			.to(.60F, -1.60F, Ease.SHARP).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve GLAIVE_BODY = SwingCurve.from(0).to(.22F, -.45F, Ease.SMOOTH)
			.to(.60F, .50F, Ease.SHARP).to(1, 0, Ease.SMOOTH).build();
	// The free arm counter-swings for balance against the cut.
	private static final SwingCurve GLAIVE_OFF_X = SwingCurve.from(0).to(.22F, -.70F, Ease.SMOOTH)
			.to(.60F, .45F, Ease.SHARP).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve GLAIVE_FP_X = SwingCurve.from(0).to(.22F, .30F, Ease.SMOOTH)
			.to(.60F, -1.20F, Ease.SHARP).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve GLAIVE_FP_YAW = SwingCurve.from(0).to(.22F, -1.35F, Ease.SMOOTH)
			.to(.60F, 1.55F, Ease.SHARP).to(1, 0, Ease.SMOOTH).build();
	// The cut reaches forward as it crosses the front, then draws back.
	private static final SwingCurve GLAIVE_FP_REACH = SwingCurve.from(0).to(.22F, .04F, Ease.SMOOTH)
			.to(GLAIVE_CONTACT, -.20F, Ease.IN).to(.70F, 0, Ease.SMOOTH).build();
	/** Lays the upright glaive flat; it tilts down during the coil and rises again after the recovery. */
	private static final SwingCurve GLAIVE_WEIGHT = SwingCurve.from(0).to(.14F, 1, Ease.SMOOTH)
			.to(.80F, 1, Ease.LINEAR).to(1, 0, Ease.SMOOTH).build();

	// ---- Unstained Warhammer: lift .00-.26, apex hang .26-.32, slam .32-.42, rebound .42-.50, recover .50-1.
	private static final SwingCurve HAMMER_ARM_X = SwingCurve.from(HELD_X).to(.26F, -2.85F, Ease.SMOOTH)
			.to(HAMMER_APEX, -2.98F, Ease.OUT).to(HAMMER_CONTACT, .42F, Ease.IN).to(.50F, .18F, Ease.OUT)
			.to(1, HELD_X, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_ARM_Y = SwingCurve.from(0).to(.26F, .10F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, .22F, Ease.IN).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_ARM_Z = SwingCurve.from(0).to(.26F, .20F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, .08F, Ease.IN).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_BODY = SwingCurve.from(0).to(HAMMER_APEX, .22F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, -.20F, Ease.IN).to(.50F, -.16F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	// The free hand joins the haft for the lift and the blow, then lets go during the recovery.
	private static final SwingCurve HAMMER_OFF_X = SwingCurve.from(0).to(.26F, -2.70F, Ease.SMOOTH)
			.to(HAMMER_APEX, -2.82F, Ease.OUT).to(HAMMER_CONTACT, .20F, Ease.IN).to(.50F, 0, Ease.OUT)
			.to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_OFF_Y = SwingCurve.from(0).to(.26F, .45F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, .40F, Ease.IN).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_WEIGHT = SwingCurve.from(0).to(.08F, 1, Ease.OUT)
			.to(.88F, 1, Ease.LINEAR).to(1, 0, Ease.SMOOTH).build();

	private static final SwingCurve HAMMER_FP_X = SwingCurve.from(0).to(HAMMER_APEX, -.14F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, -.20F, Ease.IN).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_FP_Y = SwingCurve.from(0).to(.26F, .24F, Ease.SMOOTH)
			.to(HAMMER_APEX, .28F, Ease.OUT).to(HAMMER_CONTACT, -.30F, Ease.IN).to(.50F, -.24F, Ease.OUT)
			.to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_FP_Z = SwingCurve.from(0).to(HAMMER_APEX, .04F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, -.20F, Ease.IN).to(.50F, -.16F, Ease.OUT).to(1, 0, Ease.SMOOTH).build();
	// Pitch keeps the bell above its handle on the lift, then drives it down through the blow.
	private static final SwingCurve HAMMER_FP_PITCH = SwingCurve.from(0).to(.26F, .42F, Ease.SMOOTH)
			.to(HAMMER_APEX, .52F, Ease.OUT).to(HAMMER_CONTACT, -1.00F, Ease.IN).to(.50F, -.86F, Ease.OUT)
			.to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_FP_YAW = SwingCurve.from(0).to(HAMMER_APEX, .10F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, -.16F, Ease.IN).to(1, 0, Ease.SMOOTH).build();
	private static final SwingCurve HAMMER_FP_ROLL = SwingCurve.from(0).to(HAMMER_APEX, -.08F, Ease.SMOOTH)
			.to(HAMMER_CONTACT, .14F, Ease.IN).to(1, 0, Ease.SMOOTH).build();

	private UnstainedWeaponPlayerPose() { }

	public static ArmPose dagger(float progress, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		float side = rightHanded ? -1 : 1;
		return new ArmPose(DAGGER_ARM_X.at(p), side * DAGGER_ARM_Y.at(p), side * DAGGER_ARM_Z.at(p),
				-side * DAGGER_BODY.at(p), DAGGER_WEIGHT.at(p),
				DAGGER_OFF_X.at(p), -side * DAGGER_OFF_Y.at(p), 0);
	}

	public static ArmPose glaive(float progress, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		float side = rightHanded ? 1 : -1;
		float weight = GLAIVE_WEIGHT.at(p);
		// The held-item grip turns an upright model through 90 degrees relative to the arm.
		// Rolling the arm puts both the blade and its sweep in the horizontal plane.
		return new ArmPose(GLAIVE_SWEEP.at(p), 0, side * Mth.HALF_PI, -side * GLAIVE_BODY.at(p), weight,
				GLAIVE_OFF_X.at(p), 0, 0);
	}

	public static ArmPose hammer(float progress, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		float side = rightHanded ? -1 : 1;
		return new ArmPose(HAMMER_ARM_X.at(p), side * HAMMER_ARM_Y.at(p), side * HAMMER_ARM_Z.at(p),
				-side * HAMMER_BODY.at(p), HAMMER_WEIGHT.at(p),
				HAMMER_OFF_X.at(p), -side * HAMMER_OFF_Y.at(p), 0);
	}

	public static void firstPersonDagger(PoseStack poses, float progress, float equip, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		float weight = DAGGER_WEIGHT.at(p);
		rest(poses, equip * (1 - weight), rightHanded);
		float side = rightHanded ? 1 : -1;
		poses.translate(side * DAGGER_FP_X.at(p), DAGGER_FP_Y.at(p), DAGGER_FP_Z.at(p));
		// Rotate around the authored model's grip so the blade leads the rising cut.
		poses.translate(side * .10F, -.185F, -.16F);
		poses.mulPose(Axis.XP.rotation(DAGGER_FP_PITCH.at(p)));
		poses.mulPose(Axis.ZP.rotation(side * DAGGER_FP_ROLL.at(p)));
		poses.mulPose(Axis.YP.rotation(side * DAGGER_FP_TWIST.at(p)));
		poses.translate(-side * .10F, .185F, .16F);
	}

	public static void firstPersonGlaive(PoseStack poses, float progress, float equip, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		float weight = GLAIVE_WEIGHT.at(p);
		rest(poses, equip * (1 - weight), rightHanded);
		float side = rightHanded ? 1 : -1;
		poses.translate(side * GLAIVE_FP_X.at(p), .36F * weight, -.18F * weight + GLAIVE_FP_REACH.at(p));
		poses.mulPose(Axis.YP.rotation(side * GLAIVE_FP_YAW.at(p)));
		poses.mulPose(Axis.XP.rotation(-Mth.HALF_PI * weight));
	}

	public static void firstPersonHammer(PoseStack poses, float progress, float equip, boolean rightHanded) {
		float p = Mth.clamp(progress, 0, 1);
		float weight = HAMMER_WEIGHT.at(p);
		rest(poses, equip * (1 - weight), rightHanded);
		float side = rightHanded ? 1 : -1;
		poses.translate(side * HAMMER_FP_X.at(p), HAMMER_FP_Y.at(p) + impactJolt(p), HAMMER_FP_Z.at(p));
		poses.mulPose(Axis.XP.rotation(HAMMER_FP_PITCH.at(p)));
		poses.mulPose(Axis.YP.rotation(side * HAMMER_FP_YAW.at(p)));
		poses.mulPose(Axis.ZP.rotation(side * HAMMER_FP_ROLL.at(p)));
	}

	/** A short damped shudder through the haft after the bell lands. */
	static float impactJolt(float progress) {
		float t = (progress - HAMMER_CONTACT) / .20F;
		if (t <= 0 || t >= 1) return 0;
		return .025F * Mth.sin(t * Mth.TWO_PI * 2.0F) * (1 - t) * (1 - t);
	}

	/**
	 * Poses the swinging arm, torso and free arm. The free arm follows fully only when its hand is empty;
	 * a held shield or tool keeps most of its own pose.
	 */
	public static void applyThirdPerson(HumanoidModel<?> model, ArmPose pose, boolean rightHanded,
			boolean offhandFree) {
		var arm = rightHanded ? model.rightArm : model.leftArm;
		var offArm = rightHanded ? model.leftArm : model.rightArm;
		arm.xRot = Mth.lerp(pose.weight, arm.xRot, pose.armXRot);
		arm.yRot = Mth.lerp(pose.weight, arm.yRot, pose.armYRot);
		arm.zRot = Mth.lerp(pose.weight, arm.zRot, pose.armZRot);
		float offWeight = pose.weight * (offhandFree ? 1.0F : .35F);
		offArm.xRot = Mth.lerp(offWeight, offArm.xRot, pose.offXRot);
		offArm.yRot = Mth.lerp(offWeight, offArm.yRot, pose.offYRot);
		offArm.zRot = Mth.lerp(offWeight, offArm.zRot, pose.offZRot);
		model.body.yRot = Mth.lerp(pose.weight, model.body.yRot, pose.bodyYRot);
		model.rightArm.x = -Mth.cos(model.body.yRot) * 5.0F;
		model.rightArm.z = Mth.sin(model.body.yRot) * 5.0F;
		model.leftArm.x = Mth.cos(model.body.yRot) * 5.0F;
		model.leftArm.z = -Mth.sin(model.body.yRot) * 5.0F;
	}

	private static void rest(PoseStack poses, float equip, boolean rightHanded) {
		poses.translate(rightHanded ? .56F : -.56F, -.52F - equip * .6F, -.72F);
	}

	public record ArmPose(float armXRot, float armYRot, float armZRot, float bodyYRot, float weight,
			float offXRot, float offYRot, float offZRot) { }
}
