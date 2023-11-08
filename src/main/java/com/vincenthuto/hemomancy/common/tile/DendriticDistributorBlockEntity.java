package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DendriticDistributorBlockEntity extends BlockEntity  {

	public DendriticDistributorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.dendritic_distributor.get(), pos, state);
	}

	public void tick() {
		if (level.isClientSide) {
			int globalPartCount = 128;
			double time = -level.getGameTime() * 0.21;
			Vec3[] fibboSphere = HLParticleUtils.randomSphere(globalPartCount, time, 0.5);
			double sizeMod = 2;
			double sinX = Math.abs(Math.pow(Math.sin(time), 9)) * sizeMod;
			double sinZ = Math.cos(time) * sizeMod;

			for (int i = 0; i < globalPartCount; i++) {
				level.addParticle(
						GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
								(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
						worldPosition.getX() + .5 + sinZ, worldPosition.getY() + 1 + sinX, worldPosition.getZ() + .5, 0,
						0, 0);

				level.addParticle(
						GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
								(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
						worldPosition.getX() + .5, worldPosition.getY() + 1 + sinX, worldPosition.getZ() + .5 + sinZ, 0,
						0, 0);

			}
		}

	}
}