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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Distilled in the Pallid Retort from white humor and Ghost Pipe.
 * Clears Blood Loss and grants brief Regeneration — a battlefield restorative
 * for Unstained who have taken blood-corrupted damage.
 */
public class PallidInfusionItem extends Item {

    private static final int REGEN_DURATION = 20 * 10;
    private static final int REGEN_AMPLIFIER = 0;

    public PallidInfusionItem(Properties props) {
        super(props.stacksTo(16));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hemomancy.pallid_infusion.ghost_pipe_and_white_humor_coldly")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.hemomancy.pallid_infusion.clears_blood_loss").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.hemomancy.pallid_infusion.grants_regeneration_i_10s").withStyle(ChatFormatting.WHITE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.removeEffect(EffectInit.blood_loss);
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION, REGEN_DURATION, REGEN_AMPLIFIER,
                    false, true, true));
            level.playSound(null, player.blockPosition(),
                    SoundEvents.HONEY_DRINK, SoundSource.PLAYERS, 0.8f, 1.1f);
            stack.shrink(1);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
