package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import java.util.List;
import java.util.function.BiConsumer;

public class PhlebotomistsCabinetBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final BooleanProperty GLAZED = BooleanProperty.create("glazed");
    public static final TagKey<Item> GLAZING = TagKey.create(Registries.ITEM, Hemomancy.rloc("phlebotomists_cabinet_glazing"));

    public PhlebotomistsCabinetBlock() {
        super(Properties.of().strength(2.5F, 6).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK));
        registerDefaultState(SpecimenDisplayState.empty(stateDefinition.any()).setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(GLAZED, false));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { for (var property : SpecimenDisplayState.OCCUPIED) builder.add(property); builder.add(FACING, OPEN, GLAZED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PhlebotomistsCabinetBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityInit.phlebotomists_cabinet.get()
                ? (world, pos, current, be) -> PhlebotomistsCabinetBlockEntity.tick(world, pos, current, (PhlebotomistsCabinetBlockEntity) be) : null;
    }
    private void open(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer serverPlayer && !player.isSpectator()
                && level.getBlockEntity(pos) instanceof PhlebotomistsCabinetBlockEntity cabinet) serverPlayer.openMenu(cabinet, pos);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        open(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) return ItemInteractionResult.FAIL;
        if (!state.getValue(GLAZED) && stack.is(GLAZING)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(GLAZED, true), Block.UPDATE_ALL);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 0.45F, 0.8F);
            }
        } else if (state.getValue(GLAZED) && player.isShiftKeyDown() && stack.canPerformAction(ItemKnapper.KNAPPER_DIG)) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(GLAZED, false), Block.UPDATE_ALL);
                // Creative fitting is free; removing it does not generate a pane.
                if (!player.getAbilities().instabuild) {
                    var pane = new ItemStack(Items.GLASS_PANE);
                    if (!player.getInventory().add(pane)) {
                        var facing = state.getValue(FACING);
                        var entity = new ItemEntity(level, pos.getX() + 0.5 + facing.getStepX() * 0.7,
                                pos.getY() + 0.4, pos.getZ() + 0.5 + facing.getStepZ() * 0.7, pane);
                        entity.setDefaultPickUpDelay();
                        level.addFreshEntity(entity);
                    }
                }
                level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.3F, 1.3F);
            }
        } else open(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    public static boolean hasSpecimens(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PhlebotomistsCabinetBlockEntity cabinet && cabinet.hasSpecimens();
    }
    public static void warn(Player player) {
        player.displayClientMessage(Component.translatable("message.hemomancy.cabinet.remove_specimens"), true);
    }
    @Override protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && !player.getAbilities().instabuild && hasSpecimens(level, pos)) warn(player);
    }
    @Override protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return !player.getAbilities().instabuild && hasSpecimens(level, pos) ? 0 : super.getDestroyProgress(state, player, level, pos);
    }
    @Override protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> drops) {
        if (!hasSpecimens(level, pos)) super.onExplosionHit(state, level, pos, explosion, drops);
    }
    @Override public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        return !hasSpecimens(level, pos) && super.canEntityDestroy(state, level, pos, entity);
    }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof PhlebotomistsCabinetBlockEntity cabinet ? cabinet.storage().comparatorSignal() : 0;
    }
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hemomancy.cabinet.capacity"));
        tooltip.add(Component.translatable("tooltip.hemomancy.cabinet.creative").withStyle(net.minecraft.ChatFormatting.RED));
    }
}
