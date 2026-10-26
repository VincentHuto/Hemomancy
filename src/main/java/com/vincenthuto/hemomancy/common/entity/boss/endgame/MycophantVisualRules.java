package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Timing and shape for the Spawn Mother's focal geometry (the siphon tether, the claim wrap). No Minecraft types. */
public final class MycophantVisualRules {
	private MycophantVisualRules() {
	}

	/** The siphon's mouth at rest, in blocks from her feet: right, up, forward. The siphon arm is on her right. */
	public static double[] siphonMouth() {
		return new double[] { 2.2D, 3.4D, 2.0D };
	}

	/** Where the Gaze leaves the pulp's eyes, in blocks from her feet: right, up, forward. The server raycasts from here. */
	public static double[] gazeEye() {
		return new double[] { 0.0D, 5.2D, 2.6D };
	}

	/** How far the siphon's tether droops at its middle, in blocks. */
	public static float tetherSag(float distance) {
		return Math.min(1.5F, Math.max(0.0F, distance) * 0.12F);
	}

	/** A heartbeat along the tether: pow(max(0, sin), 4), so it swallows in pulses rather than glowing evenly. */
	public static float tetherPulse(float tick) {
		float s = (float) Math.sin(tick * 0.3F);
		return s <= 0.0F ? 0.0F : s * s * s * s;
	}

	public static float claimWrapProgress(float meter) {
		return Math.max(0.0F, Math.min(1.0F, meter));
	}
}
