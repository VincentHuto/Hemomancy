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

public class ModelLivingBladeHandTame extends Model {
	private final List<ModelPart> parts = new ArrayList<ModelPart>();

	public ModelLivingBladeHandTame(ModelPart part) {
		super(RenderType::entityTranslucent);
		for (int i = 0; i < 39; i++) {
			parts.add(part.getChild(Integer.toString(i)));
		}
	}

	public static LayerDefinition createLayers() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		part.addOrReplaceChild("0",
				CubeListBuilder.create().texOffs(30, 21).addBox(-1.0029F, 5.5559F, -1.0147F, 2.0F, 2.0F, 2.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("1",
				CubeListBuilder.create().texOffs(0, 28).addBox(-0.5029F, 2.9559F, -1.2647F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("2",
				CubeListBuilder.create().texOffs(18, 27).addBox(-0.5029F, 2.9559F, 0.2353F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("3",
				CubeListBuilder.create().texOffs(27, 0).addBox(-0.5029F, -1.0441F, -1.2647F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("4",
				CubeListBuilder.create().texOffs(12, 17).addBox(-0.5029F, -1.0441F, 0.2353F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("5",
				CubeListBuilder.create().texOffs(28, 15).addBox(-0.5029F, 1.4559F, -1.2647F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("6",
				CubeListBuilder.create().texOffs(27, 21).addBox(-0.5029F, 1.4559F, 0.2353F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("7",
				CubeListBuilder.create().texOffs(21, 22).addBox(-1.0029F, 4.9559F, -2.0147F, 2.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("8",
				CubeListBuilder.create().texOffs(12, 32).addBox(0.9971F, 4.9559F, -1.0147F, 1.0F, 1.0F, 2.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("9",
				CubeListBuilder.create().texOffs(31, 26).addBox(-2.0029F, 4.9559F, -1.0147F, 1.0F, 1.0F, 2.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("10",
				CubeListBuilder.create().texOffs(10, 27).addBox(-1.0029F, 4.9559F, 0.9853F, 2.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("11",
				CubeListBuilder.create().texOffs(4, 27).addBox(-1.0029F, -1.4441F, -1.0147F, 2.0F, 6.0F, 2.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("12",
				CubeListBuilder.create().texOffs(15, 6).addBox(-1.2529F, 2.5559F, -0.5147F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("13",
				CubeListBuilder.create().texOffs(0, 6).addBox(0.2471F, 2.5559F, -0.5147F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("14",
				CubeListBuilder.create().texOffs(15, 0).addBox(-1.2529F, -1.1941F, -0.5147F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("15",
				CubeListBuilder.create().texOffs(12, 12).addBox(0.2471F, -1.1941F, -0.5147F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("16",
				CubeListBuilder.create().texOffs(0, 9).addBox(-1.2529F, 1.0559F, -0.5147F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("17",
				CubeListBuilder.create().texOffs(0, 3).addBox(0.2471F, 1.0559F, -0.5147F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("18",
				CubeListBuilder.create().texOffs(12, 22).addBox(-1.5029F, 4.5559F, -1.5147F, 3.0F, 2.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("19",
				CubeListBuilder.create().texOffs(24, 17).addBox(-1.5029F, -2.4441F, -1.5147F, 3.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("20",
				CubeListBuilder.create().texOffs(24, 11).addBox(-1.5029F, -2.5441F, -1.8147F, 3.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("21",
				CubeListBuilder.create().texOffs(21, 24).addBox(-1.5029F, -2.5441F, -1.2147F, 3.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("22",
				CubeListBuilder.create().texOffs(24, 28).addBox(0.7471F, -2.5441F, -1.5147F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("23",
				CubeListBuilder.create().texOffs(27, 6).addBox(-1.7529F, -2.5441F, -1.5147F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("24",
				CubeListBuilder.create().texOffs(12, 17).addBox(-2.0029F, -5.9441F, -2.0147F, 4.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("25",
				CubeListBuilder.create().texOffs(18, 28).addBox(-0.5029F, -6.9441F, -2.0147F, 1.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("26",
				CubeListBuilder.create().texOffs(29, 29).addBox(-0.5029F, -7.9441F, -1.0147F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("27",
				CubeListBuilder.create().texOffs(0, 0).addBox(-0.5029F, -8.9441F, 1.9853F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("28",
				CubeListBuilder.create().texOffs(6, 9).addBox(-0.0029F, -19.9441F, -1.0147F, 0.02F, 12.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("29",
				CubeListBuilder.create().texOffs(0, 9).addBox(-0.0029F, -31.9441F, -1.0147F, 0.02F, 12.0F, 3.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("30",
				CubeListBuilder.create().texOffs(0, 0).addBox(-0.0029F, -32.9441F, -0.5147F, 0.0F, 1.0F, 2.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("31",
				CubeListBuilder.create().texOffs(12, 21).addBox(-0.0029F, -33.9441F, 0.9853F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("32",
				CubeListBuilder.create().texOffs(15, 6).addBox(-2.0029F, -4.4441F, -2.0147F, 4.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("33",
				CubeListBuilder.create().texOffs(15, 0).addBox(-2.0029F, -4.4441F, -1.7147F, 4.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("34",
				CubeListBuilder.create().texOffs(12, 12).addBox(-2.0029F, -4.4441F, -2.3147F, 4.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("35",
				CubeListBuilder.create().texOffs(27, 1).addBox(-2.2029F, -4.4441F, -2.0147F, 1.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("36",
				CubeListBuilder.create().texOffs(12, 27).addBox(1.2971F, -4.4441F, -2.0147F, 1.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("37",
				CubeListBuilder.create().texOffs(0, 6).addBox(-2.5029F, -3.4441F, -2.5147F, 5.0F, 1.0F, 5.0F, false),
				PartPose.ZERO);
		part.addOrReplaceChild("38",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.5029F, -5.4441F, -2.5147F, 5.0F, 1.0F, 5.0F, false),
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
