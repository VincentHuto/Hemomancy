package com.vincenthuto.hemomancy.common.recipe;

import java.util.List;
import java.util.stream.Collectors;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hutoslib.math.MultiblockPattern;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class BloodStructureRecipe extends CustomRecipe {

	public static List<BloodStructureRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get())
				.stream().map(holder -> {
					BloodStructureRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).collect(Collectors.toList());
	}

	public static BloodStructureRecipe getStructureByLocation(Level world, ResourceLocation loc) {
		BloodStructureRecipe direct = world.getRecipeManager().getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get()).stream()
				.filter(h -> h.id().equals(loc)).findFirst().map(holder -> {
					BloodStructureRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).orElse(null);
		if (direct != null) {
			return direct;
		}

		String path = loc.getPath();
		ResourceLocation prefixed = path.startsWith("blood_structure/")
				? loc
				: ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), "blood_structure/" + path);
		BloodStructureRecipe withPrefix = world.getRecipeManager().getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get()).stream()
				.filter(h -> h.id().equals(prefixed)).findFirst().map(holder -> {
					BloodStructureRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).orElse(null);
		if (withPrefix != null) {
			return withPrefix;
		}

		if (path.startsWith("blood_structure/")) {
			ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), path.substring("blood_structure/".length()));
			return world.getRecipeManager().getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get()).stream()
					.filter(h -> h.id().equals(stripped)).findFirst().map(holder -> {
						BloodStructureRecipe recipe = holder.value();
						recipe.setId(holder.id());
						return recipe;
					}).orElse(null);
		}
		return null;
	}

	private ResourceLocation id;
	protected double bloodCost;
	protected ItemStack heldItem = null;
	protected Block hitBlock = null;

	protected MultiblockPattern pattern;

	protected ItemStack result = null;

	protected boolean unstained;

	/**
	 * Minimum initiatory degree (Harbinger) or Unstained stage required to perform
	 * this structure craft.
	 */
	protected int requiredDegree;

	public BloodStructureRecipe(ResourceLocation pId, double bloodCost, MultiblockPattern pattern, ItemStack heldItem,
			Block hitBlock, ItemStack result) {
		this(pId, bloodCost, pattern, heldItem, hitBlock, result, false);
	}

	public BloodStructureRecipe(ResourceLocation pId, double bloodCost, MultiblockPattern pattern, ItemStack heldItem,
			Block hitBlock, ItemStack result, boolean unstained) {
		this(pId, bloodCost, pattern, heldItem, hitBlock, result, unstained, 0);
	}

	public BloodStructureRecipe(ResourceLocation pId, double bloodCost, MultiblockPattern pattern, ItemStack heldItem,
			Block hitBlock, ItemStack result, boolean unstained, int requiredDegree) {
		super(CraftingBookCategory.MISC);
		this.id = pId;
		this.bloodCost = bloodCost;
		this.pattern = pattern;
		this.heldItem = heldItem;
		this.hitBlock = hitBlock;
		this.result = result;
		this.unstained = unstained;
		this.requiredDegree = requiredDegree;
	}

	public ResourceLocation getId() { return id; }

	public void setId(ResourceLocation id) {
		this.id = id;
	}

	@Override
	public ItemStack assemble(CraftingInput p_44001_, HolderLookup.Provider p_267165_) {
		return this.getResultItem(p_267165_).copy();
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	public double getBloodCost() {
		return bloodCost;
	}

	public ItemStack getHeldItem() {
		return heldItem;
	}

	public Block getHitBlock() {
		return hitBlock;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ing = super.getIngredients();
		return ing;
	}

	public MultiblockPattern getPattern() {
		return pattern;
	}

	public ItemStack getResult() {
		return result;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider a) {
		return this.result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.blood_structure_recipe_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.blood_structure_recipe_type.get();
	}

	@Override
	public boolean matches(CraftingInput pContainer, Level pLevel) {
		return true;
	}

	public boolean matchRecipe(BloodStructureRecipe checkRecipe) {
		if (heldItem.equals(checkRecipe.heldItem)) {
			return true;
		}
		return false;
	}

	public void setBloodCost(double bloodCost) {
		this.bloodCost = bloodCost;
	}

	public void setHeldItem(ItemStack heldItem) {
		this.heldItem = heldItem;
	}

	public void setHitBlock(Block hitBlock) {
		this.hitBlock = hitBlock;
	}

	public void setPattern(MultiblockPattern pattern) {
		this.pattern = pattern;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	/**
	 * Returns whether this structure recipe belongs to the Unstained path.
	 */
	public boolean isUnstained() {
		return unstained;
	}

	public void setUnstained(boolean unstained) {
		this.unstained = unstained;
	}

	/**
	 * Returns the per-recipe degree / stage requirement.
	 */
	public int getRequiredDegree() {
		return requiredDegree;
	}

	public void setRequiredDegree(int requiredDegree) {
		this.requiredDegree = requiredDegree;
	}

	@Override
	public String toString() {
		return "Blood Structure Recipe:" + result.toString();
	}

}
