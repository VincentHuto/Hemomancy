package com.vincenthuto.hemomancy.common.item.unstained;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AbsolutionDaggerItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.SilthmereGlaiveItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.UnstainedWarhammerItem;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

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
        tooltip.add(Component.literal(String.format("%.0f ml of purified lymph, cold-distilled.", amount))
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Drink: replenishes white humor.").withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Off-hand Unstained weapon + right-click: coats with hemolytic charge.")
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

    public static boolean isUnstainedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof UnstainedWarhammerItem
                || item instanceof SilthmereGlaiveItem
                || item instanceof AbsolutionDaggerItem;
    }
}
