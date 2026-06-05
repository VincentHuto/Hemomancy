package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.functional.SanguineVigilBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * The Sanguine Vigil — a Grand-tier Blood Structure that acts as a sanctum
 * ward-post. It stores up to {@link SanguineVigilBlockEntity#MAX_BLOOD} units
 * of blood and, while stocked, continuously suppresses hostile mobs within
 * {@link SanguineVigilBlockEntity#WARD_RADIUS} blocks by applying the
 * {@code blood_binding} effect.
 *
 * <p>Interactions:</p>
 * <ul>
 *   <li><b>Right-click</b> — deposit {@link #DEPOSIT_PER_CLICK} blood from
 *       the player into the Vigil to keep it stocked.</li>
 *   <li><b>Sneak + right-click</b> — display current blood level and ward
 *       status.</li>
 * </ul>
 *
 * <p>The Vigil is a 1×2×1 multi-block (filler one block above the base).</p>
 */
public class SanguineVigilBlock extends BaseEntityBlock implements IMultiBlock {

    public static final MapCodec<SanguineVigilBlock> CODEC = simpleCodec(SanguineVigilBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /** Filler block placed one above the base. */
    private static final BlockPos[] FILLER_OFFSETS = { new BlockPos(0, 1, 0) };

    /** Blood deposited per click. */
    public static final double DEPOSIT_PER_CLICK = 500.0;

    public SanguineVigilBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    public SanguineVigilBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops()
                .strength(3.0f, 8.0f)
                .sound(SoundType.BONE_BLOCK)
                .lightLevel(s -> 6));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ── IMultiBlock ────────────────────────────────────────────────────────────

    @Override
    public BlockPos[] getFillerOffsets() {
        return FILLER_OFFSETS;
    }

    // ── BlockState (facing) ────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
            boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                removeFillers(level, pos);
            }
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

    // ── BlockEntity wiring ─────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SanguineVigilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, BlockEntityInit.sanguine_vigil.get(),
                        SanguineVigilBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ── Interaction ────────────────────────────────────────────────────────────

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
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof SanguineVigilBlockEntity vigil))
            return InteractionResult.SUCCESS;

        var playerBloodOpt = HemoCapabilityAccess.getBloodVolume(player);
        if (playerBloodOpt.isEmpty() || !playerBloodOpt.get().isActive()) {
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.sanguine_vigil.inactive").withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }

        IBloodVolume playerBlood = playerBloodOpt.get();
        var vigilBloodOpt = HemoCapabilityAccess.getBloodVolume(vigil);
        if (vigilBloodOpt.isEmpty()) return InteractionResult.SUCCESS;
        IBloodVolume storage = vigilBloodOpt.get();

        if (player.isShiftKeyDown()) {
            // Status display
            boolean active = storage.getBloodVolume() > 0;
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.sanguine_vigil.status",
                    (int) storage.getBloodVolume(),
                    (int) storage.getMaxBloodVolume(),
                    active
                            ? Component.translatable("block.hemomancy.sanguine_vigil.status.active")
                            : Component.translatable("block.hemomancy.sanguine_vigil.status.dormant"))
                    .withStyle(active ? ChatFormatting.DARK_RED : ChatFormatting.GRAY));
        } else {
            // Deposit blood into vigil
            double toDeposit = Math.min(DEPOSIT_PER_CLICK,
                    Math.min(playerBlood.getBloodVolume(),
                            storage.getMaxBloodVolume() - storage.getBloodVolume()));
            if (toDeposit > 0) {
                playerBlood.drain(toDeposit);
                storage.addBloodVolume(toDeposit);
                vigil.sendUpdates();
                PacketHandler.sendToPlayer(serverPlayer, new BloodVolumeServerPacket(playerBlood));
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.sanguine_vigil.fuelled",
                        (int) toDeposit,
                        (int) storage.getBloodVolume(),
                        (int) storage.getMaxBloodVolume())
                        .withStyle(ChatFormatting.DARK_RED));
            } else {
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.sanguine_vigil.full_or_empty",
                        (int) storage.getBloodVolume(),
                        (int) storage.getMaxBloodVolume())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
