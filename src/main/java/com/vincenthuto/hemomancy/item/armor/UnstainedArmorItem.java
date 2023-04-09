package com.vincenthuto.hemomancy.item.armor;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.model.armor.UnstainedArmorModel;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class UnstainedArmorItem extends ArmorItem {

	public UnstainedArmorItem(ArmorMaterial materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().fireResistant());
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,

					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.unstained_helm.get()) {
					return UnstainedArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.unstained_chestplate.get()) {
					return UnstainedArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.unstained_leggings.get()) {
					return UnstainedArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.unstained_boots.get()) {
					return UnstainedArmorModel.boots.get();
				}
				return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
			}
		});
	}
}