package com.huto.hemomancy.manipulation.quick;

import com.huto.hemomancy.capa.tendency.EnumBloodTendency;
import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.manipulation.BloodManipulation;
import com.huto.hemomancy.manipulation.EnumManipulationRank;
import com.huto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.RegistryObject;

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
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (heldItemMainhand.isEmpty()) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item.get()));
		}
	}
}
