package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonTrialEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

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
	public static final ResourceLocation ADV_FOUNDING_FANE_ESTABLISHED =
			Hemomancy.rloc("hemomancy/founding_fane_established");
	public static final ResourceLocation ADV_COVENANT_WRITTEN_IN_PLACE =
			Hemomancy.rloc("hemomancy/covenant_written_in_place");
	public static final ResourceLocation ADV_CHAMBER_RETURNED =
			Hemomancy.rloc("hemomancy/chamber_returned");
	public static final ResourceLocation ADV_WARP_CHAIR_BOUND =
			Hemomancy.rloc("hemomancy/warp_chair_bound");
	public static final ResourceLocation ADV_CHAMBER_RITE_ATTUNED =
			Hemomancy.rloc("hemomancy/chamber_rite_attuned");
	public static final ResourceLocation ADV_COVENANT_THRONE_BOUND =
			Hemomancy.rloc("hemomancy/covenant_throne_bound");
	public static final ResourceLocation ADV_COVENANT_VIGIL_COMPLETED =
			Hemomancy.rloc("hemomancy/covenant_vigil_completed");
	public static final ResourceLocation ADV_LIVING_COVENANT_COMPLETE =
			Hemomancy.rloc("hemomancy/living_covenant_complete");
	public static final ResourceLocation ADV_HERMIT_ROAD_FIRST_REMNANT =
			Hemomancy.rloc("hemomancy/hermit_road_first_remnant");
	public static final ResourceLocation ADV_HERMIT_ROAD_LEDGER_GRANTED =
			Hemomancy.rloc("hemomancy/hermit_road_ledger_granted");
	public static final ResourceLocation ADV_HERMIT_ROAD_REPORTED =
			Hemomancy.rloc("hemomancy/hermit_road_reported");
	public static final ResourceLocation ADV_VESSEL_FILLED =
			Hemomancy.rloc("hemomancy/vessel_filled");
	public static final ResourceLocation ADV_FANE_SANGUINIUM =
			Hemomancy.rloc("hemomancy/fane_sanguinium");
	public static final ResourceLocation ADV_IRON_IN_THE_BLOOD =
			Hemomancy.rloc("hemomancy/iron_in_the_blood");
	public static final ResourceLocation ADV_FIRST_SEPARATION_STARTED =
			Hemomancy.rloc("hemomancy/first_separation_started");
	public static final ResourceLocation ADV_FIRST_SEPARATION_COMPLETE =
			Hemomancy.rloc("hemomancy/first_separation_complete");
	public static final ResourceLocation ADV_RED_TAXONOMY_INFECTED_FUNGUS =
			Hemomancy.rloc("hemomancy/red_taxonomy_infected_fungus");
	public static final ResourceLocation ADV_RED_TAXONOMY_STINKHORN_FUNGUS =
			Hemomancy.rloc("hemomancy/red_taxonomy_stinkhorn_fungus");
	public static final ResourceLocation ADV_RED_TAXONOMY_SARCODES =
			Hemomancy.rloc("hemomancy/red_taxonomy_sarcodes");
	public static final ResourceLocation ADV_RED_TAXONOMY_BLEEDING_HEART =
			Hemomancy.rloc("hemomancy/red_taxonomy_bleeding_heart");
	public static final ResourceLocation ADV_RED_TAXONOMY_RAFFLESIA =
			Hemomancy.rloc("hemomancy/red_taxonomy_rafflesia");
	public static final ResourceLocation ADV_RED_TAXONOMY_DEVILS_TOOTH =
			Hemomancy.rloc("hemomancy/red_taxonomy_devils_tooth");
	public static final ResourceLocation ADV_RED_TAXONOMY_PUFFBALL_FUNGUS =
			Hemomancy.rloc("hemomancy/red_taxonomy_puffball_fungus");
	public static final ResourceLocation ADV_RED_TAXONOMY_COMPLETE =
			Hemomancy.rloc("hemomancy/red_taxonomy_complete");
	public static final ResourceLocation ADV_ENZYME_MASTERY_VIVACIOUS =
			Hemomancy.rloc("hemomancy/enzyme_mastery_vivacious");
	public static final ResourceLocation ADV_ENZYME_MASTERY_FERVENT =
			Hemomancy.rloc("hemomancy/enzyme_mastery_fervent");
	public static final ResourceLocation ADV_ENZYME_MASTERY_NEUROTIC =
			Hemomancy.rloc("hemomancy/enzyme_mastery_neurotic");
	public static final ResourceLocation ADV_ENZYME_MASTERY_INCANDESCENT =
			Hemomancy.rloc("hemomancy/enzyme_mastery_incandescent");
	public static final ResourceLocation ADV_ENZYME_MASTERY_RUINOUS =
			Hemomancy.rloc("hemomancy/enzyme_mastery_ruinous");
	public static final ResourceLocation ADV_ENZYME_MASTERY_FRIGID =
			Hemomancy.rloc("hemomancy/enzyme_mastery_frigid");
	public static final ResourceLocation ADV_ENZYME_MASTERY_FERRIC =
			Hemomancy.rloc("hemomancy/enzyme_mastery_ferric");
	public static final ResourceLocation ADV_ENZYME_MASTERY_UMBRAL =
			Hemomancy.rloc("hemomancy/enzyme_mastery_umbral");
	public static final ResourceLocation ADV_ENZYME_MASTERY_COMPLETE =
			Hemomancy.rloc("hemomancy/enzyme_mastery_complete");
	public static final ResourceLocation ADV_FIRST_CULTURE_COMPLETE =
			Hemomancy.rloc("hemomancy/first_culture_complete");
	public static final ResourceLocation ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE =
			Hemomancy.rloc("hemomancy/mnemonist_woven_vessel_complete");
	public static final ResourceLocation ADV_MNEMONIST_FIRST_WEAVE_COMPLETE =
			Hemomancy.rloc("hemomancy/mnemonist_first_weave_complete");
	public static final ResourceLocation ADV_MNEMONIST_WOVEN_VESSEL_FINISHED =
			Hemomancy.rloc("hemomancy/mnemonist_woven_vessel_finished");
	public static final ResourceLocation ADV_NOETIC_CONDUCTIVE_MARK_RECOGNIZED =
			Hemomancy.rloc("hemomancy/noetic_conductive_mark_recognized");
	public static final ResourceLocation ADV_VICAR_MASONS_RESPITE_DIRECTIVE =
			Hemomancy.rloc("hemomancy/vicar_masons_respite_directive");
	public static final ResourceLocation ADV_VEIN_MASON_FIRST_LESSON =
			Hemomancy.rloc("hemomancy/vein_mason_first_lesson");
	public static final ResourceLocation ADV_VEIN_MASON_FIRST_SCAR_CARVED =
			Hemomancy.rloc("hemomancy/vein_mason_first_scar_carved");
	public static final ResourceLocation ADV_VEIN_MASON_FIRST_SCAR_LEARNED =
			Hemomancy.rloc("hemomancy/vein_mason_first_scar_learned");
	public static final ResourceLocation ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN =
			Hemomancy.rloc("hemomancy/vein_mason_first_effigy_pattern");
	public static final ResourceLocation ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT =
			Hemomancy.rloc("hemomancy/vein_mason_first_effigy_loadout");
	public static final ResourceLocation ADV_VEIN_MASON_CONTINUATION_READY =
			Hemomancy.rloc("hemomancy/vein_mason_continuation_ready");
	public static final ResourceLocation ADV_VEIN_MASON_REWARD_CLAIMED =
			Hemomancy.rloc("hemomancy/vein_mason_reward_claimed");
	public static final ResourceLocation ADV_ARTIFICER_ARMATURE_PLACED =
			Hemomancy.rloc("hemomancy/artificer_armature_placed");
	public static final ResourceLocation ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE =
			Hemomancy.rloc("hemomancy/artificer_first_hematic_upgrade");
	public static final ResourceLocation ADV_ARTIFICER_WORN_VOW_LESSON_READY =
			Hemomancy.rloc("hemomancy/artificer_worn_vow_lesson_ready");
	public static final ResourceLocation ADV_ARTIFICER_WORN_VOW_FITTING_READY =
			Hemomancy.rloc("hemomancy/artificer_worn_vow_fitting_ready");
	public static final ResourceLocation ADV_ARTIFICER_HEMATIC_IRON_FITTING =
			Hemomancy.rloc("hemomancy/artificer_hematic_iron_fitting");
	public static final ResourceLocation ADV_ARTIFICER_FIRST_FORK_UPGRADE =
			Hemomancy.rloc("hemomancy/artificer_first_fork_upgrade");
	public static final ResourceLocation ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY =
			Hemomancy.rloc("hemomancy/artificer_three_answers_lesson_ready");
	public static final ResourceLocation ADV_ARTIFICER_THREE_ANSWERS_FITTING_READY =
			Hemomancy.rloc("hemomancy/artificer_three_answers_fitting_ready");
	public static final ResourceLocation ADV_ARTIFICER_BARBED_FITTING =
			Hemomancy.rloc("hemomancy/artificer_barbed_fitting");
	public static final ResourceLocation ADV_ARTIFICER_CHITINITE_FITTING =
			Hemomancy.rloc("hemomancy/artificer_chitinite_fitting");
	public static final ResourceLocation ADV_ARTIFICER_PRISMATIC_FITTING =
			Hemomancy.rloc("hemomancy/artificer_prismatic_fitting");
	public static final ResourceLocation ADV_ARTIFICER_FRAME_CONSECRATED =
			Hemomancy.rloc("hemomancy/artificer_frame_consecrated");
	public static final ResourceLocation ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE =
			Hemomancy.rloc("hemomancy/artificer_first_blood_lust_upgrade");
	public static final ResourceLocation ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY =
			Hemomancy.rloc("hemomancy/artificer_crimson_vestment_lesson_ready");
	public static final ResourceLocation ADV_ARTIFICER_CRIMSON_VESTMENT_FITTING_READY =
			Hemomancy.rloc("hemomancy/artificer_crimson_vestment_fitting_ready");
	public static final ResourceLocation ADV_ARTIFICER_BLOOD_LUST_FITTING =
			Hemomancy.rloc("hemomancy/artificer_blood_lust_fitting");
	public static final ResourceLocation ADV_ARTIFICER_MONOLITHIC_FRAME =
			Hemomancy.rloc("hemomancy/artificer_monolithic_frame");
	public static final ResourceLocation ADV_ARTIFICER_FIRST_D7_UPGRADE =
			Hemomancy.rloc("hemomancy/artificer_first_d7_upgrade");
	public static final ResourceLocation ADV_ARTIFICER_WEIGHT_OF_THE_FRAME_FITTING_READY =
			Hemomancy.rloc("hemomancy/artificer_weight_of_the_frame_fitting_ready");
	public static final ResourceLocation ADV_ARTIFICER_D7_FITTING =
			Hemomancy.rloc("hemomancy/artificer_d7_fitting");
	public static final ResourceLocation ADV_ARTIFICER_FIRST_LIVING_GRAFT =
			Hemomancy.rloc("hemomancy/artificer_first_living_graft");
	public static final ResourceLocation ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY =
			Hemomancy.rloc("hemomancy/artificer_assumed_limb_lesson_ready");
	public static final ResourceLocation ADV_ARTIFICER_ASSUMED_LIMB_FITTING_READY =
			Hemomancy.rloc("hemomancy/artificer_assumed_limb_fitting_ready");
	public static final ResourceLocation ADV_ARTIFICER_LIVING_ARSENAL_FITTING =
			Hemomancy.rloc("hemomancy/artificer_living_arsenal_fitting");

	// ── Endgame milestones ─────────────────────────────────────────────────────
	public static final ResourceLocation ADV_VOICES_IN_THE_VEIN =
			Hemomancy.rloc("hemomancy/voices_in_the_vein");
	public static final ResourceLocation ADV_ETERNAL_COVENANT_SEALED =
			Hemomancy.rloc("hemomancy/eternal_covenant_sealed");
	public static final ResourceLocation ADV_VESPER_DEFEATED =
			Hemomancy.rloc("hemomancy/vesper_defeated");
	public static final ResourceLocation ADV_MYCOPHANT_DEFEATED =
			Hemomancy.rloc("hemomancy/mycophant_defeated");

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

	private static final ResourceLocation[] RED_TAXONOMY_SPECIMENS = {
		ADV_RED_TAXONOMY_INFECTED_FUNGUS,
		ADV_RED_TAXONOMY_STINKHORN_FUNGUS,
		ADV_RED_TAXONOMY_SARCODES,
		ADV_RED_TAXONOMY_BLEEDING_HEART,
		ADV_RED_TAXONOMY_RAFFLESIA,
		ADV_RED_TAXONOMY_DEVILS_TOOTH,
		ADV_RED_TAXONOMY_PUFFBALL_FUNGUS,
	};

	public static final ResourceLocation[] ENZYME_MASTERY_RECORDS = {
		ADV_ENZYME_MASTERY_VIVACIOUS,
		ADV_ENZYME_MASTERY_FERVENT,
		ADV_ENZYME_MASTERY_NEUROTIC,
		ADV_ENZYME_MASTERY_INCANDESCENT,
		ADV_ENZYME_MASTERY_RUINOUS,
		ADV_ENZYME_MASTERY_FRIGID,
		ADV_ENZYME_MASTERY_FERRIC,
		ADV_ENZYME_MASTERY_UMBRAL,
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
		LiberKnowledgeHelper.unlockForDegree(player, degree);
		PuppeteerSummonTrialEvents.awardOrdealRecipes(player, degree);
	}

	/**
	 * Grants the advancement identified by {@code id} to the player if it has
	 * not already been completed. Safe to call repeatedly.
	 *
	 * <p>Book-knowledge unlocks for this advancement are handled automatically
	 * by HutosLib's {@code BookDiscoveryEvents.onAdvancementEarned}, which
	 * consults the mappings registered in {@code BookEntryRegistry}. No
	 * explicit {@code LiberKnowledgeHelper} call is needed here.
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

	public static boolean hasAdvancement(ServerPlayer player, ResourceLocation id) {
		AdvancementHolder advancement = player.server.getAdvancements().get(id);
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	public static int getRedTaxonomySpecimenCount(ServerPlayer player) {
		int count = 0;
		for (ResourceLocation specimen : RED_TAXONOMY_SPECIMENS) {
			if (hasAdvancement(player, specimen)) {
				count++;
			}
		}
		return count;
	}

	public static boolean isRedTaxonomyComplete(ServerPlayer player) {
		return hasAdvancement(player, ADV_RED_TAXONOMY_COMPLETE);
	}

	public static int getEnzymeMasteryCount(ServerPlayer player) {
		int count = 0;
		for (ResourceLocation enzyme : ENZYME_MASTERY_RECORDS) {
			if (hasAdvancement(player, enzyme)) {
				count++;
			}
		}
		return count;
	}

	public static boolean isEnzymeMasteryComplete(ServerPlayer player) {
		return hasAdvancement(player, ADV_ENZYME_MASTERY_COMPLETE);
	}

	public static boolean hasRecordedEnzyme(ServerPlayer player, ItemStack enzyme) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(enzyme.getItem());
		if (!itemId.getNamespace().equals(Hemomancy.MOD_ID)) return false;
		ResourceLocation record = switch (itemId.getPath()) {
			case "vivacious_enzyme" -> ADV_ENZYME_MASTERY_VIVACIOUS;
			case "fervent_enzyme" -> ADV_ENZYME_MASTERY_FERVENT;
			case "neurotic_enzyme" -> ADV_ENZYME_MASTERY_NEUROTIC;
			case "incandescent_enzyme" -> ADV_ENZYME_MASTERY_INCANDESCENT;
			case "ruinous_enzyme" -> ADV_ENZYME_MASTERY_RUINOUS;
			case "frigid_enzyme" -> ADV_ENZYME_MASTERY_FRIGID;
			case "ferric_enzyme" -> ADV_ENZYME_MASTERY_FERRIC;
			case "umbral_enzyme" -> ADV_ENZYME_MASTERY_UMBRAL;
			default -> null;
		};
		return record != null && hasAdvancement(player, record);
	}

	public static boolean isVesselFilled(ServerPlayer player) {
		return hasAdvancement(player, ADV_VESSEL_FILLED);
	}

	public static boolean isLiberSanguinumCrafted(ServerPlayer player) {
		return hasAdvancement(player, ADV_FANE_SANGUINIUM);
	}

	public static boolean isHematicIronBlockCrafted(ServerPlayer player) {
		return hasAdvancement(player, ADV_IRON_IN_THE_BLOOD);
	}

	public static boolean isFirstSeparationStarted(ServerPlayer player) {
		return hasAdvancement(player, ADV_FIRST_SEPARATION_STARTED);
	}

	public static boolean isFirstSeparationComplete(ServerPlayer player) {
		return hasAdvancement(player, ADV_FIRST_SEPARATION_COMPLETE);
	}

	public static boolean isMnemonistWovenVesselComplete(ServerPlayer player) {
		return hasAdvancement(player, ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE);
	}

	public static boolean isMnemonistFirstWeaveComplete(ServerPlayer player) {
		return hasAdvancement(player, ADV_MNEMONIST_FIRST_WEAVE_COMPLETE);
	}

	public static boolean isMnemonistWovenVesselFinished(ServerPlayer player) {
		return hasAdvancement(player, ADV_MNEMONIST_WOVEN_VESSEL_FINISHED);
	}

	public static boolean isVicarMasonsRespiteDirective(ServerPlayer player) {
		return hasAdvancement(player, ADV_VICAR_MASONS_RESPITE_DIRECTIVE);
	}

	public static boolean isVeinMasonFirstLesson(ServerPlayer player) {
		return hasAdvancement(player, ADV_VEIN_MASON_FIRST_LESSON);
	}

	public static boolean isVeinMasonFirstScarCarved(ServerPlayer player) {
		return hasAdvancement(player, ADV_VEIN_MASON_FIRST_SCAR_CARVED);
	}

	public static boolean isVeinMasonFirstScarLearned(ServerPlayer player) {
		return hasAdvancement(player, ADV_VEIN_MASON_FIRST_SCAR_LEARNED);
	}

	public static boolean isVeinMasonFirstEffigyPattern(ServerPlayer player) {
		return hasAdvancement(player, ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN);
	}

	public static boolean isVeinMasonFirstEffigyLoadout(ServerPlayer player) {
		return hasAdvancement(player, ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
	}

	public static boolean isVeinMasonContinuationReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_VEIN_MASON_CONTINUATION_READY);
	}

	public static boolean isVeinMasonRewardClaimed(ServerPlayer player) {
		return hasAdvancement(player, ADV_VEIN_MASON_REWARD_CLAIMED);
	}

	public static boolean isArtificerArmaturePlaced(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_ARMATURE_PLACED);
	}

	public static boolean isArtificerFirstHematicUpgrade(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE);
	}

	public static boolean isArtificerWornVowLessonReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_WORN_VOW_LESSON_READY);
	}

	public static boolean isArtificerWornVowFittingReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_WORN_VOW_FITTING_READY);
	}

	public static boolean isArtificerHematicIronFitting(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_HEMATIC_IRON_FITTING);
	}

	public static boolean isArtificerFirstForkUpgrade(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_FIRST_FORK_UPGRADE);
	}

	public static boolean isArtificerThreeAnswersLessonReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY);
	}

	public static boolean isArtificerThreeAnswersFittingReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_THREE_ANSWERS_FITTING_READY);
	}

	public static boolean isArtificerBarbedFitting(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_BARBED_FITTING);
	}

	public static boolean isArtificerChitiniteFitting(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_CHITINITE_FITTING);
	}

	public static boolean isArtificerPrismaticFitting(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_PRISMATIC_FITTING);
	}

	public static boolean isArtificerForkFitting(ServerPlayer player) {
		return isArtificerBarbedFitting(player)
				|| isArtificerChitiniteFitting(player)
				|| isArtificerPrismaticFitting(player);
	}

	public static boolean isArtificerFrameConsecrated(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_FRAME_CONSECRATED);
	}

	public static boolean isArtificerFirstBloodLustUpgrade(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE);
	}

	public static boolean isArtificerCrimsonVestmentLessonReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY);
	}

	public static boolean isArtificerCrimsonVestmentFittingReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_CRIMSON_VESTMENT_FITTING_READY);
	}

	public static boolean isArtificerBloodLustFitting(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_BLOOD_LUST_FITTING);
	}

	public static boolean isArtificerMonolithicFrame(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_MONOLITHIC_FRAME);
	}

	public static boolean isArtificerFirstD7Upgrade(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_FIRST_D7_UPGRADE);
	}

	public static boolean isArtificerWeightOfTheFrameFittingReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_WEIGHT_OF_THE_FRAME_FITTING_READY);
	}

	public static boolean isArtificerD7Fitting(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_D7_FITTING);
	}

	public static boolean isArtificerFirstLivingGraft(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_FIRST_LIVING_GRAFT);
	}

	public static boolean isArtificerAssumedLimbLessonReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY);
	}

	public static boolean isArtificerAssumedLimbFittingReady(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_ASSUMED_LIMB_FITTING_READY);
	}

	public static boolean isArtificerLivingArsenalFitting(ServerPlayer player) {
		return hasAdvancement(player, ADV_ARTIFICER_LIVING_ARSENAL_FITTING);
	}
}
