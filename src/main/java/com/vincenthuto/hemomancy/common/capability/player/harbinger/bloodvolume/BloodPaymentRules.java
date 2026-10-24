package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.ToggleableSkillRules;

/**
 * The arithmetic of one blood payment, with no Minecraft types, so every flag combination is unit-tested.
 * {@link BloodAccounting} supplies the player's volume and Sanguine Reserve state, then records and syncs the result.
 */
public final class BloodPaymentRules {
	/** Sanguine Reserve keeps this fraction of the maximum volume out of reach of protected payments. */
	public static final double RESERVE_FRACTION = 0.15D;
	static final double EPSILON = 0.000001D;

	public static final String DORMANT = "message.hemomancy.blood_flow.dormant";
	public static final String RESERVE_PROTECTED = "message.hemomancy.blood_flow.sanguine_reserve_protected";
	public static final String INSUFFICIENT = "message.hemomancy.blood_flow.insufficient_blood";
	public static final String EXHAUSTED = "message.hemomancy.blood_flow.blood_exhausted";

	private BloodPaymentRules() {
	}

	/**
	 * How a payment is settled. A one-off payment (the default) drains whatever was asked, so asking for more than the
	 * player holds empties the volume, as {@link IBloodVolume#drain} always has. Upkeep terms are the old ledger drain:
	 * a dormant volume pays nothing, and {@code requireFullAmount} refuses a payment it cannot make in full.
	 */
	public record Terms(boolean requireActive, boolean requireFullAmount) {
		public static final Terms ONE_OFF = new Terms(false, false);
	}

	/** {@code paid} is what actually left the volume; {@code condition} explains a shortfall to the diagnostics. */
	public record Outcome(double requested, double paid, boolean satisfied, String condition) {
	}

	public static Outcome settle(IBloodVolume volume, double amount, boolean reserveProtected, boolean reserveEnabled,
			boolean countsAsSpend, Terms terms) {
		double allowed = reserveProtected
				? ToggleableSkillRules.allowedBloodDrain(reserveEnabled, volume.getBloodVolume(),
						volume.getMaxBloodVolume() * RESERVE_FRACTION, amount)
				: amount;
		double paid = 0.0D;
		boolean satisfied = false;
		String condition = "";
		if (terms.requireActive() && !volume.isActive()) {
			condition = DORMANT;
		} else if (allowed <= 0.0D) {
			condition = RESERVE_PROTECTED;
		} else if (terms.requireFullAmount() && allowed + EPSILON < amount) {
			condition = RESERVE_PROTECTED;
		} else if (terms.requireFullAmount() && volume.getBloodVolume() < amount) {
			condition = INSUFFICIENT;
		} else {
			double before = volume.getBloodVolume();
			boolean drained = volume.drain(allowed);
			paid = Math.max(0.0D, before - volume.getBloodVolume());
			satisfied = drained && paid + EPSILON >= amount;
			if (!satisfied && paid + EPSILON < amount) {
				condition = allowed + EPSILON < amount ? RESERVE_PROTECTED : EXHAUSTED;
			}
		}
		if (countsAsSpend && satisfied) {
			volume.addBloodSpend(amount);
		}
		return new Outcome(amount, paid, satisfied, condition);
	}
}
