package com.huto.hemomancy.item.rune.pattern;

import com.huto.hemomancy.gui.mindrunes.GuiRunePattern;
import com.huto.hemomancy.recipe.RecipeChiselStation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

import net.minecraft.world.item.Item.Properties;

public class ItemRunePattern extends Item {

	String text;
	RegistryObject<RecipeChiselStation> recipe;
	RegistryObject<Item> rune;

	public ItemRunePattern(Properties prop, RegistryObject<Item> rune, RegistryObject<RecipeChiselStation> recipe,
			String text) {
		super(prop.maxStackSize(1));
		this.rune = rune;
		this.recipe = recipe;
		this.text = text;
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
		return recipe.get();
	}

	public GuiRunePattern getPatternGui() {
		return new GuiRunePattern(rune, recipe, text);
	}
}
