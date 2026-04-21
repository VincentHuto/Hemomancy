package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.item.cosmetic.SanguineBlobItem;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side state for the Sanguine Blob cosmetic.
 * Tracks the position, velocity, jiggle phase, drop state, squish animation, and swing
 * stretch of the floating blood orb.
 * Ticked every client tick from {@link com.vincenthuto.hemomancy.client.event.ClientEvents}.
 */
public class BloodBallClientData {

	/** How many ticks the ball fades for after being dropped. */
	public static final int MAX_FADE_TICKS = 60;

	/** Duration in ticks of the squish-on-impact animation. */
	public static final int MAX_SQUISH_TICKS = 25;

	/**
	 * How many ticks after drop before the simulated ground-impact squish fires.
	 * At 0.04 blocks/tick² gravity this is roughly when the ball has fallen ~1.5 blocks.
	 */
	private static final int SQUISH_DELAY_TICKS = 20;

	/** Lerp factor used to smoothly drag the ball toward the look-target each tick. */
	private static final double FOLLOW_LERP = 0.25;

	/** How far ahead of the eye the ball orbits (blocks). */
	private static final double FOLLOW_DISTANCE = 2.5;

	/** Gravity acceleration applied per tick when dropped (blocks/tick²). */
	private static final double GRAVITY = 0.04;

	// ── State ──
	private static Vec3 position = null;
	private static Vec3 prevPosition = null;
	private static Vec3 velocity = Vec3.ZERO;
	private static boolean dropped = false;
	private static int fadeTicks = 0;
	private static float jigglePhase = 0f;

	/** Per-tick displacement of the ball while following the player (drives stretch). */
	private static Vec3 swingDelta = Vec3.ZERO;

	/** Remaining ticks in the squish-on-impact animation (0 = not squishing). */
	private static int squishTicks = 0;

	/** Ensures the squish fires exactly once per drop. */
	private static boolean squishTriggered = false;

	// ─────────────────────────────────────────────────────────────────────
	//  Public query
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * Returns {@code true} when the local player is holding a Sanguine Blob item
	 * in either hand and has reached Initiatory Degree 3 or higher.
	 */
	public static boolean isActive() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return false;
		Player player = mc.player;

		boolean holdsBlob = player.getMainHandItem().getItem() instanceof SanguineBlobItem
				|| player.getOffhandItem().getItem() instanceof SanguineBlobItem;
		if (!holdsBlob) return false;

		return HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(IInitiatoryDegree::getDegreeNumber)
				.orElse(0) >= 3;
	}

	/** Current interpolated world-space position of the ball, or {@code null} if inactive. */
	public static Vec3 getPosition() {
		return position;
	}

	/** Previous tick's position, used for render interpolation. */
	public static Vec3 getPrevPosition() {
		return prevPosition;
	}

	/** Whether the ball has been dropped and is falling. */
	public static boolean isDropped() {
		return dropped;
	}

	/** Remaining ticks before the dropped ball fully fades. */
	public static int getFadeTicks() {
		return fadeTicks;
	}

	/** Continuously advancing phase value driving the surface jiggle animation. */
	public static float getJigglePhase() {
		return jigglePhase;
	}

	/**
	 * Per-tick world-space displacement of the ball while following the player.
	 * Its length (blocks/tick) drives the stretch-while-swinging effect.
	 */
	public static Vec3 getSwingDelta() {
		return swingDelta;
	}

	/**
	 * Remaining ticks in the squish-on-impact animation, counting down from
	 * {@link #MAX_SQUISH_TICKS} to 0.  Zero means the ball is not squishing.
	 */
	public static int getSquishTicks() {
		return squishTicks;
	}

	// ─────────────────────────────────────────────────────────────────────
	//  Tick
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * Called once per client tick (END phase) from ClientEvents.
	 * Drives the follow-lerp and drop-gravity physics.
	 */
	public static void tick() {
		jigglePhase += 0.08f;

		if (dropped) {
			// Trigger the squish animation once, after the simulated ground-impact delay
			if (!squishTriggered && fadeTicks == MAX_FADE_TICKS - SQUISH_DELAY_TICKS) {
				squishTicks = MAX_SQUISH_TICKS;
				squishTriggered = true;
			}
			if (squishTicks > 0) {
				squishTicks--;
			}

			// Apply gravity and step position
			velocity = velocity.add(0, -GRAVITY, 0);
			prevPosition = position != null ? position : Vec3.ZERO;
			position = (position != null ? position : Vec3.ZERO).add(velocity);
			fadeTicks--;
			if (fadeTicks <= 0) {
				reset();
			}
			swingDelta = Vec3.ZERO;
			return;
		}

		if (!isActive()) {
			reset();
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		Player player = mc.player;

		Vec3 target = player.getEyePosition().add(player.getLookAngle().scale(FOLLOW_DISTANCE));
		if (position == null) {
			position = target;
			prevPosition = target;
			swingDelta = Vec3.ZERO;
		} else {
			prevPosition = position;
			position = position.lerp(target, FOLLOW_LERP);
			swingDelta = position.subtract(prevPosition);
		}
	}

	// ─────────────────────────────────────────────────────────────────────
	//  Actions
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * Triggers the drop animation.  The ball stops following the player, inherits
	 * zero initial velocity, and gravity takes over until it fades away.
	 */
	public static void drop() {
		if (!dropped && position != null) {
			dropped = true;
			fadeTicks = MAX_FADE_TICKS;
			velocity = Vec3.ZERO;
		}
	}

	// ─────────────────────────────────────────────────────────────────────
	//  Internal
	// ─────────────────────────────────────────────────────────────────────

	private static void reset() {
		position = null;
		prevPosition = null;
		velocity = Vec3.ZERO;
		dropped = false;
		fadeTicks = 0;
		swingDelta = Vec3.ZERO;
		squishTicks = 0;
		squishTriggered = false;
	}
}
