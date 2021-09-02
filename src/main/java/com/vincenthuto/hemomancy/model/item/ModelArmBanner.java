package com.vincenthuto.hemomancy.model.item;

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

public class ModelArmBanner extends Model {
	public final List<ModelPart> parts = new ArrayList<ModelPart>();

	public ModelArmBanner(ModelPart part) {
		super(RenderType::entityTranslucent);
		for (int i = 0; i < 8; i++) {
			ModelPart mesh = part.getChild(Integer.toString(i)) ;
			parts.add(mesh);
		}
	}

	public static LayerDefinition createLayers() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition leftShoulder = mesh.getRoot();

		leftShoulder.addOrReplaceChild("0",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -4.0F, -2.5F, 5.0F, 2.0F, 5.0F, false),
				PartPose.ZERO);
		leftShoulder.addOrReplaceChild("1",
				CubeListBuilder.create().texOffs(0, 7).addBox(3.0F, -2.0F, -2.5F, 1.0F, 3.0F, 5.0F, false),
				PartPose.ZERO);
		leftShoulder.addOrReplaceChild("2",
				CubeListBuilder.create().texOffs(0, 15).addBox(4.0F, -3.75F, -2.0F, 1.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		leftShoulder.addOrReplaceChild("3",
				CubeListBuilder.create().texOffs(7, 7).addBox(-1.0F, -5.0F, -1.5F, 4.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		leftShoulder.addOrReplaceChild("4",
				CubeListBuilder.create().texOffs(15, 0).addBox(-1.0F, -2.5F, 2.0F, 4.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		leftShoulder.addOrReplaceChild("5",
				CubeListBuilder.create().texOffs(12, 11).addBox(-1.0F, -2.5F, -3.0F, 4.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		leftShoulder.addOrReplaceChild("6",
				CubeListBuilder.create().texOffs(14, 16).addBox(-1.0F, -3.5F, -3.0F, 3.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		leftShoulder.addOrReplaceChild("7",
				CubeListBuilder.create().texOffs(6, 16).addBox(-1.0F, -3.5F, 2.0F, 3.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		return LayerDefinition.create(mesh, 32, 32);
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
