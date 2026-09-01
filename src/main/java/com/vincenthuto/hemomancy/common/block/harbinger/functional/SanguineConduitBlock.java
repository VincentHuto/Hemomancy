package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.HarbingerProgressScreen;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.ItemSanguineConduit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineConduitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The Sanguine Conduit in its placed form — a covenant anchor driven into the
 * ground by an Illuminatus who has reached Degree 5. Right-clicking it opens
 * the Harbinger skill-tree screen, the same as right-clicking the item in air.
 * <p>
 * The block has a minimal {@link SanguineConduitBlockEntity} solely to support
 * the BER that renders the pulsing blood-oath rings.
 * <p>
 * Placement is gated in
 * {@link ItemSanguineConduit#useOn} to
 * require Initiatory Degree ≥ 5; the block itself imposes no runtime gate.
 */
public class SanguineConduitBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {

    public static final MapCodec<SanguineConduitBlock> CODEC = simpleCodec(SanguineConduitBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape ORB_SHAPE = Block.box(5.0D, 5.0D, 5.0D, 11.0D, 12.0D, 11.0D);

    public SanguineConduitBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return WaterloggedBlockSupport.fluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                     LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        WaterloggedBlockSupport.scheduleWaterTick(state, level, currentPos);
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    // ── BlockEntity (needed for the ring BER) ─────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SanguineConduitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return null; // No tick needed — rings are rendered purely client-side via the BER
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return ORB_SHAPE;
    }

    // ── Interaction: open the Harbinger skill-tree screen ────────────────────

    /**
     * Opens the Harbinger skill-tree screen when right-clicked without an item,
     * mirroring the behaviour of the Sanguine Conduit item when used in air.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult result) {
        if (level.isClientSide) {
            HarbingerProgressScreen.openScreen();
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Forwards item-in-hand right-clicks to the screen as well, so the screen
     * opens regardless of whether the player is holding something.
     */
    @Override
    protected net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                                                  BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult result) {
        if (level.isClientSide) {
            HarbingerProgressScreen.openScreen();
        }
        return net.minecraft.world.ItemInteractionResult.SUCCESS;
    }
}

