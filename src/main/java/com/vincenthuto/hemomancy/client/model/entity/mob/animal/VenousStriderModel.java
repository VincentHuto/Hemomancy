package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VenousStriderEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class VenousStriderModel extends EntityModel<VenousStriderEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("venous_strider"), "main");

	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart tailFeathers;

	public VenousStriderModel(ModelPart root) {
		this.body = root.getChild("body");
		this.neck = root.getChild("neck");
		this.head = root.getChild("head");
		this.leftLeg = root.getChild("leftLeg");
		this.rightLeg = root.getChild("rightLeg");
		this.leftWing = root.getChild("leftWing");
		this.rightWing = root.getChild("rightWing");
		this.tailFeathers = root.getChild("tailFeathers");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Heron/crane-like body — compact, elegant
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 8.0F, 0.0F));

		// Long elegant neck
		partdefinition.addOrReplaceChild("neck", CubeListBuilder.create()
				.texOffs(0, 13).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 6.0F, -3.0F, 0.2618F, 0.0F, 0.0F));

		// Small delicate head with long beak
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(8, 13).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(20, 13).addBox(-0.5F, -1.0F, -6.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0F, -3.5F));

		// Long thin legs
		partdefinition.addOrReplaceChild("leftLeg", CubeListBuilder.create()
				.texOffs(28, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(32, 0).addBox(-1.0F, 13.0F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 10.0F, 0.0F));

		partdefinition.addOrReplaceChild("rightLeg", CubeListBuilder.create()
				.texOffs(28, 14).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(32, 14).addBox(-1.0F, 13.0F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 10.0F, 0.0F));

		// Folded wings
		partdefinition.addOrReplaceChild("leftWing", CubeListBuilder.create()
				.texOffs(40, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 7.0F, -2.0F));

		partdefinition.addOrReplaceChild("rightWing", CubeListBuilder.create()
				.texOffs(40, 11).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 7.0F, -2.0F));

		// Trailing tail feathers
		partdefinition.addOrReplaceChild("tailFeathers", CubeListBuilder.create()
				.texOffs(20, 0).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 7.0F, 4.0F, -0.2618F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(VenousStriderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.head.xRot = headPitch * ((float) Math.PI / 180F);
		this.neck.xRot = 0.2618F + headPitch * ((float) Math.PI / 180F) * 0.5F;

		// Slow, deliberate leg movement
		this.leftLeg.xRot = (float) Math.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
		this.rightLeg.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;

		// Subtle wing flutter when idle
		float wingFlutter = (float) Math.sin(ageInTicks * 0.067F) * 0.05F;
		this.leftWing.zRot = -wingFlutter;
		this.rightWing.zRot = wingFlutter;

		// Gentle tail sway
		this.tailFeathers.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.1F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		neck.render(poseStack, buffer, packedLight, packedOverlay);
		head.render(poseStack, buffer, packedLight, packedOverlay);
		leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		leftWing.render(poseStack, buffer, packedLight, packedOverlay);
		rightWing.render(poseStack, buffer, packedLight, packedOverlay);
		tailFeathers.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
