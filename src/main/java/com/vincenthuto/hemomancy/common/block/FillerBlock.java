package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A filler block used by multi-block structures. It stores a reference to the
 * main block position via its block entity and delegates interactions to the main block.
 */
public class FillerBlock extends BaseEntityBlock {

    public FillerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FillerBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Invisible — the main block handles rendering
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    /**
     * Delegate right-click interactions to the main block.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos != null) {
            BlockState mainState = level.getBlockState(mainPos);
            return mainState.getBlock().use(mainState, level, mainPos, player, hand, hit);
        }
        return InteractionResult.PASS;
    }

    /**
     * Delegate left-click / attack to the main block.
     */
    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos != null) {
            BlockState mainState = level.getBlockState(mainPos);
            mainState.getBlock().attack(mainState, level, mainPos, player);
        }
    }

    /**
     * When a filler is broken directly (e.g. by a player in creative), destroy
     * the main block instead, which will clean up all fillers.
     */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockPos mainPos = getMainBlockPos(level, pos);
            if (mainPos != null && !level.isClientSide) {
                BlockState mainState = level.getBlockState(mainPos);
                if (mainState.getBlock() instanceof IMultiBlock) {
                    level.destroyBlock(mainPos, true);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    /**
     * Fillers should not drop anything themselves — the main block handles drops.
     */
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos != null && !level.isClientSide) {
            BlockState mainState = level.getBlockState(mainPos);
            if (mainState.getBlock() instanceof IMultiBlock) {
                level.destroyBlock(mainPos, !player.isCreative());
            }
        }
        // Don't call super to prevent double-breaking issues
    }

    @Nullable
    private BlockPos getMainBlockPos(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FillerBlockEntity filler) {
            return filler.getMainBlockPos();
        }
        return null;
    }
}

