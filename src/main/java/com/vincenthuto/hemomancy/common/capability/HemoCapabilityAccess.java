package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.VisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummons;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArts;
import com.vincenthuto.hemomancy.common.capability.player.unstained.white_humor.WhiteHumorVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentContainer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipment;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.IEquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.rite.IchorianKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScarItem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarsContainer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.IKnownSummons;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.will.WillAmbushState;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.IKnownStillArts;
import com.vincenthuto.hemomancy.common.capability.player.unstained.white_humor.IWhiteHumorVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.LastDeathMemory;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.IWhiteHumorTile;
import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

/**
 * The only door to Hemomancy attachments. Every player carries every player attachment, so the {@code Player}
 * overloads return the attachment itself, typed as its own class. The {@code Entity}, {@code BlockEntity} and {@code ItemStack} overloads
 * stay {@code Optional}: most of those holders carry nothing.
 */
public final class HemoCapabilityAccess {
public static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch antecedent(Player player) { return player.getData(HemoAttachmentTypes.ANTECEDENT); }
public static com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress clinicalBlood(Player player) {
    return player.getData(HemoAttachmentTypes.CLINICAL_BLOOD);
}
public static com.vincenthuto.hemomancy.common.station.StationUpgradeProgress stationUpgrades(Player player) {
    return player.getData(HemoAttachmentTypes.STATION_UPGRADES);
}

public static MuscleMemoryState muscleMemory(Player player) {
    return player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
}

public static com.vincenthuto.hemomancy.common.capability.player.shared.migration.PlayerDataVersion dataVersion(Player player) {
    return player.getData(HemoAttachmentTypes.DATA_VERSION);
}

public static com.vincenthuto.hemomancy.common.circus.CircusProgress circusProgress(Player player) {
    return player.getData(HemoAttachmentTypes.CIRCUS_PROGRESS);
}

public static com.vincenthuto.hemomancy.common.encounter.ChamberAccess chamberAccess(Player player) {
    return player.getData(HemoAttachmentTypes.CHAMBER_ACCESS);
}

public static com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhisperInbox pendingWhispers(Player player) {
    return player.getData(HemoAttachmentTypes.PENDING_WHISPERS);
}

public static com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns cooldowns(Player player) {
    return player.getData(HemoAttachmentTypes.COOLDOWNS);
}

public static DialogueKnowledge dialogueKnowledge(Player player) {
    return player.getData(HemoAttachmentTypes.DIALOGUE_KNOWLEDGE);
}


private HemoCapabilityAccess() {}

public static com.vincenthuto.hemomancy.common.damage.SchoolCombatState getSchoolCombat(Entity entity) {
    return entity.getData(HemoAttachmentTypes.SCHOOL_COMBAT);
}

// ── Blood Volume ──────────────────────────────────────────────────────────

public static BloodVolume getBloodVolume(Player player) {
    return player.getData(HemoAttachmentTypes.BLOOD_VOLUME);
}

public static Optional<IBloodVolume> getBloodVolume(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.BLOOD_VOLUME));
}

public static Optional<IBloodVolume> getBloodVolume(BlockEntity be) {
    if (be instanceof IBloodReservoir) {
        return Optional.ofNullable(be.getData(HemoAttachmentTypes.BLOCK_BLOOD_VOLUME));
    }
    return Optional.empty();
}

public static Optional<IBloodVolume> getBloodVolume(ItemStack stack) {
    return Optional.ofNullable(stack.getCapability(HemoCapabilityKeys.ITEM_BLOOD_VOLUME));
}

public static PowerGuardrailState getPowerGuardrails(Player player) {
    return player.getData(HemoAttachmentTypes.POWER_GUARDRAILS);
}

public static WillAmbushState getWillAmbushState(Player player) {
    return player.getData(HemoAttachmentTypes.WILL_AMBUSH_STATE);
}

// ── Blood Tendency ────────────────────────────────────────────────────────

public static BloodTendency getBloodTendency(Player player) {
    return player.getData(HemoAttachmentTypes.BLOOD_TENDENCY);
}

public static Optional<IBloodTendency> getBloodTendency(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.BLOOD_TENDENCY));
}

public static Optional<IBloodTendency> getBloodTendency(BlockEntity be) {
    return Optional.of(be.getData(HemoAttachmentTypes.BLOCK_BLOOD_TENDENCY));
}

// ── Vascular System ───────────────────────────────────────────────────────

public static VascularSystem getVascularSystem(Player player) {
    return player.getData(HemoAttachmentTypes.VASCULAR_SYSTEM);
}

public static Optional<IVascularSystem> getVascularSystem(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.VASCULAR_SYSTEM));
}

// ── Known Manipulations ───────────────────────────────────────────────────

public static KnownManipulations getKnownManipulations(Player player) {
    return player.getData(HemoAttachmentTypes.KNOWN_MANIPULATIONS);
}

public static Optional<IKnownManipulations> getKnownManipulations(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.KNOWN_MANIPULATIONS));
}

public static LivingStaffProgress getLivingStaffProgress(Player player) {
    return player.getData(HemoAttachmentTypes.LIVING_STAFF_PROGRESS);
}

