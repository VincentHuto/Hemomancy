package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.event.BloodInfusionRules;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;

public final class BloodInfusionRecipe implements Recipe<RecipeInput> {
	private final Block input;
	private final double bloodCost;
	private final Block result;

	public BloodInfusionRecipe(Block input, double bloodCost, Block result) {
		this.input = Objects.requireNonNull(input);
		this.result = Objects.requireNonNull(result);
		if (input == Blocks.AIR) {
			throw new IllegalArgumentException("Blood infusion input cannot be air");
		}
		if (!BloodInfusionRules.isValidCost(bloodCost)) {
			throw new IllegalArgumentException("Blood infusion cost must be positive and finite");
		}
		if (result == Blocks.AIR || result.asItem() == Items.AIR) {
			throw new IllegalArgumentException("Blood infusion result must have a block item");
		}
		this.bloodCost = bloodCost;
	}

	public Block input() {
		return input;
	}

	public double bloodCost() {
		return bloodCost;
	}

	public Block result() {
		return result;
	}

	public boolean matches(BlockState state, boolean hasBlockEntity) {
		return !hasBlockEntity && state.is(input);
	}

	public ItemStack resultStack() {
		return new ItemStack(result);
	}

	public static List<BloodInfusionRecipe> getAllRecipes(Level level) {
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.blood_infusion_type.get()).stream()
				.map(RecipeHolder::value).toList();
	}

	@Override
	public boolean matches(RecipeInput input, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
		return resultStack();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getResultItem(HolderLookup.Provider registries) {
		return resultStack();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(Ingredient.EMPTY, Ingredient.of(input));
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.blood_infusion_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.blood_infusion_type.get();
	}
}
