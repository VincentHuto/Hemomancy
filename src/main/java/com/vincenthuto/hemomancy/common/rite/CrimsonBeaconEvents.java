package com.vincenthuto.hemomancy.common.rite;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.CrimsonBeaconSavedData.BeaconEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * Handles Crimson Beacon death-teleport: when a player with an active beacon
 * takes fatal damage, their body is teleported to the beacon position before
 * the death is finalized. The beacon is consumed (one-time use).
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CrimsonBeaconEvents {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		if (player.level().isClientSide) return;

		ServerLevel overworld = player.server.overworld();
		CrimsonBeaconSavedData data = CrimsonBeaconSavedData.get(overworld);

		if (!data.hasBeacon(player.getUUID())) return;

		BeaconEntry beacon = data.getBeacon(player.getUUID());
		if (beacon == null) return;

		// Check dimension matches
		String currentDimension = player.level().dimension().location().toString();
		if (!currentDimension.equals(beacon.dimension())) {
			// Beacon is in a different dimension â€” still consume it but warn
			data.removeBeacon(player.getUUID());
			player.displayClientMessage(
					Component.literal("The Crimson Beacon was too distant to reach...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			return;
		}

		// Teleport the player to the beacon before death finishes
		player.teleportTo(
				beacon.pos().getX() + 0.5,
				beacon.pos().getY() + 1.0,
				beacon.pos().getZ() + 0.5);

		// Consume the beacon (one-time use)
		data.removeBeacon(player.getUUID());

		player.displayClientMessage(
				Component.literal("The Crimson Beacon pulls your dying body home...")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);
	}
}
