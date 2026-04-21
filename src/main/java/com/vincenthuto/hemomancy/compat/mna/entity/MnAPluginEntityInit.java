package com.vincenthuto.hemomancy.compat.mna.entity;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class MnAPluginEntityInit {

	public static final DeferredRegister<EntityType<?>> MNA_ENTITY_TYPES = DeferredRegister
			.create(ForgeRegistries.ENTITY_TYPES, Hemomancy.MOD_ID);

	public static final RegistryObject<EntityType<SanguilithEntity>> sanguilith = MNA_ENTITY_TYPES.register(
			"sanguilith", () -> EntityType.Builder.<SanguilithEntity>of(SanguilithEntity::new, MobCategory.MISC)
					.sized(1.5f, 3.25F).build(Hemomancy.rloc("sanguilith").toString()));

	public static void onAttributeCreate(EntityAttributeCreationEvent event) {
		event.put(MnAPluginEntityInit.sanguilith.get(), SanguilithEntity.createMobAttributes().build());

	}

}
