package com.vincenthuto.hemomancy.item;

import java.util.List;
import java.util.Random;

import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class RecycledEnzymeItem extends Item {

	public RecycledEnzymeItem() {
		super(new Item.Properties().tab(HemomancyItemGroup.instance));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		if (!worldIn.isClientSide) {
			playerIn.displayClientMessage(Component.literal(String.valueOf(getTend())), false);
			playerIn.displayClientMessage(Component.literal(String.valueOf(getAmount())), false);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	public EnumBloodTendency getTend() {
		Random rand = new Random();
		int val = rand.nextInt(EnumBloodTendency.values().length);
		return EnumBloodTendency.values()[val];
	}

	public float getAmount() {
		Random rand = new Random();
		float fl = rand.nextFloat();
		int in = rand.nextInt(15);
		return fl * in;
	}

}