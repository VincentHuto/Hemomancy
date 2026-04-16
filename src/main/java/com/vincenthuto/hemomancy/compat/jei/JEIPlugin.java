package com.vincenthuto.hemomancy.compat.jei;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.DistillationRecipe;
import com.vincenthuto.hemomancy.common.recipe.MemoryWeavingRecipe;

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
	public static final RecipeType<DistillationRecipe> ghastly_distillation_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "ghastly_distillation_recipe", DistillationRecipe.class);
	public static final RecipeType<DistillationRecipe> pallid_distillation_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "pallid_distillation_recipe", DistillationRecipe.class);
	public static final RecipeType<MemoryWeavingRecipe> memory_weaving_type = RecipeType.create(Hemomancy.MOD_ID,
			"memory_weaving", MemoryWeavingRecipe.class);
	public static final RecipeType<BloodStructureRecipe> blood_structure_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "blood_structure", BloodStructureRecipe.class);
	public static final RecipeType<ScarRecipe> scar_station_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"chisel_station", ScarRecipe.class);
	public static final RecipeType<IncubatorRecipe> incubator_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"morphling_incubator", IncubatorRecipe.class);

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new DistillationRecipeCategory(registry.getJeiHelpers().getGuiHelper(), false));
		registry.addRecipeCategories(new DistillationRecipeCategory(registry.getJeiHelpers().getGuiHelper(), true));
		registry.addRecipeCategories(new MemoryWeavingRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodStructureRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new ScarStationRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new IncubatorRecipeCategory(registry.getJeiHelpers().getGuiHelper()));

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(BlockInit.ghastly_alembic.get()), ghastly_distillation_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.pallid_retort.get()), pallid_distillation_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.somatic_loom.get()), memory_weaving_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.hematic_iron_block.get()), blood_structure_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.scar_station.get()), scar_station_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.morphling_incubator.get()), incubator_recipe_type);
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
		registry.addRecipes(ghastly_distillation_recipe_type,
				DistillationRecipe.getAllRecipes(world).stream().filter(r -> !r.isPallid()).toList());
		registry.addRecipes(pallid_distillation_recipe_type,
				DistillationRecipe.getAllRecipes(world).stream().filter(DistillationRecipe::isPallid).toList());
		registry.addRecipes(memory_weaving_type, MemoryWeavingRecipe.getAllRecipes(world));
		registry.addRecipes(blood_structure_recipe_type, BloodStructureRecipe.getAllRecipes(world));
		registry.addRecipes(scar_station_recipe_type, ScarRecipe.getAllRecipes(world));
		registry.addRecipes(incubator_recipe_type, IncubatorRecipe.getAllRecipes(world));

	}

}
