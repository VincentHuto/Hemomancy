package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

/** Canonical endgame path state. Pending states record the choice before its rite completes. */
public enum EnumArchonPath {
	NONE,
	SILENT_PENDING,
	SILENT_ARCHON,
	APOTHEOS_PENDING,
	APOTHEOS;

	public static EnumArchonPath byName(String name) {
		if (name == null || name.isBlank()) return NONE;
		try {
			return valueOf(name);
		} catch (IllegalArgumentException ignored) {
			return NONE;
		}
	}
}
