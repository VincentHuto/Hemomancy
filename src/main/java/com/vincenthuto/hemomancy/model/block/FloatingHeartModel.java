package com.vincenthuto.hemomancy.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class FloatingHeartModel extends Model {

	public static final ModelLayerLocation mortal_display = new ModelLayerLocation(
			Hemomancy.rloc("mortal_display"), "main");


	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition aorta = partdefinition.addOrReplaceChild("aorta",
				CubeListBuilder.create().texOffs(11, 27)
						.addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 26)
						.addBox(-0.5F, -0.5F, 2.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(14, 3)
						.addBox(-0.5F, -0.5F, -3.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(24, 12)
						.addBox(-2.25F, -2.5F, 0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 20)
						.addBox(-0.75F, -2.5F, 1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 14)
						.addBox(-1.75F, -2.5F, -2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-4.75F, -2.5F, -2.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.5F, 10.5F, -0.55F, 0.0F, 0.4363F, 0.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main",
				CubeListBuilder.create().texOffs(0, 26)
						.addBox(-3.0F, -5.0F, -3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 14)
						.addBox(-2.0F, -11.0F, -3.3F, 4.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(24, 12)
						.addBox(-2.5F, -3.0F, -3.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(20, 0)
						.addBox(-4.0F, -12.0F, -2.8F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(25, 28)
						.addBox(-5.0F, -11.0F, -2.3F, 1.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 32)
						.addBox(0.25F, -12.5F, -2.55F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(31, 24)
						.addBox(0.75F, -11.5F, -2.3F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(29, 17)
						.addBox(-4.5F, -12.5F, -2.55F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(20, 20)
						.addBox(-1.75F, -13.5F, -3.05F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(12, 34)
						.addBox(-3.5F, -15.0F, -1.55F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(14, 0)
						.addBox(-3.0F, -5.0F, 1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(15, 12)
						.addBox(0.25F, -10.75F, -3.05F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	private final ModelPart aorta;

	private final ModelPart bb_main;

	public FloatingHeartModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.aorta = root.getChild("aorta");
		this.bb_main = root.getChild("bb_main");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		aorta.render(poseStack, buffer, packedLight, packedOverlay);
		bb_main.render(poseStack, buffer, packedLight, packedOverlay);
	}
}