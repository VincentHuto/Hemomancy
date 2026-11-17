package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

/** The terrain gate samples the big top's 43-block core whichever way the widened pavilion is turned (2026-11-16). */
class PavilionGateCoreTest {
	@Test
	void theCoreIsTheSameSquareEitherWayRound() {
		BoundingBox eastWest = new BoundingBox(100, 60, 200, 180, 82, 264);   // 81 x 65, unturned
		BoundingBox northSouth = new BoundingBox(100, 60, 200, 164, 82, 280); // 65 x 81, a quarter turn
		for (BoundingBox laid : new BoundingBox[] {eastWest, northSouth}) {
			BoundingBox core = TroupeFootprintScan.gateCore(laid, 43);
			assertEquals(43, core.getXSpan());
			assertEquals(43, core.getZSpan());
			assertEquals(laid.getCenter().getX(), core.getCenter().getX());
			assertEquals(laid.getCenter().getZ(), core.getCenter().getZ());
		}
	}

	@Test
	void noCoreMeansTheWholeSite() {
		BoundingBox laid = new BoundingBox(0, 0, 0, 26, 11, 26);
		assertEquals(laid, TroupeFootprintScan.gateCore(laid, 0));
	}
}
