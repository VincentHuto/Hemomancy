package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.station.CreativeStationUpgrades;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A cauldron that has taken projected blood. Stage 0 keeps the vanilla cauldron's body; the Working rites
 * re-band it as a Latticed (1) and then a Druse (2) Condenser in place.
 */
public class SanguineCondenserBlock extends BaseEntityBlock {
    public static final MapCodec<SanguineCondenserBlock> CODEC = simpleCodec(SanguineCondenserBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty STAGE = StationTierProperty.STAGE;

    private static final VoxelShape CAULDRON = Shapes.join(Shapes.block(),
            Shapes.or(box(0, 0, 4, 16, 3, 12), box(4, 0, 0, 12, 3, 16), box(2, 0, 2, 14, 3, 14),
                    box(2, 4, 2, 14, 16, 14)), BooleanOp.ONLY_FIRST);
    private static final VoxelShape[] SHAPES = {CAULDRON, CAULDRON, CAULDRON};

    public SanguineCondenserBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(STAGE, 0));
    }

    /** Fills an empty vanilla cauldron's place with a stage-0 Condenser owned by the projecting player. */
    @Nullable
    public static SanguineCondenserBlockEntity convertCauldron(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        if (!level.getBlockState(pos).is(Blocks.CAULDRON)) return null;
        level.setBlock(pos, BlockInit.sanguine_condenser.get().defaultBlockState()
                .setValue(FACING, owner.getDirection().getOpposite()), Block.UPDATE_ALL);
        if (!(level.getBlockEntity(pos) instanceof SanguineCondenserBlockEntity condenser)) return null;
        condenser.claimOwner(owner);
        return condenser;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(STAGE)];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STAGE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SanguineCondenserBlockEntity condenser)
                || condenser.heldFormations() <= 0) return InteractionResult.PASS;
        if (player instanceof ServerPlayer serverPlayer) condenser.collect(serverPlayer);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Projection tools and every other item keep their own use; only a held formation gathers more. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (CreativeStationUpgrades.tryApply(stack, level, pos, player))
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        if (!stack.is(ItemInit.sanguine_formation.get())) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        return useWithoutItem(state, level, pos, player, hit) == InteractionResult.PASS
                ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                : ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof SanguineCondenserBlockEntity condenser)
            condenser.dropFormations();
        super.onRemove(state, level, pos, next, moving);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SanguineCondenserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return createTickerHelper(type, BlockEntityInit.sanguine_condenser.get(),
                level.isClientSide ? SanguineCondenserBlockEntity::clientTick : SanguineCondenserBlockEntity::serverTick);
    }
}
