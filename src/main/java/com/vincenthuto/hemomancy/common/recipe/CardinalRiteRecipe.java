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

public class CardinalRiteRecipe extends CustomRecipe {

	public static List<CardinalRiteRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get())
				.stream().map(holder -> {
					CardinalRiteRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).collect(Collectors.toList());
	}

	public static CardinalRiteRecipe getRiteByLocation(Level world, ResourceLocation loc) {
		CardinalRiteRecipe direct = world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get()).stream()
				.filter(h -> h.id().equals(loc)).findFirst().map(holder -> {
					CardinalRiteRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).orElse(null);
		if (direct != null) {
			return direct;
		}

		String path = loc.getPath();
		ResourceLocation prefixed = path.startsWith("cardinal_rite/")
				? loc
				: ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), "cardinal_rite/" + path);
		CardinalRiteRecipe withPrefix = world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get()).stream()
				.filter(h -> h.id().equals(prefixed)).findFirst().map(holder -> {
					CardinalRiteRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).orElse(null);
		if (withPrefix != null) {
			return withPrefix;
		}

		if (path.startsWith("cardinal_rite/")) {
			ResourceLocation stripped = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), path.substring("cardinal_rite/".length()));
			return world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get()).stream()
					.filter(h -> h.id().equals(stripped)).findFirst().map(holder -> {
						CardinalRiteRecipe recipe = holder.value();
						recipe.setId(holder.id());
						return recipe;
					}).orElse(null);
		}
		return null;
	}

	private ResourceLocation id;
	protected double bloodCost;
	protected CardinalRiteType riteType;
	protected MultiblockPattern pattern;
	protected ItemStack result;
	protected String riteName;
	protected String riteDescription;
	protected int requiredDegree;
	protected boolean breakBlocksOnCreation;
	protected boolean unstained;

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription) {
		this(pId, bloodCost, riteType, pattern, result, riteName, riteDescription, -1, true, false);
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription,
			int requiredDegree) {
		this(pId, bloodCost, riteType, pattern, result, riteName, riteDescription, requiredDegree, true, false);
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription,
			int requiredDegree, boolean breakBlocksOnCreation) {
		this(pId, bloodCost, riteType, pattern, result, riteName, riteDescription, requiredDegree, breakBlocksOnCreation, false);
	}

	public CardinalRiteRecipe(ResourceLocation pId, double bloodCost, CardinalRiteType riteType,
			MultiblockPattern pattern, ItemStack result, String riteName, String riteDescription,
			int requiredDegree, boolean breakBlocksOnCreation, boolean unstained) {
		super(CraftingBookCategory.MISC);
		this.id = pId;
		this.bloodCost = bloodCost;
		this.riteType = riteType;
		this.pattern = pattern;
		this.result = result;
		this.riteName = riteName;
		this.riteDescription = riteDescription;
		this.requiredDegree = requiredDegree;
		this.breakBlocksOnCreation = breakBlocksOnCreation;
		this.unstained = unstained;
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

	public CardinalRiteType getRiteType() {
		return riteType;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return super.getIngredients();
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

	public String getRiteName() {
		return riteName;
	}

	public String getRiteDescription() {
		return riteDescription;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.cardinal_rite_recipe_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.cardinal_rite_recipe_type.get();
	}

	@Override
	public boolean matches(CraftingInput pContainer, Level pLevel) {
		return true;
	}

	public boolean matchRecipe(CardinalRiteRecipe checkRecipe) {
		return this.riteType == checkRecipe.riteType && this.riteName.equals(checkRecipe.riteName);
	}

	public void setBloodCost(double bloodCost) {
		this.bloodCost = bloodCost;
	}

	public void setRiteType(CardinalRiteType riteType) {
		this.riteType = riteType;
	}

	public void setPattern(MultiblockPattern pattern) {
		this.pattern = pattern;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	public void setRiteName(String riteName) {
		this.riteName = riteName;
	}

	public void setRiteDescription(String riteDescription) {
		this.riteDescription = riteDescription;
	}

	/**
	 * Returns the per-recipe degree requirement, or -1 if the default
	 * rite-type degree should be used.
	 */
	public int getRequiredDegree() {
		return requiredDegree;
	}

	public void setRequiredDegree(int requiredDegree) {
		this.requiredDegree = requiredDegree;
	}

	/**
	 * Returns whether the rite should break the multiblock structure blocks
	 * upon completion.
	 */
	public boolean shouldBreakBlocksOnCreation() {
		return breakBlocksOnCreation;
	}

	public void setBreakBlocksOnCreation(boolean breakBlocksOnCreation) {
		this.breakBlocksOnCreation = breakBlocksOnCreation;
	}

	/**
	 * Returns whether this rite belongs to the Unstained path.
	 */
	public boolean isUnstained() {
		return unstained;
	}

	public void setUnstained(boolean unstained) {
		this.unstained = unstained;
	}

	@Override
	public String toString() {
		return "Cardinal Rite Recipe: " + riteName + " [" + riteType.getSerializedName() + "]";
	}

}
