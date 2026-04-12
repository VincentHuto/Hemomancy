package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.HemojellyEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class HemojellyModel extends EntityModel<HemojellyEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("hemojelly"), "main");

	private final ModelPart bell;
	private final ModelPart innerBell;
	private final ModelPart tentacle1;
	private final ModelPart tentacle2;
	private final ModelPart tentacle3;
	private final ModelPart tentacle4;
	private final ModelPart tentacle5;

	public HemojellyModel(ModelPart root) {
		this.bell = root.getChild("bell");
		this.innerBell = root.getChild("innerBell");
		this.tentacle1 = root.getChild("tentacle1");
		this.tentacle2 = root.getChild("tentacle2");
		this.tentacle3 = root.getChild("tentacle3");
		this.tentacle4 = root.getChild("tentacle4");
		this.tentacle5 = root.getChild("tentacle5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Outer bell — dome shape
		partdefinition.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 14).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));

		// Inner bell — slightly smaller, translucent core
		partdefinition.addOrReplaceChild("innerBell", CubeListBuilder.create()
				.texOffs(24, 14).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));

		// Trailing tentacles — 5 delicate filaments
		partdefinition.addOrReplaceChild("tentacle1", CubeListBuilder.create()
				.texOffs(32, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));

		partdefinition.addOrReplaceChild("tentacle2", CubeListBuilder.create()
				.texOffs(36, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.5F, 14.0F, -2.5F));

		partdefinition.addOrReplaceChild("tentacle3", CubeListBuilder.create()
				.texOffs(40, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.5F, 14.0F, -2.5F));

		partdefinition.addOrReplaceChild("tentacle4", CubeListBuilder.create()
				.texOffs(44, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.5F, 14.0F, 2.5F));

		partdefinition.addOrReplaceChild("tentacle5", CubeListBuilder.create()
				.texOffs(48, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.5F, 14.0F, 2.5F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(HemojellyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Gentle pulsing of the bell
		float pulse = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;
		this.bell.y = 14.0F + pulse * 8.0F;
		this.innerBell.y = 14.0F + pulse * 8.0F;

		// Tentacles sway gracefully
		float sway = (float) Math.sin(ageInTicks * 0.08F) * 0.2F;
		this.tentacle1.xRot = sway;
		this.tentacle2.xRot = -sway * 0.7F;
		this.tentacle2.zRot = sway * 0.5F;
		this.tentacle3.xRot = sway * 0.8F;
		this.tentacle3.zRot = -sway * 0.5F;
		this.tentacle4.zRot = sway * 0.6F;
		this.tentacle5.zRot = -sway * 0.6F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bell.render(poseStack, buffer, packedLight, packedOverlay);
		innerBell.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle1.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle2.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle3.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle4.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle5.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
