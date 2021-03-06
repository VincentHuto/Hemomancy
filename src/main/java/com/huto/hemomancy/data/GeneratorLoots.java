package com.huto.hemomancy.data;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.BlockInit;
import com.mojang.datafixers.util.Pair;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

public class GeneratorLoots extends LootTableProvider {
	public GeneratorLoots(DataGenerator dataGeneratorIn) {
		super(dataGeneratorIn);
	}

	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
		return ImmutableList.of(Pair.of(Blocks::new, LootParameterSets.BLOCK));
	}

	private static class Blocks extends BlockLootTables {
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
			
			

			for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
				this.registerDropSelfLootTable(b.get());
			}
			for (RegistryObject<Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
				this.registerDropSelfLootTable(b.get());
			}
			for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
				this.registerDropSelfLootTable(b.get());
			}
			for (RegistryObject<Block> b : BlockInit.MODELEDBLOCKS.getEntries()) {
				this.registerDropSelfLootTable(b.get());
			}
			for (RegistryObject<Block> b : BlockInit.OBJBLOCKS.getEntries()) {
				this.registerDropSelfLootTable(b.get());
			}
			for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
				this.registerDropSelfLootTable(b.get());
			}

		}

		@Override
		protected Iterable<Block> getKnownBlocks() {
			return ForgeRegistries.BLOCKS.getValues().stream()
					.filter(b -> b.getRegistryName().getNamespace().equals(Hemomancy.MOD_ID))
					.collect(Collectors.toList());
		}
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
		map.forEach((name, table) -> LootTableManager.validateLootTable(validationtracker, name, table));
	}
}