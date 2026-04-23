package com.vincenthuto.hemomancy.common.init;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.loot.modifier.AddItemModifier;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class LootModifierInit {
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister
			.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Hemomancy.MOD_ID);

	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> ADD_ITEM = LOOT_MODIFIERS
			.register("add_item", () -> AddItemModifier.CODEC);

}
