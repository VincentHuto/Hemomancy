package com.vincenthuto.hemomancy.client.model.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BloodArmModel<T extends LivingEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation blood_arm = new ModelLayerLocation(
			Hemomancy.rloc("blood_arm"), "main");

	public static LayerDefinition createHeadLayer() {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.28F), 0);

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public BloodArmModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		head.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		body.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		leftArm.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		rightArm.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		leftLeg.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		rightLeg.render(poseStack, buffer, packedLight, packedOverlay, packedColor);

	}

}
