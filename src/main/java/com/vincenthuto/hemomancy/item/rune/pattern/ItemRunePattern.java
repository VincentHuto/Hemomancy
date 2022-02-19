package com.vincenthuto.hemomancy.item.rune.pattern;

import java.util.Optional;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.gui.mindrunes.ScreenRunePattern;
import com.vincenthuto.hemomancy.recipe.serializer.ChiselRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.registries.RegistryObject;

public class ItemRunePattern extends Item {

	String text;
	ChiselRecipe recipe;
	RegistryObject<Item> rune;

	public ItemRunePattern(Properties prop, RegistryObject<Item> rune, ChiselRecipe recipe) {
		super(prop.stacksTo(1));
		this.rune = rune;
		this.recipe = recipe;
		this.text = I18n.get(Hemomancy.MOD_ID + "." + rune.get().getRegistryName().getPath() + ".pattern.text");
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

	public ChiselRecipe getRecipe() {
		return recipe;
	}

	private static final String NBT_RECIPEID = "runescribe_recipe_id";

	/*
	 * @Nullable public ChiselRecipe getRecipe(ItemStack stack, Level world) { if
	 * (world == null || world.getRecipeManager() == null || !stack.hasTag() ||
	 * stack.getItem() != this) { return null; } CompoundTag nbt = stack.getTag();
	 * if (!nbt.contains(NBT_RECIPEID)) { return null; } ResourceLocation rLoc = new
	 * ResourceLocation(nbt.get(NBT_RECIPEID).getAsString()); Optional optRecipe =
	 * world.getRecipeManager().byKey(rLoc); if (!optRecipe.isPresent() ||
	 * !(optRecipe.get() instanceof ChiselRecipe)) { return null; } return
	 * (ChiselRecipe) optRecipe.get(); }
	 */

	public ScreenRunePattern getPatternGui() {
		return new ScreenRunePattern(rune, recipe, text);
	}
}
