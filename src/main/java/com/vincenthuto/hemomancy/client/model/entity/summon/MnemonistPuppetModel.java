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
						.texOffs(0, 0).addBox(-3.5F, -7.5F, -3.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.05F))
						.texOffs(28, 0).addBox(-2.5F, -6.5F, -4.15F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(40, 0).addBox(-1.0F, -7.85F, -4.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(16, 16).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 12.0F, 4.0F, new CubeDeformation(-0.15F))
						.texOffs(38, 18).addBox(-4.0F, 2.0F, -2.35F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(38, 22).addBox(-4.0F, 7.0F, -2.35F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("memory_spool", CubeListBuilder.create()
						.texOffs(0, 32).addBox(-3.0F, 1.5F, 2.0F, 6.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(18, 34).addBox(-4.0F, 3.0F, 2.8F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(18, 39).addBox(-4.0F, 6.0F, 2.8F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("left_thread", CubeListBuilder.create()
						.texOffs(44, 32).addBox(3.3F, 0.5F, -2.6F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		body.addOrReplaceChild("right_thread", CubeListBuilder.create()
						.texOffs(48, 32).addBox(-4.3F, 0.5F, -2.6F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(40, 16).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F))
						.texOffs(52, 16).addBox(-2.5F, 4.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
						.texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F))
						.texOffs(52, 16).mirror().addBox(-1.5F, 4.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(0, 16).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F))
						.texOffs(28, 46).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 16).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F))
						.texOffs(28, 46).mirror().addBox(-2.0F, 3.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
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
