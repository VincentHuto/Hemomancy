package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;

/**
 * Builds a strand from its data: a new strand manipulation is a {@code Sculptings} factory calling one of these and a
 * {@code ManipulationInit} line, never a class of its own. Names no manipulation ({@code SculptingGuardrailTest}).
 */
public final class Strands {
	private Strands() {
	}

	public static VerbSculpting lash(double bloodPerPulse, float damageScale, LashProfile profile) {
		return of(new StrandSpec(StrandForm.LASH, bloodPerPulse, damageScale, StrandSpec.LONG_STATE, 25.0D, profile));
	}

	public static VerbSculpting jet(double bloodPerPulse, float damageScale) {
		return of(new StrandSpec(StrandForm.JET, bloodPerPulse, damageScale, StrandSpec.LONG_STATE, 30.0D, null));
	}

	public static VerbSculpting spray(double bloodPerPulse, float damageScale) {
		return of(new StrandSpec(StrandForm.SPRAY, bloodPerPulse, damageScale, StrandSpec.LONG_STATE, 30.0D, null));
	}

	public static VerbSculpting plant(double bloodPerPulse, float damageScale) {
		return of(new StrandSpec(StrandForm.PLANT, bloodPerPulse, damageScale, StrandSpec.LONG_STATE, 20.0D, null));
	}

	public static VerbSculpting brand(double bloodPerPulse, float damageScale) {
		return of(new StrandSpec(StrandForm.BRAND, bloodPerPulse, damageScale, StrandSpec.LONG_STATE, 40.0D, null));
	}

	public static VerbSculpting of(StrandSpec spec) {
		return new StrandSculpting(spec);
	}
}
