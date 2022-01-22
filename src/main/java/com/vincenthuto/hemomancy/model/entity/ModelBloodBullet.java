package com.vincenthuto.hemomancy.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodBullet;

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

public class ModelBloodBullet extends EntityModel<EntityBloodBullet> {
	
	public static final ModelLayerLocation blood_bullet = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "blood_bullet"), "main");

	private final ModelPart head;

	public ModelBloodBullet(ModelPart root) {
		this.head = root.getChild("head");
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 9)
						.addBox(1.0F, -5.75F, -0.75F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(2, 9)
						.addBox(-1.0F, -5.75F, -0.75F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 2)
						.addBox(1.0F, -4.75F, -0.25F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(2, 2)
						.addBox(-1.0F, -4.75F, -0.25F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-2.0F, -8.0F, -1.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 7)
						.addBox(0.5F, -7.8F, 2.25F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-1.5F, -7.8F, 2.25F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 13)
						.addBox(0.75F, -8.25F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(11, 11)
						.addBox(-0.5F, -8.25F, -0.85F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(12, 0)
						.addBox(-1.75F, -8.25F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition jaw = head.addOrReplaceChild("jaw",
				CubeListBuilder.create().texOffs(0, 7)
						.addBox(-1.5F, 0.0F, -4.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(5, 15)
						.addBox(-1.0F, -0.25F, -4.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, 13)
						.addBox(-1.0F, -0.25F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(10, 7)
						.addBox(-1.0F, 0.5F, -3.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(6, 9)
						.addBox(-1.25F, -0.25F, -3.75F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 9)
						.addBox(1.25F, -0.25F, -3.75F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -5.0F, 3.0F, 0.9599F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(EntityBloodBullet entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
	}
}