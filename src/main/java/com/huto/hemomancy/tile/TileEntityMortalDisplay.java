package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.BlockEntityInit;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

public class BlockEntityMortalDisplay extends BlockEntity implements TickableBlockEntity {

	public BlockEntityMortalDisplay() {
		super(BlockEntityInit.mortal_display.get());
	}

	@Override
	public void tick() {

	}
}