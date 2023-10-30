package com.vincenthuto.hemomancy.common.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.registry.BlockInit;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class HemoBlockStateProvider extends BlockStateProvider {
	public HemoBlockStateProvider(PackOutput gen, ExistingFileHelper exFileHelper) {
		super(gen, Hemomancy.MOD_ID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {

		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			if (b == BlockInit.erythrocytic_mycelium) {

			} else {
				simpleBlock(b.get());
				cubeAll(b.get());
			}

		}
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			String name = ForgeRegistries.BLOCKS.getKey(b.get()).getPath();
			ModelFile model = models()
					.withExistingParent(name, new ResourceLocation(Hemomancy.MOD_ID, "block/shapes/cross"))
					.texture("cross", new ResourceLocation(Hemomancy.MOD_ID, "block/" + name));
			simpleBlock(b.get(), model);
		}
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			if (b == BlockInit.sanguine_pane) {

				String path = ForgeRegistries.BLOCKS.getKey(BlockInit.sanguine_pane.get()).getPath();
				paneBlock((IronBarsBlock) b.get(), ForgeRegistries.BLOCKS.getKey(b.get()).getPath(),
						modLoc("block/" + path), modLoc("block/" + path + "_top"));
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
