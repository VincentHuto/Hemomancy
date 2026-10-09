package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_scarlet_mummer.py; regenerate the model, atlas and BBModel together.
public class ScarletMummerModel extends HumanoidModel<ScarletMummerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "scarlet_mummer"), "main");

	private final ModelPart leftGorget;
	private final ModelPart rightGorget;

	public ScarletMummerModel(ModelPart root) {
		super(root);
		leftGorget = body.getChild("left_gorget");
		rightGorget = body.getChild("right_gorget");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F).texOffs(17, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition hat = mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create().texOffs(26, 0).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 1.0F, 5.0F).texOffs(47, 0).addBox(-1.5F, -9.0F, -1.5F, 3.0F, 2.0F, 3.0F).texOffs(60, 0).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(69, 0).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(74, 0).addBox(-2.0F, 1.0F, -1.5F, 4.0F, 5.0F, 3.0F).texOffs(89, 0).addBox(-1.5F, 6.0F, -1.0F, 3.0F, 4.0F, 2.0F).texOffs(100, 0).addBox(-2.5F, 10.0F, -1.5F, 5.0F, 2.0F, 3.0F).texOffs(117, 0).addBox(-0.5F, 1.0F, -2.0F, 1.0F, 8.0F, 1.0F).texOffs(0, 11).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition left_gorget = body.addOrReplaceChild("left_gorget", CubeListBuilder.create().texOffs(21, 11).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(1.5F, 1.0F, 1.0F, 0.0F, -0.5585053606381855F, -0.08726646259971647F));
        PartDefinition left_upper_fan = left_gorget.addOrReplaceChild("left_upper_fan", CubeListBuilder.create().texOffs(26, 11).addBox(0.0F, -7.0F, 0.0F, 5.0F, 11.0F, 1.0F).texOffs(39, 11).addBox(4.0F, -6.0F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.12217304763960307F));
        PartDefinition left_outer_fan = left_upper_fan.addOrReplaceChild("left_outer_fan", CubeListBuilder.create().texOffs(44, 11).addBox(0.0F, -2.0F, 0.0F, 4.0F, 8.0F, 1.0F).texOffs(55, 11).addBox(3.0F, -1.0F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(5.0F, -3.0F, 0.0F, 0.0F, 0.0F, -0.17453292519943295F));
        PartDefinition left_lower_fan = left_gorget.addOrReplaceChild("left_lower_fan", CubeListBuilder.create().texOffs(60, 11).addBox(0.0F, 0.0F, 0.0F, 4.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.4014257279586958F));
        PartDefinition left_arm = mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(71, 11).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(80, 11).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(3.0F, 2.0F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(93, 11).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(98, 11).addBox(-1.0F, 5.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(107, 11).addBox(-0.5F, 7.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.10471975511965978F));
        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(112, 11).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(1.5F, 12.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(9, 24).addBox(-1.0F, 5.0F, -3.0F, 2.0F, 1.0F, 4.0F).texOffs(22, 24).addBox(-0.5F, 4.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_gorget = body.addOrReplaceChild("right_gorget", CubeListBuilder.create().texOffs(27, 24).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-1.5F, 1.0F, 1.0F, 0.0F, 0.5585053606381855F, 0.08726646259971647F));
        PartDefinition right_upper_fan = right_gorget.addOrReplaceChild("right_upper_fan", CubeListBuilder.create().texOffs(32, 24).addBox(-5.0F, -7.0F, 0.0F, 5.0F, 11.0F, 1.0F).texOffs(45, 24).addBox(-5.0F, -6.0F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.12217304763960307F));
        PartDefinition right_outer_fan = right_upper_fan.addOrReplaceChild("right_outer_fan", CubeListBuilder.create().texOffs(50, 24).addBox(-4.0F, -2.0F, 0.0F, 4.0F, 8.0F, 1.0F).texOffs(61, 24).addBox(-4.0F, -1.0F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(-5.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.17453292519943295F));
        PartDefinition right_lower_fan = right_gorget.addOrReplaceChild("right_lower_fan", CubeListBuilder.create().texOffs(66, 24).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, -0.4014257279586958F));
        PartDefinition right_arm = mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(77, 24).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(86, 24).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(-3.0F, 2.0F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(99, 24).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F).texOffs(104, 24).addBox(-1.0F, 5.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(113, 24).addBox(-0.5F, 7.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.10471975511965978F));
        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(118, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(-1.5F, 12.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(0, 37).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(9, 37).addBox(-1.0F, 5.0F, -3.0F, 2.0F, 1.0F, 4.0F).texOffs(22, 37).addBox(-0.5F, 4.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(ScarletMummerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float flare = entity.isPerforming() ? 0.34F + Mth.sin(ageInTicks * 0.3F) * 0.05F : 0.08F;
		leftGorget.zRot = -flare;
		rightGorget.zRot = flare;
		leftGorget.yRot = entity.isPerforming() ? -0.08F : -1.15F;
		rightGorget.yRot = entity.isPerforming() ? 0.08F : 1.15F;
		if (entity.isPerforming()) {
			body.yRot = Mth.sin(ageInTicks * 0.22F) * 0.08F;
			leftArm.zRot -= 0.35F;
			rightArm.zRot += 0.35F;
		}
		// Humanoid attacks assume five-pixel shoulders; keep the authored puppet joints attached.
		leftArm.x = Mth.cos(body.yRot) * leftArm.getInitialPose().x;
		rightArm.x = Mth.cos(body.yRot) * rightArm.getInitialPose().x;
		leftArm.z = -Mth.sin(body.yRot) * leftArm.getInitialPose().x;
		rightArm.z = -Mth.sin(body.yRot) * rightArm.getInitialPose().x;
	}
}
