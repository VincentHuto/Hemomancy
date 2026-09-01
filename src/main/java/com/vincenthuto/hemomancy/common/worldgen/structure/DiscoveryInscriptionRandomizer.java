package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.inscription.DiscoveryInscriptionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class DiscoveryInscriptionRandomizer {
	private DiscoveryInscriptionRandomizer() {
	}

	static void replacePlaceholderIds(WorldGenLevel level, BoundingBox box, RandomSource random,
			ResourceLocation placeholderId, List<ResourceLocation> replacements) {
		if (replacements.isEmpty()) {
			return;
		}

		List<BlockPos> placeholderPositions = new ArrayList<>();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		for (int x = box.minX(); x <= box.maxX(); x++) {
			for (int y = box.minY(); y <= box.maxY(); y++) {
				for (int z = box.minZ(); z <= box.maxZ(); z++) {
					mutable.set(x, y, z);
					if (!level.getBlockState(mutable).is(BlockInit.blood_echo_inscription.get())
							&& !level.getBlockState(mutable).is(BlockInit.rite_fragment_inscription.get())) {
						continue;
					}
					if (!(level.getBlockEntity(mutable) instanceof DiscoveryInscriptionBlockEntity inscription)) {
						continue;
					}
					if (placeholderId.equals(inscription.getInscriptionId())) {
						placeholderPositions.add(mutable.immutable());
					}
				}
			}
		}

		if (placeholderPositions.isEmpty()) {
			return;
		}

		List<ResourceLocation> shuffled = new ArrayList<>(replacements);
		Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));

		for (int i = 0; i < placeholderPositions.size(); i++) {
			ResourceLocation chosen = shuffled.get(i % shuffled.size());
			BlockPos pos = placeholderPositions.get(i);
			if (level.getBlockEntity(pos) instanceof DiscoveryInscriptionBlockEntity inscription) {
				inscription.setInscriptionId(chosen);
			}
		}
	}
}

