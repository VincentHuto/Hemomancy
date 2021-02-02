package com.huto.hemomancy.item.morphlings;

import com.huto.hemomancy.entity.EntityLeech;
import com.huto.hemomancy.init.EntityInit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMorphlingLeech extends ItemMorphling implements IMorphling {
	public String text;

	public ItemMorphlingLeech(Properties prop, String textIn) {
		super(prop, textIn);
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
		EntityLeech[] arr = new EntityLeech[5];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = new EntityLeech(EntityInit.leech.get(), worldIn);
			arr[i].setPosition(playerIn.getPosX() - 0.5, playerIn.getPosY() + 0.25, playerIn.getPosZ() - 0.5);
			worldIn.addEntity(arr[i]);
		}
	}

}
