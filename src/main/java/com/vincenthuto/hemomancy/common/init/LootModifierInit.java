package com.vincenthuto.hemomancy.common.init;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.loot.modifier.AddItemModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class LootModifierInit {
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister
			.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Hemomancy.MOD_ID);

	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> ADD_ITEM = LOOT_MODIFIERS
			.register("add_item", () -> AddItemModifier.CODEC);
    /** Catches from a chum pool (core-loop Track 7). */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> CHUM_CATCH = LOOT_MODIFIERS
            .register("chum_catch", () -> com.vincenthuto.hemomancy.common.loot.modifier.ChumCatchModifier.CODEC);
    /** A ripe crop's harvest on over-fed ground (core-loop round two, P7). */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> TAINTED_HARVEST = LOOT_MODIFIERS
            .register("tainted_harvest", () -> com.vincenthuto.hemomancy.common.loot.modifier.TaintedHarvestModifier.CODEC);
    /** Stamps clotting Flasks and Jugs in generated loot with the time they dropped (FX-2). */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<? extends IGlobalLootModifier>> STAMP_BOTTLED = LOOT_MODIFIERS
            .register("stamp_bottled", () -> com.vincenthuto.hemomancy.common.loot.modifier.StampBottledModifier.CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>,MapCodec<? extends IGlobalLootModifier>> ANCIENT_RECORDING=LOOT_MODIFIERS.register("ancient_recording",()->com.vincenthuto.hemomancy.common.loot.modifier.AncientRecordingLootModifier.CODEC);

}
