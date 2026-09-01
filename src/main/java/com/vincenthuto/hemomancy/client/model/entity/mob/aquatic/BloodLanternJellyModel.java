package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BloodLanternJellyEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BloodLanternJellyModel extends EntityModel<BloodLanternJellyEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("blood_lantern_jelly"), "main");

	private final ModelPart bell;
	private final ModelPart glowCore;
	private final ModelPart oralArms;
	private final ModelPart tendrilNorth;
	private final ModelPart tendrilSouth;
	private final ModelPart tendrilEast;
	private final ModelPart tendrilWest;

	public BloodLanternJellyModel(ModelPart root) {
		this.bell = root.getChild("bell");
		this.glowCore = root.getChild("glowCore");
		this.oralArms = root.getChild("oralArms");
		this.tendrilNorth = root.getChild("tendrilNorth");
		this.tendrilSouth = root.getChild("tendrilSouth");
		this.tendrilEast = root.getChild("tendrilEast");
		this.tendrilWest = root.getChild("tendrilWest");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("bell", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.5F, -5.0F, -4.5F, 9.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
						.texOffs(0, 14).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("glowCore", CubeListBuilder.create()
						.texOffs(32, 14).addBox(-2.0F, -3.25F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("oralArms", CubeListBuilder.create()
						.texOffs(48, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(-0.15F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("tendrilNorth", CubeListBuilder.create()
						.texOffs(0, 24).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, -3.0F));
		root.addOrReplaceChild("tendrilSouth", CubeListBuilder.create()
						.texOffs(4, 24).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 3.0F));
		root.addOrReplaceChild("tendrilEast", CubeListBuilder.create()
						.texOffs(8, 24).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 14.0F, 0.0F));
		root.addOrReplaceChild("tendrilWest", CubeListBuilder.create()
						.texOffs(12, 24).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 14.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(BloodLanternJellyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float pulse = (float) Math.sin(ageInTicks * 0.16F) * 0.06F;
		this.bell.y = 14.0F + pulse * 6.0F;
		this.glowCore.y = 14.0F + pulse * 8.0F;
		this.glowCore.xScale = 1.0F + pulse;
		this.glowCore.zScale = 1.0F + pulse;
		float sway = (float) Math.sin(ageInTicks * 0.08F) * 0.22F;
		this.oralArms.xRot = sway * 0.35F;
		this.tendrilNorth.xRot = sway;
		this.tendrilSouth.xRot = -sway;
		this.tendrilEast.zRot = -sway * 0.85F;
		this.tendrilWest.zRot = sway * 0.85F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		this.bell.render(poseStack, buffer, packedLight, packedOverlay);
		this.glowCore.render(poseStack, buffer, packedLight, packedOverlay);
		this.oralArms.render(poseStack, buffer, packedLight, packedOverlay);
		this.tendrilNorth.render(poseStack, buffer, packedLight, packedOverlay);
		this.tendrilSouth.render(poseStack, buffer, packedLight, packedOverlay);
		this.tendrilEast.render(poseStack, buffer, packedLight, packedOverlay);
		this.tendrilWest.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
