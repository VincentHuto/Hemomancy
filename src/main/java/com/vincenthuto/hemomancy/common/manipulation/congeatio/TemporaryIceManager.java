package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TemporaryIceManager {

	private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> TEMPORARY_ICE = new HashMap<>();
	private static final Map<ResourceKey<Level>, Map<BlockPos, UUID>> ICE_OWNERS = new HashMap<>();
	private static final Set<UUID> RECOVERY_CLEARED = new HashSet<>();

	private TemporaryIceManager() {
	}

	public static void clearSessionState() {
		TEMPORARY_ICE.clear();
		ICE_OWNERS.clear();
		RECOVERY_CLEARED.clear();
	}

	public static boolean place(ServerLevel level, BlockPos pos, BlockState state, int lifetimeTicks) {
		return placeOwned(level, pos, state, lifetimeTicks, null);
	}

	public static boolean placeOwned(ServerLevel level, BlockPos pos, BlockState state, int lifetimeTicks,
			UUID owner) {
		BlockState current = level.getBlockState(pos);
		if (!current.isAir() && !current.canBeReplaced()) {
			return false;
		}
		level.setBlock(pos, state, 3);
		TEMPORARY_ICE.computeIfAbsent(level.dimension(), key -> new HashMap<>())
				.put(pos.immutable(), level.getGameTime() + lifetimeTicks);
		if (owner != null) {
			ICE_OWNERS.computeIfAbsent(level.dimension(), key -> new HashMap<>()).put(pos.immutable(), owner);
			RECOVERY_CLEARED.remove(owner);
		}
		return true;
	}

	public static void clearOwned(ServerLevel level, AABB bounds, UUID owner) {
		Map<BlockPos, Long> entries = TEMPORARY_ICE.get(level.dimension());
		Map<BlockPos, UUID> owners = ICE_OWNERS.get(level.dimension());
		if (entries == null || entries.isEmpty() || owners == null || owners.isEmpty()) return;
		Iterator<Map.Entry<BlockPos, Long>> iterator = entries.entrySet().iterator();
		while (iterator.hasNext()) {
			BlockPos pos = iterator.next().getKey();
			if (!owner.equals(owners.get(pos))
					|| !bounds.contains(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)) continue;
			if (level.getBlockState(pos).is(Blocks.PACKED_ICE)) {
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
			iterator.remove();
			owners.remove(pos);
		}
		if (entries.isEmpty()) TEMPORARY_ICE.remove(level.dimension());
		if (owners.isEmpty()) ICE_OWNERS.remove(level.dimension());
		RECOVERY_CLEARED.add(owner);
	}

	public static void clearEncounterOwned(ServerLevel level, AABB bounds, UUID owner, boolean allowReloadFallback) {
		Map<BlockPos, UUID> owners = ICE_OWNERS.get(level.dimension());
		boolean hasTrackedIce = owners != null && owners.containsValue(owner);
		clearOwned(level, bounds, owner);
		if (hasTrackedIce || !allowReloadFallback || !RECOVERY_CLEARED.add(owner)) return;
		BlockPos min = BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ);
		BlockPos max = BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ);
		for (BlockPos cursor : BlockPos.betweenClosed(min, max)) {
			if (level.getBlockState(cursor).is(Blocks.PACKED_ICE)) {
				level.setBlock(cursor, Blocks.AIR.defaultBlockState(), 3);
			}
		}
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
			Map<BlockPos, UUID> owners = ICE_OWNERS.get(level.dimension());
			if (owners != null) owners.remove(pos);
			iterator.remove();
		}
		if (entries.isEmpty()) {
			TEMPORARY_ICE.remove(level.dimension());
			ICE_OWNERS.remove(level.dimension());
		}
	}
}
