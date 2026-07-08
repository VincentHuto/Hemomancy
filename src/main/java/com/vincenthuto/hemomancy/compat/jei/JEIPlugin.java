package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.*;
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

import javax.annotation.Nonnull;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

	private static final ResourceLocation ID = Hemomancy.rloc("main");
	public static final RecipeType<DistillationRecipe> ghastly_distillation_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "ghastly_distillation_recipe", DistillationRecipe.class);
	public static final RecipeType<DistillationRecipe> pallid_distillation_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "pallid_distillation_recipe", DistillationRecipe.class);
	public static final RecipeType<MemoryWeavingRecipe> memory_weaving_type = RecipeType.create(Hemomancy.MOD_ID,
			"memory_weaving", MemoryWeavingRecipe.class);
	public static final RecipeType<ArmatureUpgradeRecipe> armature_upgrade_type = RecipeType.create(Hemomancy.MOD_ID,
			"armature_upgrade", ArmatureUpgradeRecipe.class);
	public static final RecipeType<BloodStructureRecipe> blood_structure_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "blood_structure", BloodStructureRecipe.class);
	public static final RecipeType<ScarRecipe> scar_station_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"chisel_station", ScarRecipe.class);
	public static final RecipeType<IncubatorRecipe> incubator_recipe_type = RecipeType.create(Hemomancy.MOD_ID,
			"morphling_incubator", IncubatorRecipe.class);
	public static final RecipeType<FungalScarCultivationRecipe> mycelial_crucible_recipe_type =
			MycelialCrucibleRecipeCategory.JEI_TYPE;
	public static final RecipeType<EnzymeFruitingRecipe> enzyme_fruiting_recipe_type =
			EnzymeFruitingRecipeCategory.JEI_TYPE;
	public static final RecipeType<MorphicNectarRecipe> morphic_nectar_recipe_type =
			MorphicNectarRecipeCategory.JEI_TYPE;
	public static final RecipeType<WhiteHumorPurificationRecipe> white_humor_purification_recipe_type =
			WhiteHumorPurificationRecipeCategory.JEI_TYPE;
	public static final RecipeType<LivingWeaponGraftJeiRecipe> living_weapon_graft_type =
			RecipeType.create(Hemomancy.MOD_ID, "living_weapon_graft", LivingWeaponGraftJeiRecipe.class);

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
		registry.addRecipeCategories(new HematicArmatureRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodStructureRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new ScarStationRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new IncubatorRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new MycelialCrucibleRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new EnzymeFruitingRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new MorphicNectarRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new WhiteHumorPurificationRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new LivingWeaponGraftRecipeCategory(registry.getJeiHelpers().getGuiHelper()));

	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(BlockInit.ghastly_alembic.get()), ghastly_distillation_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.pallid_retort.get()), pallid_distillation_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.somatic_loom.get()), memory_weaving_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.hematic_armature.get()), armature_upgrade_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.hematic_iron_block.get()), blood_structure_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.scar_station.get()), scar_station_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.morphling_incubator.get()), incubator_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.mycelial_crucible.get()), mycelial_crucible_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.mycelial_lantern.get()), enzyme_fruiting_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.pale_humor_flask.get()), white_humor_purification_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.iron_brazier.get()), living_weapon_graft_type);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.living_staff.get()), living_weapon_graft_type);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.living_weapon_graft.get()), living_weapon_graft_type);
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		registry.addRecipes(living_weapon_graft_type, LivingWeaponGraftJeiRecipe.all());

		ClientLevel world = Minecraft.getInstance().level;
		if (world == null) {
			Hemomancy.LOGGER.warn("JEI recipe registration skipped: client level is null.");
			return;
		}
		registry.addRecipes(ghastly_distillation_recipe_type,
				DistillationRecipe.getAllRecipes(world).stream().filter(r -> !r.isPallid()).toList());
		registry.addRecipes(pallid_distillation_recipe_type,
				DistillationRecipe.getAllRecipes(world).stream().filter(DistillationRecipe::isPallid).toList());
		registry.addRecipes(memory_weaving_type, MemoryWeavingRecipe.getAllRecipes(world));
		registry.addRecipes(armature_upgrade_type, ArmatureUpgradeRecipe.getAllRecipes(world));
		registry.addRecipes(blood_structure_recipe_type, BloodStructureRecipe.getAllRecipes(world));
		registry.addRecipes(scar_station_recipe_type, ScarRecipe.getAllRecipes(world));
		registry.addRecipes(incubator_recipe_type, IncubatorRecipe.getAllRecipes(world));
		registry.addRecipes(mycelial_crucible_recipe_type, FungalScarCultivationRecipe.getAllRecipes(world));
		registry.addRecipes(enzyme_fruiting_recipe_type, EnzymeFruitingRecipe.getAllRecipes(world));
		registry.addRecipes(morphic_nectar_recipe_type, MorphicNectarRecipe.getAllRecipes(world));
		registry.addRecipes(white_humor_purification_recipe_type, WhiteHumorPurificationRecipe.getAllRecipes(world));

	}

}
