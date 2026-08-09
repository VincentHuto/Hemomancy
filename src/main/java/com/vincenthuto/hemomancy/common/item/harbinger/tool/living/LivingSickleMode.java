package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public enum LivingSickleMode {
	SHORT_REAP,
	BLOOD_HOOK;

	public static LivingSickleMode defaultMode() {
		return SHORT_REAP;
	}

	public LivingSickleMode next() {
		return this == SHORT_REAP ? BLOOD_HOOK : SHORT_REAP;
	}

	public static LivingSickleMode byName(String name) {
		if (name != null) {
			for (LivingSickleMode mode : values()) {
				if (mode.name().equals(name)) return mode;
			}
		}
		return defaultMode();
	}
}
