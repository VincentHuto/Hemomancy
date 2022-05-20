package com.vincenthuto.hemomancy.recipe;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.init.RecipeInit;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class RecallerRecipe extends CustomRecipe {

	Map<EnumBloodTendency, Float> tendency = new HashMap<EnumBloodTendency, Float>();
	protected ItemStack result = null;
	protected Ingredient ingredient = null;

	public RecallerRecipe(ResourceLocation pId, Ingredient ingredient, Map<EnumBloodTendency, Float> tends,
			ItemStack result) {
		super(pId);
		this.ingredient = ingredient;
		this.tendency = tends;
		this.result = result;
	}

	public RecallerRecipe(ResourceLocation pId, Map<EnumBloodTendency, Float> tends, ItemStack result) {
		super(pId);
		this.ingredient = Ingredient.EMPTY;
		this.tendency = tends;
		this.result = result;
	}

	public boolean matchRecipe(RecallerRecipe checkRecipe) {
		if (getTendency().equals(checkRecipe.getTendency())) {
			if (ingredient.equals(checkRecipe.ingredient)) {
				return true;
			}
		}
		return false;
	}

	public Map<EnumBloodTendency, Float> getTendency() {
		return tendency;
	}

	public void setTendency(Map<EnumBloodTendency, Float> tendency) {
		this.tendency = tendency;
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

	@SuppressWarnings("serial")
	public static final HashMap<EnumBloodTendency, Float> blank() {
		return new HashMap<EnumBloodTendency, Float>() {
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
		return RecipeInit.recaller_recipe_type;
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.recaller_recipe_serializer.get();
	}

	public static Collection<RecallerRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager().getAllRecipesFor(RecipeInit.recaller_recipe_type);
	}

	@Override
	public String toString() {
		return "Recaller Recipe:" + result.toString();
	}

}
