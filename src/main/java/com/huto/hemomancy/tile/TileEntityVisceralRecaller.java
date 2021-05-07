package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityVisceralRecaller extends TileEntity implements ITickableTileEntity {

	public TileEntityVisceralRecaller() {
		super(TileEntityInit.visceral_artificial_recaller.get());
	}

	@Override
	public void tick() {

	}
}