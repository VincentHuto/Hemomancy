package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
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
			String tier = floorTier(entry);
			return new Content(entry.displayName(), entry.description(),
					kind + (tier == null ? "" : "  |  " + tier) + "  |  " + entry.family());
		}
		if (entry.key().kind() == RecipeMapEntry.Kind.SIGIL) {
			return new Content(entry.displayName(), entry.description(), kind + "  |  " + entry.family());
		}
		return new Content(entry.displayName(), entry.description(),
				kind + "  |  Degree " + entry.column() + "  |  " + entry.family());
	}

	/** Floors and sigils are ringed by the first rite that uses them, not by a gate of their own. */
	public static boolean showsFirstUse(RecipeMapEntry entry) {
		return entry.key().kind() == RecipeMapEntry.Kind.FLOOR || entry.key().kind() == RecipeMapEntry.Kind.SIGIL;
	}

	public static String gateLine(RecipeMapEntry entry) {
		return (showsFirstUse(entry) ? "First used at Degree " : "Requires Degree ") + entry.column();
	}

	private static String floorTier(RecipeMapEntry entry) {
		String path = entry.id().getPath();
		for (CardinalRiteType tier : CardinalRiteType.values()) {
			String name = tier.getSerializedName();
			if (path.endsWith("_" + name)) return Character.toUpperCase(name.charAt(0)) + name.substring(1);
		}
		return null;
	}

	public static int maxWidth(int guiWidth) {
		return Mth.clamp(guiWidth / 3, 140, 220);
	}

	public static String visibleDescription(RecipeMapEntry entry, boolean shiftDown) {
		if (entry.key().kind() == RecipeMapEntry.Kind.RITE && !shiftDown) return "";
		return entry.description();
	}

	public static int blueprintCueSpacerLines() {
		return 1;
	}

	public static int blueprintCueReservedWidth() {
		return 20;
	}

	public record Content(String title, String description, String context) {}
}
