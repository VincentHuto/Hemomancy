package com.vincenthuto.hemomancy.item.morphlings;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public int getBloodCost() {
		return 20;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
		/*
		 * EntityLeech[] arr = new EntityLeech[5]; for (int i = 0; i < arr.length; i++)
		 * { arr[i] = new EntityLeech(EntityInit.leech.get(), worldIn);
		 * arr[i].setPos(playerIn.getX() - 0.5, playerIn.getY() + 0.25, playerIn.getZ()
		 * - 0.5); worldIn.addFreshEntity(arr[i]); }
		 */
	}

}
