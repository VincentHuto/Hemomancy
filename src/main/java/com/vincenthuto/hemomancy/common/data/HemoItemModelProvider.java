package com.vincenthuto.hemomancy.common.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.registry.BlockInit;
import com.vincenthuto.hemomancy.common.registry.ItemInit;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HemoItemModelProvider extends ItemModelProvider {
	public HemoItemModelProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
		super(generator, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			if(b != BlockInit.sanguine_pane || b != BlockInit.active_befouling_ash_trail
					|| b != BlockInit.active_smouldering_ash_trail
					|| b != BlockInit.befouling_ash_trail
					|| b != BlockInit.smouldering_ash_trail) {
				registerBlockModel(b.get());
			}
		}
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Item> item : ItemInit.BASEITEMS.getEntries()) {
			basicItem(item.get());
		}
		for (RegistryObject<Item> item : ItemInit.SPAWNEGGS.getEntries()) {
			registerSpawnEggItem(item.get());
		}
		for (RegistryObject<Item> item : ItemInit.HANDHELDITEMS.getEntries()) {
			registerHandheldItem(item.get());
		}
	}

	private void registerBlockModel(Block block) {
		String path = ForgeRegistries.BLOCKS.getKey(block).getPath();
		getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
	}

	private void registerBasicItem(Item item) {
		String path = ForgeRegistries.ITEMS.getKey(item).getPath();
		singleTexture(path, mcLoc("item/generated"), "layer0", modLoc("item/" + path));

	}

	private void registerHandheldItem(Item item) {
		String path = ForgeRegistries.ITEMS.getKey(item).getPath();
		singleTexture(path, mcLoc("item/handheld"), "layer0", modLoc("item/" + path));
	}

	private void registerSpawnEggItem(Item item) {
		String path =ForgeRegistries.ITEMS.getKey(item).getPath();
		withExistingParent(path, mcLoc("item/template_spawn_egg"));
	}

	@Override
	public String getName() {
		return "Item Models";
	}
}