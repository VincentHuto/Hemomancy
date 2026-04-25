package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.FluidInit;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generates a shallow morphic nectar pool on the fungal gardens surface.
 * The pool is an irregular ellipse filled with morphic_nectar source fluid,
 * ringed by sporite_crystal formations that cluster more densely at the rim
 * and scatter sparsely outward.
 *
 * <p>Shape: 3–6 block radius ellipse, 1 block of visible fluid depth.
 * Sporite crystals grow from the rim and occasionally from outside it.</p>
 */
public class MorphicPoolFeature extends Feature<NoneFeatureConfiguration> {

	private static BlockState NECTAR;
	private static BlockState SPORITE;
	private static boolean resolved = false;

	private static void ensureResolved() {
		if (resolved) return;
		NECTAR = BlockInit.MORPHIC_NECTAR_BLOCK.get().defaultBlockState();
		SPORITE = BlockInit.sporite_crystal.get().defaultBlockState();
		resolved = true;
	}

	public MorphicPoolFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		ensureResolved();

		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		// Randomise pool shape
		float rx = 3.0f + random.nextFloat() * 3.0f;   // 3–6 blocks
		float rz = 3.0f + random.nextFloat() * 3.0f;
		// Independent squish factor makes pools feel less perfectly elliptical
		float squishX = 0.85f + random.nextFloat() * 0.3f;
		float squishZ = 0.85f + random.nextFloat() * 0.3f;
		float edgeNoise = 0.5f + random.nextFloat() * 0.7f;

		int maxR = Mth.ceil(Math.max(rx, rz)) + 3;
		int placed = 0;
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int dx = -maxR; dx <= maxR; dx++) {
			for (int dz = -maxR; dz <= maxR; dz++) {
				// Per-cell noise jitter blurs the edge slightly for a natural pool outline
				float jx = dx + (random.nextFloat() - 0.5f) * edgeNoise;
				float jz = dz + (random.nextFloat() - 0.5f) * edgeNoise;
				float dist = Mth.sqrt((jx * jx * squishX) / (rx * rx) + (jz * jz * squishZ) / (rz * rz));

				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;

				// Surface Y: the top of solid terrain (the block we'll replace)
				int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;

				mutable.set(x, surfaceY, z);

				if (dist < 0.78f) {
					// Inner basin — fill surface + one block below with nectar source
					if (isReplaceableByPool(level.getBlockState(mutable))) {
						level.setBlock(mutable, NECTAR, 2);
						placed++;
					}
					mutable.move(0, -1, 0);
					if (isReplaceableByPool(level.getBlockState(mutable))) {
						level.setBlock(mutable, NECTAR, 2);
					}

				} else if (dist < 1.0f) {
					// Rim — dense ring of sporite crystals
					if (isReplaceableByPool(level.getBlockState(mutable))) {
						level.setBlock(mutable, SPORITE, 2);
						placed++;
					}

				} else if (dist < 1.4f && random.nextInt(5) == 0) {
					// Outer scatter — occasional crystal clusters beyond the rim
					if (isReplaceableByPool(level.getBlockState(mutable))) {
						level.setBlock(mutable, SPORITE, 2);
						placed++;
					}
				}
			}
		}

		return placed > 0;
	}

	/** Blocks that the pool is allowed to displace. */
	private boolean isReplaceableByPool(BlockState state) {
		if (state.canBeReplaced() || state.isAir()) return true;
		Block b = state.getBlock();
		return b == BlockInit.erythrocytic_mycelium.get()
				|| b == BlockInit.erythrocytic_dirt.get()
				|| b == BlockInit.venous_stone.get()
				|| b == BlockInit.infested_venous_stone.get()
				|| b == BlockInit.hyphae_block.get()
				|| b == BlockInit.conscious_mass.get()
				|| b == BlockInit.hemorrhagic_crust.get()
				|| b == BlockInit.calcified_hyphae.get()
				|| b == FluidInit.MORPHIC_NECTAR_BLOCK.get();
	}
}
