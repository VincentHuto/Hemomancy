package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;
import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.mojang.math.Vector3d;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

public class TileEntityDendriticDistributor extends BlockEntity implements TickableBlockEntity {

	public TileEntityDendriticDistributor() {
		super(TileEntityInit.dendritic_distributor.get());
	}

	@Override
	public void tick() {
		if (level.isClientSide) {
			int globalPartCount = 128;
			double time = -world.getGameTime() * 0.21;
			Vector3d[] fibboSphere = ParticleUtils.randomSphere(globalPartCount, time, 0.5);
			double sizeMod = 2;
			double sinX = Math.abs(Math.pow(Math.sin(time), 9)) * sizeMod;
			double sinZ = Math.cos(time) * sizeMod;

			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(
						GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
								(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
						pos.getX() + .5 + sinZ, pos.getY() + 1 + sinX, pos.getZ() + .5, 0, 0, 0);

				world.addParticle(
						GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
								(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
						pos.getX() + .5, pos.getY() + 1 + sinX, pos.getZ() + .5 + sinZ, 0, 0, 0);

			}
		}

	}
}