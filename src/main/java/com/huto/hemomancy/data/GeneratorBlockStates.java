package com.huto.hemomancy.data;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.init.BlockInit;

import net.minecraft.block.Block;
import net.minecraft.block.PaneBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.RegistryObject;

public class GeneratorBlockStates extends BlockStateProvider {
	public GeneratorBlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
		super(gen, Hemomancy.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {

		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			simpleBlock(b.get());
			cubeAll(b.get());

		}
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			String name = Registry.BLOCK.getKey(b.get()).getPath();
			ModelFile model = models()
					.withExistingParent(name, new ResourceLocation(Hemomancy.MOD_ID, "block/shapes/cross"))
					.texture("cross", new ResourceLocation(Hemomancy.MOD_ID, "block/" + name));
			simpleBlock(b.get(), model);
		}
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			if (b == BlockInit.sanguine_pane) {
				String path = BlockInit.sanguine_pane.get().getRegistryName().getPath();
				paneBlock((PaneBlock) b.get(), b.get().getRegistryName().getPath(), modLoc("block/" + path),
						modLoc("block/" + path + "_top"));
			} else {

			}
		}

		/*
		 * for (RegistryObject<Block> b : BlockInit.MODELEDBLOCKS.getEntries()) {
		 * blockTexture(b.get()); }
		 * 
		 * for (RegistryObject<Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
		 * axisBlock((RotatedPillarBlock) b.get(), b.get().getRegistryName(), new
		 * ResourceLocation(Hemomancy.MOD_ID, b.get().getRegistryName().getPath() +
		 * "_end")); blockTexture(b.get());
		 * 
		 * }
		 */
	}

}
