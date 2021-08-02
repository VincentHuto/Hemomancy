package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.BlockEntityInit;
import com.hutoslib.client.particle.factory.DarkGlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.math.Vector3;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

public class BlockEntityHumaneIdol extends BlockEntity implements TickableBlockEntity {

	public BlockEntityHumaneIdol() {
		super(BlockEntityInit.humane_idol.get());
	}

	float count = 0.5f;

	@Override
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
