package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LanternTickLightManager {
	private static final Map<UUID, TrackedLight> trackedLights = new HashMap<>();

	private LanternTickLightManager() {
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		MinecraftServer server = event.getServer();
		if (server.getTickCount() % LanternTickRules.HELMET_LIGHT_REFRESH_TICKS != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			tickPlayer(player);
		}
		cleanupMissingPlayers(server);
	}

	private static void tickPlayer(ServerPlayer player) {
		boolean wearing = player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.lantern_tick_helmet.get());
		if (!wearing || player.isSpectator()) {
			clear(player.serverLevel(), player.getUUID());
			return;
		}
		BlockPos lightPos = chooseLightPos(player);
		if (lightPos == null) {
			clear(player.serverLevel(), player.getUUID());
			return;
		}
		TrackedLight previous = trackedLights.get(player.getUUID());
		if (previous != null && (!previous.dimension.equals(player.level().dimension()) || !previous.pos.equals(lightPos))) {
			clearAt(player.getServer(), previous);
		}
		BlockState state = player.serverLevel().getBlockState(lightPos);
		if (state.isAir() || state.is(Blocks.LIGHT)) {
			player.serverLevel().setBlock(lightPos, Blocks.LIGHT.defaultBlockState()
					.setValue(LightBlock.LEVEL, LanternTickRules.HELMET_LIGHT_LEVEL), 3);
			trackedLights.put(player.getUUID(), new TrackedLight(player.level().dimension(), lightPos));
		}
	}

	private static BlockPos chooseLightPos(ServerPlayer player) {
		BlockPos eye = BlockPos.containing(player.getEyePosition());
		if (canHoldLight(player.serverLevel(), eye)) {
			return eye;
		}
		BlockPos feet = player.blockPosition();
		if (canHoldLight(player.serverLevel(), feet)) {
			return feet;
		}
		for (BlockPos candidate : BlockPos.betweenClosed(feet.offset(-1, 0, -1), feet.offset(1, 1, 1))) {
			if (canHoldLight(player.serverLevel(), candidate)) {
				return candidate.immutable();
			}
		}
		return null;
	}

	private static boolean canHoldLight(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isAir() || state.is(Blocks.LIGHT);
	}

	private static void cleanupMissingPlayers(MinecraftServer server) {
		Iterator<Map.Entry<UUID, TrackedLight>> iterator = trackedLights.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, TrackedLight> entry = iterator.next();
			if (server.getPlayerList().getPlayer(entry.getKey()) == null) {
				clearAt(server, entry.getValue());
				iterator.remove();
			}
		}
	}

	private static void clear(ServerLevel fallbackLevel, UUID playerId) {
		TrackedLight tracked = trackedLights.remove(playerId);
		if (tracked != null) {
			clearAt(fallbackLevel.getServer(), tracked);
		}
	}

	private static void clearAt(MinecraftServer server, TrackedLight tracked) {
		ServerLevel level = server.getLevel(tracked.dimension);
		if (level != null && level.getBlockState(tracked.pos).is(Blocks.LIGHT)) {
			level.setBlock(tracked.pos, Blocks.AIR.defaultBlockState(), 3);
		}
	}

	private record TrackedLight(ResourceKey<Level> dimension, BlockPos pos) {
	}
}
