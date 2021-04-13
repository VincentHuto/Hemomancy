package com.huto.hemomancy.manipulation;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.init.PotionInit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ManipBloodRush extends BloodManipulation {

	public ManipBloodRush(String name, double cost, double alignLevel, EnumManipulationRank rank,
			EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, rank, tendency, section);
	}
	@Override
	public void getAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		player.addPotionEffect(new EffectInstance(PotionInit.blood_rush.get(), 250, 1));
	}

}
