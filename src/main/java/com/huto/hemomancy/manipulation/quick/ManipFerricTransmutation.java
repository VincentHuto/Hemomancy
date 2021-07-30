package com.huto.hemomancy.manipulation.quick;

import java.util.Random;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.factory.GlowParticleFactory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class ManipFerricTransmutation extends BloodManipulation {

	public ManipFerricTransmutation(String name, double cost, double alignLevel, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		ServerLevel sWorld = (ServerLevel) world;
		BlockPos pos = player.getPosition();
		Random random = player.world.rand;
		if (heldItemMainhand.getItem() == Items.GOLD_INGOT) {
			heldItemMainhand.shrink(1);
			ItemEntity iron = new ItemEntity(sWorld, pos.getX(), pos.getY(), pos.getZ(),
					new ItemStack(Items.IRON_INGOT));
			sWorld.addEntity(iron);
		}
		for (int i = 0; i < 30; i++) {
			sWorld.spawnParticle(GlowParticleFactory.createData(new ParticleColor(255 * random.nextFloat(), 0, 0)),
					pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() + 1,
					pos.getZ() + random.nextDouble(), 3, 0f, 0.2f, 0f, sWorld.rand.nextInt(3) * 0.015f);
		}
	}

}
