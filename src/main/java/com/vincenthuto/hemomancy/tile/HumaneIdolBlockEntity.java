package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.DarkGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HumaneIdolBlockEntity extends BlockEntity {

	public HumaneIdolBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.humane_idol.get(), pos, state);
	}

	float count = 0.5f;

	public void tick() {
		Vector3 centerVec = Vector3.fromBlockEntityCenter(this).add(0, -1, 0);
		double time = level.getGameTime();
		if (level.isClientSide) {
			int globalPartCount = 90;
			for (int i = 0; i < 16; i++) {
				count += 0.0005;
				if (count > 2) {
					count = 0.5f;
				}
			}
			double cos = Math.cos(time * 0.75) * count;
			double sin = Math.sin(time * 0.75) * count;

			for (int i = 0; i < globalPartCount; i++) {
				level.addParticle(DarkGlowParticleFactory.createData(ParticleColor.BLACK), centerVec.x + cos,
						centerVec.y + 1, centerVec.z - sin, 0, 0.00, 0);
				level.addParticle(DarkGlowParticleFactory.createData(ParticleColor.RED), centerVec.x - cos,
						centerVec.y + 1, centerVec.z + sin, 0, 0.00, 0);
			}
		}

	}

}
