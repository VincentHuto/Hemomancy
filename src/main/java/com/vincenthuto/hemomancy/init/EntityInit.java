package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.BloodBoltEntity;
import com.vincenthuto.hemomancy.entity.blood.BloodBulletEntity;
import com.vincenthuto.hemomancy.entity.blood.BloodCloudCarrierEntity;
import com.vincenthuto.hemomancy.entity.blood.BloodNeedleEntity;
import com.vincenthuto.hemomancy.entity.blood.BloodShotEntity;
import com.vincenthuto.hemomancy.entity.blood.CloudEntityBlood;
import com.vincenthuto.hemomancy.entity.blood.DirectedBloodOrbEntity;
import com.vincenthuto.hemomancy.entity.blood.EntityWretchedWill;
import com.vincenthuto.hemomancy.entity.blood.TrackingBloodOrbEntity;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronPillar;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronSpike;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronWall;
import com.vincenthuto.hemomancy.entity.item.EntityFlyingCharm;
import com.vincenthuto.hemomancy.entity.item.EntityMorphlingPolypItem;
import com.vincenthuto.hemomancy.entity.mob.AbhorentThoughtEntity;
import com.vincenthuto.hemomancy.entity.mob.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.entity.mob.ChitiniteEntity;
import com.vincenthuto.hemomancy.entity.mob.ChthonianEntity;
import com.vincenthuto.hemomancy.entity.mob.ChthonianQueenEntity;
import com.vincenthuto.hemomancy.entity.mob.EnthralledDollEntity;
import com.vincenthuto.hemomancy.entity.mob.FargoneEntity;
import com.vincenthuto.hemomancy.entity.mob.FunglingEntity;
import com.vincenthuto.hemomancy.entity.mob.LeechEntity;
import com.vincenthuto.hemomancy.entity.mob.LumpOfThoughtEntity;
import com.vincenthuto.hemomancy.entity.mob.MorphlingPolypEntity;
import com.vincenthuto.hemomancy.entity.mob.ThirsterEntity;
import com.vincenthuto.hemomancy.entity.projectile.TrackingPestsEntity;
import com.vincenthuto.hemomancy.entity.projectile.TrackingSerpentEntity;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class EntityInit {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES,
			Hemomancy.MOD_ID);
	
	public static final TagKey<EntityType<?>> FUNGAL = createTag("fungal");
	public static final TagKey<EntityType<?>> UMBRAL = createTag("umbral");
	public static final TagKey<EntityType<?>> INCANDESCENT = createTag("incandescent");
	public static final TagKey<EntityType<?>> FERRIC = createTag("ferric");
	public static final TagKey<EntityType<?>> VIVACIOUS = createTag("vivacious");
	public static final TagKey<EntityType<?>> RUINOUS = createTag("ruinous");
	public static final TagKey<EntityType<?>> NEUROTIC = createTag("neurotic");
	public static final TagKey<EntityType<?>> FERVENT = createTag("fervent");
	public static final TagKey<EntityType<?>> FRIGID = createTag("frigid");

	
	// Mobs
	public static final RegistryObject<EntityType<LeechEntity>> leech = ENTITY_TYPES.register("leech",
			() -> EntityType.Builder.<LeechEntity>of(LeechEntity::new, MobCategory.CREATURE).sized(0.4F, 0.1F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "leech").toString()));
	public static final RegistryObject<EntityType<FargoneEntity>> fargone = ENTITY_TYPES.register("fargone",
			() -> EntityType.Builder.<FargoneEntity>of(FargoneEntity::new, MobCategory.MONSTER).sized(1F, 1F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "fargone").toString()));
	public static final RegistryObject<EntityType<ThirsterEntity>> thirster = ENTITY_TYPES.register("thirster",
			() -> EntityType.Builder.<ThirsterEntity>of(ThirsterEntity::new, MobCategory.MONSTER).sized(1F, 1F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "thirster").toString()));
	public static final RegistryObject<EntityType<AbhorentThoughtEntity>> abhorent_thought = ENTITY_TYPES
			.register("abhorent_thought",
					() -> EntityType.Builder.<AbhorentThoughtEntity>of(AbhorentThoughtEntity::new, MobCategory.MONSTER)
							.sized(1.5f, 3F)
							.build(new ResourceLocation(Hemomancy.MOD_ID, "abhorent_thought").toString()));
	public static final RegistryObject<EntityType<BloodDrunkPuppeteerEntity>> blood_drunk_puppeteer = ENTITY_TYPES
			.register("blood_drunk_puppeteer",
					() -> EntityType.Builder
							.<BloodDrunkPuppeteerEntity>of(BloodDrunkPuppeteerEntity::new, MobCategory.MONSTER)
							.sized(0.6F, 1.8F)
							.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_drunk_puppeteer").toString()));

	public static final RegistryObject<EntityType<EnthralledDollEntity>> enthralled_doll = ENTITY_TYPES
			.register("enthralled_doll",
					() -> EntityType.Builder.<EnthralledDollEntity>of(EnthralledDollEntity::new, MobCategory.MONSTER)
							.sized(0.9F, 1F)
							.build(new ResourceLocation(Hemomancy.MOD_ID, "enthralled_doll").toString()));

	public static final RegistryObject<EntityType<LumpOfThoughtEntity>> lump_of_thought = ENTITY_TYPES.register(
			"lump_of_thought",
			() -> EntityType.Builder.<LumpOfThoughtEntity>of(LumpOfThoughtEntity::new, MobCategory.MONSTER)
					.sized(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "lump_of_thought").toString()));
	public static final RegistryObject<EntityType<MorphlingPolypEntity>> morphling_polyp = ENTITY_TYPES.register(
			"morphling_polyp",
			() -> EntityType.Builder.<MorphlingPolypEntity>of(MorphlingPolypEntity::new, MobCategory.MONSTER)
					.sized(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "morphling_polyp").toString()));

	public static final RegistryObject<EntityType<FunglingEntity>> fungling = ENTITY_TYPES.register("fungling",
			() -> EntityType.Builder.<FunglingEntity>of(FunglingEntity::new, MobCategory.CREATURE).sized(1F, 1F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "fungling").toString()));

	public static final RegistryObject<EntityType<ChitiniteEntity>> chitinite = ENTITY_TYPES.register("chitinite",
			() -> EntityType.Builder.<ChitiniteEntity>of(ChitiniteEntity::new, MobCategory.CREATURE).sized(1F, 1F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "chitinite").toString()));
	public static final RegistryObject<EntityType<ChthonianEntity>> chthonian = ENTITY_TYPES.register("chthonian",
			() -> EntityType.Builder.<ChthonianEntity>of(ChthonianEntity::new, MobCategory.MONSTER).sized(1F, 1F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "chthonian").toString()));
	public static final RegistryObject<EntityType<ChthonianQueenEntity>> chthonian_queen = ENTITY_TYPES.register(
			"chthonian_queen",
			() -> EntityType.Builder.<ChthonianQueenEntity>of(ChthonianQueenEntity::new, MobCategory.MONSTER)
					.sized(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "chthonian").toString()));

	// Projectiles

	public static final RegistryObject<EntityType<EntityFlyingCharm>> flying_charm = ENTITY_TYPES.register(
			"flying_charm",
			() -> EntityType.Builder.<EntityFlyingCharm>of(EntityFlyingCharm::new, MobCategory.MISC).sized(0.25F, 0.25F)
					.clientTrackingRange(4).updateInterval(4)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "flying_charm").toString()));

	public static final RegistryObject<EntityType<DirectedBloodOrbEntity>> directed_blood_orb = ENTITY_TYPES.register(
			"directed_blood_orb",
			() -> EntityType.Builder.<DirectedBloodOrbEntity>of(DirectedBloodOrbEntity::new, MobCategory.MISC)
					.setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "directed_blood_orb").toString()));

	public static final RegistryObject<EntityType<TrackingBloodOrbEntity>> tracking_blood_orb = ENTITY_TYPES.register(
			"tracking_blood_orb",
			() -> EntityType.Builder.<TrackingBloodOrbEntity>of(TrackingBloodOrbEntity::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_blood_orb").toString()));
	public static final RegistryObject<EntityType<BloodCloudCarrierEntity>> blood_cloud_carrier = ENTITY_TYPES.register(
			"blood_cloud_carrier",
			() -> EntityType.Builder.<BloodCloudCarrierEntity>of(BloodCloudCarrierEntity::new, MobCategory.MISC)
					.setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_cloud_carrier").toString()));
	public static final RegistryObject<EntityType<CloudEntityBlood>> blood_cloud = ENTITY_TYPES.register("blood_cloud",
			() -> EntityType.Builder.<CloudEntityBlood>of(CloudEntityBlood::new, MobCategory.MISC).setTrackingRange(150)
					.setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_cloud").toString()));

	public static final RegistryObject<EntityType<TrackingSerpentEntity>> tracking_snake = ENTITY_TYPES.register(
			"tracking_snake",
			() -> EntityType.Builder.<TrackingSerpentEntity>of(TrackingSerpentEntity::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
					.sized(0.25F, 0.25F).build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_snake").toString()));
	public static final RegistryObject<EntityType<TrackingPestsEntity>> tracking_pests = ENTITY_TYPES.register(
			"tracking_pests",
			() -> EntityType.Builder.<TrackingPestsEntity>of(TrackingPestsEntity::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
					.sized(0.25F, 0.25F).build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_pests").toString()));

	public static final RegistryObject<EntityType<EntityIronPillar>> iron_pillar = ENTITY_TYPES.register("iron_pillar",
			() -> EntityType.Builder.<EntityIronPillar>of(EntityIronPillar::new, MobCategory.MISC).sized(0.75F, 2.8F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "iron_pillar").toString()));

	public static final RegistryObject<EntityType<EntityIronWall>> iron_wall = ENTITY_TYPES.register("iron_wall",
			() -> EntityType.Builder.<EntityIronWall>of(EntityIronWall::new, MobCategory.MISC).sized(1.6F, 2.8F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "iron_wall").toString()));

	public static final RegistryObject<EntityType<EntityIronSpike>> iron_spike = ENTITY_TYPES.register("iron_spike",
			() -> EntityType.Builder.<EntityIronSpike>of(EntityIronSpike::new, MobCategory.MISC).sized(1.4F, 1.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "iron_spike").toString()));

	public static final RegistryObject<EntityType<EntityMorphlingPolypItem>> morphling_polyp_item = ENTITY_TYPES
			.register("morphling_polyp_item", () -> EntityType.Builder
					.<EntityMorphlingPolypItem>of(EntityMorphlingPolypItem::new, MobCategory.MISC).sized(0.25F, 0.25F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "morphling_polyp_item").toString()));

	public static final RegistryObject<EntityType<BloodBoltEntity>> blood_bolt = ENTITY_TYPES.register("blood_bolt",
			() -> EntityType.Builder.<BloodBoltEntity>of(BloodBoltEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_bolt").toString()));

	public static final RegistryObject<EntityType<BloodNeedleEntity>> blood_needle = ENTITY_TYPES.register(
			"blood_needle",
			() -> EntityType.Builder.<BloodNeedleEntity>of(BloodNeedleEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_needle").toString()));

	public static final RegistryObject<EntityType<BloodBulletEntity>> blood_bullet = ENTITY_TYPES.register(
			"blood_bullet",
			() -> EntityType.Builder.<BloodBulletEntity>of(BloodBulletEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_bullet").toString()));

	public static final RegistryObject<EntityType<BloodShotEntity>> blood_shot = ENTITY_TYPES.register("blood_shot",
			() -> EntityType.Builder.<BloodShotEntity>of(BloodShotEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_shot").toString()));

	public static final RegistryObject<EntityType<EntityWretchedWill>> wretched_will = ENTITY_TYPES.register(
			"wretched_will",
			() -> EntityType.Builder.<EntityWretchedWill>of(EntityWretchedWill::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "wretched_will").toString()));

	public static final RegistryObject<EntityType<BloodShotEntity>> dark_arrow = ENTITY_TYPES.register("dark_arrow",
			() -> EntityType.Builder.<BloodShotEntity>of(BloodShotEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "dark_arrow").toString()));

	@SubscribeEvent
	public static void onAttributeCreate(EntityAttributeCreationEvent event) {
		event.put(EntityInit.blood_cloud.get(), CloudEntityBlood.setAttributes().build());
		event.put(EntityInit.iron_pillar.get(), EntityIronPillar.setAttributes().build());
		event.put(EntityInit.iron_wall.get(), EntityIronWall.setAttributes().build());
		event.put(EntityInit.iron_spike.get(), EntityIronSpike.setAttributes().build());
		event.put(EntityInit.wretched_will.get(), EntityWretchedWill.setAttributes().build());
		event.put(EntityInit.leech.get(), LeechEntity.setAttributes().build());
		event.put(EntityInit.fargone.get(), FargoneEntity.setAttributes().build());
		event.put(EntityInit.fungling.get(), FargoneEntity.setAttributes().build());
		event.put(EntityInit.thirster.get(), ThirsterEntity.setAttributes().build());
		event.put(EntityInit.chitinite.get(), ChitiniteEntity.setAttributes().build());
		event.put(EntityInit.chthonian.get(), ChthonianEntity.setAttributes().build());
		event.put(EntityInit.chthonian_queen.get(), ChthonianQueenEntity.setAttributes().build());
		event.put(EntityInit.lump_of_thought.get(), LumpOfThoughtEntity.setAttributes().build());
		event.put(EntityInit.abhorent_thought.get(), AbhorentThoughtEntity.setAttributes().build());
		event.put(EntityInit.blood_drunk_puppeteer.get(), BloodDrunkPuppeteerEntity.setAttributes().build());
		event.put(EntityInit.morphling_polyp.get(), MorphlingPolypEntity.setAttributes().build());
		event.put(EntityInit.enthralled_doll.get(), EnthralledDollEntity.setAttributes().build());

	}

	public static TagKey<EntityType<?>> createTag(String name){
		return TagKey.create(Registry.ENTITY_TYPE_REGISTRY,
				new ResourceLocation(Hemomancy.MOD_ID,name));
	}
	
}
