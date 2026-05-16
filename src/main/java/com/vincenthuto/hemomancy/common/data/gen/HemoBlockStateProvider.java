package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

public class HemoBlockStateProvider extends BlockStateProvider {
    private final ExistingFileHelper existingFileHelper;

    public HemoBlockStateProvider(PackOutput gen, ExistingFileHelper exFileHelper) {
        super(gen, Hemomancy.MOD_ID, exFileHelper);
        this.existingFileHelper = exFileHelper;
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
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.BASEBLOCKS.getEntries()) {
            Block block = b.get();
			if (shouldSkipGeneratedBlockstate(block)) {
                continue;
            }
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
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.SLABBLOCKS.getEntries()) {
            Block block = b.get();
			if (shouldSkipGeneratedBlockstate(block)) {
                continue;
            }
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
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.STAIRBLOCKS.getEntries()) {
            Block block = b.get();
			if (shouldSkipGeneratedBlockstate(block)) {
                continue;
            }
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
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.COLUMNBLOCKS.getEntries()) {
            Block block = b.get();
			if (shouldSkipGeneratedBlockstate(block)) {
                continue;
            }
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
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.CROSSBLOCKS.getEntries()) {
            Block block = b.get();
			if (shouldSkipGeneratedBlockstate(block)) {
                continue;
            }
            String name = getName(block);
            ModelFile model = models()
                    .withExistingParent(name, mcLoc("block/cross"))
                    .renderType("cutout")
                    .texture("cross", modLoc("block/" + name));
            simpleBlock(block, model);
        }
    }

    private void registerSpecialBlocks() {
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.SPECIALBLOCKS.getEntries()) {
            Block block = b.get();
			if (shouldSkipGeneratedBlockstate(block)) {
                continue;
            }

            if (block instanceof IronBarsBlock paneBlock) {
                String name = getName(block);
                paneBlock(paneBlock, modLoc("block/" + name), modLoc("block/" + name + "_top"));
            }
            // Ash trails, engram, filler, and crimson flames use custom rendering
            // and are intentionally skipped from automated data generation.
        }
    }

    private void registerPottedBlocks() {
        for (DeferredHolder<Block, ? extends Block> b : BlockInit.POTTEDBLOCKS.getEntries()) {
            Block block = b.get();
			if (shouldSkipGeneratedBlockstate(block)) {
                continue;
            }
            String name = getName(block);
            if (block instanceof FlowerPotBlock) {
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
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

	private boolean hasHandAuthoredBlockstate(Block block) {
		return existingFileHelper.exists(Hemomancy.rloc(getName(block)), PackType.CLIENT_RESOURCES, ".json", "blockstates");
	}

	private boolean shouldSkipGeneratedBlockstate(Block block) {
		return hasHandAuthoredBlockstate(block)
				|| block == BlockInit.attached_gourd_stem.get()
				|| block == BlockInit.gourd_stem.get()
				|| block == BlockInit.active_befouling_ash_trail.get()
				|| block == BlockInit.active_smouldering_ash_trail.get()
				|| block == BlockInit.engram_block.get()
				|| block == BlockInit.filler_block.get()
				|| block == BlockInit.qliphoth_bloom.get()
				|| block == BlockInit.sanguine_conduit.get();
	}

    /**
     * Resolves the base texture name for variant blocks (stairs, slabs) by stripping
     * the suffix and checking if a plural form of the base block exists.
     * Handles edge cases like "polished_venous_stone_brick_stairs" where the base
     * texture is "polished_venous_stone_bricks" (plural), not "polished_venous_stone_brick".
     */
    private String resolveBaseTextureName(String blockName, String suffix) {
        String base = blockName.replace(suffix, "");
        ResourceLocation pluralLoc = Hemomancy.rloc(base + "s");
        if (BuiltInRegistries.BLOCK.containsKey(pluralLoc)) {
            return base + "s";
        }
        return base;
    }
}
