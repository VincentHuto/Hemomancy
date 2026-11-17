package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GuyLineRulesTest {
	/** A wall of canvas along z = 12 from y 1 to 11 for x 10..20, like the pavilion's long north wall. */
	private static Set<BlockPos> wall() {
		Set<BlockPos> canvas = new HashSet<>();
		for (int x = 10; x <= 20; x++)
			for (int y = 1; y <= 11; y++) canvas.add(new BlockPos(x, y, 12));
		return canvas;
	}

	@Test
	void theLineRunsToTheTopOfTheWallItFaces() {
		Set<BlockPos> canvas = wall();
		var anchor = GuyLineRules.anchor(new BlockPos(14, 1, 8), Direction.SOUTH, canvas::contains);
		assertTrue(anchor.isPresent());
		assertEquals(new Vec3(14.5D, 12.0D, 12.0D), anchor.get());
	}

	@Test
	void aStakeFacingAwayFromTheTentHasNoLine() {
		Set<BlockPos> canvas = wall();
		assertTrue(GuyLineRules.anchor(new BlockPos(14, 1, 8), Direction.NORTH, canvas::contains).isEmpty());
	}

	@Test
	void aCornerStakeFindsTheWallBesideItsLine() {
		Set<BlockPos> canvas = wall();
		var anchor = GuyLineRules.anchor(new BlockPos(8, 1, 9), Direction.SOUTH, canvas::contains);
		assertTrue(anchor.isPresent());
		assertEquals(10.5D, anchor.get().x, 1.0E-9D);
	}

	@Test
	void aWallBeyondReachIsNotTaken() {
		Set<BlockPos> canvas = wall();
		assertTrue(GuyLineRules.anchor(new BlockPos(14, 1, -10), Direction.SOUTH, canvas::contains).isEmpty());
	}

	@Test
	void theKnotLeansTowardTheTent() {
		Vec3 knot = GuyLineRules.knot(new BlockPos(0, 0, 0), Direction.SOUTH);
		assertTrue(knot.z > 0.5D && knot.y > 0.5D, "knot " + knot);
	}
}
