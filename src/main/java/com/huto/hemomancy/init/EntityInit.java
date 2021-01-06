package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.EntityLeech;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbTracking;

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

	@SubscribeEvent
	public static void registerAttributes(final FMLCommonSetupEvent event) {
		GlobalEntityTypeAttributes.put(EntityInit.leech.get(), EntityLeech.setAttributes().create());

	}

}
