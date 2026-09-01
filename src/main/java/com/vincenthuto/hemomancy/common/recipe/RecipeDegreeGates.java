package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
		return getPlayerLevel(player, recipe.isUnstained()) >= getRequiredDegree(recipe)
				&& (!(player instanceof ServerPlayer serverPlayer) || !isMnemonicReliquary(recipe)
						|| MnemonicReliquaryProgression.isTaught(serverPlayer))
				&& meetsUnstainedThresholds(player, recipe.isUnstained(), recipe.getRequiredPurity(), recipe.getRequiredClarity());
	}

	private static boolean isMnemonicReliquary(BloodStructureRecipe recipe) {
		return recipe != null && recipe.getId() != null
				&& "blood_structure/mnemonic_reliquary".equals(recipe.getId().getPath());
	}

	public static boolean playerMeets(Player player, CardinalRiteRecipe recipe) {
		return getPlayerLevel(player, recipe.isUnstained()) >= getRequiredDegree(recipe)
				&& meetsUnstainedThresholds(player, recipe.isUnstained(), recipe.getRequiredPurity(), recipe.getRequiredClarity());
	}

	/**
	 * Whether a rite should participate in station resolution for this player.
	 * Completed rank rites are deliberately excluded before floor/offering
	 * matching so an old promotion cannot make the next promotion ambiguous.
	 */
	public static boolean playerMayAttempt(Player player, CardinalRiteRecipe recipe) {
		if (!playerMeets(player, recipe)) return false;
		Integer target = recipe.isRankup() ? getRankupTargetDegree(recipe.getId()) : null;
		return target == null || rankupWindowOpen(getPlayerLevel(player, recipe.isUnstained()), target);
	}

	static boolean rankupWindowOpen(int playerLevel, int targetDegree) {
		return clampLevel(playerLevel) < clampLevel(targetDegree);
	}

	private static boolean meetsUnstainedThresholds(Player player, boolean unstained, float requiredPurity,
			float requiredClarity) {
		if (!unstained || (requiredPurity < 0 && requiredClarity < 0)) {
			return true;
		}
		return HemoCapabilityAccess.getUnstainedProgress(player)
				.map(progress -> (requiredPurity < 0 || progress.getPurity() >= requiredPurity)
						&& (requiredClarity < 0 || progress.hasClarityUnlocked()
								&& progress.getClarity() >= requiredClarity))
				.orElse(false);
	}

	public static String requirementLabel(BloodStructureRecipe recipe) {
		if (isMnemonicReliquary(recipe)) {
			return "the Mnemonist's Reliquary lesson at Degree 3";
		}
		return requirementLabel(recipe.isUnstained(), recipe.getRequiredDegree(), recipe.getRequiredPurity(),
				recipe.getRequiredClarity());
	}

	public static String requirementLabel(CardinalRiteRecipe recipe) {
		return requirementLabel(recipe.isUnstained(), recipe.getRequiredDegree(), recipe.getRequiredPurity(),
				recipe.getRequiredClarity());
	}

	private static String requirementLabel(boolean unstained, int requiredDegree, float requiredPurity,
			float requiredClarity) {
		if (unstained && requiredClarity >= 0) {
			return "Clarity " + formatThreshold(requiredClarity);
		}
		if (unstained && requiredPurity >= 0) {
			return "Purity " + formatThreshold(requiredPurity);
		}
		return unstained ? unstainedStageLabel(requiredDegree) : degreeLabel(requiredDegree);
	}

	private static String formatThreshold(float value) {
		return value == Math.rint(value) ? Integer.toString((int) value) : Float.toString(value);
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
