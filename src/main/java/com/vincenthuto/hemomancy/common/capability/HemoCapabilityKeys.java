package com.vincenthuto.hemomancy.common.capability;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling.IEquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipment;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipmentItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScarItem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.IKnownSummons;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.IKnownStillArts;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.unstained.white_humor.IWhiteHumorVolume;
import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

public final class HemoCapabilityKeys {
    private HemoCapabilityKeys() {}

    public static final EntityCapability<IBloodVolume, Void> BLOOD_VOLUME =
            EntityCapability.createVoid(Hemomancy.rloc("blood_volume"), IBloodVolume.class);

    public static final EntityCapability<IBloodTendency, Void> BLOOD_TENDENCY =
            EntityCapability.createVoid(Hemomancy.rloc("blood_tendency"), IBloodTendency.class);

    public static final EntityCapability<IVascularSystem, Void> VASCULAR_SYSTEM =
            EntityCapability.createVoid(Hemomancy.rloc("vascular_system"), IVascularSystem.class);

    public static final EntityCapability<IInitiatoryDegree, Void> INITIATORY_DEGREE =
            EntityCapability.createVoid(Hemomancy.rloc("initiatory_degree"), IInitiatoryDegree.class);

    public static final EntityCapability<IUnstainedProgress, Void> UNSTAINED_PROGRESS =
            EntityCapability.createVoid(Hemomancy.rloc("unstained_progress"), IUnstainedProgress.class);

    public static final EntityCapability<IEquippedMorphling, Void> EQUIPPED_MORPHLING =
            EntityCapability.createVoid(Hemomancy.rloc("equipped_morphling"), IEquippedMorphling.class);

    public static final EntityCapability<IKnownManipulations, Void> KNOWN_MANIPULATIONS =
            EntityCapability.createVoid(Hemomancy.rloc("known_manipulations"), IKnownManipulations.class);

    public static final EntityCapability<ILivingStaffProgress, Void> LIVING_STAFF_PROGRESS =
            EntityCapability.createVoid(Hemomancy.rloc("living_staff_progress"), ILivingStaffProgress.class);

    public static final EntityCapability<SpecimenBestiaryProgress, Void> SPECIMEN_BESTIARY =
            EntityCapability.createVoid(Hemomancy.rloc("specimen_bestiary"), SpecimenBestiaryProgress.class);

    public static final EntityCapability<IKnownStillArts, Void> KNOWN_STILL_ARTS =
            EntityCapability.createVoid(Hemomancy.rloc("known_still_arts"), IKnownStillArts.class);

    public static final EntityCapability<IKnownSummons, Void> KNOWN_SUMMONS =
            EntityCapability.createVoid(Hemomancy.rloc("known_summons"), IKnownSummons.class);

    public static final EntityCapability<IBookKnowledge, Void> LIBER_KNOWLEDGE =
            EntityCapability.createVoid(Hemomancy.rloc("liber_knowledge"), IBookKnowledge.class);

    public static final EntityCapability<SkillProgress, Void> SKILL_PROGRESS =
            EntityCapability.createVoid(Hemomancy.rloc("skill_progress"), SkillProgress.class);

    public static final EntityCapability<IWhiteHumorVolume, Void> WHITE_HUMOR_VOLUME =
            EntityCapability.createVoid(Hemomancy.rloc("white_humor_volume"), IWhiteHumorVolume.class);

    public static final EntityCapability<IVisceralOrgans, Void> VISCERAL_ORGANS =
            EntityCapability.createVoid(Hemomancy.rloc("visceral_organs"), IVisceralOrgans.class);

    public static final EntityCapability<IScars, Void> SCARS =
            EntityCapability.createVoid(Hemomancy.rloc("scars"), IScars.class);

    public static final EntityCapability<IHarbingerEquipmentItemHandler, Void> HARBINGER_EQUIPMENT =
            EntityCapability.createVoid(Hemomancy.rloc("harbinger_equipment"), IHarbingerEquipmentItemHandler.class);

    public static final ItemCapability<IBloodVolume, Void> ITEM_BLOOD_VOLUME =
            ItemCapability.createVoid(Hemomancy.rloc("item_blood_volume"), IBloodVolume.class);

    public static final ItemCapability<IScarItem, Void> ITEM_SCAR =
            ItemCapability.createVoid(Hemomancy.rloc("item_scar"), IScarItem.class);

    public static final ItemCapability<IHarbingerEquipment, Void> ITEM_HARBINGER_EQUIPMENT =
            ItemCapability.createVoid(Hemomancy.rloc("item_harbinger_equipment"), IHarbingerEquipment.class);
}
