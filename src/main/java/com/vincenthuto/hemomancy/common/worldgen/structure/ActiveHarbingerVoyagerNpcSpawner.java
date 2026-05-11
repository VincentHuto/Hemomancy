package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class ActiveHarbingerVoyagerNpcSpawner {
	private static final int MAX_PLACEMENT_ATTEMPTS = 24;

	private ActiveHarbingerVoyagerNpcSpawner() {
	}

	public static void spawnForActiveVessel(WorldGenLevel level, BoundingBox fullBox, BlockPos center,
			RandomSource random) {
		if (ActiveHarbingerVoyagerNpcRules.shouldSpawnVoyager()) {
			spawnNear(level, fullBox, random, EntityInit.harbinger_voyager.get(), center, 4);
		}
		if (ActiveHarbingerVoyagerNpcRules
				.shouldSpawnVotaryWayfarer(random.nextInt(ActiveHarbingerVoyagerNpcRules.VOTARY_WAYFARER_CHANCE))) {
			spawnNear(level, fullBox, random, EntityInit.harbinger_votary_wayfarer.get(), center.offset(2, 0, -2), 3);
		}
	}

	private static <T extends Entity> void spawnNear(WorldGenLevel level, BoundingBox fullBox, RandomSource random,
			EntityType<T> type, BlockPos origin, int spread) {
		for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS; attempt++) {
			BlockPos probe = origin.offset(random.nextInt(spread * 2 + 1) - spread, 0,
					random.nextInt(spread * 2 + 1) - spread);
			BlockPos pos = findDeckFloor(level, fullBox, probe, 4);
			if (pos != null) {
				spawnMob(level, type, pos, random);
				return;
			}
		}
	}

	private static BlockPos findDeckFloor(WorldGenLevel level, BoundingBox fullBox, BlockPos origin, int radius) {
		for (int dy = radius; dy >= -radius; dy--) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					BlockPos pos = origin.offset(dx, dy, dz);
					if (!fullBox.isInside(pos.getX(), pos.getY(), pos.getZ())) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (!state.isAir()) {
						continue;
					}
					BlockPos below = pos.below();
					if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
						return pos;
					}
				}
			}
		}
		return null;
	}

	private static <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos,
			RandomSource random) {
		T entity = type.create(level.getLevel());
		if (entity == null) {
			return;
		}
		entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}
}
