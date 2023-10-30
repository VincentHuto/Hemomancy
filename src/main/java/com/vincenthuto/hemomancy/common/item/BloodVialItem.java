package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class BloodVialItem extends Item {

	public static String TAG_ENTITY_TYPE = "entity_type";
	public static String TAG_STATE = "state";

	public static EntityType<?> getEntityType(ItemStack stack) {
		if (stack.hasTag()) {
			if (stack.getOrCreateTag().get(TAG_ENTITY_TYPE) != null) {
				EntityType<?> type = ForgeRegistries.ENTITY_TYPES
						.getValue(new ResourceLocation(stack.getOrCreateTag().getString(TAG_ENTITY_TYPE)));
				return type;
			}
		}
		return null;
	}

	public BloodVialItem(Properties prop) {
		super(prop.stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		if (stack.hasTag()) {
			tooltip.add(Component.literal(I18n.get(getEntityType(stack).getDescriptionId())).append(" Sample"));
		}
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
			if (entity instanceof LivingEntity living) {
				tag.putString(TAG_ENTITY_TYPE, ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString());
				tag.putBoolean(TAG_STATE, true);
			}
		} else {
			tag.putBoolean(TAG_STATE, false);
		}
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
		ItemStack curr = pPlayer.getItemInHand(pUsedHand);
		if (curr.getOrCreateTag().get(TAG_ENTITY_TYPE) != null) {
			System.out.println(getEntityType(curr));
		}
		return super.use(pLevel, pPlayer, pUsedHand);
	}

}