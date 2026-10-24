package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import net.minecraft.network.chat.Component;

import java.util.Locale;

/** Compact labels for the fixed-width Family filter card. */
public final class FamilyFilterLabels {
	private static final String FAMILY_ALL = "screen.hemomancy.family_filter_labels.family_all";

	private FamilyFilterLabels() {
	}

	public static String display(String nickname) {
		return nickname == null || nickname.isBlank() ? Component.translatable(FAMILY_ALL).getString() : nickname;
	}

	public static Component label(Component nickname) {
		return nickname == null ? Component.translatable(FAMILY_ALL) : nickname;
	}

	public static Component nickname(String family) {
		if (family == null || family.isBlank()) return null;
		String key = switch (family) {
			case "living_staff" -> "screen.hemomancy.family_filter_labels.staff";
			case "Bloodline/Fane" -> "screen.hemomancy.family_filter_labels.bloodline";
			case "Body/Will" -> "screen.hemomancy.family_filter_labels.body";
			case "Domain/World" -> "screen.hemomancy.family_filter_labels.domain";
			case "Qliphoth/Forbidden" -> "screen.hemomancy.family_filter_labels.qliphoth";
			case "Ritual Floors" -> "screen.hemomancy.family_filter_labels.floors";
			case "Ichorian Sigils" -> "screen.hemomancy.family_filter_labels.sigils";
			case "Ritual Infrastructure" -> "screen.hemomancy.family_filter_labels.ritual";
			case "Constructs/Effigies" -> "screen.hemomancy.family_filter_labels.constructs";
			case "Miscellaneous" -> "screen.hemomancy.family_filter_labels.misc";
			default -> null;
		};
		return key != null ? Component.translatable(key)
				: Component.translatableWithFallback(familyKey(family), properCase(family.replace('_', ' ')));
	}

	/** The translation key of a recipe-map or skill family's full name. */
	public static String familyKey(String family) {
		String slug = family.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
		return "screen.hemomancy.recipe_map_family." + slug;
	}

	private static String properCase(String value) {
		StringBuilder result = new StringBuilder(value.length());
		boolean capitalize = true;
		for (int i = 0; i < value.length(); i++) {
			char character = value.charAt(i);
			result.append(capitalize ? Character.toUpperCase(character) : Character.toLowerCase(character));
			capitalize = Character.isWhitespace(character);
		}
		return result.toString();
	}
}
