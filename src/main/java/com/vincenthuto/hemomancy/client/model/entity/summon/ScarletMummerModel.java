package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ScarletMummerModel extends HumanoidModel<ScarletMummerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("scarlet_mummer"), "main");

	private final ModelPart leftGorget;
	private final ModelPart rightGorget;

	public ScarletMummerModel(ModelPart root) {
		super(root);
		leftGorget = body.getChild("left_gorget");
		rightGorget = body.getChild("right_gorget");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -8.0F, -2.5F, 6.0F, 7.0F, 5.0F)
				.texOffs(22, 0).addBox(-2.5F, -7.0F, -3.05F, 5.0F, 5.0F, 1.0F), PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16, 16).addBox(-2.5F, 0.0F, -1.5F, 5.0F, 12.0F, 3.0F)
				.texOffs(34, 16).addBox(-3.5F, 2.0F, -2.0F, 7.0F, 2.0F, 4.0F), PartPose.ZERO);
		body.addOrReplaceChild("left_gorget", CubeListBuilder.create()
				.texOffs(32, 24).addBox(0.0F, -4.0F, -1.0F, 7.0F, 9.0F, 2.0F),
				PartPose.offsetAndRotation(2.0F, 2.0F, 1.0F, 0.0F, -0.08F, -0.08F));
		body.addOrReplaceChild("right_gorget", CubeListBuilder.create()
				.texOffs(32, 24).mirror().addBox(-7.0F, -4.0F, -1.0F, 7.0F, 9.0F, 2.0F),
				PartPose.offsetAndRotation(-2.0F, 2.0F, 1.0F, 0.0F, 0.08F, 0.08F));

		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-1.5F, -2.0F, -1.0F, 2.0F, 13.0F, 2.0F)
				.texOffs(8, 17).addBox(-2.0F, 4.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-3.5F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(0, 16).mirror().addBox(-0.5F, -2.0F, -1.0F, 2.0F, 13.0F, 2.0F)
				.texOffs(8, 17).mirror().addBox(-1.0F, 4.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(3.5F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 32).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F)
				.texOffs(8, 32).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(-1.4F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(0, 32).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F)
				.texOffs(8, 32).mirror().addBox(-1.5F, 4.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(1.4F, 12.0F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(ScarletMummerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float flare = entity.isPerforming() ? 0.34F + Mth.sin(ageInTicks * 0.3F) * 0.05F : 0.08F;
		leftGorget.zRot = -flare;
		rightGorget.zRot = flare;
		if (entity.isPerforming()) {
			body.yRot = Mth.sin(ageInTicks * 0.22F) * 0.08F;
			leftArm.zRot -= 0.35F;
			rightArm.zRot += 0.35F;
		}
	}
}
