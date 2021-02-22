package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.EntityIronPillar;
import com.huto.hemomancy.entity.EntityIronSpike;
import com.huto.hemomancy.entity.EntityLeech;
import com.huto.hemomancy.entity.EntityMorphlingPolypItem;
import com.huto.hemomancy.entity.drudge.EntityDrudge;
import com.huto.hemomancy.entity.mob.EntityChitinite;
import com.huto.hemomancy.entity.mob.EntityFargone;
import com.huto.hemomancy.entity.mob.EntityFungling;
import com.huto.hemomancy.entity.mob.EntityThirster;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbTracking;
import com.huto.hemomancy.entity.projectile.EntityTrackingPests;
import com.huto.hemomancy.entity.projectile.EntityTrackingSerpent;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class EntityInit {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES,
			Hemomancy.MOD_ID);

	// Mobs
	public static final RegistryObject<EntityType<EntityLeech>> leech = ENTITY_TYPES.register("leech",
			() -> EntityType.Builder.<EntityLeech>create(EntityLeech::new, EntityClassification.CREATURE)
					.size(0.4F, 0.1F).build(new ResourceLocation(Hemomancy.MOD_ID, "leech").toString()));
	public static final RegistryObject<EntityType<EntityFargone>> fargone = ENTITY_TYPES.register("fargone",
			() -> EntityType.Builder.<EntityFargone>create(EntityFargone::new, EntityClassification.MONSTER)
					.size(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "fargone").toString()));
	public static final RegistryObject<EntityType<EntityThirster>> thirster = ENTITY_TYPES.register("thirster",
			() -> EntityType.Builder.<EntityThirster>create(EntityThirster::new, EntityClassification.MONSTER)
					.size(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "thirster").toString()));
	public static final RegistryObject<EntityType<EntityDrudge>> drudge = ENTITY_TYPES.register("drudge",
			() -> EntityType.Builder.<EntityDrudge>create(EntityDrudge::new, EntityClassification.CREATURE)
					.size(1f, 0.5f).build(new ResourceLocation(Hemomancy.MOD_ID, "drudge").toString()));
	public static final RegistryObject<EntityType<EntityFungling>> fungling = ENTITY_TYPES.register("fungling",
			() -> EntityType.Builder.<EntityFungling>create(EntityFungling::new, EntityClassification.CREATURE)
					.size(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "fungling").toString()));
	
	public static final RegistryObject<EntityType<EntityChitinite>> chitinite = ENTITY_TYPES.register("chitinite",
			() -> EntityType.Builder.<EntityChitinite>create(EntityChitinite::new, EntityClassification.CREATURE)
					.size(1F, 1F).build(new ResourceLocation(Hemomancy.MOD_ID, "chitinite").toString()));
	
	// Projectiles
	public static final RegistryObject<EntityType<EntityBloodOrbDirected>> directed_blood_orb = ENTITY_TYPES.register(
			"directed_blood_orb",
			() -> EntityType.Builder
					.<EntityBloodOrbDirected>create(EntityBloodOrbDirected::new, EntityClassification.MISC)
					.setTrackingRange(150).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).size(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "directed_blood_orb").toString()));

	public static final RegistryObject<EntityType<EntityBloodOrbTracking>> tracking_blood_orb = ENTITY_TYPES.register(
			"tracking_blood_orb",
			() -> EntityType.Builder
					.<EntityBloodOrbTracking>create(EntityBloodOrbTracking::new, EntityClassification.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).size(0.5F, 0.5F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_blood_orb").toString()));

	public static final RegistryObject<EntityType<EntityTrackingSerpent>> tracking_snake = ENTITY_TYPES.register(
			"tracking_snake",
			() -> EntityType.Builder
					.<EntityTrackingSerpent>create(EntityTrackingSerpent::new, EntityClassification.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).size(0.25F, 0.25F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_snake").toString()));
	public static final RegistryObject<EntityType<EntityTrackingPests>> tracking_pests = ENTITY_TYPES.register(
			"tracking_pests",
			() -> EntityType.Builder.<EntityTrackingPests>create(EntityTrackingPests::new, EntityClassification.MISC)
					.setTrackingRange(64).setUpdateInterval(12).setShouldReceiveVelocityUpdates(true).size(0.25F, 0.25F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "tracking_pests").toString()));

	public static final RegistryObject<EntityType<EntityIronPillar>> iron_pillar = ENTITY_TYPES.register("iron_pillar",
			() -> EntityType.Builder.<EntityIronPillar>create(EntityIronPillar::new, EntityClassification.MISC)
					.size(1.4F, 1.5F).build(new ResourceLocation(Hemomancy.MOD_ID, "iron_pillar").toString()));

	public static final RegistryObject<EntityType<EntityIronSpike>> iron_spike = ENTITY_TYPES.register("iron_spike",
			() -> EntityType.Builder.<EntityIronSpike>create(EntityIronSpike::new, EntityClassification.MISC)
					.size(1.4F, 1.5F).build(new ResourceLocation(Hemomancy.MOD_ID, "iron_spike").toString()));

	public static final RegistryObject<EntityType<EntityMorphlingPolypItem>> morphling_polyp = ENTITY_TYPES.register(
			"morphling_polyp",
			() -> EntityType.Builder
					.<EntityMorphlingPolypItem>create(EntityMorphlingPolypItem::new, EntityClassification.MISC)
					.size(0.25F, 0.25F).build(new ResourceLocation(Hemomancy.MOD_ID, "morphling_polyp").toString()));

	@SubscribeEvent
	public static void registerAttributes(final FMLCommonSetupEvent event) {
		GlobalEntityTypeAttributes.put(EntityInit.leech.get(), EntityLeech.setAttributes().create());
		GlobalEntityTypeAttributes.put(EntityInit.fargone.get(), EntityFargone.setAttributes().create());
		GlobalEntityTypeAttributes.put(EntityInit.drudge.get(), EntityDrudge.setAttributes().create());
		GlobalEntityTypeAttributes.put(EntityInit.fungling.get(), EntityFungling.setAttributes().create());
		GlobalEntityTypeAttributes.put(EntityInit.thirster.get(), EntityThirster.setAttributes().create());
		GlobalEntityTypeAttributes.put(EntityInit.iron_pillar.get(), EntityIronPillar.setAttributes().create());
		GlobalEntityTypeAttributes.put(EntityInit.iron_spike.get(), EntityIronSpike.setAttributes().create());
		GlobalEntityTypeAttributes.put(EntityInit.chitinite.get(), EntityChitinite.setAttributes().create());

	}

}
