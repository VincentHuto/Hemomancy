package com.vincenthuto.hemomancy.common.recipe;

import java.util.List;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Ghastly Alembic recipe.
 * <ul>
 *   <li>Slot 0 — input ingredient (required)</li>
 *   <li>Slot 3 — catalyst (optional — {@link Ingredient#EMPTY} means no catalyst needed)</li>
 * </ul>
 */
public class DistillationRecipe implements Recipe<RecipeInput> {

	private final ResourceLocation id;
	private final String group;
	private final Ingredient ingredient;
	/** Optional catalyst — {@link Ingredient#EMPTY} means the recipe works without one. */
	private final Ingredient catalyst;
	private final boolean pallid;
	private final ItemStack result;
	private final float experience;
	private final int cookingTime;

	public DistillationRecipe(ResourceLocation id, String group, Ingredient ingredient,
			Ingredient catalyst, boolean pallid, ItemStack result, float experience, int cookingTime) {
		this.id = id;
		this.group = group;
		this.ingredient = ingredient;
		this.catalyst = catalyst;
		this.pallid = pallid;
		this.result = result;
		this.experience = experience;
		this.cookingTime = cookingTime;
	}

	// ---- Accessors ----

	public Ingredient getIngredient() { return ingredient; }

	/** The catalyst ingredient. {@link Ingredient#EMPTY} means no catalyst is required. */
	public Ingredient getCatalyst() { return catalyst; }

	/** True when this recipe requires a specific catalyst item. */
	public boolean requiresCatalyst() { return !catalyst.isEmpty(); }

	/** True when this recipe is for the Pallid Retort; false means Ghastly Alembic. */
	public boolean isPallid() { return pallid; }

	public float getExperience() { return experience; }
	public int getCookingTime() { return cookingTime; }

	/** Slot index of the catalyst in the Ghastly Alembic container. Must match GhastlyAlembicBlockEntity.SLOT_CATALYST. */
	public static final int SLOT_CATALYST_INDEX = 3;

	// ---- Recipe<RecipeInput> ----

	/**
	 * Matches the container against this recipe.
	 * Slot 0 = ingredient, slot 3 = catalyst (optional).
	 */
	@Override
	public boolean matches(RecipeInput container, Level level) {
		ItemStack inputStack = container.getItem(0); // SLOT_INPUT = 0
		if (!ingredient.test(inputStack)) return false;

		// If a catalyst is required, check slot 3
		if (requiresCatalyst()) {
			ItemStack catalystStack = container.getItem(SLOT_CATALYST_INDEX);
			return catalyst.test(catalystStack);
		}
		return true;
	}

	/** Convenience match that works directly from ItemStacks without a RecipeInput wrapper. */
	public boolean matchesItems(ItemStack input, ItemStack catalystStack) {
		if (!ingredient.test(input)) return false;
		if (requiresCatalyst()) return catalyst.test(catalystStack);
		return true;
	}

	@Override
	public ItemStack assemble(RecipeInput container, HolderLookup.Provider registryAccess) {
		return result.copy();
	}

	@Override
	public boolean canCraftInDimensions(int w, int h) { return true; }

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) { return result; }

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(ingredient);
		if (requiresCatalyst()) list.add(catalyst);
		return list;
	}

	/**
	 * Returns this recipe's ResourceLocation id.
	 * In 1.21.1 the Recipe interface no longer provides {@code getId()}; this is
	 * a plain accessor on the recipe object itself.
	 */
	public ResourceLocation getId() { return id; }

	/** Returns the result ItemStack directly, without needing a registry lookup. */
	public ItemStack getResultItemRaw() { return result; }

	@Override
	public String getGroup() { return group; }

	@Override
	public ItemStack getToastSymbol() {
		return new ItemStack(BlockInit.ghastly_alembic.get());
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.distillation_recipe_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.distillation_recipe_type.get();
	}

	// ---- Helpers ----

	public static List<DistillationRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.distillation_recipe_type.get())
				.stream().map(RecipeHolder::value).collect(Collectors.toList());
	}
}
