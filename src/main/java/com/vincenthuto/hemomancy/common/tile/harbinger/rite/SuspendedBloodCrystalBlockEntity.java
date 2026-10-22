package com.vincenthuto.hemomancy.common.tile.harbinger.rite;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SuspendedBloodCrystalBlockEntity extends BlockEntity {

	public float timeOffset;

	public SuspendedBloodCrystalBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.suspended_blood_crystal.get(), pos, state);
	}

	@Override
	public void onLoad() {
		if(level !=null){
			this.timeOffset = Mth.randomBetween(level.random,-3.0f,3.0f);
		}
	}
}
