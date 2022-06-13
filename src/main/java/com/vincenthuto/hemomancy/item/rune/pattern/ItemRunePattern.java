package com.vincenthuto.hemomancy.item.rune.pattern;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipeSerializer;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class ItemRunePattern extends Item {

	String path;
	RegistryObject<Item> rune;

	public ItemRunePattern(Properties prop, RegistryObject<Item> rune, String recipe) {
		super(prop.stacksTo(1));
		this.rune = rune;
		this.path = recipe;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (worldIn.isClientSide) {
			Hemomancy.proxy.openPatternGui(rune, getRecipe());
			playerIn.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
	}

	public ChiselRecipe getRecipe() {
		return ChiselRecipeSerializer.getRecipe(path);
	}

	public void getPatternGui() {
		Hemomancy.proxy.openPatternGui(rune, getRecipe());
	}
}
