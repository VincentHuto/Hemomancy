package com.vincenthuto.hemomancy.item.armor;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.model.armor.BloodLustArmorModel.EnumBloodLustMaskTypes;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class BloodLustArmorItem extends ArmorItem {

	EnumBloodLustMaskTypes maskType;

	public BloodLustArmorItem(ArmorMaterial materialIn, EquipmentSlot slot, EnumBloodLustMaskTypes maskType) {
		super(materialIn, slot, new Item.Properties());
		this.maskType = maskType;
	}

	public EnumBloodLustMaskTypes getMaskType() {
		return maskType;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
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
		});
	}

	@Override
	public void onArmorTick(ItemStack stack, Level world, Player player) {
		if (world.isClientSide) {
			for (int i = 0; i < 1; ++i) {
				if (i % 2 == 0) {
					world.addParticle(DustParticleOptions.REDSTONE, player.getRandomX(0.5D), player.getY(),
							player.getRandomZ(0.5D), (world.random.nextDouble() - 0.5D) * 2.0D,
							-world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D);
				}
			}
		}

		super.onArmorTick(stack, world, player);
	}

	public void setMaskType(EnumBloodLustMaskTypes maskType) {
		this.maskType = maskType;
	}

}