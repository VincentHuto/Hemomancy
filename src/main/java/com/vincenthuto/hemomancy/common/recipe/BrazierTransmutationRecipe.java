package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * What a lit Iron Brazier's offering becomes when it is drawn out under held Absorption with a given manipulation
 * selected (the brazier's transmutation rite). {@code sculpting} names that manipulation; without it any selection (or
 * none) works. A {@code catalyst} must be held in the off hand, {@code catalyst_count} of them (default 1), and they
 * are spent on completion; {@code blood_cost} is paid on completion. {@code byproduct} comes out beside the result.
 *
 * <pre>{@code
 * {
 *   "type": "hemomancy:brazier_transmutation",
 *   "input": { "tag": "minecraft:logs_that_burn" },
 *   "sculpting": "hemomancy:pyretic_forge",
 *   "blood_cost": 25,
 *   "result": { "id": "minecraft:charcoal", "count": 2 }
 * }
 * }</pre>
 */
public final class BrazierTransmutationRecipe implements Recipe<RecipeInput> {
	private final Ingredient input;
	private final Optional<ResourceLocation> sculpting;
	private final Optional<Ingredient> catalyst;
	private final int catalystCount;
	private final double bloodCost;
	private final ItemStack result;
	private final Optional<ItemStack> byproduct;

	public BrazierTransmutationRecipe(Ingredient input, Optional<ResourceLocation> sculpting, Optional<Ingredient> catalyst,
			int catalystCount, double bloodCost, ItemStack result, Optional<ItemStack> byproduct) {
		this.input = Objects.requireNonNull(input);
		this.sculpting = Objects.requireNonNull(sculpting);
		this.catalyst = Objects.requireNonNull(catalyst);
		if (catalystCount < 1) throw new IllegalArgumentException("Brazier transmutation catalyst_count must be at least 1");
		this.catalystCount = catalystCount;
		if (!Double.isFinite(bloodCost) || bloodCost < 0.0D) {
			throw new IllegalArgumentException("Brazier transmutation blood_cost must be finite and not negative");
		}
		if (result.isEmpty()) throw new IllegalArgumentException("Brazier transmutation result cannot be empty");
		this.bloodCost = bloodCost;
		this.result = result.copy();
		this.byproduct = byproduct.map(ItemStack::copy);
	}

	public Ingredient input() {
		return input;
	}

	public Optional<ResourceLocation> sculpting() {
		return sculpting;
	}

	public Optional<Ingredient> catalyst() {
		return catalyst;
	}

	/** How many catalysts the off hand must hold; all of them are spent. */
	public int catalystCount() {
		return catalystCount;
	}

	public double bloodCost() {
		return bloodCost;
	}

	public ItemStack result() {
		return result.copy();
	}

	public Optional<ItemStack> byproduct() {
		return byproduct.map(ItemStack::copy);
	}

	/** Whether {@code offering} is this recipe's input and the selection fits: its sculpting, or any when it names none. */
	public boolean matches(ItemStack offering, ResourceLocation selected) {
		return input.test(offering) && sculpting.map(id -> id.equals(selected)).orElse(true);
	}

	public static List<RecipeHolder<BrazierTransmutationRecipe>> getAllRecipes(Level level) {
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.brazier_transmutation_type.get());
	}

	@Override
	public boolean matches(RecipeInput container, Level level) {
		return container.size() > 0 && input.test(container.getItem(0));
	}

	@Override
	public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
		return result();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return result();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, input);
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.brazier_transmutation_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.brazier_transmutation_type.get();
	}
}
