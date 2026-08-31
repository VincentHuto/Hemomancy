package com.vincenthuto.hemomancy.common.manipulation;

public enum EnumManipulationType {
	/** Fires once when the manipulation key is pressed. */
	QUICK,
	/** Accumulates strength, fires when full, repeats while held, and fires partial charge on release. */
	CHARGED,
	/** Toggles criteria-driven effects on or off when the key is pressed. */
	PASSIVE,
	/** Starts on press, pulses at constant strength while held, and stops on release. */
	CONTINUOUS
}
