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

	private final ModelPart whole;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart tailMid;
	private final ModelPart tailArch;
	private final ModelPart telson;
	private final ModelPart rLegs;
	private final ModelPart lclaw3;
	private final ModelPart lclaw4;
	private final ModelPart lpincer2;
	private final ModelPart lLeg4;
	private final ModelPart lLeg5;
	private final ModelPart lLeg6;
	private final ModelPart lLegs;
	private final ModelPart lclaw;
	private final ModelPart lclaw2;
	private final ModelPart lpincer;
	private final ModelPart lLeg3;
	private final ModelPart lLeg2;
	private final ModelPart lLeg1;
	private float telsonStingSwell;

	public DesiccantModel(ModelPart root) {
		this.whole = root.getChild("whole");
		this.body = this.whole.getChild("body");
		this.head = this.whole.getChild("head");
		this.tail = this.whole.getChild("tail");
		this.tailMid = this.tail.getChild("tailMid");
		this.tailArch = this.tailMid.getChild("tailArch");
		this.telson = this.tailArch.getChild("telson");
		this.rLegs = this.whole.getChild("rLegs");
		this.lclaw3 = this.rLegs.getChild("lclaw3");
		this.lclaw4 = this.lclaw3.getChild("lclaw4");
		this.lpincer2 = this.lclaw4.getChild("lpincer2");
		this.lLeg4 = this.rLegs.getChild("lLeg4");
		this.lLeg5 = this.rLegs.getChild("lLeg5");
		this.lLeg6 = this.rLegs.getChild("lLeg6");
		this.lLegs = this.whole.getChild("lLegs");
		this.lclaw = this.lLegs.getChild("lclaw");
		this.lclaw2 = this.lclaw.getChild("lclaw2");
		this.lpincer = this.lclaw2.getChild("lpincer");
		this.lLeg3 = this.lLegs.getChild("lLeg3");
		this.lLeg2 = this.lLegs.getChild("lLeg2");
		this.lLeg1 = this.lLegs.getChild("lLeg1");
	}
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

		PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -1.5F, -4.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 12).addBox(-2.0F, -1.5F, 2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create().texOffs(1, 9).addBox(-2.0F, -1.0F, -1.1667F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, -3.8333F));

		PartDefinition tail = whole.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, -0.25F, -1.25F, 2.0F, 1.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 4.0F));
		PartDefinition tailMid = tail.addOrReplaceChild("tailMid", CubeListBuilder.create().texOffs(1, 16).addBox(-0.5F, -0.25F, -0.25F, 1.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.25F, 1.0F));
		PartDefinition tailArch = tailMid.addOrReplaceChild("tailArch", CubeListBuilder.create().texOffs(6, 16).addBox(-0.5F, -1.75F, -0.25F, 1.0F, 3.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 1.0F));

		PartDefinition telson = tailArch.addOrReplaceChild("telson", CubeListBuilder.create().texOffs(18, 15).addBox(0.0F, -1.0F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(13, 16).addBox(-0.5F, 1.0F, -0.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -2.75F, -0.5F, 0.5236F, 0.0F, 0.0F));

		PartDefinition rLegs = whole.addOrReplaceChild("rLegs", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition lclaw3 = rLegs.addOrReplaceChild("lclaw3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.6962F, -2.25F, -3.5F, 0.0829F, 0.303F, 0.0617F));

		PartDefinition lclaw4 = lclaw3.addOrReplaceChild("lclaw4", CubeListBuilder.create().texOffs(10, 18).mirror().addBox(-2.0254F, -0.7587F, -0.677F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.7389F, 0.2782F, 0.4173F, 0.0937F, -1.1353F, -0.0297F));

		PartDefinition lpincer2 = lclaw4.addOrReplaceChild("lpincer2", CubeListBuilder.create().texOffs(12, 12).mirror().addBox(-1.7545F, -0.8199F, -0.8685F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(17, 17).mirror().addBox(-3.7545F, -0.3199F, -1.2685F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.7082F, -0.2637F, -0.314F, -0.3615F, -0.7529F, -0.0347F));

		PartDefinition lLeg4 = rLegs.addOrReplaceChild("lLeg4", CubeListBuilder.create().texOffs(12, 10).mirror().addBox(-4.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.6962F, -2.25F, 1.25F, 0.0F, 0.2182F, 0.0F));

		PartDefinition lLeg5 = rLegs.addOrReplaceChild("lLeg5", CubeListBuilder.create().texOffs(12, 10).mirror().addBox(-4.0F, 0.25F, -2.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.1962F, -3.0F, 1.5F));

		PartDefinition lLeg6 = rLegs.addOrReplaceChild("lLeg6", CubeListBuilder.create().texOffs(12, 10).mirror().addBox(-4.0F, 0.25F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.1962F, -3.0F, -1.25F, 0.0F, -0.2618F, 0.0F));

		PartDefinition lLegs = whole.addOrReplaceChild("lLegs", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition lclaw = lLegs.addOrReplaceChild("lclaw", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6962F, -2.25F, -3.5F, 0.0829F, -0.303F, -0.0617F));

		PartDefinition lclaw2 = lclaw.addOrReplaceChild("lclaw2", CubeListBuilder.create().texOffs(10, 18).addBox(0.0254F, -0.7587F, -0.677F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7389F, 0.2782F, 0.4173F, 0.0937F, 1.1353F, 0.0297F));

		PartDefinition lpincer = lclaw2.addOrReplaceChild("lpincer", CubeListBuilder.create().texOffs(12, 12).addBox(-0.2455F, -0.8199F, -0.8685F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(17, 17).addBox(-0.2455F, -0.3199F, -1.2685F, 4.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.7082F, -0.2637F, -0.314F, -0.3615F, 0.7529F, 0.0347F));

		PartDefinition lLeg3 = lLegs.addOrReplaceChild("lLeg3", CubeListBuilder.create().texOffs(12, 10).addBox(-0.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.6962F, -2.25F, 1.25F, 0.0F, -0.2182F, 0.0F));

		PartDefinition lLeg2 = lLegs.addOrReplaceChild("lLeg2", CubeListBuilder.create().texOffs(12, 10).addBox(-1.0F, 0.25F, -2.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(5.1962F, -3.0F, 1.5F));

		PartDefinition lLeg1 = lLegs.addOrReplaceChild("lLeg1", CubeListBuilder.create().texOffs(12, 10).addBox(-1.0F, 0.25F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1962F, -3.0F, -1.25F, 0.0F, 0.2618F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}


	@Override
	public void setupAnim(DesiccantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.whole.resetPose();
		this.body.resetPose();
		this.head.resetPose();
		this.tail.resetPose();
		this.tailMid.resetPose();
		this.tailArch.resetPose();
		this.telson.resetPose();
		this.rLegs.resetPose();
		this.lLegs.resetPose();
		this.lclaw3.resetPose();
		this.lclaw4.resetPose();
		this.lpincer2.resetPose();
		this.lclaw.resetPose();
		this.lclaw2.resetPose();
		this.lpincer.resetPose();
		this.lLeg1.resetPose();
		this.lLeg2.resetPose();
		this.lLeg3.resetPose();
		this.lLeg4.resetPose();
		this.lLeg5.resetPose();
		this.lLeg6.resetPose();

		float step = limbSwing * 0.6662F;
		float gait = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
		float squirm = ageInTicks * 0.11F;
		this.whole.y += Mth.sin(step * 2.0F) * 0.12F * gait;
		this.whole.yRot = Mth.sin(squirm) * 0.035F;
		this.head.yRot = Mth.clamp(netHeadYaw * Mth.DEG_TO_RAD, -0.3F, 0.3F);
		this.head.xRot = Mth.clamp(headPitch * Mth.DEG_TO_RAD, -0.2F, 0.2F);
		this.lLegs.zRot += Mth.cos(step) * 0.08F * gait;
		this.rLegs.zRot -= Mth.cos(step) * 0.08F * gait;
		this.lLeg1.yRot += Mth.cos(step) * 0.26F * gait;
		this.lLeg2.yRot += Mth.cos(step + Mth.PI) * 0.26F * gait;
		this.lLeg3.yRot += Mth.cos(step) * 0.26F * gait;
		this.lLeg6.yRot -= Mth.cos(step + Mth.PI) * 0.26F * gait;
		this.lLeg5.yRot -= Mth.cos(step) * 0.26F * gait;
		this.lLeg4.yRot -= Mth.cos(step + Mth.PI) * 0.26F * gait;
		this.lclaw.zRot += Mth.sin(squirm + 0.8F) * 0.07F;
		this.lclaw2.yRot += Mth.sin(squirm + 1.6F) * 0.09F;
		this.lpincer.yRot += Mth.sin(squirm + 2.4F) * 0.06F;
		this.lclaw3.zRot -= Mth.sin(squirm + 2.1F) * 0.07F;
		this.lclaw4.yRot -= Mth.sin(squirm + 2.9F) * 0.09F;
		this.lpincer2.yRot -= Mth.sin(squirm + 3.7F) * 0.06F;
		this.tail.yRot = Mth.sin(squirm + 1.0F) * 0.13F;
		this.tail.xRot = Mth.sin(squirm + 2.0F) * 0.035F;
		this.tailMid.yRot = Mth.sin(squirm + 1.8F) * 0.09F;
		this.tailMid.xRot = Mth.sin(squirm + 2.5F) * 0.055F;
		this.tailArch.yRot = Mth.sin(squirm + 2.6F) * 0.08F;
		this.tailArch.xRot = Mth.sin(squirm + 3.2F) * 0.07F;
		this.telson.xRot += Mth.sin(squirm + 2.9F) * 0.09F;
		this.telson.xScale = 1.0F;
		this.telson.yScale = 1.0F;
		this.telson.zScale = 1.0F;
		this.telsonStingSwell = 0.0F;

		if (entity.isStinging()) {
			float stingProgress = entity.getStingProgress();
			float strike = Mth.sin(stingProgress * Mth.PI);
			this.telsonStingSwell = strike;
			this.tail.xRot -= strike * 0.35F;
			this.tailMid.xRot -= strike * 0.25F;
			this.tailArch.xRot -= strike * 0.15F;
			this.telson.xRot -= strike * 0.25F;
			float telsonScale = 1.0F + strike * 0.45F;
			this.telson.xScale = telsonScale;
			this.telson.yScale = telsonScale;
			this.telson.zScale = telsonScale;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		whole.render(poseStack, buffer, packedLight, packedOverlay, packedColor);

	}
}
