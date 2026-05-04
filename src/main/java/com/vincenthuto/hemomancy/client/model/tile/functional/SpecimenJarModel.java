package com.vincenthuto.hemomancy.client.model.tile.functional;

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

public class SpecimenJarModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("specimen_jar_model"), "main");

	private final ModelPart frame;
	private final ModelPart glass;

	public SpecimenJarModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		ModelPart jar = root.getChild("root");
		this.frame = jar.getChild("frame");
		this.glass = jar.getChild("glass");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("frame", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-5.0F, -2.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 12)
				.addBox(-4.0F, -14.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(36, 0)
				.addBox(-1.5F, -16.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(32, 12)
				.addBox(-5.5F, -12.0F, -5.5F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(40, 12)
				.addBox(3.5F, -12.0F, -5.5F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(48, 12)
				.addBox(-5.5F, -12.0F, 3.5F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(56, 12)
				.addBox(3.5F, -12.0F, 3.5F, 2.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		root.addOrReplaceChild("glass", CubeListBuilder.create()
				.texOffs(0, 24)
				.addBox(-4.0F, -12.0F, -4.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.02F)),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 48);
	}

	public void renderFrame(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		frame.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}

	public void renderGlass(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		glass.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		renderFrame(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		renderGlass(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
