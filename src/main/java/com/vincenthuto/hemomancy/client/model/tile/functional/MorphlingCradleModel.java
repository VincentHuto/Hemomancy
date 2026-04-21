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

public class MorphlingCradleModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("morphling_cradle_model"), "main");

	private final ModelPart root;

	public MorphlingCradleModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("base", CubeListBuilder.create()
				.texOffs(0, 0)
				.addBox(-5.0F, -3.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 12)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("arms", CubeListBuilder.create()
				.texOffs(32, 0)
				.addBox(-6.0F, -9.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0)
				.addBox(4.0F, -9.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(48, 0)
				.addBox(-1.0F, -9.0F, -6.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("retainer", CubeListBuilder.create()
				.texOffs(32, 10)
				.addBox(-2.0F, -9.0F, 3.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(32, 12)
				.addBox(-1.0F, -8.0F, 3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
