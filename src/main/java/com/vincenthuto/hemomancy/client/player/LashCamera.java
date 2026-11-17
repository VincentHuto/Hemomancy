package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.LashTuning;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * The local caster's whip aim, and the split of the mouse between it and the view. While their lash is live or fading
 * the view takes what {@link LashCameraRules#view} allows of each mouse turn, and what the cap cuts carries the whip
 * aim out ahead of the view instead: a hard flick swings the lash wide and only nudges the view. The whip's lead over
 * the view is held in front of the caster and comes back to the crosshair by itself once the hand slows
 * ({@link LashCameraRules#lead}), so it can never be left hanging over the head. At any other time the mouse turns the
 * view as vanilla does. {@code MixinMouseHandler} hands every turn here.
 */
public final class LashCamera {
	/** Degrees one unit of {@code Entity.turn} is worth. */
	private static final double DEGREES_PER_UNIT = 0.15D;
	private static final LashCameraRules.State VIEW = new LashCameraRules.State();
	private static final LashCameraRules.Lead LEAD = new LashCameraRules.Lead();
	private static boolean active;
	private static double frameTicks = 1.0D;

	private LashCamera() {
	}

	/** The length of the frame whose turn is about to be handed over, in seconds. */
	public static void frame(double seconds) {
		frameTicks = Math.max(0.0D, Math.min(2.0D, seconds * 20.0D));
	}

	/** One frame's mouse turn, in {@code Entity.turn} units, applied to {@code player}'s view. */
	public static void turn(LocalPlayer player, double yawUnits, double pitchUnits) {
		double[] view = split(yawUnits, pitchUnits);
		player.turn(view[0], view[1]);
	}

	/**
	 * Splits one frame's mouse turn, in {@code Entity.turn} units: returns what the view takes of it, in the same units,
	 * and carries the whip's lead by the rest.
	 */
	public static double[] split(double yawUnits, double pitchUnits) {
		double strength = active ? HemoClientConfig.lashCameraDamping() : 0.0D;
		if (!(strength > 0.0D)) {
			// No lash out, or the steadying is off: the mouse is the view's alone and the whip aim is the look.
			LEAD.set(0.0D, 0.0D);
			return new double[] {yawUnits, pitchUnits};
		}
		double rawYaw = yawUnits * DEGREES_PER_UNIT, rawPitch = pitchUnits * DEGREES_PER_UNIT;
		LashTuning tuning = LashRules.tuning();
		double[] view = LashCameraRules.view(VIEW, rawYaw, rawPitch, frameTicks, tuning.cameraCap(), tuning.cameraDrag(), strength);
		double beforeYaw = LEAD.yaw(), beforePitch = LEAD.pitch();
		LashCameraRules.lead(LEAD, rawYaw - view[0], rawPitch - view[1], frameTicks, tuning.maxLead(), tuning.leadKeep());
		LashCameraRules.limitStep(LEAD, beforeYaw, beforePitch, view[0], view[1], tuning.maxAimStep() * frameTicks);
		return new double[] {view[0] / DEGREES_PER_UNIT, view[1] / DEGREES_PER_UNIT};
	}

	/**
	 * Each client tick, the phase of the local caster's lash and the aim its rules hold. The whip's lead starts from
	 * that aim when the lash goes live (a throw leaves it at the end of its arc), and is dropped when the lash is
	 * neither live nor fading.
	 */
	public static void follow(LocalPlayer player, LashRules.Phase phase, float aimYaw, float aimPitch) {
		boolean out = phase == LashRules.Phase.LIVE || phase == LashRules.Phase.FADING;
		if (out && !active) {
			VIEW.reset();
			LEAD.set(Mth.wrapDegrees(aimYaw - player.getYRot()), aimPitch - player.getXRot());
			LashCameraRules.lead(LEAD, 0.0D, 0.0D, 0.0D, LashRules.tuning().maxLead(), LashRules.tuning().leadKeep());
		}
		if (!out) {
			VIEW.reset();
			LEAD.set(0.0D, 0.0D);
		}
		active = out;
	}

	/** The attack button brought the whip back to the look. */
	public static void recentre() {
		LEAD.set(0.0D, 0.0D);
	}

	public static boolean active() {
		return active;
	}

	/** The whip aim: the local player's look, carried out by the whip's lead. */
	public static float aimYaw() {
		LocalPlayer player = Minecraft.getInstance().player;
		return (float) ((player == null ? 0.0F : player.getYRot()) + LEAD.yaw());
	}

	public static float aimPitch() {
		LocalPlayer player = Minecraft.getInstance().player;
		return (float) Math.max(-89.0D, Math.min(89.0D, (player == null ? 0.0F : player.getXRot()) + LEAD.pitch()));
	}

	/** No lash of the local caster's is out: the mouse is the view's alone again. */
	public static void reset() {
		active = false;
		VIEW.reset();
		LEAD.set(0.0D, 0.0D);
	}
}
