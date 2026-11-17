package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.GuyStakeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The big top's guy stake: the set piece, plus a block entity whose renderer draws its line to the tent. */
public class TroupeGuyStakeBlock extends TroupePropBlock implements EntityBlock {
	/** The tent blocks a guy line ties off on. */
	public static final TagKey<Block> ANCHORS = TagKey.create(Registries.BLOCK, Hemomancy.rloc("guy_line_anchors"));

	public TroupeGuyStakeBlock(Properties properties, double[]... outline) {
		super(properties, outline);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GuyStakeBlockEntity(pos, state);
	}
}
