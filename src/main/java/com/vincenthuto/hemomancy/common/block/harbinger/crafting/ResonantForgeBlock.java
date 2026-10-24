package com.vincenthuto.hemomancy.common.block.harbinger.crafting;


import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
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
import java.util.Map;

public class ResonantForgeBlock extends BaseEntityBlock implements EntityBlock, IMultiBlock {
    public static final MapCodec<ResonantForgeBlock> CODEC = simpleCodec(ResonantForgeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** Hammer and grinder bays below the upper row, shared by every stage (north-facing model pixels). */
    private static final double[][] LOWER = {
            {-16, 0, 0, 0, 16, 16}, {-16, 0, 16, 0, 9, 32}, {-11, 9, 19.5, 0, 16, 29.5},
            {16, 0, 0, 32, 16, 16}, {16, 0, 16, 32, 5, 32}, {16, 5, 16, 30.25, 15, 32}};
    private static final double[] GRINDER_FEED = {17, 16, 17.5, 25.5, 17, 25};
    private static final double[] GRINDER_ARM = {16.75, 17, 16, 18.75, 28.5, 22.5};
    /** Per-stage cells traced from each stage body plus its animated parts at rest; the basin's headroom stays open. */
    private static final List<Map<Direction, Map<BlockPos, VoxelShape>>> CELLS = List.of(
            ModelShapes.cells(concat(LOWER, new double[][] {
                    {0, 0, 1, 16, 12, 16}, {0, 12, 2.25, 16, 14, 12.75}, {0, 0, 16, 16, 10, 31},
                    {-12, 16, 0, -1, 20, 16}, {-8.25, 20, 0, -4.75, 31, 2.25}, {-8.25, 23, 2.25, -4.75, 30, 16},
                    {-9, 16, 16, -0.5, 20, 29.5}, {-7, 20, 16, 0, 25, 30},
                    {16, 16, 0, 32, 21, 14}, {20.25, 21, 0, 32, 29, 11.75}, GRINDER_FEED, GRINDER_ARM})),
            ModelShapes.cells(concat(LOWER, new double[][] {
                    {0, 0, 1, 16, 12, 16}, {0, 12, 2.25, 16, 14, 12.75}, {0, 0, 16, 16, 11, 31},
                    {2.5, 11, 20, 16, 14, 30.5},
                    {-13.5, 16, 0, -1, 20, 16}, {-8.5, 20, 0, -1, 28, 9}, {-16, 28, 0, -0.5, 31, 4},
                    {-9, 16, 16, -0.5, 21, 29.5}, {-3, 21, 27, 0, 25.5, 30},
                    {16, 16, 0, 32, 30, 16}, {16, 30, 6, 31, 31.75, 13.5}, GRINDER_FEED, GRINDER_ARM})),
            ModelShapes.cells(concat(LOWER, new double[][] {
                    {0, 0, 1, 16, 16, 16}, {0, 0, 16, 16, 11, 31},
                    {-13.75, 16, 0, -1, 20, 16}, {-13.75, 20, 0, -1, 26, 9.5}, {-16, 26, 0, -0.5, 32, 16},
                    {-9, 16, 16, -0.5, 21, 29.5}, {-3, 21, 27, 0, 26, 30}, {-14, 26, 16, 0, 31.25, 30},
                    {16, 16, 0, 32, 31, 16}, {17.25, 31, 0, 32, 32, 8},
                    {22.5, 16, 16.25, 25.5, 19, 25}, {16, 19, 16, 29, 32, 29.5}})));

    public ResonantForgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty.STAGE, 0));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty.STAGE);
    }
    private static double[][] concat(double[][] a, double[][] b) {
        double[][] all = java.util.Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, all, a.length, b.length);
        return all;
    }
    @Override public BlockPos[] getFillerOffsets() { return offsets(Direction.NORTH); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    private static Map<BlockPos, VoxelShape> cells(BlockState state) {
        return CELLS.get(com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty.stage(state)).get(state.getValue(FACING));
    }
    private static VoxelShape shape(BlockState state) { return cells(state).getOrDefault(BlockPos.ZERO, Shapes.block()); }
    @Override public VoxelShape fillerShape(BlockState controller, BlockPos offset) { return cells(controller).get(offset); }
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

    /**
     * Filler cells around the controller: the hammer and grinder bays are two blocks tall in both
     * rows, while the basin column stays one block tall so its headroom is open. The controller is
     * the back row; the front row extends toward the worker, away from an upgrade rite's Focus.
     */
    public static BlockPos[] offsets(Direction facing) {
        Direction right = facing.getClockWise();
        Direction front = facing.getOpposite();
        List<BlockPos> cells = new ArrayList<>();
        for (int row = 0; row < 2; row++) {
            BlockPos base = BlockPos.ZERO.relative(front, row);
            if (row > 0) cells.add(base);
            for (int side = -1; side <= 1; side += 2)
                for (int y = 0; y < 2; y++) cells.add(base.relative(right, side).above(y));
        }
        return cells.toArray(BlockPos[]::new);
    }

    /** Fills any free footprint cell missing a linked filler, e.g. on forges placed before the front row existed. */
    public static void restoreFillers(Level level, BlockPos controller, BlockState state) {
        for (BlockPos offset : offsets(state.getValue(FACING))) {
            BlockPos fillerPos = controller.offset(offset);
            BlockState existing = level.getBlockState(fillerPos);
            if (existing.is(BlockInit.filler_block.get())
                    && level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler
                    && controller.equals(filler.getMainBlockPos())) continue;
            if (!existing.canBeReplaced()) continue;
            placeFiller(level, controller, fillerPos);
        }
    }

    private static void placeFiller(Level level, BlockPos controller, BlockPos fillerPos) {
        boolean waterlogged = level.getFluidState(fillerPos).getType() == Fluids.WATER;
        level.setBlockAndUpdate(fillerPos, BlockInit.filler_block.get().defaultBlockState()
                .setValue(BlockStateProperties.WATERLOGGED, waterlogged));
        if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) filler.setMainBlockPos(controller);
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
        for (BlockPos offset : offsets(state.getValue(FACING))) placeFiller(level, pos, pos.offset(offset));
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
        if (CreativeStationUpgrades.tryApply(held, level, pos, player))
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
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
        station.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                net.minecraft.world.item.component.BlockItemStateProperties.EMPTY
                        .with(com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty.STAGE, state));
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
