package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import javax.annotation.Nullable;

public class BoneWormColonyBlock extends PelagicColonyBlock {
    public static final IntegerProperty WORMS = IntegerProperty.create("worms", 2, 5);
    public static final MapCodec<BoneWormColonyBlock> CODEC = simpleCodec(BoneWormColonyBlock::new);

    public BoneWormColonyBlock(Properties properties) {
        super(Kind.BONE_WORM, properties);
        registerDefaultState(defaultBlockState().setValue(WORMS, 2));
    }

    @Override protected MapCodec<BoneWormColonyBlock> codec() { return CODEC; }

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

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int count = state.getValue(WORMS);
        double height = state.getValue(RETRACTED) ? (count == 2 ? 4.5 : 5.5) : (count == 2 ? 7 : 8);
        return switch (count) {
            case 2 -> box(3, 0, 8, 12, height, 13);
            case 3 -> box(3, 0, 4, 12, height, 13);
            case 4 -> box(3, 0, 3, 14, height, 13);
            default -> box(2, 0, 3, 14, height, 13);
        };
    }
}
