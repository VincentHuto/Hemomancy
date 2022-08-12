package com.vincenthuto.hemomancy.item;

import java.util.List;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BloodVialItem extends Item {

	LivingEntity entity;
	String TAG_ENTITY_TYPE = "entity_type";
	public static String TAG_STATE = "state";

	public BloodVialItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target,
			InteractionHand hand) {
		return super.interactLivingEntity(stack, playerIn, target, hand);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		CompoundTag tag = stack.getOrCreateTag();
		if (entity != null) {
			tag.putString(TAG_ENTITY_TYPE, entity.getType().getDescriptionId());
			tag.putBoolean(TAG_STATE, true);
		} else {
			tag.putBoolean(TAG_STATE, false);

		}
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			tooltip.add(new TextComponent(I18n.get(stack.getOrCreateTag().getString(TAG_ENTITY_TYPE))));
		}
	}

}