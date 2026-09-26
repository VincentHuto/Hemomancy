package com.vincenthuto.hemomancy.common.block.inscription;

import com.vincenthuto.hemomancy.common.tile.inscription.DictationTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Save-compatible alias. New tables belong to HutosLib. */
public class DictationTableBlock extends com.vincenthuto.hutoslib.common.block.DictationTableBlock {
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DictationTableBlockEntity(pos, state);
    }
}
