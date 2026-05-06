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
import java.util.stream.Collectors;

/**
 * Recipe for items purified by lingering in a physical pool of white humor.
 */
public class WhiteHumorPurificationRecipe implements Recipe<RecipeInput> {

	private final ResourceLocation id;
	private final String group;
	private final Ingredient input;
	private final ItemStack result;
	private final int transformTime;

	public WhiteHumorPurificationRecipe(ResourceLocation id, String group, Ingredient input,
			ItemStack result, int transformTime) {
		this.id = id;
		this.group = group;
		this.input = input;
		this.result = result;
		this.transformTime = transformTime;
	}

	public ResourceLocation getId() { return id; }
	public Ingredient getInput() { return input; }
	public ItemStack getResultItemRaw() { return result; }
	public int getTransformTime() { return transformTime; }

	public boolean matchesInput(ItemStack stack) {
		return input.test(stack);
	}

	@Override
	public boolean matches(RecipeInput container, Level level) {
		return input.test(container.getItem(0));
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
		list.add(input);
		return list;
	}

	@Override
	public String getGroup() { return group; }

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.white_humor_purification_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.white_humor_purification_recipe_type.get();
	}

	public static List<WhiteHumorPurificationRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager()
				.getAllRecipesFor(RecipeInit.white_humor_purification_recipe_type.get())
				.stream().map(RecipeHolder::value).collect(Collectors.toList());
	}
}
