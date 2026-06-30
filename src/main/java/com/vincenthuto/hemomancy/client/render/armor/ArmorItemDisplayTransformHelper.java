package com.vincenthuto.hemomancy.client.render.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;

public final class ArmorItemDisplayTransformHelper {
	private ArmorItemDisplayTransformHelper() {
	}

	public static void apply(ItemDisplayContext context, EquipmentSlot slot, PoseStack poseStack) {
		poseStack.translate(0.5D, 0.5D, 0.5D);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

		switch (context) {
			case GUI -> {
				translateWithNormalizedSlotOffset(poseStack, slot, -0.35D, 0.0D, 1.0D);
				if (slot == EquipmentSlot.HEAD) {
					applyHelmetGuiTransform(slot, poseStack);
				} else {
					poseStack.mulPose(Axis.YP.rotationDegrees(-28.0F));
				}
				poseStack.mulPose(Axis.ZP.rotationDegrees(8.0F));
				scaleForSlot(poseStack, slot, 0.72F);
			}
			case GROUND -> {
				translateWithNormalizedSlotOffset(poseStack, slot, -0.15D, 0.0D, 0.45D);
				scaleForSlot(poseStack, slot, 0.36F);
			}
			case FIXED -> {
				translateWithNormalizedSlotOffset(poseStack, slot, -0.18D, 0.0D, 0.65D);
				poseStack.mulPose(Axis.YP.rotationDegrees(-25.0F));
				scaleForSlot(poseStack, slot, 0.45F);
			}
			case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> {
				translateWithNormalizedSlotOffset(poseStack, slot, -0.55D, -0.18D, 0.45D);
				applyArmorFirstPersonTransform(context, slot, poseStack);
				scaleForSlot(poseStack, slot, 0.36F);
			}
			case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
				translateWithNormalizedSlotOffset(poseStack, slot, -0.45D, -0.10D, 0.45D);
				scaleForSlot(poseStack, slot, 0.32F);
			}
			default -> {
				translateWithNormalizedSlotOffset(poseStack, slot, 0.0D, 0.0D, 0.65D);
				scaleForSlot(poseStack, slot, 0.45F);
			}
		}
	}

	private static void translateWithNormalizedSlotOffset(PoseStack poseStack, EquipmentSlot slot, double baseYOffset,
			double zOffset, double slotOffsetScale) {
		poseStack.translate(0.0D, baseYOffset + normalizedSlotYOffset(slot) * slotOffsetScale, zOffset);
	}

	private static double normalizedSlotYOffset(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> 0.55D;
			case CHEST -> 0.15D;
			case LEGS -> -0.30D;
			case FEET -> -0.45D;
			default -> 0.0D;
		};
	}

	private static void applyHelmetGuiTransform(EquipmentSlot slot, PoseStack poseStack) {
		if (slot == EquipmentSlot.HEAD) {
			poseStack.mulPose(Axis.YP.rotationDegrees(-24.0F));
		}
	}

	private static void applyArmorFirstPersonTransform(ItemDisplayContext context, EquipmentSlot slot, PoseStack poseStack) {
			float yaw = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -54.0F : 54.0F;
		if (slot != EquipmentSlot.FEET) {
			poseStack.translate(0.0D, 0.15D, 0.0D);
		}else{
			poseStack.translate(0.0D, 0.1D, 0.0D);
		}
			poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
	}

	private static void scaleForSlot(PoseStack poseStack, EquipmentSlot slot, float scale) {
		float slotScale = slot == EquipmentSlot.CHEST ? scale * 0.82F : scale;
		poseStack.scale(slotScale, slotScale, slotScale);
	}
}