public static Optional<ILivingStaffProgress> getLivingStaffProgress(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.LIVING_STAFF_PROGRESS));
}

public static SpecimenBestiaryProgress getSpecimenBestiary(Player player) {
    return player.getData(HemoAttachmentTypes.SPECIMEN_BESTIARY);
}

public static Optional<SpecimenBestiaryProgress> getSpecimenBestiary(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.SPECIMEN_BESTIARY));
}

public static KnownStillArts getKnownStillArts(Player player) {
    return player.getData(HemoAttachmentTypes.KNOWN_STILL_ARTS);
}

public static Optional<IKnownStillArts> getKnownStillArts(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.KNOWN_STILL_ARTS));
}

public static KnownSummons getKnownSummons(Player player) {
    return player.getData(HemoAttachmentTypes.KNOWN_SUMMONS);
}

public static Optional<IKnownSummons> getKnownSummons(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.KNOWN_SUMMONS));
}

public static LiberKnowledge getLiberKnowledge(Player player) {
    return player.getData(HemoAttachmentTypes.LIBER_KNOWLEDGE);
}

public static Optional<IBookKnowledge> getLiberKnowledge(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.LIBER_KNOWLEDGE));
}

public static IchorianKnowledge getIchorianKnowledge(Player player) {
    return player.getData(HemoAttachmentTypes.ICHORIAN_KNOWLEDGE);
}

public static Optional<IchorianKnowledge> getIchorianKnowledge(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.ICHORIAN_KNOWLEDGE));
}

public static SkillProgress getSkillProgress(Player player) {
    return player.getData(HemoAttachmentTypes.SKILL_PROGRESS);
}

public static Optional<SkillProgress> getSkillProgress(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.SKILL_PROGRESS));
}

// ── Initiatory Degree ─────────────────────────────────────────────────────

public static InitiatoryDegree getInitiatoryDegree(Player player) {
    return player.getData(HemoAttachmentTypes.INITIATORY_DEGREE);
}

public static Optional<IInitiatoryDegree> getInitiatoryDegree(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.INITIATORY_DEGREE));
}

/** Convenience helper replacing InitiatoryDegreeProvider.getPlayerDegreeNumber. */
public static int getPlayerDegreeNumber(Player player) {
    return getInitiatoryDegree(player).getDegreeNumber();
}

// ── Equipped Morphling ────────────────────────────────────────────────────

public static EquippedMorphling getEquippedMorphling(Player player) {
    return player.getData(HemoAttachmentTypes.EQUIPPED_MORPHLING);
}

public static Optional<IEquippedMorphling> getEquippedMorphling(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.EQUIPPED_MORPHLING));
}

// ── Unstained Progress ────────────────────────────────────────────────────

public static UnstainedProgress getUnstainedProgress(Player player) {
    return player.getData(HemoAttachmentTypes.UNSTAINED_PROGRESS);
}

public static Optional<IUnstainedProgress> getUnstainedProgress(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.UNSTAINED_PROGRESS));
}

// ── White Humor Volume ────────────────────────────────────────────────────

public static Optional<IWhiteHumorVolume> getWhiteHumorVolume(BlockEntity be) {
    if (be instanceof IWhiteHumorTile) {
        return Optional.of(be.getData(HemoAttachmentTypes.BLOCK_WHITE_HUMOR_VOLUME));
    }
    return Optional.empty();
}

public static WhiteHumorVolume getWhiteHumorVolume(Player player) {
    return player.getData(HemoAttachmentTypes.WHITE_HUMOR_VOLUME);
}

public static Optional<IWhiteHumorVolume> getWhiteHumorVolume(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.WHITE_HUMOR_VOLUME));
}

// ── Visceral Organs ───────────────────────────────────────────────────────

public static VisceralOrgans getVisceralOrgans(Player player) {
    return player.getData(HemoAttachmentTypes.VISCERAL_ORGANS);
}

public static Optional<IVisceralOrgans> getVisceralOrgans(Entity entity) {
    return Optional.ofNullable(entity.getCapability(HemoCapabilityKeys.VISCERAL_ORGANS));
}

// ── Scars ─────────────────────────────────────────────────────────────────

public static HarbingerEquipmentContainer getEquipment(Player player) {
    HarbingerEquipmentContainer equipment = player.getData(HemoAttachmentTypes.HARBINGER_EQUIPMENT);
    equipment.bindHolder(player);
    return equipment;
}

public static Optional<IHarbingerEquipment> getScar(ItemStack stack) {
    return Optional.ofNullable(stack.getCapability(HemoCapabilityKeys.ITEM_HARBINGER_EQUIPMENT));
}

public static ScarsContainer getScarState(Player player) {
    ScarsContainer scars = player.getData(HemoAttachmentTypes.SCARS);
    scars.bindHolder(player);
    return scars;
}

public static Optional<IScarItem> getScarItem(ItemStack stack) {
    return Optional.ofNullable(stack.getCapability(HemoCapabilityKeys.ITEM_SCAR));
}

public static LastDeathMemory getLastDeathMemory(Player player) {
    return player.getData(HemoAttachmentTypes.LAST_DEATH_MEMORY);
}
}
