package com.vincenthuto.hemomancy.common.block.shared;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A filler block used by multi-block structures. It stores a reference to the
 * main block position via its block entity and delegates interactions to the main block.
 */
public class FillerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
  public static final MapCodec<FillerBlock> CODEC = simpleCodec(FillerBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public FillerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return WaterloggedBlockSupport.fluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
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
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return isHematicArmatureCenterFiller(level, pos) ? Shapes.empty() : Shapes.block();
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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos == null || mainPos.equals(pos)) {
            return InteractionResult.PASS;
        }

        BlockState mainState = level.getBlockState(mainPos);
        if (mainState.getBlock() == this) {
            return InteractionResult.PASS;
        }

        BlockHitResult redirectedHit = new BlockHitResult(hit.getLocation(), hit.getDirection(), mainPos, hit.isInside());
        return mainState.useWithoutItem(level, player, redirectedHit);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos == null || mainPos.equals(pos)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockState mainState = level.getBlockState(mainPos);
        if (mainState.getBlock() == this) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockHitResult redirectedHit = new BlockHitResult(hit.getLocation(), hit.getDirection(), mainPos, hit.isInside());
        return mainState.useItemOn(stack, level, player, hand, redirectedHit);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        destroyMainBlock(level, pos, !player.isCreative(), player.isCreative());
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
                    if (mainState.is(BlockInit.qliphoth_bloom.get())) {
                        return;
                    }
                    level.destroyBlock(mainPos, true);
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        destroyMainBlock(level, pos, !player.isCreative(), player.isCreative());
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void destroyMainBlock(Level level, BlockPos pos, boolean dropBlock, boolean creativePlayer) {
        BlockPos mainPos = getMainBlockPos(level, pos);
        if (mainPos != null && !level.isClientSide) {
            BlockState mainState = level.getBlockState(mainPos);
            if (mainState.getBlock() instanceof IMultiBlock) {
                if (!MultiBlockBreakRules.shouldDestroyMainFromPlayer(
                        mainState.is(BlockInit.qliphoth_bloom.get()), creativePlayer)) {
                    return;
                }
                level.destroyBlock(mainPos, dropBlock);
            }
        }
    }

    @Nullable
    private BlockPos getMainBlockPos(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FillerBlockEntity filler) {
            return filler.getMainBlockPos();
        }
        return null;
    }

    private static boolean isHematicArmatureCenterFiller(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FillerBlockEntity filler)) {
            return false;
        }
        BlockPos mainPos = filler.getMainBlockPos();
        if (mainPos == null || !level.getBlockState(mainPos).is(BlockInit.hematic_armature.get())) {
            return false;
        }
        BlockPos relative = pos.subtract(mainPos);
        return relative.equals(new BlockPos(0, 1, 0)) || relative.equals(new BlockPos(0, 2, 0));
    }
}
