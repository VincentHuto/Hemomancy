package com.vincenthuto.hemomancy.common.item.tool;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.List;

import com.vincenthuto.hemomancy.client.render.item.BloodGourdItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarType;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.EnumBloodGourdTiers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class BloodGourdItem extends Item implements IScar, HemoClientItemExtensionsProvider {

	public static String TAG_STATE = "state";
	EnumBloodGourdTiers tier;

	public BloodGourdItem(Properties prop, EnumBloodGourdTiers tierIn) {
		super(prop);
		this.tier = tierIn;
	}

	private static CompoundTag getCustomData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		boolean bloodPresent = HemoCapabilityAccess.getBloodVolume(stack).isPresent();
		if (bloodPresent) {
			IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(stack)
					.orElseThrow(NullPointerException::new);
			CompoundTag data = getCustomData(stack);
			tooltip.add(Component.literal("Max Blood Volume: " + tier.getMaxVolume())
					.withStyle(ChatFormatting.GOLD));
			if (!data.isEmpty()) {
				tooltip.add(Component.literal("Blood Volume: " + bloodVolume.getBloodVolume())
						.withStyle(ChatFormatting.RED));
				if (data.getBoolean(TAG_STATE)) {
					tooltip.add(Component.literal("State: Open").withStyle(ChatFormatting.RED));
				} else {
					tooltip.add(Component.literal("State: Corked").withStyle(ChatFormatting.GRAY));
				}
			}
		}
	}

	public double getMaxBlood() {
		return tier.getMaxVolume();
	}

	@Override
	public ScarType getScarType() {
		return ScarType.GOURD;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(stack)
				.orElseThrow(NullPointerException::new);
		CompoundTag data = getCustomData(stack);
		if (entityIn instanceof Player player) {
			if (!data.isEmpty()) {

				// Prevent overflow
				if (bloodVolume.getBloodVolume() > tier.getMaxVolume()) {
					bloodVolume.setBloodVolume(tier.getMaxVolume());
				}
				if (data.getBoolean(TAG_STATE)) {
					// Restore player blood
					IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player)
							.orElseThrow(NullPointerException::new);
					if (playerVolume.getBloodVolume() < 5000 && bloodVolume.getBloodVolume() > 0) {
						bloodVolume.drain(this.tier.getTierLevel()/2f);
						playerVolume.fill(this.tier.getTierLevel()/2f);
					}

				} else {
//					// Refill from player
//					if (bloodVolume.getBloodVolume() < tier.getMaxVolume() / 10) {
//						RandomSource rand = worldIn.random;
//						if (rand.nextInt(200) == 20) {
//							player.hurt(player.damageSources().generic(), 0.5f);
//							bloodVolume.fill(50f);
//						}
//					}
				}
			}

		}
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new BloodGourdItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if (stack.getItem() instanceof BloodGourdItem) {
			CompoundTag compound = getCustomData(stack);
			if (!compound.getBoolean(TAG_STATE)) {
				playerIn.playSound(SoundEvents.BEACON_ACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			} else {
				playerIn.playSound(SoundEvents.BEACON_DEACTIVATE, 0.40f, 1F);
				compound.putBoolean(TAG_STATE, !compound.getBoolean(TAG_STATE));
			}
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public boolean willAutoSync(LivingEntity player) {
		return true;
	}

}
