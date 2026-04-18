package com.vincenthuto.hemomancy.client.model.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

/**
 * Model for the Sanguine Monolith — a 1-block-wide, 0.5-block-deep, 2-block-tall
 * black slab. The bottom third of the monolith has an animated red vein pattern
 * rendered as an overlay in the renderer.
 */
public class SanguineMonolithModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelsanguinemonolith"), "main");

	private final ModelPart slab;

	public SanguineMonolithModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.slab = root.getChild("slab");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Main slab body: 16 wide (1 block), 32 tall (2 blocks), 8 deep (0.5 block)
		partdefinition.addOrReplaceChild("slab", CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-8.0F, -32.0F, -4.0F, 16.0F, 32.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		slab.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
