package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.Possession;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoad;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentContainer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryState;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.VisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.rite.IchorianKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarsContainer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummons;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.will.WillAmbushState;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.migration.PlayerDataVersion;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArts;
import com.vincenthuto.hemomancy.common.capability.player.unstained.white_humor.WhiteHumorVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.memory.LastDeathMemoryAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class HemoAttachmentTypes {
    private HemoAttachmentTypes() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Hemomancy.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<java.util.List<net.minecraft.world.level.levelgen.structure.BoundingBox>>> PHLEGETHONTIC_PENDING_VEIN =
            ATTACHMENT_TYPES.register("phlegethontic_pending_vein", () -> AttachmentType
                    .<java.util.List<net.minecraft.world.level.levelgen.structure.BoundingBox>>builder(() -> java.util.List.of())
                    .serialize(net.minecraft.world.level.levelgen.structure.BoundingBox.CODEC.listOf()).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.worldgen.terrain.phlegethontic.PhlegethonticOreReservations>> PHLEGETHONTIC_ORE_RESERVATIONS =
            ATTACHMENT_TYPES.register("phlegethontic_ore_reservations", () -> AttachmentType
                    .serializable(com.vincenthuto.hemomancy.common.worldgen.terrain.phlegethontic.PhlegethonticOreReservations::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.damage.SchoolCombatState>> SCHOOL_COMBAT =
            ATTACHMENT_TYPES.register("school_combat",
                    () -> AttachmentType.serializable(com.vincenthuto.hemomancy.common.damage.SchoolCombatState::new).build());

    // ── Player attachments (copyOnDeath so data survives death/respawn) ──
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentResearch>> ANTECEDENT = ATTACHMENT_TYPES.register("antecedent", () -> AttachmentType.serializable(com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentResearch::new).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress>> CLINICAL_BLOOD =
            ATTACHMENT_TYPES.register("clinical_blood", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress::new).copyOnDeath().build());

    // Legacy: ADVANCED_BREWING and RESONANT_FORGE are read once by StationUpgradeMigration, then removed from the player.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.brewing.AdvancedBrewingProgress>> ADVANCED_BREWING =
            ATTACHMENT_TYPES.register("advanced_brewing", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.brewing.AdvancedBrewingProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.enchanting.ResonantForgeProgress>> RESONANT_FORGE =
            ATTACHMENT_TYPES.register("resonant_forge", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.enchanting.ResonantForgeProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.capability.player.harbinger.station.StationUpgradeProgress>> STATION_UPGRADES =
            ATTACHMENT_TYPES.register("station_upgrades", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.capability.player.harbinger.station.StationUpgradeProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BloodVolume>> BLOOD_VOLUME =
            ATTACHMENT_TYPES.register("blood_volume",
                    () -> AttachmentType.serializable(BloodVolume::new).copyOnDeath().build());

    /** A Silentium's possession: copied on death so a visit that ends badly can still be put right. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Possession>> POSSESSION =
            ATTACHMENT_TYPES.register("possession",
                    () -> AttachmentType.serializable(Possession::new).copyOnDeath().build());

    /** Who holds a would-be surrogate with Blood Binding, and the Qliphoth floor holding the binding open. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateTether>> SURROGATE_TETHER =
            ATTACHMENT_TYPES.register("surrogate_tether",
                    () -> AttachmentType.serializable(com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateTether::new).build());

    /** Not copied on death: a respawned Apotheos starts with no spores. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SporeLoad>> SPORE_LOAD =
            ATTACHMENT_TYPES.register("spore_load",
                    () -> AttachmentType.serializable(SporeLoad::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PowerGuardrailState>> POWER_GUARDRAILS =
            ATTACHMENT_TYPES.register("power_guardrails",
                    () -> AttachmentType.serializable(PowerGuardrailState::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WillAmbushState>> WILL_AMBUSH_STATE =
            ATTACHMENT_TYPES.register("will_ambush_state",
                    () -> AttachmentType.serializable(WillAmbushState::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BloodTendency>> BLOOD_TENDENCY =
            ATTACHMENT_TYPES.register("blood_tendency",
                    () -> AttachmentType.serializable(BloodTendency::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<VascularSystem>> VASCULAR_SYSTEM =
            ATTACHMENT_TYPES.register("vascular_system",
                    () -> AttachmentType.serializable(VascularSystem::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<InitiatoryDegree>> INITIATORY_DEGREE =
            ATTACHMENT_TYPES.register("initiatory_degree",
                    () -> AttachmentType.serializable(InitiatoryDegree::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<UnstainedProgress>> UNSTAINED_PROGRESS =
            ATTACHMENT_TYPES.register("unstained_progress",
                    () -> AttachmentType.serializable(UnstainedProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EquippedMorphling>> EQUIPPED_MORPHLING =
            ATTACHMENT_TYPES.register("equipped_morphling",
                    () -> AttachmentType.serializable(EquippedMorphling::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KnownManipulations>> KNOWN_MANIPULATIONS =
            ATTACHMENT_TYPES.register("known_manipulations",
                    () -> AttachmentType.serializable(KnownManipulations::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LivingStaffProgress>> LIVING_STAFF_PROGRESS =
            ATTACHMENT_TYPES.register("living_staff_progress",
                    () -> AttachmentType.serializable(LivingStaffProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpecimenBestiaryProgress>> SPECIMEN_BESTIARY =
            ATTACHMENT_TYPES.register("specimen_bestiary",
                    () -> AttachmentType.serializable(SpecimenBestiaryProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KnownStillArts>> KNOWN_STILL_ARTS =
            ATTACHMENT_TYPES.register("known_still_arts",
                    () -> AttachmentType.serializable(KnownStillArts::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KnownSummons>> KNOWN_SUMMONS =
            ATTACHMENT_TYPES.register("known_summons",
                    () -> AttachmentType.serializable(KnownSummons::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LiberKnowledge>> LIBER_KNOWLEDGE =
            ATTACHMENT_TYPES.register("liber_knowledge",
                    () -> AttachmentType.serializable(LiberKnowledge::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<IchorianKnowledge>> ICHORIAN_KNOWLEDGE =
            ATTACHMENT_TYPES.register("ichorian_knowledge",
                    () -> AttachmentType.serializable(IchorianKnowledge::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DialogueKnowledge>> DIALOGUE_KNOWLEDGE =
            ATTACHMENT_TYPES.register("dialogue_knowledge",
                    () -> AttachmentType.serializable(DialogueKnowledge::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillProgress>> SKILL_PROGRESS =
            ATTACHMENT_TYPES.register("skill_progress",
                    () -> AttachmentType.serializable(SkillProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WhiteHumorVolume>> WHITE_HUMOR_VOLUME =
            ATTACHMENT_TYPES.register("white_humor_volume",
                    () -> AttachmentType.serializable(WhiteHumorVolume::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<VisceralOrgans>> VISCERAL_ORGANS =
            ATTACHMENT_TYPES.register("visceral_organs",
                    () -> AttachmentType.serializable(VisceralOrgans::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ScarsContainer>> SCARS =
            ATTACHMENT_TYPES.register("scars",
                    () -> AttachmentType.serializable(() -> new ScarsContainer()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HarbingerEquipmentContainer>> HARBINGER_EQUIPMENT =
            ATTACHMENT_TYPES.register("harbinger_equipment",
                    () -> AttachmentType.serializable(() -> new HarbingerEquipmentContainer()).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LastDeathMemoryAttachment>> LAST_DEATH_MEMORY =
            ATTACHMENT_TYPES.register("last_death_memory",
                    () -> AttachmentType.serializable(LastDeathMemoryAttachment::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MuscleMemoryState>> MUSCLE_MEMORY =
            ATTACHMENT_TYPES.register("muscle_memory",
                    () -> AttachmentType.serializable(MuscleMemoryState::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerDataVersion>> DATA_VERSION =
            ATTACHMENT_TYPES.register("data_version",
                    () -> AttachmentType.serializable(PlayerDataVersion::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgress>> CIRCUS_PROGRESS =
            ATTACHMENT_TYPES.register("circus_progress", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgress::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.encounter.ChamberAccess>> CHAMBER_ACCESS =
            ATTACHMENT_TYPES.register("chamber_access", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.encounter.ChamberAccess::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhisperInbox>> PENDING_WHISPERS =
            ATTACHMENT_TYPES.register("pending_whispers", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhisperInbox::new).copyOnDeath().build());

    /** Saved with the player but never copied to a clone: these cooldowns reset on death and End return (S-7). */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns>> COOLDOWNS =
            ATTACHMENT_TYPES.register("cooldowns", () -> AttachmentType.serializable(
                    com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns::new)
                    .copyHandler((cooldowns, holder, provider) -> null).build());

    // ── Block-entity attachments (no copyOnDeath) ──
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BloodVolume>> BLOCK_BLOOD_VOLUME =
            ATTACHMENT_TYPES.register("block_blood_volume",
                    () -> AttachmentType.serializable(BloodVolume::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BloodTendency>> BLOCK_BLOOD_TENDENCY =
            ATTACHMENT_TYPES.register("block_blood_tendency",
                    () -> AttachmentType.serializable(BloodTendency::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<WhiteHumorVolume>> BLOCK_WHITE_HUMOR_VOLUME =
            ATTACHMENT_TYPES.register("block_white_humor_volume",
                    () -> AttachmentType.serializable(WhiteHumorVolume::new).build());
}
