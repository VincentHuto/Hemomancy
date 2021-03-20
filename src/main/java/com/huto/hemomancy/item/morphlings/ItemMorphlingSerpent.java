package com.huto.hemomancy.item.morphlings;

import com.huto.hemomancy.entity.projectile.EntityTrackingSerpent;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMorphlingSerpent extends ItemMorphling implements IMorphling {

	public ItemMorphlingSerpent(Properties prop) {
		super(prop);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItem(handIn);
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public int getBloodCost() {
		return 20;
	}

	@Override
	public void use(PlayerEntity playerIn, Hand handIn, ItemStack itemStack, World worldIn) {
		/*
		 * EntityTrackingSerpent miss = new EntityTrackingSerpent((PlayerEntity)
		 * playerIn, false); miss.setPosition(playerIn.getPosX() - 0.5,
		 * playerIn.getPosY() + 0.6, playerIn.getPosZ() - 0.5); worldIn.addEntity(miss);
		 */
		EntityTrackingSerpent[] arr = new EntityTrackingSerpent[5];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new EntityTrackingSerpent((PlayerEntity) playerIn, false);
			arr[i].setPosition(playerIn.getPosX() - 0.5, playerIn.getPosY() + 0.6, playerIn.getPosZ() - 0.5);
			worldIn.addEntity(arr[i]);
		}

	}

}
