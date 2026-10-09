package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusBeastTamerModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_beast_tamer"), "main");
	private static final int LASH_SEGMENTS = 3;
	private final ModelPart whipCoil;
	private final ModelPart whipHandle;
	private final ModelPart[] lash = new ModelPart[LASH_SEGMENTS];
	private final ModelPart rightCoatTail;
	private final ModelPart leftCoatTail;

	public CircusBeastTamerModel(ModelPart root) {
		super(root);
		whipCoil = body.getChild("whip_coil");
		whipHandle = rightArm.getChild("whip_handle");
		ModelPart segment = whipHandle;
		for (int i = 0; i < LASH_SEGMENTS; i++) {
			segment = segment.getChild("whip_lash_" + i);
			lash[i] = segment;
		}
		rightCoatTail = body.getChild("right_coat_tail");
		leftCoatTail = body.getChild("left_coat_tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition hat = mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition mantle = body.addOrReplaceChild("mantle", CubeListBuilder.create().texOffs(58, 0).addBox(-5.0F, -0.5F, -3.0F, 10.0F, 3.0F, 6.0F).texOffs(91, 0).addBox(-3.0F, 2.5F, 2.0F, 6.0F, 2.0F, 1.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 17).addBox(-4.5F, 7.0F, -2.5F, 9.0F, 1.5F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition whip_coil = body.addOrReplaceChild("whip_coil", CubeListBuilder.create().texOffs(29, 17).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 1.0F), PartPose.offset(2.5F, 9.0F, -2.5F));
        PartDefinition right_coat_tail = body.addOrReplaceChild("right_coat_tail", CubeListBuilder.create().texOffs(38, 17).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(-2.0F, 11.0F, 2.5F, 0.13962634015954636F, 0.0F, 0.06981317007977318F));
        PartDefinition left_coat_tail = body.addOrReplaceChild("left_coat_tail", CubeListBuilder.create().texOffs(49, 17).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(2.0F, 11.0F, 2.5F, 0.13962634015954636F, 0.0F, -0.06981317007977318F));
        PartDefinition right_arm = mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 17).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition right_epaulette = right_arm.addOrReplaceChild("right_epaulette", CubeListBuilder.create().texOffs(77, 17).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 1.5F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition whip_handle = right_arm.addOrReplaceChild("whip_handle", CubeListBuilder.create().texOffs(98, 17).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offset(-1.0F, 10.0F, 0.0F));
        PartDefinition whip_lash_0 = whip_handle.addOrReplaceChild("whip_lash_0", CubeListBuilder.create().texOffs(103, 17).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition whip_lash_1 = whip_lash_0.addOrReplaceChild("whip_lash_1", CubeListBuilder.create().texOffs(108, 17).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition whip_lash_2 = whip_lash_1.addOrReplaceChild("whip_lash_2", CubeListBuilder.create().texOffs(113, 17).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_arm = mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 34).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition left_epaulette = left_arm.addOrReplaceChild("left_epaulette", CubeListBuilder.create().texOffs(17, 34).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 1.5F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(38, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F).texOffs(55, 34).addBox(-2.0F, 6.0F, -2.5F, 4.0F, 6.0F, 4.5F), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(74, 34).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F).texOffs(91, 34).addBox(-2.0F, 6.0F, -2.5F, 4.0F, 6.0F, 4.5F), PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		float flare = limbSwingAmount * 0.45F + Mth.sin(ageInTicks * 0.08F) * 0.03F;
		rightCoatTail.xRot = rightCoatTail.getInitialPose().xRot + flare;
		leftCoatTail.xRot = leftCoatTail.getInitialPose().xRot + flare;
		boolean act = onStage(state);
		whipCoil.visible = !act;
		whipHandle.visible = act;
		if (!act) return;
		boolean crack = state == ActState.PERFORM;
		rightArm.xRot = crack ? -2.6F + Mth.sin(ageInTicks * 0.35F) * 0.25F : -1.9F;
		rightArm.yRot = 0.0F;
		rightArm.zRot = 0.0F;
		// The free hand stays open toward the beast.
		leftArm.xRot = -0.5F;
		leftArm.zRot = -0.25F;
		head.xRot = -0.1F;
		for (int i = 0; i < LASH_SEGMENTS; i++) {
			lash[i].xRot = crack ? 0.35F + Mth.sin(ageInTicks * 0.35F - i * 0.9F) * 0.55F : 0.25F;
		}
	}
}
