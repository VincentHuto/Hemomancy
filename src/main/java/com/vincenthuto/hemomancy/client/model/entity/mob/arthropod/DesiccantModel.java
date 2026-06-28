package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.DesiccantEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class DesiccantModel extends EntityModel<DesiccantEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("desiccant"), "main");
	private static final int TELSON_STING_COLOR = 0xFFFF3030;

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart telson;
	private final ModelPart leftLegs;
	private final ModelPart rightLegs;
	private float telsonStingSwell;

	public DesiccantModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tail = root.getChild("tail");
		this.telson = root.getChild("telson");
		this.leftLegs = root.getChild("leftLegs");
		this.rightLegs = root.getChild("rightLegs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Scorpion-like body
		partdefinition.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.0F, 0.0F));

		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 11).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(14, 11).addBox(-3.0F, -1.0F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(14, 15).addBox(1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.0F, -4.0F));

		// Tail curving upward
		partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(20, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(20, 6).addBox(-0.5F, -4.0F, 3.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 20.0F, 4.0F));

		partdefinition.addOrReplaceChild("telson", CubeListBuilder.create()
				.texOffs(26, 6).addBox(-0.5F, -6.0F, 2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 20.0F, 4.0F));

		// Legs
		partdefinition.addOrReplaceChild("leftLegs", CubeListBuilder.create()
				.texOffs(0, 17).addBox(0.0F, 0.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 19).addBox(0.0F, 0.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(0.0F, 0.0F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(0.0F, 0.0F, 3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 22.0F, 0.0F));

		partdefinition.addOrReplaceChild("rightLegs", CubeListBuilder.create()
				.texOffs(10, 17).addBox(-4.0F, 0.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(10, 19).addBox(-4.0F, 0.0F, -1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(10, 21).addBox(-4.0F, 0.0F, 1.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(10, 23).addBox(-4.0F, 0.0F, 3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 22.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(DesiccantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float legSwing = (float) Math.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
		this.leftLegs.zRot = legSwing;
		this.rightLegs.zRot = -legSwing;
		this.tail.xRot = 0.0F;
		this.telson.xRot = 0.0F;
		this.telson.xScale = 1.0F;
		this.telson.yScale = 1.0F;
		this.telson.zScale = 1.0F;
		this.telsonStingSwell = 0.0F;

		if (entity.isStinging()) {
			float stingProgress = entity.getStingProgress();
			float strike = Mth.sin(stingProgress * Mth.PI);
			this.telsonStingSwell = strike;
			this.tail.xRot = -0.35F - strike * 0.75F;
			this.telson.xRot = this.tail.xRot - strike * 0.25F;
			float telsonScale = 1.0F + strike * 0.45F;
			this.telson.xScale = telsonScale;
			this.telson.yScale = telsonScale;
			this.telson.zScale = telsonScale;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		head.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		tail.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		int telsonColor = this.telsonStingSwell > 0.0F ? TELSON_STING_COLOR : packedColor;
		telson.render(poseStack, buffer, packedLight, packedOverlay, telsonColor);
		leftLegs.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		rightLegs.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
