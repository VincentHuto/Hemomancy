package com.vincenthuto.hemomancy.common.item.tool;

import java.util.List;
import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.BloodGourdItemRenderer;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.ScarType;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.EnumBloodGourdTiers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;
import net.minecraft.core.Direction;

public class BloodGourdItem extends Item implements IScar {

	public static String TAG_STATE = "state";
	EnumBloodGourdTiers tier;

	public BloodGourdItem(Properties prop, EnumBloodGourdTiers tierIn) {
		super(prop);
		this.tier = tierIn;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		boolean bloodPresent = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA).isPresent();
		if (bloodPresent) {
			IBloodVolume bloodVolume = stack.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(NullPointerException::new);
			tooltip.add(Component.literal("Max Blood Volume: " + tier.getMaxVolume())
					.withStyle(ChatFormatting.GOLD));
			if (stack.hasTag()) {
				tooltip.add(Component.literal("Blood Volume: " + bloodVolume.getBloodVolume())
						.withStyle(ChatFormatting.RED));
				if (stack.getTag().getBoolean(TAG_STATE)) {
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
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		super.initializeClient(consumer);
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new BloodGourdItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		});
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return false;
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
	public boolean willAutoSync(LivingEntity player) {
		return true;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		final IScar self = this;
		return new ICapabilityProvider() {
			private final LazyOptional<IScar> opt = LazyOptional.of(() -> self);

			@Nonnull
			@Override
			public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
				return ScarsCapabilities.ITEM_SCAR.orEmpty(cap, opt);
			}
		};
	}

}
