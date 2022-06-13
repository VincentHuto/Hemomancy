package com.vincenthuto.hemomancy.model.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ModelChitiniteShield<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation chitinite_shield = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "chitinite_shield"), "main");

	private final ModelPart shield;

	public ModelChitiniteShield(ModelPart part) {
		super(RenderType::entityTranslucent);
		this.shield = part.getChild("shield");

	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition shield = partdefinition.addOrReplaceChild("shield",
				CubeListBuilder.create().texOffs(26, 0)
						.addBox(-1.0F, -15.5F, -3.0F, 2.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(26, 12)
						.addBox(-6.0F, -21.5F, -4.0F, 12.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-5.0F, -21.5F, -5.0F, 10.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 56)
						.addBox(-6.0F, -24.5F, -3.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 36)
						.addBox(-7.0F, -21.5F, -3.0F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 36)
						.addBox(6.0F, -21.5F, -3.0F, 1.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 24)
						.addBox(-5.0F, -23.5F, -4.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 56)
						.addBox(-6.0F, -3.5F, -3.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 24)
						.addBox(-5.0F, -3.5F, -4.0F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.0F, 8.5F, 3.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		shield.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
