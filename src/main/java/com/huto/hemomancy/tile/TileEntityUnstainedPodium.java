package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

public class TileEntityUnstainedPodium extends BlockEntity implements TickableBlockEntity {

	public TileEntityUnstainedPodium() {
		super(TileEntityInit.unstained_podium.get());
	}

	@Override
	public void tick() {

	}
}
