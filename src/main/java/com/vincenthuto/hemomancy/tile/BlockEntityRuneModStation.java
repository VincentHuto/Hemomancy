package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockEntityRuneModStation extends BlockEntity {

	private static final int SET_COOLDOWN_EVENT = 1;
	private static final int CRAFT_EFFECT_EVENT = 2;
	private static int cooldown = 0;

	public BlockEntityRuneModStation(BlockPos pos, BlockState state) {
		super(BlockEntityInit.rune_mod_station.get(), pos, state);
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		switch (id) {
		case SET_COOLDOWN_EVENT:
			cooldown = type;
			return true;
		case CRAFT_EFFECT_EVENT: {
			if (level.isClientSide) {
				for (int i = 0; i < 25; i++) {
					ParticleOptions data = GlowParticleFactory.createData(new ParticleColor(255, 0, 0));
					level.addParticle(data, worldPosition.getX() + 0.5 + Math.random() * 0.4 - 0.2,
							worldPosition.getY() + 1, worldPosition.getZ() + 0.5 + Math.random() * 0.4 - 0.2, 0, 0, 0);
				}
			}
			return true;
		}
		default:
			return super.triggerEvent(id, type);
		}
	}

	@SuppressWarnings("unused")
	public static void animTick(Level level, BlockPos pos, BlockState state, BlockEntityRuneModStation ent) {
		double globalTime = cooldown * 0.02;
//		if (level.isClientSide) {
//			int globalPartCount = 128;
//			Vec3[] fibboSphere = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5);
//			Vec3[] corona = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.55);
//			Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5,
//					false);
//			Vec3[] earth = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.1);
//			Vec3[] mars = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.08);
//
//			Vec3[] randomSwim = HLParticleUtils.randomSwimming(globalPartCount, -level.getGameTime() * 0.005, 1, false);
//
//			for (int i = 0; i < globalPartCount; i++) {
//
//				level.addParticle(
//						GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
//								(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
//						pos.getX() + fibboSphere[i].x + .5, pos.getY() + 1.5 + fibboSphere[i].y,
//						pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);
//
//				level.addParticle(
//						GlowParticleFactory.createData(new ParticleColor((int) (inversedSphere[i].x * 255),
//								(int) (inversedSphere[i].y * 255), (int) (inversedSphere[i].z * 255))),
//						pos.getX() + inversedSphere[i].x + .5, pos.getY() + 1.5 + inversedSphere[i].y,
//						pos.getZ() + inversedSphere[i].z + .5, 0, 0.00, 0);
//
//				level.addParticle(
//						GlowParticleFactory.createData(new ParticleColor((int) (randomSwim[i].x * 255),
//								(int) (randomSwim[i].y * 255), (int) (randomSwim[i].z * 255))),
//						pos.getX() + randomSwim[i].x + .5, pos.getY() + 1.1 + randomSwim[i].y,
//						pos.getZ() + randomSwim[i].z + .5, 0, 0.00, 0);
//
//				/*
//				 * // This creates a Star like effect
//				 * world.addParticle(GlowParticleData.createData(new ParticleColor(255,
//				 * (world.rand.nextInt()), 0)), pos.getX() + fibboSphere[i].x + .5, pos.getY() +
//				 * 1.5 + fibboSphere[i].y, pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);
//				 * 
//				 * if (i % 2 == 0) { world.addParticle(GlowParticleData.createData(new
//				 * ParticleColor(100, 80, 10)), pos.getX() + corona[i].x + .5, pos.getY() + 1.5
//				 * + corona[i].y, pos.getZ() + corona[i].z + .5, 0.0, -0.00, 0.0); }
//				 * world.addParticle(GlowParticleData.createData(new ParticleColor(255, 0, 0)),
//				 * pos.getX() + inversedSphere[i].x + .5, pos.getY() + 1.5 +
//				 * inversedSphere[i].y, pos.getZ() + inversedSphere[i].z + .5, 0, 0.00, 0);
//				 */
//			}
//		}

		if (cooldown > 0) {
			if (level.isClientSide) {
				int partCount = 128;
				double time = cooldown * 0.02;
				Vec3[] fibboSphere = HLParticleUtils.fibboSphere(partCount, time, Math.tan(time));
				Vec3[] expandingSphere = HLParticleUtils.randomSphere(partCount, time, Math.tan(time * 0.25));
				Vec3[] randomSphere = HLParticleUtils.randomSphere(partCount, time, 1);
				Vec3[] randomSwimming = HLParticleUtils.randomSwimming(partCount, time, 1, false);
				Vec3[] squashStretch = HLParticleUtils.squashAndStretch(partCount, time, 1, false);
				Vec3[] funMovement = HLParticleUtils.tangentFunnel(partCount, time, 1, false);
				Vec3[] inversedSphere = HLParticleUtils.inversedSphere(partCount, time, 1, false);
				Vec3[] lotusFountain = HLParticleUtils.lotusFountain(partCount, time, 1, false);
				Vec3[] lotusFountainFlip = HLParticleUtils.lotusFountainFlip(partCount, time, 1, false);
				Vec3[] cosmicBirthFlip = HLParticleUtils.cosmicBirthFlip(partCount, time, 1, false);
				Vec3[] bloomingFlower = HLParticleUtils.bloomingFlower(partCount, time, 1, false);
				Vec3[] bloomingFlowerFlip = HLParticleUtils.bloomingFlowerFlip(partCount, time, 1, false);
				Vec3[] cosmicBirthInverse = HLParticleUtils.cosmicBirthInverse(partCount, time, 1, false);
				Vec3[] cosmicBirthInverseFlip = HLParticleUtils.cosmicBirthInverseFlip(partCount, time, 1, false);

				Vec3[] selectedParticle = fibboSphere;
				for (int i = 0; i < partCount; i++) {
					level.addParticle(
							GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
									(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
							pos.getX() + fibboSphere[i].x + .5, pos.getY() + 1.25 + fibboSphere[i].y,
							pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);
					level.addParticle(
							GlowParticleFactory.createData(new ParticleColor((int) (expandingSphere[i].x * 255),
									(int) (expandingSphere[i].y * 255), (int) (expandingSphere[i].z * 255))),
							pos.getX() + expandingSphere[i].x + .5, pos.getY() + 1.25 + expandingSphere[i].y,
							pos.getZ() + expandingSphere[i].z + .5, 0, 0.00, 0);

				}

			}
		}

		if (cooldown > 0) {
			cooldown--;
		}
	}

	public void onActivated(Player player, ItemStack stack) {
		if (level.isClientSide) {
			return;
		}
		level.blockEvent(getBlockPos(), BlockInit.rune_mod_station.get(), SET_COOLDOWN_EVENT, 60);
		level.blockEvent(getBlockPos(), BlockInit.rune_mod_station.get(), CRAFT_EFFECT_EVENT, 0);
	}

}
