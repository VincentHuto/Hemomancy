package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

/**
 * How one lash manipulation leans between its two halves: the quick tap on the attack button, and the charged whip on a
 * held Projection. Data on the registration, beside the {@link StrandSpec}.
 *
 * @param tapDamage           a tap's damage before the spec's scale
 * @param tapBlood            a tap's blood, before the cost ledger
 * @param tapCooldown         ticks between taps
 * @param tapStateTicks       how long a tap lays the school's state; 0 for a tap that only wounds
 * @param chargeBloodPerPulse blood a pulse costs while the orb gathers
 * @param chargeTicks         ticks of holding for a full charge
 * @param chargedMultiplier   what a full charge multiplies the first crack by
 * @param style               the lash's rope and its signature
 */
public record LashProfile(float tapDamage, double tapBlood, int tapCooldown, int tapStateTicks,
		double chargeBloodPerPulse, int chargeTicks, float chargedMultiplier, LashStyle style) {
	/** A plain lash: one tip, the shared rope, nothing of its own. */
	public LashProfile(float tapDamage, double tapBlood, int tapCooldown, int tapStateTicks, double chargeBloodPerPulse,
			int chargeTicks, float chargedMultiplier) {
		this(tapDamage, tapBlood, tapCooldown, tapStateTicks, chargeBloodPerPulse, chargeTicks, chargedMultiplier, LashStyle.PLAIN);
	}

	public LashProfile {
		if (style == null) style = LashStyle.PLAIN;
		tapDamage = Math.max(0.0F, tapDamage);
		tapBlood = Math.max(0.0D, tapBlood);
		tapCooldown = Math.max(1, tapCooldown);
		tapStateTicks = Math.max(0, tapStateTicks);
		chargeBloodPerPulse = Math.max(0.0D, chargeBloodPerPulse);
		chargeTicks = Math.max(1, chargeTicks);
		chargedMultiplier = Math.max(1.0F, chargedMultiplier);
	}

	/** These numbers with {@code style} for the rope and signature. */
	public LashProfile with(LashStyle style) {
		return new LashProfile(tapDamage, tapBlood, tapCooldown, tapStateTicks, chargeBloodPerPulse, chargeTicks, chargedMultiplier, style);
	}

	/** What a crack carrying {@code charge} (0 to 1) is multiplied by. */
	public double chargeScale(double charge) {
		return 1.0D + (chargedMultiplier - 1.0D) * Math.max(0.0D, Math.min(1.0D, charge));
	}
}
