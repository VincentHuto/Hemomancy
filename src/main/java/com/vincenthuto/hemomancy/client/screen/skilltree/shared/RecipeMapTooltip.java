package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class RecipeMapTooltip {
	private RecipeMapTooltip() {}

	public static Content content(RecipeMapEntry entry) {
		Component kind = Component.translatable(switch (entry.key().kind()) {
			case RITE -> "screen.hemomancy.recipe_map_tooltip.rite";
			case CRAFTING -> "screen.hemomancy.recipe_map_tooltip.crafting";
			case SIGIL -> "screen.hemomancy.recipe_map_tooltip.sigil";
			case FLOOR -> "screen.hemomancy.recipe_map_tooltip.ritual_floor";
		});
		Component family = HarbingerRecipeMapDefinitions.familyName(entry.family());
		if (entry.key().kind() == RecipeMapEntry.Kind.FLOOR) {
			String tier = floorTier(entry);
			return new Content(entry.displayName(), entry.description(), tier == null
					? Component.translatable("screen.hemomancy.recipe_map_tooltip.context_pair", kind, family)
					: Component.translatable("screen.hemomancy.recipe_map_tooltip.context_triple", kind, tier, family));
		}
		if (entry.key().kind() == RecipeMapEntry.Kind.SIGIL) {
			return new Content(entry.displayName(), entry.description(),
					Component.translatable("screen.hemomancy.recipe_map_tooltip.context_pair", kind, family));
		}
		return new Content(entry.displayName(), entry.description(),
				Component.translatable("screen.hemomancy.recipe_map_tooltip.context_triple", kind,
						Component.translatable("message.hemomancy.recipe_degree_gates.degree", entry.column()), family));
	}

	/** Floors and sigils are ringed by the first rite that uses them, not by a gate of their own. */
	public static boolean showsFirstUse(RecipeMapEntry entry) {
		return entry.key().kind() == RecipeMapEntry.Kind.FLOOR || entry.key().kind() == RecipeMapEntry.Kind.SIGIL;
	}

	public static Component gateLine(RecipeMapEntry entry) {
		return Component.translatable(showsFirstUse(entry) ? "screen.hemomancy.recipe_map_tooltip.first_used_at_degree"
				: "screen.hemomancy.recipe_map_tooltip.requires_degree", entry.column());
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

	public record Content(String title, String description, Component context) {}
}
