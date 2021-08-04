package com.huto.hemomancy.item.tool;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemDrudgeElectrode extends Item {

	public static String TAG_MODE = "mode";

	public ItemDrudgeElectrode(Properties properties) {
		super(properties);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		return true;
//		if (target instanceof EntityDrudge) {
//			if (!target.level.isClientSide) {
//				ParticleUtils.spawnPoof((ServerLevel) target.level, target.blockPosition());
//			}
//			ItemEntity itemEnt = new ItemEntity(target.level, target.getX(), target.getY(), target.getZ(),
//					new ItemStack(ItemInit.living_will.get(), 1));
//			target.level.addFreshEntity(itemEnt);
//			target.remove(RemovalReason.KILLED);
//			return false;
//		} else {
//			return super.hurtEnemy(stack, target, attacker);
//		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		if (stack.getItem() instanceof ItemDrudgeElectrode) {
			CompoundTag compound = stack.getOrCreateTag();
			if (!compound.getBoolean(TAG_MODE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_MODE, !compound.getBoolean(TAG_MODE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_MODE, !compound.getBoolean(TAG_MODE));
			}
			stack.setTag(compound);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(new TextComponent("Used to give and reflect commands to drudges"));
		if (stack.hasTag()) {
			if (stack.getTag().getBoolean(TAG_MODE)) {
				tooltip.add(new TranslatableComponent("State: On").withStyle(ChatFormatting.RED));
			} else {
				tooltip.add(new TranslatableComponent("State: Off").withStyle(ChatFormatting.GRAY));
			}
		}
		super.appendHoverText(stack, worldIn, tooltip, flagIn);

	}
}
