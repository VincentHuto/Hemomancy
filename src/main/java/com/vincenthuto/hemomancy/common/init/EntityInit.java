package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentAnnettaInfectionEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.item.EntityFlyingCharm;
import com.vincenthuto.hemomancy.common.entity.item.EntityMorphlingPolypItem;
import com.vincenthuto.hemomancy.common.entity.item.EntityQliphothSeedItem;
import com.vincenthuto.hemomancy.common.entity.mob.animal.*;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BarbedUrchinEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BloodLanternJellyEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BrinedVotaryEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.ChalybeateSnailEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.HemojellyEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleTuning;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.*;
import com.vincenthuto.hemomancy.common.entity.mob.monster.*;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.npc.DrudgeEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVotaryWayfarerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedAcolyteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedGuardianEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedScoutEntity;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedZealotEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.*;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.entity.utility.ArmatureRestraintEntity;
import com.vincenthuto.hemomancy.common.entity.utility.AwakenedIchorianSigilEntity;
import com.vincenthuto.hemomancy.common.entity.utility.ArborOfWillEntity;
import com.vincenthuto.hemomancy.common.entity.utility.CovenantThroneSeatEntity;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.entity.utility.UnsettledIchorEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE,
            Hemomancy.MOD_ID);

    public static final TagKey<EntityType<?>> FUNGAL_TAG = createTag("fungal");
    public static final TagKey<EntityType<?>> UMBRAL_TAG = createTag("umbral");
    public static final TagKey<EntityType<?>> INCANDESCENT_TAG = createTag("incandescent");
    public static final TagKey<EntityType<?>> FERRIC_TAG = createTag("ferric");
    public static final TagKey<EntityType<?>> VIVACIOUS_TAG = createTag("vivacious");
    public static final TagKey<EntityType<?>> RUINOUS_TAG = createTag("ruinous");
    public static final TagKey<EntityType<?>> NEUROTIC_TAG = createTag("neurotic");
    public static final TagKey<EntityType<?>> FERVENT_TAG = createTag("fervent");
    public static final TagKey<EntityType<?>> FRIGID_TAG = createTag("frigid");
    public static final TagKey<EntityType<?>> HEMOMANCY_MOB = createTag("hemomancy_mob");
    public static final TagKey<EntityType<?>> SPECIMEN_JAR_CAPTURABLE = createTag("specimen_jar_capturable");
    public static final TagKey<EntityType<?>> WILLS = createTag("wills");
    public static final TagKey<EntityType<?>> PUPPET_ATTENTION_IMMUNE = createTag("puppet_attention_immune");

    public static final DeferredHolder<EntityType<?>, EntityType<CovenantThroneSeatEntity>> covenant_throne_seat =
            ENTITY_TYPES.register("covenant_throne_seat",
                    () -> EntityType.Builder.<CovenantThroneSeatEntity>of(CovenantThroneSeatEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build(Hemomancy.rloc("covenant_throne_seat").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ArmatureRestraintEntity>> hematic_armature_restraint =
            ENTITY_TYPES.register("hematic_armature_restraint",
                    () -> EntityType.Builder.<ArmatureRestraintEntity>of(ArmatureRestraintEntity::new, MobCategory.MISC)
                            .sized(0.01F, 0.01F)
                            .clientTrackingRange(8)
                            .updateInterval(20)
                            .build(Hemomancy.rloc("hematic_armature_restraint").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<UnsettledIchorEntity>> unsettled_ichor =
            ENTITY_TYPES.register("unsettled_ichor",
                    () -> EntityType.Builder.<UnsettledIchorEntity>of(UnsettledIchorEntity::new, MobCategory.MISC)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(Hemomancy.rloc("unsettled_ichor").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<AwakenedIchorianSigilEntity>> awakened_ichorian_sigil =
            ENTITY_TYPES.register("awakened_ichorian_sigil",
                    () -> EntityType.Builder.<AwakenedIchorianSigilEntity>of(
                                    AwakenedIchorianSigilEntity::new, MobCategory.MISC)
                            .sized(2.5F, 2.5F)
                            .clientTrackingRange(12)
                            .updateInterval(2)
                            .build(Hemomancy.rloc("awakened_ichorian_sigil").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HumanitySpriteEntity>> humanity_sprite =
            ENTITY_TYPES.register("humanity_sprite",
                    () -> EntityType.Builder.<HumanitySpriteEntity>of(HumanitySpriteEntity::new, MobCategory.MISC)
                            .sized(1.25F, 4.25F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(Hemomancy.rloc("humanity_sprite").toString()));

    // Mobs
    public static final DeferredHolder<EntityType<?>, EntityType<UnstainedZealotEntity>> unstained_zealot = ENTITY_TYPES.register(
            "unstained_zealot",
            () -> EntityType.Builder.of(UnstainedZealotEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("unstained_zealot").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<UnstainedGuardianEntity>> unstained_guardian = ENTITY_TYPES.register(
            "unstained_guardian",
            () -> EntityType.Builder.of(UnstainedGuardianEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("unstained_guardian").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<UnstainedAcolyteEntity>> unstained_acolyte = ENTITY_TYPES.register(
            "unstained_acolyte",
            () -> EntityType.Builder.of(UnstainedAcolyteEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("unstained_acolyte").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<UnstainedScoutEntity>> unstained_scout = ENTITY_TYPES.register(
            "unstained_scout",
            () -> EntityType.Builder.of(UnstainedScoutEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("unstained_scout").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PaleIntercessionEntity>> spectral_companion = ENTITY_TYPES.register(
            "spectral_companion",
            () -> EntityType.Builder.of(PaleIntercessionEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("spectral_companion").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PhantasmalEchoEntity>> phantasmal_echo = ENTITY_TYPES.register(
            "phantasmal_echo",
            () -> EntityType.Builder.of(PhantasmalEchoEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .updateInterval(2)
                    .build(Hemomancy.rloc("phantasmal_echo").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerHermitEntity>> harbinger_hermit = ENTITY_TYPES.register(
            "harbinger_hermit",
            () -> EntityType.Builder.of(HarbingerHermitEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_hermit").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerAlchemistEntity>> harbinger_alchemist = ENTITY_TYPES.register(
            "harbinger_alchemist",
            () -> EntityType.Builder.of(HarbingerAlchemistEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_alchemist").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerArtificerEntity>> harbinger_artificer = ENTITY_TYPES.register(
            "harbinger_artificer",
            () -> EntityType.Builder.of(HarbingerArtificerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_artificer").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerCicatrixAnchoriteEntity>> harbinger_cicatrix_anchorite = ENTITY_TYPES.register(
            "harbinger_cicatrix_anchorite",
            () -> EntityType.Builder.of(HarbingerCicatrixAnchoriteEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_cicatrix_anchorite").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerMnemonistEntity>> harbinger_mnemonist = ENTITY_TYPES.register(
            "harbinger_mnemonist",
            () -> EntityType.Builder.of(HarbingerMnemonistEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_mnemonist").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerVicarEntity>> harbinger_vicar = ENTITY_TYPES.register(
            "harbinger_vicar",
            () -> EntityType.Builder.of(HarbingerVicarEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_vicar").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerVoyagerEntity>> harbinger_voyager = ENTITY_TYPES.register(
            "harbinger_voyager",
            () -> EntityType.Builder.of(HarbingerVoyagerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_voyager").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerVotaryWayfarerEntity>> harbinger_votary_wayfarer = ENTITY_TYPES.register(
            "harbinger_votary_wayfarer",
            () -> EntityType.Builder.of(HarbingerVotaryWayfarerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_votary_wayfarer").toString()));

    // Boss room: Hematic Construct (inner trial minion)
    public static final DeferredHolder<EntityType<?>, EntityType<HematicConstructEntity>> hematic_construct = ENTITY_TYPES.register(
            "hematic_construct",
            () -> EntityType.Builder.of(HematicConstructEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build(Hemomancy.rloc("hematic_construct").toString()));

    // Boss: Hollow Vessel (Saint Hemorath)
    public static final DeferredHolder<EntityType<?>, EntityType<HemorathEntity>> hemorath = ENTITY_TYPES.register(
            "hemorath",
            () -> EntityType.Builder.of(HemorathEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.2F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(Hemomancy.rloc("hemorath").toString()));

    // Boss: Seraphae (The Bound Radiance)
    public static final DeferredHolder<EntityType<?>, EntityType<SeraphaeEntity>> seraphae = ENTITY_TYPES.register(
            "seraphae",
            () -> EntityType.Builder.of(SeraphaeEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.4F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(Hemomancy.rloc("seraphae").toString()));

    // Seraphae fragment (spawned during fracturing)
    public static final DeferredHolder<EntityType<?>, EntityType<SeraphaeFragmentEntity>> seraphae_fragment = ENTITY_TYPES.register(
            "seraphae_fragment",
            () -> EntityType.Builder.of(SeraphaeFragmentEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.8F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("seraphae_fragment").toString()));

    // Containment Anchor (arena element for Seraphae fight)
    public static final DeferredHolder<EntityType<?>, EntityType<ContainmentAnchorEntity>> containment_anchor = ENTITY_TYPES.register(
            "containment_anchor",
            () -> EntityType.Builder.of(ContainmentAnchorEntity::new, MobCategory.MISC)
                    .sized(0.8F, 1.2F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("containment_anchor").toString()));

    // Boss: Annetta Knowles (The Stained Priestess)
    public static final DeferredHolder<EntityType<?>, EntityType<AnnettaKnowlesEntity>> annetta_knowles = ENTITY_TYPES.register(
            "annetta_knowles",
            () -> EntityType.Builder.of(AnnettaKnowlesEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(10)
                    .build(Hemomancy.rloc("annetta_knowles").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<StainedPriestessEntity>> stained_priestess = ENTITY_TYPES.register(
            "stained_priestess",
            () -> EntityType.Builder.of(StainedPriestessEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.6F)
                    .clientTrackingRange(10)
                    .build(Hemomancy.rloc("stained_priestess").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<LatentAnnettaInfectionEntity>> latent_annetta_infection = ENTITY_TYPES.register(
            "latent_annetta_infection",
            () -> EntityType.Builder.of(LatentAnnettaInfectionEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.2F)
                    .clientTrackingRange(10)
                    .build(Hemomancy.rloc("latent_annetta_infection").toString()));

    // Boss: Putriciel (The Rotting Saint)
    public static final DeferredHolder<EntityType<?>, EntityType<PutricielEntity>> putriciel = ENTITY_TYPES.register(
            "putriciel",
            () -> EntityType.Builder.of(PutricielEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 2.6F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(Hemomancy.rloc("putriciel").toString()));

    // Boss: Velorum (The Frozen Martyr)
    public static final DeferredHolder<EntityType<?>, EntityType<VelorumEntity>> velorum = ENTITY_TYPES.register(
            "velorum",
            () -> EntityType.Builder.of(VelorumEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.0F)
                    .clientTrackingRange(10)
                    .build(Hemomancy.rloc("velorum").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VesperTheCrownedRefusalEntity>> vesper_crowned_refusal = ENTITY_TYPES.register(
            "vesper_crowned_refusal",
            () -> EntityType.Builder.of(VesperTheCrownedRefusalEntity::new, MobCategory.MONSTER)
                    .sized(4.0F, 6.0F)
                    .clientTrackingRange(12)
                    .fireImmune()
                    .build(Hemomancy.rloc("vesper_crowned_refusal").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VesperTheEveningStarEntity>> vesper_evening_star = ENTITY_TYPES.register(
            "vesper_evening_star",
            () -> EntityType.Builder.of(VesperTheEveningStarEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 2.3F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(Hemomancy.rloc("vesper_evening_star").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MycophantEntity>> mycophant = ENTITY_TYPES.register(
            "mycophant",
            () -> EntityType.Builder.of(MycophantEntity::new, MobCategory.MONSTER)
                    .sized(4.0F, 6.0F)
                    .clientTrackingRange(12)
                    .fireImmune()
                    .build(Hemomancy.rloc("mycophant").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<LeechEntity>> leech = ENTITY_TYPES.register("leech",
            () -> EntityType.Builder.of(LeechEntity::new, MobCategory.CREATURE).sized(0.4F, 0.1F)
                    .build(Hemomancy.rloc("leech").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<BloodlickerEntity>> bloodlicker =
            ENTITY_TYPES.register("bloodlicker",
                    () -> EntityType.Builder.of(BloodlickerEntity::new, MobCategory.MONSTER)
                            .sized(1.15F, 1.45F)
                            .clientTrackingRange(8)
                            .build(Hemomancy.rloc("bloodlicker").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<BogRevenantEntity>> bog_revenant = ENTITY_TYPES.register("bog_revenant",
            () -> EntityType.Builder.of(BogRevenantEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("bog_revenant").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<FargoneEntity>> fargone = ENTITY_TYPES.register("fargone",
            () -> EntityType.Builder.of(FargoneEntity::new, MobCategory.MONSTER).sized(1F, 1.8F)
                    .build(Hemomancy.rloc("fargone").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<DesiccantEntity>> desiccant = ENTITY_TYPES.register(
            "desiccant",
            () -> EntityType.Builder.of(DesiccantEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.5F).build(Hemomancy.rloc("desiccant").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<ThirsterEntity>> thirster = ENTITY_TYPES.register("thirster",
            () -> EntityType.Builder.of(ThirsterEntity::new, MobCategory.MONSTER).sized(1F, 1.8F)
                    .build(Hemomancy.rloc("thirster").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<AbhorentThoughtEntity>> abhorent_thought = ENTITY_TYPES
            .register("abhorent_thought",
                    () -> EntityType.Builder.of(AbhorentThoughtEntity::new, MobCategory.MONSTER)
                            .sized(1.5f, 3.25F)
                            .build(Hemomancy.rloc("abhorent_thought").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ErythromyceliumEruptusEntity>> erythromycelium_eruptus = ENTITY_TYPES
            .register("erythromycelium_eruptus",
                    () -> EntityType.Builder.of(ErythromyceliumEruptusEntity::new, MobCategory.MONSTER)
                            .sized(1.5f, 3F)
                            .build(Hemomancy.rloc("erythromycelium_eruptus").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodDrunkPuppeteerEntity>> blood_drunk_puppeteer = ENTITY_TYPES
            .register("blood_drunk_puppeteer",
                    () -> EntityType.Builder
                            .of(BloodDrunkPuppeteerEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .build(Hemomancy.rloc("blood_drunk_puppeteer").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<WillEntity>> will = ENTITY_TYPES
            .register("will",
                    () -> EntityType.Builder
                            .<WillEntity>of(WillEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build(Hemomancy.rloc("will").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<WillAnchorEntity>> will_anchor = ENTITY_TYPES
            .register("will_anchor",
                    () -> EntityType.Builder
                            .<WillAnchorEntity>of(WillAnchorEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build(Hemomancy.rloc("will_anchor").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ArborOfWillEntity>> arbor_of_will = ENTITY_TYPES
            .register("arbor_of_will", () -> EntityType.Builder
                    .<ArborOfWillEntity>of(ArborOfWillEntity::new, MobCategory.MISC)
                    .sized(1.6F, 5.5F)
                    .clientTrackingRange(64)
                    .updateInterval(20)
                    .build(Hemomancy.rloc("arbor_of_will").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodThrallEntity>> blood_thrall = ENTITY_TYPES
            .register("blood_thrall",
                    () -> EntityType.Builder.<BloodThrallEntity>of(BloodThrallEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F)
                            .build(Hemomancy.rloc("blood_thrall").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VeinwingVultureEntity>> veinwing_vulture = ENTITY_TYPES
            .register("veinwing_vulture",
                    () -> EntityType.Builder.<VeinwingVultureEntity>of(VeinwingVultureEntity::new, MobCategory.CREATURE)
                            .sized(0.5F, 0.8F)
                            .clientTrackingRange(8)
                            .build(Hemomancy.rloc("veinwing_vulture").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MarrowSpitterEntity>> marrow_spitter = ENTITY_TYPES
            .register("marrow_spitter",
                    () -> EntityType.Builder.<MarrowSpitterEntity>of(MarrowSpitterEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(Hemomancy.rloc("marrow_spitter").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<GoreboundHulkEntity>> gorebound_hulk = ENTITY_TYPES
            .register("gorebound_hulk",
                    () -> EntityType.Builder.<GoreboundHulkEntity>of(GoreboundHulkEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 2.4F)
                            .clientTrackingRange(8)
                            .build(Hemomancy.rloc("gorebound_hulk").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MnemonistPuppetEntity>> mnemonist_puppet = ENTITY_TYPES
            .register("mnemonist_puppet",
                    () -> EntityType.Builder.<MnemonistPuppetEntity>of(MnemonistPuppetEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build(Hemomancy.rloc("mnemonist_puppet").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ScarletMummerEntity>> scarlet_mummer = ENTITY_TYPES
            .register("scarlet_mummer",
                    () -> EntityType.Builder.<ScarletMummerEntity>of(ScarletMummerEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.9F)
                            .clientTrackingRange(8)
                            .build(Hemomancy.rloc("scarlet_mummer").toString()));

	public static final DeferredHolder<EntityType<?>, EntityType<SanguineHoundEntity>> sanguine_hound = ENTITY_TYPES
			.register("sanguine_hound",
					() -> EntityType.Builder.<SanguineHoundEntity>of(SanguineHoundEntity::new, MobCategory.CREATURE)
							.sized(0.65F, 0.85F)
							.clientTrackingRange(10)
							.build(Hemomancy.rloc("sanguine_hound").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<DrudgeEntity>> drudge = ENTITY_TYPES
            .register("drudge",
                    () -> EntityType.Builder.<DrudgeEntity>of(DrudgeEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.5F)
                            .build(Hemomancy.rloc("drudge").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EnthralledDollEntity>> enthralled_doll = ENTITY_TYPES
            .register("enthralled_doll",
                    () -> EntityType.Builder.<EnthralledDollEntity>of(EnthralledDollEntity::new, MobCategory.MONSTER)
                            .sized(0.5F, 0.5F)
                            .build(Hemomancy.rloc("enthralled_doll").toString()));


    public static final DeferredHolder<EntityType<?>, EntityType<BarbedUrchinEntity>> barbed_urchin = ENTITY_TYPES.register(
            "barbed_urchin",
            () -> EntityType.Builder.of(BarbedUrchinEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(1F, 1F).build(Hemomancy.rloc("barbed_urchin").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ChalybeateSnailEntity>> chalybeate_snail = ENTITY_TYPES.register(
            "chalybeate_snail",
            () -> EntityType.Builder.of(ChalybeateSnailEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(0.9F, 0.45F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("chalybeate_snail").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodLanternJellyEntity>> blood_lantern_jelly = ENTITY_TYPES.register(
            "blood_lantern_jelly",
            () -> EntityType.Builder.of(BloodLanternJellyEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("blood_lantern_jelly").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MnemonicWhaleEntity>> mnemonic_whale = ENTITY_TYPES.register(
            "mnemonic_whale",
            () -> EntityType.Builder.of(MnemonicWhaleEntity::new, MobCategory.WATER_CREATURE)
                    .sized(MnemonicWhaleTuning.HITBOX_WIDTH, MnemonicWhaleTuning.HITBOX_HEIGHT)
                    .clientTrackingRange(10)
                    .build(Hemomancy.rloc("mnemonic_whale").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BrinedVotaryEntity>> brined_votary = ENTITY_TYPES.register(
            "brined_votary",
            () -> EntityType.Builder.of(BrinedVotaryEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("brined_votary").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PrismCuttleEntity>> prism_cuttle = ENTITY_TYPES.register(
            "prism_cuttle",
            () -> EntityType.Builder.of(PrismCuttleEntity::new, MobCategory.WATER_CREATURE)
                    .sized(0.7F, 0.55F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build(Hemomancy.rloc("prism_cuttle").toString()));


    public static final DeferredHolder<EntityType<?>, EntityType<LumpOfThoughtEntity>> lump_of_thought = ENTITY_TYPES.register(
            "lump_of_thought",
            () -> EntityType.Builder.of(LumpOfThoughtEntity::new, MobCategory.MONSTER)
                    .sized(1F, 1F).build(Hemomancy.rloc("lump_of_thought").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<MorphlingPolypEntity>> morphling_polyp = ENTITY_TYPES.register(
            "morphling_polyp",
            () -> EntityType.Builder.of(MorphlingPolypEntity::new, MobCategory.MONSTER)
                    .sized(1F, 1F).build(Hemomancy.rloc("morphling_polyp").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FunglingEntity>> fungling = ENTITY_TYPES.register("fungling",
            () -> EntityType.Builder.of(FunglingEntity::new, MobCategory.CREATURE).sized(1F, 1F)
                    .build(Hemomancy.rloc("fungling").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ToothPecksEntity>> tooth_pecks = ENTITY_TYPES.register("tooth_pecks",
            () -> EntityType.Builder.of(ToothPecksEntity::new, MobCategory.MONSTER).sized(0.6F, 0.45F)
                    .build(Hemomancy.rloc("tooth_pecks").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HemolymphopodaEntity>> hemolymphopoda = ENTITY_TYPES.register(
            "hemolymphopoda",
            () -> EntityType.Builder.of(HemolymphopodaEntity::new, MobCategory.AMBIENT).sized(0.9F, 0.3F)
                    .build(Hemomancy.rloc("hemolymphopoda").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<LanternTickEntity>> lantern_tick = ENTITY_TYPES.register(
            "lantern_tick",
            () -> EntityType.Builder.of(LanternTickEntity::new, MobCategory.MONSTER).sized(0.55F, 0.35F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("lantern_tick").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ChitiniteEntity>> chitinite = ENTITY_TYPES.register("chitinite",
            () -> EntityType.Builder.of(ChitiniteEntity::new, MobCategory.CREATURE).sized(1F, 0.3F)
                    .build(Hemomancy.rloc("chitinite").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FerventChitiniteEntity>> fervent_chitinite = ENTITY_TYPES.register("fervent_chitinite",
            () -> EntityType.Builder.of(FerventChitiniteEntity::new, MobCategory.CREATURE).sized(1F, 0.3F)
                    .build(Hemomancy.rloc("fervent_chitinite").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VenomRibCentipedeEntity>> venom_rib_centipede = ENTITY_TYPES.register(
            "venom_rib_centipede",
            () -> EntityType.Builder.of(VenomRibCentipedeEntity::new, MobCategory.MONSTER)
                    .sized(1.85F, 0.55F)
                    .clientTrackingRange(10)
                    .build(Hemomancy.rloc("venom_rib_centipede").toString()));


    public static final DeferredHolder<EntityType<?>, EntityType<ChthonianEntity>> chthonian = ENTITY_TYPES.register("chthonian",
            () -> EntityType.Builder.of(ChthonianEntity::new, MobCategory.MONSTER).sized(1F, 1F)
                    .build(Hemomancy.rloc("chthonian").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<ChthonianQueenEntity>> chthonian_queen = ENTITY_TYPES.register(
            "chthonian_queen",
            () -> EntityType.Builder.of(ChthonianQueenEntity::new, MobCategory.MONSTER)
                    .sized(1F, 1F).build(Hemomancy.rloc("chthonian").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<CrimsonDoeEntity>> crimson_doe = ENTITY_TYPES.register(
            "crimson_doe",
            () -> EntityType.Builder.of(CrimsonDoeEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.4F).build(Hemomancy.rloc("crimson_doe").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VerdigrisMothEntity>> verdigris_moth = ENTITY_TYPES.register(
            "verdigris_moth",
            () -> EntityType.Builder.of(VerdigrisMothEntity::new, MobCategory.AMBIENT)
                    .sized(0.7F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build(Hemomancy.rloc("verdigris_moth").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HematicBurrowerEntity>> hematic_burrower = ENTITY_TYPES.register(
            "hematic_burrower",
            () -> EntityType.Builder.of(HematicBurrowerEntity::new, MobCategory.CREATURE)
                    .sized(0.65F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build(Hemomancy.rloc("hematic_burrower").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<ScarletSerpentEntity>> scarlet_serpent = ENTITY_TYPES.register(
            "scarlet_serpent",
            () -> EntityType.Builder.of(ScarletSerpentEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.35F)
                    .clientTrackingRange(8)
                    .build(Hemomancy.rloc("scarlet_serpent").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HemojellyEntity>> hemojelly = ENTITY_TYPES.register(
            "hemojelly",
            () -> EntityType.Builder.of(HemojellyEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.9F).build(Hemomancy.rloc("hemojelly").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VenousStriderEntity>> venous_strider = ENTITY_TYPES.register(
            "venous_strider",
            () -> EntityType.Builder.of(VenousStriderEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F).build(Hemomancy.rloc("venous_strider").toString()));

    // Projectiles
    public static final DeferredHolder<EntityType<?>, EntityType<EntityFlyingCharm>> flying_charm = ENTITY_TYPES.register(
            "flying_charm",
            () -> EntityType.Builder.<EntityFlyingCharm>of(EntityFlyingCharm::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .clientTrackingRange(4).updateInterval(4)
                    .build(Hemomancy.rloc("flying_charm").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<DirectedBloodOrbEntity>> directed_blood_orb = ENTITY_TYPES.register(
            "directed_blood_orb",
            () -> EntityType.Builder.<DirectedBloodOrbEntity>of(DirectedBloodOrbEntity::new, MobCategory.MISC)
                    .setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
                    .build(Hemomancy.rloc("directed_blood_orb").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<TrackingBloodOrbEntity>> tracking_blood_orb = ENTITY_TYPES.register(
            "tracking_blood_orb",
            () -> EntityType.Builder.<TrackingBloodOrbEntity>of(TrackingBloodOrbEntity::new, MobCategory.MISC)
                    .setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
                    .build(Hemomancy.rloc("tracking_blood_orb").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<BloodCloudCarrierEntity>> blood_cloud_carrier = ENTITY_TYPES.register(
            "blood_cloud_carrier",
            () -> EntityType.Builder.<BloodCloudCarrierEntity>of(BloodCloudCarrierEntity::new, MobCategory.MISC)
                    .setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
                    .build(Hemomancy.rloc("blood_cloud_carrier").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<CloudEntityBlood>> blood_cloud = ENTITY_TYPES.register("blood_cloud",
            () -> EntityType.Builder.<CloudEntityBlood>of(CloudEntityBlood::new, MobCategory.MISC).setTrackingRange(150)
                    .setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
                    .build(Hemomancy.rloc("blood_cloud").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<TrackingSerpentEntity>> tracking_snake = ENTITY_TYPES.register(
            "tracking_snake",
            () -> EntityType.Builder.<TrackingSerpentEntity>of(TrackingSerpentEntity::new, MobCategory.MISC)
                    .setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
                    .sized(0.25F, 0.25F).build(Hemomancy.rloc("tracking_snake").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<ConstrictorCordEntity>> constrictor_cord_projectile =
            ENTITY_TYPES.register("constrictor_cord_projectile",
                    () -> EntityType.Builder.<ConstrictorCordEntity>of(ConstrictorCordEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(4)
                            .build(Hemomancy.rloc("constrictor_cord_projectile").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<BloodChumEntity>> blood_chum_projectile =
            ENTITY_TYPES.register("blood_chum_projectile",
                    () -> EntityType.Builder.<BloodChumEntity>of(BloodChumEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(4)
                            .build(Hemomancy.rloc("blood_chum_projectile").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<TrackingPestsEntity>> tracking_pests = ENTITY_TYPES.register(
            "tracking_pests",
            () -> EntityType.Builder.<TrackingPestsEntity>of(TrackingPestsEntity::new, MobCategory.MISC)
                    .setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
                    .sized(0.25F, 0.25F).build(Hemomancy.rloc("tracking_pests").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityIronPillar>> iron_pillar = ENTITY_TYPES.register("iron_pillar",
            () -> EntityType.Builder.<EntityIronPillar>of(EntityIronPillar::new, MobCategory.MISC).sized(0.75F, 2.8F)
                    .build(Hemomancy.rloc("iron_pillar").toString()));

    // Dormant future encounter entity. Keep the registry id for existing-world compatibility;
    // do not expose or spawn it until its boss-mechanic caller is authored.
    public static final DeferredHolder<EntityType<?>, EntityType<EntityIronWall>> iron_wall = ENTITY_TYPES.register("iron_wall",
            () -> EntityType.Builder.<EntityIronWall>of(EntityIronWall::new, MobCategory.MISC).sized(1.6F, 2.8F)
                    .build(Hemomancy.rloc("iron_wall").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityIronSpike>> iron_spike = ENTITY_TYPES.register("iron_spike",
            () -> EntityType.Builder.<EntityIronSpike>of(EntityIronSpike::new, MobCategory.MISC).sized(1.4F, 1.5F)
                    .build(Hemomancy.rloc("iron_spike").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMorphlingPolypItem>> morphling_polyp_item = ENTITY_TYPES
            .register("morphling_polyp_item", () -> EntityType.Builder
                    .<EntityMorphlingPolypItem>of(EntityMorphlingPolypItem::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .build(Hemomancy.rloc("morphling_polyp_item").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityQliphothSeedItem>> qliphoth_seed_item = ENTITY_TYPES
            .register("qliphoth_seed_item", () -> EntityType.Builder
                    .<EntityQliphothSeedItem>of(EntityQliphothSeedItem::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .build(Hemomancy.rloc("qliphoth_seed_item").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodBoltEntity>> blood_bolt = ENTITY_TYPES.register("blood_bolt",
            () -> EntityType.Builder.<BloodBoltEntity>of(BloodBoltEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(4).setUpdateInterval(20)
                    .build(Hemomancy.rloc("blood_bolt").toString()));

	public static final DeferredHolder<EntityType<?>, EntityType<LivingSickleHookEntity>> living_sickle_hook =
			ENTITY_TYPES.register("living_sickle_hook",
					() -> EntityType.Builder.<LivingSickleHookEntity>of(LivingSickleHookEntity::new, MobCategory.MISC)
							.sized(0.9F, 0.35F).clientTrackingRange(8).updateInterval(1)
							.build(Hemomancy.rloc("living_sickle_hook").toString()));
	public static final DeferredHolder<EntityType<?>, EntityType<LivingFlailHeadProjectileEntity>> living_flail_head =
			ENTITY_TYPES.register("living_flail_head",
					() -> EntityType.Builder.<LivingFlailHeadProjectileEntity>of(
								LivingFlailHeadProjectileEntity::new, MobCategory.MISC)
							.sized(0.85F, 0.85F).clientTrackingRange(12).updateInterval(1)
							.build(Hemomancy.rloc("living_flail_head").toString()));
	public static final DeferredHolder<EntityType<?>, EntityType<VesperScuteProjectileEntity>> vesper_scute_projectile =
			ENTITY_TYPES.register("vesper_scute_projectile",
					() -> EntityType.Builder.<VesperScuteProjectileEntity>of(VesperScuteProjectileEntity::new, MobCategory.MISC)
							.sized(0.65F, 0.24F).clientTrackingRange(12).updateInterval(1)
							.build(Hemomancy.rloc("vesper_scute_projectile").toString()));
	public static final DeferredHolder<EntityType<?>, EntityType<VeinwingFeatherEntity>> veinwing_feather =
			ENTITY_TYPES.register("veinwing_feather",
					() -> EntityType.Builder.<VeinwingFeatherEntity>of(VeinwingFeatherEntity::new, MobCategory.MISC)
							.sized(0.25F, 0.25F).clientTrackingRange(8).updateInterval(1)
							.build(Hemomancy.rloc("veinwing_feather").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodNeedleEntity>> blood_needle = ENTITY_TYPES.register(
            "blood_needle",
            () -> EntityType.Builder.<BloodNeedleEntity>of(BloodNeedleEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(4).setUpdateInterval(20)
                    .build(Hemomancy.rloc("blood_needle").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodBulletEntity>> blood_bullet = ENTITY_TYPES.register(
            "blood_bullet",
            () -> EntityType.Builder.<BloodBulletEntity>of(BloodBulletEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(4).setUpdateInterval(20)
                    .build(Hemomancy.rloc("blood_bullet").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodShotEntity>> blood_shot = ENTITY_TYPES.register("blood_shot",
            () -> EntityType.Builder.<BloodShotEntity>of(BloodShotEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(4).setUpdateInterval(20)
                    .build(Hemomancy.rloc("blood_shot").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityWretchedWill>> wretched_will = ENTITY_TYPES.register(
            "wretched_will",
            () -> EntityType.Builder.<EntityWretchedWill>of(EntityWretchedWill::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(4).setUpdateInterval(20)
                    .build(Hemomancy.rloc("wretched_will").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<DarkArrowEntity>> dark_arrow = ENTITY_TYPES.register("dark_arrow",
            () -> EntityType.Builder.<DarkArrowEntity>of(DarkArrowEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                    .clientTrackingRange(4).setUpdateInterval(20)
                    .build(Hemomancy.rloc("dark_arrow").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<SanguisLanceaEntity>> sanguis_lancea = ENTITY_TYPES.register(
            "sanguis_lancea", () -> EntityType.Builder.<SanguisLanceaEntity>of(SanguisLanceaEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20)
                    .build(Hemomancy.rloc("sanguis_lancea").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HemolyticVialEntity>> hemolytic_vial_projectile = ENTITY_TYPES.register(
            "hemolytic_vial_projectile",
            () -> EntityType.Builder.<HemolyticVialEntity>of(HemolyticVialEntity::new, MobCategory.MISC).sized(0.25F, 0.25F)
                    .clientTrackingRange(4).setUpdateInterval(10)
                    .build(Hemomancy.rloc("hemolytic_vial_projectile").toString()));

    public static TagKey<EntityType<?>> createTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Hemomancy.rloc(name));
    }

    @SubscribeEvent
    public static void onRegisterSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        Hemomancy.LOGGER.info("[Hemomancy] Registering spawn placements...");
        event.register(EntityInit.chitinite.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ChitiniteEntity::canSpawnInCave,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.fervent_chitinite.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FerventChitiniteEntity::canSpawnInCave,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.barbed_urchin.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BarbedUrchinEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.blood_lantern_jelly.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BloodLanternJellyEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.prism_cuttle.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PrismCuttleEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.mnemonic_whale.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MnemonicWhaleEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.hemolymphopoda.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HemolymphopodaEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.bog_revenant.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BogRevenantEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.fargone.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FargoneEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.abhorent_thought.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbhorentThoughtEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.desiccant.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DesiccantEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.crimson_doe.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CrimsonDoeEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.verdigris_moth.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, VerdigrisMothEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.hematic_burrower.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HematicBurrowerEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.morphling_polyp.get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MorphlingPolypEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.scarlet_serpent.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ScarletSerpentEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.hemojelly.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HemojellyEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.venous_strider.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, VenousStriderEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.lantern_tick.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LanternTickEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        // Dimension-biome mobs — wired in BiomeInit but need SpawnPlacements so the engine can place them.
        event.register(EntityInit.thirster.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ThirsterEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.lump_of_thought.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, LumpOfThoughtEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.erythromycelium_eruptus.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ErythromyceliumEruptusEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.blood_drunk_puppeteer.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BloodDrunkPuppeteerEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.chthonian.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ChthonianEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.chthonian_queen.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ChthonianQueenEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.venom_rib_centipede.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, VenomRibCentipedeEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.fungling.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FunglingEntity::checkMobSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        Hemomancy.LOGGER.info("[Hemomancy] Spawn placements registered successfully!");
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntityInit.hematic_construct.get(), HematicConstructEntity.setAttributes().build());
        event.put(EntityInit.blood_cloud.get(), BloodConstructEntity.setAttributes().build());
        event.put(EntityInit.iron_pillar.get(), BloodConstructEntity.setAttributes().build());
        event.put(EntityInit.iron_wall.get(), BloodConstructEntity.setAttributes().build());
        event.put(EntityInit.iron_spike.get(), BloodConstructEntity.setAttributes().build());
        event.put(EntityInit.wretched_will.get(), BloodConstructEntity.setAttributes().build());
        event.put(EntityInit.leech.get(), LeechEntity.setAttributes().build());
        event.put(EntityInit.bloodlicker.get(), BloodlickerEntity.setAttributes().build());
        event.put(EntityInit.bog_revenant.get(), BogRevenantEntity.setAttributes().build());
        event.put(EntityInit.fargone.get(), FargoneEntity.setAttributes().build());
        event.put(EntityInit.fungling.get(), FargoneEntity.setAttributes().build());
        event.put(EntityInit.thirster.get(), ThirsterEntity.setAttributes().build());
        event.put(EntityInit.chitinite.get(), ChitiniteEntity.setAttributes().build());
        event.put(EntityInit.fervent_chitinite.get(), ChitiniteEntity.setAttributes().build());
        event.put(EntityInit.chthonian.get(), ChthonianEntity.setAttributes().build());
        event.put(EntityInit.chthonian_queen.get(), ChthonianQueenEntity.setAttributes().build());
        event.put(EntityInit.lump_of_thought.get(), LumpOfThoughtEntity.setAttributes().build());
        event.put(EntityInit.barbed_urchin.get(), BarbedUrchinEntity.setAttributes().build());
        event.put(EntityInit.chalybeate_snail.get(), ChalybeateSnailEntity.setAttributes().build());
        event.put(EntityInit.blood_lantern_jelly.get(), BloodLanternJellyEntity.setAttributes().build());
        event.put(EntityInit.mnemonic_whale.get(), MnemonicWhaleEntity.setAttributes().build());
        event.put(EntityInit.brined_votary.get(), BrinedVotaryEntity.setAttributes().build());
        event.put(EntityInit.prism_cuttle.get(), PrismCuttleEntity.setAttributes().build());
        event.put(EntityInit.hemolymphopoda.get(), HemolymphopodaEntity.setAttributes().build());
        event.put(EntityInit.lantern_tick.get(), LanternTickEntity.setAttributes().build());
        event.put(EntityInit.abhorent_thought.get(), AbhorentThoughtEntity.setAttributes().build());
        event.put(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusEntity.setAttributes().build());
        event.put(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerEntity.setAttributes().build());
        event.put(EntityInit.will.get(), WillEntity.setAttributes().build());
        event.put(EntityInit.morphling_polyp.get(), MorphlingPolypEntity.setAttributes().build());
        event.put(EntityInit.enthralled_doll.get(), EnthralledDollEntity.setAttributes().build());
        event.put(EntityInit.blood_thrall.get(), BloodThrallEntity.setAttributes().build());
        event.put(EntityInit.veinwing_vulture.get(), VeinwingVultureEntity.setAttributes().build());
        event.put(EntityInit.marrow_spitter.get(), MarrowSpitterEntity.setAttributes().build());
        event.put(EntityInit.gorebound_hulk.get(), GoreboundHulkEntity.setAttributes().build());
        event.put(EntityInit.mnemonist_puppet.get(), MnemonistPuppetEntity.setAttributes().build());
        event.put(EntityInit.scarlet_mummer.get(), ScarletMummerEntity.setAttributes().build());
		event.put(EntityInit.sanguine_hound.get(), SanguineHoundEntity.setAttributes().build());
        event.put(EntityInit.drudge.get(), DrudgeEntity.setAttributes().build());
        event.put(EntityInit.unstained_zealot.get(), UnstainedZealotEntity.setAttributes().build());
        event.put(EntityInit.unstained_guardian.get(), UnstainedGuardianEntity.setAttributes().build());
        event.put(EntityInit.unstained_acolyte.get(), UnstainedAcolyteEntity.setAttributes().build());
        event.put(EntityInit.unstained_scout.get(), UnstainedScoutEntity.setAttributes().build());
        event.put(EntityInit.spectral_companion.get(), PaleIntercessionEntity.setAttributes().build());
        event.put(EntityInit.phantasmal_echo.get(), PhantasmalEchoEntity.setAttributes().build());
        event.put(EntityInit.harbinger_hermit.get(), HarbingerHermitEntity.setAttributes().build());
        event.put(EntityInit.harbinger_alchemist.get(), HarbingerAlchemistEntity.setAttributes().build());
        event.put(EntityInit.harbinger_artificer.get(), HarbingerArtificerEntity.setAttributes().build());
        event.put(EntityInit.harbinger_cicatrix_anchorite.get(), HarbingerCicatrixAnchoriteEntity.setAttributes().build());
        event.put(EntityInit.harbinger_mnemonist.get(), HarbingerMnemonistEntity.setAttributes().build());
        event.put(EntityInit.harbinger_vicar.get(), HarbingerVicarEntity.setAttributes().build());
        event.put(EntityInit.harbinger_voyager.get(), HarbingerVoyagerEntity.setAttributes().build());
        event.put(EntityInit.harbinger_votary_wayfarer.get(), HarbingerVotaryWayfarerEntity.setAttributes().build());
        event.put(EntityInit.hemorath.get(), HemorathEntity.setAttributes().build());
        event.put(EntityInit.seraphae.get(), SeraphaeEntity.setAttributes().build());
        event.put(EntityInit.seraphae_fragment.get(), SeraphaeFragmentEntity.setAttributes().build());
        event.put(EntityInit.annetta_knowles.get(), AnnettaKnowlesEntity.setAttributes().build());
        event.put(EntityInit.stained_priestess.get(), StainedPriestessEntity.setAttributes().build());
        event.put(EntityInit.latent_annetta_infection.get(), LatentAnnettaInfectionEntity.setAttributes().build());
        event.put(EntityInit.putriciel.get(), PutricielEntity.setAttributes().build());
        event.put(EntityInit.velorum.get(), VelorumEntity.setAttributes().build());
        event.put(EntityInit.vesper_crowned_refusal.get(), VesperTheCrownedRefusalEntity.setAttributes().build());
        event.put(EntityInit.vesper_evening_star.get(), VesperTheEveningStarEntity.setAttributes().build());
        event.put(EntityInit.mycophant.get(), MycophantEntity.setAttributes().build());
        event.put(EntityInit.desiccant.get(), DesiccantEntity.setAttributes().build());
        event.put(EntityInit.crimson_doe.get(), CrimsonDoeEntity.setAttributes().build());
        event.put(EntityInit.verdigris_moth.get(), VerdigrisMothEntity.setAttributes().build());
        event.put(EntityInit.hematic_burrower.get(), HematicBurrowerEntity.setAttributes().build());
        event.put(EntityInit.scarlet_serpent.get(), ScarletSerpentEntity.setAttributes().build());
        event.put(EntityInit.hemojelly.get(), HemojellyEntity.setAttributes().build());
        event.put(EntityInit.venous_strider.get(), VenousStriderEntity.setAttributes().build());
        event.put(EntityInit.tooth_pecks.get(), ToothPecksEntity.setAttributes().build());
        event.put(EntityInit.venom_rib_centipede.get(), VenomRibCentipedeEntity.setAttributes().build());
    }

    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        // Spawn placements are now registered via RegisterSpawnPlacementsEvent above.
    }
}
