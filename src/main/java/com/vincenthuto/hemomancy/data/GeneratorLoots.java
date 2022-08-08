package com.vincenthuto.hemomancy.data;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class GeneratorLoots extends LootTableProvider {
	public GeneratorLoots(DataGenerator dataGeneratorIn, ExistingFileHelper helper) {
		super(dataGeneratorIn);
	}

	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
		return ImmutableList.of(Pair.of(Blocks::new, LootContextParamSets.BLOCK));
	}

	private static class Blocks extends BlockLoot {
		@Override
		protected void addTables() {
			/*
			 * LootPool.Builder builder = LootPool.builder()
			 * .name(ModBlocks.GOO_BLOCK.get().getRegistryName().toString())
			 * .rolls(ConstantRange.of(1)) .acceptCondition(SurvivesExplosion.builder())
			 * .addEntry(ItemLootEntry.builder(ModItems.GOO_RESIDUE.get()) );
			 * this.registerLootTable(ModBlocks.GOO_BLOCK.get(),
			 * LootTable.builder().addLootPool(builder));
			 * 
			 * LootPool.Builder builder2 = LootPool.builder()
			 * .name(ModBlocks.GOO_BLOCK_TERRAIN.get().getRegistryName().toString())
			 * .rolls(ConstantRange.of(1)) .acceptCondition(SurvivesExplosion.builder())
			 * .addEntry(ItemLootEntry.builder(ModItems.GOO_RESIDUE.get()) );
			 * this.registerLootTable(ModBlocks.GOO_BLOCK_TERRAIN.get(),
			 * LootTable.builder().addLootPool(builder2));
			 */
			BlockInit.getAllBlockEntries().stream().forEach(b->dropSelf(b));

		}

		@Override
		protected Iterable<Block> getKnownBlocks() {
			return ForgeRegistries.BLOCKS.getValues().stream()
					.filter(b -> b.getRegistryName().getNamespace().equals(Hemomancy.MOD_ID))
					.collect(Collectors.toList());
		}
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
		map.forEach((name, table) -> LootTables.validate(validationtracker, name, table));
	}
}