package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.functional.AbocipherEmitterBlockEntity;
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

	public static void placeHarbingerOutpostEmitters(WorldGenLevel level, BoundingBox fullBox, RandomSource random,
			int centerX, int centerZ, int minY, int maxY) {
		int quarterX = Math.max(2, (fullBox.maxX() - fullBox.minX()) / 4);
		int quarterZ = Math.max(2, (fullBox.maxZ() - fullBox.minZ()) / 4);
		int lowerY = AbocipherEmitterPlacementRules.harbingerOutpostEmitterY(minY, maxY, 0);
		int middleY = AbocipherEmitterPlacementRules.harbingerOutpostEmitterY(minY, maxY, 1);
		int upperY = AbocipherEmitterPlacementRules.harbingerOutpostEmitterY(minY, maxY, 2);

		placeNear(level, fullBox, random, new BlockPos(centerX, lowerY, centerZ), 4, 2,
				AbocipherEmitterBlockEntity.Profile.HARBINGER_OUTPOST);
		placeNear(level, fullBox, random, new BlockPos(centerX - quarterX, middleY, centerZ + quarterZ), 3, 2,
				AbocipherEmitterBlockEntity.Profile.HARBINGER_OUTPOST);
		placeNear(level, fullBox, random, new BlockPos(centerX + quarterX, upperY, centerZ - quarterZ), 3, 2,
				AbocipherEmitterBlockEntity.Profile.HARBINGER_OUTPOST);
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
