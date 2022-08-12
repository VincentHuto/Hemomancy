package com.vincenthuto.hemomancy.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.BlockInit;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockTagGenerator extends BlockTagsProvider {
	public BlockTagGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
		super(gen, Hemomancy.MOD_ID, exFileHelper);
	}

	@Override
	public String getName() {
		return super.getName() + ": " + Hemomancy.MOD_ID;
	}

	@Override
	protected void addTags() {

		TagsProvider.TagAppender<Block> axeTag = tag(BlockTags.MINEABLE_WITH_AXE);
		TagsProvider.TagAppender<Block> pickTag = tag(BlockTags.MINEABLE_WITH_PICKAXE);
		BlockInit.getAllBlockEntries().stream()
				.filter(b -> b.defaultBlockState().getMaterial() == Material.WOOD).forEach(axeTag::add);
		BlockInit.getAllBlockEntries().stream()
				.filter(b -> b.defaultBlockState().getMaterial() == Material.STONE).forEach(pickTag::add);
		BlockInit.getAllBlockEntries().stream()
				.filter(b -> b.defaultBlockState().getMaterial() == Material.METAL).forEach(pickTag::add);

		// .filter(b->b.defaultBlockState().getMaterial()==Material.STONE)))
//		tag(BlockTags.SLABS).add(FBContent.blockFramedSlab.get());
//		tag(BlockTags.STAIRS).add(FBContent.blockFramedStairs.get());
//		tag(BlockTags.WALLS).add(FBContent.blockFramedWall.get());
//		tag(BlockTags.FENCES).add(FBContent.blockFramedFence.get());
//		tag(BlockTags.DOORS).add(FBContent.blockFramedDoor.get(), FBContent.blockFramedIronDoor.get());
//		tag(BlockTags.WOODEN_DOORS).add(FBContent.blockFramedDoor.get());
//		tag(BlockTags.TRAPDOORS).add(FBContent.blockFramedTrapDoor.get(), FBContent.blockFramedIronTrapDoor.get());
//		tag(BlockTags.WOODEN_TRAPDOORS).add(FBContent.blockFramedTrapDoor.get());
//		tag(BlockTags.CLIMBABLE).add(FBContent.blockFramedLadder.get());
//		tag(BlockTags.SIGNS).add(FBContent.blockFramedSign.get(), FBContent.blockFramedWallSign.get());
//		tag(BlockTags.STANDING_SIGNS).add(FBContent.blockFramedSign.get());
//		tag(BlockTags.WALL_SIGNS).add(FBContent.blockFramedWallSign.get());
//		tag(Tags.Blocks.CHESTS).add(FBContent.blockFramedChest.get());
//		tag(BlockTags.RAILS).add(FBContent.blockFramedRailSlope.get());

	}
}