package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityDendriticDistributor extends TileEntity implements ITickableTileEntity {

	public TileEntityDendriticDistributor() {
		super(TileEntityInit.dendritic_distributor.get());
	}

	@Override
	public void tick() {

	}
}
