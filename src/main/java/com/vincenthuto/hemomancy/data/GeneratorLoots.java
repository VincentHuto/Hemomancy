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
import com.vincenthuto.hemomancy.init.EntityInit;
import com.vincenthuto.hemomancy.init.ItemInit;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class GeneratorLoots extends LootTableProvider {
	public GeneratorLoots(DataGenerator dataGeneratorIn, ExistingFileHelper helper) {
		super(dataGeneratorIn);
	}

	@Override
	protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
		return ImmutableList.of(Pair.of(Blocks::new, LootContextParamSets.BLOCK),
				Pair.of(Entites::new, LootContextParamSets.ENTITY));
	}

	private static class Entites extends EntityLoot {
		@Override
		protected void addTables() {
			this.add(EntityInit.chitinite.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.chitinous_husk.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.chthonian.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.chitinous_husk.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));
			
			this.add(EntityInit.chthonian_queen.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.chitinous_husk.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.leech.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.swollen_leech.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.abhorent_thought.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.fargone.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.thirster.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.blood_drunk_puppeteer.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.puppeteering_thread.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.enthralled_doll.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.bleeding_bulb.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.lump_of_thought.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.morphling_polyp.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));

			this.add(EntityInit.fungling.get(), LootTable.lootTable().withPool(LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(LootItem.lootTableItem(BlockInit.infected_fungus.get())
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
							.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
					.when(LootItemKilledByPlayerCondition.killedByPlayer())));
		}

		@Override
		protected Iterable<EntityType<?>> getKnownEntities() {
			return ForgeRegistries.ENTITIES.getValues().stream()
					.filter(b -> b.getRegistryName().getNamespace().equals(Hemomancy.MOD_ID))
					.collect(Collectors.toList());
		}
	}

	private static class Blocks extends BlockLoot {
		@Override
		protected void addTables() {

			BlockInit.getAllBlockEntries().stream().forEach(b -> dropSelf(b));

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