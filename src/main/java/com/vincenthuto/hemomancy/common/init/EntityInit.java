package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.CrimsonDoeEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.FunglingEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LeechEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ToothPecksEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VenousStriderEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BarbedUrchinEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.HemojellyEntity;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.*;
import com.vincenthuto.hemomancy.common.entity.boss.hemorath.HollowVesselEntity;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import com.vincenthuto.hemomancy.common.entity.boss.putriciel.PutricielEntity;
import com.vincenthuto.hemomancy.common.entity.boss.velorum.VelorumEntity;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeFragmentEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.*;
import com.vincenthuto.hemomancy.common.entity.mob.monster.HematicConstructEntity;
import com.vincenthuto.hemomancy.common.entity.npc.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.HarbingerHermitEntity;
import com.vincenthuto.hemomancy.common.entity.npc.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.UnstainedAcolyteEntity;
import com.vincenthuto.hemomancy.common.entity.npc.UnstainedGuardianEntity;
import com.vincenthuto.hemomancy.common.entity.npc.UnstainedZealotEntity;
import com.vincenthuto.hemomancy.common.entity.summon.*;
import com.vincenthuto.hemomancy.common.entity.item.EntityFlyingCharm;
import com.vincenthuto.hemomancy.common.entity.item.EntityMorphlingPolypItem;
import com.vincenthuto.hemomancy.common.entity.item.EntityQliphothSeedItem;
import com.vincenthuto.hemomancy.common.entity.projectile.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
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

    public static final DeferredHolder<EntityType<?>, EntityType<SpectralCompanionEntity>> spectral_companion = ENTITY_TYPES.register(
            "spectral_companion",
            () -> EntityType.Builder.of(SpectralCompanionEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("spectral_companion").toString()));

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

    public static final DeferredHolder<EntityType<?>, EntityType<HarbingerVicarEntity>> harbinger_vicar = ENTITY_TYPES.register(
            "harbinger_vicar",
            () -> EntityType.Builder.of(HarbingerVicarEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .build(Hemomancy.rloc("harbinger_vicar").toString()));

    // Boss room: Hematic Construct (inner trial minion)
    public static final DeferredHolder<EntityType<?>, EntityType<HematicConstructEntity>> hematic_construct = ENTITY_TYPES.register(
            "hematic_construct",
            () -> EntityType.Builder.of(HematicConstructEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build(Hemomancy.rloc("hematic_construct").toString()));

    // Boss: Hollow Vessel (Saint Hemorath)
    public static final DeferredHolder<EntityType<?>, EntityType<HollowVesselEntity>> hollow_vessel = ENTITY_TYPES.register(
            "hollow_vessel",
            () -> EntityType.Builder.of(HollowVesselEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.2F)
                    .clientTrackingRange(10)
                    .fireImmune()
                    .build(Hemomancy.rloc("hollow_vessel").toString()));

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

    public static final DeferredHolder<EntityType<?>, EntityType<LeechEntity>> leech = ENTITY_TYPES.register("leech",
            () -> EntityType.Builder.of(LeechEntity::new, MobCategory.CREATURE).sized(0.4F, 0.1F)
                    .build(Hemomancy.rloc("leech").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<FargoneEntity>> fargone = ENTITY_TYPES.register("fargone",
            () -> EntityType.Builder.of(FargoneEntity::new, MobCategory.MONSTER).sized(1F, 1.8F)
                    .build(Hemomancy.rloc("fargone").toString()));
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

    public static final DeferredHolder<EntityType<?>, EntityType<BloodThrallEntity>> blood_thrall = ENTITY_TYPES
            .register("blood_thrall",
                    () -> EntityType.Builder.<BloodThrallEntity>of(BloodThrallEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.7F)
                            .build(Hemomancy.rloc("blood_thrall").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EnthralledDollEntity>> enthralled_doll = ENTITY_TYPES
            .register("enthralled_doll",
                    () -> EntityType.Builder.<EnthralledDollEntity>of(EnthralledDollEntity::new, MobCategory.MONSTER)
                            .sized(0.5F, 0.5F)
                            .build(Hemomancy.rloc("enthralled_doll").toString()));


    public static final DeferredHolder<EntityType<?>, EntityType<BarbedUrchinEntity>> barbed_urchin = ENTITY_TYPES.register(
            "barbed_urchin",
            () -> EntityType.Builder.of(BarbedUrchinEntity::new, MobCategory.WATER_AMBIENT)
                    .sized(1F, 1F).build(Hemomancy.rloc("barbed_urchin").toString()));


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

    public static final DeferredHolder<EntityType<?>, EntityType<ChitiniteEntity>> chitinite = ENTITY_TYPES.register("chitinite",
            () -> EntityType.Builder.of(ChitiniteEntity::new, MobCategory.CREATURE).sized(1F, 0.3F)
                    .build(Hemomancy.rloc("chitinite").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FerventChitiniteEntity>> fervent_chitinite = ENTITY_TYPES.register("fervent_chitinite",
            () -> EntityType.Builder.of(FerventChitiniteEntity::new, MobCategory.CREATURE).sized(1F, 0.3F)
                    .build(Hemomancy.rloc("fervent_chitinite").toString()));


    public static final DeferredHolder<EntityType<?>, EntityType<ChthonianEntity>> chthonian = ENTITY_TYPES.register("chthonian",
            () -> EntityType.Builder.of(ChthonianEntity::new, MobCategory.MONSTER).sized(1F, 1F)
                    .build(Hemomancy.rloc("chthonian").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<ChthonianQueenEntity>> chthonian_queen = ENTITY_TYPES.register(
            "chthonian_queen",
            () -> EntityType.Builder.of(ChthonianQueenEntity::new, MobCategory.MONSTER)
                    .sized(1F, 1F).build(Hemomancy.rloc("chthonian").toString()));

    // New biome mobs
    public static final DeferredHolder<EntityType<?>, EntityType<DessicantEntity>> dessicant = ENTITY_TYPES.register(
            "dessicant",
            () -> EntityType.Builder.of(DessicantEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.5F).build(Hemomancy.rloc("dessicant").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<CruorFiendEntity>> cruor_fiend = ENTITY_TYPES.register(
            "cruor_fiend",
            () -> EntityType.Builder.of(CruorFiendEntity::new, MobCategory.MONSTER)
                    .fireImmune().sized(0.8F, 2.0F).build(Hemomancy.rloc("cruor_fiend").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<VoidDrinkerEntity>> void_drinker = ENTITY_TYPES.register(
            "void_drinker",
            () -> EntityType.Builder.of(VoidDrinkerEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.5F).build(Hemomancy.rloc("void_drinker").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<FrozenClotEntity>> frozen_clot = ENTITY_TYPES.register(
            "frozen_clot",
            () -> EntityType.Builder.of(FrozenClotEntity::new, MobCategory.MONSTER)
                    .sized(1.0F, 1.0F).build(Hemomancy.rloc("frozen_clot").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<AbyssalSiphonEntity>> abyssal_siphon = ENTITY_TYPES.register(
            "abyssal_siphon",
            () -> EntityType.Builder.of(AbyssalSiphonEntity::new, MobCategory.MONSTER)
                    .sized(1.2F, 0.6F).build(Hemomancy.rloc("abyssal_siphon").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<SynapseHoundEntity>> synapse_hound = ENTITY_TYPES.register(
            "synapse_hound",
            () -> EntityType.Builder.of(SynapseHoundEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 1.0F).build(Hemomancy.rloc("synapse_hound").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<MyelinBorerEntity>> myelin_borer = ENTITY_TYPES.register(
            "myelin_borer",
            () -> EntityType.Builder.of(MyelinBorerEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.4F).build(Hemomancy.rloc("myelin_borer").toString()));

    // Passive Mobs
    public static final DeferredHolder<EntityType<?>, EntityType<CrimsonDoeEntity>> crimson_doe = ENTITY_TYPES.register(
            "crimson_doe",
            () -> EntityType.Builder.of(CrimsonDoeEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.4F).build(Hemomancy.rloc("crimson_doe").toString()));

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
    public static final DeferredHolder<EntityType<?>, EntityType<TrackingPestsEntity>> tracking_pests = ENTITY_TYPES.register(
            "tracking_pests",
            () -> EntityType.Builder.<TrackingPestsEntity>of(TrackingPestsEntity::new, MobCategory.MISC)
                    .setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
                    .sized(0.25F, 0.25F).build(Hemomancy.rloc("tracking_pests").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityIronPillar>> iron_pillar = ENTITY_TYPES.register("iron_pillar",
            () -> EntityType.Builder.<EntityIronPillar>of(EntityIronPillar::new, MobCategory.MISC).sized(0.75F, 2.8F)
                    .build(Hemomancy.rloc("iron_pillar").toString()));

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

    public static final DeferredHolder<EntityType<?>, EntityType<BloodShotEntity>> dark_arrow = ENTITY_TYPES.register("dark_arrow",
            () -> EntityType.Builder.<BloodShotEntity>of(BloodShotEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
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
        event.register(EntityInit.hemolymphopoda.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HemolymphopodaEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.fargone.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FargoneEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.abhorent_thought.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbhorentThoughtEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.dessicant.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, DessicantEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.cruor_fiend.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CruorFiendEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.void_drinker.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, VoidDrinkerEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.frozen_clot.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, FrozenClotEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.abyssal_siphon.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, AbyssalSiphonEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.synapse_hound.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SynapseHoundEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.myelin_borer.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, MyelinBorerEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.crimson_doe.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CrimsonDoeEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.hemojelly.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HemojellyEntity::canSpawnHere,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.venous_strider.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, VenousStriderEntity::canSpawnHere,
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
        event.register(EntityInit.chthonian.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ChthonianEntity::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(EntityInit.chthonian_queen.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ChthonianQueenEntity::checkMonsterSpawnRules,
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
        event.put(EntityInit.fargone.get(), FargoneEntity.setAttributes().build());
        event.put(EntityInit.fungling.get(), FargoneEntity.setAttributes().build());
        event.put(EntityInit.thirster.get(), ThirsterEntity.setAttributes().build());
        event.put(EntityInit.chitinite.get(), ChitiniteEntity.setAttributes().build());
        event.put(EntityInit.fervent_chitinite.get(), ChitiniteEntity.setAttributes().build());
        event.put(EntityInit.chthonian.get(), ChthonianEntity.setAttributes().build());
        event.put(EntityInit.chthonian_queen.get(), ChthonianQueenEntity.setAttributes().build());
        event.put(EntityInit.lump_of_thought.get(), LumpOfThoughtEntity.setAttributes().build());
        event.put(EntityInit.barbed_urchin.get(), BarbedUrchinEntity.setAttributes().build());
        event.put(EntityInit.hemolymphopoda.get(), HemolymphopodaEntity.setAttributes().build());
        event.put(EntityInit.abhorent_thought.get(), AbhorentThoughtEntity.setAttributes().build());
        event.put(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusEntity.setAttributes().build());
        event.put(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerEntity.setAttributes().build());
        event.put(EntityInit.morphling_polyp.get(), MorphlingPolypEntity.setAttributes().build());
        event.put(EntityInit.enthralled_doll.get(), EnthralledDollEntity.setAttributes().build());
        event.put(EntityInit.blood_thrall.get(), BloodThrallEntity.setAttributes().build());
        event.put(EntityInit.unstained_zealot.get(), UnstainedZealotEntity.setAttributes().build());
        event.put(EntityInit.unstained_guardian.get(), UnstainedGuardianEntity.setAttributes().build());
        event.put(EntityInit.unstained_acolyte.get(), UnstainedAcolyteEntity.setAttributes().build());
        event.put(EntityInit.spectral_companion.get(), SpectralCompanionEntity.setAttributes().build());
        event.put(EntityInit.harbinger_hermit.get(), HarbingerHermitEntity.setAttributes().build());
        event.put(EntityInit.harbinger_alchemist.get(), HarbingerAlchemistEntity.setAttributes().build());
        event.put(EntityInit.harbinger_vicar.get(), HarbingerVicarEntity.setAttributes().build());
        event.put(EntityInit.hollow_vessel.get(), HollowVesselEntity.setAttributes().build());
        event.put(EntityInit.seraphae.get(), SeraphaeEntity.setAttributes().build());
        event.put(EntityInit.seraphae_fragment.get(), SeraphaeFragmentEntity.setAttributes().build());
        event.put(EntityInit.annetta_knowles.get(), AnnettaKnowlesEntity.setAttributes().build());
        event.put(EntityInit.putriciel.get(), PutricielEntity.setAttributes().build());
        event.put(EntityInit.velorum.get(), VelorumEntity.setAttributes().build());
        event.put(EntityInit.dessicant.get(), DessicantEntity.setAttributes().build());
        event.put(EntityInit.cruor_fiend.get(), CruorFiendEntity.setAttributes().build());
        event.put(EntityInit.void_drinker.get(), VoidDrinkerEntity.setAttributes().build());
        event.put(EntityInit.frozen_clot.get(), FrozenClotEntity.setAttributes().build());
        event.put(EntityInit.abyssal_siphon.get(), AbyssalSiphonEntity.setAttributes().build());
        event.put(EntityInit.synapse_hound.get(), SynapseHoundEntity.setAttributes().build());
        event.put(EntityInit.myelin_borer.get(), MyelinBorerEntity.setAttributes().build());
        event.put(EntityInit.crimson_doe.get(), CrimsonDoeEntity.setAttributes().build());
        event.put(EntityInit.hemojelly.get(), HemojellyEntity.setAttributes().build());
        event.put(EntityInit.venous_strider.get(), VenousStriderEntity.setAttributes().build());
        event.put(EntityInit.tooth_pecks.get(), ToothPecksEntity.setAttributes().build());
    }

    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        // Spawn placements are now registered via RegisterSpawnPlacementsEvent above.
    }
}
