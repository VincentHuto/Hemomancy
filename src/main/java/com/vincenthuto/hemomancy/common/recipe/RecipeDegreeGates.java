package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

/**
 * Shared degree/stage gates for recipe-driven Hemomancy progression.
 * Blood cost and rite size remain costs/shape data only; progression access
 * comes from each recipe's explicit required degree.
 */
public final class RecipeDegreeGates {
	public static final int MIN_LEVEL = 0;
	public static final int MAX_LEVEL = 8;
	public static final int[] LEVELS = { 0, 1, 2, 3, 4, 5, 6, 7, 8 };

	private static final Map<String, Integer> RANKUP_RITE_TARGET_DEGREES = Map.ofEntries(
			Map.entry("cardinal_rite/sanguine_initiation", 1),
			Map.entry("cardinal_rite/votary_rite", 2),
			Map.entry("cardinal_rite/initiate_rite", 3),
			Map.entry("cardinal_rite/sanguine_brotherhood", 4),
			Map.entry("cardinal_rite/illuminatus_rite", 5),
			Map.entry("cardinal_rite/sanctified_rite", 6),
			Map.entry("cardinal_rite/archon_rite", 7),
			Map.entry("cardinal_rite/apotheos_rite", 8));

	private RecipeDegreeGates() {}

	public static int clampLevel(int level) {
		return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
	}

	public static int getRequiredDegree(BloodStructureRecipe recipe) {
		return recipe == null ? MIN_LEVEL : clampLevel(recipe.getRequiredDegree());
	}

	public static int getRequiredDegree(CardinalRiteRecipe recipe) {
		return recipe == null ? MIN_LEVEL : clampLevel(recipe.getRequiredDegree());
	}

	public static int getPlayerLevel(Player player, boolean unstained) {
		if (unstained) {
			return clampLevel(HemoCapabilityAccess.getPlayerUnstainedLevel(player));
		}
		return clampLevel(HemoCapabilityAccess.getPlayerDegreeNumber(player));
	}

	public static boolean playerMeets(Player player, BloodStructureRecipe recipe) {
		return getPlayerLevel(player, recipe.isUnstained()) >= getRequiredDegree(recipe);
	}

	public static boolean playerMeets(Player player, CardinalRiteRecipe recipe) {
		return getPlayerLevel(player, recipe.isUnstained()) >= getRequiredDegree(recipe);
	}

	public static Integer getRankupTargetDegree(ResourceLocation riteId) {
		return riteId == null ? null : RANKUP_RITE_TARGET_DEGREES.get(riteId.getPath());
	}

	public static String degreeLabel(int degree) {
		int clamped = clampLevel(degree);
		if (clamped == 0) {
			return "No Degree";
		}
		EnumInitiatoryDegree named = EnumInitiatoryDegree.byNumber(clamped);
		return named != null ? "Degree " + clamped + " - " + named.getTitle() : "Degree " + clamped;
	}

	public static String unstainedStageLabel(int level) {
		return switch (clampLevel(level)) {
			case 0 -> "Unbegun";
			case 1 -> "Begun";
			case 2 -> "Tainted";
			case 3 -> "Cleansing";
			case 4 -> "Absolved";
			case 5 -> "Purified";
			case 6 -> "Discerning";
			case 7 -> "Vigilant";
			case 8 -> "Enlightened";
			default -> "Unknown";
		};
	}
}
