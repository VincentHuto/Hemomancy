package com.huto.hemomancy.manipulation.conjuration;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ManipBaseConjuration extends BloodManipulation {

	public ManipBaseConjuration(String name, double cost, double alignLevel, EnumManipulationRank rank,
			EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, EnumManipulationType.QUICK, rank, tendency, section);
	}

	public Item getItem() {
		return Items.AIR;

	}

	@Override
	public void getAction(PlayerEntity player, ServerWorld world, ItemStack heldItemMainhand, BlockPos position) {
		if (heldItemMainhand.isEmpty()) {
			player.setHeldItem(Hand.MAIN_HAND, new ItemStack(getItem()));
		}
	}
}
