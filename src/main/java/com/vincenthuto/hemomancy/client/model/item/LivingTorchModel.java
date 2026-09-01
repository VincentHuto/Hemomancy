package com.vincenthuto.hemomancy.client.model.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class LivingTorchModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("living_torch"), "main");
	private static final float TORCH_SHAFT_BOTTOM_Y = -10.0F;
	private static final float TORCH_SHAFT_TOP_Y = 18.0F;
	private static final float TORCH_FLAME_MIN_Y = 20.5F;

	private final ModelPart root;

	public LivingTorchModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("shaft",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-0.75F, TORCH_SHAFT_BOTTOM_Y, -0.75F, 1.5F,
								TORCH_SHAFT_TOP_Y - TORCH_SHAFT_BOTTOM_Y, 1.5F,
								new CubeDeformation(0.0F))
						.texOffs(8, 0).addBox(-1.35F, TORCH_SHAFT_BOTTOM_Y - 1.5F, -1.35F, 2.7F, 3.2F, 2.7F,
								new CubeDeformation(0.0F))
						.texOffs(18, 0).addBox(-1.15F, 9.0F, -1.15F, 2.3F, 3.0F, 2.3F,
								new CubeDeformation(-0.05F)),
				PartPose.ZERO);
		root.addOrReplaceChild("crown",
				CubeListBuilder.create()
						.texOffs(28, 0).addBox(-2.1F, 15.2F, -2.1F, 4.2F, 4.2F, 4.2F,
								new CubeDeformation(-0.15F))
						.texOffs(0, 32).addBox(-1.55F, 18.3F, -1.55F, 3.1F, 3.0F, 3.1F,
								new CubeDeformation(-0.25F))
						.texOffs(18, 10).addBox(-2.45F, 13.9F, -0.55F, 4.9F, 1.4F, 1.1F,
								new CubeDeformation(0.0F))
						.texOffs(18, 14).addBox(-0.55F, 13.9F, -2.45F, 1.1F, 1.4F, 4.9F,
								new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("flame",
				CubeListBuilder.create()
						.texOffs(34, 20).addBox(-1.15F, TORCH_FLAME_MIN_Y, -0.02F, 2.3F, 7.0F, 0.04F,
								new CubeDeformation(0.0F))
						.texOffs(42, 20).addBox(-0.02F, TORCH_FLAME_MIN_Y + 0.35F, -1.15F, 0.04F, 6.3F, 2.3F,
								new CubeDeformation(0.0F))
						.texOffs(50, 20).addBox(-0.6F, TORCH_FLAME_MIN_Y + 1.0F, -0.03F, 1.2F, 4.8F, 0.06F,
								new CubeDeformation(0.0F)),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
