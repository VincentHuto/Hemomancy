package com.vincenthuto.hemomancy.item.armor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class ItemSpikedChestplate extends ArmorItem {

	public ItemSpikedChestplate(ArmorMaterial materialIn, EquipmentSlot slot, Properties builderIn) {
		super(materialIn, slot, builderIn);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		// TODO Auto-generated method stub
		return super.getArmorTexture(stack, entity, slot, type);
	}

	
	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @OnlyIn(Dist.CLIENT)
	 * 
	 * @Override public <A extends HumanoidModel<?>> A getArmorModel(LivingEntity
	 * entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, A _default) { if
	 * (itemStack != ItemStack.EMPTY) { if (itemStack.getItem() instanceof
	 * ArmorItem) { ModelChitiniteChest model = new ModelChitiniteChest();
	 * model.body.visible = armorSlot == EquipmentSlot.CHEST; model.young =
	 * _default.young; model.crouching = _default.crouching; model.riding =
	 * _default.riding; model.rightArmPose = _default.rightArmPose;
	 * model.leftArmPose = _default.leftArmPose; return (A) model; } } return null;
	 * }
	 */

}