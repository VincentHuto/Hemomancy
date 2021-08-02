package com.huto.hemomancy.item.morphlings;

import com.huto.hemomancy.entity.EntityLeech;
import com.huto.hemomancy.init.EntityInit;

import net.minecraft.util.ActionResultType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMorphlingLeech extends ItemMorphling implements IMorphling {

	public ItemMorphlingLeech(Properties prop) {
		super(prop);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> onItemRightClick(Level worldIn, Player playerIn, InteractionHand handIn) {
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
