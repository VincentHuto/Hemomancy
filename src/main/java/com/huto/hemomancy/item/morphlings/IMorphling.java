package com.huto.hemomancy.item.morphlings;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface IMorphling {

//	int getTier();

	int getBloodCost();

//	int getAllegianceChance();

	// public boolean canUseModule(int rarity);

	public void use(PlayerEntity playerIn, Hand handIn, ItemStack itemStack, World worldIn);

}
