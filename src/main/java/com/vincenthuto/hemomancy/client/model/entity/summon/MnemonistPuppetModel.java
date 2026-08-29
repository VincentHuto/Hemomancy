package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
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

public class MnemonistPuppetModel extends HumanoidModel<MnemonistPuppetEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("mnemonist_puppet"), "main");

	private final ModelPart memorySpool;
	private final ModelPart leftThread;
	private final ModelPart rightThread;

	public MnemonistPuppetModel(ModelPart root) {
		super(root);
		this.memorySpool = this.body.getChild("memory_spool");
		this.leftThread = this.body.getChild("left_thread");
		this.rightThread = this.body.getChild("right_thread");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.9F, -7.8F, -2.6F, 5.8F, 7.0F, 5.2F, new CubeDeformation(0.05F))
						.texOffs(24, 0).addBox(-2.45F, -7.25F, -3.2F, 4.9F, 5.9F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(38, 0).addBox(-2.8F, -7.7F, -3.05F, 5.6F, 0.7F, 0.6F, new CubeDeformation(0.0F))
						.texOffs(38, 3).addBox(-2.8F, -1.8F, -3.05F, 5.6F, 0.7F, 0.6F, new CubeDeformation(0.0F))
						.texOffs(52, 0).addBox(-0.3F, -5.9F, -3.35F, 0.6F, 3.3F, 0.5F, new CubeDeformation(0.0F))
						.texOffs(55, 0).addBox(-1.65F, -4.5F, -3.35F, 3.3F, 0.6F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(0, 14).addBox(-2.8F, 0.0F, -1.7F, 5.6F, 12.0F, 3.4F, new CubeDeformation(-0.05F))
						.texOffs(20, 14).addBox(-3.5F, 0.8F, -2.0F, 7.0F, 1.2F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(20, 20).addBox(-3.35F, 3.4F, -2.05F, 6.7F, 1.0F, 4.1F, new CubeDeformation(0.0F))
						.texOffs(20, 26).addBox(-3.15F, 6.1F, -2.05F, 6.3F, 1.0F, 4.1F, new CubeDeformation(0.0F))
						.texOffs(20, 32).addBox(-2.95F, 8.8F, -2.0F, 5.9F, 1.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(44, 14).addBox(-1.0F, 1.8F, -2.25F, 2.0F, 8.0F, 0.7F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("memory_spool", CubeListBuilder.create()
						.texOffs(0, 38).addBox(-5.8F, 2.2F, 1.8F, 11.6F, 5.8F, 3.6F, new CubeDeformation(0.08F))
						.texOffs(32, 38).addBox(-3.7F, 3.1F, 1.35F, 7.4F, 4.0F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(0, 50).addBox(-6.6F, 1.5F, 2.2F, 1.4F, 7.2F, 2.8F, new CubeDeformation(0.05F))
						.texOffs(0, 50).mirror().addBox(5.2F, 1.5F, 2.2F, 1.4F, 7.2F, 2.8F, new CubeDeformation(0.05F))
						.texOffs(10, 50).addBox(-7.5F, 4.2F, 2.7F, 15.0F, 1.3F, 1.8F, new CubeDeformation(0.0F))
						.texOffs(10, 55).addBox(-4.6F, 2.4F, 5.0F, 9.2F, 1.0F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(10, 59).addBox(-4.6F, 6.8F, 5.0F, 9.2F, 1.0F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("left_thread", CubeListBuilder.create()
						.texOffs(52, 14).addBox(3.65F, -11.5F, -2.45F, 0.35F, 23.0F, 0.35F, new CubeDeformation(0.0F))
						.texOffs(56, 14).addBox(3.2F, 10.4F, -2.85F, 1.3F, 1.8F, 1.3F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("right_thread", CubeListBuilder.create()
						.texOffs(52, 14).mirror().addBox(-4.0F, -11.5F, -2.45F, 0.35F, 23.0F, 0.35F, new CubeDeformation(0.0F))
						.texOffs(56, 14).mirror().addBox(-4.5F, 10.4F, -2.85F, 1.3F, 1.8F, 1.3F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(48, 24).addBox(-1.8F, -2.0F, -1.25F, 2.5F, 12.5F, 2.5F, new CubeDeformation(-0.05F))
						.texOffs(38, 50).addBox(-2.15F, 1.5F, -1.6F, 3.2F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(38, 55).addBox(-2.15F, 6.0F, -1.6F, 3.2F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(50, 50).addBox(-1.7F, 9.5F, -1.55F, 2.4F, 3.2F, 3.1F, new CubeDeformation(0.0F))
						.texOffs(60, 50).addBox(-1.55F, 11.7F, -2.9F, 0.6F, 2.6F, 0.6F, new CubeDeformation(0.0F))
						.texOffs(60, 50).addBox(-0.45F, 11.7F, -3.1F, 0.6F, 2.8F, 0.6F, new CubeDeformation(0.0F)),
				PartPose.offset(-4.2F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
						.texOffs(48, 24).mirror().addBox(-0.7F, -2.0F, -1.25F, 2.5F, 12.5F, 2.5F, new CubeDeformation(-0.05F))
						.texOffs(38, 50).mirror().addBox(-1.05F, 1.5F, -1.6F, 3.2F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(38, 55).mirror().addBox(-1.05F, 6.0F, -1.6F, 3.2F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(50, 50).mirror().addBox(-0.7F, 9.5F, -1.55F, 2.4F, 3.2F, 3.1F, new CubeDeformation(0.0F))
						.texOffs(60, 50).mirror().addBox(0.95F, 11.7F, -2.9F, 0.6F, 2.6F, 0.6F, new CubeDeformation(0.0F))
						.texOffs(60, 50).mirror().addBox(-0.15F, 11.7F, -3.1F, 0.6F, 2.8F, 0.6F, new CubeDeformation(0.0F)),
				PartPose.offset(4.2F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(0, 26).addBox(-1.35F, 0.0F, -1.25F, 2.7F, 12.0F, 2.5F, new CubeDeformation(-0.05F))
						.texOffs(10, 26).addBox(-1.7F, 2.8F, -1.6F, 3.4F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(10, 31).addBox(-1.7F, 7.0F, -1.6F, 3.4F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(52, 38).addBox(-1.7F, 10.4F, -3.0F, 3.4F, 1.8F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 26).mirror().addBox(-1.35F, 0.0F, -1.25F, 2.7F, 12.0F, 2.5F, new CubeDeformation(-0.05F))
						.texOffs(10, 26).mirror().addBox(-1.7F, 2.8F, -1.6F, 3.4F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(10, 31).mirror().addBox(-1.7F, 7.0F, -1.6F, 3.4F, 1.4F, 3.2F, new CubeDeformation(0.0F))
						.texOffs(52, 38).mirror().addBox(-1.7F, 10.4F, -3.0F, 3.4F, 1.8F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float pulse = Mth.sin(ageInTicks * 0.16F) * 0.04F;
		this.memorySpool.zRot = pulse;
		this.leftThread.xRot = pulse * 0.5F;
		this.rightThread.xRot = -pulse * 0.5F;
	}
}
