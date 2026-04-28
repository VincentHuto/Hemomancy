package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import com.vincenthuto.hemomancy.common.entity.projectile.HemolyticVialEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class HemolyticVialItem extends Item {

    private static final int HEMOLYTIC_VIAL_COOLDOWN_TICKS = 20;

    public HemolyticVialItem(Properties props) {
        super(props);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Concentrated hemolytic solution, sealed in brittle glass.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.literal("Throw to shatter. Purges blood-taint on impact.")
                .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.literal("Harbingers and blood-kin will find little mercy in it.")
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.blockPosition(),
                SoundEvents.BOTTLE_EMPTY, SoundSource.NEUTRAL,
                0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));

        if (!level.isClientSide) {
            HemolyticVialEntity vial = new HemolyticVialEntity(level, player);
            vial.setItem(stack.copyWithCount(1));
            // Match vanilla throwable vial behavior more closely: slower speed + lobbed arc.
            vial.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0f, 0.5f, 1.0f);
            level.addFreshEntity(vial);
            player.getCooldowns().addCooldown(this, HEMOLYTIC_VIAL_COOLDOWN_TICKS);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
