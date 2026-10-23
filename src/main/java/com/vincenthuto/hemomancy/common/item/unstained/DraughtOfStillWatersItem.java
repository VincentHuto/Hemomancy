package com.vincenthuto.hemomancy.common.item.unstained;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Distilled in the Pallid Retort from white humor and Lethean Dew.
 * Clears Blood Loss and applies Silver Ward I for 3 minutes.
 * A field medicine of the Unstained — not a miracle, just chemistry.
 */
public class DraughtOfStillWatersItem extends Item {

    private static final int SILVER_WARD_DURATION = 20 * 60 * 3;
    private static final int SILVER_WARD_AMPLIFIER = 0;

    public DraughtOfStillWatersItem(Properties props) {
        super(props.stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hemomancy.draught_of_still_waters.cold_distilled_in_memory_of_still")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.hemomancy.draught_of_still_waters.clears_blood_loss").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.hemomancy.draught_of_still_waters.grants_silver_ward_i_3_min").withStyle(ChatFormatting.WHITE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.removeEffect(EffectInit.blood_loss);
            player.addEffect(new MobEffectInstance(
                    EffectInit.silver_ward, SILVER_WARD_DURATION, SILVER_WARD_AMPLIFIER,
                    false, true, true));
            level.playSound(null, player.blockPosition(),
                    SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.8f, 1.3f);
            stack.shrink(1);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
