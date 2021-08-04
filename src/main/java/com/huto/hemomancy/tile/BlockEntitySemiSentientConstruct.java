package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntitySemiSentientConstruct extends BlockEntity  {

	public BlockEntitySemiSentientConstruct(BlockPos pos, BlockState state) {
		super(BlockEntityInit.semi_sentient_construct.get(), pos, state);
	}

	public void tick() {

		if (level.isClientSide) {
			/*
			 * double randX = pos.getX() + Math.sin(world.getGameTime() * 0.2f) * 0.3f +
			 * 0.4f; double randY = pos.getY(); double randZ = pos.getZ() +
			 * Math.cos(world.getGameTime() * 0.2f) * 0.3f + 0.4f;
			 * 
			 * world.addParticle(GlowParticleData.createData(new ParticleColor(255, 0,255)),
			 * randX + 0.1, randY + 1, randZ + 0.1, 0, 0.00, 0);
			 * 
			 * double randX1 = pos.getX() + Math.sin(-world.getGameTime() * 0.2f) * 0.3f +
			 * 0.4f; double randY1 = pos.getY(); double randZ1 = pos.getZ() +
			 * Math.cos(-world.getGameTime() * 0.2f) * 0.3f + 0.4f;
			 * world.addParticle(GlowParticleData.createData(new ParticleColor(255, 0,
			 * 255)), randX1 + 0.1, randY1 + 1, randZ1 + 0.1, 0, 0.005, 0);
			 */
		}

	}
}
