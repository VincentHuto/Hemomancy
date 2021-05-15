package com.huto.hemomancy.item;

import java.util.List;
import java.util.Random;

import com.huto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemRecycledEnzyme extends Item {

	public ItemRecycledEnzyme() {
		super(new Item.Properties().group(HemomancyItemGroup.instance));
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if (!worldIn.isRemote) {
			playerIn.sendStatusMessage(new StringTextComponent(String.valueOf(getTend())), false);
			playerIn.sendStatusMessage(new StringTextComponent(String.valueOf(getAmount())), false);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
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