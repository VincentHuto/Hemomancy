package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_veinwing_vulture.py; regenerate the model, atlas and BBModel together.
public class VeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "veinwing_vulture"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart leftForewing;
	private final ModelPart rightForewing;
	private final ModelPart tail;
	private final ModelPart leftTalon;
	private final ModelPart rightTalon;

	public VeinwingVultureModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.leftForewing = this.leftWing.getChild("left_forewing");
		this.rightForewing = this.rightWing.getChild("right_forewing");
		this.tail = this.root.getChild("tail");
		this.leftTalon = this.root.getChild("left_talon");
		this.rightTalon = this.root.getChild("right_talon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition torso = root.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 7.0F, 4.0F).texOffs(17, 0).addBox(-3.0F, -7.0F, 0.0F, 6.0F, 3.0F, 3.0F).texOffs(36, 0).addBox(-0.5F, -5.0F, -3.0F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition ruff = root.addOrReplaceChild("ruff", CubeListBuilder.create().texOffs(41, 0).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 2.0F, 5.0F).texOffs(62, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, -6.0F, -0.5F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(79, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F).texOffs(88, 0).addBox(-1.5F, -5.0F, -3.0F, 3.0F, 3.0F, 3.0F).texOffs(101, 0).addBox(-1.0F, -4.0F, -5.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -7.0F, -2.5F));
        PartDefinition beak_hook = head.addOrReplaceChild("beak_hook", CubeListBuilder.create().texOffs(110, 0).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -2.5F, -5.0F, 0.2792526803190927F, 0.0F, 0.0F));
        PartDefinition left_wing = root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(115, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(0, 12).addBox(0.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F).texOffs(15, 12).addBox(1.0F, 0.5F, 0.0F, 6.0F, 5.0F, 1.0F), PartPose.offset(2.5F, -4.0F, 0.0F));
        PartDefinition left_forewing = left_wing.addOrReplaceChild("left_forewing", CubeListBuilder.create().texOffs(30, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(39, 12).addBox(0.0F, -0.5F, -0.5F, 9.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, -0.10471975511965978F, -0.29670597283903605F));
        PartDefinition left_membrane_0 = left_forewing.addOrReplaceChild("left_membrane_0", CubeListBuilder.create().texOffs(60, 12).addBox(0.0F, 0.0F, 0.0F, 3.0F, 6.0F, 1.0F).texOffs(69, 12).addBox(2.0F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.08726646259971647F));
        PartDefinition left_membrane_1 = left_forewing.addOrReplaceChild("left_membrane_1", CubeListBuilder.create().texOffs(74, 12).addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F).texOffs(83, 12).addBox(2.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(3.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.19198621771937624F));
        PartDefinition left_membrane_2 = left_forewing.addOrReplaceChild("left_membrane_2", CubeListBuilder.create().texOffs(88, 12).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 1.0F).texOffs(97, 12).addBox(2.0F, -0.5F, -0.5F, 1.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(6.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.29670597283903605F));
        PartDefinition left_talon = root.addOrReplaceChild("left_talon", CubeListBuilder.create().texOffs(102, 12).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(107, 12).addBox(-1.5F, 3.0F, -2.5F, 1.0F, 1.0F, 3.0F).texOffs(116, 12).addBox(0.5F, 3.0F, -2.5F, 1.0F, 1.0F, 3.0F).texOffs(0, 25).addBox(-0.5F, 3.0F, 0.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(1.25F, 2.0F, 0.0F));
        PartDefinition right_wing = root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(7, 25).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(16, 25).addBox(-6.0F, -0.5F, -0.5F, 6.0F, 1.0F, 1.0F).texOffs(31, 25).addBox(-7.0F, 0.5F, 0.0F, 6.0F, 5.0F, 1.0F), PartPose.offset(-2.5F, -4.0F, 0.0F));
        PartDefinition right_forewing = right_wing.addOrReplaceChild("right_forewing", CubeListBuilder.create().texOffs(46, 25).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(55, 25).addBox(-9.0F, -0.5F, -0.5F, 9.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.10471975511965978F, 0.29670597283903605F));
        PartDefinition right_membrane_0 = right_forewing.addOrReplaceChild("right_membrane_0", CubeListBuilder.create().texOffs(76, 25).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 6.0F, 1.0F).texOffs(85, 25).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.08726646259971647F));
        PartDefinition right_membrane_1 = right_forewing.addOrReplaceChild("right_membrane_1", CubeListBuilder.create().texOffs(90, 25).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F).texOffs(99, 25).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.19198621771937624F));
        PartDefinition right_membrane_2 = right_forewing.addOrReplaceChild("right_membrane_2", CubeListBuilder.create().texOffs(104, 25).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 10.0F, 1.0F).texOffs(113, 25).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(-6.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.29670597283903605F));
        PartDefinition right_talon = root.addOrReplaceChild("right_talon", CubeListBuilder.create().texOffs(118, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(0, 38).addBox(-1.5F, 3.0F, -2.5F, 1.0F, 1.0F, 3.0F).texOffs(9, 38).addBox(0.5F, 3.0F, -2.5F, 1.0F, 1.0F, 3.0F).texOffs(18, 38).addBox(-0.5F, 3.0F, 0.5F, 1.0F, 1.0F, 2.0F), PartPose.offset(-1.25F, 2.0F, 0.0F));
        PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(25, 38).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 2.0F, 2.0F));
        PartDefinition tail_cord = tail.addOrReplaceChild("tail_cord", CubeListBuilder.create().texOffs(36, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, 0.19198621771937624F, 0.0F, 0.0F));
        PartDefinition tail_needle = tail_cord.addOrReplaceChild("tail_needle", CubeListBuilder.create().texOffs(41, 38).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, 0.29670597283903605F, 0.0F, 0.0F));
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
		this.leftForewing.zRot = -0.3F - flap * 0.35F;
		this.rightForewing.zRot = 0.3F + flap * 0.35F;
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
