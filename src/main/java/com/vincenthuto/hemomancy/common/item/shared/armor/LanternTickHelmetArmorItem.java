package com.vincenthuto.hemomancy.common.item.shared.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LanternTickHelmetArmorItem extends ArmorItem {
	public LanternTickHelmetArmorItem(Holder<ArmorMaterial> materialIn, ArmorItem.Type slot) {
		super(materialIn, slot, new Item.Properties().stacksTo(1));
	}

	public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
			EquipmentSlot slot, String type) {
		return "hemomancy:textures/models/armor/empty.png";
	}

}
