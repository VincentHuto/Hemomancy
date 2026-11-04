package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import net.minecraft.world.entity.Mob;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

// Made with Blockbench 4.1.1
// Exported for Minecraft version 1.17 with Mojang mappings


public class ThirsterModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("modelthirster"), "main");
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head2 = Head.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.5111F, -7.4805F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.7104F, 1.2249F));

		PartDefinition lowerJaw = head2.addOrReplaceChild("lowerJaw", CubeListBuilder.create().texOffs(16, 22).addBox(-3.0F, -2.0261F, -6.8191F, 6.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5312F, -0.9696F, -0.0873F, 0.0F, 0.0F));

		PartDefinition neck = Head.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(39, 28).addBox(-2.0F, -27.9136F, -0.8389F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 26.0F, -2.0F));

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -1.0F, 2.0F, 8.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 32).addBox(-4.0F, 1.0F, -1.0F, 8.0F, 11.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 46).addBox(-4.0F, 10.0F, 2.0F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(44, 12).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 2.0F));

		PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(38, 38).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 2.0F));

		PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(22, 32).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	private final ModelPart Head;
	private final ModelPart Body;
	private final ModelPart RightArm;
	private final ModelPart LeftArm;
	private final ModelPart RightLeg;

	private final ModelPart LeftLeg;
	private final ModelPart lowerJaw;
	public ThirsterModel(ModelPart root) {
		this.lowerJaw = root.getChild("Head").getChild("head2").getChild("lowerJaw");
		this.Head = root.getChild("Head");
		this.Body = root.getChild("Body");
		this.RightArm = root.getChild("RightArm");
		this.LeftArm = root.getChild("LeftArm");
		this.RightLeg = root.getChild("RightLeg");
		this.LeftLeg = root.getChild("LeftLeg");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		Head.render(poseStack, buffer, packedLight, packedOverlay);
		Body.render(poseStack, buffer, packedLight, packedOverlay);
		RightArm.render(poseStack, buffer, packedLight, packedOverlay);
		LeftArm.render(poseStack, buffer, packedLight, packedOverlay);
		RightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		LeftLeg.render(poseStack, buffer, packedLight, packedOverlay);
	}

	/**
	 * A gaunt, hungry biped: a stooped walk with the arms hanging loose, the jaw working whenever it has prey in
	 * sight, and a two-handed grab for the attack beat.
	 */
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		Head.resetPose();
		Body.resetPose();
		RightArm.resetPose();
		LeftArm.resetPose();
		RightLeg.resetPose();
		LeftLeg.resetPose();
		lowerJaw.resetPose();
		boolean hungry = entity instanceof Mob mob && mob.isAggressive();
		float stride = Math.min(1.0F, limbSwingAmount * 1.5F);
		float phase = limbSwing * 0.6662F;
		Head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
		Head.xRot += headPitch * Mth.DEG_TO_RAD + 0.08F * stride;
		Body.xRot = 0.12F + 0.04F * stride;
		RightLeg.xRot = Mth.cos(phase) * 1.2F * stride;
		LeftLeg.xRot = Mth.cos(phase + Mth.PI) * 1.2F * stride;
		RightArm.xRot = Mth.cos(phase + Mth.PI) * 0.9F * stride + 0.15F;
		LeftArm.xRot = Mth.cos(phase) * 0.9F * stride + 0.15F;
		RightArm.zRot = 0.08F + Mth.cos(ageInTicks * 0.09F) * 0.03F;
		LeftArm.zRot = -0.08F - Mth.cos(ageInTicks * 0.09F + 1.0F) * 0.03F;
		// The jaw chatters when it is hunting and hangs slack otherwise.
		lowerJaw.xRot += hungry ? 0.25F + Mth.abs(Mth.sin(ageInTicks * 0.35F)) * 0.3F : 0.12F;
		if (attackTime > 0.0F) {
			// Both arms come up and lunge forward together, the head thrown back and the jaw wide.
			float reach = Mth.sin(attackTime * Mth.PI);
			RightArm.xRot = -1.3F * reach + RightArm.xRot * (1.0F - reach);
			LeftArm.xRot = -1.3F * reach + LeftArm.xRot * (1.0F - reach);
			RightArm.yRot = -0.35F * reach;
			LeftArm.yRot = 0.35F * reach;
			Head.xRot -= 0.3F * reach;
			lowerJaw.xRot += 0.5F * reach;
			Body.xRot += 0.2F * reach;
		}
	}
}
