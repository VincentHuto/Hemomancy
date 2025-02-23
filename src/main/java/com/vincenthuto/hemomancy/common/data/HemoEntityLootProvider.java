package com.vincenthuto.hemomancy.common.data;

import java.util.function.BiConsumer;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable.Builder;

public class HemoEntityLootProvider implements  LootTableSubProvider  {

	@Override
	public void generate(BiConsumer<ResourceLocation, Builder> p_249643_) {
	}



	}




	// @Override
	// protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
	// 	return ImmutableList.of(Pair.of(Blocks::new, LootContextParamSets.BLOCK),
	// 			Pair.of(Entites::new, LootContextParamSets.ENTITY));
	// }

	// private static class Entites extends EntityLoot {
	// 	@Override
	// 	protected void addTables() {
	// 		this.add(EntityInit.chitinite.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.chitinous_husk.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.chthonian.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.chitinous_husk.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));
			
	// 		this.add(EntityInit.chthonian_queen.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.chitinous_husk.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.leech.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.swollen_leech.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.abhorent_thought.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.fargone.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.thirster.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.blood_drunk_puppeteer.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.puppeteering_thread.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.enthralled_doll.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.bleeding_bulb.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.lump_of_thought.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.morphling_polyp.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(ItemInit.sanguine_formation.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));

	// 		this.add(EntityInit.fungling.get(), LootTable.lootTable().withPool(LootPool.lootPool()
	// 				.setRolls(ConstantValue.exactly(1.0F))
	// 				.add(LootItem.lootTableItem(BlockInit.infected_fungus.get())
	// 						.apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
	// 						.apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))
	// 				.when(LootItemKilledByPlayerCondition.killedByPlayer())));
	// 	}

	// 	@Override
	// 	protected Iterable<EntityType<?>> getKnownEntities() {
	// 		return ForgeRegistries.ENTITIES.getValues().stream()
	// 				.filter(b -> b.getRegistryName().getNamespace().equals(Hemomancy.MOD_ID))
	// 				.collect(Collectors.toList());
	// 	}
	// }

	// private static class Blocks extends BlockLoot {
	// 	@Override
	// 	protected void addTables() {

	// 		BlockInit.getAllBlockEntries().stream().forEach(b -> dropSelf(b));

	// 	}

	// 	@Override
	// 	protected Iterable<Block> getKnownBlocks() {
	// 		return ForgeRegistries.BLOCKS.getValues().stream()
	// 				.filter(b -> b.getRegistryName().getNamespace().equals(Hemomancy.MOD_ID))
	// 				.collect(Collectors.toList());
	// 	}
	// }

	// @Override
	// protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
	// 	map.forEach((name, table) -> LootTables.validate(validationtracker, name, table));
	// }