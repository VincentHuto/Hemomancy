package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
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
				.texOffs(0, 0).addBox(-2.5F, -8.4F, -2.1F, 5.0F, 7.5F, 4.2F, new CubeDeformation(0.05F))
				.texOffs(20, 0).addBox(-2.15F, -7.9F, -2.7F, 4.3F, 6.4F, 0.8F, new CubeDeformation(0.0F))
				.texOffs(36, 0).addBox(-1.6F, -5.8F, -2.95F, 1.1F, 0.45F, 0.45F, new CubeDeformation(0.0F))
				.texOffs(36, 0).mirror().addBox(0.5F, -5.8F, -2.95F, 1.1F, 0.45F, 0.45F, new CubeDeformation(0.0F))
				.texOffs(42, 0).addBox(-2.7F, -8.3F, -2.45F, 5.4F, 0.65F, 0.7F, new CubeDeformation(0.0F))
				.texOffs(42, 3).addBox(-2.7F, -1.8F, -2.45F, 5.4F, 0.65F, 0.7F, new CubeDeformation(0.0F)), PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-2.15F, 0.0F, -1.4F, 4.3F, 12.0F, 2.8F, new CubeDeformation(-0.05F))
				.texOffs(18, 14).addBox(-2.8F, 0.8F, -1.8F, 5.6F, 1.2F, 3.6F, new CubeDeformation(0.0F))
				.texOffs(18, 20).addBox(-2.65F, 4.0F, -1.85F, 5.3F, 0.8F, 3.7F, new CubeDeformation(0.0F))
				.texOffs(18, 25).addBox(-2.45F, 7.2F, -1.8F, 4.9F, 0.8F, 3.6F, new CubeDeformation(0.0F))
				.texOffs(18, 30).addBox(-2.25F, 10.1F, -1.7F, 4.5F, 0.8F, 3.4F, new CubeDeformation(0.0F))
				.texOffs(42, 14).addBox(-0.45F, 1.2F, -2.1F, 0.9F, 9.5F, 0.7F, new CubeDeformation(0.0F)), PartPose.ZERO);
		PartDefinition leftGorget = body.addOrReplaceChild("left_gorget", CubeListBuilder.create()
				.texOffs(0, 36).addBox(0.0F, -5.5F, -0.8F, 6.4F, 9.5F, 1.6F, new CubeDeformation(0.03F))
				.texOffs(20, 36).addBox(0.3F, -5.8F, -1.15F, 0.8F, 10.0F, 2.3F, new CubeDeformation(0.0F))
				.texOffs(28, 36).addBox(5.5F, -4.5F, -1.05F, 0.8F, 8.0F, 2.1F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 1.5F, 1.0F, 0.0F, -0.08F, -0.08F));
		leftGorget.addOrReplaceChild("upper_fan", CubeListBuilder.create()
				.texOffs(34, 36).addBox(-0.2F, -5.4F, -0.8F, 5.4F, 7.2F, 1.6F, new CubeDeformation(0.02F))
				.texOffs(52, 36).addBox(4.5F, -5.2F, -1.0F, 0.7F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.4F, -3.5F, 0.0F, 0.0F, 0.0F, -0.55F));
		leftGorget.addOrReplaceChild("lower_fan", CubeListBuilder.create()
				.texOffs(34, 48).addBox(-0.2F, -1.0F, -0.7F, 5.0F, 5.8F, 1.4F, new CubeDeformation(0.02F))
				.texOffs(52, 48).addBox(4.1F, -0.8F, -0.9F, 0.7F, 5.4F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.5F, 2.8F, 0.0F, 0.0F, 0.0F, 0.45F));
		PartDefinition rightGorget = body.addOrReplaceChild("right_gorget", CubeListBuilder.create()
				.texOffs(0, 36).mirror().addBox(-6.4F, -5.5F, -0.8F, 6.4F, 9.5F, 1.6F, new CubeDeformation(0.03F))
				.texOffs(20, 36).mirror().addBox(-1.1F, -5.8F, -1.15F, 0.8F, 10.0F, 2.3F, new CubeDeformation(0.0F))
				.texOffs(28, 36).mirror().addBox(-6.3F, -4.5F, -1.05F, 0.8F, 8.0F, 2.1F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5F, 1.5F, 1.0F, 0.0F, 0.08F, 0.08F));
		rightGorget.addOrReplaceChild("upper_fan", CubeListBuilder.create()
				.texOffs(34, 36).mirror().addBox(-5.2F, -5.4F, -0.8F, 5.4F, 7.2F, 1.6F, new CubeDeformation(0.02F))
				.texOffs(52, 36).mirror().addBox(-5.2F, -5.2F, -1.0F, 0.7F, 7.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.4F, -3.5F, 0.0F, 0.0F, 0.0F, 0.55F));
		rightGorget.addOrReplaceChild("lower_fan", CubeListBuilder.create()
				.texOffs(34, 48).mirror().addBox(-4.8F, -1.0F, -0.7F, 5.0F, 5.8F, 1.4F, new CubeDeformation(0.02F))
				.texOffs(52, 48).mirror().addBox(-4.8F, -0.8F, -0.9F, 0.7F, 5.4F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.5F, 2.8F, 0.0F, 0.0F, 0.0F, -0.45F));

		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 14).addBox(-1.3F, -2.0F, -0.85F, 1.8F, 13.5F, 1.7F, new CubeDeformation(-0.03F))
				.texOffs(8, 14).addBox(-1.65F, 2.2F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(8, 19).addBox(-1.65F, 7.0F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(0, 55).addBox(-1.2F, 10.7F, -1.1F, 1.6F, 3.0F, 2.2F, new CubeDeformation(0.0F))
				.texOffs(8, 55).addBox(-1.1F, 13.0F, -2.2F, 0.45F, 3.3F, 0.45F, new CubeDeformation(0.0F))
				.texOffs(8, 55).addBox(-0.2F, 13.0F, -2.5F, 0.45F, 3.6F, 0.45F, new CubeDeformation(0.0F)), PartPose.offset(-3.2F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(0, 14).mirror().addBox(-0.5F, -2.0F, -0.85F, 1.8F, 13.5F, 1.7F, new CubeDeformation(-0.03F))
				.texOffs(8, 14).mirror().addBox(-0.85F, 2.2F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(8, 19).mirror().addBox(-0.85F, 7.0F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(0, 55).mirror().addBox(-0.4F, 10.7F, -1.1F, 1.6F, 3.0F, 2.2F, new CubeDeformation(0.0F))
				.texOffs(8, 55).mirror().addBox(0.65F, 13.0F, -2.2F, 0.45F, 3.3F, 0.45F, new CubeDeformation(0.0F))
				.texOffs(8, 55).mirror().addBox(-0.25F, 13.0F, -2.5F, 0.45F, 3.6F, 0.45F, new CubeDeformation(0.0F)), PartPose.offset(3.2F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 26).addBox(-0.9F, 0.0F, -0.85F, 1.8F, 12.0F, 1.7F, new CubeDeformation(-0.03F))
				.texOffs(8, 26).addBox(-1.25F, 3.0F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(8, 31).addBox(-1.25F, 7.3F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(18, 55).addBox(-1.25F, 10.6F, -3.2F, 2.5F, 1.7F, 3.8F, new CubeDeformation(0.0F)), PartPose.offset(-1.2F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(0, 26).mirror().addBox(-0.9F, 0.0F, -0.85F, 1.8F, 12.0F, 1.7F, new CubeDeformation(-0.03F))
				.texOffs(8, 26).mirror().addBox(-1.25F, 3.0F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(8, 31).mirror().addBox(-1.25F, 7.3F, -1.2F, 2.5F, 1.2F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(18, 55).mirror().addBox(-1.25F, 10.6F, -3.2F, 2.5F, 1.7F, 3.8F, new CubeDeformation(0.0F)), PartPose.offset(1.2F, 12.0F, 0.0F));
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
