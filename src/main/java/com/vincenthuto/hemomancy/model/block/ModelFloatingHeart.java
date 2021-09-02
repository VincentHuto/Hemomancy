package com.vincenthuto.hemomancy.model.block;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class ModelFloatingHeart extends Model {
	private final List<ModelPart> parts = new ArrayList<ModelPart>();

	public ModelFloatingHeart(ModelPart part) {
		super(RenderType::entityTranslucent);
		for (int i = 0; i < 19; i++) {
			parts.add(part.getChild(Integer.toString(i)));
		}
	}

	public static LayerDefinition createLayers() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		part.addOrReplaceChild("0",
				CubeListBuilder.create().texOffs(11, 27).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 5.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("1",
				CubeListBuilder.create().texOffs(0, 26).addBox(-0.5F, -0.5F, 2.5F, 1.0F, 3.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("2",
				CubeListBuilder.create().texOffs(14, 3).addBox(-0.5F, -0.5F, -3.5F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("3",
				CubeListBuilder.create().texOffs(24, 12).addBox(-2.25F, -2.5F, 0.5F, 1.0F, 3.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("4",
				CubeListBuilder.create().texOffs(20, 20).addBox(-0.75F, -2.5F, 1.0F, 1.0F, 3.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("5",
				CubeListBuilder.create().texOffs(0, 14).addBox(-1.75F, -2.5F, -2.0F, 1.0F, 3.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("6",
				CubeListBuilder.create().texOffs(0, 0).addBox(-4.75F, -2.5F, -2.0F, 1.0F, 5.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("7",
				CubeListBuilder.create().texOffs(0, 26).addBox(-3.0F, -5.0F, -3.0F, 4.0F, 2.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("8",
				CubeListBuilder.create().texOffs(0, 14).addBox(-2.0F, -11.0F, -3.3F, 4.0F, 6.0F, 6.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("9",
				CubeListBuilder.create().texOffs(24, 12).addBox(-2.5F, -3.0F, -3.0F, 3.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("10",
				CubeListBuilder.create().texOffs(20, 0).addBox(-4.0F, -12.0F, -2.8F, 4.0F, 7.0F, 5.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("11",
				CubeListBuilder.create().texOffs(25, 28).addBox(-5.0F, -11.0F, -2.3F, 1.0F, 6.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("12",
				CubeListBuilder.create().texOffs(0, 32).addBox(0.25F, -12.5F, -2.55F, 2.0F, 2.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("13",
				CubeListBuilder.create().texOffs(31, 24).addBox(0.75F, -11.5F, -2.3F, 2.0F, 2.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("14",
				CubeListBuilder.create().texOffs(29, 17).addBox(-4.5F, -12.5F, -2.55F, 2.0F, 2.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("15",
				CubeListBuilder.create().texOffs(20, 20).addBox(-1.75F, -13.5F, -3.05F, 2.0F, 3.0F, 5.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("16",
				CubeListBuilder.create().texOffs(12, 34).addBox(-3.5F, -15.0F, -1.55F, 2.0F, 4.0F, 2.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("17",
				CubeListBuilder.create().texOffs(14, 0).addBox(-3.0F, -5.0F, 1.0F, 3.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("18",
				CubeListBuilder.create().texOffs(15, 12).addBox(0.25F, -10.75F, -3.05F, 2.0F, 3.0F, 5.0F, false),
				PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		parts.forEach((model) -> model.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn));
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
