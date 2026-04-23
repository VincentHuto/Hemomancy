package com.vincenthuto.hemomancy.common.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.RecipeInit;

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

public class MemoryWeavingRecipe extends CustomRecipe {

	/** Blood cost multiplier per required tendency. */
	private static final double BLOOD_COST_PER_TENDENCY = 50.0;
	/** Base crafting time in ticks before tendency scaling. */
	private static final int BASE_CRAFT_TIME_TICKS = 100;
	/** Additional ticks per required tendency. */
	private static final int CRAFT_TIME_PER_TENDENCY = 20;

	private ResourceLocation id;

	@SuppressWarnings("serial")
	public static final HashMap<EnumBloodTendency, Float> blank() {
		return new HashMap<>() {
			{
				put(EnumBloodTendency.ANIMUS, 0f);
				put(EnumBloodTendency.MORTEM, 0f);
				put(EnumBloodTendency.DUCTILIS, 0f);
				put(EnumBloodTendency.FERRIC, 0f);
				put(EnumBloodTendency.LUX, 0f);
				put(EnumBloodTendency.TENEBRIS, 0f);
				put(EnumBloodTendency.FLAMMEUS, 0f);
				put(EnumBloodTendency.CONGEATIO, 0f);
			}
		};
	}
	public static List<MemoryWeavingRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.memory_weaving_type.get())
				.stream().map(RecipeHolder::value).collect(Collectors.toList());
	}
	Map<EnumBloodTendency, Float> tendency = new HashMap<>();

	protected ItemStack result = null;

	protected Ingredient ingredient = null;

	public MemoryWeavingRecipe(ResourceLocation pId, Ingredient ingredient, Map<EnumBloodTendency, Float> tends,
			ItemStack result) {
		super(CraftingBookCategory.MISC);
		this.id = pId;
		this.ingredient = ingredient;
		this.tendency = tends;
		this.result = result;
	}

	public MemoryWeavingRecipe(ResourceLocation pId, Map<EnumBloodTendency, Float> tends, ItemStack result) {
		super(CraftingBookCategory.MISC);
		this.id = pId;
		this.ingredient = Ingredient.EMPTY;
		this.tendency = tends;
		this.result = result;
	}

	public ResourceLocation getId() { return id; }

	@Override
	public ItemStack assemble(CraftingInput p_44001_, HolderLookup.Provider p_267165_) {
		return this.getResultItem(p_267165_).copy();
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ing = super.getIngredients();
		ing.add(ingredient);
		return ing;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider a) {
		return this.result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.memory_weaving_serializer.get();
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.memory_weaving_type.get();
	}

	@Override
	public boolean matches(CraftingInput pContainer, Level pLevel) {
		return true;
	}

	public boolean matchRecipe(MemoryWeavingRecipe checkRecipe) {
		if (getTendency().equals(checkRecipe.getTendency())) {
			if (ingredient.equals(checkRecipe.ingredient)) {
				return true;
			}
		}
		return false;
	}

	public void setTendency(Map<EnumBloodTendency, Float> tendency) {
		this.tendency = tendency;
	}

	/**
	 * Returns whether the given tendency is required by this recipe.
	 * A tendency is required if its map value is > 0.
	 */
	public boolean isTendencyRequired(EnumBloodTendency tend) {
		return tendency.getOrDefault(tend, 0f) > 0f;
	}

	/**
	 * Counts the number of required tendencies for this recipe.
	 * Used to scale ritual duration and blood cost.
	 */
	public int getRequiredTendencyCount() {
		int count = 0;
		for (Float val : tendency.values()) {
			if (val != null && val > 0f) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Blood cost for the ritual, derived from number of required tendencies.
	 */
	public double getBloodCost() {
		return getRequiredTendencyCount() * BLOOD_COST_PER_TENDENCY;
	}

	/**
	 * Crafting time in ticks for the ritual, derived from number of required tendencies.
	 */
	public int getCraftTimeTicks() {
		return BASE_CRAFT_TIME_TICKS + getRequiredTendencyCount() * CRAFT_TIME_PER_TENDENCY;
	}


	@Override
	public String toString() {
		return "Memory Weaving Recipe:" + result.toString();
	}

}
