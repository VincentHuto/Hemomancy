package com.vincenthuto.hemomancy.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.iron.EntityIronSpike;

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


public class ModelIronSpike<T extends Entity> extends EntityModel<EntityIronSpike> {
	
	public static final ModelLayerLocation iron_spike = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "iron_spike"), "main");
	
	private final ModelPart whole;

	public ModelIronSpike(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create().texOffs(0, 13).addBox(-16.0F, -8.0F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-15.0F, -11.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-15.0F, -3.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-14.0F, -14.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-13.0F, -18.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-17.0F, -8.0F, -3.0F, 1.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-8.0F, -8.0F, -3.0F, 1.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-15.0F, -8.0F, 4.0F, 6.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 13).addBox(-15.0F, -8.0F, -5.0F, 6.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(12.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(EntityIronSpike entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		whole.render(poseStack, buffer, packedLight, packedOverlay);
	}
}