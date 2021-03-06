package com.huto.hemomancy.manipulation.quick;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

public class ManipConjuration extends BloodManipulation {

	RegistryObject<Item> item;

	public ManipConjuration(String name, RegistryObject<Item> item, double cost, double alignLevel,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, EnumManipulationType.QUICK, rank, tendency, section);
		this.item = item;
	}
	public RegistryObject<Item> getItem() {
		return item;
	}

	@Override
	public void getAction(PlayerEntity player, World world, ItemStack heldItemMainhand, BlockPos position) {
		if (heldItemMainhand.isEmpty()) {
			player.setHeldItem(Hand.MAIN_HAND, new ItemStack(item.get()));
		}
	}
}
