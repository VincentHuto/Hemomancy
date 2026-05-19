package com.vincenthuto.hemomancy.client.model.item;

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
import net.minecraft.world.entity.LivingEntity;

public class SporiticThuribleModel<T extends LivingEntity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("sporitic_thurible"), "main");

	private final ModelPart body;
	private final ModelPart chainLink;

	public SporiticThuribleModel(ModelPart root) {
		this.body = root.getChild("body");
		this.chainLink = root.getChild("chain_link");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bowl = body.addOrReplaceChild("bowl", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.35F))
				.texOffs(0, 11).addBox(-3.5F, 3.4F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(-0.25F))
				.texOffs(24, 24).addBox(-2.0F, 6.2F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(-0.35F))
				.texOffs(28, 18).addBox(-1.4F, 8.0F, -1.4F, 2.8F, 2.0F, 2.8F, new CubeDeformation(-0.45F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition lid = body.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(-3.2F, 2.0F, -3.2F, 6.4F, 2.0F, 6.4F, new CubeDeformation(-0.25F))
				.texOffs(0, 7).addBox(-2.6F, -1.2F, -2.6F, 5.2F, 2.5F, 5.2F, new CubeDeformation(-0.55F))
				.texOffs(25, 0).addBox(-1.5F, -1.8F, -1.5F, 3.0F, 1.6F, 3.0F, new CubeDeformation(-0.45F))
				.texOffs(26, 6).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition vents = body.addOrReplaceChild("vents", CubeListBuilder.create().texOffs(37, 0).addBox(2.5F, 0.2F, -0.35F, 0.7F, 4.0F, 0.7F, new CubeDeformation(0.0F))
				.texOffs(37, 0).addBox(-3.2F, 0.2F, -0.35F, 0.7F, 4.0F, 0.7F, new CubeDeformation(0.0F))
				.texOffs(37, 0).addBox(-0.35F, 0.2F, -3.2F, 0.7F, 4.0F, 0.7F, new CubeDeformation(0.0F))
				.texOffs(37, 0).addBox(-0.35F, 0.2F, 2.5F, 0.7F, 4.0F, 0.7F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));


		partdefinition.addOrReplaceChild("chain_link",
				CubeListBuilder.create()
						.texOffs(42, 0).addBox(-1.3F, -2.0F, -0.2F, 2.6F, 0.35F, 0.4F, new CubeDeformation(0.0F))
						.texOffs(42, 0).addBox(-1.3F, 1.65F, -0.2F, 2.6F, 0.35F, 0.4F, new CubeDeformation(0.0F))
						.texOffs(42, 4).addBox(-1.65F, -1.65F, -0.2F, 0.35F, 3.3F, 0.4F, new CubeDeformation(0.0F))
						.texOffs(42, 4).addBox(1.3F, -1.65F, -0.2F, 0.35F, 3.3F, 0.4F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	public void renderBody(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	public void renderChainLink(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		chainLink.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		renderBody(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	}
}
