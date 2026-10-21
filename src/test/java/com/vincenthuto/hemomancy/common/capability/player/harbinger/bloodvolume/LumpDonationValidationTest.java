package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** F-1: a lump donation must never drain the donor or fill the bloodline pool with a non-finite amount. */
class LumpDonationValidationTest {
	private static final double DONOR_BLOOD = 400.0D;

	@Test
	void nanDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(Double.NaN, DONOR_BLOOD));
	}

	@Test
	void infinityDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(Double.POSITIVE_INFINITY, DONOR_BLOOD));
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(Double.NEGATIVE_INFINITY, DONOR_BLOOD));
	}

	@Test
	void zeroDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(0.0D, DONOR_BLOOD));
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(-0.0D, DONOR_BLOOD));
	}

	@Test
	void negativeDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(-50.0D, DONOR_BLOOD));
	}

	@Test
	void validRequestIsCappedByTheDonorsBlood() {
		assertEquals(150.0D, BloodVolumeTransferRules.donationAmount(150.0D, DONOR_BLOOD));
		assertEquals(DONOR_BLOOD, BloodVolumeTransferRules.donationAmount(1000.0D, DONOR_BLOOD));
	}

	@Test
	void emptyOrCorruptDonorDonatesNothing() {
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(150.0D, 0.0D));
		assertEquals(0.0D, BloodVolumeTransferRules.donationAmount(150.0D, Double.NaN));
	}
}
