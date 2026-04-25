package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VenousRidgeFeature extends Feature<NoneFeatureConfiguration> {
	private static final int FEATURE_WRITE_RADIUS_CHUNKS = 0;
	private static final float TWO_PI = (float) (Math.PI * 2.0D);
	private static final float HALF_PI = (float) (Math.PI * 0.5D);

	public VenousRidgeFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();

		int length = Mth.nextInt(random, 18, 34);
		float angle = random.nextFloat() * TWO_PI;
		float curve = (random.nextFloat() - 0.5F) * 0.055F;
		float wavePhase = random.nextFloat() * TWO_PI;
		float waveScale = Mth.nextFloat(random, 1.0F, 2.0F);
		int palette = random.nextInt(3);
		boolean emerges = random.nextFloat() < 0.45F;
		int buriedDepth = emerges ? Mth.nextInt(random, 1, 2) : 0;
		int placed = 0;

		for (int step = 0; step < length; step++) {
			float t = step / (float) length;
			float wave = Mth.sin(t * TWO_PI + wavePhase) * waveScale
					+ Mth.sin(t * Mth.PI * 3.0F + wavePhase * 0.5F) * 0.65F;
			float localAngle = angle + curve * step + Mth.sin(t * Mth.PI) * 0.25F;
			int x = origin.getX() + Mth.floor(Mth.cos(localAngle) * step + Mth.cos(localAngle + HALF_PI) * wave);
			int z = origin.getZ() + Mth.floor(Mth.sin(localAngle) * step + Mth.sin(localAngle + HALF_PI) * wave);
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
			BlockPos surface = new BlockPos(x, y, z);
			int yOffset = emerges && t < 0.35F ? Mth.floor(Mth.lerp(t / 0.35F, -buriedDepth, 0.0F)) : 0;
			BlockPos node = surface.above(yOffset);
			boolean embedded = yOffset < 0;

			if (!respectsCutoff(level, node) || !canPlaceAt(level, surface, node, embedded)) {
				continue;
			}

			int radius = random.nextInt(8) == 0 ? 1 : 0;
			int height = (!embedded && random.nextInt(emerges && t < 0.55F ? 4 : 9) == 0) ? 2 : 1;
			placed += placeRidgeNode(level, random, surface, node, radius, height, localAngle, embedded, palette);
		}

		return placed > 0;
	}

	private int placeRidgeNode(WorldGenLevel level, RandomSource random, BlockPos surface, BlockPos center, int radius,
			int height, float angle, boolean embedded, int palette) {
		int placed = 0;
		Direction side = Math.abs(Mth.cos(angle)) > Math.abs(Mth.sin(angle)) ? Direction.NORTH : Direction.EAST;

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (Math.abs(dx) + Math.abs(dz) > radius) {
					continue;
				}

				BlockPos node = center.offset(dx, 0, dz);
				if (!respectsCutoff(level, node) || !canPlaceAt(level, surface.offset(dx, 0, dz), node, embedded)) {
					continue;
				}

				placed += setRidgeBlock(level, random, node, embedded, palette);
				if (dx == 0 && dz == 0) {
					for (int y = 1; y < height; y++) {
						placed += setRidgeBlock(level, random, node.above(y), false, palette);
					}
				}
			}
		}

		if (!embedded && random.nextInt(9) == 0) {
			placed += setRidgeBlock(level, random, surface.relative(side), false, palette);
			if (random.nextBoolean()) {
				placed += setRidgeBlock(level, random, surface.relative(side.getOpposite()), false, palette);
			}
		}

		return placed;
	}

	private int setRidgeBlock(WorldGenLevel level, RandomSource random, BlockPos pos, boolean embedded, int palette) {
		BlockState state = level.getBlockState(pos);
		if (!respectsCutoff(level, pos) || !(state.canBeReplaced() || embedded && isFungalSurfaceBlock(state.getBlock()))) {
			return 0;
		}

		level.setBlock(pos, randomRidgeState(random, palette), 2);
		return 1;
	}

	private BlockState randomRidgeState(RandomSource random, int palette) {
		int roll = random.nextInt(12);
		if (palette == 0 && roll < 8) {
			return BlockInit.infested_venous_stone.get().defaultBlockState();
		}
		if (palette == 1 && roll < 8) {
			return BlockInit.hyphae_block.get().defaultBlockState();
		}
		if (palette == 2 && roll < 6) {
			return BlockInit.hemorrhagic_crust.get().defaultBlockState();
		}
		if (roll == 11) {
			return BlockInit.conscious_mass.get().defaultBlockState();
		}
		return random.nextBoolean()
				? BlockInit.infested_venous_stone.get().defaultBlockState()
				: BlockInit.hyphae_block.get().defaultBlockState();
	}

	private boolean canPlaceAt(WorldGenLevel level, BlockPos surface, BlockPos target, boolean embedded) {
		BlockState below = level.getBlockState(surface.below());
		BlockState targetState = level.getBlockState(target);
		return !level.getBlockState(surface).is(Blocks.WATER)
				&& (targetState.canBeReplaced() || embedded && isFungalSurfaceBlock(targetState.getBlock()))
				&& isFungalSurfaceBlock(below.getBlock());
	}

	private boolean isFungalSurfaceBlock(Block block) {
		return block == BlockInit.erythrocytic_mycelium.get()
				|| block == BlockInit.erythrocytic_dirt.get()
				|| block == BlockInit.venous_stone.get()
				|| block == BlockInit.infested_venous_stone.get()
				|| block == BlockInit.hyphae_block.get()
				|| block == BlockInit.conscious_mass.get()
				|| block == BlockInit.hemorrhagic_crust.get()
				|| block == BlockInit.calcified_hyphae.get();
	}

	private boolean respectsCutoff(WorldGenLevel level, BlockPos pos) {
		if (!(level instanceof WorldGenRegion region)) {
			return true;
		}

		ChunkPos center = region.getCenter();
		int chunkX = SectionPos.blockToSectionCoord(pos.getX());
		int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
		return Math.abs(chunkX - center.x) <= FEATURE_WRITE_RADIUS_CHUNKS
				&& Math.abs(chunkZ - center.z) <= FEATURE_WRITE_RADIUS_CHUNKS;
	}
}
