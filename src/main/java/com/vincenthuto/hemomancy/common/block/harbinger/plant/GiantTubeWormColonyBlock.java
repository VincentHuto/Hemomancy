package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class GiantTubeWormColonyBlock extends PelagicColonyBlock {
    public static final IntegerProperty WORMS = IntegerProperty.create("worms", 2, 5);
    public static final MapCodec<GiantTubeWormColonyBlock> CODEC = simpleCodec(GiantTubeWormColonyBlock::new);

    public GiantTubeWormColonyBlock(Properties properties) {
        super(Kind.TUBE_WORM, properties);
        registerDefaultState(defaultBlockState().setValue(WORMS, 2));
    }

    @Override protected MapCodec<GiantTubeWormColonyBlock> codec() { return CODEC; }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORMS);
    }

    @Override protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(asItem()) && state.getValue(WORMS) < 5
                || super.canBeReplaced(state, context);
    }

    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state = context.getLevel().getBlockState(context.getClickedPos());
        if (state.is(this)) return state.setValue(WORMS, Math.min(5, state.getValue(WORMS) + 1));
        return super.getStateForPlacement(context);
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        // Adding to the upper half must not place a third block above the colony.
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        var updated = super.updateShape(state, direction, neighbor, level, pos, neighborPos);
        var partner = state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
        if (updated.is(this) && direction == partner && neighbor.is(this) && neighbor.getValue(HALF) != state.getValue(HALF))
            return updated.setValue(WORMS, neighbor.getValue(WORMS));
        return updated;
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int height = state.getValue(HALF) == DoubleBlockHalf.LOWER ? 16 : state.getValue(RETRACTED) ? 9 : 12;
        return switch (state.getValue(WORMS)) {
            case 2 -> box(2, 0, 8, 12, height, 13);
            case 3 -> box(2, 0, 4, 12, height, 13);
            default -> box(2, 0, 3, 14, height, 13);
        };
    }
}
