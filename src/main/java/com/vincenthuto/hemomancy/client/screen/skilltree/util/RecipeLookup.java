package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.recipe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

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

		// Ghastly Alembic / Pallid Retort recipes
		for (var holder : rm.getAllRecipesFor(RecipeInit.distillation_recipe_type.get())) {
			DistillationRecipe r = holder.value();
			ItemStack result = r.getResultItem(ra);
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.GHASTLY_ALEMBIC));
			}
		}

		// Memory Weaving recipes
		for (var holder : rm.getAllRecipesFor(RecipeInit.memory_weaving_type.get())) {
			MemoryWeavingRecipe r = holder.value();
			ItemStack result = r.getResultItem(ra);
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.MEMORY_WEAVING));
			}
		}

		// Blood Structure recipes
		for (var holder : rm.getAllRecipesFor(RecipeInit.blood_structure_recipe_type.get())) {
			BloodStructureRecipe r = holder.value();
			ItemStack result = r.getResult();
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.BLOOD_STRUCTURE));
			}
		}

		// Chisel recipes
		for (var holder : rm.getAllRecipesFor(RecipeInit.chisel_recipe.get())) {
			ScarRecipe r = holder.value();
			ItemStack result = r.getResultItem();
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.CHISEL));
			}
		}

		// Incubator recipes
		for (var holder : rm.getAllRecipesFor(RecipeInit.incubator_recipe_type.get())) {
			IncubatorRecipe r = holder.value();
			ItemStack result = r.getResultItemStack();
			if (result != null && !result.isEmpty()) {
				CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.INCUBATOR));
			}
		}

		// Vanilla crafting recipes are checked last so mod-specific recipes win.
		for (var holder : rm.getAllRecipesFor(RecipeType.CRAFTING)) {
			CraftingRecipe r = holder.value();
			try {
				ItemStack result = r.getResultItem(ra);
				if (!result.isEmpty()) {
					CACHE.putIfAbsent(result.getItem(), new FoundRecipe(r, RecipeKind.VANILLA_CRAFTING));
				}
			} catch (RuntimeException ignored) {
				continue;
			}
		}
	}
}
