package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HematicSutureNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoText;
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
    private static final BloodAccounting.Source THRALL_SPAWN = BloodAccounting.Source.of("blood_thrall", Category.TOOL, false, false);

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
                            HemoText.translatable("message.hemomancy.blood_thrall_item.source_bound", pos.toShortString()), true);
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
                                Component.translatable("message.hemomancy.blood_thrall_item.choose_different_source_and_destination_blocks"), true);
                    }
                    return InteractionResult.FAIL;
                }
                tag.put(TAG_DEST, NbtUtils.writeBlockPos(pos));
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            HemoText.translatable("message.hemomancy.blood_thrall_item.destination_bound_right_click_ground_to",
                            		pos.toShortString()), true);
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
                            Component.translatable("message.hemomancy.blood_thrall_item.shift_click_a_source_then_a"), true);
                }
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide) {
                // Blood cost
                IBloodVolume playerVol = HemoCapabilityAccess.getBloodVolume(player);
                if (playerVol == null || playerVol.getBloodVolume() < SPAWN_BLOOD_COST) {
                    player.displayClientMessage(
                            HemoText.translatable("message.hemomancy.blood_thrall_item.not_enough_blood_need_ml", (int) SPAWN_BLOOD_COST), true);
                    return InteractionResult.FAIL;
                }
                BloodAccounting.pay(player, SPAWN_BLOOD_COST, THRALL_SPAWN);

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

        tips.add(Component.translatable("tooltip.hemomancy.blood_thrall.a_living_clot_animated_by_blood")
                .withStyle(ChatFormatting.DARK_RED));
        tips.add(Component.translatable("tooltip.hemomancy.blood_thrall.shift_click_bind_source_destination")
                .withStyle(ChatFormatting.GRAY));
        tips.add(Component.translatable("tooltip.hemomancy.blood_thrall.right_click_ground_deploy_thrall")
                .withStyle(ChatFormatting.GRAY));
        tips.add(Component.translatable("tooltip.hemomancy.blood_thrall.shift_click_thrall_pick_up")
                .withStyle(ChatFormatting.GRAY));
        tips.add(HemoText.translatable("tooltip.hemomancy.blood_thrall.cost_ml",
        		(int) SPAWN_BLOOD_COST).withStyle(ChatFormatting.DARK_GRAY));

        if (tag != null) {
            if (tag.contains(TAG_SOURCE)) {
                BlockPos src = NbtUtils.readBlockPos(tag, TAG_SOURCE).orElse(BlockPos.ZERO);
                tips.add(Component.translatable("tooltip.hemomancy.blood_thrall.source_s", src.toShortString())
                        .withStyle(ChatFormatting.RED));
            }
            if (tag.contains(TAG_DEST)) {
                BlockPos dest = NbtUtils.readBlockPos(tag, TAG_DEST).orElse(BlockPos.ZERO);
                tips.add(Component.translatable("tooltip.hemomancy.blood_thrall.destination_s", dest.toShortString())
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
