package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

public final class BloodVolumeTransferRules {
	private BloodVolumeTransferRules() {
	}

	public static boolean fillFromSource(IBloodVolume target, IBloodVolume source, double requestedAmount) {
		double amount = transferableAmount(target, source, requestedAmount);
		if (amount <= 0.0D) {
			return false;
		}
		source.drain(amount);
		target.fill(amount);
		return true;
	}

	public static double transferableAmount(IBloodVolume target, IBloodVolume source, double requestedAmount) {
		if (target == null || source == null || requestedAmount <= 0.0D) {
			return 0.0D;
		}
		double sourceBlood = Math.max(0.0D, source.getBloodVolume());
		double targetRoom = Math.max(0.0D, target.getMaxBloodVolume() - target.getBloodVolume());
		return Math.min(requestedAmount, Math.min(sourceBlood, targetRoom));
	}

	/**
	 * How much of a client-requested donation the donor can actually give. Non-finite and non-positive requests give
	 * nothing: {@code Math.min(NaN, x)} is NaN, which would pass a {@code <= 0} check and drain the donor to NaN.
	 */
	public static double donationAmount(double requestedAmount, double donorBlood) {
		if (!Double.isFinite(requestedAmount) || requestedAmount <= 0.0D) {
			return 0.0D;
		}
		double amount = Math.min(requestedAmount, donorBlood);
		return amount > 0.0D ? amount : 0.0D;
	}
}
