package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.plant.PelagicColonyBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat;
import net.minecraft.core.*;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.shapes.*;
import javax.annotation.Nullable;

public class PelagicColonyBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public enum Kind { ANEMONE, BONE_WORM, TUBE_WORM }
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty RETRACTED = BooleanProperty.create("retracted");
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public final Kind kind;
    public PelagicColonyBlock(Kind kind, Properties properties) {
        super(properties); this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, true).setValue(RETRACTED, false).setValue(HALF, DoubleBlockHalf.LOWER));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(p -> new PelagicColonyBlock(kind, p)); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(WATERLOGGED, RETRACTED, HALF); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState(); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (kind) {
            case ANEMONE -> box(2, 0, 2, 14, state.getValue(RETRACTED) ? 3 : 8, 14);
            case BONE_WORM -> box(3, 0, 3, 13, state.getValue(RETRACTED) ? 2 : 7, 13);
            case TUBE_WORM -> box(2, 0, 2, 14, state.getValue(HALF) == DoubleBlockHalf.LOWER ? 16 : 12, 14);
        };
    }
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var level = context.getLevel(); var pos = context.getClickedPos();
        if (!PelagicHabitat.sourceWater(level, pos)) return null;
        if (kind == Kind.TUBE_WORM && !PelagicHabitat.sourceWater(level, pos.above())) return null;
        return defaultBlockState().canSurvive(level, pos) ? defaultBlockState() : null;
    }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (!PelagicHabitat.loaded(level, pos.below()) || !state.getValue(WATERLOGGED)) return false;
        var below = level.getBlockState(pos.below());
        if (kind == Kind.TUBE_WORM && state.getValue(HALF) == DoubleBlockHalf.UPPER)
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        if (!below.isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        return switch (kind) {
            case ANEMONE -> true;
            case BONE_WORM -> below.is(Blocks.BONE_BLOCK);
            case TUBE_WORM -> PelagicHabitat.mineral(below)
                    && PelagicHabitat.nearBlock(level, pos.below(), 4, 2, s -> s.is(Blocks.MAGMA_BLOCK));
        };
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (kind == Kind.TUBE_WORM) level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor,
                                             LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        boolean lostHalf = kind == Kind.TUBE_WORM && (state.getValue(HALF) == DoubleBlockHalf.LOWER && direction == Direction.UP
                || state.getValue(HALF) == DoubleBlockHalf.UPPER && direction == Direction.DOWN)
                && (!neighbor.is(this) || neighbor.getValue(HALF) == state.getValue(HALF));
        if (lostHalf || !canSurvive(state, level, pos)) return state.getFluidState().createLegacyBlock();
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && kind == Kind.TUBE_WORM) {
            var other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            if (level.getBlockState(other).is(this))
                level.setBlock(other, level.getBlockState(other).getFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
    @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;
        var root = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (kind == Kind.ANEMONE && living.tickCount % 20 == 0)
            living.hurt(level.damageSources().cactus(), .5F);
        if (level.getBlockEntity(root) instanceof PelagicColonyBlockEntity colony) colony.retract();
    }
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new PelagicColonyBlockEntity(pos, state) : null;
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntityInit.pelagic_colony.get(), PelagicColonyBlockEntity::tick);
    }
}
