package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HemoItemModelProvider extends ItemModelProvider {
	public HemoItemModelProvider(PackOutput generator, ExistingFileHelper existingFileHelper) {
		super(generator, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		for (DeferredHolder<Block, ? extends Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (DeferredHolder<Block, ? extends Block> b : BlockInit.SLABBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (DeferredHolder<Block, ? extends Block> b : BlockInit.STAIRBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (DeferredHolder<Block, ? extends Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (DeferredHolder<Block, ? extends Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			Block block = b.get();
			if (block != BlockInit.sanguine_pane.get()
					&& block != BlockInit.cleansed_sanguine_pane.get()
					&& block != BlockInit.active_befouling_ash_trail.get()
					&& block != BlockInit.active_smouldering_ash_trail.get()
					&& block != BlockInit.befouling_ash_trail.get()
					&& block != BlockInit.virid_salis_trail.get()
					&& block != BlockInit.smouldering_ash_trail.get()
					&& block != BlockInit.engram_block.get()
					&& block != BlockInit.filler_block.get()
					&& block != BlockInit.crimson_flames.get()) {
				registerBlockModel(block);
			}
		}
		for (DeferredHolder<Block, ? extends Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			registerBlockModel(b.get());
		}
		for (DeferredHolder<Item, ? extends Item> item : ItemInit.BASEITEMS.getEntries()) {
			if (item.get() instanceof ItemScarPattern patternItem) {
				String itemPath = BuiltInRegistries.ITEM.getKey(item.get()).getPath();
				String scarPath = BuiltInRegistries.ITEM.getKey(patternItem.getSCAR().get()).getPath();
				getBuilder(itemPath)
						.parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
						.texture("layer0", modLoc("item/scar_pattern"))
						.texture("layer1", modLoc("item/" + scarPath));
			} else if (item.get() instanceof BloodMemoryItem) {
				String itemPath = BuiltInRegistries.ITEM.getKey(item.get()).getPath();
				getBuilder(itemPath)
						.parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
						.texture("layer0", modLoc("item/memories/memory_blank"))
						.texture("layer1", modLoc("item/memories/" + itemPath + "_overlay"));
			} else if (item.get() == ItemInit.hemolymphopoda_headpiece.get()) {
				getBuilder("hemolymphopoda_headpiece")
						.parent(new ModelFile.UncheckedModelFile("builtin/entity"));
			} else if (item.get() == ItemInit.hematic_suture_needle.get()) {
				getBuilder("hematic_suture_needle")
						.parent(new ModelFile.UncheckedModelFile(mcLoc("item/generated")))
						.texture("layer0", modLoc("item/blood_projection"));
			} else {
				basicItem(item.get());
			}
		}
		for (DeferredHolder<Item, ? extends Item> item : ItemInit.SPAWNEGGS.getEntries()) {
			registerSpawnEggItem(item.get());
		}
		for (DeferredHolder<Item, ? extends Item> item : ItemInit.HANDHELDITEMS.getEntries()) {
			registerHandheldItem(item.get());
		}
	}

	private void registerBlockModel(Block block) {
		String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
		getBuilder(path).parent(new ModelFile.UncheckedModelFile(modLoc("block/" + path)));
	}

	private void registerHandheldItem(Item item) {
		String path = BuiltInRegistries.ITEM.getKey(item).getPath();
		singleTexture(path, mcLoc("item/handheld"), "layer0", modLoc("item/" + path));
	}

	private void registerSpawnEggItem(Item item) {
		String path = BuiltInRegistries.ITEM.getKey(item).getPath();
		withExistingParent(path, mcLoc("item/template_spawn_egg"));
	}

	@Override
	public String getName() {
		return "Item Models";
	}
}
