package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;

/**
 * The Ringmaster's finale in his Chamber (2026-11-18), as pure rules: its phases, how far his mind has swallowed the
 * tent at each point of the fight, the kneel that catches the blow that would kill him, and the charge his bats bring
 * him. Every number is the builder's starting value for the owner to tune.
 */
public final class FinaleRules {
	/** CROSSING: under his opening scene, the copy passing for the real tent. RAFTERS to 50%, REVEAL, DESCENT, KNEEL. */
	public enum FinalePhase { CROSSING, RAFTERS, REVEAL, DESCENT, KNEEL, ENDED }

	/** How the player ends it at his kneel: a kill frees the troupe (Liberation), absorbing him takes his place. */
	public enum Ending { KILLED, ABSORBED }

	public static final int INTRO_TICKS = 60, REVEAL_TICKS = 50, KNEEL_SETTLE_TICKS = 30;
	/** He comes down from the rafters at this fraction of his health. */
	public static final float DESCENT_AT = 0.5F;
	/**
	 * The merge (owner, 2026-11-18 third pass): his Chamber's material is painted onto the tent's own block faces outside
	 * the ring. It rises over the opening to {@link #VEIL_OPEN}, nearly opaque, then thins as he is worn down: to
	 * {@link #VEIL_HALF} as he comes down at half health and {@link #VEIL_LAST} at the end, so the tent's stripes show
	 * through more and more. The kneel holds {@link #VEIL_LAST}.
	 */
	public static final float VEIL_OPEN = 0.95F, VEIL_HALF = 0.55F, VEIL_LAST = 0.25F;
	public static final int VEIL_OPENING_TICKS = 100;
	/** The ring that stays real under the veil: a column this wide and tall round the carousel's cell. */
	public static final float RING_RADIUS = 9.0F, RING_HEIGHT = 9.0F;
	public static final int CHARGE_MAX = 100, CHARGE_PER_SIP = 20, EMPOWERED_COST = 50;

	private FinaleRules() {
	}

	/** The phase after this tick. KNEEL and ENDED are entered by the kneel and the end, never by time or health. */
	public static FinalePhase next(FinalePhase phase, int phaseTicks, float healthFraction) {
		return switch (phase) {
			case CROSSING -> phaseTicks >= INTRO_TICKS ? FinalePhase.RAFTERS : phase;
			case RAFTERS -> healthFraction <= DESCENT_AT ? FinalePhase.REVEAL : phase;
			case REVEAL -> phaseTicks >= REVEAL_TICKS ? FinalePhase.DESCENT : phase;
			default -> phase;
		};
	}

	/** How strongly his Chamber covers the tent's faces this tick: 0 while the copy passes for the real tent. */
	public static float veilStrength(FinalePhase phase, int phaseTicks, float healthFraction) {
		float health = Mth.clamp(healthFraction, 0.0F, 1.0F);
		return switch (phase) {
			case CROSSING, ENDED -> 0.0F;
			case RAFTERS -> {
				float pushed = Mth.clamp((1.0F - health) / (1.0F - DESCENT_AT), 0.0F, 1.0F);
				float opening = HemoEasing.smoothstep(Mth.clamp(phaseTicks / (float) VEIL_OPENING_TICKS, 0.0F, 1.0F));
				yield opening * HemoEasing.lerp(VEIL_OPEN, VEIL_HALF, pushed);
			}
			case REVEAL, DESCENT -> HemoEasing.lerp(VEIL_LAST, VEIL_HALF, Mth.clamp(health / DESCENT_AT, 0.0F, 1.0F));
			case KNEEL -> VEIL_LAST;
		};
	}

	/** A blow that would kill him while he fights brings him to his knees instead. */
	public static boolean kneelCatches(FinalePhase phase, float damage, float health) {
		return (phase == FinalePhase.RAFTERS || phase == FinalePhase.REVEAL || phase == FinalePhase.DESCENT)
				&& damage >= health;
	}

	/** Only the owner's own blow, once the kneel has settled, kills him. */
	public static boolean mayKill(FinalePhase phase, int phaseTicks, boolean ownerBlow) {
		return phase == FinalePhase.KNEEL && phaseTicks >= KNEEL_SETTLE_TICKS && ownerBlow;
	}

	/**
	 * Whether a blow at the kneel is the owner's own: caused and dealt by them (melee or their own direct cast). Their
	 * arrow in flight, hound or bound body is not, so it can never end the kneel over a player drawing his blood off.
	 */
	public static boolean ownBlow(boolean entityIsOwner, boolean directIsOwner) {
		return entityIsOwner && directIsOwner;
	}

	/** His charge after {@code sips} more of stolen blood reach him. */
	public static int charge(int charge, int sips) {
		return Math.min(CHARGE_MAX, charge + sips * CHARGE_PER_SIP);
	}

	public static boolean mayEmpower(int charge) {
		return charge >= EMPOWERED_COST;
	}
}
