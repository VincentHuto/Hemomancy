package com.vincenthuto.hemomancy.common.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The one writer of a player's {@code mayfly} and {@code flying} abilities. Each {@link Source} that grants flight says
 * how it grants and how it takes flight away.
 *
 * <p>A revoke asks the other sources first (G-7b): while another source still grants flight, the player keeps it at
 * that source's speed. The granting sources are kept per player object, so a respawned player starts with none.
 */
public final class FlightArbiter {
	private static final float DEFAULT_FLYING_SPEED = 0.05F;
	private static final Map<Player, Set<Source>> GRANTS = Collections.synchronizedMap(new WeakHashMap<>());

	private FlightArbiter() {
	}

	public enum Source {
		/** The full Edacious set: slow hover flight while worn. */
		EDACIOUS_SET(0.025F, false, true),
		/** Silent Slipping: lifts the player into flight at once. */
		SILENT_SLIPPING(0.035F, true, true),
		/** The Elytra effect: hover flight while airborne and out of water. A creative player's speed resets. */
		ELYTRA_EFFECT(Float.NaN, false, false),
		/** A possessing Silentium in a winged facsimile (bat, blaze): lifted into flight at once. */
		POSSESSION(0.04F, true, true);

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

	/** Whether {@code source} currently holds a flight grant for the player. */
	public static boolean grants(Player player, Source source) {
		synchronized (GRANTS) {
			Set<Source> granted = GRANTS.get(player);
			return granted != null && granted.contains(source);
		}
	}

	/** Lets the player fly for {@code source}, at its flying speed, and syncs a server player's abilities. */
	public static void grant(Player player, Source source) {
		synchronized (GRANTS) {
			GRANTS.computeIfAbsent(player, ignored -> EnumSet.noneOf(Source.class)).add(source);
		}
		player.getAbilities().mayfly = true;
		if (source.liftsIntoFlight) player.getAbilities().flying = true;
		if (!Float.isNaN(source.flyingSpeed)) player.getAbilities().setFlyingSpeed(source.flyingSpeed);
		sync(player);
	}

	/**
	 * Ends {@code source}'s grant. While another source still grants flight the player keeps it, at that source's
	 * speed. Otherwise flight is taken away from a survival or adventure player; creative and spectating players keep
	 * theirs. The flying speed returns to the default (for the Elytra effect, only a creative or spectating player's).
	 */
	public static void revoke(Player player, Source source) {
		Source remaining = release(player, source);
		if (remaining != null) {
			if (!Float.isNaN(remaining.flyingSpeed)) player.getAbilities().setFlyingSpeed(remaining.flyingSpeed);
			sync(player);
			return;
		}
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

	/** Removes {@code source} from the player's grants and returns a source that still grants, or null. */
	private static Source release(Player player, Source source) {
		synchronized (GRANTS) {
			Set<Source> granted = GRANTS.get(player);
			if (granted == null) return null;
			granted.remove(source);
			if (granted.isEmpty()) {
				GRANTS.remove(player);
				return null;
			}
			return granted.iterator().next();
		}
	}

	public static void logout(Player player) {
		GRANTS.remove(player);
	}

	public static void clearSessionState() {
		GRANTS.clear();
	}

	private static void sync(Player player) {
		if (player instanceof ServerPlayer serverPlayer) serverPlayer.onUpdateAbilities();
	}
}
