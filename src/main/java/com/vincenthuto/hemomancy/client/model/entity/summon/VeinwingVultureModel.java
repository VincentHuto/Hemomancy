package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class VeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("veinwing_vulture"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart tail;
	private final ModelPart leftTalon;
	private final ModelPart rightTalon;

	public VeinwingVultureModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.tail = this.root.getChild("tail");
		this.leftTalon = this.root.getChild("left_talon");
		this.rightTalon = this.root.getChild("right_talon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.4F, -5.0F, -1.6F, 4.8F, 8.5F, 3.2F, new CubeDeformation(0.2F))
						.texOffs(20, 0).addBox(-3.0F, -5.6F, -1.35F, 6.0F, 2.5F, 2.7F, new CubeDeformation(0.1F))
						.texOffs(38, 0).addBox(-1.2F, -7.2F, -0.8F, 2.4F, 2.6F, 1.6F, new CubeDeformation(0.05F))
						.texOffs(48, 0).addBox(-2.0F, -4.3F, -2.05F, 4.0F, 1.0F, 0.6F, new CubeDeformation(0.0F))
						.texOffs(48, 3).addBox(-2.25F, -1.8F, -2.05F, 4.5F, 0.8F, 0.6F, new CubeDeformation(0.0F))
						.texOffs(48, 6).addBox(-1.8F, 0.6F, -2.0F, 3.6F, 0.7F, 0.55F, new CubeDeformation(0.0F))
						.texOffs(62, 0).addBox(-3.4F, -5.1F, -0.7F, 1.3F, 2.2F, 1.4F, new CubeDeformation(0.1F))
						.texOffs(62, 0).mirror().addBox(2.1F, -5.1F, -0.7F, 1.3F, 2.2F, 1.4F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 15.0F, 0.0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2.2F, -2.8F, -3.4F, 4.4F, 3.8F, 3.8F, new CubeDeformation(0.08F))
						.texOffs(18, 16).addBox(-1.75F, -2.2F, -4.2F, 3.5F, 2.3F, 1.2F, new CubeDeformation(0.03F))
						.texOffs(30, 16).addBox(-1.15F, -0.55F, -8.0F, 2.3F, 1.8F, 4.2F, new CubeDeformation(0.02F))
						.texOffs(44, 16).addBox(-1.75F, 0.55F, -6.8F, 3.5F, 0.8F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(58, 16).addBox(-2.8F, -2.6F, -3.8F, 1.4F, 1.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(58, 16).mirror().addBox(1.4F, -2.6F, -3.8F, 1.4F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -6.0F, -0.8F));
		head.addOrReplaceChild("beak_hook", CubeListBuilder.create()
						.texOffs(70, 16).addBox(-0.75F, -0.2F, -2.4F, 1.5F, 1.3F, 2.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.65F, -7.8F, -0.42F, 0.0F, 0.0F));

		PartDefinition leftWing = root.addOrReplaceChild("left_wing", CubeListBuilder.create()
						.texOffs(0, 28).addBox(-0.8F, -1.3F, -1.3F, 3.0F, 2.6F, 2.6F, new CubeDeformation(0.05F))
						.texOffs(14, 28).addBox(1.0F, -0.65F, -0.65F, 5.5F, 1.3F, 1.3F, new CubeDeformation(0.0F))
						.texOffs(30, 28).addBox(4.1F, -1.05F, -1.05F, 1.6F, 2.1F, 2.1F, new CubeDeformation(0.05F)),
				PartPose.offset(2.0F, -4.0F, 0.0F));
		PartDefinition leftForewing = leftWing.addOrReplaceChild("forewing", CubeListBuilder.create()
						.texOffs(0, 34).addBox(0.0F, -0.55F, -0.55F, 8.5F, 1.1F, 1.1F, new CubeDeformation(0.0F))
						.texOffs(0, 38).addBox(0.4F, 0.2F, -0.35F, 3.0F, 3.6F, 0.7F, new CubeDeformation(0.0F))
						.texOffs(16, 38).addBox(3.2F, 0.2F, -0.35F, 2.9F, 5.5F, 0.7F, new CubeDeformation(0.0F))
						.texOffs(32, 38).addBox(5.9F, 0.2F, -0.35F, 2.5F, 6.8F, 0.7F, new CubeDeformation(0.0F))
						.texOffs(48, 34).addBox(2.6F, -0.15F, -0.55F, 0.8F, 5.0F, 1.1F, new CubeDeformation(0.0F))
						.texOffs(54, 34).addBox(5.6F, -0.15F, -0.55F, 0.8F, 7.0F, 1.1F, new CubeDeformation(0.0F))
						.texOffs(60, 34).addBox(8.0F, -0.1F, -0.5F, 0.7F, 7.8F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.2F, 0.0F, 0.0F, 0.0F, -0.08F, -0.32F));
		leftForewing.addOrReplaceChild("outer_hook", CubeListBuilder.create()
						.texOffs(68, 34).addBox(-0.45F, -0.4F, -0.4F, 0.9F, 4.6F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(8.35F, 7.1F, 0.0F, 0.0F, 0.0F, -0.32F));

		PartDefinition rightWing = root.addOrReplaceChild("right_wing", CubeListBuilder.create()
						.texOffs(0, 28).mirror().addBox(-2.2F, -1.3F, -1.3F, 3.0F, 2.6F, 2.6F, new CubeDeformation(0.05F))
						.texOffs(14, 28).mirror().addBox(-6.5F, -0.65F, -0.65F, 5.5F, 1.3F, 1.3F, new CubeDeformation(0.0F))
						.texOffs(30, 28).mirror().addBox(-5.7F, -1.05F, -1.05F, 1.6F, 2.1F, 2.1F, new CubeDeformation(0.05F)),
				PartPose.offset(-2.0F, -4.0F, 0.0F));
		PartDefinition rightForewing = rightWing.addOrReplaceChild("forewing", CubeListBuilder.create()
						.texOffs(0, 34).mirror().addBox(-8.5F, -0.55F, -0.55F, 8.5F, 1.1F, 1.1F, new CubeDeformation(0.0F))
						.texOffs(0, 38).mirror().addBox(-3.4F, 0.2F, -0.35F, 3.0F, 3.6F, 0.7F, new CubeDeformation(0.0F))
						.texOffs(16, 38).mirror().addBox(-6.1F, 0.2F, -0.35F, 2.9F, 5.5F, 0.7F, new CubeDeformation(0.0F))
						.texOffs(32, 38).mirror().addBox(-8.4F, 0.2F, -0.35F, 2.5F, 6.8F, 0.7F, new CubeDeformation(0.0F))
						.texOffs(48, 34).mirror().addBox(-3.4F, -0.15F, -0.55F, 0.8F, 5.0F, 1.1F, new CubeDeformation(0.0F))
						.texOffs(54, 34).mirror().addBox(-6.4F, -0.15F, -0.55F, 0.8F, 7.0F, 1.1F, new CubeDeformation(0.0F))
						.texOffs(60, 34).mirror().addBox(-8.7F, -0.1F, -0.5F, 0.7F, 7.8F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-5.2F, 0.0F, 0.0F, 0.0F, 0.08F, 0.32F));
		rightForewing.addOrReplaceChild("outer_hook", CubeListBuilder.create()
						.texOffs(68, 34).mirror().addBox(-0.45F, -0.4F, -0.4F, 0.9F, 4.6F, 0.9F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-8.35F, 7.1F, 0.0F, 0.0F, 0.0F, 0.32F));

		PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(80, 0).addBox(-0.7F, 0.0F, -0.7F, 1.4F, 4.8F, 1.4F, new CubeDeformation(0.0F))
						.texOffs(86, 0).addBox(-1.1F, 1.0F, -1.0F, 2.2F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 3.0F, 1.0F));
		PartDefinition tailCord = tail.addOrReplaceChild("cord", CubeListBuilder.create()
						.texOffs(96, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.5F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(102, 0).addBox(-0.8F, 5.2F, -0.8F, 1.6F, 2.2F, 1.6F, new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.18F, 0.0F, 0.0F));
		tailCord.addOrReplaceChild("needle", CubeListBuilder.create()
						.texOffs(110, 0).addBox(-0.35F, 0.0F, -0.35F, 0.7F, 3.2F, 0.7F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 6.7F, 0.0F, 0.3F, 0.0F, 0.0F));

		root.addOrReplaceChild("left_talon", CubeListBuilder.create()
						.texOffs(80, 14).addBox(-0.65F, 0.0F, -0.65F, 1.3F, 4.0F, 1.3F, new CubeDeformation(0.0F))
						.texOffs(88, 14).addBox(-0.8F, 3.2F, -3.8F, 0.7F, 0.8F, 3.8F, new CubeDeformation(0.0F))
						.texOffs(88, 14).addBox(0.1F, 3.2F, -3.8F, 0.7F, 0.8F, 3.8F, new CubeDeformation(0.0F))
						.texOffs(104, 14).addBox(-0.35F, 3.4F, 0.0F, 0.7F, 0.7F, 2.8F, new CubeDeformation(0.0F)),
				PartPose.offset(1.2F, 3.0F, -0.4F));

		root.addOrReplaceChild("right_talon", CubeListBuilder.create()
						.texOffs(80, 14).mirror().addBox(-0.65F, 0.0F, -0.65F, 1.3F, 4.0F, 1.3F, new CubeDeformation(0.0F))
						.texOffs(88, 14).mirror().addBox(-0.8F, 3.2F, -3.8F, 0.7F, 0.8F, 3.8F, new CubeDeformation(0.0F))
						.texOffs(88, 14).mirror().addBox(0.1F, 3.2F, -3.8F, 0.7F, 0.8F, 3.8F, new CubeDeformation(0.0F))
						.texOffs(104, 14).mirror().addBox(-0.35F, 3.4F, 0.0F, 0.7F, 0.7F, 2.8F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.2F, 3.0F, -0.4F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(VeinwingVultureEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		float flap = Mth.sin(ageInTicks * 0.55F) * 0.45F;
		this.root.y = 15.0F + Mth.sin(ageInTicks * 0.16F) * 0.6F;
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.7F;
		this.leftWing.zRot = 0.25F + flap;
		this.rightWing.zRot = -0.25F - flap;
		this.leftWing.yRot = -0.18F;
		this.rightWing.yRot = 0.18F;
		this.tail.xRot = 0.18F + Mth.sin(ageInTicks * 0.22F) * 0.08F;
		this.leftTalon.xRot = Mth.cos(ageInTicks * 0.18F) * 0.12F;
		this.rightTalon.xRot = -this.leftTalon.xRot;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
