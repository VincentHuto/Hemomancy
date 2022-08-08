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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorRecipes extends RecipeProvider {
	public GeneratorRecipes(DataGenerator generator, ExistingFileHelper helper) {
		super(generator);
	}

	@Override
	protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

		SimpleCookingRecipeBuilder
				.smelting(Ingredient.of(ItemInit.swollen_leech.get()), ItemInit.dried_leech.get(), 1f, 200)
				.unlockedBy("has_swollen_leech", has(ItemInit.swollen_leech.get())).save(consumer);

		SimpleCookingRecipeBuilder
				.smelting(Ingredient.of(ItemInit.foul_paste.get()), BlockInit.befouling_ash_trail.get(), 1f, 200)
				.unlockedBy("has_foul_paste", has(ItemInit.foul_paste.get())).save(consumer);

		SimpleCookingRecipeBuilder
				.smelting(Ingredient.of(BlockInit.polished_venous_stone_bricks.get()),
						BlockInit.cracked_polished_venous_stone_bricks.get(), 1f, 200)
				.unlockedBy("has_polished_venous_stone_bricks", has(BlockInit.polished_venous_stone_bricks.get()))
				.save(consumer);

		ShapelessRecipeBuilder.shapeless(ItemInit.hematic_iron_scrap.get(), 4)
				.requires(BlockInit.hematic_iron_block.get())
				.unlockedBy("has_hematic_iron_block", has(BlockInit.hematic_iron_block.get())).save(consumer);

		ShapelessRecipeBuilder.shapeless(BlockInit.blood_wood_planks.get(), 4).requires(BlockInit.blood_wood_log.get())
				.unlockedBy("has_blood_wood_log", has(BlockInit.blood_wood_log.get())).save(consumer);

		ShapelessRecipeBuilder.shapeless(BlockInit.infested_venous_stone.get(), 1)
				.requires(BlockInit.venous_stone.get()).requires(Items.NETHER_WART).requires(Items.BROWN_MUSHROOM)
				.requires(Items.BONE_MEAL).requires(Items.RED_MUSHROOM)
				.unlockedBy("has_nether_wart", has(Items.NETHER_WART)).save(consumer);

		ShapelessRecipeBuilder.shapeless(BlockInit.smouldering_ash_trail.get(), 3).requires(Items.BLAZE_POWDER)
				.requires(Items.GUNPOWDER).requires(Items.BONE_MEAL)
				.unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER)).save(consumer);

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
