package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeFragmentEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Model for a Seraphae fragment — a small radiant shard / wisp.
 * Simple geometric shape: a diamond-like prism that floats and rotates.
 * UV layout is 32×32.
 */
public class SeraphaeFragmentModel<T extends SeraphaeFragmentEntity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("seraphae_fragment"), "main");

	private final ModelPart core;

	public SeraphaeFragmentModel(ModelPart root) {
		this.core = root.getChild("core");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Central diamond / prism shape
		partdefinition.addOrReplaceChild("core",
				CubeListBuilder.create()
						// Main body — tall narrow prism
						.texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
						// Small radiant spike on top
						.texOffs(0, 18).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
						// Small radiant spike on bottom
						.texOffs(8, 18).addBox(-1.0F, 6.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 16.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		// Gentle floating rotation
		this.core.yRot = ageInTicks * 0.1F;
		this.core.y = 16.0F + (float) Math.sin(ageInTicks * 0.08F) * 1.5F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, float red, float green, float blue, float alpha) {
		core.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
