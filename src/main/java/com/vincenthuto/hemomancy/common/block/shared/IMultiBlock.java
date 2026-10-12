package com.vincenthuto.hemomancy.common.block.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Interface for blocks that are part of a multi-block structure.
 * The main block implements this to define the filler positions
 * and handle cleanup when the structure is broken.
 */
public interface IMultiBlock {

    /**
     * Returns the relative positions (offsets from the main block) that
     * should be filled with filler blocks.
     * For example, a 1x3x1 block would return {(0,1,0), (0,2,0)}.
     */
    BlockPos[] getFillerOffsets();

    /**
     * Outline and collision of the filler standing at {@code offset} (world space) from the
     * controller, or null to keep a full cube.
     */
    @Nullable
    default VoxelShape fillerShape(BlockState controller, BlockPos offset) {
        return null;
    }

    /**
     * Called to place filler blocks when the main block is placed.
     */
    default void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
        MultiBlockHelper.placeFillers(level, mainPos, this);
    }

    /**
     * Called to remove filler blocks when the main block is broken.
     */
    default void removeFillers(Level level, BlockPos mainPos) {
        MultiBlockHelper.removeFillers(level, mainPos, this);
    }

    /**
     * Checks if there is enough space to place the multi-block structure.
     */
    default boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
        return MultiBlockHelper.canPlace(level, mainPos, this);
    }
}

