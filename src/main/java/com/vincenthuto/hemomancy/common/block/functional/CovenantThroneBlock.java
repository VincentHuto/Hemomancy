package com.vincenthuto.hemomancy.common.block.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
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
 * The Covenant Throne — a Grand-tier Blood Structure available only to the
 * <em>Progenitor</em> (the bloodline leader whose UUID matches the stored
 * bloodline's {@code leaderUUID}).
 *
 * <p>Two interactions are supported:</p>
 * <ul>
 *   <li><b>Right-click</b> — Sets the Progenitor's respawn point at this
 *       throne.  Costs no blood but is progenitor-only.  Other players receive
 *       a refusal message.</li>
 *   <li><b>Sneak + right-click</b> — Triggers the <em>Covenant Trance</em>:
 *       drains {@link #TRANCE_BLOOD_COST} blood from the Progenitor and applies
 *       a powerful suite of effects for {@link #TRANCE_DURATION_TICKS} ticks.
 *       Subject to a {@link #TRANCE_COOLDOWN_TICKS}-tick cooldown tracked in
 *       the block entity.</li>
 * </ul>
 *
 * <p>The throne is a 1×2×1 multi-block (the filler occupies the position above
 * the base, mirroring the Sanguine Monolith pattern).</p>
 */
public class CovenantThroneBlock extends BaseEntityBlock implements IMultiBlock {

    public static final MapCodec<CovenantThroneBlock> CODEC = simpleCodec(CovenantThroneBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /** Filler block placed one above the base. */
    private static final BlockPos[] FILLER_OFFSETS = { new BlockPos(0, 1, 0) };

    /** Blood cost to trigger the Covenant Trance. */
    public static final double TRANCE_BLOOD_COST = 2500.0;

    /** Duration of Covenant Trance effects in ticks (600 s = 10 min). */
    public static final int TRANCE_DURATION_TICKS = 12_000;

    /** Cooldown between trances in ticks (10 min). */
    public static final int TRANCE_COOLDOWN_TICKS = 12_000;

    public CovenantThroneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    public CovenantThroneBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops()
                .strength(3.5f, 9.0f)
                .sound(SoundType.BONE_BLOCK)
                .lightLevel(s -> 4));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
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
        return new CovenantThroneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return null; // No tick needed; all logic is interaction-driven
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

        if (!isProgenitor(serverPlayer)) {
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.covenant_throne.not_progenitor")
                    .withStyle(ChatFormatting.DARK_RED));
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            triggerCovenanTrance(level, pos, serverPlayer);
        } else {
            claimRespawn(level, pos, serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    // ── Respawn claim ──────────────────────────────────────────────────────────

    private static void claimRespawn(Level level, BlockPos pos, ServerPlayer player) {
        player.setRespawnPosition(level.dimension(), pos, 0.0f, false, false);
        player.sendSystemMessage(Component.translatable(
                "block.hemomancy.covenant_throne.respawn_set")
                .withStyle(ChatFormatting.DARK_RED));
    }

    // ── Covenant Trance ────────────────────────────────────────────────────────

    private static void triggerCovenanTrance(Level level, BlockPos pos, ServerPlayer player) {
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
        throne.sendUpdates();
        PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(blood));

        // Apply Covenant Trance effects
        player.addEffect(new MobEffectInstance(EffectInit.blood_rush.getHolder().get(),
                TRANCE_DURATION_TICKS, 1, false, true, true));
        player.addEffect(new MobEffectInstance(EffectInit.sanguine_siphon.getHolder().get(),
                TRANCE_DURATION_TICKS, 0, false, true, true));
        player.addEffect(new MobEffectInstance(EffectInit.mycorrhizal_mending.getHolder().get(),
                TRANCE_DURATION_TICKS, 0, false, true, true));

        player.sendSystemMessage(Component.translatable(
                "block.hemomancy.covenant_throne.trance_entered")
                .withStyle(ChatFormatting.DARK_RED));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the given player is the leader (Progenitor) of
     * their own valid bloodline.
     */
    public static boolean isProgenitor(Player player) {
        var bloodOpt = HemoCapabilityAccess.getBloodVolume(player);
        if (bloodOpt.isEmpty() || !bloodOpt.get().isActive()) return false;
        Bloodline bloodline = bloodOpt.get().getBloodLine();
        return bloodline.isValid() && player.getUUID().equals(bloodline.getLeaderUUID());
    }
}
