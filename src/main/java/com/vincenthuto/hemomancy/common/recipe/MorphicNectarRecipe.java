package com.vincenthuto.hemomancy.common.recipe;

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
 * Recipe for the Morphic Nectar fluid. When a matching item is thrown into
 * a pool of morphic_nectar and remains submerged for {@link #transformTime}
 * ticks it is replaced by {@link #result}.
 *
 * JSON format:
 * <pre>{@code
 * {
 *   "type": "hemomancy:morphic_nectar",
 *   "input": { "item": "minecraft:stone" },
 *   "result": { "id": "minecraft:obsidian", "count": 1 },
 *   "transform_time": 60
 * }
 * }</pre>
 */
public class MorphicNectarRecipe implements Recipe<RecipeInput> {

	private final ResourceLocation id;
	private final String group;
	private final Ingredient input;
	private final ItemStack result;
	private final int transformTime;

	public MorphicNectarRecipe(ResourceLocation id, String group, Ingredient input,
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

	/** Returns true if the given stack matches this recipe's input ingredient. */
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
		return RecipeInit.morphic_nectar_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.morphic_nectar_recipe_type.get();
	}

	public static List<MorphicNectarRecipe> getAllRecipes(Level world) {
		return world.getRecipeManager()
				.getAllRecipesFor(RecipeInit.morphic_nectar_recipe_type.get())
				.stream().map(RecipeHolder::value).collect(Collectors.toList());
	}
}
