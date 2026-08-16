package com.vincenthuto.hemomancy.common.item.harbinger;

public enum BloodSamplingResult {
	SUCCESS("success"),
	ALREADY_FILLED("already_filled"),
	INVALID_TARGET("invalid_target"),
	INSUFFICIENT_CONDITION("insufficient_condition"),
	FAILED("failed");

	private final String translationKey;

	BloodSamplingResult(String key) {
		this.translationKey = "message.hemomancy.blood_sampling." + key;
	}

	public String translationKey() {
		return translationKey;
	}
}
