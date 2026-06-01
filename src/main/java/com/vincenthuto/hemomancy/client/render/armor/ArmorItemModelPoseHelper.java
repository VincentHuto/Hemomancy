package com.vincenthuto.hemomancy.client.render.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public final class ArmorItemModelPoseHelper {
	private ArmorItemModelPoseHelper() {
	}

	public static void reset(HumanoidModel<?> model) {
		model.head.getAllParts().forEach(ModelPart::resetPose);
		model.hat.getAllParts().forEach(ModelPart::resetPose);
		model.body.getAllParts().forEach(ModelPart::resetPose);
		model.rightArm.getAllParts().forEach(ModelPart::resetPose);
		model.leftArm.getAllParts().forEach(ModelPart::resetPose);
		model.rightLeg.getAllParts().forEach(ModelPart::resetPose);
		model.leftLeg.getAllParts().forEach(ModelPart::resetPose);
		model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		model.crouching = false;
		model.swimAmount = 0.0F;
	}
}
