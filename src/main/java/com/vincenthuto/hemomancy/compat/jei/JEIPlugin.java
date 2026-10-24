package com.vincenthuto.hemomancy.compat.jei;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.CraftingMapRecipe;
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
    @Override public void onRuntimeAvailable(mezz.jei.api.runtime.IJeiRuntime runtime) {
        ClinicalRecipeVisibility.runtime(runtime);
    }
    @Override public void onRuntimeUnavailable() { ClinicalRecipeVisibility.runtime(null); }


	private static final ResourceLocation ID = Hemomancy.rloc("main");
	public static final RecipeType<DistillationRecipe> ghastly_distillation_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "ghastly_distillation_recipe", DistillationRecipe.class);
	public static final RecipeType<DistillationRecipe> pallid_distillation_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "pallid_distillation_recipe", DistillationRecipe.class);
	public static final RecipeType<MemoryWeavingRecipe> memory_weaving_type = RecipeType.create(Hemomancy.MOD_ID,
			"memory_weaving", MemoryWeavingRecipe.class);
	public static final RecipeType<ArmatureUpgradeRecipe> armature_upgrade_type = RecipeType.create(Hemomancy.MOD_ID,
			"armature_upgrade", ArmatureUpgradeRecipe.class);
	public static final RecipeType<CraftingMapRecipe> blood_structure_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "blood_structure", CraftingMapRecipe.class);
	public static final RecipeType<CraftingMapRecipe> threshold_rite_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "threshold_rite", CraftingMapRecipe.class);
	public static final RecipeType<CraftingMapRecipe> dominion_rite_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "dominion_rite", CraftingMapRecipe.class);
	public static final RecipeType<CraftingMapRecipe> working_craft_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "working_craft", CraftingMapRecipe.class);
	public static final RecipeType<CraftingMapRecipe> communion_rite_recipe_type = RecipeType
			.create(Hemomancy.MOD_ID, "communion_rite", CraftingMapRecipe.class);
	public static final RecipeType<BloodInfusionRecipe> blood_infusion_recipe_type =
			BloodInfusionRecipeCategory.JEI_TYPE;
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
		registry.addRecipeCategories(new SuccessionRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new DistillationRecipeCategory(registry.getJeiHelpers().getGuiHelper(), false));
		registry.addRecipeCategories(new DistillationRecipeCategory(registry.getJeiHelpers().getGuiHelper(), true));
		registry.addRecipeCategories(new AdvancedBrewingRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new MemoryWeavingRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new HematicArmatureRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		registry.addRecipeCategories(new BloodStructureRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
		for (CardinalRitePurpose purpose : CardinalRitePurpose.values()) {
			registry.addRecipeCategories(new BloodStructureRecipeCategory(registry.getJeiHelpers().getGuiHelper(), purpose));
		}
		registry.addRecipeCategories(new BloodInfusionRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
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
		registry.addRecipeCatalyst(new ItemStack(BlockInit.cardinal_focus.get()), SuccessionRecipeCategory.TYPE);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.living_staff.get()), SuccessionRecipeCategory.TYPE);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.ghastly_alembic.get()), ghastly_distillation_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.ghastly_alembic.get()), AdvancedBrewingRecipeCategory.TYPE);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.pallid_retort.get()), pallid_distillation_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.somatic_loom.get()), memory_weaving_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.hematic_armature.get()), armature_upgrade_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.hematic_iron_block.get()), blood_structure_recipe_type);
		for (CardinalRitePurpose purpose : CardinalRitePurpose.values()) {
			registry.addRecipeCatalyst(new ItemStack(BlockInit.cardinal_focus.get()), riteTypeFor(purpose));
			registry.addRecipeCatalyst(new ItemStack(ItemInit.living_staff.get()), riteTypeFor(purpose));
		}
		registry.addRecipeCatalyst(new ItemStack(ItemInit.blood_projection.get()), blood_infusion_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.scar_station.get()), scar_station_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.morphling_incubator.get()), incubator_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.mycelial_crucible.get()), mycelial_crucible_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.mycelial_lantern.get()), enzyme_fruiting_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.pale_humor_flask.get()), white_humor_purification_recipe_type);
		registry.addRecipeCatalyst(new ItemStack(BlockInit.iron_brazier.get()), living_weapon_graft_type);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.living_staff.get()), living_weapon_graft_type);
		registry.addRecipeCatalyst(new ItemStack(ItemInit.living_weapon_graft.get()), living_weapon_graft_type);
	}

	/** The JEI rite category that lists every Harbinger rite of a purpose. */
	public static RecipeType<CraftingMapRecipe> riteTypeFor(CardinalRitePurpose purpose) {
		return switch (purpose) {
			case SELF -> threshold_rite_recipe_type;
			case WORLD -> dominion_rite_recipe_type;
			case MAKING -> working_craft_recipe_type;
			case BOND -> communion_rite_recipe_type;
		};
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		registry.addRecipes(living_weapon_graft_type, LivingWeaponGraftJeiRecipe.all());
		registry.addItemStackInfo(java.util.List.of(new ItemStack(net.minecraft.world.item.Items.CAULDRON),
				new ItemStack(BlockInit.sanguine_condenser.get()),
				new ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.sanguine_formation.get())),
				net.minecraft.network.chat.Component.translatable("jei.hemomancy.sanguine_condenser.info"));

		ClientLevel world = Minecraft.getInstance().level;
		if (world == null) {
			Hemomancy.LOGGER.warn("JEI recipe registration skipped: client level is null.");
			return;
		}
		registry.addRecipes(ghastly_distillation_recipe_type,
				DistillationRecipe.getAllRecipes(world).stream().filter(r -> !r.isPallid()).toList());
		registry.addRecipes(pallid_distillation_recipe_type,
				DistillationRecipe.getAllRecipes(world).stream().filter(DistillationRecipe::isPallid).toList());
		registry.addRecipes(AdvancedBrewingRecipeCategory.TYPE, AdvancedBrewingDisplay.all(world));
		registry.addRecipes(memory_weaving_type, MemoryWeavingRecipe.getAllRecipes(world));
		registry.addRecipes(armature_upgrade_type, ArmatureUpgradeRecipe.getAllRecipes(world));
		registry.addRecipes(blood_structure_recipe_type, BloodStructureRecipe.getAllRecipes(world).stream()
				.map(CraftingMapRecipe::of).toList());
		for (CardinalRitePurpose purpose : CardinalRitePurpose.values()) {
			// Succession and Restoration keep their own category, laid out around the effigy.
			registry.addRecipes(riteTypeFor(purpose), CardinalRiteRecipe.getAllRecipes(world).stream()
					.filter(recipe -> recipe.isCataloguedUnder(purpose))
					.filter(recipe -> !com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.isRecipe(recipe.getId()))
					.map(CraftingMapRecipe::of).toList());
		}
		registry.addRecipes(SuccessionRecipeCategory.TYPE, CardinalRiteRecipe.getAllRecipes(world).stream()
				.filter(recipe -> com.vincenthuto.hemomancy.common.rite.succession.SuccessionRites.isRecipe(recipe.getId())).toList());
		registry.addRecipes(blood_infusion_recipe_type, BloodInfusionRecipe.getAllRecipes(world));
		registry.addRecipes(scar_station_recipe_type, ScarRecipe.getAllRecipes(world));
		registry.addRecipes(incubator_recipe_type, IncubatorRecipe.getAllRecipes(world));
		registry.addRecipes(mycelial_crucible_recipe_type, FungalScarCultivationRecipe.getAllRecipes(world));
		registry.addRecipes(enzyme_fruiting_recipe_type, EnzymeFruitingRecipe.getAllRecipes(world));
		registry.addRecipes(morphic_nectar_recipe_type, MorphicNectarRecipe.getAllRecipes(world));
		registry.addRecipes(white_humor_purification_recipe_type, WhiteHumorPurificationRecipe.getAllRecipes(world));

	}

}
