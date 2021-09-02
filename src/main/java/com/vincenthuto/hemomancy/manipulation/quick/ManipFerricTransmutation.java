package com.vincenthuto.hemomancy.manipulation.quick;

import java.util.Random;

import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hemomancy.capa.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ManipFerricTransmutation extends BloodManipulation {

	public ManipFerricTransmutation(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		ServerLevel sLevel = (ServerLevel) world;
		BlockPos pos = player.blockPosition();
		Random random = player.level.random;
		if (heldItemMainhand.getItem() == Items.GOLD_INGOT) {
			heldItemMainhand.shrink(1);
			ItemEntity iron = new ItemEntity(sLevel, pos.getX(), pos.getY(), pos.getZ(),
					new ItemStack(Items.IRON_INGOT));
			sLevel.addFreshEntity(iron);
		}
		for (int i = 0; i < 30; i++) {
			sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
					pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
					pos.getZ() + random.nextDouble(), 3, 0f, 0.2f, 0f, sLevel.random.nextInt(3) * 0.015f);
		}
	}

}
