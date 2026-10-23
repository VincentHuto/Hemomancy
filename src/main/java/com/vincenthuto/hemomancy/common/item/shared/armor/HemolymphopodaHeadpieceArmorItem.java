package com.vincenthuto.hemomancy.common.item.shared.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HemolymphopodaHeadpieceArmorItem extends ArmorItem {

	public HemolymphopodaHeadpieceArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().stacksTo(1));
	}

	public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity,
			net.minecraft.world.entity.EquipmentSlot slot, String type) {
		return "hemomancy:textures/models/armor/empty.png";
	}

}
