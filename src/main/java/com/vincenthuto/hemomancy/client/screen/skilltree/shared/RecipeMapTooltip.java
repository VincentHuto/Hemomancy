package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.util.Mth;

public final class RecipeMapTooltip {
	private RecipeMapTooltip() {}

	public static Content content(RecipeMapEntry entry) {
		String kind = switch (entry.key().kind()) {
			case RITE -> "Rite";
			case CRAFTING -> "Crafting";
			case SIGIL -> "Sigil";
			case FLOOR -> "Ritual Floor";
		};
		if (entry.key().kind() == RecipeMapEntry.Kind.FLOOR) {
			String tier = switch (entry.column()) {
				case 0 -> "Minor";
				case 1 -> "Lesser";
				case 2 -> "Greater";
				default -> "Grand";
			};
			return new Content(entry.displayName(), entry.description(),
					kind + "  |  " + tier + "  |  " + entry.family());
		}
		return new Content(entry.displayName(), entry.description(),
				kind + "  |  Degree " + entry.column() + "  |  " + entry.family());
	}

	public static int maxWidth(int guiWidth) {
		return Mth.clamp(guiWidth / 3, 140, 220);
	}

	public record Content(String title, String description, String context) {}
}
