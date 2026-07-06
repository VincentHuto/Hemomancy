package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.BloodGourdItemRenderer;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentType;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.IHarbingerEquipment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.List;

public class BloodGourdItem extends Item implements IHarbingerEquipment, HemoClientItemExtensionsProvider {

	public static String TAG_STATE = "state";
	EnumBloodGourdTiers tier;

	public BloodGourdItem(Properties prop, EnumBloodGourdTiers tierIn) {
		super(prop);
		this.tier = tierIn;
	}

	private static CompoundTag getCustomData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private static String formatBloodAmount(double amount) {
		double rounded = Math.round(amount * 10.0D) / 10.0D;
		long whole = Math.round(rounded);
		return Math.abs(rounded - whole) < 1.0E-6D ? Long.toString(whole) : Double.toString(rounded);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		HemoCapabilityAccess.getBloodVolume(stack).ifPresent(bloodVolume -> {
			CompoundTag data = getCustomData(stack);
			tooltip.add(Component.literal(tier.getRoleName()).withStyle(ChatFormatting.DARK_RED));
			tooltip.add(Component.literal("Capacity: " + tier.getMaxVolume() + " ml")
					.withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.literal("Flow Rate: " + tier.getTransferRate() + " ml/tick")
					.withStyle(ChatFormatting.RED));
			tooltip.add(Component.literal("Kill Siphon: " + Math.round(tier.getKillSiphonMultiplier() * 100) + "%")
					.withStyle(ChatFormatting.RED));
			if (tier.getPassiveGenerationRate() > 0) {
				tooltip.add(Component.literal("Marrow Growth: " + tier.getPassiveGenerationRate() + " ml/tick")
						.withStyle(ChatFormatting.DARK_RED));
			}
			tooltip.add(Component.literal("Proper vessel: no Blood Drunkenness")
					.withStyle(ChatFormatting.GRAY));
			if (!data.isEmpty()) {
				tooltip.add(Component.literal("Blood Volume: " + formatBloodAmount(bloodVolume.getBloodVolume()) + " ml")
						.withStyle(ChatFormatting.RED));
				if (data.getBoolean(TAG_STATE)) {
					tooltip.add(Component.literal("State: Open").withStyle(ChatFormatting.RED));
				} else {
					tooltip.add(Component.literal("State: Corked").withStyle(ChatFormatting.GRAY));
				}
			}
		});
	}

	public double getMaxBlood() {
		return tier.getMaxVolume();
	}

	public double getKillSiphonMultiplier() {
		return tier.getKillSiphonMultiplier();
	}

	public double getTransferRate() {
		return tier.getTransferRate();
	}

	@Override
	public HarbingerEquipmentType getHarbingerEquipmentType() {
		return HarbingerEquipmentType.GOURD;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if (worldIn.isClientSide) {
			return;
		}
		if (entityIn instanceof Player player) {
			tickBloodTransfer(stack, player);
		}
	}

	@Override
	public void onWornTick(LivingEntity entity) {
		if (entity.level().isClientSide) {
			return;
		}
		if (entity instanceof Player player) {
			HemoCapabilityAccess.getEquipment(player).ifPresent(equipment -> {
				for (int slot = 0; slot < equipment.getSlots(); slot++) {
					ItemStack stack = equipment.getStackInSlot(slot);
					if (stack.getItem() == this) {
						tickBloodTransfer(stack, player);
					}
				}
			});
		}
	}

	private void tickBloodTransfer(ItemStack stack, Player player) {
		IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(stack).orElse(null);
		if (bloodVolume == null) {
			return;
		}
		CompoundTag data = getCustomData(stack);
		if (data.isEmpty()) {
			return;
		}

		// Prevent overflow
		if (bloodVolume.getBloodVolume() > tier.getMaxVolume()) {
			bloodVolume.setBloodVolume(tier.getMaxVolume());
		}
		if (tier.getPassiveGenerationRate() > 0 && bloodVolume.getBloodVolume() < tier.getMaxVolume()) {
			bloodVolume.fill(tier.getPassiveGenerationRate());
		}
		if (data.getBoolean(TAG_STATE)) {
			// Restore player blood
			IBloodVolume playerVolume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
			if (playerVolume == null) {
				return;
			}
			if (playerVolume.getBloodVolume() < playerVolume.getMaxBloodVolume() && bloodVolume.getBloodVolume() > 0) {
				double transfer = Math.min(this.tier.getTransferRate(), bloodVolume.getBloodVolume());
				transfer = Math.min(transfer, playerVolume.getMaxBloodVolume() - playerVolume.getBloodVolume());
				if (player instanceof ServerPlayer serverPlayer) {
					BloodFlowLedger.transferFromVessel(serverPlayer, playerVolume, bloodVolume,
							"open_blood_gourd", "Open Blood Gourd", transfer, 1);
				}
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
