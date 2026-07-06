package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.client.model.armor.SheolicBloodLustArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;

public class SheolicBloodLustArmorItem extends AbstractSpecialBloodLustArmorItem {
	public SheolicBloodLustArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, "sheolic_blood_lust");
	}

	@Override
	protected HumanoidModel<?> modelForSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> SheolicBloodLustArmorModel.helmet.get();
			case CHEST -> SheolicBloodLustArmorModel.chest.get();
			case LEGS -> SheolicBloodLustArmorModel.legs.get();
			case FEET -> SheolicBloodLustArmorModel.boots.get();
			default -> SheolicBloodLustArmorModel.chest.get();
		};
	}
}
