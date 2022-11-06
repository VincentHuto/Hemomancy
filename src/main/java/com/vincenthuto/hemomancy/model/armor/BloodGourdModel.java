package com.vincenthuto.hemomancy.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class BloodGourdModel<T extends LivingEntity> extends EntityModel<T> {

	public static final ModelLayerLocation blood_gourd = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_gourd"), "main");

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition gourd = body.addOrReplaceChild("gourd",
				CubeListBuilder.create().texOffs(0, 12)
						.addBox(-1.5F, -2.7222F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-1.5F, -0.7222F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
						.addBox(1.15F, -0.6722F, -1.5F, 1.0F, 4.0F, 3.0F, new CubeDeformation(-0.375F)).texOffs(9, 14)
						.addBox(-2.1F, -0.6722F, -1.5F, 1.0F, 4.0F, 3.0F, new CubeDeformation(-0.375F)).texOffs(18, 0)
						.addBox(-1.5F, -0.6722F, 1.1F, 3.0F, 4.0F, 1.0F, new CubeDeformation(-0.375F)).texOffs(17, 14)
						.addBox(-1.5F, -0.6722F, -2.1F, 3.0F, 4.0F, 1.0F, new CubeDeformation(-0.375F)).texOffs(9, 9)
						.addBox(-1.5F, -3.9722F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)).texOffs(9, 4)
						.addBox(-1.5F, 2.0278F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)).texOffs(0, 7)
						.addBox(-1.5F, -2.2222F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(5.75F, 12.5722F, 0.25F, 0.7418F, 0.0F, 0.0F));

		PartDefinition rope = gourd.addOrReplaceChild("rope", CubeListBuilder.create().texOffs(9, 0).addBox(1.4F,
				-0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.05F)),
				PartPose.offset(-0.1F, -0.8389F, 0.0333F));

		PartDefinition bone = rope.addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(9, 1).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
						new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(1.3F, -0.0333F, 1.0667F, 0.0F, 0.3927F, 0.0F));

		PartDefinition bone2 = rope.addOrReplaceChild("bone2",
				CubeListBuilder.create().texOffs(9, 0).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
						new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(1.3F, -0.0333F, -1.0333F, -0.0319F, 0.0064F, 0.0646F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	private final ModelPart body;

	public BloodGourdModel(ModelPart root) {
		this.body = root.getChild("body");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

	}

}