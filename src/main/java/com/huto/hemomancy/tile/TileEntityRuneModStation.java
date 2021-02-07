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
			Vector3d[] fibboSphere = ParticleUtil.pointOnSphere(512, -this.world.getGameTime() * 0.01);
			Vector3d[] randomSwimming = ParticleUtil.randomSwimming(512, -this.world.getGameTime() * 0.01);
			Vector3d[] squashStretch = ParticleUtil.squashAndStretch(512, -this.world.getGameTime() * 0.01);
			Vector3d[] funMovement = ParticleUtil.funMovement(512, -this.world.getGameTime() * 0.01);

			for (int i = 0; i < funMovement.length; i++) {

				world.addParticle(
						GlowParticleData.createData(new ParticleColor((int) (fibboSphere[i].x * 255),
								(int) (fibboSphere[i].y * 255), (int) (fibboSphere[i].z * 255))),
						pos.getX() + fibboSphere[i].x + .5, pos.getY() + 2 + +fibboSphere[i].y,
						pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);

			}
		}

	}
}
