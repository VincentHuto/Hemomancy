package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.routing.BloodRoutingHelper;
import com.vincenthuto.hemomancy.common.routing.BloodRoutingMode;
import com.vincenthuto.hemomancy.common.routing.BloodRoutingSavedData;
import com.vincenthuto.hemomancy.common.routing.DirectBloodLinkData;
import com.vincenthuto.hemomancy.common.tile.functional.HematicSutureNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class HematicSutureNeedleItem extends Item {

    public HematicSutureNeedleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        return bindAt(context.getLevel(), context.getClickedPos(), player);
    }

    public InteractionResult bindAt(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(serverPlayer);
        if (degree < 3) {
            message(serverPlayer, "item.hemomancy.hematic_suture_needle.need_degree", ChatFormatting.GRAY, 3);
            return InteractionResult.FAIL;
        }

        BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
        if (!isRoutableTarget(blockEntity)) {
            message(serverPlayer, "item.hemomancy.hematic_suture_needle.invalid_target", ChatFormatting.GRAY);
            return InteractionResult.FAIL;
        }

        BloodRoutingSavedData savedData = BloodRoutingSavedData.get(serverLevel);
        DirectBloodLinkData existing = savedData.getLink(pos);
        if (player.isShiftKeyDown() && existing != null && existing.owner().equals(player.getUUID())) {
            DirectBloodLinkData next = cycleLink(serverLevel, serverPlayer, pos, existing, degree);
            savedData.setLink(pos, next);
            play(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME);
            return InteractionResult.SUCCESS;
        }

        DirectBloodLinkData link = existing != null && existing.owner().equals(player.getUUID())
                ? existing
                : DirectBloodLinkData.nearby(player.getUUID());
        savedData.setLink(pos, link);
        message(serverPlayer, "item.hemomancy.hematic_suture_needle.bound", ChatFormatting.DARK_RED,
                describeMode(link));
        play(level, pos, SoundEvents.BONE_BLOCK_PLACE);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
                if (volume != null && volume.isActive()) {
                    boolean enabled = !volume.isBloodRoutingOptInEnabled();
                    volume.setBloodRoutingOptInEnabled(enabled);
                    HemoCapabilityAccess.getBloodVolume(player).ifPresent(v ->
                            com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents.syncVolume(serverPlayer, v));
                    message(serverPlayer,
                            enabled ? "item.hemomancy.hematic_suture_needle.opt_in_on"
                                    : "item.hemomancy.hematic_suture_needle.opt_in_off",
                            enabled ? ChatFormatting.DARK_RED : ChatFormatting.GRAY);
                } else {
                    message(serverPlayer, "item.hemomancy.hematic_suture_needle.inactive", ChatFormatting.GRAY);
                }
            } else {
                message(serverPlayer, "item.hemomancy.hematic_suture_needle.status", ChatFormatting.GRAY);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private DirectBloodLinkData cycleLink(ServerLevel level, ServerPlayer player, BlockPos pos,
            DirectBloodLinkData existing, int degree) {
        if (existing.mode() == BloodRoutingMode.NEARBY) {
            if (degree < 5 || !BloodRoutingHelper.canUseSanctumMode(level, player, pos)) {
                message(player, "item.hemomancy.hematic_suture_needle.sanctum_unavailable", ChatFormatting.GRAY);
                return existing;
            }
            DirectBloodLinkData next = existing.withMode(BloodRoutingMode.SANCTUM).withBloodlineEnabled(false);
            message(player, "item.hemomancy.hematic_suture_needle.mode", ChatFormatting.DARK_RED, describeMode(next));
            return next;
        }

        if (!existing.bloodlineEnabled() && hasBloodlinePermission(player)) {
            DirectBloodLinkData next = existing.withBloodlineEnabled(true);
            message(player, "item.hemomancy.hematic_suture_needle.mode", ChatFormatting.DARK_RED, describeMode(next));
            return next;
        }

        DirectBloodLinkData next = DirectBloodLinkData.nearby(player.getUUID());
        message(player, "item.hemomancy.hematic_suture_needle.mode", ChatFormatting.DARK_RED, describeMode(next));
        return next;
    }

    private boolean hasBloodlinePermission(ServerPlayer player) {
        IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
        if (volume == null) {
            return false;
        }
        Bloodline line = volume.getBloodLine();
        if (line == null || !line.isValid()) {
            return false;
        }
        BloodlineSavedData savedData = BloodlineSavedData.get(player.server.overworld());
        Bloodline globalLine = savedData.getBloodline(line.getBloodlineUUID());
        if (globalLine == null) {
            return false;
        }
        return player.getUUID().equals(globalLine.getLeaderUUID()) || volume.isBloodRoutingOptInEnabled();
    }

    private boolean isRoutableTarget(BlockEntity blockEntity) {
        return blockEntity instanceof HematicSutureNodeBlockEntity
                || blockEntity != null && HemoCapabilityAccess.getBloodVolume(blockEntity).isPresent();
    }

    private Component describeMode(DirectBloodLinkData link) {
        String key = switch (link.mode()) {
            case NEARBY -> "item.hemomancy.hematic_suture_needle.mode.nearby";
            case SANCTUM -> link.bloodlineEnabled()
                    ? "item.hemomancy.hematic_suture_needle.mode.sanctum_bloodline"
                    : "item.hemomancy.hematic_suture_needle.mode.sanctum";
        };
        return Component.translatable(key);
    }

    private void message(ServerPlayer player, String key, ChatFormatting style, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(style), true);
    }

    private void play(Level level, BlockPos pos, net.minecraft.sounds.SoundEvent sound) {
        level.playSound(null, pos, sound, SoundSource.PLAYERS, 0.6f, 1.1f);
    }
}
