package com.vincenthuto.hemomancy.client.model.item.unstained;

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
import net.minecraft.client.renderer.RenderType;

public class AbsolutionDaggerModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("absolution_dagger"), "root");

	private final ModelPart root;

	public AbsolutionDaggerModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		var root = meshDefinition.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

		root.addOrReplaceChild("stiletto_blade",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-0.7F, -15.0F, -0.25F, 1.4F, 8.0F, 0.5F, new CubeDeformation(0.0F))
						.texOffs(4, 0)
						.addBox(-0.45F, -20.0F, -0.2F, 0.9F, 5.0F, 0.4F, new CubeDeformation(0.0F))
						.texOffs(8, 0)
						.addBox(-0.25F, -23.0F, -0.15F, 0.5F, 3.0F, 0.3F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		root.addOrReplaceChild("guard",
				CubeListBuilder.create().texOffs(0, 9)
						.addBox(-3.0F, -7.5F, -0.5F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("grip",
				CubeListBuilder.create().texOffs(0, 12)
						.addBox(-0.75F, -6.5F, -0.75F, 1.5F, 8.0F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("pommel",
				CubeListBuilder.create().texOffs(8, 12)
						.addBox(-1.25F, 1.0F, -1.25F, 2.5F, 2.0F, 2.5F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
