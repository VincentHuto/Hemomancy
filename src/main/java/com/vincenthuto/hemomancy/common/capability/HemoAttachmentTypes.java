package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.EquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarsContainer;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummons;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArts;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.VisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolume;
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

    // ── Player attachments (copyOnDeath so data survives death/respawn) ──
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BloodVolume>> BLOOD_VOLUME =
            ATTACHMENT_TYPES.register("blood_volume",
                    () -> AttachmentType.serializable(BloodVolume::new).copyOnDeath().build());

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

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KnownStillArts>> KNOWN_STILL_ARTS =
            ATTACHMENT_TYPES.register("known_still_arts",
                    () -> AttachmentType.serializable(KnownStillArts::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KnownSummons>> KNOWN_SUMMONS =
            ATTACHMENT_TYPES.register("known_summons",
                    () -> AttachmentType.serializable(KnownSummons::new).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LiberKnowledge>> LIBER_KNOWLEDGE =
            ATTACHMENT_TYPES.register("liber_knowledge",
                    () -> AttachmentType.serializable(LiberKnowledge::new).copyOnDeath().build());

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

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LastDeathMemoryAttachment>> LAST_DEATH_MEMORY =
            ATTACHMENT_TYPES.register("last_death_memory",
                    () -> AttachmentType.serializable(LastDeathMemoryAttachment::new).copyOnDeath().build());

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

    // ── ItemStack attachment (no copyOnDeath) ──
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BloodVolume>> ITEM_BLOOD_VOLUME =
            ATTACHMENT_TYPES.register("item_blood_volume",
                    () -> AttachmentType.serializable(BloodVolume::new).build());
}
