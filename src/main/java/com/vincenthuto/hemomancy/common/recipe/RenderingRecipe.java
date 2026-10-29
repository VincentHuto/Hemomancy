package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * What a rendering pool breaks an Apotheos's offering into. Each item in the offered stack rolls each result's
 * {@code chance} on its own, after {@code render_time} ticks submerged in rendering bile.
 *
 * <pre>{@code
 * {
 *   "type": "hemomancy:rendering",
 *   "input": { "item": "minecraft:bone" },
 *   "results": [ { "id": "hemomancy:ruinous_enzyme", "count": 1, "chance": 0.25 } ],
 *   "render_time": 100
 * }
 * }</pre>
 */
public class RenderingRecipe implements Recipe<RecipeInput> {

	/** One output of a rendering: a stack per offered item, landing with {@code chance}. */
	public record Result(ItemStack stack, float chance) {}

	private final ResourceLocation id;
	private final String group;
	private final Ingredient input;
	private final List<Result> results;
	private final int renderTime;

	public RenderingRecipe(ResourceLocation id, String group, Ingredient input, List<Result> results, int renderTime) {
		this.id = id;
		this.group = group;
		this.input = input;
		this.results = List.copyOf(results);
		this.renderTime = renderTime;
	}

	public ResourceLocation getId() { return id; }
	public Ingredient getInput() { return input; }
	public List<Result> getResults() { return results; }
	public int getRenderTime() { return renderTime; }

	public boolean matchesInput(ItemStack stack) {
		return input.test(stack);
	}

	@Override
	public boolean matches(RecipeInput container, Level level) {
		return container.size() > 0 && input.test(container.getItem(0));
	}

	@Override
	public ItemStack assemble(RecipeInput container, HolderLookup.Provider registryAccess) {
		return results.isEmpty() ? ItemStack.EMPTY : results.get(0).stack().copy();
	}

	@Override
	public boolean canCraftInDimensions(int w, int h) { return true; }

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
		return results.isEmpty() ? ItemStack.EMPTY : results.get(0).stack();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> list = NonNullList.create();
		list.add(input);
		return list;
	}

	@Override
	public String getGroup() { return group; }

	@Override
	public boolean isSpecial() { return true; }

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.rendering_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.rendering_recipe_type.get();
	}

	public static List<RenderingRecipe> getAllRecipes(Level level) {
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.rendering_recipe_type.get())
				.stream().map(RecipeHolder::value).toList();
	}
}
