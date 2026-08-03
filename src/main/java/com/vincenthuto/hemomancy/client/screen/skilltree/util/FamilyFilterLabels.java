package com.vincenthuto.hemomancy.client.screen.skilltree.util;

/** Compact labels for the fixed-width Family filter card. */
public final class FamilyFilterLabels {
	private FamilyFilterLabels() {
	}

	public static String display(String nickname) {
		return nickname == null || nickname.isBlank() ? "Family: All" : nickname;
	}

	public static String nickname(String family) {
		if (family == null || family.isBlank()) return null;
		return switch (family) {
			case "living_staff" -> "Staff";
			case "Bloodline/Fane" -> "Bloodline";
			case "Body/Will" -> "Body";
			case "Domain/World" -> "Domain";
			case "Qliphoth/Forbidden" -> "Qliphoth";
			case "Ritual Floors" -> "Floors";
			case "Ichorian Sigils" -> "Sigils";
			case "Ritual Infrastructure" -> "Ritual";
			case "Constructs/Effigies" -> "Constructs";
			case "Miscellaneous" -> "Misc";
			default -> properCase(family.replace('_', ' '));
		};
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
