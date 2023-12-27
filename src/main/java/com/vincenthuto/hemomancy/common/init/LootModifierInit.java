package com.vincenthuto.hemomancy.common.init;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.loot.modifier.AddItemModifier;

import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class LootModifierInit {
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister
			.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Hemomancy.MOD_ID);

	public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ITEM = LOOT_MODIFIERS
			.register("add_item", AddItemModifier.CODEC);

}
