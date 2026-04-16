package com.vincenthuto.hemomancy.client.screen.skilltree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Looks up the first recipe that produces a given item across all Hemomancy
 * recipe types and vanilla crafting. Results are cached per-item.
 */
public final class RecipeLookup {

	public enum RecipeKind {
		VANILLA_CRAFTING,
		GHASTLY_ALEMBIC,
		MEMORY_WEAVING,
		BLOOD_STRUCTURE,
		CHISEL,
		INCUBATOR
	}

	public record FoundRecipe(Recipe<?> recipe, RecipeKind kind) {}

	private static final Map<Item, FoundRecipe> CACHE = new HashMap<>();
	private static boolean initialized = false;

	private RecipeLookup() {}

	/** Call when opening a screen to force a fresh scan. */
	public static void clearCache() {
		CACHE.clear();
		initialized = false;
	}

	@Nullable
	public static FoundRecipe find(ItemStack output) {
		if (output == null || output.isEmpty()) return null;

		Item item = output.getItem();
		if (CACHE.containsKey(item)) return CACHE.get(item);

		if (!initialized) {
			buildCache();
			initialized = true;
			return CACHE.get(item);
		}

		return null;
	}

	private static void buildCache() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) return;

		var rm = level.getRecipeManager();
		var ra = level.registryAccess();

		// Ghastly Alembic recipes
		for (DistillationRecipe r : rm.getAllRecipesFor(RecipeInit.distillation_recipe_type.get())) {
			ItemStack result = r.getResultItem(ra);
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.GHASTLY_ALEMBIC));
			}
		}

		// Memory Weaving recipes
		for (MemoryWeavingRecipe r : rm.getAllRecipesFor(RecipeInit.memory_weaving_type.get())) {
			ItemStack result = r.getResultItem(ra);
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.MEMORY_WEAVING));
			}
		}

		// Blood Structure recipes
		for (BloodStructureRecipe r : rm.getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get())) {
			ItemStack result = r.getResult();
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.BLOOD_STRUCTURE));
			}
		}

		// Chisel recipes
		for (ScarRecipe r : rm.getAllRecipesFor(RecipeInit.chisel_recipe.get())) {
			ItemStack result = r.getResultItem();
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.CHISEL));
			}
		}

		// Incubator recipes
		for (IncubatorRecipe r : rm.getAllRecipesFor(RecipeInit.incubator_recipe_type.get())) {
			ItemStack result = r.getResultItemStack();
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.INCUBATOR));
			}
		}

		// Vanilla crafting recipes (last — mod-specific recipes take priority)
		List<CraftingRecipe> craftingRecipes = rm.getAllRecipesFor(RecipeType.CRAFTING);
		for (CraftingRecipe r : craftingRecipes) {
			try {
				ItemStack result = r.getResultItem(ra);
				if (!result.isEmpty()) {
					CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.VANILLA_CRAFTING));
				}
			} catch (Exception ignored) {}
		}
	}
}
