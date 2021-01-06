package com.huto.hemomancy.item.tool;

import java.util.List;
import java.util.Random;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class ItemBloodGourd extends Item {

	public static boolean state;
	public static String TAG_STATE = "state";
	public float maxBlood;
	public float currentBlood;

	public ItemBloodGourd(Properties prop, float maxBloodIn) {
		super(prop);
		maxBlood = maxBloodIn;

	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean stateIn) {
		state = stateIn;
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		IBloodVolume bloodVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.orElseThrow(NullPointerException::new);
		if (entityIn instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entityIn;
			stack.getOrCreateTag();
			if (stack.hasTag()) {

				// Prevent overflow
				if (bloodVolume.getBloodVolume() > maxBlood) {
					bloodVolume.setBloodVolume(maxBlood);
				}
				if (stack.getOrCreateTag().getBoolean(TAG_STATE)) {
					// Restore player blood
					IBloodVolume playerVolume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
							.orElseThrow(NullPointerException::new);
					if (playerVolume.getBloodVolume() < 5000) {
						bloodVolume.subtractBloodVolume(0.5f);
						playerVolume.addBloodVolume(0.5f);

					}

				} else {
					// Refill from player
					if (bloodVolume.getBloodVolume() < maxBlood) {
						Random rand = worldIn.rand;
						if (rand.nextInt(200) == 20) {
							player.attackEntityFrom(DamageSource.GENERIC, 0.5f);
							bloodVolume.addBloodVolume(50f);
						}
					}
				}
			}

		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getHeldItemMainhand();
		if (stack.getItem() instanceof ItemBloodGourd) {
			CompoundNBT compound = stack.getOrCreateTag();
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.setTag(compound);
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		boolean bloodPresent = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA).isPresent();
		if (bloodPresent) {
			IBloodVolume bloodVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			tooltip.add(new TranslationTextComponent("Max Blood Volume: " + maxBlood).mergeStyle(TextFormatting.GOLD));

			if (stack.hasTag()) {
				tooltip.add(new TranslationTextComponent("Blood Volume: " + bloodVolume.getBloodVolume())
						.mergeStyle(TextFormatting.RED));
				if (stack.getTag().getBoolean(TAG_STATE)) {
					tooltip.add(new TranslationTextComponent("State: Open").mergeStyle(TextFormatting.RED));
				} else {
					tooltip.add(new TranslationTextComponent("State: Corked").mergeStyle(TextFormatting.GRAY));
				}
			}
		}
	}

	public float getMaxBlood() {
		return maxBlood;
	}

	public float getCurrentBlood() {
		return currentBlood;
	}

}
