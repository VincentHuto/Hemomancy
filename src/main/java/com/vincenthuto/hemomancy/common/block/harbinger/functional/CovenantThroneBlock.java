package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.utility.CovenantThroneSeatEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.tile.functional.CovenantThroneBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Progenitor-only covenant shrine.  First valid right-click claims the respawn
 * point and seats the Progenitor; right-clicking while seated enters Covenant
 * Trance.  Sneak is left to normal dismount behavior.
 */
public class CovenantThroneBlock extends BaseEntityBlock implements IMultiBlock, SimpleWaterloggedBlock {

    public static final MapCodec<CovenantThroneBlock> CODEC = simpleCodec(CovenantThroneBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 9.0D, 14.0D);
    private static final double SEAT_Y_OFFSET = 0.35D;

    private static final BlockPos[] LOCAL_FILLER_OFFSETS = new BlockPos[] {
            new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0),
            new BlockPos(-1, 1, 0), new BlockPos(1, 1, 0),
            new BlockPos(-1, 2, 0), new BlockPos(0, 2, 0), new BlockPos(1, 2, 0)
    };

    private static final BlockPos[] RENDER_BOUNDS_OFFSETS = LOCAL_FILLER_OFFSETS;

    public static final double TRANCE_BLOOD_COST = 2500.0;
    public static final int TRANCE_DURATION_TICKS = 12_000;
    public static final int TRANCE_COOLDOWN_TICKS = 12_000;

    public CovenantThroneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.SOUTH)
                .setValue(WATERLOGGED, false));
    }

    public CovenantThroneBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops()
                .strength(3.5f, 9.0f)
                .sound(SoundType.BONE_BLOCK)
                .lightLevel(state -> 4));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockPos[] getFillerOffsets() {
        return RENDER_BOUNDS_OFFSETS;
    }

    @Override
    public boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
        BlockState state = level.getBlockState(mainPos);
        Direction facing = state.is(this) ? state.getValue(FACING) : Direction.SOUTH;
        return canPlaceMultiBlock(level, mainPos, facing);
    }

    private boolean canPlaceMultiBlock(Level level, BlockPos mainPos, Direction facing) {
        for (BlockPos localOffset : LOCAL_FILLER_OFFSETS) {
            BlockPos fillerPos = mainPos.offset(rotateOffset(localOffset, facing));
            if (fillerPos.getY() < level.getMinBuildHeight() || fillerPos.getY() >= level.getMaxBuildHeight()) {
                return false;
            }
            if (!level.getBlockState(fillerPos).canBeReplaced()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
        Direction facing = mainState.getValue(FACING);
        for (BlockPos localOffset : LOCAL_FILLER_OFFSETS) {
            BlockPos fillerPos = mainPos.offset(rotateOffset(localOffset, facing));
            boolean waterlogged = level.getFluidState(fillerPos).getType() == Fluids.WATER;
            BlockState fillerState = BlockInit.filler_block.get().defaultBlockState()
                    .setValue(FillerBlock.WATERLOGGED, waterlogged);
            level.setBlock(fillerPos, fillerState, 3);
            if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity fillerEntity) {
                fillerEntity.setMainBlockPos(mainPos);
            }
        }
    }

    @Override
    public void removeFillers(Level level, BlockPos mainPos) {
        BlockState mainState = level.getBlockState(mainPos);
        Direction facing = mainState.is(this) ? mainState.getValue(FACING) : Direction.SOUTH;
        removeFillers(level, mainPos, facing);
    }

    private void removeFillers(Level level, BlockPos mainPos, Direction facing) {
        for (BlockPos localOffset : LOCAL_FILLER_OFFSETS) {
            BlockPos fillerPos = mainPos.offset(rotateOffset(localOffset, facing));
            BlockState state = level.getBlockState(fillerPos);
            if (state.is(BlockInit.filler_block.get())) {
                boolean waterlogged = state.getValue(FillerBlock.WATERLOGGED);
                level.setBlock(fillerPos, waterlogged
                        ? Blocks.WATER.defaultBlockState()
                        : Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static BlockPos rotateOffset(BlockPos offset, Direction facing) {
        return switch (facing) {
            case NORTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
            case EAST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
            case WEST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
            default -> offset;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (canPlaceMultiBlock(context.getLevel(), context.getClickedPos(), facing)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            placeFillers(level, pos, state);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            discardSeatsForThrone(level, pos);
            removeFillers(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CovenantThroneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult result) {
        return handleInteraction(level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        handleInteraction(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    private InteractionResult handleInteraction(Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        if (!isProgenitor(serverPlayer)) {
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.covenant_throne.not_progenitor")
                    .withStyle(ChatFormatting.DARK_RED));
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            return InteractionResult.SUCCESS;
        }

        if (isSeatedOnThisThrone(player, pos)) {
            triggerCovenantTrance(level, pos, serverPlayer);
        } else {
            claimRespawn(level, pos, serverPlayer);
            seatPlayer(level, pos, level.getBlockState(pos), serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isSeatedOnThisThrone(Player player, BlockPos pos) {
        return player.getVehicle() instanceof CovenantThroneSeatEntity seat && seat.isForThrone(pos);
    }

    private static void claimRespawn(Level level, BlockPos pos, ServerPlayer player) {
        player.setRespawnPosition(level.dimension(), pos, 0.0f, false, false);
        player.sendSystemMessage(Component.translatable(
                "block.hemomancy.covenant_throne.respawn_set")
                .withStyle(ChatFormatting.DARK_RED));
    }

    private static void seatPlayer(Level level, BlockPos pos, BlockState state, ServerPlayer player) {
        Vec3 seatPos = seatPosition(pos, state);
        CovenantThroneSeatEntity seat = findSeatEntity(level, pos);
        if (seat == null) {
            seat = new CovenantThroneSeatEntity(level, pos, seatPos.x, seatPos.y, seatPos.z);
            level.addFreshEntity(seat);
        } else {
            seat.setPos(seatPos.x, seatPos.y, seatPos.z);
        }
        if (level.getBlockEntity(pos) instanceof CovenantThroneBlockEntity throne) {
            throne.setSeatedPlayer(player.getUUID());
        }
        player.startRiding(seat, true);
    }

    @Nullable
    private static CovenantThroneSeatEntity findSeatEntity(Level level, BlockPos pos) {
        AABB search = new AABB(pos).inflate(2.0D, 2.0D, 2.0D);
        for (CovenantThroneSeatEntity seat : level.getEntitiesOfClass(CovenantThroneSeatEntity.class, search)) {
            if (seat.isForThrone(pos) && seat.isAlive()) {
                return seat;
            }
        }
        return null;
    }

    private static Vec3 seatPosition(BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
        Vec3 backNudge = Vec3.atLowerCornerOf(facing.getOpposite().getNormal()).scale(0.08D);
        return new Vec3(pos.getX() + 0.5D + backNudge.x,
                pos.getY() + SEAT_Y_OFFSET,
                pos.getZ() + 0.5D + backNudge.z);
    }

    private static void discardSeatsForThrone(Level level, BlockPos pos) {
        AABB search = new AABB(pos).inflate(3.0D, 3.0D, 3.0D);
        for (CovenantThroneSeatEntity seat : level.getEntitiesOfClass(CovenantThroneSeatEntity.class, search)) {
            if (seat.isForThrone(pos)) {
                seat.discard();
            }
        }
    }

    private static void triggerCovenantTrance(Level level, BlockPos pos, ServerPlayer player) {
        if (!(level.getBlockEntity(pos) instanceof CovenantThroneBlockEntity throne)) return;

        long now = level.getGameTime();
        long lastTrance = throne.getLastTranceTime();
        long elapsed = now - lastTrance;

        if (lastTrance > 0 && elapsed < TRANCE_COOLDOWN_TICKS) {
            long remainingSeconds = (TRANCE_COOLDOWN_TICKS - elapsed) / 20;
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.covenant_throne.trance_cooldown", remainingSeconds)
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        var bloodOpt = HemoCapabilityAccess.getBloodVolume(player);
        if (bloodOpt.isEmpty() || !bloodOpt.get().isActive()) {
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.covenant_throne.no_blood")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        IBloodVolume blood = bloodOpt.get();
        if (blood.wouldOverstrain(TRANCE_BLOOD_COST)) {
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.covenant_throne.insufficient_blood",
                    (int) TRANCE_BLOOD_COST)
                    .withStyle(ChatFormatting.DARK_RED));
            return;
        }

        blood.drain(TRANCE_BLOOD_COST);
        throne.setLastTranceTime(now);
        throne.startTranceVisual(now);
        PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(blood));

        player.addEffect(new MobEffectInstance(EffectInit.blood_rush,
                TRANCE_DURATION_TICKS, 1, false, true, true));
        player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon,
                TRANCE_DURATION_TICKS, 0, false, true, true));
        player.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending,
                TRANCE_DURATION_TICKS, 0, false, true, true));

        player.sendSystemMessage(Component.translatable(
                "block.hemomancy.covenant_throne.trance_entered")
                .withStyle(ChatFormatting.DARK_RED));
    }

    public static boolean isProgenitor(Player player) {
        var bloodOpt = HemoCapabilityAccess.getBloodVolume(player);
        if (bloodOpt.isEmpty() || !bloodOpt.get().isActive()) return false;
        Bloodline bloodline = bloodOpt.get().getBloodLine();
        return bloodline.isValid() && player.getUUID().equals(bloodline.getLeaderUUID());
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return WaterloggedBlockSupport.fluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
