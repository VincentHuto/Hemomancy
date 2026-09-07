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

public final class LivingSickleModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("living_sickle"), "main");
	private final ModelPart root;

	public LivingSickleModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("haft", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 26.0F, 2.0F)
				.texOffs(10, 0).addBox(-1.25F, 8.0F, -1.25F, 2.5F, 14.0F, 2.5F)
				.texOffs(20, 0).addBox(-1.6F, 7.0F, -1.6F, 3.2F, 2.0F, 3.2F)
				.texOffs(20, 6).addBox(-1.6F, 18.0F, -1.6F, 3.2F, 2.0F, 3.2F)
				.texOffs(34, 0).addBox(-2.0F, 21.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.ZERO);
		root.addOrReplaceChild("blade_root", CubeListBuilder.create()
				.texOffs(0, 32).addBox(-2.0F, -9.0F, -1.5F, 6.0F, 6.0F, 3.0F), PartPose.ZERO);
		root.addOrReplaceChild("blade_spine", CubeListBuilder.create()
				.texOffs(18, 32).addBox(0.0F, -1.75F, -1.0F, 8.0F, 3.5F, 2.0F),
				PartPose.offsetAndRotation(1.5F, -7.0F, 0.0F, 0.0F, 0.0F, 0.0873F));
		root.addOrReplaceChild("blade_mid", CubeListBuilder.create()
				.texOffs(40, 32).addBox(0.0F, -1.5F, -0.9F, 7.0F, 3.0F, 1.8F),
				PartPose.offsetAndRotation(9.2F, -5.4F, 0.0F, 0.0F, 0.0F, 0.3491F));
		root.addOrReplaceChild("blade_bend", CubeListBuilder.create()
				.texOffs(0, 42).addBox(0.0F, -1.25F, -0.8F, 5.5F, 2.5F, 1.6F),
				PartPose.offsetAndRotation(15.3F, -2.7F, 0.0F, 0.0F, 0.0F, 0.6981F));
		root.addOrReplaceChild("blade_hook", CubeListBuilder.create()
				.texOffs(16, 42).addBox(0.0F, -1.1F, -0.7F, 4.5F, 2.2F, 1.4F),
				PartPose.offsetAndRotation(18.9F, 1.0F, 0.0F, 0.0F, 0.0F, 1.0472F));
		root.addOrReplaceChild("blade_curl", CubeListBuilder.create()
				.texOffs(30, 42).addBox(0.0F, -0.9F, -0.6F, 4.0F, 1.8F, 1.2F),
				PartPose.offsetAndRotation(21.1F, 4.8F, 0.0F, 0.0F, 0.0F, 1.3963F));
		root.addOrReplaceChild("blade_tip", CubeListBuilder.create()
				.texOffs(44, 42).addBox(0.0F, -0.6F, -0.5F, 3.5F, 1.2F, 1.0F),
				PartPose.offsetAndRotation(21.8F, 8.7F, 0.0F, 0.0F, 0.0F, 2.0071F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
