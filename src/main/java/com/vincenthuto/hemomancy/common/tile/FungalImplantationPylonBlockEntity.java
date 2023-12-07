package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FungalImplantationPylonBlockEntity extends BlockEntity {

	@SuppressWarnings("unused")
	public static void animTick(Level level, BlockPos pos, BlockState state, FungalImplantationPylonBlockEntity ent) {
		if (level.isClientSide) {
			int globalPartCount = 128;
			Vec3[] fibboSphere = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5);
			Vec3[] corona = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.55);
			Vec3[] inversedSphere = HLParticleUtils.inversedSphere(globalPartCount, -level.getGameTime() * 0.01, 0.5,
					false);
			Vec3[] earth = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.1);
			Vec3[] mars = HLParticleUtils.randomSphere(globalPartCount, -level.getGameTime() * 0.01, 0.08);
			Vec3[] randomSwim = HLParticleUtils.randomSwimming(globalPartCount, -level.getGameTime() * 0.005, 1, false);

//			for (int i = 0; i < globalPartCount; i++) {
//				// This creates a Star like effect
//				level.addParticle(GlowParticleFactory.createData(new ParticleColor(255, (level.random.nextInt()), 0)),
//						pos.getX() + fibboSphere[i].x + .5, pos.getY() + 1.5 + fibboSphere[i].y,
//						pos.getZ() + fibboSphere[i].z + .5, 0, 0.00, 0);
//
//				if (i % 2 == 0) {
//					level.addParticle(GlowParticleFactory.createData(new ParticleColor(100, 80, 10)),
//							pos.getX() + corona[i].x + .5, pos.getY() + 1.5 + corona[i].y,
//							pos.getZ() + corona[i].z + .5, 0.0, -0.00, 0.0);
//				}
//				level.addParticle(GlowParticleFactory.createData(new ParticleColor(255, 0, 0)),
//						pos.getX() + inversedSphere[i].x + .5, pos.getY() + 1.5 + inversedSphere[i].y,
//						pos.getZ() + inversedSphere[i].z + .5, 0, 0.00, 0);
//			}
		}
	}

	public FungalImplantationPylonBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.fungal_implantation_pylon.get(), pos, state);
	}
}
