package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DeepOceanVentFeature extends Feature<NoneFeatureConfiguration> {
	private static final int MIN_WATER_DEPTH = 12;

	private static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
	private static final BlockState SMOOTH_BASALT = Blocks.SMOOTH_BASALT.defaultBlockState();
	private static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
	private static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
	private static final BlockState MAGMA = Blocks.MAGMA_BLOCK.defaultBlockState();

	public DeepOceanVentFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		RandomSource random = ctx.random();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		BlockPos floor = findVentFloor(level, ctx.origin(), mutable);
		if (floor == null || !hasStableFloor(level, floor, mutable)) {
			return false;
		}

		int radius = 4 + random.nextInt(3);
		buildMineralApron(level, floor, radius, random, mutable);

		int chimneyCount = 2 + random.nextInt(3);
		for (int i = 0; i < chimneyCount; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int distance = i == 0 ? 0 : 1 + random.nextInt(Math.max(2, radius - 1));
			int dx = Mth.floor(Math.cos(angle) * distance);
			int dz = Mth.floor(Math.sin(angle) * distance);
			BlockPos chimneyBase = findLocalFloor(level, floor.getX() + dx, floor.getZ() + dz, floor.getY() + 5, mutable);
			if (chimneyBase != null) {
				buildChimney(level, chimneyBase, 3 + random.nextInt(5), random, mutable);
			}
		}

		spawnInhabitants(level, floor, radius, random, mutable);
		return true;
	}

	private BlockPos findVentFloor(WorldGenLevel level, BlockPos origin, BlockPos.MutableBlockPos mutable) {
		int startY = Math.min(level.getSeaLevel() + 8, level.getMaxBuildHeight() - 2);
		return findLocalFloor(level, origin.getX(), origin.getZ(), Math.max(startY, origin.getY() + 8), mutable);
	}

	private BlockPos findLocalFloor(WorldGenLevel level, int x, int z, int startY, BlockPos.MutableBlockPos mutable) {
		int yStart = Math.min(startY, level.getMaxBuildHeight() - 2);
		for (int y = yStart; y > level.getMinBuildHeight() + 1; y--) {
			mutable.set(x, y, z);
			if (!isWaterSource(level.getBlockState(mutable))) {
				continue;
			}
			mutable.move(Direction.DOWN);
			BlockState below = level.getBlockState(mutable);
			boolean sturdyFloor = below.isFaceSturdy(level, mutable, Direction.UP) && !below.liquid();
			mutable.move(Direction.UP);
			if (sturdyFloor && waterDepthAbove(level, mutable) >= MIN_WATER_DEPTH) {
				return mutable.immutable();
			}
		}
		return null;
	}

	private int waterDepthAbove(WorldGenLevel level, BlockPos pos) {
		int depth = 0;
		BlockPos.MutableBlockPos probe = pos.mutable();
		int top = Math.min(level.getSeaLevel() + 4, level.getMaxBuildHeight() - 1);
		while (probe.getY() <= top && isWaterSource(level.getBlockState(probe))) {
			depth++;
			probe.move(Direction.UP);
		}
		return depth;
	}

	private boolean hasStableFloor(WorldGenLevel level, BlockPos floor, BlockPos.MutableBlockPos mutable) {
		int found = 0;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				BlockPos local = findLocalFloor(level, floor.getX() + dx, floor.getZ() + dz, floor.getY() + 5, mutable);
				if (local != null && Math.abs(local.getY() - floor.getY()) <= 3) {
					found++;
				}
			}
		}
		return found >= 16;
	}

	private void buildMineralApron(WorldGenLevel level, BlockPos center, int radius, RandomSource random,
								   BlockPos.MutableBlockPos mutable) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				float dist = Mth.sqrt(dx * dx + dz * dz);
				float noise = Mth.sin(dx * 0.8F + dz * 1.1F) * 0.45F + random.nextFloat() * 0.35F;
				if (dist > radius + noise) {
					continue;
				}
				BlockPos floor = findLocalFloor(level, center.getX() + dx, center.getZ() + dz, center.getY() + 5, mutable);
				if (floor == null || Math.abs(floor.getY() - center.getY()) > 4) {
					continue;
				}
				mutable.set(floor);
				placeIfSubmerged(level, mutable, chooseApronBlock(random, dist, radius));
				if (dist < radius * 0.55F && random.nextInt(7) == 0) {
					mutable.move(Direction.UP);
					placeIfSubmerged(level, mutable, chooseMineralBlock(random));
				}
			}
		}
	}

	private void buildChimney(WorldGenLevel level, BlockPos base, int height, RandomSource random,
							  BlockPos.MutableBlockPos mutable) {
		for (int y = 0; y < height; y++) {
			float progress = (float) y / Math.max(1, height - 1);
			float radius = progress < 0.25F ? 1.6F : 1.05F;
			if (progress > 0.7F) {
				radius = 0.85F;
			}

			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					float dist = Mth.sqrt(dx * dx + dz * dz);
					float roughness = random.nextFloat() * 0.25F;
					if (dist <= radius + roughness) {
						mutable.set(base.getX() + dx, base.getY() + y, base.getZ() + dz);
						BlockState block = y == height - 1 && dist < 0.85F ? MAGMA : chooseMineralBlock(random);
						placeIfSubmerged(level, mutable, block);
					}
				}
			}
		}

		int sideVents = 1 + random.nextInt(3);
		for (int i = 0; i < sideVents; i++) {
			Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
			int y = 1 + random.nextInt(Math.max(1, height - 1));
			mutable.set(base.getX() + direction.getStepX() * 2, base.getY() + y, base.getZ() + direction.getStepZ() * 2);
			placeIfSubmerged(level, mutable, random.nextBoolean() ? MAGMA : SMOOTH_BASALT);
		}
	}

	private BlockState chooseApronBlock(RandomSource random, float dist, int radius) {
		if (dist < radius * 0.65F && random.nextInt(9) == 0) {
			return MAGMA;
		}
		if (random.nextInt(28) == 0) {
			return BlockInit.infested_venous_stone.get().defaultBlockState();
		}
		if (random.nextInt(40) == 0) {
			return BlockInit.conscious_mass.get().defaultBlockState();
		}
		return chooseMineralBlock(random);
	}

	private BlockState chooseMineralBlock(RandomSource random) {
		int roll = random.nextInt(10);
		if (roll < 4) return BASALT;
		if (roll < 6) return SMOOTH_BASALT;
		if (roll < 8) return BLACKSTONE;
		return DEEPSLATE;
	}

	private static boolean isWaterSource(BlockState state) {
		return state.getFluidState().is(FluidTags.WATER) && state.getFluidState().isSource();
	}

	private static void placeIfSubmerged(WorldGenLevel level, BlockPos.MutableBlockPos pos, BlockState state) {
		BlockState existing = level.getBlockState(pos);
		if (existing.isAir() || existing.canBeReplaced() || existing.getFluidState().is(FluidTags.WATER)) {
			level.setBlock(pos, state, 2);
		}
	}

	private void spawnInhabitants(WorldGenLevel level, BlockPos center, int radius, RandomSource random,
								  BlockPos.MutableBlockPos mutable) {
		int count = 2 + random.nextInt(4);
		int spawned = 0;
		for (int attempt = 0; attempt < 40 && spawned < count; attempt++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int distance = 2 + random.nextInt(radius + 5);
			int x = center.getX() + Mth.floor(Math.cos(angle) * distance);
			int z = center.getZ() + Mth.floor(Math.sin(angle) * distance);
			BlockPos floor = findLocalFloor(level, x, z, center.getY() + 6, mutable);
			if (floor == null || Math.abs(floor.getY() - center.getY()) > 5) {
				continue;
			}
			mutable.set(floor).move(Direction.UP);
			if (isWaterSource(level.getBlockState(floor)) && isWaterSource(level.getBlockState(mutable))) {
				spawnMob(level, EntityInit.chalybeate_snail.get(), floor);
				spawned++;
			}
		}
	}

	private <T extends Entity> void spawnMob(WorldGenLevel level, EntityType<T> type, BlockPos pos) {
		T entity = type.create(level.getLevel());
		if (entity == null) return;
		entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D,
				level.getRandom().nextFloat() * 360.0F, 0.0F);
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
			mob.setPersistenceRequired();
		}
		level.addFreshEntityWithPassengers(entity);
	}
}
