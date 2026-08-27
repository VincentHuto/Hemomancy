package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.tile.functional.GourdvineTapBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Containers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class GourdvineTapBlock extends BaseEntityBlock {

    public static final MapCodec<GourdvineTapBlock> CODEC = simpleCodec(GourdvineTapBlock::new);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 3);
    public static final int MAX_STAGE = 3;

    private static final VoxelShape SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 14.0D, 14.0D);

    public GourdvineTapBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GourdvineTapBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, BlockEntityInit.gourdvine_tap.get(), GourdvineTapBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof GourdvineTapBlockEntity be)) return InteractionResult.SUCCESS;
        ItemStack extracted = be.extractGourd();
        if (!extracted.isEmpty()) {
            if (!player.addItem(extracted)) player.drop(extracted, false);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        int growthBoost = growthBoost(stack);
        if (growthBoost > 0 && state.getValue(STAGE) < MAX_STAGE) {
            int next = GourdvineTapGrowthRules.advance(state.getValue(STAGE), growthBoost);
            level.setBlock(pos, state.setValue(STAGE, next), 3);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.levelEvent(1505, pos, 0); // bonemeal particles
            }
            return ItemInteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof GourdvineTapBlockEntity be)) return ItemInteractionResult.SUCCESS;
        if (be.insertGourd(player, stack)) {
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static int growthBoost(ItemStack stack) {
        if (stack.getItem() instanceof EnzymeItem) return 2;
        if (stack.is(ItemInit.foul_paste.get()) || stack.is(ItemInit.spore_sac.get())) return 1;
        return 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GourdvineTapBlockEntity tap) {
                Containers.dropContents(level, pos, tap);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
