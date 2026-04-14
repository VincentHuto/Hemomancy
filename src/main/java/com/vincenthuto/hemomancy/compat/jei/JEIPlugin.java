package com.vincenthuto.hemomancy.compat.jei;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.ChiselRecipe;
import com.vincenthuto.hemomancy.common.recipe.IncubatorRecipe;
import com.vincenthuto.hemomancy.common.recipe.GhastlyAlembicRecipe;
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
	public static final RecipeType<GhastlyAlembicRecipe> ghastly_alembic_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"ghastly_alembic", GhastlyAlembicRecipe.class);
	public static final RecipeType<MemoryWeavingRecipe> memory_weaving_type = RecipeType.create(Hemomancy.MOD_ID,
			"memory_weaving", MemoryWeavingRecipe.class);
	public static final RecipeType<BloodStructureRecipe> blood_structure_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "blood_structure", BloodStructureRecipe.class);
	public static final RecipeType<ChiselRecipe> chisel_station_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"chisel_station", ChiselRecipe.class);
	public static final RecipeType<IncubatorRecipe> incubator_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"morphling_incubator", IncubatorRecipe.class);

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(new GhastlyAlembicRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new MemoryWeavingRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodStructureRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new ChiselStationRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new IncubatorRecipeCategory(registry.getJeiHelpers().getGuiHelper()));

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(BlockInit.ghastly_alembic.get()), ghastly_alembic_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.somatic_loom.get()), memory_weaving_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.hematic_iron_block.get()), blood_structure_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.runic_chisel_station.get()), chisel_station_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.morphling_incubator.get()), incubator_recipe_type);
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		ClientLevel world = Objects.requireNonNull(Minecraft.getInstance().level);
		registry.addRecipes(ghastly_alembic_recipe_type, GhastlyAlembicRecipe.getAllRecipes(world));
		registry.addRecipes(memory_weaving_type, MemoryWeavingRecipe.getAllRecipes(world));
		registry.addRecipes(blood_structure_recipe_type, BloodStructureRecipe.getAllRecipes(world));
		registry.addRecipes(chisel_station_recipe_type, ChiselRecipe.getAllRecipes(world));
		registry.addRecipes(incubator_recipe_type, IncubatorRecipe.getAllRecipes(world));

	}

}