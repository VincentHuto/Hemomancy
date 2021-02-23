package com.huto.hemomancy.item.rune.pattern;

import com.huto.hemomancy.gui.mindrunes.GuiRunePattern;
import com.huto.hemomancy.recipes.RecipeChiselStation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemRunePattern extends Item {
	public String text;

	public ItemRunePattern(Properties prop, String textIn) {
		super(prop);
		this.text = textIn;
		prop.maxStackSize(1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		if (worldIn.isRemote) {
			Minecraft.getInstance().displayGuiScreen(getPatternGui());
			playerIn.playSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 0.40f, 1F);
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	public RecipeChiselStation getRecipe() {
		return null;
	}

	public GuiRunePattern getPatternGui() {
		return null;
	}
}
