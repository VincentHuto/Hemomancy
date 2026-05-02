package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;

import java.util.List;

import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class BloodLustArmorItem extends ArmorItem implements HemoClientItemExtensionsProvider {

	public enum MaskType {
		NONE,
		TENGU,
		HORNED
	}

	MaskType maskType;

	public BloodLustArmorItem(Holder<ArmorMaterial> materialIn, Type slot, MaskType maskType) {
		super(materialIn, slot, new Item.Properties());
		this.maskType = maskType;
	}

	public MaskType getMaskType() {
		return maskType;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.bloodlust_set_bonus").withStyle(ChatFormatting.DARK_RED));
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.blood_lust_helm.get()) {
					return BloodLustArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_horned.get()) {
					return BloodLustArmorModel.horned.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_helm_tengu.get()) {
					return BloodLustArmorModel.tengu.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_chest.get()) {
					return BloodLustArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_legs.get()) {
					return BloodLustArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.blood_lust_boots.get()) {
					return BloodLustArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}
		};
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, world, entity, slotId, isSelected);
		if (world.isClientSide && entity instanceof Player player && isEquipped(player, stack)) {
			world.addParticle(DustParticleOptions.REDSTONE, player.getRandomX(0.5D), player.getY(),
					player.getRandomZ(0.5D), (world.random.nextDouble() - 0.5D) * 2.0D,
					-world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D);
		}
	}

	private static boolean isEquipped(Player player, ItemStack stack) {
		for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
				EquipmentSlot.FEET }) {
			if (ItemStack.isSameItemSameComponents(player.getItemBySlot(slot), stack)) {
				return true;
			}
		}
		return false;
	}

	public void setMaskType(MaskType maskType) {
		this.maskType = maskType;
	}

}
