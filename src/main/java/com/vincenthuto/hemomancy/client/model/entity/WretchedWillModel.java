package com.vincenthuto.hemomancy.client.model.entity;

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

public class WretchedWillModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation wretched_will = new ModelLayerLocation(
			Hemomancy.rloc("modelwretchedwill"), "main");

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.0F, 10.0F, 2.0F));

		PartDefinition skull = whole.addOrReplaceChild("skull",
				CubeListBuilder.create().texOffs(46, 36)
						.addBox(1.0F, 2.0F, 2.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(46, 44)
						.addBox(-4.0F, 2.0F, 2.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(17, 9)
						.addBox(-3.0F, 2.0F, 1.0F, 4.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(46, 6)
						.addBox(-5.0F, -3.0F, 2.5F, 8.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(5, 11)
						.addBox(-3.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(5, 7)
						.addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(10, 55)
						.addBox(0.0F, 0.0F, 1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(31, 26)
						.addBox(3.0F, -3.0F, 2.0F, 1.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(10, 42)
						.addBox(4.0F, -1.0F, 3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(54, 0)
						.addBox(4.0F, -2.0F, 4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 11)
						.addBox(4.0F, -3.0F, 5.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 7)
						.addBox(-7.0F, -3.0F, 5.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(41, 25)
						.addBox(-7.0F, -1.0F, 3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 53)
						.addBox(-7.0F, -2.0F, 4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(46, 51)
						.addBox(4.0F, 1.0F, 4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(51, 30)
						.addBox(2.0F, 2.0F, 4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(40, 50)
						.addBox(-5.0F, 2.0F, 4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(18, 42)
						.addBox(2.0F, 1.0F, 3.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(32, 39)
						.addBox(-5.0F, 1.0F, 8.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(32, 42)
						.addBox(-5.0F, 1.0F, 3.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(52, 52)
						.addBox(-7.0F, 1.0F, 4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 27)
						.addBox(-6.0F, -3.0F, 2.0F, 1.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(36, 0)
						.addBox(-5.0F, -3.0F, 10.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(46, 11)
						.addBox(-4.0F, -2.0F, 11.0F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(54, 43)
						.addBox(-5.0F, 0.0F, 1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 24)
						.addBox(-2.0F, -1.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(17, 7)
						.addBox(-2.0F, -2.0F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 7)
						.addBox(-3.0F, -4.0F, 1.0F, 4.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(54, 16)
						.addBox(-3.0F, -4.0F, 10.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(36, 6)
						.addBox(-3.0F, 2.0F, 10.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 4)
						.addBox(-2.0F, 2.0F, 11.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 18)
						.addBox(-2.0F, 3.0F, 10.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(26, 0)
						.addBox(-4.0F, 2.0F, 8.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 21)
						.addBox(1.0F, 2.0F, 8.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(34, 9)
						.addBox(-5.0F, -4.0F, 2.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(24, 0)
						.addBox(1.0F, -4.0F, 2.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 40)
						.addBox(-5.0F, -5.0F, 3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(38, 18)
						.addBox(1.0F, -5.0F, 3.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 18)
						.addBox(-3.0F, -5.0F, 2.0F, 4.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(8, 50)
						.addBox(-5.0F, -6.0F, 4.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(49, 25)
						.addBox(1.0F, -6.0F, 4.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(12, 34)
						.addBox(-3.0F, -6.0F, 3.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-6.0F, -4.0F, 3.0F, 10.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(16, 19)
						.addBox(-6.0F, -5.0F, 4.0F, 10.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.0F, -5.0F));

		PartDefinition jaw = whole.addOrReplaceChild("jaw",
				CubeListBuilder.create().texOffs(17, 24)
						.addBox(-2.0F, 1.5313F, -6.0313F, 4.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(54, 35)
						.addBox(-2.0F, 1.5313F, 0.9688F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(10, 27)
						.addBox(-2.0F, -0.4688F, 1.9688F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(32, 24)
						.addBox(-1.0F, 2.5313F, -7.0313F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 18)
						.addBox(2.0F, 1.5313F, -4.0313F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 47)
						.addBox(-3.0F, 1.5313F, -4.0313F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(24, 42)
						.addBox(2.0F, 2.5313F, -5.0313F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(30, 50)
						.addBox(3.0F, 0.5313F, -3.0313F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(10, 31)
						.addBox(2.9F, -0.4688F, -1.0313F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(17, 11)
						.addBox(1.1F, -0.4688F, 0.9688F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-3.1F, -0.4688F, 0.9688F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 50)
						.addBox(-4.0F, 0.5313F, -3.0313F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 30)
						.addBox(-3.9F, -0.4688F, -1.0313F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(26, 3)
						.addBox(3.0F, 2.5313F, -1.0313F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 27)
						.addBox(-4.0F, 2.5313F, -1.0313F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(38, 42)
						.addBox(-3.0F, 2.5313F, -5.0313F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.0F, 2.4688F, 2.0313F));

		PartDefinition bone = jaw.addOrReplaceChild("bone", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	private final ModelPart whole;

	public WretchedWillModel(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	      this.whole.yRot = netHeadYaw * ((float)Math.PI / 180F);
	      this.whole.xRot = headPitch * ((float)Math.PI / 180F);
	}
}