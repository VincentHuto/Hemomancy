package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.blood.BloodBoltEntity;
import com.vincenthuto.hemomancy.common.entity.blood.BloodBulletEntity;
import com.vincenthuto.hemomancy.common.entity.blood.BloodCloudCarrierEntity;
import com.vincenthuto.hemomancy.common.entity.blood.BloodConstructEntity;
import com.vincenthuto.hemomancy.common.entity.blood.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.blood.BloodShotEntity;
import com.vincenthuto.hemomancy.common.entity.blood.CloudEntityBlood;
import com.vincenthuto.hemomancy.common.entity.blood.DirectedBloodOrbEntity;
import com.vincenthuto.hemomancy.common.entity.blood.EntityWretchedWill;
import com.vincenthuto.hemomancy.common.entity.blood.TrackingBloodOrbEntity;
import com.vincenthuto.hemomancy.common.entity.blood.iron.EntityIronPillar;
import com.vincenthuto.hemomancy.common.entity.blood.iron.EntityIronSpike;
import com.vincenthuto.hemomancy.common.entity.blood.iron.EntityIronWall;
import com.vincenthuto.hemomancy.common.entity.item.EntityFlyingCharm;
import com.vincenthuto.hemomancy.common.entity.item.EntityMorphlingPolypItem;
import com.vincenthuto.hemomancy.common.entity.mob.AbhorentThoughtEntity;
import com.vincenthuto.hemomancy.common.entity.mob.BarbedUrchinEntity;
import com.vincenthuto.hemomancy.common.entity.mob.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.mob.ChitiniteEntity;
import com.vincenthuto.hemomancy.common.entity.mob.ChthonianEntity;
import com.vincenthuto.hemomancy.common.entity.mob.ChthonianQueenEntity;
import com.vincenthuto.hemomancy.common.entity.mob.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.entity.mob.ErythromyceliumEruptusEntity;
import com.vincenthuto.hemomancy.common.entity.mob.FargoneEntity;
import com.vincenthuto.hemomancy.common.entity.mob.FunglingEntity;
import com.vincenthuto.hemomancy.common.entity.mob.LeechEntity;
import com.vincenthuto.hemomancy.common.entity.mob.LumpOfThoughtEntity;
import com.vincenthuto.hemomancy.common.entity.mob.MorphlingPolypEntity;
import com.vincenthuto.hemomancy.common.entity.mob.ThirsterEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.TrackingPestsEntity;
import com.vincenthuto.hemomancy.common.entity.projectile.TrackingSerpentEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class EntityInit {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
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


	// Mobs
	public static final RegistryObject<EntityType<LeechEntity>> leech = ENTITY_TYPES.register("leech",
			() -> EntityType.Builder.<LeechEntity>of(LeechEntity::new, MobCategory.CREATURE).sized(0.4F, 0.1F)
					.build(Hemomancy.rloc("leech").toString()));
	public static final RegistryObject<EntityType<FargoneEntity>> fargone = ENTITY_TYPES.register("fargone",
			() -> EntityType.Builder.<FargoneEntity>of(FargoneEntity::new, MobCategory.MONSTER).sized(1F, 1F)
					.build(Hemomancy.rloc("fargone").toString()));
	public static final RegistryObject<EntityType<ThirsterEntity>> thirster = ENTITY_TYPES.register("thirster",
			() -> EntityType.Builder.<ThirsterEntity>of(ThirsterEntity::new, MobCategory.MONSTER).sized(1F, 1F)
					.build(Hemomancy.rloc("thirster").toString()));
	
	public static final RegistryObject<EntityType<AbhorentThoughtEntity>> abhorent_thought = ENTITY_TYPES
			.register("abhorent_thought",
					() -> EntityType.Builder.<AbhorentThoughtEntity>of(AbhorentThoughtEntity::new, MobCategory.MONSTER)
							.sized(1.5f, 3F)
							.build(Hemomancy.rloc("abhorent_thought").toString()));

	public static final RegistryObject<EntityType<ErythromyceliumEruptusEntity>> erythromycelium_eruptus = ENTITY_TYPES
			.register("erythromycelium_eruptus",
					() -> EntityType.Builder.<ErythromyceliumEruptusEntity>of(ErythromyceliumEruptusEntity::new, MobCategory.MONSTER)
							.sized(1.5f, 3F)
							.build(Hemomancy.rloc("erythromycelium_eruptus").toString()));
	
	public static final RegistryObject<EntityType<BloodDrunkPuppeteerEntity>> blood_drunk_puppeteer = ENTITY_TYPES
			.register("blood_drunk_puppeteer",
					() -> EntityType.Builder
							.<BloodDrunkPuppeteerEntity>of(BloodDrunkPuppeteerEntity::new, MobCategory.MONSTER)
							.sized(0.6F, 1.8F)
							.build(Hemomancy.rloc("blood_drunk_puppeteer").toString()));

	public static final RegistryObject<EntityType<EnthralledDollEntity>> enthralled_doll = ENTITY_TYPES
			.register("enthralled_doll",
					() -> EntityType.Builder.<EnthralledDollEntity>of(EnthralledDollEntity::new, MobCategory.MONSTER)
							.sized(0.9F, 1F)
							.build(Hemomancy.rloc("enthralled_doll").toString()));


	public static final RegistryObject<EntityType<BarbedUrchinEntity>> barbed_urchin = ENTITY_TYPES.register(
			"barbed_urchin",
			() -> EntityType.Builder.<BarbedUrchinEntity>of(BarbedUrchinEntity::new, MobCategory.WATER_AMBIENT )
					.sized(1F, 1F).build(Hemomancy.rloc("barbed_urchin").toString()));
	
	
	public static final RegistryObject<EntityType<LumpOfThoughtEntity>> lump_of_thought = ENTITY_TYPES.register(
			"lump_of_thought",
			() -> EntityType.Builder.<LumpOfThoughtEntity>of(LumpOfThoughtEntity::new, MobCategory.MONSTER)
					.sized(1F, 1F).build(Hemomancy.rloc("lump_of_thought").toString()));
	public static final RegistryObject<EntityType<MorphlingPolypEntity>> morphling_polyp = ENTITY_TYPES.register(
			"morphling_polyp",
			() -> EntityType.Builder.<MorphlingPolypEntity>of(MorphlingPolypEntity::new, MobCategory.MONSTER)
					.sized(1F, 1F).build(Hemomancy.rloc("morphling_polyp").toString()));

	public static final RegistryObject<EntityType<FunglingEntity>> fungling = ENTITY_TYPES.register("fungling",
			() -> EntityType.Builder.<FunglingEntity>of(FunglingEntity::new, MobCategory.CREATURE).sized(1F, 1F)
					.build(Hemomancy.rloc("fungling").toString()));

	public static final RegistryObject<EntityType<ChitiniteEntity>> chitinite = ENTITY_TYPES.register("chitinite",
			() -> EntityType.Builder.<ChitiniteEntity>of(ChitiniteEntity::new, MobCategory.CREATURE).sized(1F, 1F)
					.build(Hemomancy.rloc("chitinite").toString()));
	public static final RegistryObject<EntityType<ChthonianEntity>> chthonian = ENTITY_TYPES.register("chthonian",
			() -> EntityType.Builder.<ChthonianEntity>of(ChthonianEntity::new, MobCategory.MONSTER).sized(1F, 1F)
					.build(Hemomancy.rloc("chthonian").toString()));
	public static final RegistryObject<EntityType<ChthonianQueenEntity>> chthonian_queen = ENTITY_TYPES.register(
			"chthonian_queen",
			() -> EntityType.Builder.<ChthonianQueenEntity>of(ChthonianQueenEntity::new, MobCategory.MONSTER)
					.sized(1F, 1F).build(Hemomancy.rloc("chthonian").toString()));

	// Projectiles

	public static final RegistryObject<EntityType<EntityFlyingCharm>> flying_charm = ENTITY_TYPES.register(
			"flying_charm",
			() -> EntityType.Builder.<EntityFlyingCharm>of(EntityFlyingCharm::new, MobCategory.MISC).sized(0.25F, 0.25F)
					.clientTrackingRange(4).updateInterval(4)
					.build(Hemomancy.rloc("flying_charm").toString()));

	public static final RegistryObject<EntityType<DirectedBloodOrbEntity>> directed_blood_orb = ENTITY_TYPES.register(
			"directed_blood_orb",
			() -> EntityType.Builder.<DirectedBloodOrbEntity>of(DirectedBloodOrbEntity::new, MobCategory.MISC)
					.setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(Hemomancy.rloc("directed_blood_orb").toString()));

	public static final RegistryObject<EntityType<TrackingBloodOrbEntity>> tracking_blood_orb = ENTITY_TYPES.register(
			"tracking_blood_orb",
			() -> EntityType.Builder.<TrackingBloodOrbEntity>of(TrackingBloodOrbEntity::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(Hemomancy.rloc("tracking_blood_orb").toString()));
	public static final RegistryObject<EntityType<BloodCloudCarrierEntity>> blood_cloud_carrier = ENTITY_TYPES.register(
			"blood_cloud_carrier",
			() -> EntityType.Builder.<BloodCloudCarrierEntity>of(BloodCloudCarrierEntity::new, MobCategory.MISC)
					.setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(Hemomancy.rloc("blood_cloud_carrier").toString()));
	public static final RegistryObject<EntityType<CloudEntityBlood>> blood_cloud = ENTITY_TYPES.register("blood_cloud",
			() -> EntityType.Builder.<CloudEntityBlood>of(CloudEntityBlood::new, MobCategory.MISC).setTrackingRange(150)
					.setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(Hemomancy.rloc("blood_cloud").toString()));

	public static final RegistryObject<EntityType<TrackingSerpentEntity>> tracking_snake = ENTITY_TYPES.register(
			"tracking_snake",
			() -> EntityType.Builder.<TrackingSerpentEntity>of(TrackingSerpentEntity::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
					.sized(0.25F, 0.25F).build(Hemomancy.rloc("tracking_snake").toString()));
	public static final RegistryObject<EntityType<TrackingPestsEntity>> tracking_pests = ENTITY_TYPES.register(
			"tracking_pests",
			() -> EntityType.Builder.<TrackingPestsEntity>of(TrackingPestsEntity::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
					.sized(0.25F, 0.25F).build(Hemomancy.rloc("tracking_pests").toString()));

	public static final RegistryObject<EntityType<EntityIronPillar>> iron_pillar = ENTITY_TYPES.register("iron_pillar",
			() -> EntityType.Builder.<EntityIronPillar>of(EntityIronPillar::new, MobCategory.MISC).sized(0.75F, 2.8F)
					.build(Hemomancy.rloc("iron_pillar").toString()));

	public static final RegistryObject<EntityType<EntityIronWall>> iron_wall = ENTITY_TYPES.register("iron_wall",
			() -> EntityType.Builder.<EntityIronWall>of(EntityIronWall::new, MobCategory.MISC).sized(1.6F, 2.8F)
					.build(Hemomancy.rloc("iron_wall").toString()));

	public static final RegistryObject<EntityType<EntityIronSpike>> iron_spike = ENTITY_TYPES.register("iron_spike",
			() -> EntityType.Builder.<EntityIronSpike>of(EntityIronSpike::new, MobCategory.MISC).sized(1.4F, 1.5F)
					.build(Hemomancy.rloc("iron_spike").toString()));

	public static final RegistryObject<EntityType<EntityMorphlingPolypItem>> morphling_polyp_item = ENTITY_TYPES
			.register("morphling_polyp_item", () -> EntityType.Builder
					.<EntityMorphlingPolypItem>of(EntityMorphlingPolypItem::new, MobCategory.MISC).sized(0.25F, 0.25F)
					.build(Hemomancy.rloc("morphling_polyp_item").toString()));

	public static final RegistryObject<EntityType<BloodBoltEntity>> blood_bolt = ENTITY_TYPES.register("blood_bolt",
			() -> EntityType.Builder.<BloodBoltEntity>of(BloodBoltEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(Hemomancy.rloc("blood_bolt").toString()));

	public static final RegistryObject<EntityType<BloodNeedleEntity>> blood_needle = ENTITY_TYPES.register(
			"blood_needle",
			() -> EntityType.Builder.<BloodNeedleEntity>of(BloodNeedleEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(Hemomancy.rloc("blood_needle").toString()));

	public static final RegistryObject<EntityType<BloodBulletEntity>> blood_bullet = ENTITY_TYPES.register(
			"blood_bullet",
			() -> EntityType.Builder.<BloodBulletEntity>of(BloodBulletEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(Hemomancy.rloc("blood_bullet").toString()));

	public static final RegistryObject<EntityType<BloodShotEntity>> blood_shot = ENTITY_TYPES.register("blood_shot",
			() -> EntityType.Builder.<BloodShotEntity>of(BloodShotEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(Hemomancy.rloc("blood_shot").toString()));

	public static final RegistryObject<EntityType<EntityWretchedWill>> wretched_will = ENTITY_TYPES.register(
			"wretched_will",
			() -> EntityType.Builder.<EntityWretchedWill>of(EntityWretchedWill::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(Hemomancy.rloc("wretched_will").toString()));

	public static final RegistryObject<EntityType<BloodShotEntity>> dark_arrow = ENTITY_TYPES.register("dark_arrow",
			() -> EntityType.Builder.<BloodShotEntity>of(BloodShotEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(Hemomancy.rloc("dark_arrow").toString()));

	public static TagKey<EntityType<?>> createTag(String name){
		return TagKey.create(Registries.ENTITY_TYPE,
				new ResourceLocation(Hemomancy.MOD_ID,name));
	}
	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {
		SpawnPlacements.register(EntityInit.barbed_urchin.get(), SpawnPlacements.Type.IN_WATER,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BarbedUrchinEntity::canSpawnHere);

	}


	@SubscribeEvent
	public static void onAttributeCreate(EntityAttributeCreationEvent event) {
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
		event.put(EntityInit.chthonian.get(), ChthonianEntity.setAttributes().build());
		event.put(EntityInit.chthonian_queen.get(), ChthonianQueenEntity.setAttributes().build());
		event.put(EntityInit.lump_of_thought.get(), LumpOfThoughtEntity.setAttributes().build());
		event.put(EntityInit.barbed_urchin.get(), BarbedUrchinEntity.setAttributes().build());
		event.put(EntityInit.abhorent_thought.get(), AbhorentThoughtEntity.setAttributes().build());
		event.put(EntityInit.erythromycelium_eruptus.get(), ErythromyceliumEruptusEntity.setAttributes().build());
		event.put(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerEntity.setAttributes().build());
		event.put(EntityInit.morphling_polyp.get(), MorphlingPolypEntity.setAttributes().build());
		event.put(EntityInit.enthralled_doll.get(), EnthralledDollEntity.setAttributes().build());

	}

}
