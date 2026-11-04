package com.vincenthuto.hemomancy.common.tile.inscription;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** Retains the old entity ID and inventory NBT for existing tables. */
public class DictationTableBlockEntity extends com.vincenthuto.hutoslib.common.block.entity.DictationTableBlockEntity {
    public DictationTableBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.dictation_table.get(), pos, state);
    }
}
