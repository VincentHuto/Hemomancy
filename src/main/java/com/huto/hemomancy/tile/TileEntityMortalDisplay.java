package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMortalDisplay extends TileEntity implements ITickableTileEntity {

	public TileEntityMortalDisplay() {
		super(TileEntityInit.mortal_display.get());
	}

	@Override
	public void tick() {

	}
}