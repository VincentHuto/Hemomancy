package com.vincenthuto.hemomancy.common.manipulation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Server-side record of when each player started charging their selected manipulation. The client reports its
 * own charge ticks on release; the server never credits more than it watched (see
 * {@link ManipulationCastingRules#clampReportedCharge}).
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ManipulationChargeClock {
	private static final Map<UUID, Start> STARTS = new HashMap<>();

	private ManipulationChargeClock() {
	}

	/**
	 * Called for each charge update the client sends while holding the key. {@code held == 1} is a fresh key-down
	 * and restarts the clock; {@code held <= 0} is a cancel; any other update only starts a clock that is missing
	 * or belongs to another manipulation.
	 */
	public static void observe(Player player, String manipulation, int held) {
		if (held <= 0) {
			STARTS.remove(player.getUUID());
			return;
		}
		Start start = STARTS.get(player.getUUID());
		if (held == 1 || start == null || !start.manipulation.equals(manipulation)) {
			STARTS.put(player.getUUID(), new Start(manipulation, player.level().getGameTime()));
		}
	}

	/**
	 * Whether the server observed a key-down for this manipulation that no cast has consumed yet and that is not
	 * stale ({@link ManipulationCastingRules#chargeStartStale}); a key-down left behind by a refused cast expires.
	 */
	public static boolean hasStart(Player player, BloodManipulation manipulation) {
		Start start = STARTS.get(player.getUUID());
		return start != null && start.manipulation.equals(manipulation.getName())
				&& !ManipulationCastingRules.chargeStartStale(start.at, player.level().getGameTime(),
						manipulation.getRequiredChargeTicks());
	}

	/** The client's reported charge ticks, clamped to the hold the server observed for this manipulation. */
	public static float clamp(Player player, BloodManipulation manipulation, float clientTicks) {
		Start start = STARTS.get(player.getUUID());
		long startedAt = start != null && start.manipulation.equals(manipulation.getName()) ? start.at : -1L;
		return ManipulationCastingRules.clampReportedCharge(clientTicks, startedAt, player.level().getGameTime(),
				manipulation.getRequiredChargeTicks());
	}

	/** A charged cast resolved; the next one needs a fresh key-down. */
	public static void release(Player player) {
		STARTS.remove(player.getUUID());
	}

	@SubscribeEvent
	public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
		release(event.getEntity());
	}

	/** Called by {@code TransientServerStateEvents} when a server starts and stops. */
	public static void clearSessionState() {
		STARTS.clear();
	}

	private record Start(String manipulation, long at) {
	}
}
