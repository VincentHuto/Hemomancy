package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
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
		registerBaseBlocks();
		registerSlabBlocks();
		registerStairBlocks();
		registerColumnBlocks();
		registerCrossBlocks();
		registerSpecialBlocks();
		registerPottedBlocks();
	}

	private void registerBaseBlocks() {
		for (RegistryObject<Block> b : BlockInit.BASEBLOCKS.getEntries()) {
			Block block = b.get();
			if (block == BlockInit.erythrocytic_mycelium.get()) {
				String name = getName(block);
				ModelFile model = models()
						.withExistingParent(name, mcLoc("block/cube_bottom_top"))
						.texture("top", modLoc("block/" + name + "_top"))
						.texture("bottom", modLoc("block/" + name + "_bottom"))
						.texture("side", modLoc("block/" + name + "_side"));
				simpleBlock(block, model);
			} else if (block instanceof StairBlock stairBlock) {
				String name = getName(block);
				String baseName = resolveBaseTextureName(name, "_stairs");
				stairsBlock(stairBlock, modLoc("block/" + baseName));
			} else {
				simpleBlock(block);
				cubeAll(block);
			}
		}
	}

	private void registerSlabBlocks() {
		for (RegistryObject<Block> b : BlockInit.SLABBLOCKS.getEntries()) {
			Block block = b.get();
			if (block instanceof SlabBlock slabBlock) {
				String name = getName(block);
				String baseName = resolveBaseTextureName(name, "_slab");
				slabBlock(slabBlock,
						modLoc("block/" + baseName),
						modLoc("block/" + baseName));
			} else {
				simpleBlock(block);
				cubeAll(block);
			}
		}
	}

	private void registerStairBlocks() {
		for (RegistryObject<Block> b : BlockInit.STAIRBLOCKS.getEntries()) {
			Block block = b.get();
			if (block instanceof StairBlock stairBlock) {
				String name = getName(block);
				String baseName = resolveBaseTextureName(name, "_stairs");
				stairsBlock(stairBlock, modLoc("block/" + baseName));
			} else {
				simpleBlock(block);
				cubeAll(block);
			}
		}
	}

	private void registerColumnBlocks() {
		for (RegistryObject<Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
			Block block = b.get();
			if (block instanceof RotatedPillarBlock pillarBlock) {
				String name = getName(block);
				ResourceLocation side = modLoc("block/" + name);
				ResourceLocation end = modLoc("block/" + name + "_top");
				axisBlock(pillarBlock, side, end);
			} else {
				simpleBlock(block);
				cubeAll(block);
			}
		}
	}

	private void registerCrossBlocks() {
		for (RegistryObject<Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
			String name = getName(b.get());
			ModelFile model = models()
					.withExistingParent(name, mcLoc("block/cross"))
					.renderType("cutout")
					.texture("cross", modLoc("block/" + name));
			simpleBlock(b.get(), model);
		}
	}

	private void registerSpecialBlocks() {
		for (RegistryObject<Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
			Block block = b.get();

			if (block instanceof IronBarsBlock paneBlock) {
				String name = getName(block);
				paneBlock(paneBlock, modLoc("block/" + name), modLoc("block/" + name + "_top"));
			}
			// Ash trails, engram, filler, and crimson flames use custom rendering
			// and are intentionally skipped from automated data generation
		}
	}

	private void registerPottedBlocks() {
		for (RegistryObject<Block> b : BlockInit.POTTEDBLOCKS.getEntries()) {
			Block block = b.get();
			String name = getName(block);
			if (block instanceof FlowerPotBlock potBlock) {
				String plantName = name.replace("potted_", "");
				ModelFile model = models()
						.withExistingParent(name, mcLoc("block/flower_pot_cross"))
						.renderType("cutout")
						.texture("plant", modLoc("block/" + plantName));
				simpleBlock(block, model);
			}
		}
	}

	private String getName(Block block) {
		return ForgeRegistries.BLOCKS.getKey(block).getPath();
	}

	/**
	 * Resolves the base texture name for variant blocks (stairs, slabs) by stripping
	 * the suffix and checking if a plural form of the base block exists.
	 * Handles edge cases like "polished_venous_stone_brick_stairs" where the base
	 * texture is "polished_venous_stone_bricks" (plural), not "polished_venous_stone_brick".
	 */
	private String resolveBaseTextureName(String blockName, String suffix) {
		String base = blockName.replace(suffix, "");
		ResourceLocation pluralLoc = new ResourceLocation(Hemomancy.MOD_ID, base + "s");
		if (ForgeRegistries.BLOCKS.containsKey(pluralLoc)) {
			return base + "s";
		}
		return base;
	}
}
