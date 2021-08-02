package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.gui.guide.GuiGuideTitlePage;
import com.huto.hemomancy.render.item.RenderItemTome;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBloodyBook extends ItemTome {

	public ItemBloodyBook(Properties prop) {
		super(prop.setISTER(() -> RenderItemTome::new));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> onItemRightClick(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (worldIn.isClientSide) {
			Minecraft.getInstance().displayGuiScreen(new GuiGuideTitlePage());
			playerIn.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 0.40f, 1F);

		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent(TextFormatting.GOLD + "A guide to your blood and its power."));
	}

	@Override
	public Rarity getRarity(ItemStack par1ItemStack) {
		return Rarity.UNCOMMON;
	}

}
