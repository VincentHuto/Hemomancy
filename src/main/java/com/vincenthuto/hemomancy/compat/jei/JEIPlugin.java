package com.vincenthuto.hemomancy.compat.jei;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.JuiceinatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecallerRecipe;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

	private static final ResourceLocation ID = Hemomancy.rloc("main");
	public static final RecipeType<JuiceinatorRecipe> juiceinator_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"juiceinator", JuiceinatorRecipe.class);
	public static final RecipeType<RecallerRecipe> recaller_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"recaller", RecallerRecipe.class);
	public static final RecipeType<BloodStructureRecipe> blood_structure_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "blood_structure", BloodStructureRecipe.class);

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new JuiceinatorRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new RecallerRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodStructureRecipeCategory(registry.getJeiHelpers().getGuiHelper()));

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(BlockInit.juiceinator.get()), juiceinator_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.visceral_artificial_recaller.get()), recaller_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.hematic_iron_block.get()), blood_structure_recipe_type);
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
		registry.addRecipes(juiceinator_recipe_type, JuiceinatorRecipe.getAllRecipes(world));
		registry.addRecipes(recaller_recipe_type, RecallerRecipe.getAllRecipes(world));
		registry.addRecipes(blood_structure_recipe_type, BloodStructureRecipe.getAllRecipes(world));

	}

}