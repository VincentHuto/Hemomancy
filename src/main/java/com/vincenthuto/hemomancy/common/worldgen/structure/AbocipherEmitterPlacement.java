package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.AbocipherEmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class AbocipherEmitterPlacement {
	private static final int MAX_ATTEMPTS = 48;

	private AbocipherEmitterPlacement() {
	}

	public static void placeBloodTempleEmitters(WorldGenLevel level, BoundingBox fullBox, RandomSource random,
			BlockPos center) {
		BlockPos origin = new BlockPos(center.getX(), Math.max(fullBox.minY() + 2, center.getY()), center.getZ());
		placeNear(level, fullBox, random, origin, 4, 1, AbocipherEmitterBlockEntity.Profile.BLOOD_TEMPLE);
	}

	private static boolean placeNear(WorldGenLevel level, BoundingBox fullBox, RandomSource random, BlockPos origin,
			int horizontalSpread, int verticalSpread, AbocipherEmitterBlockEntity.Profile profile) {
		for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
			int dx = attempt == 0 ? 0 : random.nextInt(horizontalSpread * 2 + 1) - horizontalSpread;
			int dy = attempt == 0 ? 0 : random.nextInt(verticalSpread * 2 + 1) - verticalSpread;
			int dz = attempt == 0 ? 0 : random.nextInt(horizontalSpread * 2 + 1) - horizontalSpread;
			BlockPos candidate = origin.offset(dx, dy, dz);
			if (!fullBox.isInside(candidate.getX(), candidate.getY(), candidate.getZ())) {
				continue;
			}
			if (!level.getBlockState(candidate).isAir()) {
				continue;
			}

			level.setBlock(candidate, BlockInit.abocipher_emitter.get().defaultBlockState(), Block.UPDATE_CLIENTS);
			if (level.getBlockEntity(candidate) instanceof AbocipherEmitterBlockEntity emitter) {
				emitter.configure(profile, random.nextLong());
			}
			return true;
		}
		return false;
	}
}
