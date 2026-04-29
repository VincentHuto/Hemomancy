package com.vincenthuto.hemomancy.common.block.functional;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * The Sanguine Conduit in its placed form — a covenant anchor driven into the
 * ground by an Illuminatus who has reached Degree 5. The block has no block
 * entity; all oath-witness logic is handled at placement time (or by future
 * consecration mechanics). Placement is gated in
 * {@link com.vincenthuto.hemomancy.common.item.ItemSanguineConduit#useOn} to
 * require Initiatory Degree ≥ 5; the block itself imposes no runtime checks.
 */
public class SanguineConduitBlock extends Block {

	public SanguineConduitBlock() {
		super(BlockBehaviour.Properties.of()
				.mapColor(MapColor.COLOR_RED)
				.requiresCorrectToolForDrops()
				.strength(3.0f, 6.0f)
				.sound(SoundType.BONE_BLOCK)
				.lightLevel(state -> 4));
	}
}
