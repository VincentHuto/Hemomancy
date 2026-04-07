package com.vincenthuto.hemomancy.common.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.RecipeInit;

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

public class RecallerRecipe extends CustomRecipe {

	/** Blood cost multiplier per unit of tendency. */
	private static final float BLOOD_COST_PER_TENDENCY = 50f;
	/** Base crafting time in ticks before tendency scaling. */
	private static final int BASE_CRAFT_TIME_TICKS = 100;
	/** Additional ticks per unit of tendency. */
	private static final int CRAFT_TIME_PER_TENDENCY = 20;

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
	public static List<RecallerRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.recaller_recipe_type.get());
	}
	Map<EnumBloodTendency, Float> tendency = new HashMap<>();

	protected ItemStack result = null;

	protected Ingredient ingredient = null;

	public RecallerRecipe(ResourceLocation pId, Ingredient ingredient, Map<EnumBloodTendency, Float> tends,
			ItemStack result) {
		super(pId, null);
		this.ingredient = ingredient;
		this.tendency = tends;
		this.result = result;
	}

	public RecallerRecipe(ResourceLocation pId, Map<EnumBloodTendency, Float> tends, ItemStack result) {
		super(pId, CraftingBookCategory.MISC);
		this.ingredient = Ingredient.EMPTY;
		this.tendency = tends;
		this.result = result;
	}

	
	@Override
	public ItemStack assemble(CraftingContainer p_44001_, RegistryAccess p_267165_) {
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
	public ItemStack getResultItem(RegistryAccess a) {
		return this.result;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.recaller_recipe_serializer.get();
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.recaller_recipe_type.get();
	}

	@Override
	public boolean matches(CraftingContainer pContainer, Level pLevel) {
		return true;
	}

	public boolean matchRecipe(RecallerRecipe checkRecipe) {
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
	 * Computes the total absolute tendency required by this recipe.
	 * Used to scale ritual duration and blood cost.
	 */
	public float getTotalTendency() {
		float total = 0;
		for (Float val : tendency.values()) {
			if (val != null) {
				total += Math.abs(val);
			}
		}
		return total;
	}

	/**
	 * Blood cost for the ritual, derived from total tendency strength.
	 */
	public float getBloodCost() {
		return getTotalTendency() * BLOOD_COST_PER_TENDENCY;
	}

	/**
	 * Crafting time in ticks for the ritual, derived from total tendency.
	 */
	public int getCraftTimeTicks() {
		return BASE_CRAFT_TIME_TICKS + (int) (getTotalTendency() * CRAFT_TIME_PER_TENDENCY);
	}


	@Override
	public String toString() {
		return "Recaller Recipe:" + result.toString();
	}

}
