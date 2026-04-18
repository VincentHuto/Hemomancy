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
 * Model for the Sanguine Monolith — a tall, dark, rectangular slab inspired by
 * the SEELE monoliths from Neon Genesis Evangelion. The monolith is a narrow,
 * imposing pillar with a slight inset glyph panel on the front face.
 */
public class SanguineMonolithModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelsanguinemonolith"), "main");

	private final ModelPart slab;
	private final ModelPart glyph_panel;
	private final ModelPart base;

	public SanguineMonolithModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.slab = root.getChild("slab");
		this.glyph_panel = root.getChild("glyph_panel");
		this.base = root.getChild("base");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Main slab body: 12 wide, 44 tall, 4 deep (narrow, imposing pillar)
		partdefinition.addOrReplaceChild("slab", CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-6.0F, -44.0F, -2.0F, 12.0F, 44.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Glyph panel: slightly recessed front face detail
		partdefinition.addOrReplaceChild("glyph_panel", CubeListBuilder.create()
						.texOffs(32, 0)
						.addBox(-4.0F, -38.0F, -2.5F, 8.0F, 32.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Base plinth: slightly wider than the slab
		partdefinition.addOrReplaceChild("base", CubeListBuilder.create()
						.texOffs(0, 48)
						.addBox(-7.0F, -1.0F, -3.0F, 14.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		slab.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		glyph_panel.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
