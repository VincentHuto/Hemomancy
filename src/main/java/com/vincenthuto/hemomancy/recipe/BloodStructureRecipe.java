package com.vincenthuto.hemomancy.recipe;

import java.util.Collection;

import com.vincenthuto.hemomancy.init.RecipeInit;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class BloodStructureRecipe extends CustomRecipe {

	protected float bloodCost;
	protected Ingredient heldItem = null;
	protected Ingredient hitBlock = null;
	protected ItemStack result = null;

	public BloodStructureRecipe(ResourceLocation pId, Ingredient ingredient, ItemStack result) {
		super(pId);
		this.heldItem = ingredient;
		this.result = result;
	}

	public boolean matchRecipe(BloodStructureRecipe checkRecipe) {
		if (heldItem.equals(checkRecipe.heldItem)) {
			return true;
		}
		return false;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ing = super.getIngredients();
		ing.add(heldItem);
		return ing;
	}

	@Override
	public ItemStack getResultItem() {
		return this.result;
	}

	@Override
	public boolean matches(CraftingContainer pContainer, Level pLevel) {
		return true;
	}

	@Override
	public ItemStack assemble(CraftingContainer pContainer) {
		return this.getResultItem().copy();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.blood_structure_recipe_type;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.blood_structure_recipe_serializer.get();
	}

	public static Collection<BloodStructureRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.blood_structure_recipe_type);
	}

	@Override
	public String toString() {
		return "Blood Structure Recipe:" + result.toString();
	}

}
