package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class CircusDiscoveryProgress {
	private static final String DISCOVERED_KEY = "hemomancy.circus_discovered";
	private static final ResourceKey<Structure> CIRCUS_PAVILION = ResourceKey.create(
			Registries.STRUCTURE, Hemomancy.rloc("circus_pavilion"));

	private CircusDiscoveryProgress() {
	}

	public static boolean hasDiscovered(Player player) {
		return player.getPersistentData().getBoolean(DISCOVERED_KEY);
	}

	public static boolean markDiscovered(ServerPlayer player) {
		if (hasDiscovered(player)) return false;
		player.getPersistentData().putBoolean(DISCOVERED_KEY, true);
		return true;
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| hasDiscovered(player) || player.tickCount % 20 != 0) return;
		var start = player.serverLevel().structureManager().getStructureWithPieceAt(
				player.blockPosition(), holder -> holder.is(CIRCUS_PAVILION));
		if (!start.isValid() || !markDiscovered(player)) return;
		player.displayClientMessage(Component.translatable("hemomancy.circus.discovery")
				.withStyle(ChatFormatting.DARK_RED), false);
	}
}
