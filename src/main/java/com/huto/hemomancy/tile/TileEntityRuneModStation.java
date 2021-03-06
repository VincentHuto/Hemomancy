package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.TileEntityInit;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;
import com.hutoslib.client.particle.factory.GlowParticleFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;

public class TileEntityRuneModStation extends TileEntity implements ITickableTileEntity {

	private static final int SET_COOLDOWN_EVENT = 1;
	private static final int CRAFT_EFFECT_EVENT = 2;
	private int cooldown = 0;

	public TileEntityRuneModStation() {
		super(TileEntityInit.rune_mod_station.get());
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		switch (id) {
		case SET_COOLDOWN_EVENT:
			cooldown = type;
			return true;
		case CRAFT_EFFECT_EVENT: {
			if (world.isRemote) {
				for (int i = 0; i < 25; i++) {
					IParticleData data = GlowParticleFactory.createData(new ParticleColor(255, 0, 0));
					world.addParticle(data, pos.getX() + 0.5 + Math.random() * 0.4 - 0.2, pos.getY() + 1,
							pos.getZ() + 0.5 + Math.random() * 0.4 - 0.2, 0, 0, 0);
				}
			}
			return true;
		}
		default:
			return super.receiveClientEvent(id, type);
		}
	}

	@SuppressWarnings("unused")
	@Override
	public void tick() {
		double globalTime = cooldown * 0.02;
		if (world.isRemote) {
			int globalPartCount = 128;
			Vector3d[] fibboSphere = ParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.5);
			Vector3d[] corona = ParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.55);
			Vector3d[] inversedSphere = ParticleUtils.inversedSphere(globalPartCount, -world.getGameTime() * 0.01, 0.5,
					false);
			Vector3d[] earth = ParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.1);
			Vector3d[] mars = ParticleUtils.randomSphere(globalPartCount, -world.getGameTime() * 0.01, 0.08);

			Vector3d[] randomSwim = ParticleUtils.randomSwimming(globalPartCount, -world.getGameTime() * 0.005, 1,
					false);

			for (int i = 0; i < globalPartCount; i++) {

				world.addParticle(
						GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
								(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
						pos.getX() + fibboSphere[i].x + .5, pos.getY() + 1.5 + fibboSphere[i].y,
						pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);

				world.addParticle(
						GlowParticleFactory.createData(new ParticleColor((int) (inversedSphere[i].x * 255),
								(int) (inversedSphere[i].y * 255), (int) (inversedSphere[i].z * 255))),
						pos.getX() + inversedSphere[i].x + .5, pos.getY() + 1.5 + inversedSphere[i].y,
						pos.getZ() + inversedSphere[i].z + .5, 0, 0.00, 0);

				world.addParticle(
						GlowParticleFactory.createData(new ParticleColor((int) (randomSwim[i].x * 255),
								(int) (randomSwim[i].y * 255), (int) (randomSwim[i].z * 255))),
						pos.getX() + randomSwim[i].x + .5, pos.getY() + 1.1 + randomSwim[i].y,
						pos.getZ() + randomSwim[i].z + .5, 0, 0.00, 0);

				/*
				 * // This creates a Star like effect
				 * world.addParticle(GlowParticleData.createData(new ParticleColor(255,
				 * (world.rand.nextInt()), 0)), pos.getX() + fibboSphere[i].x + .5, pos.getY() +
				 * 1.5 + fibboSphere[i].y, pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);
				 * 
				 * if (i % 2 == 0) { world.addParticle(GlowParticleData.createData(new
				 * ParticleColor(100, 80, 10)), pos.getX() + corona[i].x + .5, pos.getY() + 1.5
				 * + corona[i].y, pos.getZ() + corona[i].z + .5, 0.0, -0.00, 0.0); }
				 * world.addParticle(GlowParticleData.createData(new ParticleColor(255, 0, 0)),
				 * pos.getX() + inversedSphere[i].x + .5, pos.getY() + 1.5 +
				 * inversedSphere[i].y, pos.getZ() + inversedSphere[i].z + .5, 0, 0.00, 0);
				 */
			}
		}

		if (cooldown > 0) {
			if (world.isRemote) {
				int partCount = 128;
				double time = cooldown * 0.02;
				Vector3d[] fibboSphere = ParticleUtils.fibboSphere(partCount, time, Math.tan(time));
				Vector3d[] expandingSphere = ParticleUtils.randomSphere(partCount, time, Math.tan(time * 0.25));
				Vector3d[] randomSphere = ParticleUtils.randomSphere(partCount, time, 1);
				Vector3d[] randomSwimming = ParticleUtils.randomSwimming(partCount, time, 1, false);
				Vector3d[] squashStretch = ParticleUtils.squashAndStretch(partCount, time, 1, false);
				Vector3d[] funMovement = ParticleUtils.tangentFunnel(partCount, time, 1, false);
				Vector3d[] inversedSphere = ParticleUtils.inversedSphere(partCount, time, 1, false);
				Vector3d[] lotusFountain = ParticleUtils.lotusFountain(partCount, time, 1, false);
				Vector3d[] lotusFountainFlip = ParticleUtils.lotusFountainFlip(partCount, time, 1, false);
				Vector3d[] cosmicBirthFlip = ParticleUtils.cosmicBirthFlip(partCount, time, 1, false);
				Vector3d[] bloomingFlower = ParticleUtils.bloomingFlower(partCount, time, 1, false);
				Vector3d[] bloomingFlowerFlip = ParticleUtils.bloomingFlowerFlip(partCount, time, 1, false);
				Vector3d[] cosmicBirthInverse = ParticleUtils.cosmicBirthInverse(partCount, time, 1, false);
				Vector3d[] cosmicBirthInverseFlip = ParticleUtils.cosmicBirthInverseFlip(partCount, time, 1, false);

				Vector3d[] selectedParticle = fibboSphere;
				for (int i = 0; i < partCount; i++) {
					world.addParticle(
							GlowParticleFactory.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
									(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
							pos.getX() + fibboSphere[i].x + .5, pos.getY() + 1.5 + fibboSphere[i].y,
							pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);
					world.addParticle(
							GlowParticleFactory.createData(new ParticleColor((int) (expandingSphere[i].x * 255),
									(int) (expandingSphere[i].y * 255), (int) (expandingSphere[i].z * 255))),
							pos.getX() + expandingSphere[i].x + .5, pos.getY() + 1.5 + expandingSphere[i].y,
							pos.getZ() + expandingSphere[i].z + .5, 0, 0.00, 0);

				}

			}
		}

		if (cooldown > 0) {
			cooldown--;
		}
	}

	public void onActivated(PlayerEntity player, ItemStack stack) {
		if (world.isRemote) {
			return;
		}
		world.addBlockEvent(getPos(), BlockInit.rune_mod_station.get(), SET_COOLDOWN_EVENT, 60);
		world.addBlockEvent(getPos(), BlockInit.rune_mod_station.get(), CRAFT_EFFECT_EVENT, 0);
	}

}
