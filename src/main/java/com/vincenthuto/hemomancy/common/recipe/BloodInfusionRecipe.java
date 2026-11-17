package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.event.BloodInfusionRules;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A placed block struck by blood becomes another block. With no {@code sculpting} it is plain Blood Infusion: Projection
 * feeds the block and it collapses into its infused form as an item. With {@code sculpting} (a manipulation id such as
 * {@code hemomancy:pyretic_forge}) only that manipulation's Sculpting of Projection runs it, and plain infusion ignores
 * it; {@code in_place} then converts the block where it stands instead of lifting it out as an item.
 */
public final class BloodInfusionRecipe implements Recipe<RecipeInput> {
	private final Block input;
	private final double bloodCost;
	private final Block result;
	private final Optional<ResourceLocation> sculpting;
	private final boolean inPlace;

	public BloodInfusionRecipe(Block input, double bloodCost, Block result) {
		this(input, bloodCost, result, Optional.empty(), false);
	}

	public BloodInfusionRecipe(Block input, double bloodCost, Block result, Optional<ResourceLocation> sculpting,
			boolean inPlace) {
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
		this.sculpting = Objects.requireNonNull(sculpting);
		this.inPlace = inPlace;
	}

	/** The manipulation whose Sculpting runs this recipe; empty for plain Blood Infusion. */
	public Optional<ResourceLocation> sculpting() {
		return sculpting;
	}

	/** Whether a Sculpting converts the block where it stands rather than lifting it out as an item. */
	public boolean inPlace() {
		return inPlace;
	}

	/** Whether this recipe is run by the Sculpting of {@code manipulation} (a plain recipe is run by none). */
	public boolean isSculptedBy(ResourceLocation manipulation) {
		return sculpting.map(manipulation::equals).orElse(false);
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
