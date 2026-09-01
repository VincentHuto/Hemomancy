package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarsContainer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArts;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Development-only ownership boundary for player state changed by the journey. */
public final class HemoJourneySnapshot {
	public static final String SNAPSHOT_KEY = "hemomancy.dev_test.journey.snapshot";
	public static final String STAGE_KEY = "hemomancy.dev_test.journey.stage";

	private static final String BLOOD_ACTIVE = "blood_active";
	private static final String BLOOD_CURRENT = "blood_current";
	private static final String BLOOD_MAX = "blood_max";
	private static final String DEGREE = "degree";
	private static final String DEGREE_STATE = "degree_state";
	private static final String BLOOD_TENDENCY = "blood_tendency";
	private static final String INVENTORY = "inventory";
	private static final String ADVANCEMENTS = "advancements";
	private static final String ORIGIN_DIMENSION = "origin_dimension";
	private static final String ORIGIN_POSITION = "origin_position";
	private static final String CURRENT_STAGE = "current_stage";
	private static final String SKILL_PROGRESS = "skill_progress";
	private static final String LIBER_KNOWLEDGE = "liber_knowledge";
	private static final String KNOWN_MANIPULATIONS = "known_manipulations";
	private static final String SCAR_STATE = "scar_state";
	private static final String UNSTAINED_PROGRESS = "unstained_progress";
	private static final String KNOWN_STILL_ARTS = "known_still_arts";
	private static final String MUSCLE_MEMORY = "muscle_memory";
	private static final String LIVING_STAFF_PROGRESS = "living_staff_progress";
	private static final String VASCULAR_SYSTEM = "vascular_system";
	private static final String RECIPE_BOOK = "recipe_book";
	private static final String SPECIMEN_BESTIARY = "specimen_bestiary";
	private static final String PERSISTENT_DATA = "persistent_data";
	private static final String VASC_EQUIPMENT = "vasc_equipment";
	private static final int VASC_SLOT = 5;
	private static final List<String> JOURNEY_PERSISTENT_KEYS = List.of(
			ArtificerAssignments.WORN_VOW_REWARD_CLAIM_KEY,
			ArtificerAssignments.THREE_ANSWERS_REWARD_CLAIM_KEY,
			ArtificerAssignments.CRIMSON_VESTMENT_REWARD_CLAIM_KEY,
			ArtificerAssignments.ASSUMED_LIMB_REWARD_CLAIM_KEY,
			ArtificerAssignments.D7_REWARD_CLAIM_KEY,
			ArtificerAssignments.FORK_RESEARCH_REWARD_CLAIM_KEY,
			ArtificerAssignments.FIRST_FORK_FAMILY_KEY,
			ArtificerAssignments.FIRST_D7_LINEAGE_KEY,
			FungalGardenTravelHelper.ARCHON_CHOICE_KEY,
			FungalGardenTravelHelper.REVELATION_CHOICE_PENDING,
			"hemomancy.blessed_hermit",
			"hemomancy.claimed_heart_hermit",
			"hemomancy.vicar_consecration_kit_claimed");

	/** Exact advancement ownership for every checkpoint currently driven by the journey. */
	private static final List<ResourceLocation> JOURNEY_ADVANCEMENTS = List.of(
			Hemomancy.rloc("hemomancy/the_first_awakening"),
			Hemomancy.rloc("hemomancy/degree_1_neophyte"),
			HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT,
			HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED,
			HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED,
			Hemomancy.rloc("hemomancy/vessel_filled"),
			Hemomancy.rloc("hemomancy/fane_sanguinium"),
			Hemomancy.rloc("hemomancy/iron_in_the_blood"),
			FirstBloodcraftAssignment.ADV_REWARD_CLAIMED,
			HarbingerAdvancementGranter.ADV_DEGREE_2_VOTARY,
			FirstSeparationAssignment.ADV_BRIEFED,
			HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED,
			HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE,
			FirstSeparationAssignment.ADV_REWARD_CLAIMED,
			BodyAnswersAssignment.ADV_BRIEFED,
			BodyAnswersAssignment.ADV_COMPLETE,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_INFECTED_FUNGUS,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_STINKHORN_FUNGUS,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_SARCODES,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_BLEEDING_HEART,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_RAFFLESIA,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_DEVILS_TOOTH,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_PUFFBALL_FUNGUS,
			HarbingerAdvancementGranter.ADV_RED_TAXONOMY_COMPLETE,
			ArtificerAssignments.WORN_VOW_BRIEFED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_ARMATURE_PLACED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE,
			HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_LESSON_READY,
			HarbingerAdvancementGranter.ADV_ARTIFICER_WORN_VOW_FITTING_READY,
			HarbingerAdvancementGranter.ADV_ARTIFICER_HEMATIC_IRON_FITTING,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_VIVACIOUS,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_FERVENT,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_NEUROTIC,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_INCANDESCENT,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_RUINOUS,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_FRIGID,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_FERRIC,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_UMBRAL,
			HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_COMPLETE,
			HarbingerAdvancementGranter.ADV_DEGREE_3_INITIATE,
			HarbingerAdvancementGranter.ADV_FIRST_CULTURE_COMPLETE,
			HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE,
			MnemonicReliquaryProgression.ADV_TAUGHT,
			HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE,
			HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED,
			HarbingerAdvancementGranter.ADV_NOETIC_CONDUCTIVE_MARK_RECOGNIZED,
			ArtificerAssignments.THREE_ANSWERS_BRIEFED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE,
			HarbingerAdvancementGranter.ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY,
			ArtificerAssignments.THREE_ANSWERS_INSPECTED,
			ArtificerAssignments.THREE_ANSWERS_COUNSELED,
			ArtificerAssignments.THREE_ANSWERS_DEMONSTRATED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_THREE_ANSWERS_FITTING_READY,
			HarbingerAdvancementGranter.ADV_ARTIFICER_BARBED_FITTING,
			HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT,
			HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE,
			HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON,
			HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED,
			HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED,
			HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN,
			HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT,
			HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY,
			HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED,
			HarbingerAdvancementGranter.ADV_DEGREE_5_ILLUMINATUS,
			VeinMasonAssignments.D5_VARICOSE,
			VeinMasonAssignments.D5_DIAGNOSED,
			VeinMasonAssignments.D5_TREATED,
			VeinMasonAssignments.D5_READY,
			VeinMasonAssignments.D5_REWARD,
			ArtificerAssignments.ASSUMED_LIMB_BRIEFED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT,
			HarbingerAdvancementGranter.ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY,
			ArtificerAssignments.ASSUMED_LIMB_DEMONSTRATED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_ASSUMED_LIMB_FITTING_READY,
			HarbingerAdvancementGranter.ADV_ARTIFICER_LIVING_ARSENAL_FITTING,
			Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/blade"),
			Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/axe"),
			Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/spear"),
			Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/claws"),
			Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/crossbow"),
			Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/torch"),
			Hemomancy.rloc("recipe/hemomancy/living_weapon_graft/flail"),
			ArtificerAssignments.CRIMSON_VESTMENT_BRIEFED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_FRAME_CONSECRATED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY,
			ArtificerAssignments.CRIMSON_VESTMENT_INSPECTED,
			ArtificerAssignments.CRIMSON_VESTMENT_COUNSELED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE,
			ArtificerAssignments.CRIMSON_VESTMENT_DEMONSTRATED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_CRIMSON_VESTMENT_FITTING_READY,
			HarbingerAdvancementGranter.ADV_ARTIFICER_BLOOD_LUST_FITTING,
			HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED,
			HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE,
			HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED,
			VeinMasonAssignments.D6_REFERRAL,
			VeinMasonAssignments.D6_COUNSEL,
			VeinMasonAssignments.D6_FIRST_ROUTE,
			VeinMasonAssignments.D6_LOADOUT,
			VeinMasonAssignments.D6_SECOND_ROUTE,
			VeinMasonAssignments.D6_READY,
			VeinMasonAssignments.D6_REWARD,
			HarbingerAdvancementGranter.ADV_WARP_CHAIR_BOUND,
			HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED,
			HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED,
			HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND,
			HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED,
			HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE,
			HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON,
			HarbingerAdvancementGranter.ADV_DEGREE_8_APOTHEOS,
			ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_MONOLITHIC_FRAME,
			HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_D7_UPGRADE,
			ArtificerAssignments.WEIGHT_OF_FRAME_LESSON_READY,
			ArtificerAssignments.WEIGHT_OF_FRAME_INSPECTED,
			ArtificerAssignments.WEIGHT_OF_FRAME_DEMONSTRATED,
			HarbingerAdvancementGranter.ADV_ARTIFICER_WEIGHT_OF_THE_FRAME_FITTING_READY,
			HarbingerAdvancementGranter.ADV_ARTIFICER_D7_FITTING,
			UnstainedAdvancementGranter.ADV_TAINTED,
			UnstainedAdvancementGranter.ADV_CLEANSING,
			UnstainedAdvancementGranter.ADV_ABSOLVED,
			UnstainedAdvancementGranter.ADV_PURIFIED,
			UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED,
			UnstainedAdvancementGranter.ADV_DISCERNING,
			UnstainedAdvancementGranter.ADV_VIGILANT,
			UnstainedAdvancementGranter.ADV_RESOLUTE_STAGE,
			UnstainedAdvancementGranter.ADV_ENLIGHTENED_SEEKER);

