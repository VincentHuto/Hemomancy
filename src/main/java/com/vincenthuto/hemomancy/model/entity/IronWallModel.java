package com.vincenthuto.hemomancy.model.entity;

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
import net.minecraft.world.entity.Entity;


public class IronWallModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation iron_wall = new ModelLayerLocation(
			Hemomancy.rloc("iron_wall"), "main");

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create().texOffs(0, 48).addBox(-8.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(28, 72).addBox(-8.0F, -16.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(54, 63).addBox(-8.0F, -24.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 16).addBox(-8.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(48, 32).addBox(8.0F, -8.0F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(74, 43).addBox(10.0F, -16.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(74, 11).addBox(10.0F, -24.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(48, 48).addBox(8.0F, -31.0F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(48, 16).addBox(-15.0F, -8.0F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(56, 77).addBox(-14.0F, -16.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(74, 27).addBox(-14.0F, -24.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(48, 0).addBox(-15.0F, -31.0F, -4.0F, 7.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(24, 8).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(24, 40).addBox(-8.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 32).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 66).addBox(0.0F, -16.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(14, 80).addBox(1.0F, -15.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(70, 0).addBox(1.0F, -15.0F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 80).addBox(1.0F, -23.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 0).addBox(1.0F, -23.0F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(78, 55).addBox(-7.0F, -23.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(-7.0F, -23.0F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(77, 62).addBox(-7.0F, -15.0F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(72, 77).addBox(-7.0F, -15.0F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 58).addBox(0.0F, -24.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(0.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(24, 24).addBox(0.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	private final ModelPart whole;

	public IronWallModel(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		whole.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}
}