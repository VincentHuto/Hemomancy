package com.vincenthuto.hemomancy.recipe;

import java.util.Collection;
import java.util.List;

import com.vincenthuto.hemomancy.init.RecipeInit;
import com.vincenthuto.hutoslib.client.render.block.MultiblockPattern;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BloodStructureRecipe extends CustomRecipe {

	protected float bloodCost;
	protected ItemStack heldItem = null;
	protected Block hitBlock = null;
	protected MultiblockPattern pattern;
	protected ItemStack result = null;

	public BloodStructureRecipe(ResourceLocation pId, float bloodCost, MultiblockPattern pattern, ItemStack heldItem,
			Block hitBlock, ItemStack result) {
		super(pId);
		this.bloodCost = bloodCost;
		this.pattern = pattern;
		this.heldItem = heldItem;
		this.hitBlock = hitBlock;
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
		return RecipeInit.blood_structure_recipe_type.get();
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.blood_structure_recipe_serializer.get();
	}

	public static BloodStructureRecipe getStructureByLocation(Level world, ResourceLocation loc) {
		//Example new ResourceLocation("hemomancy:blood_structure/living_staff_recipe")
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get()).stream()
				.filter(t -> t.getId().equals(loc)).findFirst().orElse(null);
	}

	public static List<BloodStructureRecipe> getAllRecipes(Level world) {

		List<BloodStructureRecipe> collection = world.getRecipeManager()
				.getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get());

		return collection;
	}

	@Override
	public String toString() {
		return "Blood Structure Recipe:" + result.toString();
	}

	public float getBloodCost() {
		return bloodCost;
	}

	public void setBloodCost(float bloodCost) {
		this.bloodCost = bloodCost;
	}

	public ItemStack getHeldItem() {
		return heldItem;
	}

	public void setHeldItem(ItemStack heldItem) {
		this.heldItem = heldItem;
	}

	public Block getHitBlock() {
		return hitBlock;
	}

	public void setHitBlock(Block hitBlock) {
		this.hitBlock = hitBlock;
	}

	public MultiblockPattern getPattern() {
		return pattern;
	}

	public void setPattern(MultiblockPattern pattern) {
		this.pattern = pattern;
	}

	public ItemStack getResult() {
		return result;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

}
