package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.enchanting.ScriptoriumLegacyMigration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkSerializer.class)
public abstract class MixinScriptoriumChunkMigration {
    @ModifyVariable(method = "read", at = @At("HEAD"), argsOnly = true, remap = false)
    private static CompoundTag hemomancy$migrateScriptorium(CompoundTag tag) {
        ScriptoriumLegacyMigration.migrateChunk(tag);
        return tag;
    }
}
