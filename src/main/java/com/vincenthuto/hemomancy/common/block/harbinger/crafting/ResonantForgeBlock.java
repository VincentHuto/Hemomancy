package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

public class ResonantForgeBlock extends BaseEntityBlock implements EntityBlock, IMultiBlock {
    public static final MapCodec<ResonantForgeBlock> CODEC = simpleCodec(ResonantForgeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final BlockPos[] NORTH_OFFSETS = {
            new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
            new BlockPos(-1, 1, 0), new BlockPos(1, 1, 0)
    };
    private static final VoxelShape SHAPE_X = Shapes.or(Block.box(-16, 0, 0, 32, 16, 16),
            Block.box(-16, 16, 0, 0, 32, 16), Block.box(16, 16, 0, 32, 32, 16));
    private static final VoxelShape SHAPE_Z = Shapes.or(Block.box(0, 0, -16, 16, 16, 32),
            Block.box(0, 16, -16, 16, 32, 0), Block.box(0, 16, 16, 16, 32, 32));

    public ResonantForgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockPos[] getFillerOffsets() { return NORTH_OFFSETS; }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    private static VoxelShape shape(BlockState state) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_X : SHAPE_Z;
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) { return shape(state); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) { return shape(state); }
    @Override public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) { return Shapes.empty(); }
    @Override public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) { return true; }
    @Override public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) { return 1; }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        return canPlace(context.getLevel(), context.getClickedPos(), state.getValue(FACING)) ? state : null;
    }

    private boolean canPlace(Level level, BlockPos controller, Direction facing) {
        for (BlockPos offset : offsets(facing)) if (!level.getBlockState(controller.offset(offset)).canBeReplaced()) return false;
        return true;
    }

    private static BlockPos[] offsets(Direction facing) {
        Direction right = facing.getClockWise();
        return new BlockPos[] {
                new BlockPos(-right.getStepX(), 0, -right.getStepZ()),
                new BlockPos(right.getStepX(), 0, right.getStepZ()),
                new BlockPos(-right.getStepX(), 1, -right.getStepZ()),
                new BlockPos(right.getStepX(), 1, right.getStepZ())
        };
    }

    public static boolean hasCompleteStructure(Level level, BlockPos controller, BlockState state) {
        if (!(state.getBlock() instanceof ResonantForgeBlock) || !state.hasProperty(FACING)) return false;
        for (BlockPos offset : offsets(state.getValue(FACING))) {
            BlockPos fillerPos = controller.offset(offset);
            if (!level.getBlockState(fillerPos).is(BlockInit.filler_block.get())
                    || !(level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler)
                    || !controller.equals(filler.getMainBlockPos())) return false;
        }
        return true;
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;
        for (BlockPos offset : offsets(state.getValue(FACING))) {
            BlockPos fillerPos = pos.offset(offset);
            boolean waterlogged = level.getFluidState(fillerPos).getType() == Fluids.WATER;
            level.setBlockAndUpdate(fillerPos, BlockInit.filler_block.get().defaultBlockState()
                    .setValue(BlockStateProperties.WATERLOGGED, waterlogged));
            if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) filler.setMainBlockPos(pos);
        }
    }

    private void removeFillers(Level level, BlockPos pos, Direction facing) {
        for (BlockPos offset : offsets(facing)) {
            BlockPos fillerPos = pos.offset(offset);
            if (level.getBlockState(fillerPos).is(BlockInit.filler_block.get())) level.removeBlock(fillerPos, false);
        }
    }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ResonantForgeBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, BlockEntityInit.resonant_forge.get(), ResonantForgeBlockEntity::serverTick);
    }

    private InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof ResonantForgeBlockEntity forge)) return InteractionResult.PASS;
        if (forge.hammerWorn()) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("message.hemomancy.resonant_forge.hammer_worn"), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) serverPlayer.openMenu(forge, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return open(level, pos, player);
    }

    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ResonantForgeBlockEntity forge)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        Part part = part(state, pos, hit);
        if (!level.isClientSide && part == Part.HAMMER && forge.hammerWorn()
                && (forge.depositHammerIron(held) || forge.depositHammerAsh(held))) return ItemInteractionResult.SUCCESS;
        if (!level.isClientSide && part == Part.WHEEL && forge.wheelWorn()
                && (forge.depositWheelAsh(held) || forge.redressWheel(player, hand, held))) return ItemInteractionResult.SUCCESS;
        open(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    public Part part(BlockState state, BlockPos controller, BlockHitResult hit) {
        Direction right = state.getValue(FACING).getClockWise();
        double dx = hit.getLocation().x - (controller.getX() + .5);
        double dz = hit.getLocation().z - (controller.getZ() + .5);
        double side = dx * right.getStepX() + dz * right.getStepZ();
        return side < -.35 ? Part.HAMMER : side > .35 ? Part.WHEEL : Part.BASIN;
    }

    public Part part(BlockState state, BlockPos controller, BlockPos selected) {
        Direction right = state.getValue(FACING).getClockWise();
        BlockPos delta = selected.subtract(controller);
        int side = delta.getX() * right.getStepX() + delta.getZ() * right.getStepZ();
        return side < 0 ? Part.HAMMER : side > 0 ? Part.WHEEL : Part.BASIN;
    }

    @Override public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(blockEntity instanceof ResonantForgeBlockEntity forge)) return super.getDrops(state, params);
        forge.cancelOperation();
        ItemStack station = new ItemStack(this);
        CompoundTagWithoutItems saved = new CompoundTagWithoutItems(forge.saveWithoutMetadata(params.getLevel().registryAccess()));
        BlockItem.setBlockEntityData(station, BlockEntityInit.resonant_forge.get(), saved.tag());
        return List.of(station);
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock())) {
            if (level.getBlockEntity(pos) instanceof ResonantForgeBlockEntity forge && !level.isClientSide) {
                forge.cancelOperation();
                Containers.dropContents(level, pos, forge);
                if (forge.hammerIronDeposited()) popResource(level, pos, new ItemStack(BlockInit.hematic_iron_block.get()));
                if (forge.hammerAshDeposited()) popResource(level, pos, new ItemStack(BlockInit.smouldering_ash_trail.get()));
                if (forge.wheelAshDeposited()) popResource(level, pos, new ItemStack(BlockInit.befouling_ash_trail.get()));
                removeFillers(level, pos, state.getValue(FACING));
            }
        }
        super.onRemove(state, level, pos, next, moving);
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return rotate(state, mirror.getRotation(state.getValue(FACING))); }

    public enum Part { HAMMER, BASIN, WHEEL }

    private record CompoundTagWithoutItems(net.minecraft.nbt.CompoundTag tag) {
        CompoundTagWithoutItems {
            tag.remove("Items"); tag.remove("Operation"); tag.remove("Progress"); tag.remove("TotalTicks");
            tag.remove("ReservedBlood"); tag.remove("HammerIron"); tag.remove("HammerAsh");
            tag.remove("WheelAsh"); tag.remove("HammerRepairBlood"); tag.remove("RiteLocked");
        }
    }
}
