package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleUtil;
import com.huto.hemomancy.particle.data.GlowParticleData;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;

public class TileEntityRuneModStation extends TileEntity implements ITickableTileEntity {

	public TileEntityRuneModStation() {
		super(TileEntityInit.rune_mod_station.get());
	}

	@Override
	public void tick() {
		if (world.isRemote) {
			int partCount = 256;
			double time = -this.world.getGameTime() * 0.01;
			Vector3d[] fibboSphere = ParticleUtil.fibboSphere(partCount, time, 1);
			Vector3d[] expandingSphere = ParticleUtil.randomSphere(partCount, time, Math.sin(time));
			Vector3d[] randomSphere = ParticleUtil.randomSphere(partCount, time, 1);
			Vector3d[] randomSwimming = ParticleUtil.randomSwimming(partCount, time, 1);
			Vector3d[] squashStretch = ParticleUtil.squashAndStretch(partCount, time, 1);
			Vector3d[] funMovement = ParticleUtil.tangentFunnel(partCount, time, 1);
			Vector3d[] inversedSphere = ParticleUtil.inversedSphere(partCount, time, 1);
			Vector3d[] lotusFountain = ParticleUtil.lotusFountain(partCount, time, 1);
			Vector3d[] cosmicBirthFlip = ParticleUtil.cosmicBirthFlip(partCount, time, 1);
			Vector3d[] bloomingFlower = ParticleUtil.bloomingFlower(partCount, time, 1);
			Vector3d[] bloomingFlowerFlip = ParticleUtil.bloomingFlowerFlip(partCount, time, 1);
			Vector3d[] cosmicBirthInverse = ParticleUtil.cosmicBirthInverse(partCount, time, 1);
			Vector3d[] cosmicBirthInverseFlip = ParticleUtil.cosmicBirthInverseFlip(partCount, time, 1);
			for (int i = 0; i < partCount; i++) {
				world.addParticle(
						GlowParticleData.createData(new ParticleColor((int) (lotusFountain[i].x * 255),
								(int) (lotusFountain[i].y * 255), (int) (lotusFountain[i].z * 255))),
						pos.getX() + lotusFountain[i].x + .5, pos.getY() + 2 + lotusFountain[i].y,
						pos.getZ() + lotusFountain[i].z + .5, 0, 0.00, 0);

			}

		}

	}
}
