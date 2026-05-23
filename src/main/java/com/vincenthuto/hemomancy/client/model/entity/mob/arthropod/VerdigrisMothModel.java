package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VerdigrisMothEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class VerdigrisMothModel extends EntityModel<VerdigrisMothEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("verdigris_moth"), "main");

	private final ModelPart whole;
	private final ModelPart body;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart leftTail;
	private final ModelPart rightTail;
	private final ModelPart antennae;

	public VerdigrisMothModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.whole = root.getChild("whole");
		this.body = this.whole.getChild("body");
		this.leftWing = this.whole.getChild("leftWing");
		this.rightWing = this.whole.getChild("rightWing");
		this.leftTail = this.whole.getChild("leftTail");
		this.rightTail = this.whole.getChild("rightTail");
		this.antennae = this.body.getChild("antennae");
	}
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 21.5F, 0.0F));

		PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(-1, 22).addBox(-1.0F, -0.75F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(19, 30).addBox(-1.0F, -0.9F, -4.7F, 2.0F, 1.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition antennae = body.addOrReplaceChild("antennae", CubeListBuilder.create().texOffs(28, 30).addBox(0.6F, 0.3F, -6.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(28, 30).mirror().addBox(-1.6F, 0.55F, -6.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -1.05F, -0.2F));

		PartDefinition leftWing = whole.addOrReplaceChild("leftWing", CubeListBuilder.create().texOffs(0, 0).addBox(-9.75F, -0.25F, -4.25F, 10.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.15F, -0.15F, -0.15F, 0.0F, -0.08F, 0.16F));

		PartDefinition rightWing = whole.addOrReplaceChild("rightWing", CubeListBuilder.create().texOffs(0, 11).addBox(-0.25F, -0.25F, -4.25F, 10.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.15F, -0.15F, -0.15F, 0.0F, 0.08F, -0.16F));

		PartDefinition leftTail = whole.addOrReplaceChild("leftTail", CubeListBuilder.create().texOffs(19, 22).addBox(-0.78F, -0.055F, -0.0456F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -0.3F, 3.1F, 0.18F, -0.05F, 0.08F));

		PartDefinition rightTail = whole.addOrReplaceChild("rightTail", CubeListBuilder.create().texOffs(19, 22).mirror().addBox(-0.22F, -0.055F, -0.0456F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.4F, -0.3F, 3.1F, 0.18F, 0.05F, -0.08F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(VerdigrisMothEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		float airborne = entity.onGround() ? 0.25F : 1.0F;
		float flap = Mth.sin(ageInTicks * 0.72F) * 0.34F * airborne;
		this.leftWing.zRot = -0.16F + flap;
		this.rightWing.zRot = 0.16F - flap;
		this.leftWing.yRot = 0.08F + flap * 0.15F;
		this.rightWing.yRot = -0.08F - flap * 0.15F;
		this.leftTail.xRot = 0.18F + Mth.sin(ageInTicks * 0.28F) * 0.08F;
		this.rightTail.xRot = 0.18F + Mth.sin(ageInTicks * 0.28F + Mth.PI) * 0.08F;
		this.antennae.zRot = Mth.sin(ageInTicks * 0.18F) * 0.06F;
		this.body.y = Mth.sin(ageInTicks * 0.22F) * 0.08F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		this.whole.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
