package com.vincenthuto.hemomancy.common.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * The one writer of a player's {@code mayfly} and {@code flying} abilities. Each {@link Source} that grants flight says
 * how it grants and how it takes flight away.
 *
 * <p>A revoke does not yet ask whether another source still grants flight, exactly as before step 9: the Elytra
 * effect grounding an Edacious wearer drops their flight until Edacious grants it again on the next player tick, and
 * in water it keeps doing so (G-7, see the consolidation ledger).
 */
public final class FlightArbiter {
	private static final float DEFAULT_FLYING_SPEED = 0.05F;

	private FlightArbiter() {
	}

	public enum Source {
		/** The full Edacious set: slow hover flight while worn. */
		EDACIOUS_SET(0.025F, false, true),
		/** Silent Slipping: lifts the player into flight at once. */
		SILENT_SLIPPING(0.035F, true, true),
		/** The Elytra effect: hover flight while airborne and out of water. A creative player's speed resets. */
		ELYTRA_EFFECT(Float.NaN, false, false);

		private final float flyingSpeed;
		private final boolean liftsIntoFlight;
		private final boolean resetsSpeedForEveryone;

		Source(float flyingSpeed, boolean liftsIntoFlight, boolean resetsSpeedForEveryone) {
			this.flyingSpeed = flyingSpeed;
			this.liftsIntoFlight = liftsIntoFlight;
			this.resetsSpeedForEveryone = resetsSpeedForEveryone;
		}
	}

	public static boolean mayFly(Player player) {
		return player.getAbilities().mayfly;
	}

	/** Lets the player fly for {@code source}, at its flying speed, and syncs a server player's abilities. */
	public static void grant(Player player, Source source) {
		player.getAbilities().mayfly = true;
		if (source.liftsIntoFlight) player.getAbilities().flying = true;
		if (!Float.isNaN(source.flyingSpeed)) player.getAbilities().setFlyingSpeed(source.flyingSpeed);
		sync(player);
	}

	/**
	 * Takes flight away from a survival or adventure player; creative and spectating players keep theirs. The flying
	 * speed returns to the default (for the Elytra effect, only a creative or spectating player's).
	 */
	public static void revoke(Player player, Source source) {
		boolean creative = player.isCreative() || player.isSpectator();
		if (!creative) {
			player.getAbilities().mayfly = false;
			player.getAbilities().flying = false;
		}
		if (source.resetsSpeedForEveryone || creative) player.getAbilities().setFlyingSpeed(DEFAULT_FLYING_SPEED);
		sync(player);
	}

	/** Ends active flight without touching the ability or syncing: Sheolic Bastion's root, every tick. */
	public static void ground(Player player) {
		player.getAbilities().flying = false;
	}

	private static void sync(Player player) {
		if (player instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
	}
}
