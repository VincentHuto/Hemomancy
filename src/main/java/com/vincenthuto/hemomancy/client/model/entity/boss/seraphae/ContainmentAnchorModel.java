package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;

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
 * Model for a Containment Anchor — a static pillar / pylon placed in the arena.
 * A tall obelisk-like structure with a glowing core ring.
 * UV layout is 64×32.
 */
public class ContainmentAnchorModel extends EntityModel<ContainmentAnchorEntity> {

	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("containment_anchor"), "main");

	private final ModelPart base;
	private final ModelPart pillar;
	private final ModelPart crown;

	public ContainmentAnchorModel(ModelPart root) {
		this.base = root.getChild("base");
		this.pillar = root.getChild("pillar");
		this.crown = root.getChild("crown");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Broad base block
		partdefinition.addOrReplaceChild("base",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -2.0F, -4.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Tall central pillar
		partdefinition.addOrReplaceChild("pillar",
				CubeListBuilder.create()
						.texOffs(0, 10).addBox(-2.0F, -16.0F, -2.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.0F, 0.0F));

		// Crown / cap with ring detail
		partdefinition.addOrReplaceChild("crown",
				CubeListBuilder.create()
						.texOffs(16, 10).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(ContainmentAnchorEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		// Crown slowly rotates when active
		if (entity.isActive()) {
			this.crown.yRot = ageInTicks * 0.15F;
		} else {
			this.crown.yRot = 0.0F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		base.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		pillar.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		crown.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
