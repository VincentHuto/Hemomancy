package com.vincenthuto.hemomancy.common.event;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.Hemomancy;

/**
 * Utility class for programmatically granting Hemomancy Harbinger-path
 * advancements when initiatory degrees are attained or when specific
 * in-world rites are completed.
 *
 * <p>Advancement JSON files that are meant to be granted this way should declare
 * {@code "trigger": "minecraft:impossible"} in their criteria, ensuring they
 * can only be awarded through this helper and not by normal gameplay triggers.
 */
public final class HarbingerAdvancementGranter {

	// ── Degree chain advancements ──────────────────────────────────────────────
	public static final ResourceLocation ADV_DEGREE_1_NEOPHYTE =
			Hemomancy.rloc("hemomancy/degree_1_neophyte");
	public static final ResourceLocation ADV_DEGREE_2_VOTARY =
			Hemomancy.rloc("hemomancy/degree_2_votary");
	public static final ResourceLocation ADV_DEGREE_3_INITIATE =
			Hemomancy.rloc("hemomancy/degree_3_initiate");
	public static final ResourceLocation ADV_DEGREE_4_ADEPT =
			Hemomancy.rloc("hemomancy/degree_4_adept");
	public static final ResourceLocation ADV_DEGREE_5_ILLUMINATUS =
			Hemomancy.rloc("hemomancy/degree_5_illuminatus");
	public static final ResourceLocation ADV_DEGREE_6_SANCTIFIED =
			Hemomancy.rloc("hemomancy/degree_6_sanctified");
	public static final ResourceLocation ADV_DEGREE_7_ARCHON =
			Hemomancy.rloc("hemomancy/degree_7_archon");
	public static final ResourceLocation ADV_DEGREE_8_APOTHEOS =
			Hemomancy.rloc("hemomancy/degree_8_apotheos");

	// ── Order function milestones ──────────────────────────────────────────────
	public static final ResourceLocation ADV_BLOOD_IS_BOUND =
			Hemomancy.rloc("hemomancy/blood_is_bound");
	public static final ResourceLocation ADV_CRIMSON_LODGE_CONSECRATED =
			Hemomancy.rloc("hemomancy/crimson_lodge_consecrated");
	public static final ResourceLocation ADV_FOUNDING_SANCTUM_ESTABLISHED =
			Hemomancy.rloc("hemomancy/founding_sanctum_established");

	// ── Endgame milestones ─────────────────────────────────────────────────────
	public static final ResourceLocation ADV_VOICES_IN_THE_VEIN =
			Hemomancy.rloc("hemomancy/voices_in_the_vein");
	public static final ResourceLocation ADV_ETERNAL_COVENANT_SEALED =
			Hemomancy.rloc("hemomancy/eternal_covenant_sealed");

	// ── Mastery side branches ──────────────────────────────────────────────────
	public static final ResourceLocation ADV_SANGUINE_DOMAIN =
			Hemomancy.rloc("hemomancy/sanguine_domain");

	/** Ordered array of degree advancements, index 0 = degree 1. */
	private static final ResourceLocation[] DEGREE_ADVANCEMENTS = {
		ADV_DEGREE_1_NEOPHYTE,
		ADV_DEGREE_2_VOTARY,
		ADV_DEGREE_3_INITIATE,
		ADV_DEGREE_4_ADEPT,
		ADV_DEGREE_5_ILLUMINATUS,
		ADV_DEGREE_6_SANCTIFIED,
		ADV_DEGREE_7_ARCHON,
		ADV_DEGREE_8_APOTHEOS,
	};

	private HarbingerAdvancementGranter() {}

	/**
	 * Grants all degree advancements up to and including {@code degree}.
	 * Degrees are numbered 1–8; values outside this range are silently ignored.
	 * Safe to call repeatedly — already-completed advancements are skipped.
	 *
	 * @param player The server-side player to receive the advancement(s).
	 * @param degree The degree number (1–8) the player has just reached.
	 */
	public static void grantDegree(ServerPlayer player, int degree) {
		for (int i = 1; i <= degree && i <= DEGREE_ADVANCEMENTS.length; i++) {
			grantIfNotDone(player, DEGREE_ADVANCEMENTS[i - 1]);
		}
	}

	/**
	 * Grants the advancement identified by {@code id} to the player if it has
	 * not already been completed. Safe to call repeatedly.
	 *
	 * @param player The server-side player to receive the advancement.
	 * @param id     The ResourceLocation of the advancement to grant.
	 */
	public static void grantIfNotDone(ServerPlayer player, ResourceLocation id) {
		AdvancementHolder advancement = player.server.getAdvancements().get(id);
		if (advancement == null) return;

		AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
		if (progress.isDone()) return;

		for (String criterion : progress.getRemainingCriteria()) {
			player.getAdvancements().award(advancement, criterion);
		}
	}
}
