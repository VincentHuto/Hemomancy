package com.huto.hemomancy.item.rune.pattern;

import com.huto.hemomancy.gui.mindrunes.GuiRunePattern;
import com.huto.hemomancy.recipe.RecipeChiselStation;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.RegistryObject;

public class ItemRunePattern extends Item {

	String text;
	RegistryObject<RecipeChiselStation> recipe;
	RegistryObject<Item> rune;

	public ItemRunePattern(Properties prop, RegistryObject<Item> rune, RegistryObject<RecipeChiselStation> recipe,
			String text) {
		super(prop.stacksTo(1));
		this.rune = rune;
		this.recipe = recipe;
		this.text = text;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (worldIn.isClientSide) {
			Minecraft.getInstance().setScreen(getPatternGui());
			playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	public RecipeChiselStation getRecipe() {
		return recipe.get();
	}

	public GuiRunePattern getPatternGui() {
		return new GuiRunePattern(rune, recipe, text);
	}
}
