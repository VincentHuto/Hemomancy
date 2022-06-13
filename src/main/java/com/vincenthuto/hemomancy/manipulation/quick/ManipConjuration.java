package com.vincenthuto.hemomancy.manipulation.quick;

import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.manipulation.EnumManipulationType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

public class ManipConjuration extends BloodManipulation {

	RegistryObject<Item> item;

	public ManipConjuration(String name,RegistryObject<Item> item, double cost, double alignLevel, double xpCost,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, EnumManipulationType.QUICK, rank, tendency, section);
		this.item = item;
	}

	public 	RegistryObject<Item> getItem() {
		return item;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (heldItemMainhand.isEmpty()) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item.get()));
		}
	}
}
