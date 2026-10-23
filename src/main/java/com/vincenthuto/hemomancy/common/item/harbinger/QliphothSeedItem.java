package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.entity.item.EntityQliphothSeedItem;
import com.vincenthuto.hemomancy.common.init.EntityInit;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class QliphothSeedItem extends Item {

    public QliphothSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(Level world, Entity location, ItemStack stack) {
        EntityQliphothSeedItem entity = new EntityQliphothSeedItem(
                EntityInit.qliphoth_seed_item.get(), world,
                location.getX(), location.getY(), location.getZ(), stack);
        entity.setPickUpDelay(40);
        entity.setDeltaMovement(location.getDeltaMovement());
        return entity;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.hemomancy.qliphoth_seed.tooltip.use")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
    }

}
