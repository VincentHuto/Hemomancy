package com.vincenthuto.hemomancy.common.data;

import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HemoBlockTagProvider extends BlockTagsProvider {

	public HemoBlockTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider,
			@Nullable ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, Hemomancy.MOD_ID, existingFileHelper);
	}

	@Override
	public String getName() {
		return super.getName() + ": " + Hemomancy.MOD_ID;
	}

	// .filter(b->b.defaultBlockState().getMaterial()==Material.STONE)))
	// tag(BlockTags.SLABS).add(FBContent.blockFramedSlab.get());
	// tag(BlockTags.STAIRS).add(FBContent.blockFramedStairs.get());
	// tag(BlockTags.WALLS).add(FBContent.blockFramedWall.get());
	// tag(BlockTags.FENCES).add(FBContent.blockFramedFence.get());
	// tag(BlockTags.DOORS).add(FBContent.blockFramedDoor.get(),
	// FBContent.blockFramedIronDoor.get());
	// tag(BlockTags.WOODEN_DOORS).add(FBContent.blockFramedDoor.get());
	// tag(BlockTags.TRAPDOORS).add(FBContent.blockFramedTrapDoor.get(),
	// FBContent.blockFramedIronTrapDoor.get());
	// tag(BlockTags.WOODEN_TRAPDOORS).add(FBContent.blockFramedTrapDoor.get());
	// tag(BlockTags.CLIMBABLE).add(FBContent.blockFramedLadder.get());
	// tag(BlockTags.SIGNS).add(FBContent.blockFramedSign.get(),
	// FBContent.blockFramedWallSign.get());
	// tag(BlockTags.STANDING_SIGNS).add(FBContent.blockFramedSign.get());
	// tag(BlockTags.WALL_SIGNS).add(FBContent.blockFramedWallSign.get());
	// tag(Tags.Blocks.CHESTS).add(FBContent.blockFramedChest.get());
	// tag(BlockTags.RAILS).add(FBContent.blockFramedRailSlope.get());


	@Override
	protected void addTags(Provider p_256380_) {
//		TagsProvider.TagAppender<Block> axeTag = tag(BlockTags.MINEABLE_WITH_AXE);
//		TagsProvider.TagAppender<Block> pickTag = tag(BlockTags.MINEABLE_WITH_PICKAXE);
//
//		
//		BlockInit.getAllBlockEntries().stream()
//				.filter(b -> b.defaultBlockState().getMaterial() == Material.WOOD).map(b -> ResourceKey.create(Registries.BLOCK	, ForgeRegistries.BLOCKS.getKey(b))).forEach(axeTag::add);
//		BlockInit.getAllBlockEntries().stream()
//				.filter(b -> b.defaultBlockState().getMaterial() == Material.STONE).map(b -> ResourceKey.create(Registries.BLOCK	, ForgeRegistries.BLOCKS.getKey(b))).forEach(pickTag::add);
//		BlockInit.getAllBlockEntries().stream()
//				.filter(b -> b.defaultBlockState().getMaterial() == Material.METAL).map(b -> ResourceKey.create(Registries.BLOCK	, ForgeRegistries.BLOCKS.getKey(b))).forEach(pickTag::add);	
	}
}