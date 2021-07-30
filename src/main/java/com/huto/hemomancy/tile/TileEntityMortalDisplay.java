package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;

import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TileEntityMortalDisplay extends BlockEntity implements TickableBlockEntity {

	public TileEntityMortalDisplay() {
		super(TileEntityInit.mortal_display.get());
	}

	@Override
	public void tick() {

	}
}