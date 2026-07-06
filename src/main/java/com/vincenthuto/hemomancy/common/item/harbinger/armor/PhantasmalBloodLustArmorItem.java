package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.client.model.armor.PhantasmalBloodLustArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;

public class PhantasmalBloodLustArmorItem extends AbstractSpecialBloodLustArmorItem {
	public PhantasmalBloodLustArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, "phantasmal_blood_lust");
	}

	@Override
	protected HumanoidModel<?> modelForSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> PhantasmalBloodLustArmorModel.helmet.get();
			case CHEST -> PhantasmalBloodLustArmorModel.chest.get();
			case LEGS -> PhantasmalBloodLustArmorModel.legs.get();
			case FEET -> PhantasmalBloodLustArmorModel.boots.get();
			default -> PhantasmalBloodLustArmorModel.chest.get();
		};
	}
}
