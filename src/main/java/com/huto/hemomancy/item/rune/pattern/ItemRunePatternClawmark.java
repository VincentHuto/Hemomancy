package com.huto.hemomancy.item.rune.pattern;

import com.huto.hemomancy.gui.mindrunes.GuiRunePattern;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.recipes.ModChiselRecipes;
import com.huto.hemomancy.recipes.RecipeChiselStation;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;

public class ItemRunePatternClawmark extends ItemRunePattern  {

	public ItemRunePatternClawmark(Properties prop, String textIn) {
		super(prop, textIn);
	}


	@Override
	public Rarity getRarity(ItemStack par1ItemStack) {
		return Rarity.COMMON;
	}

	@Override
	public RecipeChiselStation getRecipe() {
		return ModChiselRecipes.recipeClawMark;
	}

	@Override
	public GuiRunePattern getPatternGui() {
		return new GuiRunePattern(new ItemStack(ItemInit.rune_clawmark.get()), ModChiselRecipes.recipeClawMark, text);
	}

}