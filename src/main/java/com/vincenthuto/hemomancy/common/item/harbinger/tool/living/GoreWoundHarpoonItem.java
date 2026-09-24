package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.entity.projectile.GoreWoundHarpoonEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public final class GoreWoundHarpoonItem extends ArrowItem {
    public GoreWoundHarpoonItem(Properties properties) {
        super(properties);
    }

    @Override
    public GoreWoundHarpoonEntity createArrow(Level level, ItemStack ammo, LivingEntity shooter,
                                                @Nullable ItemStack weapon) {
        return new GoreWoundHarpoonEntity(level, shooter, weapon);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.hemomancy.gore_wound_harpoon.pull")
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.translatable("tooltip.hemomancy.gore_wound_harpoon.crossbow")
                .withStyle(ChatFormatting.GRAY));
    }
}
