package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsFieldCaseBlockEntity;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import java.util.function.BiConsumer;

public class PhlebotomistsFieldCaseBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 8, 16);
    public PhlebotomistsFieldCaseBlock() {
        super(Properties.of().strength(1.5F, 6).sound(SoundType.WOOL).noOcclusion().pushReaction(PushReaction.BLOCK));
        registerDefaultState(SpecimenDisplayState.empty(stateDefinition.any()).setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { for (var property : SpecimenDisplayState.OCCUPIED) builder.add(property); builder.add(FACING, OPEN); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PhlebotomistsFieldCaseBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityInit.phlebotomists_field_case.get()
                ? (world, pos, current, be) -> PhlebotomistsFieldCaseBlockEntity.tick(world, pos, current, (PhlebotomistsFieldCaseBlockEntity) be) : null;
    }
    private void interact(Level level, BlockPos pos, Player player, boolean emptyHand) {
        if (level.isClientSide || player.isSpectator() || !(level.getBlockEntity(pos) instanceof PhlebotomistsFieldCaseBlockEntity be)) return;
        if (player.isShiftKeyDown() && emptyHand) {
            if (!be.pack(player, false)) player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.hemomancy.field_case.busy"), true);
        } else if (player instanceof ServerPlayer server && be.available()) server.openMenu(be, pos);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        interact(level, pos, player, true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        interact(level, pos, player, stack.isEmpty());
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (level.isClientSide) return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        boolean packed = level.getBlockEntity(pos) instanceof PhlebotomistsFieldCaseBlockEntity be && be.pack(player, true);
        if (!packed) level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        return packed;
    }
    private static boolean occupied(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PhlebotomistsFieldCaseBlockEntity be && be.hasSpecimens();
    }
    @Override protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> drops) {
        if (!occupied(level, pos)) super.onExplosionHit(state, level, pos, explosion, drops);
    }
    @Override public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return !occupied(level, pos) && super.canEntityDestroy(state, level, pos, entity);
    }
    @Override public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity be, ItemStack tool) {
        // Packing already delivered exactly one case; vanilla loot would duplicate it.
        player.awardStat(net.minecraft.stats.Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(.005F);
    }
}
