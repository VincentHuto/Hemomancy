package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.client.model.armor.EdaciousBloodLustArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;

public class EdaciousBloodLustArmorItem extends AbstractSpecialBloodLustArmorItem {
	public EdaciousBloodLustArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, "edacious_blood_lust");
	}

	@Override
	protected HumanoidModel<?> modelForSlot(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> EdaciousBloodLustArmorModel.helmet.get();
			case CHEST -> EdaciousBloodLustArmorModel.chest.get();
			case LEGS -> EdaciousBloodLustArmorModel.legs.get();
			case FEET -> EdaciousBloodLustArmorModel.boots.get();
			default -> EdaciousBloodLustArmorModel.chest.get();
		};
	}

	@Override
	protected void setupSpecialModelAnimations(LivingEntity entityLiving, EquipmentSlot equipmentSlot, Model model,
			float ageInTicks) {
		if (model instanceof EdaciousBloodLustArmorModel<?> edaciousModel) {
			edaciousModel.animateArmorDetails(entityLiving, ageInTicks);
			edaciousModel.setRenderWingsInMainPass(equipmentSlot != EquipmentSlot.CHEST);
		}
	}
}
