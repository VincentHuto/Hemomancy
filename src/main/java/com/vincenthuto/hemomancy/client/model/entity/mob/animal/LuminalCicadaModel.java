package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaRules;
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

public final class LuminalCicadaModel extends EntityModel<LuminalCicadaEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("luminal_cicada"), "main");
	private final ModelPart root;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart abdomen;
	private final ModelPart lantern;
	private final ModelPart[] leftLegs;
	private final ModelPart[] rightLegs;

	public LuminalCicadaModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.root = root.getChild("root");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.abdomen = this.root.getChild("abdomen");
		this.lantern = this.abdomen.getChild("lantern");
		this.leftLegs = new ModelPart[] {
				this.root.getChild("left_front_leg"),
				this.root.getChild("left_middle_leg"),
				this.root.getChild("left_back_leg")
		};
		this.rightLegs = new ModelPart[] {
				this.root.getChild("right_front_leg"),
				this.root.getChild("right_middle_leg"),
				this.root.getChild("right_back_leg")
		};
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.offset(0.0F, 20.5F, 0.0F));
		PartDefinition abdomen = root.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 20)
				.addBox(-2.0F, -1.5F, 0.0F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, -0.5F));
		root.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(0, 32)
				.addBox(-2.5F, -2.0F, -3.5F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
		root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 32)
				.addBox(-2.0F, -1.5F, -5.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(36, 32).addBox(-2.35F, -1.1F, -5.3F, 0.75F, 1.25F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(36, 32).mirror().addBox(1.6F, -1.1F, -5.3F, 0.75F, 1.25F, 1.0F,
						new CubeDeformation(0.0F)), PartPose.ZERO);
		abdomen.addOrReplaceChild("lantern", CubeListBuilder.create().texOffs(20, 40)
				.addBox(-1.5F, -1.1F, 6.25F, 3.0F, 2.2F, 1.0F, new CubeDeformation(0.05F)), PartPose.ZERO);
		root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 0)
				.addBox(0.0F, 0.0F, -3.5F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.5F, -1.8F, 0.0F));
		root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 9).mirror()
				.addBox(-8.0F, 0.0F, -3.5F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.5F, -1.8F, 0.0F));
		root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(32, 40)
				.addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.5F, 0.7F, -2.8F, 0.0F, -0.55F, 0.35F));
		root.addOrReplaceChild("left_middle_leg", CubeListBuilder.create().texOffs(32, 40)
				.addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.5F, 0.8F, -1.2F, 0.0F, 0.0F, 0.35F));
		root.addOrReplaceChild("left_back_leg", CubeListBuilder.create().texOffs(32, 40)
				.addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, 0.7F, 1.2F, 0.0F, 0.55F, 0.35F));
		root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(32, 40).mirror()
				.addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5F, 0.7F, -2.8F, 0.0F, 0.55F, -0.35F));
		root.addOrReplaceChild("right_middle_leg", CubeListBuilder.create().texOffs(32, 40).mirror()
				.addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.5F, 0.8F, -1.2F, 0.0F, 0.0F, -0.35F));
		root.addOrReplaceChild("right_back_leg", CubeListBuilder.create().texOffs(32, 40).mirror()
				.addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.7F, 1.2F, 0.0F, -0.55F, -0.35F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(LuminalCicadaEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		boolean clinging = entity.getClingFace() != null;
		float buzz = clinging ? 0.0F : Mth.sin(ageInTicks * 2.4F) * 0.18F;
		this.leftWing.yRot = clinging ? 1.05F : -0.15F;
		this.rightWing.yRot = clinging ? -1.05F : 0.15F;
		this.leftWing.zRot = 0.12F + buzz;
		this.rightWing.zRot = -0.12F - buzz;
		float leftLegRoll = LuminalCicadaRules.legRoll(clinging, false);
		float rightLegRoll = LuminalCicadaRules.legRoll(clinging, true);
		for (ModelPart leg : this.leftLegs) leg.zRot = leftLegRoll;
		for (ModelPart leg : this.rightLegs) leg.zRot = rightLegRoll;
		this.abdomen.xRot = LuminalCicadaRules.abdomenPitch(clinging, ageInTicks);
		float tailScale = entity.isFlashing() ? 1.25F : LuminalCicadaRules.tailGlowScale(ageInTicks);
		this.lantern.xScale = tailScale;
		this.lantern.yScale = tailScale;
		this.lantern.zScale = tailScale;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		this.root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
