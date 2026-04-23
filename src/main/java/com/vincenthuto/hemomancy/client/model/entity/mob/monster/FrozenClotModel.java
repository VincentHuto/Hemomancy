package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.FrozenClotEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class FrozenClotModel extends EntityModel<FrozenClotEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("frozen_clot"), "main");

	private final ModelPart body;
	private final ModelPart crystal1;
	private final ModelPart crystal2;
	private final ModelPart crystal3;

	public FrozenClotModel(ModelPart root) {
		this.body = root.getChild("body");
		this.crystal1 = root.getChild("crystal1");
		this.crystal2 = root.getChild("crystal2");
		this.crystal3 = root.getChild("crystal3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Irregular frozen blood mass
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 20).addBox(-3.0F, -7.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 17.0F, 0.0F));

		// Crystalline protrusions
		partdefinition.addOrReplaceChild("crystal1", CubeListBuilder.create()
				.texOffs(24, 20).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		partdefinition.addOrReplaceChild("crystal2", CubeListBuilder.create()
				.texOffs(30, 20).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, 12.0F, 3.0F, -0.2618F, 0.0F, 0.2618F));

		partdefinition.addOrReplaceChild("crystal3", CubeListBuilder.create()
				.texOffs(36, 20).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 12.0F, -4.0F, 0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(FrozenClotEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Subtle pulsing effect
		float pulse = (float) Math.sin(ageInTicks * 0.05F) * 0.02F;
		this.body.yRot = pulse;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		crystal1.render(poseStack, buffer, packedLight, packedOverlay);
		crystal2.render(poseStack, buffer, packedLight, packedOverlay);
		crystal3.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
