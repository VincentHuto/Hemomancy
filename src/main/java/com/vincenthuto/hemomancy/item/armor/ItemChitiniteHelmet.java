package com.vincenthuto.hemomancy.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class ItemChitiniteHelmet extends ArmorItem {

	public ItemChitiniteHelmet(ArmorMaterial materialIn, EquipmentSlot slot, Properties builderIn) {
		super(materialIn, slot, builderIn);
	}

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @OnlyIn(Dist.CLIENT)
	 * 
	 * @Override public <A extends HumanoidModel<?>> A getArmorModel(LivingEntity
	 * entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, A _default) { if
	 * (itemStack != ItemStack.EMPTY) { if (itemStack.getItem() instanceof
	 * ArmorItem) { ModelChitiniteHelmet model = new ModelChitiniteHelmet();
	 * model.body.visible = armorSlot == EquipmentSlot.HEAD; model.young =
	 * _default.young; model.crouching = _default.crouching; model.riding =
	 * _default.riding; model.rightArmPose = _default.rightArmPose;
	 * model.leftArmPose = _default.leftArmPose; return (A) model; } } return null;
	 * }
	 */

}