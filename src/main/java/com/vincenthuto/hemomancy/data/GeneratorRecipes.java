package com.vincenthuto.hemomancy.data;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

public class GeneratorRecipes extends RecipeProvider {
	public GeneratorRecipes(DataGenerator generator) {
		super(generator);
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

		SimpleCookingRecipeBuilder.smelting(Ingredient.of(ItemInit.foul_paste.get()),
				BlockInit.befouling_ash_trail.get(), 1f, 200);

		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BlockInit.polished_venous_stone_bricks.get()),
				BlockInit.cracked_polished_venous_stone_bricks.get(), 1f, 200);

		ShapelessRecipeBuilder.shapeless(ItemInit.hematic_iron_scrap.get(), 4)
				.requires(BlockInit.hematic_iron_block.get())
				.unlockedBy("has_hematic_iron_block", has(BlockInit.hematic_iron_block.get())).save(consumer);

		ShapelessRecipeBuilder.shapeless(BlockInit.blood_wood_planks.get(), 4).requires(BlockInit.blood_wood_log.get())
				.unlockedBy("has_blood_wood_log", has(BlockInit.blood_wood_log.get())).save(consumer);

		ShapelessRecipeBuilder.shapeless(BlockInit.befouling_ash_trail.get(), 3).requires(Items.NETHER_WART)
				.requires(Items.GUNPOWDER).requires(Items.BONE_MEAL)
				.unlockedBy("has_nether_wart", has(Items.NETHER_WART)).save(consumer);

		ShapelessRecipeBuilder.shapeless(BlockInit.infested_venous_stone.get(), 1)
				.requires(BlockInit.venous_stone.get()).requires(Items.NETHER_WART).requires(Items.BROWN_MUSHROOM)
				.requires(Items.BONE_MEAL).requires(Items.RED_MUSHROOM)
				.unlockedBy("has_nether_wart", has(Items.NETHER_WART)).save(consumer);

		ShapelessRecipeBuilder.shapeless(BlockInit.smouldering_ash_trail.get(), 3).requires(Items.BLAZE_POWDER)
				.requires(Items.GUNPOWDER).requires(Items.BONE_MEAL)
				.unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER)).save(consumer);

		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern.get()).requires(Items.PAPER)
				.requires(ItemInit.rune_blank.get()).unlockedBy("has_rune_blank", has(ItemInit.rune_blank.get()))
				.save(consumer);

