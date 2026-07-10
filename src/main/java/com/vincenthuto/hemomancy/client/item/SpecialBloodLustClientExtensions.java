package com.vincenthuto.hemomancy.client.item;

import com.vincenthuto.hemomancy.client.model.armor.EdaciousBloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.PhantasmalBloodLustArmorModel;
import com.vincenthuto.hemomancy.client.model.armor.SheolicBloodLustArmorModel;
import com.vincenthuto.hemomancy.client.render.item.ModelBackedArmorItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public final class SpecialBloodLustClientExtensions {
	private SpecialBloodLustClientExtensions() {
	}

	public static IClientItemExtensions create(String texturePrefix) {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ModelBackedArmorItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

			@Override
			public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
					EquipmentSlot armorSlot, HumanoidModel<?> defaultModel) {
				return modelForSlot(texturePrefix, armorSlot);
			}

			@Override
			public void setupModelAnimations(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot equipmentSlot,
					Model model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
					float netHeadYaw, float headPitch) {
				if (model instanceof EdaciousBloodLustArmorModel<?> edaciousModel) {
					edaciousModel.animateArmorDetails(entityLiving, ageInTicks);
					edaciousModel.setRenderWingsInMainPass(equipmentSlot != EquipmentSlot.CHEST);
				}
			}

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}

	private static HumanoidModel<?> modelForSlot(String texturePrefix, EquipmentSlot slot) {
		return switch (texturePrefix) {
			case "edacious_blood_lust" -> switch (slot) {
				case HEAD -> EdaciousBloodLustArmorModel.helmet.get();
				case CHEST -> EdaciousBloodLustArmorModel.chest.get();
				case LEGS -> EdaciousBloodLustArmorModel.legs.get();
				case FEET -> EdaciousBloodLustArmorModel.boots.get();
				default -> EdaciousBloodLustArmorModel.chest.get();
			};
			case "sheolic_blood_lust" -> switch (slot) {
				case HEAD -> SheolicBloodLustArmorModel.helmet.get();
				case CHEST -> SheolicBloodLustArmorModel.chest.get();
				case LEGS -> SheolicBloodLustArmorModel.legs.get();
				case FEET -> SheolicBloodLustArmorModel.boots.get();
				default -> SheolicBloodLustArmorModel.chest.get();
			};
			case "phantasmal_blood_lust" -> switch (slot) {
				case HEAD -> PhantasmalBloodLustArmorModel.helmet.get();
				case CHEST -> PhantasmalBloodLustArmorModel.chest.get();
				case LEGS -> PhantasmalBloodLustArmorModel.legs.get();
				case FEET -> PhantasmalBloodLustArmorModel.boots.get();
				default -> PhantasmalBloodLustArmorModel.chest.get();
			};
			default -> throw new IllegalArgumentException("Unknown Blood Lust texture prefix " + texturePrefix);
		};
	}
}
