package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.stream.Collectors;

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
	private final Ingredient bloodInput;
	private final boolean consumeCatalyst;
	private final boolean pallid;
	private final ItemStack result;
	private final float experience;
	private final int cookingTime;
	/**
	 * White humor drained from the Pallid Retort's tank when this recipe completes.
	 * 0 means the recipe generates white humor instead of consuming it.
	 */
	private final int whiteHumorCost;

	public DistillationRecipe(ResourceLocation id, String group, Ingredient ingredient,
			Ingredient catalyst, boolean pallid, ItemStack result, float experience, int cookingTime) {
		this(id, group, ingredient, catalyst, Ingredient.EMPTY, false, pallid,
				result, experience, cookingTime, 0);
	}

	public DistillationRecipe(ResourceLocation id, String group, Ingredient ingredient,
			Ingredient catalyst, boolean pallid, ItemStack result, float experience, int cookingTime,
			int whiteHumorCost) {
		this(id, group, ingredient, catalyst, Ingredient.EMPTY, false, pallid,
				result, experience, cookingTime, whiteHumorCost);
	}

	public DistillationRecipe(ResourceLocation id, String group, Ingredient ingredient,
			Ingredient catalyst, Ingredient bloodInput, boolean consumeCatalyst, boolean pallid,
			ItemStack result, float experience, int cookingTime, int whiteHumorCost) {
		this.id = id;
		this.group = group;
		this.ingredient = ingredient;
		this.catalyst = catalyst;
		this.bloodInput = bloodInput;
		this.consumeCatalyst = consumeCatalyst;
		this.pallid = pallid;
		this.result = result;
		this.experience = experience;
		this.cookingTime = cookingTime;
		this.whiteHumorCost = whiteHumorCost;
	}

	// ---- Accessors ----

	public Ingredient getIngredient() { return ingredient; }

	/** The catalyst ingredient. {@link Ingredient#EMPTY} means no catalyst is required. */
	public Ingredient getCatalyst() { return catalyst; }
	public Ingredient getBloodInput() { return bloodInput; }

	/** True when this recipe requires a specific catalyst item. */
	public boolean requiresCatalyst() { return !catalyst.isEmpty(); }
	public boolean requiresBloodInput() { return !bloodInput.isEmpty(); }
	public boolean consumesCatalyst() { return consumeCatalyst; }

	/** True when this recipe is for the Pallid Retort; false means Ghastly Alembic. */
	public boolean isPallid() { return pallid; }

	public float getExperience() { return experience; }
	public int getCookingTime() { return cookingTime; }
	/** White humor drained from the retort tank on completion; 0 = no cost (generates humor instead). */
	public int getWhiteHumorCost() { return whiteHumorCost; }

	/** Slot index of the catalyst in the Ghastly Alembic container. Must match GhastlyAlembicBlockEntity.SLOT_CATALYST. */
	public static final int SLOT_CATALYST_INDEX = 3;
	public static final int SLOT_BLOOD_INPUT_INDEX = 5;

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
			if (!catalyst.test(catalystStack)) return false;
		}
		return !requiresBloodInput() || bloodInput.test(container.getItem(SLOT_BLOOD_INPUT_INDEX));
	}

	/** Convenience match that works directly from ItemStacks without a RecipeInput wrapper. */
	public boolean matchesItems(ItemStack input, ItemStack catalystStack) {
		return matchesItems(input, catalystStack, ItemStack.EMPTY);
	}

	public boolean matchesItems(ItemStack input, ItemStack catalystStack, ItemStack bloodInputStack) {
		if (!ingredient.test(input)) return false;
		if (requiresCatalyst() && !catalyst.test(catalystStack)) return false;
		return !requiresBloodInput() || bloodInput.test(bloodInputStack);
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
		if (requiresBloodInput()) list.add(bloodInput);
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
