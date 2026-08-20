package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BlackVeilCovenantManager {
	private static final List<Entry> VEILS = new ArrayList<>();

	private BlackVeilCovenantManager() {
	}

	public static void clearSessionState() {
		VEILS.clear();
	}

	public static void addVeil(ServerLevel level, BlockPos center, double radius, int durationTicks) {
		VEILS.add(new Entry(level.dimension(), center.immutable(), radius, level.getGameTime() + durationTicks));
	}

	public static boolean isSyntheticDarkness(Level level, BlockPos pos) {
		long now = level.getGameTime();
		for (Entry entry : VEILS) {
			if (entry.dimension.equals(level.dimension()) && now <= entry.expiryTick && entry.contains(pos)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDarkEnough(Level level, BlockPos pos, int maxLightLevel) {
		return isSyntheticDarkness(level, pos) || level.getMaxLocalRawBrightness(pos) <= maxLightLevel;
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		long now = level.getGameTime();
		Iterator<Entry> iterator = VEILS.iterator();
		while (iterator.hasNext()) {
			Entry entry = iterator.next();
			if (entry.dimension.equals(level.dimension()) && now > entry.expiryTick) {
				iterator.remove();
			}
		}
	}

	private record Entry(ResourceKey<Level> dimension, BlockPos center, double radius, long expiryTick) {
		boolean contains(BlockPos pos) {
			double dx = pos.getX() + 0.5 - (center.getX() + 0.5);
			double dy = pos.getY() + 0.5 - (center.getY() + 1.0);
			double dz = pos.getZ() + 0.5 - (center.getZ() + 0.5);
			return dx * dx + dy * dy + dz * dz <= radius * radius;
		}
	}
}
