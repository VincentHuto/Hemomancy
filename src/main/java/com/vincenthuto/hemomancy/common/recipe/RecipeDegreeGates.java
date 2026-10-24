package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
			return clampLevel(ProgressionGates.unstainedStage(player));
		}
		return clampLevel(HemoCapabilityAccess.getPlayerDegreeNumber(player));
	}

	public static boolean playerMeets(Player player, BloodStructureRecipe recipe) {
		return getPlayerLevel(player, recipe.isUnstained()) >= getRequiredDegree(recipe)
				&& meetsUnstainedThresholds(player, recipe.isUnstained(), recipe.getRequiredPurity(), recipe.getRequiredClarity());
	}

	/** The Mnemonic Reliquary's Working craft also waits for the Mnemonist's lesson. */
	static boolean needsReliquaryLesson(ResourceLocation id) {
		return id != null && Hemomancy.MOD_ID.equals(id.getNamespace())
				&& "cardinal_rite/working/mnemonic_reliquary".equals(id.getPath());
	}

	public static boolean playerMeets(Player player, CardinalRiteRecipe recipe) {
		return getPlayerLevel(player, recipe.isUnstained()) >= getRequiredDegree(recipe)
				&& (!(player instanceof ServerPlayer serverPlayer) || !needsReliquaryLesson(recipe.getId())
						|| MnemonicReliquaryProgression.isTaught(serverPlayer))
				&& meetsUnstainedThresholds(player, recipe.isUnstained(), recipe.getRequiredPurity(), recipe.getRequiredClarity());
	}

	/**
	 * Whether a rite should participate in station resolution for this player.
	 * Completed rank rites are deliberately excluded before floor/offering
	 * matching so an old promotion cannot make the next promotion ambiguous.
	 */
	public static boolean playerMayAttempt(Player player, CardinalRiteRecipe recipe) {
		if (!playerMeets(player, recipe)) return false;
		Integer target = getRankupTargetDegree(recipe);
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
		var progress = HemoCapabilityAccess.getUnstainedProgress(player);
		return (requiredPurity < 0 || progress.getPurity() >= requiredPurity)
				&& (requiredClarity < 0 || progress.hasClarityUnlocked()
						&& progress.getClarity() >= requiredClarity);
	}

	public static MutableComponent requirementLabel(BloodStructureRecipe recipe) {
		return requirementLabel(recipe.isUnstained(), recipe.getRequiredDegree(), recipe.getRequiredPurity(),
				recipe.getRequiredClarity());
	}

	public static MutableComponent requirementLabel(CardinalRiteRecipe recipe) {
		if (needsReliquaryLesson(recipe.getId())) {
			return Component.translatable("message.hemomancy.recipe_degree_gates.the_mnemonists_reliquary_lesson");
		}
		return requirementLabel(recipe.isUnstained(), recipe.getRequiredDegree(), recipe.getRequiredPurity(),
				recipe.getRequiredClarity());
	}

	private static MutableComponent requirementLabel(boolean unstained, int requiredDegree, float requiredPurity,
			float requiredClarity) {
		if (unstained && requiredClarity >= 0) {
			return Component.translatable("message.hemomancy.recipe_degree_gates.clarity", formatThreshold(requiredClarity));
		}
		if (unstained && requiredPurity >= 0) {
			return Component.translatable("message.hemomancy.recipe_degree_gates.purity", formatThreshold(requiredPurity));
		}
		return unstained ? unstainedStageLabel(requiredDegree) : degreeLabel(requiredDegree);
	}

	private static String formatThreshold(float value) {
		return value == Math.rint(value) ? Integer.toString((int) value) : Float.toString(value);
	}

	/**
	 * The degree a rank rite grants: one above the degree it requires. Every
	 * Harbinger rank rite follows this, so the recipe itself is the authority.
	 */
	public static Integer getRankupTargetDegree(CardinalRiteRecipe recipe) {
		if (recipe == null || !recipe.isRankup() || recipe.isUnstained()) return null;
		return clampLevel(getRequiredDegree(recipe) + 1);
	}

	/** Legacy lookup by id, kept for rites that predate recipe-carried targets. */
	public static Integer getRankupTargetDegree(ResourceLocation riteId) {
		return riteId == null ? null : RANKUP_RITE_TARGET_DEGREES.get(riteId.getPath());
	}

	public static MutableComponent degreeLabel(int degree) {
		int clamped = clampLevel(degree);
		if (clamped == 0) {
			return Component.translatable("message.hemomancy.recipe_degree_gates.no_degree");
		}
		EnumInitiatoryDegree named = EnumInitiatoryDegree.byNumber(clamped);
		return named != null
				? Component.translatable("message.hemomancy.recipe_degree_gates.degree_named", clamped, named.displayName())
				: Component.translatable("message.hemomancy.recipe_degree_gates.degree", clamped);
	}

	public static MutableComponent unstainedStageLabel(int level) {
		return Component.translatable(switch (clampLevel(level)) {
			case 0 -> "message.hemomancy.recipe_degree_gates.stage_unbegun";
			case 1 -> "message.hemomancy.recipe_degree_gates.stage_begun";
			case 2 -> "hemomancy.purity.tainted";
			case 3 -> "hemomancy.purity.cleansing";
			case 4 -> "hemomancy.purity.absolved";
			case 5 -> "hemomancy.purity.purified";
			case 6 -> "hemomancy.clarity.discerning";
			case 7 -> "hemomancy.clarity.vigilant";
			case 8 -> "hemomancy.clarity.enlightened";
			default -> "message.hemomancy.recipe_degree_gates.stage_unknown";
		});
	}
}
