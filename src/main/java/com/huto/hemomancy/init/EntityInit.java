package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.EntityLeech;

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
	
	public static final RegistryObject<EntityType<EntityLeech>> leech = ENTITY_TYPES.register("leech",
			() -> EntityType.Builder.<EntityLeech>create(EntityLeech::new, EntityClassification.CREATURE).size(0.4F, 0.1F)
					.build(new ResourceLocation(Hemomancy.MOD_ID, "leech").toString()));

	@SubscribeEvent
	public static void registerAttributes(final FMLCommonSetupEvent event) {
		GlobalEntityTypeAttributes.put(EntityInit.leech.get(), EntityLeech.setAttributes().create());

		
	}
	
}
