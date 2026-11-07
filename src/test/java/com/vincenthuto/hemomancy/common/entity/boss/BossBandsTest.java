package com.vincenthuto.hemomancy.common.entity.boss;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** One set of bands for every Saint (resolution B-1), sectors from the facing, and the facing angle. */
final class BossBandsTest {
	@Test
	void theStandardBandsAreTheResolvedOnes() {
		BossBands bands = BossBands.STANDARD;
		assertEquals(BossBands.Band.HUG, bands.band(2.99D));
		assertEquals(BossBands.Band.NEAR, bands.band(3.0D), "a lower bound belongs to the outer band");
		assertEquals(BossBands.Band.NEAR, bands.band(6.49D));
		assertEquals(BossBands.Band.MID, bands.band(6.5D));
		assertEquals(BossBands.Band.MID, bands.band(11.99D));
		assertEquals(BossBands.Band.FAR, bands.band(12.0D));
	}

	@Test
	void anotherThresholdSetReadsItsOwnBounds() {
		BossBands mycophant = new BossBands(3.5D, 7.0D, 13.0D);
		assertEquals(BossBands.Band.HUG, mycophant.band(3.2D));
		assertEquals(BossBands.Band.NEAR, mycophant.band(6.9D));
		assertEquals(BossBands.Band.MID, mycophant.band(12.9D));
		assertEquals(BossBands.Band.FAR, mycophant.band(13.0D));
	}

	@Test
	void sectorsFromTheFacing() {
		assertEquals(BossBands.Sector.FRONT, BossBands.sector(60.0D));
		assertEquals(BossBands.Sector.FLANK, BossBands.sector(60.1D));
		assertEquals(BossBands.Sector.FLANK, BossBands.sector(120.0D));
		assertEquals(BossBands.Sector.REAR, BossBands.sector(120.1D));
	}

	@Test
	void degreesFromFacingUsesMinecraftsForward() {
		// yaw 0 faces south (+z)
		assertEquals(0.0D, BossBands.degreesFromFacing(0, 0, 0.0F, 0, 5), 1.0E-9);
		assertEquals(90.0D, BossBands.degreesFromFacing(0, 0, 0.0F, 5, 0), 1.0E-9);
		assertEquals(180.0D, BossBands.degreesFromFacing(0, 0, 0.0F, 0, -5), 1.0E-9);
		// yaw 90 faces west (-x)
		assertEquals(0.0D, BossBands.degreesFromFacing(0, 0, 90.0F, -5, 0), 1.0E-9);
		assertEquals(0.0D, BossBands.degreesFromFacing(2, 2, 45.0F, 2, 2), "a point on the centre is ahead");
	}
}
