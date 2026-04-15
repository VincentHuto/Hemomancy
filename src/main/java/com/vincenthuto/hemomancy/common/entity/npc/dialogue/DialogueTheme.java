package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

/**
 * Visual theme applied to the dialogue screen. Each theme produces a
 * distinct colour palette and animated background that matches the
 * speaking faction's aesthetic.
 * <ul>
 *   <li>{@link #BLOOD} – Red/crimson veiny appearance (Harbinger Hermit, default).</li>
 *   <li>{@link #UNSTAINED} – Blue/white with floating hollow rhombuses (Zealots, Unstained faction).</li>
 *   <li>{@link #FUNGAL} – Orange/amber with floating spores and tendrils (Fungal Whispers).</li>
 * </ul>
 */
public enum DialogueTheme {
	BLOOD,
	UNSTAINED,
	FUNGAL;

	private static final DialogueTheme[] VALUES = values();

	public static DialogueTheme fromOrdinal(int ordinal) {
		return (ordinal >= 0 && ordinal < VALUES.length) ? VALUES[ordinal] : BLOOD;
	}
}
