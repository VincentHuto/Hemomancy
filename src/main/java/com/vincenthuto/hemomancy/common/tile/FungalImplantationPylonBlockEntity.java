package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FungalImplantationPylonBlockEntity extends BlockEntity {

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state,
			FungalImplantationPylonBlockEntity te) {

	}

	@SuppressWarnings("unused")
	public static void clientTick(Level level, BlockPos pos, BlockState state, FungalImplantationPylonBlockEntity ent) {
		if (level.isClientSide) {
			int globalPartCount = 128;
			Vec3[] tangentFunnel = HLParticleUtils.tangentFunnel(globalPartCount, -level.getGameTime() * 0.01, 0.5,
					false);
			for (int i = 0; i < globalPartCount; i++) {
				level.addParticle(
						GlowParticleFactory.createData(ParticleColor.YELLOW),
						pos.getX() + tangentFunnel[i].x + .5, pos.getY() + 2.75 + tangentFunnel[i].y,
						pos.getZ() + tangentFunnel[i].z + .5, 0, 0.001f, 0);
				level.addParticle(
						GlowParticleFactory.createData(ParticleColor.YELLOW),
						pos.getX() + tangentFunnel[i].x + .5, pos.getY() + 2.75  + tangentFunnel[i].y,
						pos.getZ() + tangentFunnel[i].z + .5, 0, -0.001f, 0);
				level.addParticle(
						GlowParticleFactory.createData(ParticleColor.WHITE),
						pos.getX() + tangentFunnel[i].x + .5, pos.getY() + 2.75  + tangentFunnel[i].y,
						pos.getZ() + tangentFunnel[i].z + .5, 0, 0.00, 0);
			
			
			}

		}
	}

	public FungalImplantationPylonBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.fungal_implantation_pylon.get(), pos, state);
	}
}
