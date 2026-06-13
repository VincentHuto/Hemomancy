package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class LastDeathMemoryAttachment extends LastDeathMemory implements INBTSerializable<CompoundTag> {
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_PRESENT, hasMemory());
        tag.putString(KEY_DIMENSION, dimensionId());
        tag.putLong(KEY_POS, packedPosition());
        tag.putLong(KEY_TIME, gameTime());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.getBoolean(KEY_PRESENT)) {
            remember(tag.getString(KEY_DIMENSION), tag.getLong(KEY_POS), tag.getLong(KEY_TIME));
        } else {
            clear();
        }
    }
}
