package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

/** Pure timing, resource, cone, and cancellation rules for the Living Torch channel. */
public final class LivingTorchBreathRules {
	public static final int MAX_USE_TICKS = 72_000;
	public static final int WINDUP_TICKS = 6;
	public static final int PULSE_INTERVAL_TICKS = 4;
	public static final double RANGE = 7.0D;
	public static final double HALF_ANGLE_DEGREES = 28.0D;
	public static final double BLOOD_COST_PER_TICK = 3.0D;
	public static final float DAMAGE_PER_PULSE = 1.5F;
	public static final float MOVEMENT_MULTIPLIER = 0.35F;

	private static final double CONE_DOT_THRESHOLD = Math.cos(Math.toRadians(HALF_ANGLE_DEGREES));

	private LivingTorchBreathRules() { }

	public static boolean shouldDrainBlood(int elapsedTicks) {
		return elapsedTicks >= WINDUP_TICKS;
	}

	public static boolean isDamagePulse(int elapsedTicks) {
		return shouldDrainBlood(elapsedTicks)
				&& (elapsedTicks - WINDUP_TICKS) % PULSE_INTERVAL_TICKS == 0;
	}

	public static boolean canPay(double blood) {
		return blood >= BLOOD_COST_PER_TICK;
	}

	public static boolean canHitCandidate(boolean hasLineOfSight, boolean hostile, boolean alreadyHit) {
		return hasLineOfSight && hostile && !alreadyHit;
	}

	public static double bloodAfterPayment(double blood) {
		return canPay(blood) ? blood - BLOOD_COST_PER_TICK : blood;
	}

	public static boolean isInsideCone(double originX, double originY, double originZ,
			double lookX, double lookY, double lookZ,
			double targetX, double targetY, double targetZ) {
		double dx = targetX - originX;
		double dy = targetY - originY;
		double dz = targetZ - originZ;
		double distanceSquared = dx * dx + dy * dy + dz * dz;
		if (distanceSquared <= 1.0E-8D || distanceSquared > RANGE * RANGE) return false;
		double lookLength = Math.sqrt(lookX * lookX + lookY * lookY + lookZ * lookZ);
		if (lookLength <= 1.0E-8D) return false;
		double distance = Math.sqrt(distanceSquared);
		double dot = (dx * lookX + dy * lookY + dz * lookZ) / (distance * lookLength);
		return dot >= CONE_DOT_THRESHOLD;
	}

	public static StopReason stopReason(ChannelState state) {
		if (!state.using()) return StopReason.RELEASED;
		if (!state.sameHeldStack()) return StopReason.HAND_OR_ITEM_CHANGED;
		if (!state.alive()) return StopReason.DEAD;
		if (!state.connected()) return StopReason.LOGGED_OUT;
		if (!state.sameDimension()) return StopReason.DIMENSION_CHANGED;
		if (state.staffFormRestored()) return StopReason.STAFF_FORM_RESTORED;
		if (state.blockingCardinalRite()) return StopReason.CARDINAL_RITE_BLOCKING;
		if (!state.activeBlood()) return StopReason.NO_ACTIVE_BLOOD;
		return StopReason.NONE;
	}

	public enum StopReason {
		NONE, RELEASED, HAND_OR_ITEM_CHANGED, DEAD, LOGGED_OUT, DIMENSION_CHANGED,
		STAFF_FORM_RESTORED, CARDINAL_RITE_BLOCKING, NO_ACTIVE_BLOOD
	}

	public record ChannelState(boolean using, boolean sameHeldStack, boolean alive,
			boolean connected, boolean sameDimension, boolean staffFormRestored,
			boolean blockingCardinalRite, boolean activeBlood) {
		public ChannelState withUsing(boolean value) { return new ChannelState(value, sameHeldStack, alive, connected, sameDimension, staffFormRestored, blockingCardinalRite, activeBlood); }
		public ChannelState withSameHeldStack(boolean value) { return new ChannelState(using, value, alive, connected, sameDimension, staffFormRestored, blockingCardinalRite, activeBlood); }
		public ChannelState withAlive(boolean value) { return new ChannelState(using, sameHeldStack, value, connected, sameDimension, staffFormRestored, blockingCardinalRite, activeBlood); }
		public ChannelState withConnected(boolean value) { return new ChannelState(using, sameHeldStack, alive, value, sameDimension, staffFormRestored, blockingCardinalRite, activeBlood); }
		public ChannelState withSameDimension(boolean value) { return new ChannelState(using, sameHeldStack, alive, connected, value, staffFormRestored, blockingCardinalRite, activeBlood); }
		public ChannelState withStaffFormRestored(boolean value) { return new ChannelState(using, sameHeldStack, alive, connected, sameDimension, value, blockingCardinalRite, activeBlood); }
		public ChannelState withBlockingCardinalRite(boolean value) { return new ChannelState(using, sameHeldStack, alive, connected, sameDimension, staffFormRestored, value, activeBlood); }
		public ChannelState withActiveBlood(boolean value) { return new ChannelState(using, sameHeldStack, alive, connected, sameDimension, staffFormRestored, blockingCardinalRite, value); }
	}
}
