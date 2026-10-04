package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

public enum VialCentrifugeStartupResult {
	IDLE("idle"),
	SUCCESS("success"),
	ALREADY_RUNNING("already_running"),
	NO_PROCESSABLE_SAMPLES("no_processable_samples"),
	IMBALANCE("imbalance"),
	INVALID_SAMPLE("invalid_sample"),
	BLOCKED_ENZYME_OUTPUT("blocked_enzyme_output"),
	BLOCKED_VIAL_RETURN("blocked_vial_return"),
	BLOCKED_SECONDARY_OUTPUT("blocked_secondary_output"), // Retain ordinal for saved pre-shared-grid status.
	BLOCKED_POWDER_OUTPUT("blocked_powder_output"),
	RITE_LOCKED("rite_locked");

	private final String translationKey;

	VialCentrifugeStartupResult(String key) {
		this.translationKey = "message.hemomancy.vial_centrifuge." + key;
	}

	public String translationKey() {
		return translationKey;
	}

	public static VialCentrifugeStartupResult byId(int id) {
		VialCentrifugeStartupResult[] values = values();
		return id >= 0 && id < values.length ? values[id] : IDLE;
	}
}
