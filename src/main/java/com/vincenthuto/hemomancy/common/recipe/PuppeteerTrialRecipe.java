package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hutoslib.math.MultiblockPattern;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Collectors;

public class PuppeteerTrialRecipe extends BloodStructureRecipe {
	private final String summonName;
	private final boolean consumePattern;

	public static List<PuppeteerTrialRecipe> getAllTrialRecipes(Level level) {
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.puppeteer_trial_recipe_type.get())
				.stream().map(holder -> {
					PuppeteerTrialRecipe recipe = holder.value();
					recipe.setId(holder.id());
					return recipe;
				}).collect(Collectors.toList());
	}

	public PuppeteerTrialRecipe(ResourceLocation id, double bloodCost, MultiblockPattern pattern,
			ItemStack heldItem, Block hitBlock, String summonName, boolean consumePattern, int fallbackRequiredDegree) {
		super(id, bloodCost, pattern, heldItem, hitBlock, ItemStack.EMPTY, false,
				PuppeteerSummonDefinitions.byName(summonName)
						.map(definition -> definition.requiredDegree())
						.orElse(fallbackRequiredDegree));
		this.summonName = summonName;
		this.consumePattern = consumePattern;
	}

	public String getSummonName() {
		return summonName;
	}

	public boolean shouldConsumePattern() {
		return consumePattern;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.puppeteer_trial_recipe_serializer.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.puppeteer_trial_recipe_type.get();
	}

	@Override
	public String toString() {
		return "Puppeteer Trial Recipe:" + summonName;
	}
}
