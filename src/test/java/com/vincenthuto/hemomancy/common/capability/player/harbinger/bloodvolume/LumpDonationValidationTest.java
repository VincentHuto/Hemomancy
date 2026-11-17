package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * F-1: a lump donation must never drain the donor or fill the bloodline pool with a non-finite amount. PT-42: it
 * takes only what the pool has room for, and nothing from a full pool.
 */
class LumpDonationValidationTest {
	private static final double DONOR_BLOOD = 400.0D;
	private static final double POOL_ROOM = 10_000.0D;

	@Test
	void nanDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(Double.NaN, DONOR_BLOOD, POOL_ROOM));
	}

	@Test
	void infinityDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(Double.POSITIVE_INFINITY, DONOR_BLOOD, POOL_ROOM));
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(Double.NEGATIVE_INFINITY, DONOR_BLOOD, POOL_ROOM));
	}

	@Test
	void zeroDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(0.0D, DONOR_BLOOD, POOL_ROOM));
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(-0.0D, DONOR_BLOOD, POOL_ROOM));
	}

	@Test
	void negativeDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(-50.0D, DONOR_BLOOD, POOL_ROOM));
	}

	@Test
	void validRequestIsCappedByTheDonorsBlood() {
		assertEquals(150.0D, BloodVolumeTransferRules.donationAmount(150.0D, DONOR_BLOOD, POOL_ROOM));
		assertEquals(DONOR_BLOOD, BloodVolumeTransferRules.donationAmount(1000.0D, DONOR_BLOOD, POOL_ROOM));
	}

	@Test
	void emptyOrCorruptDonorDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(150.0D, 0.0D, POOL_ROOM));
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(150.0D, Double.NaN, POOL_ROOM));
	}

	@Test
	void validRequestIsCappedByThePoolsRoom() {
		assertEquals(120.0D, BloodVolumeTransferRules.donationAmount(150.0D, DONOR_BLOOD, 120.0D));
		assertEquals(150.0D, BloodVolumeTransferRules.donationAmount(150.0D, DONOR_BLOOD, 150.0D));
	}

	@Test
	void aFullPoolTakesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(150.0D, DONOR_BLOOD, 0.0D));
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(150.0D, DONOR_BLOOD, -25.0D));
	}
}
