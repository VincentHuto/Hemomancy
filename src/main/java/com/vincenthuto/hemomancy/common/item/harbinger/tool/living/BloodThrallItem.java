package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.functional.HematicSutureNodeBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/**
 * Blood Thrall Effigy — a two-step item:
 * <ol>
 *   <li>Shift+Right-click on a source {@link IBloodReservoir} → binds source (glows red)</li>
 *   <li>Shift+Right-click on a destination {@link IBloodReservoir} → binds destination (glows blue)</li>
 *   <li>Right-click on the ground → spawns a {@link BloodThrallEntity} that shuttles
 *       blood between the two positions. Consumes the item + blood cost.</li>
 * </ol>
 */
public class BloodThrallItem extends Item {

    private static final String TAG_SOURCE = "ThrallSource";
    private static final String TAG_DEST = "ThrallDest";
    private static final float SPAWN_BLOOD_COST = 500f;

    public BloodThrallItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        BlockPos pos = ctx.getClickedPos();
        ItemStack stack = ctx.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);

        // ── Shift+click on an IBloodReservoir → bind source/dest ──
        if (player.isShiftKeyDown() && be != null) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

            if (!tag.contains(TAG_SOURCE) && isValidThrallSource(be)) {
                // First bind → source
                tag.put(TAG_SOURCE, NbtUtils.writeBlockPos(pos));
                tag.remove(TAG_DEST); // reset dest if re-binding
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.literal("§4Source bound: §c" + pos.toShortString()), true);
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 0.8f);
                }
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (tag.contains(TAG_SOURCE) && HemoCapabilityAccess.getBloodVolume(be).isPresent()) {
                // Second bind → destination
                BlockPos src = NbtUtils.readBlockPos(tag, TAG_SOURCE).orElse(BlockPos.ZERO);
                if (src.equals(pos)) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(
                                Component.literal("§cSource and destination cannot be the same!"), true);
                    }
                    return InteractionResult.FAIL;
                }
                tag.put(TAG_DEST, NbtUtils.writeBlockPos(pos));
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.literal("§1Destination bound: §9" + pos.toShortString()
                                    + " §7(Right-click ground to deploy)"), true);
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
                }
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // ── Normal click on ground → spawn thrall ──
        if (!player.isShiftKeyDown()) {
            CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (!tag.contains(TAG_SOURCE) || !tag.contains(TAG_DEST)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.literal("§cShift+click a source, then a destination first!"), true);
                }
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide) {
                // Blood cost
                IBloodVolume playerVol = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
                if (playerVol == null || playerVol.getBloodVolume() < SPAWN_BLOOD_COST) {
                    player.displayClientMessage(
                            Component.literal("§cNot enough blood! Need " + (int) SPAWN_BLOOD_COST + " mL."), true);
                    return InteractionResult.FAIL;
                }
                playerVol.drain(SPAWN_BLOOD_COST);

                BlockPos src = NbtUtils.readBlockPos(tag, TAG_SOURCE).orElse(BlockPos.ZERO);
                BlockPos dest = NbtUtils.readBlockPos(tag, TAG_DEST).orElse(BlockPos.ZERO);

                BloodThrallEntity thrall = new BloodThrallEntity(level, player, src, dest);
                thrall.moveTo(pos.getX() + 0.5, pos.above().getY(), pos.getZ() + 0.5, player.getYRot(), 0);
                level.addFreshEntity(thrall);

                level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.8f, 0.6f);

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    // ── Tooltips ──

    @Override
      public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tips, TooltipFlag flag) {
        super.appendHoverText(stack, context, tips, flag);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();

        tips.add(Component.literal("A living clot, animated by blood magic.")
                .withStyle(ChatFormatting.DARK_RED));
        tips.add(Component.literal("Shift+click: bind source → destination")
                .withStyle(ChatFormatting.GRAY));
        tips.add(Component.literal("Right-click ground: deploy thrall")
                .withStyle(ChatFormatting.GRAY));
        tips.add(Component.literal("Shift+click thrall: pick up")
                .withStyle(ChatFormatting.GRAY));
        tips.add(Component.literal("Cost: " + (int) SPAWN_BLOOD_COST + " mL")
                .withStyle(ChatFormatting.DARK_GRAY));

        if (tag != null) {
            if (tag.contains(TAG_SOURCE)) {
                BlockPos src = NbtUtils.readBlockPos(tag, TAG_SOURCE).orElse(BlockPos.ZERO);
                tips.add(Component.literal("Source: " + src.toShortString())
                        .withStyle(ChatFormatting.RED));
            }
            if (tag.contains(TAG_DEST)) {
                BlockPos dest = NbtUtils.readBlockPos(tag, TAG_DEST).orElse(BlockPos.ZERO);
                tips.add(Component.literal("Destination: " + dest.toShortString())
                        .withStyle(ChatFormatting.BLUE));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(TAG_SOURCE) && tag.contains(TAG_DEST);
    }

    private boolean isValidThrallSource(BlockEntity be) {
        return be instanceof HematicSutureNodeBlockEntity || HemoCapabilityAccess.getBloodVolume(be).isPresent();
    }
}
