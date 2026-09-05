package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public final class PeacockSpiderModel extends HierarchicalModel<PeacockSpiderEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("peacock_spider"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart fan;
	private final ModelPart[] legs;

	public PeacockSpiderModel(ModelPart bakedRoot) {
		root = bakedRoot.getChild("root");
		head = root.getChild("head");
		fan = root.getChild("fan");
		legs = new ModelPart[] {
				root.getChild("left_front_leg"), root.getChild("right_front_leg"),
				root.getChild("left_mid_front_leg"), root.getChild("right_mid_front_leg"),
				root.getChild("left_mid_hind_leg"), root.getChild("right_mid_hind_leg"),
				root.getChild("left_hind_leg"), root.getChild("right_hind_leg")
		};
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition meshRoot = mesh.getRoot();
		PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
		root.addOrReplaceChild("thorax", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -2.5F, -5.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 11).addBox(-2.5F, -2.0F, -4.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 31).addBox(-2.0F, -1.6F, -4.6F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 31).addBox(1.0F, -1.6F, -4.6F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.0F));
		head.addOrReplaceChild("left_pedipalp", CubeListBuilder.create().texOffs(50, 11).addBox(-2.0F, -0.5F, -3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, -3.5F));
		head.addOrReplaceChild("right_pedipalp", CubeListBuilder.create().texOffs(50, 11).addBox(0.0F, -0.5F, -3.0F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, -3.5F));
		PartDefinition fan = root.addOrReplaceChild("fan", CubeListBuilder.create().texOffs(0, 21).addBox(-6.0F, -8.0F, -0.5F, 12.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 7.0F, -0.12F, 0.0F, 0.0F));
		fan.addOrReplaceChild("fan_crown", CubeListBuilder.create().texOffs(28, 21).addBox(-3.0F, -3.0F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));
		addLeg(root, "left_front_leg", -3.0F, -3.5F, -0.55F, -0.55F, false);
		addLeg(root, "right_front_leg", 3.0F, -3.5F, 0.55F, 0.55F, true);
		addLeg(root, "left_mid_front_leg", -3.5F, -1.0F, -0.2F, -0.75F, false);
		addLeg(root, "right_mid_front_leg", 3.5F, -1.0F, 0.2F, 0.75F, true);
		addLeg(root, "left_mid_hind_leg", -3.5F, 2.0F, 0.2F, -0.75F, false);
		addLeg(root, "right_mid_hind_leg", 3.5F, 2.0F, -0.2F, 0.75F, true);
		addLeg(root, "left_hind_leg", -3.0F, 4.5F, 0.55F, -0.55F, false);
		addLeg(root, "right_hind_leg", 3.0F, 4.5F, -0.55F, 0.55F, true);
		return LayerDefinition.create(mesh, 64, 64);
	}

	private static void addLeg(PartDefinition root, String name, float x, float z, float yRot, float zRot, boolean right) {
		float innerX = right ? 0.0F : -7.0F;
		PartDefinition upper = root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 35).addBox(innerX, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(x, 0.0F, z, 0.0F, yRot, zRot));
		upper.addOrReplaceChild("lower", CubeListBuilder.create().texOffs(0, 39).addBox(innerX, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(right ? 7.0F : -7.0F, 0.0F, 0.0F, 0.0F, 0.0F, right ? -0.65F : 0.65F));
	}

	@Override public ModelPart root() { return root; }

	@Override
	public void setupAnim(PeacockSpiderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.35F;
		head.xRot = headPitch * Mth.DEG_TO_RAD * 0.25F;
		fan.xRot = -0.12F + Mth.sin(ageInTicks * 0.12F) * 0.05F;
		float stride = Mth.clamp(limbSwingAmount * 1.8F, 0.0F, 1.0F);
		for (int i = 0; i < legs.length; i++) {
			float step = Mth.cos(limbSwing * 1.7F + (i / 2 % 2) * Mth.PI) * 0.45F * stride;
			legs[i].yRot += i % 2 == 0 ? step : -step;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