//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_beast_c.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Tags.Items.GEMS_DIAMOND).requires(Items.BONE)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_beast.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.BEEF).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_clawmark.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.FLINT).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_communion.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.GHAST_TEAR).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get()))
//				.save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_corruption_c.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.FERMENTED_SPIDER_EYE).requires(Tags.Items.GEMS_DIAMOND)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_hunter_c.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.GOLDEN_SWORD).requires(Tags.Items.GEMS_DIAMOND)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_heir.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.STRING).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_impurity_c.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.ROTTEN_FLESH).requires(Tags.Items.GEMS_DIAMOND)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_lake.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.WATER_BUCKET).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get()))
//				.save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_metamorphosis_cw.get())
//				.requires(ItemInit.rune_pattern.get()).requires(Items.GOLDEN_APPLE)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_metamorphosis.get())
//				.requires(ItemInit.rune_pattern.get()).requires(Items.GOLDEN_CARROT)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_milkweed_c.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.ENDER_EYE).requires(Tags.Items.GEMS_DIAMOND)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_moon.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.REDSTONE).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get()))
//				.save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_oedon.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.RED_DYE).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get()))
//				.save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_radiance_c.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.GLOWSTONE_DUST).requires(Tags.Items.GEMS_DIAMOND)
//				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);
//
//		ShapelessRecipeBuilder.shapeless(ItemInit.rune_pattern_rapture.get()).requires(ItemInit.rune_pattern.get())
//				.requires(Items.GLOWSTONE_DUST).unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get()))
//				.save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.rune_blank.get()).define('N', Blocks.OBSIDIAN).define('O', Blocks.SNOW)
				.define('P', Items.REDSTONE).pattern("NON").pattern("OPO").pattern("NON")
				.unlockedBy("has_obsidian", has(Blocks.OBSIDIAN)).save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.mind_spike.get()).define('B', Items.IRON_INGOT).define('E', Items.REDSTONE)
				.define('N', Items.STICK).pattern("  N").pattern("EBE").pattern("BBE")
				.unlockedBy("has_redstone", has(Items.REDSTONE)).save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.rune_binder.get()).define('R', ItemInit.rune_pattern.get())
				.define('G', ItemInit.sanguine_formation.get()).pattern("GGG").pattern("GRG").pattern("GGG")
				.unlockedBy("has_rune_pattern", has(ItemInit.rune_pattern.get())).save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.self_reflection_mirror.get()).define('G', Items.GOLD_INGOT)
				.define('A', Items.GOLD_INGOT).define('L', Ingredient.of(ItemTags.LOGS))
				.define('M', BlockInit.rune_mod_station.get()).pattern("AGA").pattern("GMG").pattern("ALA")
				.unlockedBy("has_rune_mod_station", has(BlockInit.rune_mod_station.get())).save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.runic_chisel_station.get())
				.define('C', BlockInit.chiseled_hematic_iron_block.get())
				.define('T', BlockInit.hematic_iron_block.get()).define('V', BlockInit.venous_stone.get())
				.define('P', Ingredient.of(ItemTags.PLANKS)).define('L', Ingredient.of(ItemTags.LOGS)).pattern("PVP")
				.pattern("LCL").pattern("LTL").unlockedBy("has_venous_stone", has(BlockInit.venous_stone.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.chiseled_hematic_iron_block.get(), 4)
				.define('T', BlockInit.hematic_iron_block.get()).pattern("TT").pattern("TT")
				.unlockedBy("has_hematic_iron_block", has(BlockInit.hematic_iron_block.get())).save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.hematic_iron_block.get(), 4)
				.define('T', BlockInit.chiseled_hematic_iron_block.get()).pattern("TT").pattern("TT")
				.unlockedBy("has_chiseled_hematic_iron_block", has(BlockInit.chiseled_hematic_iron_block.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.hematic_iron_helm.get()).define('R', ItemInit.hematic_iron_scrap.get())
				.pattern("RRR").pattern("R R")
				.unlockedBy("has_hematic_iron_scrap", has(ItemInit.hematic_iron_scrap.get())).save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.hematic_iron_chestplate.get())
				.define('R', ItemInit.hematic_iron_scrap.get()).pattern("R R").pattern("RRR").pattern("RRR")
				.unlockedBy("has_hematic_iron_scrap", has(ItemInit.hematic_iron_scrap.get())).save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.hematic_iron_leggings.get()).define('R', ItemInit.hematic_iron_scrap.get())
				.pattern("RRR").pattern("R R").pattern("R R")
				.unlockedBy("has_hematic_iron_scrap", has(ItemInit.hematic_iron_scrap.get())).save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.hematic_iron_boots.get()).define('R', ItemInit.hematic_iron_scrap.get())
				.pattern("R R").pattern("R R")
				.unlockedBy("has_hematic_iron_scrap", has(ItemInit.hematic_iron_scrap.get())).save(consumer);

		ShapedRecipeBuilder.shaped(ItemInit.hematic_iron_sword.get()).define('R', ItemInit.hematic_iron_scrap.get())
				.define('N', Items.STICK).pattern("R").pattern("R").pattern("N")
				.unlockedBy("has_hematic_iron_scrap", has(ItemInit.hematic_iron_scrap.get())).save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.venous_stone_stairs.get(), 4).define('C', BlockInit.venous_stone.get())
				.pattern("C  ").pattern("CC ").pattern("CCC")
				.unlockedBy("has_venous_stone", has(BlockInit.venous_stone.get())).save(consumer);
		ShapedRecipeBuilder.shaped(BlockInit.polished_venous_stone_stairs.get(), 4)
				.define('C', BlockInit.polished_venous_stone.get()).pattern("C  ").pattern("CC ").pattern("CCC")
				.unlockedBy("has_polished_venous_stone", has(BlockInit.polished_venous_stone.get())).save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.polished_venous_stone_brick_stairs.get(), 4)
				.define('C', BlockInit.polished_venous_stone_bricks.get()).pattern("C  ").pattern("CC ").pattern("CCC")
				.unlockedBy("has_polished_venous_stone_bricks", has(BlockInit.polished_venous_stone_bricks.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.venous_stone_slab.get(), 6).define('C', BlockInit.venous_stone.get())
				.pattern("CCC").unlockedBy("has_venous_stone", has(BlockInit.venous_stone.get())).save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.polished_venous_stone_slab.get(), 6)
				.define('C', BlockInit.polished_venous_stone.get()).pattern("CCC")
				.unlockedBy("has_polished_venous_stone", has(BlockInit.polished_venous_stone.get())).save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.polished_venous_stone_brick_slab.get(), 6)
				.define('C', BlockInit.polished_venous_stone_bricks.get()).pattern("CCC")
				.unlockedBy("has_polished_venous_stone_bricks", has(BlockInit.polished_venous_stone_bricks.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.polished_venous_stone.get(), 4).define('C', BlockInit.venous_stone.get())
				.pattern("CC").pattern("CC").unlockedBy("has_venous_stone", has(BlockInit.venous_stone.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.chiseled_polished_venous_stone.get(), 1)
				.define('C', BlockInit.polished_venous_stone_slab.get()).pattern("C").pattern("C")
				.unlockedBy("has_polished_venous_stone_slab", has(BlockInit.polished_venous_stone_slab.get()))
				.save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.polished_venous_stone_bricks.get(), 4)
				.define('C', BlockInit.polished_venous_stone.get()).pattern("CC").pattern("CC")
				.unlockedBy("has_polished_venous_stone", has(BlockInit.polished_venous_stone.get())).save(consumer);

		ShapedRecipeBuilder.shaped(BlockInit.hematic_iron_pillar.get(), 2)
				.define('C', BlockInit.hematic_iron_block.get()).pattern("C").pattern("C")
				.unlockedBy("has_hematic_iron_block", has(BlockInit.hematic_iron_block.get())).save(consumer);

	}
}
