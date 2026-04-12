package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class MyelinBorerModel extends EntityModel<MyelinBorerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("myelin_borer"), "main");

	private final ModelPart head;
	private final ModelPart segment1;
	private final ModelPart segment2;
	private final ModelPart segment3;
	private final ModelPart tail;

	public MyelinBorerModel(ModelPart root) {
		this.head = root.getChild("head");
		this.segment1 = root.getChild("segment1");
		this.segment2 = root.getChild("segment2");
		this.segment3 = root.getChild("segment3");
		this.tail = root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Grub/borer body - segmented
		partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(14, 0).addBox(-0.5F, -1.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.0F, -3.0F));

		partdefinition.addOrReplaceChild("segment1", CubeListBuilder.create()
				.texOffs(0, 6).addBox(-2.5F, -2.5F, -1.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.0F, 0.0F));

		partdefinition.addOrReplaceChild("segment2", CubeListBuilder.create()
				.texOffs(0, 13).addBox(-3.0F, -3.0F, -1.5F, 6.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.0F, 3.0F));

		partdefinition.addOrReplaceChild("segment3", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-2.5F, -2.5F, -1.5F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.0F, 6.0F));

		partdefinition.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(16, 6).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.0F, 9.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(MyelinBorerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Undulating worm-like movement
		float wave = limbSwing * 0.6662F;
		float amplitude = limbSwingAmount * 0.15F;
		this.head.yRot = (float) Math.sin(wave) * amplitude;
		this.segment1.yRot = (float) Math.sin(wave + 1.0F) * amplitude;
		this.segment2.yRot = (float) Math.sin(wave + 2.0F) * amplitude;
		this.segment3.yRot = (float) Math.sin(wave + 3.0F) * amplitude;
		this.tail.yRot = (float) Math.sin(wave + 4.0F) * amplitude;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
		segment1.render(poseStack, buffer, packedLight, packedOverlay);
		segment2.render(poseStack, buffer, packedLight, packedOverlay);
		segment3.render(poseStack, buffer, packedLight, packedOverlay);
		tail.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
