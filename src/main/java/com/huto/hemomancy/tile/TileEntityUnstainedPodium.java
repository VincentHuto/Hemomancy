package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityUnstainedPodium extends TileEntity implements ITickableTileEntity {

	public TileEntityUnstainedPodium() {
		super(TileEntityInit.unstained_podium.get());
	}

	@Override
	public void tick() {

	}
}