	private HemoJourneySnapshot() {
	}

	public static HemoJourneyResult capture(ServerPlayer player) {
		CompoundTag data = player.getPersistentData();
		if (data.contains(SNAPSHOT_KEY)) {
			return HemoJourneyResult.fail(currentStage(data),
					"Journey snapshot already exists; use journey reset or clear instead.");
		}

		CompoundTag snapshot = new CompoundTag();
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		snapshot.putBoolean(BLOOD_ACTIVE, blood.isActive());
		snapshot.putDouble(BLOOD_CURRENT, blood.getBloodVolume());
		snapshot.putDouble(BLOOD_MAX, blood.getMaxBloodVolume());
		snapshot.putInt(DEGREE, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber());
		snapshot.put(DEGREE_STATE, player.getData(HemoAttachmentTypes.INITIATORY_DEGREE)
				.serializeNBT(player.registryAccess()));
		snapshot.put(BLOOD_TENDENCY, player.getData(HemoAttachmentTypes.BLOOD_TENDENCY)
				.serializeNBT(player.registryAccess()));
		snapshot.put(SKILL_PROGRESS,
				HemoCapabilityAccess.requireSkillProgress(player).serializeNBT(player.registryAccess()));
		if (!(HemoCapabilityAccess.requireLiberKnowledge(player) instanceof LiberKnowledge liberKnowledge)) {
			return HemoJourneyResult.fail(currentStage(data), "Liber knowledge snapshot failed: unsupported implementation.");
		}
		snapshot.put(LIBER_KNOWLEDGE, liberKnowledge.serializeNBT(player.registryAccess()));
		if (!(HemoCapabilityAccess.requireKnownManipulations(player) instanceof KnownManipulations knownManipulations)) {
			return HemoJourneyResult.fail(currentStage(data), "Known manipulation snapshot failed: unsupported implementation.");
		}
		snapshot.put(KNOWN_MANIPULATIONS, HemoJourneyManipulationState.capture(knownManipulations, player.registryAccess()));
		if (!(HemoCapabilityAccess.requireScarState(player) instanceof ScarsContainer scars)) {
			return HemoJourneyResult.fail(currentStage(data), "Scar state snapshot failed: unsupported implementation.");
		}
		snapshot.put(SCAR_STATE, scars.serializeNBT(player.registryAccess()));
		if (!(HemoCapabilityAccess.requireUnstainedProgress(player) instanceof UnstainedProgress unstained)) {
			return HemoJourneyResult.fail(currentStage(data), "Unstained progress snapshot failed: unsupported implementation.");
		}
		snapshot.put(UNSTAINED_PROGRESS, unstained.serializeNBT(player.registryAccess()));
		if (!(HemoCapabilityAccess.requireKnownStillArts(player) instanceof KnownStillArts knownStillArts)) {
			return HemoJourneyResult.fail(currentStage(data), "Known Still Arts snapshot failed: unsupported implementation.");
		}
		snapshot.put(KNOWN_STILL_ARTS, knownStillArts.serializeNBT(player.registryAccess()));
		snapshot.put(MUSCLE_MEMORY, player.getData(HemoAttachmentTypes.MUSCLE_MEMORY)
				.serializeNBT(player.registryAccess()));
		snapshot.put(LIVING_STAFF_PROGRESS, player.getData(HemoAttachmentTypes.LIVING_STAFF_PROGRESS)
				.serializeNBT(player.registryAccess()));
		snapshot.put(VASCULAR_SYSTEM, player.getData(HemoAttachmentTypes.VASCULAR_SYSTEM)
				.serializeNBT(player.registryAccess()));
		snapshot.put(RECIPE_BOOK, player.getRecipeBook().toNbt());
		snapshot.put(SPECIMEN_BESTIARY, HemoCapabilityAccess.requireSpecimenBestiary(player)
				.serializeNBT(player.registryAccess()));
		snapshot.put(PERSISTENT_DATA, capturePersistentData(player));
		HemoJourneyWorldState.capture(player, snapshot);
		snapshot.put(VASC_EQUIPMENT, HemoCapabilityAccess.requireEquipment(player).getStackInSlot(VASC_SLOT)
				.saveOptional(player.registryAccess()));

		ListTag inventory = new ListTag();
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			CompoundTag entry = new CompoundTag();
			entry.putInt("slot", slot);
			entry.put("stack", player.getInventory().getItem(slot).saveOptional(player.registryAccess()));
			inventory.add(entry);
		}
		snapshot.put(INVENTORY, inventory);

