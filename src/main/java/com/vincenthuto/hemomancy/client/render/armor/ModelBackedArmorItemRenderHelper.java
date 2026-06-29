package com.vincenthuto.hemomancy.client.render.armor;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.armor.BarbedArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.BloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.ChalybeateFortressArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.ChitiniteArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.CovenantLeaderArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.PrismaticArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.UnstainedArmorModel;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class ModelBackedArmorItemRenderHelper {
	public static final ResourceLocation BLOOD_LUST_LAYER_1 = armorTexture("blood_lust_layer_1");
	public static final ResourceLocation BLOOD_LUST_LAYER_2 = armorTexture("blood_lust_layer_2");
	public static final ResourceLocation BARBED_LAYER_1 = armorTexture("barbed_layer_1");
	public static final ResourceLocation BARBED_LAYER_2 = armorTexture("barbed_layer_2");
	public static final ResourceLocation CHITINITE_LAYER_1 = armorTexture("chitinite_layer_1");
	public static final ResourceLocation CHITINITE_LAYER_2 = armorTexture("chitinite_layer_2");
	public static final ResourceLocation PRISMATIC_LAYER_1 = armorTexture("prismatic_layer_1");
	public static final ResourceLocation PRISMATIC_LAYER_2 = armorTexture("prismatic_layer_2");
	public static final ResourceLocation UNSTAINED_LAYER_1 = armorTexture("unstained_layer_1");
	public static final ResourceLocation UNSTAINED_LAYER_2 = armorTexture("unstained_layer_2");
	public static final ResourceLocation VENOUS_STRIDER_LAYER_1 = armorTexture("venous_strider_layer_1");
	public static final ResourceLocation COVENANT_MANTLE_LAYER_1 = armorTexture("covenant_mantle_layer_1");

	private ModelBackedArmorItemRenderHelper() {
	}

	public static ArmorItemRenderDefinition definitionFor(ItemStack stack) {
		if (stack.is(ItemInit.blood_lust_helm.get()) || stack.is(ItemInit.blood_lust_helm_lodestone.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, BloodLustArmorModel.helmet::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.blood_lust_helm_grinning.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, BloodLustArmorModel.grinning::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.blood_lust_helm_tengu.get())
				|| stack.is(ItemInit.blood_lust_helm_velorum.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, BloodLustArmorModel.tengu::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.edacious_blood_lust_helm.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, BloodLustArmorModel.tengu::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.sheolic_blood_lust_helm.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, BloodLustArmorModel.helmet::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.phantasmal_blood_lust_helm.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, BloodLustArmorModel.tengu::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.blood_lust_chest.get())
				|| stack.is(ItemInit.edacious_blood_lust_chest.get())
				|| stack.is(ItemInit.sheolic_blood_lust_chest.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_chest.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.CHEST, BloodLustArmorModel.chest::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.blood_lust_legs.get())
				|| stack.is(ItemInit.edacious_blood_lust_legs.get())
				|| stack.is(ItemInit.sheolic_blood_lust_legs.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_legs.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.LEGS, BloodLustArmorModel.legs::get,
					BLOOD_LUST_LAYER_2);
		} else if (stack.is(ItemInit.blood_lust_boots.get())
				|| stack.is(ItemInit.edacious_blood_lust_boots.get())
				|| stack.is(ItemInit.sheolic_blood_lust_boots.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_boots.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.FEET, BloodLustArmorModel.boots::get,
					BLOOD_LUST_LAYER_1);
		} else if (stack.is(ItemInit.barbed_helm.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, BarbedArmorModel.helmet::get,
					BARBED_LAYER_1);
		} else if (stack.is(ItemInit.barbed_chestplate.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.CHEST, BarbedArmorModel.chest::get,
					BARBED_LAYER_1);
		} else if (stack.is(ItemInit.barbed_leggings.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.LEGS, BarbedArmorModel.legs::get,
					BARBED_LAYER_2);
		} else if (stack.is(ItemInit.barbed_boots.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.FEET, BarbedArmorModel.boots::get,
					BARBED_LAYER_1);
		} else if (stack.is(ItemInit.chitinite_helm.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, ChitiniteArmorModel.helmet::get,
					CHITINITE_LAYER_1);
		} else if (stack.is(ItemInit.chitinite_chestplate.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.CHEST, ChitiniteArmorModel.chest::get,
					CHITINITE_LAYER_1);
		} else if (stack.is(ItemInit.chitinite_leggings.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.LEGS, ChitiniteArmorModel.legs::get,
					CHITINITE_LAYER_2);
		} else if (stack.is(ItemInit.chitinite_boots.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.FEET, ChitiniteArmorModel.boots::get,
					CHITINITE_LAYER_1);
		} else if (stack.is(ItemInit.prismatic_helm.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, PrismaticArmorModel.helmet::get,
					PRISMATIC_LAYER_1);
		} else if (stack.is(ItemInit.prismatic_chestplate.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.CHEST, PrismaticArmorModel.chest::get,
					PRISMATIC_LAYER_1);
		} else if (stack.is(ItemInit.prismatic_leggings.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.LEGS, PrismaticArmorModel.legs::get,
					PRISMATIC_LAYER_2);
		} else if (stack.is(ItemInit.prismatic_boots.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.FEET, PrismaticArmorModel.boots::get,
					PRISMATIC_LAYER_1);
		} else if (stack.is(ItemInit.unstained_helm.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.HEAD, UnstainedArmorModel.helmet::get,
					UNSTAINED_LAYER_1);
		} else if (stack.is(ItemInit.unstained_chestplate.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.CHEST, UnstainedArmorModel.chest::get,
					UNSTAINED_LAYER_1);
		} else if (stack.is(ItemInit.unstained_leggings.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.LEGS, UnstainedArmorModel.legs::get,
					UNSTAINED_LAYER_2);
		} else if (stack.is(ItemInit.unstained_boots.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.FEET, UnstainedArmorModel.boots::get,
					UNSTAINED_LAYER_1);
		} else if (stack.is(ItemInit.venous_strider_sabatons.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.FEET, ChalybeateFortressArmorModel.boots::get,
					VENOUS_STRIDER_LAYER_1);
		} else if (stack.is(ItemInit.covenant_mantle.get())) {
			return new ArmorItemRenderDefinition(EquipmentSlot.CHEST, CovenantLeaderArmorModel.chest::get,
					COVENANT_MANTLE_LAYER_1);
		}
		return null;
	}

	private static ResourceLocation armorTexture(String name) {
		return Hemomancy.rloc("textures/models/armor/" + name + ".png");
	}

	public record ArmorItemRenderDefinition(EquipmentSlot slot,
			Supplier<? extends HumanoidModel<LivingEntity>> model,
			ResourceLocation texture) {
	}
}
