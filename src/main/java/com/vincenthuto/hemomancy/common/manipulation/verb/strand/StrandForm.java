package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;

import java.util.EnumSet;
import java.util.Set;

/**
 * The five shapes a strand takes. One manipulation has one form; how the player moves the strand decides what it does.
 * Each form sets the player's pace while it is out (CL-25) and how far it reaches against the verb's reach.
 */
public enum StrandForm {
	/** Swept side to side; rewards stroke speed and clean reversals ({@link LashRules}). */
	LASH(0.8D, 1.0D),
	/** Held on one target; pressure builds, then pierces ({@link JetRules}). */
	JET(0.5D, JetRules.REACH_SCALE),
	/** Fanned close; low damage, heavy school state ({@link SprayRules}). */
	SPRAY(1.0D, SprayRules.REACH_SCALE),
	/** Sunk into the ground; a delayed eruption sized by the blood sunk ({@link PlantRules}). */
	PLANT(0.6D, 1.0D),
	/** Held on a creature until it takes; marks it for Absorption ({@link BrandRules}). */
	BRAND(0.6D, 1.0D);

	/** The player's pace while the strand is out, against walking (1). Replaces vanilla's use-item slowdown. */
	public final double movement;
	/** How far the strand reaches against the verb's reach (the lash adds {@link LashRules#REACH_BONUS} instead). */
	public final double reachScale;

	StrandForm(double movement, double reachScale) {
		this.movement = movement;
		this.reachScale = reachScale;
	}

	/** The strand's length for a verb reach. */
	public double length(double reach) {
		return this == LASH ? LashRules.length(reach) : reach * reachScale;
	}

	/** What the pick looks for: a plant opens only on a block; every other form on air, a creature or a block. */
	public Set<VerbTargetKind> wants() {
		return this == PLANT ? EnumSet.of(VerbTargetKind.BLOCK)
				: EnumSet.of(VerbTargetKind.AIR, VerbTargetKind.CREATURE, VerbTargetKind.BLOCK);
	}
}
