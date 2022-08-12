package com.vincenthuto.hemomancy.item.tool;

import java.util.List;
import java.util.Random;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.item.EnumBloodGourdTiers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BloodGourdItem extends Item {

	public static String TAG_STATE = "state";
	EnumBloodGourdTiers tier;

	public BloodGourdItem(Properties prop, EnumBloodGourdTiers tierIn) {
		super(prop);
		this.tier = tierIn;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		IBloodVolume bloodVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		if (entityIn instanceof Player) {
			Player player = (Player) entityIn;
			stack.getOrCreateTag();
			if (stack.hasTag()) {

				// Prevent overflow
				if (bloodVolume.getBloodVolume() > tier.getMaxVolume()) {
					bloodVolume.setBloodVolume(tier.getMaxVolume());
				}
				if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
					// Restore player blood
					IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					if (playerVolume.getBloodVolume() < 5000 && bloodVolume.getBloodVolume() > 0) {
						bloodVolume.drain(0.5f);
						playerVolume.fill(0.5f);

					}

				} else {
					// Refill from player
					if (bloodVolume.getBloodVolume() < tier.getMaxVolume() / 10) {
						Random rand = worldIn.random;
						if (rand.nextInt(200) == 20) {
							player.hurt(ItemInit.bloodLoss, 0.5f);
							bloodVolume.fill(50f);
						}
					}
				}
			}

		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getMainHandItem();
		if (stack.getItem() instanceof BloodGourdItem) {
			CompoundTag compound = stack.getOrCreateTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.setTag(compound);
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		boolean bloodPresent = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA).isPresent();
		if (bloodPresent) {
			IBloodVolume bloodVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			tooltip.add(new TranslatableComponent("Max Blood Volume: " + tier.getMaxVolume())
					.withStyle(ChatFormatting.GOLD));

			if (stack.hasTag()) {
				tooltip.add(new TranslatableComponent("Blood Volume: " + bloodVolume.getBloodVolume())
						.withStyle(ChatFormatting.RED));
				if (stack.getTag().getBoolean(TAG_STATE)) {
					tooltip.add(new TranslatableComponent("State: Open").withStyle(ChatFormatting.RED));
				} else {
					tooltip.add(new TranslatableComponent("State: Corked").withStyle(ChatFormatting.GRAY));
				}
			}
		}
	}

	public double getMaxBlood() {
		return tier.getMaxVolume();
	}

}
