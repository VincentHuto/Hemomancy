package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BrinedVotaryEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class BrinedVotaryModel<T extends BrinedVotaryEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("brined_votary"), "main");

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
						.texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F,
								new CubeDeformation(0.1F)),
				PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(16, 18).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F,
								new CubeDeformation(0.25F))
						.texOffs(0, 36).addBox(-3.0F, 2.0F, 2.2F, 6.0F, 9.0F, 2.0F,
								new CubeDeformation(0.0F))
						.texOffs(34, 36).addBox(-5.0F, 8.0F, -2.5F, 10.0F, 8.0F, 5.0F,
								new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("right_arm",
				CubeListBuilder.create()
						.texOffs(40, 18).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.2F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm",
				CubeListBuilder.create()
						.texOffs(40, 18).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.2F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg",
				CubeListBuilder.create()
						.texOffs(0, 18).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.1F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg",
				CubeListBuilder.create()
						.texOffs(0, 18).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.1F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	public BrinedVotaryModel(ModelPart root) {
		super(root);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		if (entity.isInWaterOrBubble()) {
			float sway = Mth.sin(ageInTicks * 0.16F) * 0.18F;
			this.rightArm.xRot = -0.65F + sway;
			this.leftArm.xRot = -0.65F - sway;
			this.rightLeg.xRot *= 0.35F;
			this.leftLeg.xRot *= 0.35F;
			this.body.xRot = Mth.sin(ageInTicks * 0.08F) * 0.04F;
		}
	}
}