		CompoundTag advancements = new CompoundTag();
		for (ResourceLocation id : JOURNEY_ADVANCEMENTS) {
			advancements.putBoolean(id.toString(), HarbingerAdvancementGranter.hasAdvancement(player, id));
		}
		snapshot.put(ADVANCEMENTS, advancements);

		snapshot.putString(ORIGIN_DIMENSION, player.level().dimension().location().toString());
		CompoundTag origin = new CompoundTag();
		origin.putDouble("x", player.getX());
		origin.putDouble("y", player.getY());
		origin.putDouble("z", player.getZ());
		origin.putFloat("yaw", player.getYRot());
		origin.putFloat("pitch", player.getXRot());
		snapshot.put(ORIGIN_POSITION, origin);
		snapshot.putString(CURRENT_STAGE, currentStage(data).id());
		data.put(SNAPSHOT_KEY, snapshot);
		return new HemoJourneyResult(true, currentStage(data), "Captured journey player snapshot.");
	}

	public static HemoJourneyResult resetForJourney(ServerPlayer player) {
		CompoundTag data = player.getPersistentData();
		if (!data.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
			return HemoJourneyResult.fail(currentStage(data), "No journey snapshot exists to protect reset state.");
		}
		String availabilityFailure = validateAdvancementAvailability(player);
		if (availabilityFailure != null) return HemoJourneyResult.fail(currentStage(data), availabilityFailure);
		Map<ResourceLocation, Boolean> resetAdvancements = new LinkedHashMap<>();
		for (ResourceLocation id : JOURNEY_ADVANCEMENTS) resetAdvancements.put(id, false);
		String operationFailure = validateAdvancementOperations(player, resetAdvancements);
		if (operationFailure != null) return HemoJourneyResult.fail(currentStage(data), operationFailure);
		ParsedState rollback;
		try {
			rollback = captureLiveState(player);
		} catch (IllegalStateException exception) {
			return HemoJourneyResult.fail(currentStage(data), "Journey reset preflight failed: " + exception.getMessage());
		}
		List<ItemStack> emptyInventory = new ArrayList<>();
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) emptyInventory.add(ItemStack.EMPTY);
		var emptySkills = new com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress();
		var emptyKnowledge = new LiberKnowledge();
		var emptyManipulations = new KnownManipulations();
		var emptyScars = new ScarsContainer();
		var emptyUnstained = new UnstainedProgress();
		var emptyStillArts = new KnownStillArts();
		var emptyMuscleMemory = new MuscleMemoryState();
		var emptyLivingStaffProgress = new LivingStaffProgress();
		var emptyVascularSystem = new VascularSystem();
		var emptyDegree = new InitiatoryDegree();
		var emptyTendency = new BloodTendency();
		var emptyRecipeBook = new ServerRecipeBook();
		var emptyBestiary = new SpecimenBestiaryProgress();
		ParsedState reset = new ParsedState(false, 0.0D, 5000.0D, 0,
				emptyDegree.serializeNBT(player.registryAccess()), emptyTendency.serializeNBT(player.registryAccess()), emptyInventory,
				resetAdvancements, emptySkills.serializeNBT(player.registryAccess()),
				emptyKnowledge.serializeNBT(player.registryAccess()),
				HemoJourneyManipulationState.capture(emptyManipulations, player.registryAccess()),
				emptyScars.serializeNBT(player.registryAccess()), emptyUnstained.serializeNBT(player.registryAccess()),
				emptyStillArts.serializeNBT(player.registryAccess()),
				emptyMuscleMemory.serializeNBT(player.registryAccess()),
				emptyLivingStaffProgress.serializeNBT(player.registryAccess()),
				emptyVascularSystem.serializeNBT(player.registryAccess()), emptyRecipeBook.toNbt(),
				emptyBestiary.serializeNBT(player.registryAccess()), new CompoundTag(),
				ItemStack.EMPTY, rollback.target(),
				HemoJourneyStage.MORTAL_DISPLAY);
		ApplyResult applied = applyStateSafely(player, reset);
		if (!applied.success()) {
			return rollbackFailure(player, rollback, currentStage(data), "Journey reset failed: " + applied.message());
		}
		player.removeAllEffects();
		HemoJourneyWorldState.reset(player, data.getCompound(SNAPSHOT_KEY));
		data.putString(STAGE_KEY, HemoJourneyStage.MORTAL_DISPLAY.id());
		return new HemoJourneyResult(true, HemoJourneyStage.MORTAL_DISPLAY, "Reset player to the early Harbinger journey state.");
	}

	public static HemoJourneyResult restore(ServerPlayer player) {
		CompoundTag data = player.getPersistentData();
		if (!data.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
			return HemoJourneyResult.fail(currentStage(data), "No journey snapshot exists to restore.");
		}
		CompoundTag snapshot = data.getCompound(SNAPSHOT_KEY);
		PreflightResult preflight = preflightSnapshot(player, snapshot);
		if (!preflight.success()) return HemoJourneyResult.fail(currentStage(data), preflight.message());
		ParsedState rollback;
		try {
			rollback = captureLiveState(player);
		} catch (IllegalStateException exception) {
			return HemoJourneyResult.fail(currentStage(data), "Rollback capture failed: " + exception.getMessage());
		}
		ParsedState target = preflight.state();
		ApplyResult applied = applyStateSafely(player, target);
		if (!applied.success()) {
			return rollbackFailure(player, rollback, currentStage(data), "Journey restore failed: " + applied.message());
		}
		HemoJourneyWorldState.restore(player, snapshot);
		HemoJourneyStage restoredStage = target.stage();
		data.remove(SNAPSHOT_KEY);
		data.remove(STAGE_KEY);
		return new HemoJourneyResult(true, restoredStage, "Restored pre-journey player state.");
	}

	private static PreflightResult preflightSnapshot(ServerPlayer player, CompoundTag snapshot) {
		if (!snapshot.contains(BLOOD_ACTIVE, Tag.TAG_BYTE) || !snapshot.contains(BLOOD_CURRENT, Tag.TAG_ANY_NUMERIC)
				|| !snapshot.contains(BLOOD_MAX, Tag.TAG_ANY_NUMERIC) || !snapshot.contains(DEGREE, Tag.TAG_ANY_NUMERIC)
				|| !snapshot.contains(DEGREE_STATE, Tag.TAG_COMPOUND)
				|| !snapshot.contains(BLOOD_TENDENCY, Tag.TAG_COMPOUND)
				|| !snapshot.contains(SKILL_PROGRESS, Tag.TAG_COMPOUND)
				|| !snapshot.contains(LIBER_KNOWLEDGE, Tag.TAG_COMPOUND)
				|| !snapshot.contains(KNOWN_MANIPULATIONS, Tag.TAG_LIST)
				|| !snapshot.contains(SCAR_STATE, Tag.TAG_COMPOUND)
				|| !snapshot.contains(UNSTAINED_PROGRESS, Tag.TAG_COMPOUND)
				|| !snapshot.contains(KNOWN_STILL_ARTS, Tag.TAG_COMPOUND)
				|| !snapshot.contains(MUSCLE_MEMORY, Tag.TAG_COMPOUND)
				|| !snapshot.contains(LIVING_STAFF_PROGRESS, Tag.TAG_COMPOUND)
				|| !snapshot.contains(VASCULAR_SYSTEM, Tag.TAG_COMPOUND)
				|| !snapshot.contains(RECIPE_BOOK, Tag.TAG_COMPOUND)
				|| !snapshot.contains(SPECIMEN_BESTIARY, Tag.TAG_COMPOUND)
				|| !snapshot.contains(PERSISTENT_DATA, Tag.TAG_COMPOUND)
				|| !HemoJourneyWorldState.validSnapshot(snapshot)) {
			return PreflightResult.fail("Snapshot restore failed: missing or invalid blood state.");
		}
		double current = snapshot.getDouble(BLOOD_CURRENT);
		double max = snapshot.getDouble(BLOOD_MAX);
		int degree = snapshot.getInt(DEGREE);
		if (!Double.isFinite(current) || !Double.isFinite(max) || max < 0.0D || degree < 0 || degree > 8) {
			return PreflightResult.fail("Snapshot restore failed: blood or degree value is out of range.");
		}
		CompoundTag skillTag = snapshot.getCompound(SKILL_PROGRESS);
		if (!validateSkillProgressSchema(player, skillTag)) return PreflightResult.fail("Skill progress restore failed: malformed schema.");
		CompoundTag knowledgeTag = snapshot.getCompound(LIBER_KNOWLEDGE);
		if (!validateLiberKnowledgeSchema(player, knowledgeTag)) return PreflightResult.fail("Liber knowledge restore failed: malformed schema.");
		ListTag manipulationTag = snapshot.getList(KNOWN_MANIPULATIONS, Tag.TAG_COMPOUND);
		if (!validateKnownManipulationsSchema(player, manipulationTag)) return PreflightResult.fail("Known manipulation restore failed: malformed schema.");
		CompoundTag scarTag = snapshot.getCompound(SCAR_STATE);
		if (!validateScarStateSchema(player, scarTag)) return PreflightResult.fail("Scar state restore failed: malformed schema.");
		CompoundTag unstainedTag = snapshot.getCompound(UNSTAINED_PROGRESS);
		if (!validateUnstainedProgressSchema(player, unstainedTag)) return PreflightResult.fail("Unstained progress restore failed: malformed schema.");
		CompoundTag stillArtsTag = snapshot.getCompound(KNOWN_STILL_ARTS);
		if (!validateKnownStillArtsSchema(player, stillArtsTag)) return PreflightResult.fail("Known Still Arts restore failed: malformed schema.");
		CompoundTag muscleMemoryTag = snapshot.getCompound(MUSCLE_MEMORY);
		if (!validateMuscleMemorySchema(player, muscleMemoryTag)) return PreflightResult.fail("Muscle Memory restore failed: malformed schema.");
		CompoundTag livingStaffTag = snapshot.getCompound(LIVING_STAFF_PROGRESS);
		if (!validateLivingStaffProgressSchema(player, livingStaffTag)) return PreflightResult.fail("Living Staff progress restore failed: malformed schema.");
		CompoundTag vascularTag = snapshot.getCompound(VASCULAR_SYSTEM);
		CompoundTag recipeBookTag = snapshot.getCompound(RECIPE_BOOK);
		if (!validateRecipeBookSchema(player, recipeBookTag)) return PreflightResult.fail("Recipe book restore failed: malformed schema.");
		CompoundTag bestiaryTag = snapshot.getCompound(SPECIMEN_BESTIARY);
		if (!validateSpecimenBestiarySchema(player, bestiaryTag)) return PreflightResult.fail("Specimen Bestiary restore failed: malformed schema.");
		CompoundTag persistentDataTag = snapshot.getCompound(PERSISTENT_DATA);
		if (!validatePersistentDataSchema(persistentDataTag)) return PreflightResult.fail("Persistent journey data restore failed: malformed schema.");
		ListTag inventoryTag = snapshot.getList(INVENTORY, Tag.TAG_COMPOUND);
		if (inventoryTag.size() != player.getInventory().getContainerSize()) return PreflightResult.fail("Inventory restore failed: saved slot count does not match.");
		List<ItemStack> inventory = new ArrayList<>();
		for (int slot = 0; slot < inventoryTag.size(); slot++) {
			CompoundTag entry = inventoryTag.getCompound(slot);
			if (!entry.contains("slot", Tag.TAG_INT) || entry.getInt("slot") != slot || !entry.contains("stack")) return PreflightResult.fail("Inventory restore failed at slot " + slot + ": invalid entry.");
			ItemStack stack = parseStack(player, entry.get("stack"));
			if (stack == null) return PreflightResult.fail("Inventory restore failed at slot " + slot + ": ItemStack could not be parsed.");
			inventory.add(stack);
		}
		ItemStack vasc = parseStack(player, snapshot.get(VASC_EQUIPMENT));
		if (vasc == null) return PreflightResult.fail("VASC equipment restore failed: missing or unparseable stack.");
		String availabilityFailure = validateAdvancementAvailability(player);
		if (availabilityFailure != null) return PreflightResult.fail(availabilityFailure);
		if (!snapshot.contains(ADVANCEMENTS, Tag.TAG_COMPOUND)) return PreflightResult.fail("Advancement restore failed: snapshot map is missing.");
		CompoundTag advancementTag = snapshot.getCompound(ADVANCEMENTS);
		Map<ResourceLocation, Boolean> advancements = new LinkedHashMap<>();
		for (ResourceLocation id : JOURNEY_ADVANCEMENTS) {
			if (!advancementTag.contains(id.toString(), Tag.TAG_BYTE)) return PreflightResult.fail("Advancement restore failed: snapshot is missing " + id + ".");
			advancements.put(id, advancementTag.getBoolean(id.toString()));
		}
		String operationFailure = validateAdvancementOperations(player, advancements);
		if (operationFailure != null) return PreflightResult.fail(operationFailure);
		Target target = parseTarget(player, snapshot);
		if (target == null) return PreflightResult.fail("Origin restore failed: invalid dimension, position, or rotation.");
		if (!snapshot.contains(CURRENT_STAGE, Tag.TAG_STRING) || !isKnownStage(snapshot.getString(CURRENT_STAGE))) return PreflightResult.fail("Stage restore failed: saved stage is missing or invalid.");
		return PreflightResult.ok(new ParsedState(snapshot.getBoolean(BLOOD_ACTIVE), current, max, degree,
				snapshot.getCompound(DEGREE_STATE).copy(), snapshot.getCompound(BLOOD_TENDENCY).copy(),
				inventory, advancements, skillTag.copy(), knowledgeTag.copy(), manipulationTag.copy(), scarTag.copy(),
				unstainedTag.copy(), stillArtsTag.copy(), muscleMemoryTag.copy(), livingStaffTag.copy(), vascularTag.copy(), recipeBookTag.copy(), bestiaryTag.copy(),
				persistentDataTag.copy(), vasc, target,
				stageById(snapshot.getString(CURRENT_STAGE))));
	}

	private static ParsedState captureLiveState(ServerPlayer player) {
		if (!(HemoCapabilityAccess.requireLiberKnowledge(player) instanceof LiberKnowledge knowledge)) throw new IllegalStateException("unsupported Liber knowledge implementation");
		if (!(HemoCapabilityAccess.requireKnownManipulations(player) instanceof KnownManipulations manipulations)) throw new IllegalStateException("unsupported known manipulation implementation");
		if (!(HemoCapabilityAccess.requireScarState(player) instanceof ScarsContainer scars)) throw new IllegalStateException("unsupported scar state implementation");
		if (!(HemoCapabilityAccess.requireUnstainedProgress(player) instanceof UnstainedProgress unstained)) throw new IllegalStateException("unsupported Unstained progress implementation");
		if (!(HemoCapabilityAccess.requireKnownStillArts(player) instanceof KnownStillArts stillArts)) throw new IllegalStateException("unsupported Known Still Arts implementation");
		MuscleMemoryState muscleMemory = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
		List<ItemStack> inventory = new ArrayList<>();
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
		Map<ResourceLocation, Boolean> advancements = new LinkedHashMap<>();
		for (ResourceLocation id : JOURNEY_ADVANCEMENTS) {
			if (player.server.getAdvancements().get(id) == null) throw new IllegalStateException("missing advancement " + id);
			advancements.put(id, HarbingerAdvancementGranter.hasAdvancement(player, id));
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		Target target = new Target((ServerLevel) player.level(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		return new ParsedState(blood.isActive(), blood.getBloodVolume(), blood.getMaxBloodVolume(),
				HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber(),
				player.getData(HemoAttachmentTypes.INITIATORY_DEGREE).serializeNBT(player.registryAccess()),
				player.getData(HemoAttachmentTypes.BLOOD_TENDENCY).serializeNBT(player.registryAccess()), inventory, advancements,
				HemoCapabilityAccess.requireSkillProgress(player).serializeNBT(player.registryAccess()),
				knowledge.serializeNBT(player.registryAccess()), HemoJourneyManipulationState.capture(manipulations, player.registryAccess()),
				scars.serializeNBT(player.registryAccess()), unstained.serializeNBT(player.registryAccess()),
				stillArts.serializeNBT(player.registryAccess()),
				muscleMemory.serializeNBT(player.registryAccess()),
				player.getData(HemoAttachmentTypes.LIVING_STAFF_PROGRESS).serializeNBT(player.registryAccess()),
				player.getData(HemoAttachmentTypes.VASCULAR_SYSTEM).serializeNBT(player.registryAccess()),
				player.getRecipeBook().toNbt(),
				HemoCapabilityAccess.requireSpecimenBestiary(player).serializeNBT(player.registryAccess()),
				capturePersistentData(player),
				HemoCapabilityAccess.requireEquipment(player).getStackInSlot(VASC_SLOT).copy(),
				target, currentStage(player.getPersistentData()));
	}

	private static ApplyResult applyState(ServerPlayer player, ParsedState target) {
		var equipment = HemoCapabilityAccess.requireEquipment(player);
		boolean wasEventBlocked = equipment.isEventBlocked();
		equipment.setEventBlock(true);
		try {
			equipment.setStackInSlot(VASC_SLOT, target.vasc().copy());
		} finally {
			equipment.setEventBlock(wasEventBlocked);
		}
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		blood.setMaxBloodVolume(target.bloodMax());
		blood.setBloodVolume(target.bloodCurrent());
		blood.setActive(target.bloodActive());
		player.getData(HemoAttachmentTypes.INITIATORY_DEGREE).deserializeNBT(
				player.registryAccess(), target.degreeStateTag().copy());
		player.getData(HemoAttachmentTypes.BLOOD_TENDENCY).deserializeNBT(
				player.registryAccess(), target.bloodTendencyTag().copy());
		for (int slot = 0; slot < target.inventory().size(); slot++) player.getInventory().setItem(slot, target.inventory().get(slot).copy());
		for (Map.Entry<ResourceLocation, Boolean> entry : target.advancements().entrySet()) {
			String failure = setAdvancementState(player, entry.getKey(), entry.getValue());
			if (failure != null) return ApplyResult.fail(failure);
		}
		if (!(HemoCapabilityAccess.requireLiberKnowledge(player) instanceof LiberKnowledge knowledge)) return ApplyResult.fail("unsupported Liber knowledge implementation");
		if (!(HemoCapabilityAccess.requireKnownManipulations(player) instanceof KnownManipulations manipulations)) return ApplyResult.fail("unsupported known manipulation implementation");
		if (!(HemoCapabilityAccess.requireScarState(player) instanceof ScarsContainer scars)) return ApplyResult.fail("unsupported scar state implementation");
		if (!(HemoCapabilityAccess.requireUnstainedProgress(player) instanceof UnstainedProgress unstained)) return ApplyResult.fail("unsupported Unstained progress implementation");
		if (!(HemoCapabilityAccess.requireKnownStillArts(player) instanceof KnownStillArts stillArts)) return ApplyResult.fail("unsupported Known Still Arts implementation");
		HemoCapabilityAccess.requireSkillProgress(player).deserializeNBT(player.registryAccess(), target.skillTag().copy());
		knowledge.deserializeNBT(player.registryAccess(), target.knowledgeTag().copy());
		HemoJourneyManipulationState.apply(manipulations, target.manipulationTag(), player.registryAccess());
		scars.deserializeNBT(player.registryAccess(), target.scarTag().copy());
		unstained.deserializeNBT(player.registryAccess(), target.unstainedTag().copy());
		stillArts.deserializeNBT(player.registryAccess(), target.stillArtsTag().copy());
		player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).deserializeNBT(
				player.registryAccess(), target.muscleMemoryTag().copy());
		player.getData(HemoAttachmentTypes.LIVING_STAFF_PROGRESS).deserializeNBT(
				player.registryAccess(), target.livingStaffProgressTag().copy());
		player.getData(HemoAttachmentTypes.VASCULAR_SYSTEM).deserializeNBT(
				player.registryAccess(), target.vascularSystemTag().copy());
		ServerRecipeBook recipeBook = new ServerRecipeBook();
		recipeBook.fromNbt(target.recipeBookTag().copy(), player.server.getRecipeManager());
		player.getRecipeBook().copyOverData(recipeBook);
		player.getRecipeBook().sendInitialRecipeBook(player);
		var bestiary = HemoCapabilityAccess.requireSpecimenBestiary(player);
		bestiary.deserializeNBT(player.registryAccess(), target.bestiaryTag().copy());
		CompoundTag persistentData = player.getPersistentData();
		for (String key : JOURNEY_PERSISTENT_KEYS) persistentData.remove(key);
		for (String key : target.persistentDataTag().getAllKeys()) {
			persistentData.put(key, target.persistentDataTag().get(key).copy());
		}
		SkillPointGainEvents.syncSkills(player);
		LiberKnowledgeEvents.sync(player);
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(
				manipulations.getKnownManips(), manipulations.getSelectedManip(), manipulations.getVeinList(),
				manipulations.getSelectedVein(), manipulations.getActiveAvatarForm(), manipulations.getLastVeinMineStart(),
				new ArrayList<>(manipulations.getEquippedManipNames()), new ArrayList<>(manipulations.getLoadouts())));
		PacketHandler.sendToPlayer(player, new PacketSyncScarsState(player, scars));
		UnstainedProgressEvents.syncProgress(player, unstained);
		KnownStillArtEvents.sync(player, stillArts);
		MuscleMemoryEvents.sync(player);
		LivingStaffBondHelper.syncProgress(player);
		VascularSystemEvents.syncVascular(player, HemoCapabilityAccess.requireVascularSystem(player));
		InitiatoryDegreeEvents.syncDegree(player, HemoCapabilityAccess.requireInitiatoryDegree(player));
		BloodTendencyEvents.syncTendency(player, HemoCapabilityAccess.requireBloodTendency(player));
		SpecimenBestiaryEvents.sync(player, bestiary);
		Target destination = target.target();
		player.teleportTo(destination.level(), destination.x(), destination.y(), destination.z(), destination.yaw(), destination.pitch());
		if (!teleportMatches(player, target)) return ApplyResult.fail("teleport postcondition did not match target");
		return verifyAppliedState(player, target);
	}

	private static ApplyResult applyStateSafely(ServerPlayer player, ParsedState target) {
		try {
			return applyState(player, target);
		} catch (RuntimeException exception) {
			String message = exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
			return ApplyResult.fail("state application threw " + message);
		}
	}

	private static ApplyResult verifyAppliedState(ServerPlayer player, ParsedState target) {
		var blood = HemoCapabilityAccess.requireBloodVolume(player);
		if (blood.isActive() != target.bloodActive() || Double.compare(blood.getBloodVolume(), target.bloodCurrent()) != 0
				|| Double.compare(blood.getMaxBloodVolume(), target.bloodMax()) != 0
				|| HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() != target.degree()) return ApplyResult.fail("blood or degree postcondition mismatch");
		if (!player.getData(HemoAttachmentTypes.INITIATORY_DEGREE).serializeNBT(player.registryAccess())
				.equals(target.degreeStateTag())) return ApplyResult.fail("Initiatory Degree postcondition mismatch");
		if (!player.getData(HemoAttachmentTypes.BLOOD_TENDENCY).serializeNBT(player.registryAccess())
				.equals(target.bloodTendencyTag())) return ApplyResult.fail("Blood Tendency postcondition mismatch");
		for (int slot = 0; slot < target.inventory().size(); slot++) if (!sameStack(player.getInventory().getItem(slot), target.inventory().get(slot))) return ApplyResult.fail("inventory postcondition mismatch at slot " + slot);
		if (!sameStack(HemoCapabilityAccess.requireEquipment(player).getStackInSlot(VASC_SLOT), target.vasc())) return ApplyResult.fail("VASC equipment postcondition mismatch");
		for (Map.Entry<ResourceLocation, Boolean> entry : target.advancements().entrySet()) if (HarbingerAdvancementGranter.hasAdvancement(player, entry.getKey()) != entry.getValue()) return ApplyResult.fail("advancement postcondition mismatch for " + entry.getKey());
		if (!HemoCapabilityAccess.requireSkillProgress(player).serializeNBT(player.registryAccess()).equals(target.skillTag())) return ApplyResult.fail("skill progress postcondition mismatch");
		if (!(HemoCapabilityAccess.requireLiberKnowledge(player) instanceof LiberKnowledge knowledge)
				|| !knowledge.serializeNBT(player.registryAccess()).equals(target.knowledgeTag())) return ApplyResult.fail("Liber knowledge postcondition mismatch");
		if (!(HemoCapabilityAccess.requireKnownManipulations(player) instanceof KnownManipulations manipulations)
				|| !HemoJourneyManipulationState.matches(manipulations, target.manipulationTag(), player.registryAccess())) return ApplyResult.fail("Known manipulation postcondition mismatch");
		if (!(HemoCapabilityAccess.requireScarState(player) instanceof ScarsContainer scars)
				|| !scars.serializeNBT(player.registryAccess()).equals(target.scarTag())) return ApplyResult.fail("Scar state postcondition mismatch");
		if (!(HemoCapabilityAccess.requireUnstainedProgress(player) instanceof UnstainedProgress unstained)
				|| !unstained.serializeNBT(player.registryAccess()).equals(target.unstainedTag())) return ApplyResult.fail("Unstained progress postcondition mismatch");
		if (!(HemoCapabilityAccess.requireKnownStillArts(player) instanceof KnownStillArts stillArts)
				|| !stillArts.serializeNBT(player.registryAccess()).equals(target.stillArtsTag())) return ApplyResult.fail("Known Still Arts postcondition mismatch");
		if (!player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).serializeNBT(player.registryAccess())
				.equals(target.muscleMemoryTag())) return ApplyResult.fail("Muscle Memory postcondition mismatch");
		if (!player.getData(HemoAttachmentTypes.LIVING_STAFF_PROGRESS).serializeNBT(player.registryAccess())
				.equals(target.livingStaffProgressTag())) return ApplyResult.fail("Living Staff progress postcondition mismatch");
		if (!player.getData(HemoAttachmentTypes.VASCULAR_SYSTEM).serializeNBT(player.registryAccess())
				.equals(target.vascularSystemTag())) return ApplyResult.fail("Vascular system postcondition mismatch");
		if (!player.getRecipeBook().toNbt().equals(target.recipeBookTag())) return ApplyResult.fail("Recipe book postcondition mismatch");
		if (!HemoCapabilityAccess.requireSpecimenBestiary(player).serializeNBT(player.registryAccess())
				.equals(target.bestiaryTag())) return ApplyResult.fail("Specimen Bestiary postcondition mismatch");
		if (!capturePersistentData(player).equals(target.persistentDataTag())) return ApplyResult.fail("Persistent journey data postcondition mismatch");
		return ApplyResult.ok();
	}

	private static HemoJourneyResult rollbackFailure(ServerPlayer player, ParsedState rollback,
			HemoJourneyStage stage, String originalFailure) {
		ApplyResult rollbackResult = rollbackLiveState(player, rollback);
		String message = originalFailure;
		if (!rollbackResult.success()) message += " Rollback also failed: " + rollbackResult.message();
		else message += " Live player state was rolled back; durable snapshot retained.";
		return HemoJourneyResult.fail(stage, message);
	}

	private static ApplyResult rollbackLiveState(ServerPlayer player, ParsedState rollback) {
		return applyStateSafely(player, rollback);
	}

	private static String setAdvancementState(ServerPlayer player, ResourceLocation id, boolean targetComplete) {
		AdvancementHolder advancement = player.server.getAdvancements().get(id);
		if (advancement == null) return "Advancement state failed: missing " + id + ".";
		AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
		if (!targetComplete) {
			List<String> completed = new ArrayList<>();
			progress.getCompletedCriteria().forEach(completed::add);
			for (String criterion : completed) {
				if (!player.getAdvancements().revoke(advancement, criterion)) return "Advancement revoke failed for " + id + " criterion " + criterion + ".";
			}
		} else {
			HarbingerAdvancementGranter.grantIfNotDone(player, id);
		}
		if (progress.isDone() != targetComplete) return "Advancement restore failed after applying " + id + ".";
		return null;
	}

	private static String validateAdvancementAvailability(ServerPlayer player) {
		for (ResourceLocation id : JOURNEY_ADVANCEMENTS) if (player.server.getAdvancements().get(id) == null) return "Advancement preflight failed: missing " + id + ".";
		return null;
	}

	private static String validateAdvancementOperations(ServerPlayer player,
			Map<ResourceLocation, Boolean> targets) {
		for (Map.Entry<ResourceLocation, Boolean> entry : targets.entrySet()) {
			AdvancementHolder holder = player.server.getAdvancements().get(entry.getKey());
			if (holder == null) return "Advancement preflight failed: missing " + entry.getKey() + ".";
			AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
			if (entry.getValue() && !progress.isDone() && !progress.getRemainingCriteria().iterator().hasNext()) {
				return "Advancement preflight failed: no awardable criteria for " + entry.getKey() + ".";
			}
			if (!entry.getValue() && progress.isDone() && !progress.getCompletedCriteria().iterator().hasNext()) {
				return "Advancement preflight failed: no revocable criteria for " + entry.getKey() + ".";
			}
		}
		return null;
	}

	private static boolean validateSkillProgressSchema(ServerPlayer player, CompoundTag tag) {
		if (!tag.contains("skills", Tag.TAG_LIST)) return false;
		ListTag skills = tag.getList("skills", Tag.TAG_COMPOUND);
		boolean metaFound = false;
		for (Tag value : skills) {
			if (!(value instanceof CompoundTag entry) || !entry.contains("name", Tag.TAG_STRING)) return false;
			if ("__meta__".equals(entry.getString("name"))) {
				metaFound = entry.contains("skillPoints", Tag.TAG_ANY_NUMERIC)
						&& entry.contains("totalManipulationUses", Tag.TAG_ANY_NUMERIC)
						&& entry.contains("totalKillsWithBlood", Tag.TAG_ANY_NUMERIC)
						&& entry.contains("totalRitesCompleted", Tag.TAG_ANY_NUMERIC)
						&& entry.contains("totalHemoAdvancements", Tag.TAG_ANY_NUMERIC)
						&& entry.contains("completedMilestones", Tag.TAG_LIST);
			}
		}
		if (!metaFound) return false;
		var temporary = new com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress();
		temporary.deserializeNBT(player.registryAccess(), tag.copy());
		return temporary.serializeNBT(player.registryAccess()).equals(tag);
	}

	private static boolean validateLiberKnowledgeSchema(ServerPlayer player, CompoundTag tag) {
		if (!tag.contains("UnlockedEntries", Tag.TAG_LIST) || !tag.contains("KnownMemos", Tag.TAG_LIST)
				|| !tag.contains("EntrySources", Tag.TAG_COMPOUND) || !tag.contains("PendingMemos", Tag.TAG_LIST)) return false;
		if (!validResourceList(tag.getList("UnlockedEntries", Tag.TAG_STRING))
				|| !validResourceList(tag.getList("KnownMemos", Tag.TAG_STRING))
				|| !validResourceList(tag.getList("PendingMemos", Tag.TAG_STRING))) return false;
		CompoundTag sources = tag.getCompound("EntrySources");
		for (String key : sources.getAllKeys()) if (ResourceLocation.tryParse(key) == null || !sources.contains(key, Tag.TAG_LIST)) return false;
		LiberKnowledge temporary = new LiberKnowledge();
		temporary.deserializeNBT(player.registryAccess(), tag.copy());
		return temporary.serializeNBT(player.registryAccess()).equals(tag);
	}

	private static boolean validateKnownManipulationsSchema(ServerPlayer player, ListTag tag) {
		if (tag.isEmpty()) return false;
		try {
			KnownManipulations temporary = new KnownManipulations();
			HemoJourneyManipulationState.apply(temporary, tag, player.registryAccess());
			return HemoJourneyManipulationState.matches(temporary, tag, player.registryAccess());
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static boolean validateScarStateSchema(ServerPlayer player, CompoundTag tag) {
		try {
			ScarsContainer temporary = new ScarsContainer();
			temporary.deserializeNBT(player.registryAccess(), tag.copy());
			return temporary.serializeNBT(player.registryAccess()).equals(tag);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static boolean validateUnstainedProgressSchema(ServerPlayer player, CompoundTag tag) {
		try {
			UnstainedProgress temporary = new UnstainedProgress();
			temporary.deserializeNBT(player.registryAccess(), tag.copy());
			return temporary.serializeNBT(player.registryAccess()).equals(tag);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static boolean validateKnownStillArtsSchema(ServerPlayer player, CompoundTag tag) {
		try {
			KnownStillArts temporary = new KnownStillArts();
			temporary.deserializeNBT(player.registryAccess(), tag.copy());
			return temporary.serializeNBT(player.registryAccess()).equals(tag);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static boolean validateMuscleMemorySchema(ServerPlayer player, CompoundTag tag) {
		try {
			MuscleMemoryState temporary = new MuscleMemoryState();
			temporary.deserializeNBT(player.registryAccess(), tag.copy());
			return temporary.serializeNBT(player.registryAccess()).equals(tag);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static boolean validateLivingStaffProgressSchema(ServerPlayer player, CompoundTag tag) {
		try {
			LivingStaffProgress temporary = new LivingStaffProgress();
			temporary.deserializeNBT(player.registryAccess(), tag.copy());
			return temporary.serializeNBT(player.registryAccess()).equals(tag);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static boolean validateRecipeBookSchema(ServerPlayer player, CompoundTag tag) {
		try {
			ServerRecipeBook temporary = new ServerRecipeBook();
			temporary.fromNbt(tag.copy(), player.server.getRecipeManager());
			return temporary.toNbt().equals(tag);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static boolean validateSpecimenBestiarySchema(ServerPlayer player, CompoundTag tag) {
		try {
			SpecimenBestiaryProgress temporary = new SpecimenBestiaryProgress();
			temporary.deserializeNBT(player.registryAccess(), tag.copy());
			return temporary.serializeNBT(player.registryAccess()).equals(tag);
		} catch (RuntimeException exception) {
			return false;
		}
	}

	private static CompoundTag capturePersistentData(ServerPlayer player) {
		CompoundTag captured = new CompoundTag();
		CompoundTag data = player.getPersistentData();
		for (String key : JOURNEY_PERSISTENT_KEYS) if (data.contains(key)) captured.put(key, data.get(key).copy());
		return captured;
	}

	private static boolean validatePersistentDataSchema(CompoundTag tag) {
		for (String key : tag.getAllKeys()) {
			if (!JOURNEY_PERSISTENT_KEYS.contains(key)
					|| !tag.contains(key, Tag.TAG_BYTE) && !tag.contains(key, Tag.TAG_STRING)) return false;
		}
		return true;
	}

	private static boolean validResourceList(ListTag values) {
		for (Tag value : values) if (ResourceLocation.tryParse(value.getAsString()) == null) return false;
		return true;
	}

	private static ItemStack parseStack(ServerPlayer player, Tag saved) {
		if (saved instanceof CompoundTag compound && compound.isEmpty()) return ItemStack.EMPTY;
		return saved == null ? null : ItemStack.parse(player.registryAccess(), saved).orElse(null);
	}

	private static Target parseTarget(ServerPlayer player, CompoundTag snapshot) {
		if (!snapshot.contains(ORIGIN_DIMENSION, Tag.TAG_STRING) || !snapshot.contains(ORIGIN_POSITION, Tag.TAG_COMPOUND)) return null;
		ResourceLocation id = ResourceLocation.tryParse(snapshot.getString(ORIGIN_DIMENSION));
		ServerLevel level = id == null ? null : player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
		CompoundTag pos = snapshot.getCompound(ORIGIN_POSITION);
		if (level == null || !pos.contains("x", Tag.TAG_ANY_NUMERIC) || !pos.contains("y", Tag.TAG_ANY_NUMERIC)
				|| !pos.contains("z", Tag.TAG_ANY_NUMERIC) || !pos.contains("yaw", Tag.TAG_ANY_NUMERIC)
				|| !pos.contains("pitch", Tag.TAG_ANY_NUMERIC)) return null;
		double x = pos.getDouble("x"), y = pos.getDouble("y"), z = pos.getDouble("z");
		float yaw = pos.getFloat("yaw"), pitch = pos.getFloat("pitch");
		if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || !Float.isFinite(yaw) || !Float.isFinite(pitch)) return null;
		BlockPos blockPos = BlockPos.containing(x, y, z);
		if (level.isOutsideBuildHeight(blockPos) || !level.getWorldBorder().isWithinBounds(blockPos)) return null;
		return new Target(level, x, y, z, yaw, pitch);
	}

	private static boolean teleportMatches(ServerPlayer player, ParsedState target) {
		Target destination = target.target();
		return player.level().dimension().equals(destination.level().dimension())
				&& player.distanceToSqr(destination.x(), destination.y(), destination.z()) <= 1.0E-6D
				&& Math.abs(net.minecraft.util.Mth.wrapDegrees(player.getYRot() - destination.yaw())) <= 0.01F
				&& Math.abs(player.getXRot() - destination.pitch()) <= 0.01F;
	}

	private static boolean sameStack(ItemStack left, ItemStack right) {
		return left.getCount() == right.getCount() && ItemStack.isSameItemSameComponents(left, right);
	}

	private static HemoJourneyStage currentStage(CompoundTag data) {
		return stageById(data.getString(STAGE_KEY));
	}

	private static HemoJourneyStage stageById(String id) {
		for (HemoJourneyStage stage : HemoJourneyStage.values()) {
			if (stage.id().equals(id)) {
				return stage;
			}
		}
		return HemoJourneyStage.MORTAL_DISPLAY;
	}

	private static boolean isKnownStage(String id) {
		for (HemoJourneyStage stage : HemoJourneyStage.values()) {
			if (stage.id().equals(id)) {
				return true;
			}
		}
		return false;
	}

	private record Target(ServerLevel level, double x, double y, double z, float yaw, float pitch) {
	}

	private record ParsedState(boolean bloodActive, double bloodCurrent, double bloodMax, int degree,
			CompoundTag degreeStateTag, CompoundTag bloodTendencyTag,
			List<ItemStack> inventory, Map<ResourceLocation, Boolean> advancements, CompoundTag skillTag,
			CompoundTag knowledgeTag, ListTag manipulationTag, CompoundTag scarTag, CompoundTag unstainedTag,
			CompoundTag stillArtsTag,
			CompoundTag muscleMemoryTag, CompoundTag livingStaffProgressTag, CompoundTag vascularSystemTag,
			CompoundTag recipeBookTag, CompoundTag bestiaryTag,
			CompoundTag persistentDataTag, ItemStack vasc,
			Target target, HemoJourneyStage stage) {
	}

	private record ApplyResult(boolean success, String message) {
		private static ApplyResult ok() { return new ApplyResult(true, ""); }
		private static ApplyResult fail(String message) { return new ApplyResult(false, message); }
	}

	private record PreflightResult(boolean success, ParsedState state, String message) {
		private static PreflightResult ok(ParsedState state) { return new PreflightResult(true, state, ""); }
		private static PreflightResult fail(String message) { return new PreflightResult(false, null, message); }
	}
}
