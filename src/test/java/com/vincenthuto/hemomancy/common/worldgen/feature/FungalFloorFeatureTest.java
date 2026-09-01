package com.vincenthuto.hemomancy.common.worldgen.feature;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FungalFloorFeatureTest {
	@Test
	void everyColumnGetsOneToThreeBlocksAndAllSurfaceMaterialsAppear() {
		Set<Integer> heights = new HashSet<>();
		Set<Integer> materials = new HashSet<>();
		long seed = 0x5EEDL;

		for (int x = -64; x < 64; x++) {
			for (int z = -64; z < 64; z++) {
				int height = FungalFloorPattern.heightAt(seed, x, z);
				heights.add(height);
				materials.add(FungalFloorPattern.surfaceAt(seed, x, z));
				assertEquals(height, FungalFloorPattern.heightAt(seed, x, z));
			}
		}

		assertEquals(Set.of(1, 2, 3), heights);
		assertEquals(Set.of(0, 1, 2), materials);
	}

	@Test
	void materialRegionsKeepSolidCentersButDitherTheirEdges() {
		long seed = 0x5EEDL;
		int ditheredCells = 0;

		for (int cellX = -8; cellX < 8; cellX++) {
			for (int cellZ = -8; cellZ < 8; cellZ++) {
				Set<Integer> center = new HashSet<>();
				Set<Integer> edge = new HashSet<>();
				for (int localX = 0; localX < 8; localX++) {
					for (int localZ = 0; localZ < 8; localZ++) {
						int material = FungalFloorPattern.surfaceAt(seed,
								cellX * 8 + localX, cellZ * 8 + localZ);
						if (localX >= 2 && localX <= 5 && localZ >= 2 && localZ <= 5) center.add(material);
						else edge.add(material);
					}
				}
				assertEquals(1, center.size(), "Material noise leaked into a region interior");
				if (edge.size() > 1) ditheredCells++;
			}
		}

		assertEquals(true, ditheredCells > 64, "Too few region edges blend into their neighbors");
	}
}
