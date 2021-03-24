package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.particle.data.DarkGlowParticleData;
import com.huto.hemomancy.particle.util.ParticleUtil;
import com.huto.hemomancy.util.Vector3;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityHumaneIdol extends TileEntity implements ITickableTileEntity {

	public TileEntityHumaneIdol() {
		super(TileEntityInit.humane_idol.get());
	}

	@Override
	public void tick() {
		Vector3 centerVec = Vector3.fromTileEntityCenter(this).add(0, 0, 0);
		double time = world.getGameTime();
		double timeOsc = Math.cos(world.getGameTime())*0.5;
		double timeOsc1 = Math.sin(world.getGameTime())*0.5;
		
		if (world.isRemote) {
			
			int globalPartCount = 90;
			double cos = Math.cos(time);
			double sin = Math.sin(time);
			for (int i = 0; i < globalPartCount; i++) {
				world.addParticle(DarkGlowParticleData.createData(ParticleUtil.BlACK), centerVec.x+cos-timeOsc1, centerVec.y+1,
						centerVec.z-sin-timeOsc, 0, 0.00, 0);
			}
		}
	}
}
