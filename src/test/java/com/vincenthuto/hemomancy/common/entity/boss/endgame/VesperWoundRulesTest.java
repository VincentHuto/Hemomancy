package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperWoundRulesTest {
	private static VesperWoundRules.Wound fresh() {
		return VesperWoundRules.open(VesperWoundRules.Site.CARAPACE);
	}

	@Test
	void absorptionDrainsToBreak() {
		VesperWoundRules.Wound wound = fresh();
		for (int i = 0; i < 99; i++) wound = VesperWoundRules.drain(wound, 1.0F);
		assertFalse(VesperWoundRules.broken(wound));
		wound = VesperWoundRules.drain(wound, 1.0F);
		assertTrue(VesperWoundRules.broken(wound));
		assertEquals(VesperWoundRules.DRAIN_REQUIRED, VesperWoundRules.drain(wound, 50.0F).drained(), 1.0E-5F);
	}

	@Test
	void hitsKeepOpenAndDrainOnlyAtFallbackRate() {
		VesperWoundRules.Wound wound = fresh();
		for (int i = 0; i < VesperWoundRules.SCAB_DELAY - 1; i++) wound = VesperWoundRules.tick(wound);
		wound = VesperWoundRules.hit(wound, 20.0F);
		assertEquals(0, wound.idleTicks(), "a hit keeps the wound from scabbing");
		assertEquals(20.0F * VesperWoundRules.HIT_FALLBACK_RATIO, wound.drained(), 1.0E-5F);
	}

	@Test
	void scabsAfterDelayButNeverBelowFloor() {
		VesperWoundRules.Wound wound = VesperWoundRules.drain(fresh(), 60.0F);
		for (int i = 0; i < VesperWoundRules.SCAB_DELAY; i++) wound = VesperWoundRules.tick(wound);
		assertEquals(60.0F, wound.drained(), 1.0E-5F, "no scabbing during the grace delay");
		for (int i = 0; i < 2000; i++) wound = VesperWoundRules.tick(wound);
		assertEquals(VesperWoundRules.scabFloor(60.0F), wound.drained(), 1.0E-5F);
		assertEquals(30.0F, VesperWoundRules.scabFloor(60.0F), 1.0E-5F);
		assertFalse(VesperWoundRules.broken(wound));
	}

	@Test
	void puppetTearDrainsChunk() {
		VesperWoundRules.Wound wound = VesperWoundRules.tear(fresh());
		assertEquals(VesperWoundRules.PUPPET_TEAR, wound.drained(), 1.0E-5F);
		assertEquals(0, wound.idleTicks());
	}

	@Test
	void legacyAnchorLoadsAsOpenWoundWithScaledProgress() {
		CompoundTag legacy = new CompoundTag();
		legacy.putInt("ActiveThroneAnchor", 1);
		legacy.putFloat("ActiveAnchorDamage", 20.0F);
		VesperWoundRules.Wound wound = VesperWoundRules.load(legacy);
		assertNotNull(wound);
		assertEquals(VesperWoundRules.Site.NECK, wound.site());
		assertEquals(50.0F, wound.drained(), 1.0E-5F);

		CompoundTag none = new CompoundTag();
		none.putInt("ActiveThroneAnchor", -1);
		assertNull(VesperWoundRules.load(none));
	}

	@Test
	void woundRoundTripsThroughNbt() {
		VesperWoundRules.Wound wound = new VesperWoundRules.Wound(VesperWoundRules.Site.TAIL, 42.5F, 13, 55.0F);
		CompoundTag tag = new CompoundTag();
		VesperWoundRules.save(wound, tag);
		assertEquals(wound, VesperWoundRules.load(tag));
	}

	@Test
	void siteIndexMatchesThresholdOrder() {
		assertEquals(VesperWoundRules.Site.CARAPACE, VesperWoundRules.Site.fromIndex(0));
		assertEquals(VesperWoundRules.Site.NECK, VesperWoundRules.Site.fromIndex(1));
		assertEquals(VesperWoundRules.Site.TAIL, VesperWoundRules.Site.fromIndex(2));
	}

	@Test
	void socketsSitAtThreeDistinctPlacesOnTheMount() {
		VesperWoundRules.Socket carapace = VesperWoundRules.socket(VesperWoundRules.Site.CARAPACE);
		VesperWoundRules.Socket neck = VesperWoundRules.socket(VesperWoundRules.Site.NECK);
		VesperWoundRules.Socket tail = VesperWoundRules.socket(VesperWoundRules.Site.TAIL);
		assertTrue(neck.forward() > 2.0D, "the neck wound is out front");
		assertTrue(tail.forward() < -1.0D, "the tail wound is at the rear");
		assertTrue(carapace.up() > neck.up() && carapace.up() > tail.up(), "the carapace wound is on top");
	}

	@Test
	void socketOffsetRotatesWithTheMountsYaw() {
		VesperWoundRules.Socket neck = VesperWoundRules.socket(VesperWoundRules.Site.NECK);
		double[] facingSouth = VesperWoundRules.worldOffset(neck, 0.0F);
		assertEquals(neck.forward(), facingSouth[2], 1.0E-6D);
		double[] facingWest = VesperWoundRules.worldOffset(neck, 90.0F);
		assertEquals(-neck.forward(), facingWest[0], 1.0E-6D);
		assertEquals(neck.up(), facingWest[1], 1.0E-6D);
	}

	@Test
	void guardTurnsTheWoundAwayFromADrainerStandingAtIt() {
		assertEquals(VesperWoundRules.Guard.TURN_GUARD, VesperWoundRules.guard(VesperWoundRules.Site.NECK, 10.0D));
		assertEquals(VesperWoundRules.Guard.TURN_GUARD, VesperWoundRules.guard(VesperWoundRules.Site.TAIL, 170.0D));
		assertEquals(VesperWoundRules.Guard.BITE, VesperWoundRules.guard(VesperWoundRules.Site.CARAPACE, 0.0D));
		assertEquals(VesperWoundRules.Guard.TAIL_SWEEP, VesperWoundRules.guard(VesperWoundRules.Site.CARAPACE, -175.0D));
		assertEquals(VesperWoundRules.Guard.SHAKE_OFF, VesperWoundRules.guard(VesperWoundRules.Site.NECK, 90.0D));
		for (VesperWoundRules.Site site : VesperWoundRules.Site.values()) {
			for (double angle = -180.0D; angle <= 180.0D; angle += 15.0D) {
				assertNotNull(VesperWoundRules.guard(site, angle));
			}
		}
	}
}
