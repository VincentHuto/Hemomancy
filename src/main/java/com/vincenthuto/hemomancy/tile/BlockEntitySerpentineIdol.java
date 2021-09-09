package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.particle.factory.SerpentParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntitySerpentineIdol extends BlockEntity {

	public BlockEntitySerpentineIdol(BlockPos pos, BlockState state) {
		super(BlockEntityInit.serpentine_idol.get(), pos, state);
	}

	float count = 0.5f;

	public void tick() {
		Vector3 centerVec = Vector3.fromTileEntityCenter(this).add(0, 0, 0);
		double time = level.getGameTime();
		if (!this.level.isClientSide) {
			ServerLevel sLevel = (ServerLevel) level;
			sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 2, 0f, 0.0f, 0f, 0);
			sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 4, 0f, 0.0f, 0f, 0.0005f);
			sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 8, 0f, 0.0f, 0f, 0.0015f);
			sLevel.sendParticles(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 1, 0f, 0.0f, 0f, 0.0035f);

		}
	}

}
