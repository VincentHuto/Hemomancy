package com.vincenthuto.hemomancy.common.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;

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
		for (RegistryObject<Block> b : BlockInit.SLABBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Block> b : BlockInit.STAIRBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			Block block = b.get();
			if (block != BlockInit.sanguine_pane.get()
					&& block != BlockInit.cleansed_sanguine_pane.get()
					&& block != BlockInit.active_befouling_ash_trail.get()
					&& block != BlockInit.active_smouldering_ash_trail.get()
					&& block != BlockInit.befouling_ash_trail.get()
					&& block != BlockInit.smouldering_ash_trail.get()
					&& block != BlockInit.engram_block.get()
					&& block != BlockInit.filler_block.get()
					&& block != BlockInit.crimson_flames.get()) {
				registerBlockModel(block);
			}
		}
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (RegistryObject<Item> item : ItemInit.BASEITEMS.getEntries()) {
			if (item.get() instanceof ItemRunePattern patternItem) {
				String itemPath = ForgeRegistries.ITEMS.getKey(item.get()).getPath();
				String runePath = ForgeRegistries.ITEMS.getKey(patternItem.getRune().get()).getPath();
				getBuilder(itemPath)
						.parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
						.texture("layer0", modLoc("item/rune_pattern"))
						.texture("layer1", modLoc("item/" + runePath));
			} else if (item.get() instanceof BloodMemoryItem) {
				String itemPath = ForgeRegistries.ITEMS.getKey(item.get()).getPath();
				getBuilder(itemPath)
						.parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
						.texture("layer0", modLoc("item/memories/memory_blank"))
						.texture("layer1", modLoc("item/memories/" + itemPath + "_overlay"));
			} else {
				basicItem(item.get());
			}
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

	private void registerHandheldItem(Item item) {
		String path = ForgeRegistries.ITEMS.getKey(item).getPath();
		singleTexture(path, mcLoc("item/handheld"), "layer0", modLoc("item/" + path));
	}

	private void registerSpawnEggItem(Item item) {
		String path = ForgeRegistries.ITEMS.getKey(item).getPath();
		withExistingParent(path, mcLoc("item/template_spawn_egg"));
	}

	@Override
	public String getName() {
		return "Item Models";
	}
}