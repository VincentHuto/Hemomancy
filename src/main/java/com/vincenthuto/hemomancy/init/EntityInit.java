package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodBolt;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodBullet;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodCloud;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodCloudCarrier;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodNeedle;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodOrbDirected;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodOrbTracking;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodShot;
import com.vincenthuto.hemomancy.entity.blood.EntityWretchedWill;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronPillar;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronSpike;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronWall;
import com.vincenthuto.hemomancy.entity.item.EntityMorphlingPolypItem;
import com.vincenthuto.hemomancy.entity.projectile.EntityTrackingPests;
import com.vincenthuto.hemomancy.entity.projectile.EntityTrackingSerpent;

import net.minecraft.resources.ResourceLocation;
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

	// Mobs
//	public static final RegistryObject<EntityType<EntityLeech>> leech = ENTITY_TYPES.register("leech",
//			() -> EntityType.Builder.<EntityLeech>of(EntityLeech::new, MobCategory.CREATURE).sized(0.4F, 0.1F)
//					.build(new ResourceLocation(Hemomancy.MOD_ID, "leech").toString()));
//	public static final RegistryObject<EntityType<EntityFargone>> fargone = ENTITY_TYPES.register("fargone",
//			() -> EntityType.Builder.<EntityFargone>of(EntityFargone::new, MobCategory.MONSTER).sized(1F, 1F)
//					.build(new ResourceLocation(Hemomancy.MOD_ID, "fargone").toString()));
//	public static final RegistryObject<EntityType<EntityThirster>> thirster = ENTITY_TYPES.register("thirster",
//			() -> EntityType.Builder.<EntityThirster>of(EntityThirster::new, MobCategory.MONSTER).sized(1F, 1F)
//					.build(new ResourceLocation(Hemomancy.MOD_ID, "thirster").toString()));
//	public static final RegistryObject<EntityType<EntityAbhorentThought>> abhorent_thought = ENTITY_TYPES
//			.register("abhorent_thought",
//					() -> EntityType.Builder.<EntityAbhorentThought>of(EntityAbhorentThought::new, MobCategory.MONSTER)
//							.sized(1.5f, 3F)
//							.build(new ResourceLocation(Hemomancy.MOD_ID, "abhorent_thought").toString()));
//	public static final RegistryObject<EntityType<EntityLumpOfThought>> lump_of_thought = ENTITY_TYPES.register(
//			"lump_of_thought",
//			() -> EntityType.Builder.<EntityLumpOfThought>of(EntityLumpOfThought::new, MobCategory.MONSTER)
//					.sized(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "lump_of_thought").toString()));
//	public static final RegistryObject<EntityType<EntityMorphlingPolyp>> morphling_polyp = ENTITY_TYPES.register(
//			"morphling_polyp",
//			() -> EntityType.Builder.<EntityMorphlingPolyp>of(EntityMorphlingPolyp::new, MobCategory.MONSTER)
//					.sized(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "morphling_polyp").toString()));
//
//	public static final RegistryObject<EntityType<EntityDrudge>> drudge = ENTITY_TYPES.register("drudge",
//			() -> EntityType.Builder.<EntityDrudge>of(EntityDrudge::new, MobCategory.CREATURE).sized(1f, 0.5f)
//					.build(new ResourceLocation(Hemomancy.MOD_ID, "drudge").toString()));
//	public static final RegistryObject<EntityType<EntityFungling>> fungling = ENTITY_TYPES.register("fungling",
//			() -> EntityType.Builder.<EntityFungling>of(EntityFungling::new, MobCategory.CREATURE).sized(1F, 1F)
//					.build(new ResourceLocation(Hemomancy.MOD_ID, "fungling").toString()));
//	public static final RegistryObject<EntityType<EntityChitinite>> chitinite = ENTITY_TYPES.register("chitinite",
//			() -> EntityType.Builder.<EntityChitinite>of(EntityChitinite::new, MobCategory.CREATURE).sized(1F, 1F)
//					.build(new ResourceLocation(Hemomancy.MOD_ID, "chitinite").toString()));
//	public static final RegistryObject<EntityType<EntityChthonian>> chthonian = ENTITY_TYPES.register("chthonian",
//			() -> EntityType.Builder.<EntityChthonian>of(EntityChthonian::new, MobCategory.MONSTER).sized(1F, 1F)
//					.build(new ResourceLocation(Hemomancy.MOD_ID, "chthonian").toString()));
//	public static final RegistryObject<EntityType<EntityChthonianQueen>> chthonian_queen = ENTITY_TYPES.register(
//			"chthonian_queen",
//			() -> EntityType.Builder.<EntityChthonianQueen>of(EntityChthonianQueen::new, MobCategory.MONSTER)
//					.sized(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "chthonian").toString()));

	// Projectiles
	public static final RegistryObject<EntityType<EntityBloodOrbDirected>> directed_blood_orb = ENTITY_TYPES.register(
			"directed_blood_orb",
			() -> EntityType.Builder.<EntityBloodOrbDirected>of(EntityBloodOrbDirected::new, MobCategory.MISC)
					.setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "directed_blood_orb").toString()));

	public static final RegistryObject<EntityType<EntityBloodOrbTracking>> tracking_blood_orb = ENTITY_TYPES.register(
			"tracking_blood_orb",
			() -> EntityType.Builder.<EntityBloodOrbTracking>of(EntityBloodOrbTracking::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_blood_orb").toString()));
	public static final RegistryObject<EntityType<EntityBloodCloudCarrier>> blood_cloud_carrier = ENTITY_TYPES.register(
			"blood_cloud_carrier",
			() -> EntityType.Builder.<EntityBloodCloudCarrier>of(EntityBloodCloudCarrier::new, MobCategory.MISC)
					.setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_cloud_carrier").toString()));
	public static final RegistryObject<EntityType<EntityBloodCloud>> blood_cloud = ENTITY_TYPES.register("blood_cloud",
			() -> EntityType.Builder.<EntityBloodCloud>of(EntityBloodCloud::new, MobCategory.MISC).setTrackingRange(150)
					.setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).sized(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_cloud").toString()));

	public static final RegistryObject<EntityType<EntityTrackingSerpent>> tracking_snake = ENTITY_TYPES.register(
			"tracking_snake",
			() -> EntityType.Builder.<EntityTrackingSerpent>of(EntityTrackingSerpent::new, MobCategory.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true)
					.sized(0.25F, 0.25F).build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_snake").toString()));
	public static final RegistryObject<EntityType<EntityTrackingPests>> tracking_pests = ENTITY_TYPES.register(
			"tracking_pests",
			() -> EntityType.Builder.<EntityTrackingPests>of(EntityTrackingPests::new, MobCategory.MISC)
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

	public static final RegistryObject<EntityType<EntityBloodBolt>> blood_bolt = ENTITY_TYPES.register("blood_bolt",
			() -> EntityType.Builder.<EntityBloodBolt>of(EntityBloodBolt::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_bolt").toString()));

	public static final RegistryObject<EntityType<EntityBloodNeedle>> blood_needle = ENTITY_TYPES.register(
			"blood_needle",
			() -> EntityType.Builder.<EntityBloodNeedle>of(EntityBloodNeedle::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_needle").toString()));

	public static final RegistryObject<EntityType<EntityBloodBullet>> blood_bullet = ENTITY_TYPES.register(
			"blood_bullet",
			() -> EntityType.Builder.<EntityBloodBullet>of(EntityBloodBullet::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_bullet").toString()));

	public static final RegistryObject<EntityType<EntityBloodShot>> blood_shot = ENTITY_TYPES.register("blood_shot",
			() -> EntityType.Builder.<EntityBloodShot>of(EntityBloodShot::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "blood_shot").toString()));

	public static final RegistryObject<EntityType<EntityWretchedWill>> wretched_will = ENTITY_TYPES.register(
			"wretched_will",
			() -> EntityType.Builder.<EntityWretchedWill>of(EntityWretchedWill::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "wretched_will").toString()));

	public static final RegistryObject<EntityType<EntityBloodShot>> dark_arrow = ENTITY_TYPES.register("dark_arrow",
			() -> EntityType.Builder.<EntityBloodShot>of(EntityBloodShot::new, MobCategory.MISC).sized(0.5F, 0.5F)
					.clientTrackingRange(4).setUpdateInterval(20)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "dark_arrow").toString()));

	@SubscribeEvent
	public static void onAttributeCreate(EntityAttributeCreationEvent event) {
		event.put(EntityInit.blood_cloud.get(), EntityBloodCloud.setAttributes().build());
		event.put(EntityInit.iron_pillar.get(), EntityIronPillar.setAttributes().build());
		event.put(EntityInit.iron_wall.get(), EntityIronWall.setAttributes().build());
		event.put(EntityInit.iron_spike.get(), EntityIronSpike.setAttributes().build());
		event.put(EntityInit.wretched_will.get(), EntityWretchedWill.setAttributes().build());
	}
//		event.put(EntityInit.leech.get(), EntityLeech.setAttributes().build());
//		event.put(EntityInit.fargone.get(), EntityFargone.setAttributes().build());
//		event.put(EntityInit.drudge.get(), EntityDrudge.setAttributes().build());
//		event.put(EntityInit.fungling.get(), EntityFungling.setAttributes().build());
//		event.put(EntityInit.thirster.get(), EntityThirster.setAttributes().build());
//		event.put(EntityInit.chitinite.get(), EntityChitinite.setAttributes().build());
//		event.put(EntityInit.chthonian.get(), EntityChthonian.setAttributes().build());
//		event.put(EntityInit.chthonian_queen.get(), EntityChthonianQueen.setAttributes().build());
//		event.put(EntityInit.lump_of_thought.get(), EntityLumpOfThought.setAttributes().build());
//		event.put(EntityInit.abhorent_thought.get(), EntityAbhorentThought.setAttributes().build());
//		event.put(EntityInit.morphling_polyp.get(), EntityMorphlingPolyp.setAttributes().build());

}
