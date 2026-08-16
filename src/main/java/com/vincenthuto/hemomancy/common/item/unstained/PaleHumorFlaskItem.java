package com.vincenthuto.hemomancy.common.item.unstained;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.WhiteHumorPoolSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.unstained.tool.SilthmereGlaiveItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.UnstainedWarhammerItem;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Bottled white humor drained from the Pallid Retort.
 *
 * Right-click bare-handed: fills the player's white humor reservoir (Unstained
 * path only — requires purification begun).
 *
 * Right-click with an Unstained weapon in the off-hand: coats the weapon with
 * white humor, causing it to apply Hemolysis on hit until the coating is used up.
 */
public class PaleHumorFlaskItem extends Item {

    /** CustomData key placed on Unstained weapons when coated. */
    public static final String TAG_WHITE_HUMOR_COATED = "white_humor_coated";
    public static final String TAG_WHITE_HUMOR_CHARGES = "white_humor_charges";

    private final double amount;

    public PaleHumorFlaskItem(Properties props, double amount) {
        super(props.stacksTo(16));
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(String.format("%.0f mL of purified lymph, cold-distilled.", amount))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Drink: replenishes white humor.").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Right-click with an Unstained weapon in the off-hand to coat it for 24 hits.")
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            ItemStack offhand = player.getOffhandItem();
            if (isUnstainedWeapon(offhand)) {
                // Coat the weapon
                CompoundTag weaponTag = offhand.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                weaponTag.putBoolean(TAG_WHITE_HUMOR_COATED, true);
                weaponTag.putInt(TAG_WHITE_HUMOR_CHARGES, WhiteHumorCoatingRules.CHARGES_PER_FLASK);
                offhand.set(DataComponents.CUSTOM_DATA, CustomData.of(weaponTag));
                stack.shrink(1);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.8f, 1.1f);
                player.displayClientMessage(
                        Component.literal("The white humor seeps into the blade.")
                                .withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC),
                        true);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }

            // Drink — fill white humor reservoir
            var whOpt = HemoCapabilityAccess.getWhiteHumorVolume(player);
            if (whOpt.isPresent()) {
                var vol = whOpt.get();
                // Activate the reservoir the first time if the player has begun purification
                if (!vol.isActive()) {
                    HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
                        if (progress.hasBegunPurification()) vol.setActive(true);
                    });
                }
                if (vol.isActive()) {
                    if (vol.isFull()) {
                        player.displayClientMessage(
                                Component.literal("Your humors are already in balance."), true);
                    } else {
                        vol.fill(amount);
                        level.playSound(null, player.blockPosition(),
                                SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.8f, 1.2f);
                        stack.shrink(1);
                    }
                } else {
                    player.displayClientMessage(
                            Component.literal("You have no use for this yet."), true);
                }
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPlaceContext placeContext = new BlockPlaceContext(context);
        BlockPos placePos = placeContext.getClickedPos();
        BlockState targetState = level.getBlockState(placePos);

        if (!targetState.canBeReplaced(placeContext)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            level.setBlock(placePos, BlockInit.WHITE_HUMOR_BLOCK.get().defaultBlockState(), 3);
            if (level instanceof ServerLevel serverLevel) {
                WhiteHumorPoolSavedData.get(serverLevel).resetSource(placePos);
            }
            level.playSound(null, placePos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.8f, 1.1f);
            if (player == null || !player.isCreative()) {
                stack.shrink(1);
                ItemStack emptyFlask = new ItemStack(HLItemInit.cured_clay_flask.get());
                if (player != null && !player.getInventory().add(emptyFlask)) {
                    player.drop(emptyFlask, false);
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean isUnstainedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof UnstainedWarhammerItem
                || item instanceof SilthmereGlaiveItem;
    }

    public static boolean consumeCoatingHit(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.getBoolean(TAG_WHITE_HUMOR_COATED)) return false;
        int charges = tag.contains(TAG_WHITE_HUMOR_CHARGES)
                ? tag.getInt(TAG_WHITE_HUMOR_CHARGES) : WhiteHumorCoatingRules.CHARGES_PER_FLASK;
        if (!WhiteHumorCoatingRules.isActive(charges)) return false;
        charges = WhiteHumorCoatingRules.afterHit(charges);
        if (charges == 0) {
            tag.remove(TAG_WHITE_HUMOR_COATED);
            tag.remove(TAG_WHITE_HUMOR_CHARGES);
        } else {
            tag.putInt(TAG_WHITE_HUMOR_CHARGES, charges);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return true;
    }
}
