package com.vincenthuto.hemomancy.data;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GeneratorItemModels extends ItemModelProvider {
	public GeneratorItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		// Our block items
//
//		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
//			registerBlockModel(b.get());
//		}
//		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
//			registerBasicItem(b.get().asItem());
//		}
//		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
//			registerBlockModel(b.get());
//		}
//		for (RegistryObject<Item> item : ItemInit.BASEITEMS.getEntries()) {
//			registerBasicItem(item.get());
//		}
//		for (RegistryObject<Item> item : ItemInit.SPAWNEGGS.getEntries()) {
//			registerSpawnEggItem(item.get());
//		}
//		for (RegistryObject<Item> item : ItemInit.HANDHELDITEMS.getEntries()) {
//			registerHandheldItem(item.get());
//		}
	}

//	private void registerBlockModel(Block block) {
//		String path = block.getRegistryName().getPath();
//		getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
//	}
//
//	private void registerBasicItem(Item item) {
//		String path = item.getRegistryName().getPath();
//		if (!(item instanceof ItemRunePattern)) {
//			singleTexture(path, mcLoc("item/generated"), "layer0", modLoc("item/" + path));
//		} else {
//			singleTexture(path, mcLoc("item/generated"), "layer0", modLoc("item/rune_pattern"));
//
//		}
//
//	}
//
//	private void registerHandheldItem(Item item) {
//		String path = item.getRegistryName().getPath();
//		singleTexture(path, mcLoc("item/handheld"), "layer0", modLoc("item/" + path));
//	}
//
//	private void registerSpawnEggItem(Item item) {
//		String path = item.getRegistryName().getPath();
//		withExistingParent(path, mcLoc("item/template_spawn_egg"));
//	}

	@Override
	public String getName() {
		return "Item Models";
	}
}