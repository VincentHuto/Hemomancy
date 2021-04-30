package com.huto.hemomancy.item;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ItemBloodVial extends Item {

	LivingEntity entity;
	String TAG_ENTITY_TYPE = "entity_type";
	public static String TAG_STATE = "state";

	public ItemBloodVial(Properties prop) {
		super(prop.maxStackSize(1));
	}

	@Override
	public ActionResultType itemInteractionForEntity(ItemStack stack, PlayerEntity playerIn, LivingEntity target,
			Hand hand) {
		return super.itemInteractionForEntity(stack, playerIn, target, hand);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		CompoundNBT tag = stack.getOrCreateTag();
		if (entity != null) {
			tag.putString(TAG_ENTITY_TYPE, entity.getType().getTranslationKey());
			tag.putBoolean(TAG_STATE, true);
		} else {
			tag.putBoolean(TAG_STATE, false);

		}
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			tooltip.add(new StringTextComponent(I18n.format(stack.getOrCreateTag().getString(TAG_ENTITY_TYPE))));
		}
	}

}