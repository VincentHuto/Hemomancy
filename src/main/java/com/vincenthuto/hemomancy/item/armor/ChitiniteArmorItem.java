package com.vincenthuto.hemomancy.item.armor;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.model.armor.ChitiniteArmorModel;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;

public class ChitiniteArmorItem extends ArmorItem {

	public ChitiniteArmorItem(ArmorMaterial materialIn, EquipmentSlot slot) {
		super(materialIn, slot, new Item.Properties().tab(HemomancyItemGroup.instance).fireResistant());
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new IItemRenderProperties() {
			@Override
			public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> _default) {
				if (itemStack.getItem() == ItemInit.chitinite_helm.get()) {
					return ChitiniteArmorModel.helmet.get();
				} else if (itemStack.getItem() == ItemInit.chitinite_chestplate.get()) {
					return ChitiniteArmorModel.chest.get();
				} else if (itemStack.getItem() == ItemInit.chitinite_leggings.get()) {
					return ChitiniteArmorModel.legs.get();
				} else if (itemStack.getItem() == ItemInit.chitinite_boots.get()) {
					return ChitiniteArmorModel.boots.get();
				}
				return IItemRenderProperties.super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
			}
		});
	}
}