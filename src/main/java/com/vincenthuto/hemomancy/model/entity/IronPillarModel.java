package com.vincenthuto.hemomancy.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronPillar;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class IronPillarModel<T extends Entity> extends EntityModel<EntityIronPillar> {

	public static final ModelLayerLocation iron_pillar = new ModelLayerLocation(
			Hemomancy.rloc("iron_pillar"), "main");

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole",
				CubeListBuilder.create().texOffs(-5, 1)
						.addBox(-16.0F, -9.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(-5, 1)
						.addBox(-16.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(1, 14)
						.addBox(-15.0F, -16.0F, -2.75F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(1, 14)
						.addBox(-15.0F, -24.0F, -2.75F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(12.0F, 25.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	private final ModelPart whole;

	public IronPillarModel(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(EntityIronPillar entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

	}
}