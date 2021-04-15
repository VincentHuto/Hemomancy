package com.huto.hemomancy.manipulation.continuous;

import java.util.Random;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.huto.hemomancy.particle.factory.GlowParticleFactory;
import com.huto.hemomancy.particle.util.ParticleColor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ManipBloodAneurysm extends BloodManipulation {

	public ManipBloodAneurysm(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}
	@Override
	public void performAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		ServerWorld sWorld = (ServerWorld) world;
		BlockPos pos = player.getPosition();
		Random random = player.world.rand;
		for (int i = 0; i < 30; i++) {
			sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
					pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
					pos.getZ() + random.nextDouble(), 3, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
		}
	}

}
