package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.BlockEntityInit;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

public class BlockEntityUnstainedPodium extends BlockEntity implements TickableBlockEntity {

	public BlockEntityUnstainedPodium() {
		super(BlockEntityInit.unstained_podium.get());
	}

	@Override
	public void tick() {

	}
}
