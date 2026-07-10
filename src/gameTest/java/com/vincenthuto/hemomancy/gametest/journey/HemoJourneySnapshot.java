package com.vincenthuto.hemomancy.gametest.journey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointGainEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.FirstBloodcraftAssignmentHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;

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
import net.minecraft.world.item.ItemStack;

/** Development-only ownership boundary for player state changed by the journey. */
public final class HemoJourneySnapshot {
	public static final String SNAPSHOT_KEY = "hemomancy.dev_test.journey.snapshot";
	public static final String STAGE_KEY = "hemomancy.dev_test.journey.stage";

	private static final String BLOOD_ACTIVE = "blood_active";
	private static final String BLOOD_CURRENT = "blood_current";
	private static final String BLOOD_MAX = "blood_max";
	private static final String DEGREE = "degree";
	private static final String INVENTORY = "inventory";
	private static final String ADVANCEMENTS = "advancements";
	private static final String ORIGIN_DIMENSION = "origin_dimension";
	private static final String ORIGIN_POSITION = "origin_position";
	private static final String CURRENT_STAGE = "current_stage";
	private static final String SKILL_PROGRESS = "skill_progress";
	private static final String LIBER_KNOWLEDGE = "liber_knowledge";
	private static final String KNOWN_MANIPULATIONS = "known_manipulations";
	private static final String VASC_EQUIPMENT = "vasc_equipment";
	private static final int VASC_SLOT = 5;

