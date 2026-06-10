package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TemporaryIceManager {

	private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> TEMPORARY_ICE = new HashMap<>();

	private TemporaryIceManager() {
	}

	public static boolean place(ServerLevel level, BlockPos pos, BlockState state, int lifetimeTicks) {
		BlockState current = level.getBlockState(pos);
		if (!current.isAir() && !current.canBeReplaced()) {
			return false;
		}
		level.setBlock(pos, state, 3);
		TEMPORARY_ICE.computeIfAbsent(level.dimension(), key -> new HashMap<>())
				.put(pos.immutable(), level.getGameTime() + lifetimeTicks);
		return true;
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		Map<BlockPos, Long> entries = TEMPORARY_ICE.get(level.dimension());
		if (entries == null || entries.isEmpty()) {
			return;
		}

		long now = level.getGameTime();
		Iterator<Map.Entry<BlockPos, Long>> iterator = entries.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<BlockPos, Long> entry = iterator.next();
			if (now < entry.getValue()) {
				continue;
			}
			BlockPos pos = entry.getKey();
			if (level.getBlockState(pos).is(Blocks.PACKED_ICE)) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			iterator.remove();
		}
		if (entries.isEmpty()) {
			TEMPORARY_ICE.remove(level.dimension());
		}
	}
}
