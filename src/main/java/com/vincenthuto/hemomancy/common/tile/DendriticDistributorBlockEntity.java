package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DendriticDistributorBlockEntity extends BlockEntity {

	public DendriticDistributorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.dendritic_distributor.get(), pos, state);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, DendriticDistributorBlockEntity te) {
		double globalTime = 0.02;
		int globalPartCount = 128;
		Vec3[] fibboSphere = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5);
		Vec3[] corona = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.55);
		Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5,
				false);
		Vec3[] earth = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.1);
		Vec3[] mars = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.08);

		Vec3[] randomSwim = HLParticleUtils.randomSwimming(globalPartCount, -level.getGameTime() * 0.005, 1, false);

		for (int i = 0; i < globalPartCount; i++) {

//			level.addParticle(
//					GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
//							(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
//					pos.getX() + fibboSphere[i].x + .5, pos.getY() + 1.5 + fibboSphere[i].y,
//					pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);

//			level.addParticle(
//					GlowParticleFactory.createData(new ParticleColor((int) (inversedSphere[i].x * 255),
//							(int) (inversedSphere[i].y * 255), (int) (inversedSphere[i].z * 255))),
//					pos.getX() + inversedSphere[i].x + .5, pos.getY() + 1.5 + inversedSphere[i].y,
//					pos.getZ() + inversedSphere[i].z + .5, 0, 0.00, 0);

//			level.addParticle(
//					GlowParticleFactory.createData(new ParticleColor((int) (randomSwim[i].x * 255),
//							(int) (randomSwim[i].y * 255), (int) (randomSwim[i].z * 255))),
//					pos.getX() + randomSwim[i].x + .5, pos.getY() + 1.1 + randomSwim[i].y,
//					pos.getZ() + randomSwim[i].z + .5, 0, 0.00, 0);

//			// This creates a Star like effect
//			level.addParticle(GlowParticleFactory.createData(new ParticleColor(255, (level.random.nextInt()), 0)),
//					pos.getX() + fibboSphere[i].x + .5, pos.getY() + 1.5 + fibboSphere[i].y,
//					pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);
//
//			if (i % 2 == 0) {
//				level.addParticle(GlowParticleFactory.createData(new ParticleColor(100, 80, 10)),
//						pos.getX() + corona[i].x + .5, pos.getY() + 1.5 + corona[i].y, pos.getZ() + corona[i].z + .5,
//						0.0, -0.00, 0.0);
//			}
//			level.addParticle(GlowParticleFactory.createData(new ParticleColor(255, 0, 0)),
//					pos.getX() + inversedSphere[i].x + .5, pos.getY() + 1.5 + inversedSphere[i].y,
//					pos.getZ() + inversedSphere[i].z + .5, 0, 0.00, 0);

		}
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			DendriticDistributorBlockEntity te) {

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