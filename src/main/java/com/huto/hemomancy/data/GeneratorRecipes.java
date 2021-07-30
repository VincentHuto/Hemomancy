package com.huto.hemomancy.data;

import java.util.function.Consumer;

import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;

import net.minecraft.block.Blocks;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

public class GeneratorRecipes extends RecipeProvider {
	public GeneratorRecipes(DataGenerator generator) {
		super(generator);
	}

	@Override
	protected void registerRecipes(Consumer<FinishedRecipe> consumer) {

		SimpleCookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(ItemInit.raw_clay_flask.get()),
				ItemInit.cured_clay_flask.get(), 1f, 200);

		CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(BlockInit.polished_venous_stone_bricks.get()),
				BlockInit.cracked_polished_venous_stone_bricks.get(), 1f, 200);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.hematic_iron_scrap.get(), 4)
				.addIngredient(BlockInit.hematic_iron_block.get())
				.addCriterion("has_hematic_iron_block", hasItem(BlockInit.hematic_iron_block.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(BlockInit.blood_wood_planks.get(), 4)
				.addIngredient(BlockInit.blood_wood_log.get())
				.addCriterion("has_blood_wood_log", hasItem(BlockInit.blood_wood_log.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(BlockInit.befouling_ash_trail.get(), 3).addIngredient(Items.NETHER_WART)
				.addIngredient(Items.GUNPOWDER).addIngredient(Items.BONE_MEAL)
				.addCriterion("has_nether_wart", hasItem(Items.NETHER_WART)).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(BlockInit.infested_venous_stone.get(), 1)
				.addIngredient(BlockInit.venous_stone.get()).addIngredient(Items.NETHER_WART)
				.addIngredient(Items.BROWN_MUSHROOM).addIngredient(Items.BONE_MEAL).addIngredient(Items.RED_MUSHROOM)
				.addCriterion("has_nether_wart", hasItem(Items.NETHER_WART)).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(BlockInit.smouldering_ash_trail.get(), 3)
				.addIngredient(Items.BLAZE_POWDER).addIngredient(Items.GUNPOWDER).addIngredient(Items.BONE_MEAL)
				.addCriterion("has_blaze_powder", hasItem(Items.BLAZE_POWDER)).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern.get()).addIngredient(Items.PAPER)
				.addIngredient(ItemInit.rune_blank.get())
				.addCriterion("has_rune_blank", hasItem(ItemInit.rune_blank.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_beast_c.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Tags.Items.GEMS_DIAMOND)
				.addIngredient(Items.BONE).addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get()))
				.build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_beast.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.BEEF)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_clawmark.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.FLINT)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_communion.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.GHAST_TEAR)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_corruption_c.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.FERMENTED_SPIDER_EYE)
				.addIngredient(Tags.Items.GEMS_DIAMOND)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_hunter_c.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.GOLDEN_SWORD)
				.addIngredient(Tags.Items.GEMS_DIAMOND)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_heir.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.STRING)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_impurity_c.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.ROTTEN_FLESH)
				.addIngredient(Tags.Items.GEMS_DIAMOND)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_lake.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.WATER_BUCKET)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_metamorphosis_cw.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.GOLDEN_APPLE)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_metamorphosis.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.GOLDEN_CARROT)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_milkweed_c.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.ENDER_EYE)
				.addIngredient(Tags.Items.GEMS_DIAMOND)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_moon.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.REDSTONE)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_oedon.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.RED_DYE)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_radiance_c.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.GLOWSTONE_DUST)
				.addIngredient(Tags.Items.GEMS_DIAMOND)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapelessRecipeBuilder.shapelessRecipe(ItemInit.rune_pattern_rapture.get())
				.addIngredient(ItemInit.rune_pattern.get()).addIngredient(Items.GLOWSTONE_DUST)
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.rune_blank.get()).key('N', Blocks.OBSIDIAN).key('O', Blocks.SNOW)
				.key('P', Items.REDSTONE).patternLine("NON").patternLine("OPO").patternLine("NON")
				.addCriterion("has_obsidian", hasItem(Blocks.OBSIDIAN)).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.mind_spike.get()).key('B', Items.IRON_INGOT).key('E', Items.REDSTONE)
				.key('N', Items.STICK).patternLine("  N").patternLine("EBE").patternLine("BBE")
				.addCriterion("has_redstone", hasItem(Items.REDSTONE)).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.rune_binder.get()).key('R', ItemInit.rune_pattern.get())
				.key('G', ItemInit.sanguine_formation.get()).patternLine("GGG").patternLine("GRG").patternLine("GGG")
				.addCriterion("has_rune_pattern", hasItem(ItemInit.rune_pattern.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.self_reflection_mirror.get()).key('G', Items.GOLD_INGOT)
				.key('A', Items.GOLD_INGOT).key('L', Ingredient.fromTag(ItemTags.LOGS))
				.key('M', BlockInit.rune_mod_station.get()).patternLine("AGA").patternLine("GMG").patternLine("ALA")
				.addCriterion("has_rune_mod_station", hasItem(BlockInit.rune_mod_station.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.runic_chisel_station.get())
				.key('C', BlockInit.chiseled_hematic_iron_block.get()).key('T', BlockInit.hematic_iron_block.get())
				.key('V', BlockInit.venous_stone.get()).key('P', Ingredient.fromTag(ItemTags.PLANKS))
				.key('L', Ingredient.fromTag(ItemTags.LOGS)).patternLine("PVP").patternLine("LCL").patternLine("LTL")
				.addCriterion("has_venous_stone", hasItem(BlockInit.venous_stone.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.chiseled_hematic_iron_block.get(), 4)
				.key('T', BlockInit.hematic_iron_block.get()).patternLine("TT").patternLine("TT")
				.addCriterion("has_hematic_iron_block", hasItem(BlockInit.hematic_iron_block.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.hematic_iron_block.get(), 4)
				.key('T', BlockInit.chiseled_hematic_iron_block.get()).patternLine("TT").patternLine("TT")
				.addCriterion("has_chiseled_hematic_iron_block", hasItem(BlockInit.chiseled_hematic_iron_block.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.hematic_iron_helm.get()).key('R', ItemInit.hematic_iron_scrap.get())
				.patternLine("RRR").patternLine("R R")
				.addCriterion("has_hematic_iron_scrap", hasItem(ItemInit.hematic_iron_scrap.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.hematic_iron_chestplate.get())
				.key('R', ItemInit.hematic_iron_scrap.get()).patternLine("R R").patternLine("RRR").patternLine("RRR")
				.addCriterion("has_hematic_iron_scrap", hasItem(ItemInit.hematic_iron_scrap.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.hematic_iron_leggings.get())
				.key('R', ItemInit.hematic_iron_scrap.get()).patternLine("RRR").patternLine("R R").patternLine("R R")
				.addCriterion("has_hematic_iron_scrap", hasItem(ItemInit.hematic_iron_scrap.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.hematic_iron_boots.get()).key('R', ItemInit.hematic_iron_scrap.get())
				.patternLine("R R").patternLine("R R")
				.addCriterion("has_hematic_iron_scrap", hasItem(ItemInit.hematic_iron_scrap.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.hematic_iron_sword.get()).key('R', ItemInit.hematic_iron_scrap.get())
				.key('N', Items.STICK).patternLine("R").patternLine("R").patternLine("N")
				.addCriterion("has_hematic_iron_scrap", hasItem(ItemInit.hematic_iron_scrap.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(ItemInit.raw_clay_flask.get()).key('C', Items.CLAY_BALL).patternLine(" C ")
				.patternLine("C C").patternLine("CCC").addCriterion("has_clay_ball", hasItem(Items.CLAY_BALL))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.venous_stone_stairs.get(), 4).key('C', BlockInit.venous_stone.get())
				.patternLine("C  ").patternLine("CC ").patternLine("CCC")
				.addCriterion("has_venous_stone", hasItem(BlockInit.venous_stone.get())).build(consumer);
		ShapedRecipeBuilder.shapedRecipe(BlockInit.polished_venous_stone_stairs.get(), 4)
				.key('C', BlockInit.polished_venous_stone.get()).patternLine("C  ").patternLine("CC ")
				.patternLine("CCC")
				.addCriterion("has_polished_venous_stone", hasItem(BlockInit.polished_venous_stone.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.polished_venous_stone_brick_stairs.get(), 4)
				.key('C', BlockInit.polished_venous_stone_bricks.get()).patternLine("C  ").patternLine("CC ")
				.patternLine("CCC")
				.addCriterion("has_polished_venous_stone_bricks", hasItem(BlockInit.polished_venous_stone_bricks.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.venous_stone_slab.get(), 6).key('C', BlockInit.venous_stone.get())
				.patternLine("CCC").addCriterion("has_venous_stone", hasItem(BlockInit.venous_stone.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.polished_venous_stone_slab.get(), 6)
				.key('C', BlockInit.polished_venous_stone.get()).patternLine("CCC")
				.addCriterion("has_polished_venous_stone", hasItem(BlockInit.polished_venous_stone.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.polished_venous_stone_brick_slab.get(), 6)
				.key('C', BlockInit.polished_venous_stone_bricks.get()).patternLine("CCC")
				.addCriterion("has_polished_venous_stone_bricks", hasItem(BlockInit.polished_venous_stone_bricks.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.polished_venous_stone.get(), 4)
				.key('C', BlockInit.venous_stone.get()).patternLine("CC").patternLine("CC")
				.addCriterion("has_venous_stone", hasItem(BlockInit.venous_stone.get())).build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.chiseled_polished_venous_stone.get(), 1)
				.key('C', BlockInit.polished_venous_stone_slab.get()).patternLine("C").patternLine("C")
				.addCriterion("has_polished_venous_stone_slab", hasItem(BlockInit.polished_venous_stone_slab.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.polished_venous_stone_bricks.get(), 4)
				.key('C', BlockInit.polished_venous_stone.get()).patternLine("CC").patternLine("CC")
				.addCriterion("has_polished_venous_stone", hasItem(BlockInit.polished_venous_stone.get()))
				.build(consumer);

		ShapedRecipeBuilder.shapedRecipe(BlockInit.hematic_iron_pillar.get(), 2)
				.key('C', BlockInit.hematic_iron_block.get()).patternLine("C").patternLine("C")
				.addCriterion("has_hematic_iron_block", hasItem(BlockInit.hematic_iron_block.get())).build(consumer);

	}
}
