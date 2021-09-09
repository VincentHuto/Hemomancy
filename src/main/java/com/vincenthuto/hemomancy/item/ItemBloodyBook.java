package com.vincenthuto.hemomancy.item;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemBloodyBook extends ItemTome {

	public ItemBloodyBook(Properties prop) {
		super(prop);
	}


	@Override
	public InteractionResultHolder<ItemStack> use(Level lvl, Player p_41433_, InteractionHand p_41434_) {
		if (lvl.isClientSide()) {
			Hemomancy.proxy.openGuideGui();
		}
		return super.use(lvl, p_41433_, p_41434_);
	}
	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent(ChatFormatting.GOLD + "A guide to your blood and its power."));
	}

	@Override
	public Rarity getRarity(ItemStack par1ItemStack) {
		return Rarity.UNCOMMON;
	}

}
