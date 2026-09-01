package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FungalPodiumBlockEntity extends BlockEntity {

	public static void animTick(Level level, BlockPos pos, BlockState state, FungalPodiumBlockEntity ent) {
		if (level.isClientSide) {
			int globalPartCount = 128;
			Vec3[] fibboSphere = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5);
			Vec3[] corona = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.55);
			Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5,
					false);
			Vec3[] earth = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.1);
			Vec3[] mars = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.08);
			Vec3[] randomSwim = HLParticleUtils.randomSwimming(globalPartCount, -level.getGameTime() * 0.005, 1, false);

			RandomSource random = level.random;

			// Only emit particles every other tick to reduce client load
			if (level.getGameTime() % 2 == 0) {
				for (int i = 0; i < globalPartCount / 4; i++) {
					// Outer pulsing sphere — deep crimson
					level.addParticle(
							GlowParticleFactory.createData(new ParticleColor(
									180 + random.nextInt(40),
									0,
									random.nextInt(20))),
							pos.getX() + fibboSphere[i].x + 0.5,
							pos.getY() + 1.5 + fibboSphere[i].y,
							pos.getZ() + fibboSphere[i].z + 0.5,
							0, 0.0, 0);

					// Corona — pale gold mycelium glow
					if (i % 2 == 0) {
						level.addParticle(
								GlowParticleFactory.createData(new ParticleColor(
										220 + random.nextInt(35),
										180 + random.nextInt(40),
										80 + random.nextInt(30))),
								pos.getX() + corona[i].x + 0.5,
								pos.getY() + 1.5 + corona[i].y,
								pos.getZ() + corona[i].z + 0.5,
								0.0, -0.002, 0.0);
					}

					// Inner inverse sphere — blood-dark core
					level.addParticle(
							GlowParticleFactory.createData(new ParticleColor(
									100 + random.nextInt(60),
									0,
									random.nextInt(10))),
							pos.getX() + inversedSphere[i].x + 0.5,
							pos.getY() + 1.5 + inversedSphere[i].y,
							pos.getZ() + inversedSphere[i].z + 0.5,
							0, 0.0, 0);
				}
			}
		}
	}

	public FungalPodiumBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.fungal_podium.get(), pos, state);
	}
}
