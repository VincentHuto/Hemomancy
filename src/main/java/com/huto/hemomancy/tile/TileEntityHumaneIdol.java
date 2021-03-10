package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.ParticleUtil;
import com.huto.hemomancy.particle.data.GlowParticleData;
import com.huto.hemomancy.util.Vector3;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;

public class TileEntityHumaneIdol extends TileEntity implements ITickableTileEntity {

	public TileEntityHumaneIdol() {
		super(TileEntityInit.humane_idol.get());
	}

	@Override
	public void tick() {
		Vector3 centerVec = Vector3.fromTileEntityCenter(this).add(0, 0, 0);
		double time = world.getGameTime()*0.01*Math.random();
		if (world.isRemote) {
			int globalPartCount = 128;
			Vector3d[] inversedSphere = ParticleUtil.inversedSphere(globalPartCount, -time, 0.5,
					false);
			for (int i = 0; i < globalPartCount; i++) {

				world.addParticle(
						GlowParticleData
								.createData(
										new ParticleColor(
												(int) Math.max(inversedSphere[i].y * 255,
														Math.max(inversedSphere[i].x * 255, inversedSphere[i].z * 255)),
												0, 0)),
						centerVec.x + inversedSphere[i].x, centerVec.y + inversedSphere[i].y,
						centerVec.z + inversedSphere[i].z, 0, 0.00, 0);
			}
		}
	}
}
