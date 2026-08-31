package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ManipulationChannelManager {
	private static final int PULSE_TICKS = 20;
	private static final Map<UUID, ChannelState> CHANNELS = new ConcurrentHashMap<>();

	private ManipulationChannelManager() {
	}

	public static void start(ServerPlayer player) {
		if (CHANNELS.containsKey(player.getUUID())) return;
		BloodManipulation manipulation = selectedContinuous(player);
		if (manipulation == null) return;
		if (!manipulation.canContinueChannel(player, player.level())) return;
		if (manipulation.tryPerformContinuousPulse(player, player.level(), player.getMainHandItem(),
				player.blockPosition())) {
			CHANNELS.put(player.getUUID(), new ChannelState(manipulation.getName(), player.level().getGameTime()));
			ManipulationCastSounds.play(player.level(), player, manipulation);
		}
	}

	public static void stop(ServerPlayer player) {
		stop(player, true);
	}

	public static void stop(ServerPlayer player, boolean released) {
		ChannelState state = CHANNELS.remove(player.getUUID());
		if (state == null) return;
		BloodManipulation manipulation = ManipulationInit.getByName(state.manipulationName());
		if (manipulation != null) manipulation.finishContinuousAction(player, released);
	}

	public static boolean isChanneling(UUID playerId) {
		return CHANNELS.containsKey(playerId);
	}

	public static void clearSessionState() {
		for (Map.Entry<UUID, ChannelState> entry : CHANNELS.entrySet()) {
			BloodManipulation manipulation = ManipulationInit.getByName(entry.getValue().manipulationName());
			if (manipulation != null) manipulation.clearContinuousSession(entry.getKey());
		}
		CHANNELS.clear();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ChannelState state = CHANNELS.get(player.getUUID());
		if (state == null) return;
		if (!player.isAlive()) {
			stop(player, false);
			return;
		}
		BloodManipulation manipulation = selectedContinuous(player);
		if (manipulation == null || !manipulation.getName().equals(state.manipulationName())
				|| !manipulation.canContinueChannel(player, player.level())) {
			stop(player, false);
			return;
		}
		manipulation.tickContinuousAction(player, player.level());
		long now = player.level().getGameTime();
		if (now - state.lastPulseTick() < PULSE_TICKS) return;
		if (!manipulation.tryPerformContinuousPulse(player, player.level(), player.getMainHandItem(),
				player.blockPosition())) {
			stop(player, false);
			return;
		}
		CHANNELS.put(player.getUUID(), new ChannelState(state.manipulationName(), now));
	}

	@SubscribeEvent
	public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) stop(player, false);
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) stop(player, false);
	}

	private static BloodManipulation selectedContinuous(ServerPlayer player) {
		if (HemoCapabilityAccess.getUnstainedProgress(player)
				.map(UnstainedAccessRules::blocksKnownBloodPowerUse).orElse(false)) return null;
		var known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null || known.getSelectedManip() == null) return null;
		BloodManipulation selected = ManipulationInit.getByName(known.getSelectedManip().getName());
		if (selected == null || selected.getType() != EnumManipulationType.CONTINUOUS
				|| ManipulationRetirementRules.isRetiredManipulation(selected)
				|| !known.isManipEquipped(selected)) return null;
		return selected;
	}

	private record ChannelState(String manipulationName, long lastPulseTick) {
	}
}
