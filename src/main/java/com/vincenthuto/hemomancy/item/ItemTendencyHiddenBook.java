package com.vincenthuto.hemomancy.item;

import java.util.List;

import com.vincenthuto.hutoslib.common.item.ItemGuideBook;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemTendencyHiddenBook extends ItemGuideBook {

	public ItemTendencyHiddenBook(Properties prop, ResourceLocation loc) {
		super(prop, loc);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (worldIn.isClientSide) {
			// Minecraft.getInstance().setScreen(new GuiTendencyTitlePage(true));
			playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);

		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add( Component.translatable(ChatFormatting.LIGHT_PURPLE + "Hidden"));
		tooltip.add( Component.translatable(ChatFormatting.GOLD + "Contains information on"));
		tooltip.add( Component.translatable(ChatFormatting.GOLD + "all known blood types."));

	}

	@Override
	public Rarity getRarity(ItemStack par1ItemStack) {
		return Rarity.UNCOMMON;
	}

}
