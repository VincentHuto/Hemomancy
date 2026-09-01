package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utility class for placing and removing filler blocks in multi-block structures.
 */
public class MultiBlockHelper {

    /**
     * Checks if all filler positions are replaceable (air, water, etc.)
     */
    public static boolean canPlace(Level level, BlockPos mainPos, IMultiBlock multiBlock) {
        for (BlockPos offset : multiBlock.getFillerOffsets()) {
            BlockPos fillerPos = mainPos.offset(offset);
            if (!level.getBlockState(fillerPos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Places filler blocks at all offset positions and links them to the main block.
     */
    public static void placeFillers(Level level, BlockPos mainPos, IMultiBlock multiBlock) {
        for (BlockPos offset : multiBlock.getFillerOffsets()) {
            BlockPos fillerPos = mainPos.offset(offset);
            boolean waterlogged = level.getFluidState(fillerPos).is(FluidTags.WATER);
            level.setBlockAndUpdate(fillerPos, BlockInit.filler_block.get().defaultBlockState()
                    .setValue(FillerBlock.WATERLOGGED, waterlogged));
            BlockEntity be = level.getBlockEntity(fillerPos);
            if (be instanceof FillerBlockEntity filler) {
                filler.setMainBlockPos(mainPos);
            }
        }
    }

    /**
     * Removes all filler blocks associated with a main block position.
     */
    public static void removeFillers(Level level, BlockPos mainPos, IMultiBlock multiBlock) {
        for (BlockPos offset : multiBlock.getFillerOffsets()) {
            BlockPos fillerPos = mainPos.offset(offset);
            BlockState fillerState = level.getBlockState(fillerPos);
            if (fillerState.is(BlockInit.filler_block.get())) {
                if (fillerState.getValue(FillerBlock.WATERLOGGED)) {
                    level.setBlockAndUpdate(fillerPos, Blocks.WATER.defaultBlockState());
                } else {
                    level.removeBlock(fillerPos, false);
                }
            }
        }
    }
}

