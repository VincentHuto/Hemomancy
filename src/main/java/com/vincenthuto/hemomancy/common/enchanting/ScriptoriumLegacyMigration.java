package com.vincenthuto.hemomancy.common.enchanting;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;

/** Compatibility for saves made before Scriptorium stages shared one block. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ScriptoriumLegacyMigration {
    private ScriptoriumLegacyMigration() {}

    @SubscribeEvent
    public static void registerAliases(ModifyRegistriesEvent event) {
        for (String old : new String[]{"eightfold_scriptorium", "monolithic_scriptorium"}) {
            BuiltInRegistries.BLOCK.addAlias(Hemomancy.rloc(old), Hemomancy.rloc("enzymatic_scriptorium"));
            BuiltInRegistries.ITEM.addAlias(Hemomancy.rloc(old), Hemomancy.rloc("enzymatic_scriptorium"));
        }
    }

    public static void migrateState(CompoundTag state) {
        int stage = switch (state.getString("Name")) {
            case "hemomancy:eightfold_scriptorium" -> 1;
            case "hemomancy:monolithic_scriptorium" -> 2;
            default -> -1;
        };
        if (stage < 0) return;
        state.putString("Name", "hemomancy:enzymatic_scriptorium");
        CompoundTag properties = state.getCompound("Properties");
        properties.putString("stage", Integer.toString(stage));
        state.put("Properties", properties);
    }

    public static void migrateChunk(CompoundTag chunk) {
        for (Tag section : chunk.getList("sections", Tag.TAG_COMPOUND)) {
            for (Tag state : ((CompoundTag) section).getCompound("block_states").getList("palette", Tag.TAG_COMPOUND)) {
                migrateState((CompoundTag) state);
            }
        }
    }
}
