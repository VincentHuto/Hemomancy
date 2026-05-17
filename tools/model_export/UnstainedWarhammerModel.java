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
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class UnstainedWarhammerModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("unstained_warhammer"), "root");

	private final ModelPart root;

	public UnstainedWarhammerModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot().addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.ZERO);

		root.addOrReplaceChild("haft",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-0.5F, -13.0F, -0.5F, 1.0F, 30.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(4, 0)
						.addBox(-0.75F, 8.0F, -0.75F, 1.5F, 8.0F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		PartDefinition bell = root.addOrReplaceChild("bell_head", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, -15.0F, 0.0F, 0.0F, 0.0F, -1.5708F));
		bell.addOrReplaceChild("bell_crown",
				CubeListBuilder.create().texOffs(8, 0)
						.addBox(-2.0F, -7.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		bell.addOrReplaceChild("bell_body",
				CubeListBuilder.create().texOffs(0, 9)
						.addBox(-3.0F, -5.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		bell.addOrReplaceChild("bell_flared_face",
				CubeListBuilder.create().texOffs(0, 20)
						.addBox(-4.0F, -1.0F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		bell.addOrReplaceChild("pale_clapper",
				CubeListBuilder.create().texOffs(24, 0)
						.addBox(-1.0F, 1.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		root.addOrReplaceChild("pommel",
				CubeListBuilder.create().texOffs(24, 4)
						.addBox(-1.5F, 16.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
