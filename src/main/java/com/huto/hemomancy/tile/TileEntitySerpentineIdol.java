package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.particle.factory.SerpentParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.math.Vector3;

import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.server.ServerWorld;

public class TileEntitySerpentineIdol extends BlockEntity implements TickableBlockEntity {

	public TileEntitySerpentineIdol() {
		super(TileEntityInit.serpentine_idol.get());
	}

	@Override
	public void tick() {
		Vector3 centerVec = Vector3.fromTileEntityCenter(this).add(0, 0, 0);
		double time = world.getGameTime();
		if (!this.world.isRemote) {
			ServerWorld sWorld = (ServerWorld) world;
			sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(50, 50, 50)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 2, 0f, 0.0f, 0f, 0);
			sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(100, 0, 0)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 4, 0f, 0.0f, 0f, 0.0005f);
			sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 8, 0f, 0.0f, 0f, 0.0015f);
			sWorld.spawnParticle(SerpentParticleFactory.createData(new ParticleColor(255, 0, 0)),
					centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
					centerVec.y + Math.sin(time * 0.1) * 0.25f,
					centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 1, 0f, 0.0f, 0f, 0.0035f);

		}
	}
}
