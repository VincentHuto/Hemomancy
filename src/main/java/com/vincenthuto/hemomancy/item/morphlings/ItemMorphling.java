package com.vincenthuto.hemomancy.item.morphlings;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMorphling extends Item implements IMorphling {

	public ItemMorphling(Properties prop) {
		super(prop);
		prop.stacksTo(1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public int getBloodCost() {
		return 0;
	}

	@Override
	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn) {
	}

}
