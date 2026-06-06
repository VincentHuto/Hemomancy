package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingSanctumSavedData;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.functional.ConsecratedBloodwellBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The Consecrated Bloodwell — a Grand-tier Blood Structure that stores up to
 * {@link ConsecratedBloodwellBlockEntity#MAX_BLOOD} units of blood as a local
 * reserve. Blood can be deposited freely, but withdrawal requires the player
 * to be standing inside their own Founding Sanctum. This prevents abuse of
 * large portable blood reserves while making the sanctum itself a meaningful
 * home-base resource.
 *
 * <ul>
 *   <li>Right-click — deposit {@link #TRANSFER_PER_CLICK} blood from player
 *       into the well.</li>
 *   <li>Sneak + right-click — withdraw {@link #TRANSFER_PER_CLICK} blood from
 *       the well to the player (sanctum-gated).</li>
 * </ul>
 *
 * A passive server tick also honours the player's {@code autoDrawEnabled}
 * setting: while standing in their own sanctum, blood is slowly siphoned from
 * the well into the player up to their configured threshold.
 */
public class ConsecratedBloodwellBlock extends BaseEntityBlock {

    public static final MapCodec<ConsecratedBloodwellBlock> CODEC = simpleCodec(ConsecratedBloodwellBlock::new);

    /** Blood transferred per manual interaction click. */
    public static final double TRANSFER_PER_CLICK = 1000.0;

    public ConsecratedBloodwellBlock(Properties properties) {
        super(properties);
    }

    public ConsecratedBloodwellBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .requiresCorrectToolForDrops()
                .strength(3.0f, 8.0f)
                .sound(SoundType.METAL));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getLevel() instanceof ServerLevel level
                && context.getPlayer() instanceof ServerPlayer player
                && !FoundingSanctumSavedData.get(level).canPlaceBloodwell(context.getClickedPos())) {
            player.displayClientMessage(Component.translatable(
                    "block.hemomancy.consecrated_bloodwell.duplicate_heart")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
            return null;
        }
        return super.getStateForPlacement(context);
    }

    // ── BlockEntity wiring ─────────────────────────────────────────────────────

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConsecratedBloodwellBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, BlockEntityInit.consecrated_bloodwell.get(),
                        ConsecratedBloodwellBlockEntity::serverTick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            FoundingSanctumSavedData data = FoundingSanctumSavedData.get(serverLevel);
            UUID owner = data.findOwnerContaining(pos);
            if (owner != null) {
                data.removeHeart(owner, pos);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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
        if (!(level.getBlockEntity(pos) instanceof ConsecratedBloodwellBlockEntity well))
            return InteractionResult.SUCCESS;

        var playerBloodOpt = HemoCapabilityAccess.getBloodVolume(player);
        if (playerBloodOpt.isEmpty() || !playerBloodOpt.get().isActive()) {
            player.sendSystemMessage(Component.translatable(
                    "block.hemomancy.consecrated_bloodwell.inactive").withStyle(ChatFormatting.GRAY));
            return InteractionResult.SUCCESS;
        }

        IBloodVolume playerBlood = playerBloodOpt.get();
        var wellBloodOpt = HemoCapabilityAccess.getBloodVolume(well);
        if (wellBloodOpt.isEmpty()) return InteractionResult.SUCCESS;
        IBloodVolume storage = wellBloodOpt.get();

        if (player.isShiftKeyDown()) {
            // Withdraw — sanctum-gated
            if (!isInOwnSanctum(serverPlayer)) {
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.consecrated_bloodwell.not_in_sanctum")
                        .withStyle(ChatFormatting.DARK_RED));
                return InteractionResult.SUCCESS;
            }
            double toWithdraw = Math.min(TRANSFER_PER_CLICK,
                    Math.min(storage.getBloodVolume(),
                            playerBlood.getMaxBloodVolume() - playerBlood.getBloodVolume()));
            if (toWithdraw > 0) {
                storage.subtractBloodVolume(toWithdraw);
                playerBlood.fill(toWithdraw);
                well.sendUpdates();
                syncPlayerBlood(serverPlayer, playerBlood);
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.consecrated_bloodwell.withdrew",
                        (int) toWithdraw,
                        (int) storage.getBloodVolume(),
                        (int) storage.getMaxBloodVolume())
                        .withStyle(ChatFormatting.DARK_RED));
            } else {
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.consecrated_bloodwell.empty_or_full")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            // Deposit — always allowed
            double toDeposit = Math.min(TRANSFER_PER_CLICK,
                    Math.min(playerBlood.getBloodVolume(),
                            storage.getMaxBloodVolume() - storage.getBloodVolume()));
            if (toDeposit > 0) {
                playerBlood.drain(toDeposit);
                storage.addBloodVolume(toDeposit);
                well.sendUpdates();
                syncPlayerBlood(serverPlayer, playerBlood);
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.consecrated_bloodwell.deposited",
                        (int) toDeposit,
                        (int) storage.getBloodVolume(),
                        (int) storage.getMaxBloodVolume())
                        .withStyle(ChatFormatting.DARK_RED));
            } else {
                player.sendSystemMessage(Component.translatable(
                        "block.hemomancy.consecrated_bloodwell.cannot_deposit",
                        (int) storage.getBloodVolume(),
                        (int) storage.getMaxBloodVolume())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the player is standing within their own Founding
     * Sanctum radius. The sanctum must have been consecrated by this exact player.
     */
    public static boolean isInOwnSanctum(ServerPlayer player) {
        FoundingSanctumSavedData data = FoundingSanctumSavedData.get((ServerLevel) player.level());
        UUID owner = HemoCapabilityAccess.getBloodVolume(player)
                .map(IBloodVolume::getBloodLine)
                .filter(Bloodline::isValid)
                .map(Bloodline::getLeaderUUID)
                .orElse(player.getUUID());
        return data.isWithinSanctum(owner, player.blockPosition());
    }

    private static void syncPlayerBlood(ServerPlayer player, IBloodVolume volume) {
        PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
    }
}