	/*
	 * Exact journey advancement ownership (six real checkpoints): First Awakening,
	 * Degree 1, Vessel Filled, Liber crafted (Fane Sanguinium), Hematic Iron
	 * crafted, and reward claimed. Formation projection has no advancement of its
	 * own; Founding Fane Established is unrelated later progression and is excluded.
	 */
	private static final List<ResourceLocation> JOURNEY_ADVANCEMENTS = List.of(
			Hemomancy.rloc("hemomancy/the_first_awakening"),
			Hemomancy.rloc("hemomancy/degree_1_neophyte"),
			Hemomancy.rloc("hemomancy/vessel_filled"),
			Hemomancy.rloc("hemomancy/fane_sanguinium"),
			Hemomancy.rloc("hemomancy/iron_in_the_blood"),
			FirstBloodcraftAssignmentHelper.ADV_REWARD_CLAIMED);

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
		ParsedState reset = new ParsedState(false, 0.0D, 5000.0D, 0, emptyInventory,
				resetAdvancements, emptySkills.serializeNBT(player.registryAccess()),
				emptyKnowledge.serializeNBT(player.registryAccess()),
				HemoJourneyManipulationState.capture(emptyManipulations, player.registryAccess()), ItemStack.EMPTY, rollback.target(),
				HemoJourneyStage.MORTAL_DISPLAY);
		ApplyResult applied = applyStateSafely(player, reset);
		if (!applied.success()) {
			return rollbackFailure(player, rollback, currentStage(data), "Journey reset failed: " + applied.message());
		}
		player.removeAllEffects();
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
		HemoJourneyStage restoredStage = target.stage();
		data.remove(SNAPSHOT_KEY);
		data.remove(STAGE_KEY);
		return new HemoJourneyResult(true, restoredStage, "Restored pre-journey player state.");
	}

	private static PreflightResult preflightSnapshot(ServerPlayer player, CompoundTag snapshot) {
		if (!snapshot.contains(BLOOD_ACTIVE, Tag.TAG_BYTE) || !snapshot.contains(BLOOD_CURRENT, Tag.TAG_ANY_NUMERIC)
				|| !snapshot.contains(BLOOD_MAX, Tag.TAG_ANY_NUMERIC) || !snapshot.contains(DEGREE, Tag.TAG_ANY_NUMERIC)
				|| !snapshot.contains(SKILL_PROGRESS, Tag.TAG_COMPOUND)
				|| !snapshot.contains(LIBER_KNOWLEDGE, Tag.TAG_COMPOUND)
				|| !snapshot.contains(KNOWN_MANIPULATIONS, Tag.TAG_LIST)) {
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
				inventory, advancements, skillTag.copy(), knowledgeTag.copy(), manipulationTag.copy(), vasc, target,
				stageById(snapshot.getString(CURRENT_STAGE))));
	}

	private static ParsedState captureLiveState(ServerPlayer player) {
		if (!(HemoCapabilityAccess.requireLiberKnowledge(player) instanceof LiberKnowledge knowledge)) throw new IllegalStateException("unsupported Liber knowledge implementation");
		if (!(HemoCapabilityAccess.requireKnownManipulations(player) instanceof KnownManipulations manipulations)) throw new IllegalStateException("unsupported known manipulation implementation");
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
				HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber(), inventory, advancements,
				HemoCapabilityAccess.requireSkillProgress(player).serializeNBT(player.registryAccess()),
				knowledge.serializeNBT(player.registryAccess()), HemoJourneyManipulationState.capture(manipulations, player.registryAccess()),
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
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(target.degree());
		for (int slot = 0; slot < target.inventory().size(); slot++) player.getInventory().setItem(slot, target.inventory().get(slot).copy());
		for (Map.Entry<ResourceLocation, Boolean> entry : target.advancements().entrySet()) {
			String failure = setAdvancementState(player, entry.getKey(), entry.getValue());
			if (failure != null) return ApplyResult.fail(failure);
		}
		if (!(HemoCapabilityAccess.requireLiberKnowledge(player) instanceof LiberKnowledge knowledge)) return ApplyResult.fail("unsupported Liber knowledge implementation");
		if (!(HemoCapabilityAccess.requireKnownManipulations(player) instanceof KnownManipulations manipulations)) return ApplyResult.fail("unsupported known manipulation implementation");
		HemoCapabilityAccess.requireSkillProgress(player).deserializeNBT(player.registryAccess(), target.skillTag().copy());
		knowledge.deserializeNBT(player.registryAccess(), target.knowledgeTag().copy());
		HemoJourneyManipulationState.apply(manipulations, target.manipulationTag(), player.registryAccess());
		SkillPointGainEvents.syncSkills(player);
		LiberKnowledgeEvents.sync(player);
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(
				manipulations.getKnownManips(), manipulations.getSelectedManip(), manipulations.getVeinList(),
				manipulations.getSelectedVein(), manipulations.isAvatarActive(), manipulations.getLastVeinMineStart(),
				new ArrayList<>(manipulations.getEquippedManipNames()), new ArrayList<>(manipulations.getLoadouts())));
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
		for (int slot = 0; slot < target.inventory().size(); slot++) if (!sameStack(player.getInventory().getItem(slot), target.inventory().get(slot))) return ApplyResult.fail("inventory postcondition mismatch at slot " + slot);
		if (!sameStack(HemoCapabilityAccess.requireEquipment(player).getStackInSlot(VASC_SLOT), target.vasc())) return ApplyResult.fail("VASC equipment postcondition mismatch");
		for (Map.Entry<ResourceLocation, Boolean> entry : target.advancements().entrySet()) if (HarbingerAdvancementGranter.hasAdvancement(player, entry.getKey()) != entry.getValue()) return ApplyResult.fail("advancement postcondition mismatch for " + entry.getKey());
		if (!HemoCapabilityAccess.requireSkillProgress(player).serializeNBT(player.registryAccess()).equals(target.skillTag())) return ApplyResult.fail("skill progress postcondition mismatch");
		if (!(HemoCapabilityAccess.requireLiberKnowledge(player) instanceof LiberKnowledge knowledge)
				|| !knowledge.serializeNBT(player.registryAccess()).equals(target.knowledgeTag())) return ApplyResult.fail("Liber knowledge postcondition mismatch");
		if (!(HemoCapabilityAccess.requireKnownManipulations(player) instanceof KnownManipulations manipulations)
				|| !HemoJourneyManipulationState.matches(manipulations, target.manipulationTag(), player.registryAccess())) return ApplyResult.fail("Known manipulation postcondition mismatch");
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
			List<ItemStack> inventory, Map<ResourceLocation, Boolean> advancements, CompoundTag skillTag,
			CompoundTag knowledgeTag, ListTag manipulationTag, ItemStack vasc, Target target, HemoJourneyStage stage) {
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
