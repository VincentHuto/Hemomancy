package com.vincenthuto.hemomancy.client.model.item;

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

public final class LivingSickleModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("living_sickle"), "main");
	private final ModelPart root;

	public LivingSickleModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("haft", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-0.75F, -5.0F, -0.75F, 1.5F, 27.0F, 1.5F,
						new CubeDeformation(0.0F))
				.texOffs(8, 0).addBox(-1.5F, 18.5F, -1.5F, 3.0F, 4.0F, 3.0F,
						new CubeDeformation(0.0F)), PartPose.ZERO);
		root.addOrReplaceChild("hook", CubeListBuilder.create()
				.texOffs(0, 32).addBox(0.0F, -6.0F, -0.5F, 11.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(0, 36).addBox(7.5F, -4.5F, -0.5F, 5.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(0, 40).addBox(10.5F, -2.5F, -0.5F, 4.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(0, 44).addBox(12.5F, -0.5F, -0.5F, 3.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(24, 32).addBox(-1.0F, -7.0F, -1.0F, 4.0F, 4.0F, 2.0F,
						new CubeDeformation(0.0F)), PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
