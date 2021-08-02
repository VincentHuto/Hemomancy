package com.huto.hemomancy.recipe;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.rune.ItemRuneBinder;

import net.minecraft.core.NonNullList;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CopyRuneBinderDataRecipe extends ShapedRecipe {
	public CopyRuneBinderDataRecipe(final ResourceLocation id, final String group, final int recipeWidth,
			final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
		super(id, group, recipeWidth, recipeHeight, ingredients, recipeOutput);
	}

	public CopyRuneBinderDataRecipe(ShapedRecipe shapedRecipe) {
		super(shapedRecipe.getId(), shapedRecipe.getGroup(), shapedRecipe.getRecipeWidth(),
				shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getRecipeOutput());
	}

	@Override
	public ItemStack getCraftingResult(CraftingInventory inv) {
		final ItemStack craftingResult = super.getCraftingResult(inv);
		ItemStack dataSource = ItemStack.EMPTY;

		if (!craftingResult.isEmpty()) {
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				final ItemStack item = inv.getStackInSlot(i);
				if (!item.isEmpty() && item.getItem() instanceof ItemRuneBinder) {
					dataSource = item;
					break;
				}
			}

			if (!dataSource.isEmpty() && dataSource.hasTag()) {
				craftingResult.setTag(dataSource.getTag().copy());
			}
		}

		return craftingResult;
	}

	public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>>
			implements IRecipeSerializer<CopyRuneBinderDataRecipe> {
		@Nullable
		@Override
		public CopyRuneBinderDataRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			return new CopyRuneBinderDataRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer));
		}

		@Override
		public CopyRuneBinderDataRecipe read(ResourceLocation recipeId, JsonObject json) {
			try {
				return new CopyRuneBinderDataRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json));
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error reading CopyRuneBinder Recipe from packet: ", exception);
				throw exception;
			}
		}

		@Override
		public void write(PacketBuffer buffer, CopyRuneBinderDataRecipe recipe) {
			try {
				IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe);
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error writing CopyRuneBinder Recipe to packet: ", exception);
				throw exception;
			}
		}
	}

}