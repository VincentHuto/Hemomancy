package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityMortalDisplay extends BlockEntity {

	public BlockEntityMortalDisplay(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mortal_display.get(), pos, state);
	}

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		if (level.getBlockEntity(pos) instanceof BlockEntityMortalDisplay te) {
			Vector3 centerVec = Vector3.fromBlockEntityCenter(te).add(0, 0.125, 0);
			if (level.isClientSide) {

				double time = level.getGameTime();
				level.addParticle(GlowParticleFactory.createData(new ParticleColor(50, 50, 50)),
						centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
						centerVec.y + Math.sin(time * 0.1) * 0.55f,
						centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
				level.addParticle(GlowParticleFactory.createData(new ParticleColor(100, 0, 0)),
						centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
						centerVec.y + Math.sin(time * 0.1) * 0.55f,
						centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
				level.addParticle(GlowParticleFactory.createData(new ParticleColor(255, 0, 0)),
						centerVec.x + Math.sin(time * 0.3) * (0.50 + Math.sin(time) * 0.05),
						centerVec.y + Math.sin(time * 0.1) * 0.55f,
						centerVec.z + Math.cos(time * 0.3) * (0.50 + Math.sin(time) * 0.05), 0, 0f, 0.0f);
				level.addParticle(GlowParticleFactory.createData(new ParticleColor(50, 50, 50)),
						centerVec.x - Math.sin(time * 0.3) * (0.50 + Math.cos(time) * 0.05),
						centerVec.y - Math.sin(time * 0.1) * 0.55f,
						centerVec.z - Math.cos(time * 0.3) * (0.50 + Math.cos(time) * 0.05), 0, 0f, 0.0f);
				level.addParticle(GlowParticleFactory.createData(new ParticleColor(100, 100, 0)),
						centerVec.x - Math.sin(time * 0.3) * (0.50 + Math.cos(time) * 0.05),
						centerVec.y - Math.sin(time * 0.1) * 0.55f,
						centerVec.z - Math.cos(time * 0.3) * (0.50 + Math.cos(time) * 0.05), 0, 0f, 0.0f);
				level.addParticle(GlowParticleFactory.createData(new ParticleColor(255, 255, 0)),
						centerVec.x - Math.sin(time * 0.3) * (0.50 + Math.cos(time) * 0.05),
						centerVec.y - Math.sin(time * 0.1) * 0.55f,
						centerVec.z - Math.cos(time * 0.3) * (0.50 + Math.cos(time) * 0.05), 0, 0f, 0.0f);

			}
		}

	}
}