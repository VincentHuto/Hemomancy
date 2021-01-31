package com.huto.hemomancy.recipes;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.item.tool.ItemMorphlingJar;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CopyMorphlingJarDataRecipe extends ShapedRecipe {
	public CopyMorphlingJarDataRecipe(final ResourceLocation id, final String group, final int recipeWidth,
			final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
		super(id, group, recipeWidth, recipeHeight, ingredients, recipeOutput);
	}

	public CopyMorphlingJarDataRecipe(ShapedRecipe shapedRecipe) {
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
				if (!item.isEmpty() && item.getItem() instanceof ItemMorphlingJar) {
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
			implements IRecipeSerializer<CopyMorphlingJarDataRecipe> {
		@Nullable
		@Override
		public CopyMorphlingJarDataRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
			return new CopyMorphlingJarDataRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer));
		}

		@Override
		public CopyMorphlingJarDataRecipe read(ResourceLocation recipeId, JsonObject json) {
			try {
				return new CopyMorphlingJarDataRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json));
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error reading CopyJar Recipe from packet: ", exception);
				throw exception;
			}
		}

		@Override
		public void write(PacketBuffer buffer, CopyMorphlingJarDataRecipe recipe) {
			try {
				IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe);
			} catch (Exception exception) {
				Hemomancy.LOGGER.info("Error writing CopyJar Recipe to packet: ", exception);
				throw exception;
			}
		}
	}

}