package com.vincenthuto.hemomancy.common.recipe;

import java.util.List;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hutoslib.math.MultiblockPattern;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class CardinalRiteRecipe extends CustomRecipe {

	public static List<CardinalRiteRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get());
	}

	public static CardinalRiteRecipe getRiteByLocation(Level world, ResourceLocation loc) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.cardinal_rite_recipe_type.get()).stream()
				.filter(t -> t.getId().equals(loc)).findFirst().orElse(null);
	}

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
		super(pId, CraftingBookCategory.MISC);
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

	@Override
	public ItemStack assemble(CraftingContainer p_44001_, RegistryAccess p_267165_) {
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
	public boolean matches(CraftingContainer pContainer, Level pLevel) {
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
	 * upon completion. Defaults to {@code true}. Set to {@code false} to
	 * preserve the built structure (e.g. the Qliphoth Bloom ritual keeps the
	 * platform beneath the spawned tree).
	 */
	public boolean shouldBreakBlocksOnCreation() {
		return breakBlocksOnCreation;
	}

	public void setBreakBlocksOnCreation(boolean breakBlocksOnCreation) {
		this.breakBlocksOnCreation = breakBlocksOnCreation;
	}

	/**
	 * Returns whether this rite belongs to the Unstained path rather than
	 * the blood faction. Unstained rites are displayed in the
	 * UnstainedProgressScreen instead of the HarbingerProgressScreen.
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
