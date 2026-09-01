package com.vincenthuto.hemomancy.client.model.item.unstained;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class SilthmereGlaiveModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("silthmere_glaive"), "root");

	private final ModelPart root;

	public SilthmereGlaiveModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot().addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.ZERO);

		root.addOrReplaceChild("shaft",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-0.5F, -11.0F, -0.5F, 1.0F, 32.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(4, 0)
						.addBox(-0.75F, 9.0F, -0.75F, 1.5F, 9.0F, 1.5F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		PartDefinition blade = root.addOrReplaceChild("blade", CubeListBuilder.create(),
				PartPose.offset(0.0F, -17.0F, 0.0F));
		blade.addOrReplaceChild("blade_spine",
				CubeListBuilder.create().texOffs(8, 0)
						.addBox(-0.6F, -14.0F, -0.35F, 1.2F, 16.0F, 0.7F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		blade.addOrReplaceChild("main_cutting_blade",
				CubeListBuilder.create().texOffs(13, 0)
						.addBox(0.0F, -12.0F, -0.35F, 3.0F, 13.0F, 0.7F, new CubeDeformation(0.0F))
						.texOffs(22, 0)
						.addBox(1.4F, -15.0F, -0.3F, 1.4F, 5.0F, 0.6F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.45F, -0.6F, 0.0F, 0.0F, 0.0F, 0.18F));
		blade.addOrReplaceChild("back_hook",
				CubeListBuilder.create().texOffs(18, 16)
						.addBox(-2.2F, -3.0F, -0.3F, 2.0F, 5.0F, 0.6F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.35F, -2.0F, 0.0F, 0.0F, 0.0F, -0.55F));
		blade.addOrReplaceChild("blade_tip",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-0.45F, -18.0F, -0.25F, 0.9F, 5.0F, 0.5F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		root.addOrReplaceChild("collar",
				CubeListBuilder.create().texOffs(8, 16)
						.addBox(-1.5F, -16.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("blade_socket",
				CubeListBuilder.create().texOffs(0, 24)
						.addBox(-0.7F, -16.0F, -0.7F, 1.4F, 5.0F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		return LayerDefinition.create(meshDefinition, 32, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
