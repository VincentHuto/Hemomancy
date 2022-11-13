package com.vincenthuto.hemomancy.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ItemModelGenerator extends ItemModelProvider {
	public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return "Item Models";
	}

	private void registerBasicItem(Item item) {
		String path = HLTextUtils.getItemRegistryName(item);
		singleTexture(path, mcLoc("item/generated"), "layer0", modLoc("item/" + path));

	}

	private void registerBlockModel(Block block) {
		String path = HLTextUtils.getBlockRegistryName(block);
		getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
	}

	private void registerHandheldItem(Item item) {
		String path = HLTextUtils.getItemRegistryName(item);
		singleTexture(path, mcLoc("item/handheld"), "layer0", modLoc("item/" + path));
	}

	@Override
	protected void registerModels() {
		// Our block items

		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			registerBasicItem(b.get().asItem());
		}
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Item> item : ItemInit.BASEITEMS.getEntries()) {
			registerBasicItem(item.get());
		}
		for (RegistryObject<Item> item : ItemInit.SPAWNEGGS.getEntries()) {
			registerSpawnEggItem(item.get());
		}
		for (RegistryObject<Item> item : ItemInit.HANDHELDITEMS.getEntries()) {
			registerHandheldItem(item.get());
		}
	}

	private void registerSpawnEggItem(Item item) {
		String path = HLTextUtils.getItemRegistryName(item);
		withExistingParent(path, mcLoc("item/template_spawn_egg"));
	}
}