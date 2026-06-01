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

public class LivingFlailModel<T extends LivingEntity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("living_flail"), "main");
	private static final float GRIP_MIN_Y = -9.5F;
	private static final float GRIP_HEIGHT = 18.0F;
	private static final float CHAIN_COLLAR_MIN_Y = 8.5F;

	private final ModelPart handle;
	private final ModelPart head;
	private final ModelPart chainLink;

	public LivingFlailModel(ModelPart root) {
		this.handle = root.getChild("handle");
		this.head = root.getChild("head");
		this.chainLink = root.getChild("chain_link");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("handle",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-0.85F, GRIP_MIN_Y, -0.85F, 1.7F, GRIP_HEIGHT, 1.7F,
								new CubeDeformation(0.0F))
						.texOffs(8, 0).addBox(-1.45F, GRIP_MIN_Y - 2.0F, -1.45F, 2.9F, 3.0F, 2.9F,
								new CubeDeformation(0.0F))
						.texOffs(20, 0).addBox(-1.5F, CHAIN_COLLAR_MIN_Y, -1.5F, 3.0F, 3.1F, 3.0F,
								new CubeDeformation(0.0F))
						.texOffs(32, 0).addBox(-2.0F, CHAIN_COLLAR_MIN_Y + 1.0F, -0.45F, 4.0F, 1.0F, 0.9F,
								new CubeDeformation(0.0F))
						.texOffs(32, 4).addBox(-0.45F, CHAIN_COLLAR_MIN_Y + 1.0F, -2.0F, 0.9F, 1.0F, 4.0F,
								new CubeDeformation(0.0F))
						.texOffs(44, 0).addBox(-1.15F, -2.0F, -1.15F, 2.3F, 4.0F, 2.3F,
								new CubeDeformation(-0.08F)),
				PartPose.ZERO);
		root.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(0, 32).addBox(-3.6F, -3.6F, -3.6F, 7.2F, 7.2F, 7.2F,
								new CubeDeformation(-0.35F))
						.texOffs(30, 32).addBox(-1.0F, -5.6F, -1.0F, 2.0F, 2.2F, 2.0F,
								new CubeDeformation(0.0F))
						.texOffs(30, 32).addBox(-1.0F, 3.4F, -1.0F, 2.0F, 2.2F, 2.0F,
								new CubeDeformation(0.0F))
						.texOffs(40, 32).addBox(-5.6F, -1.0F, -1.0F, 2.2F, 2.0F, 2.0F,
								new CubeDeformation(0.0F))
						.texOffs(40, 32).addBox(3.4F, -1.0F, -1.0F, 2.2F, 2.0F, 2.0F,
								new CubeDeformation(0.0F))
						.texOffs(50, 32).addBox(-1.0F, -1.0F, -5.6F, 2.0F, 2.0F, 2.2F,
								new CubeDeformation(0.0F))
						.texOffs(50, 32).addBox(-1.0F, -1.0F, 3.4F, 2.0F, 2.0F, 2.2F,
								new CubeDeformation(0.0F))
						.texOffs(26, 44).addBox(-2.2F, -2.2F, -2.2F, 4.4F, 4.4F, 4.4F,
								new CubeDeformation(-0.45F)),
				PartPose.ZERO);
		root.addOrReplaceChild("chain_link",
				CubeListBuilder.create()
						.texOffs(36, 0).addBox(-1.2F, -1.8F, -0.18F, 2.4F, 0.32F, 0.36F, new CubeDeformation(0.0F))
						.texOffs(36, 0).addBox(-1.2F, 1.48F, -0.18F, 2.4F, 0.32F, 0.36F, new CubeDeformation(0.0F))
						.texOffs(36, 4).addBox(-1.52F, -1.48F, -0.18F, 0.32F, 2.96F, 0.36F, new CubeDeformation(0.0F))
						.texOffs(36, 4).addBox(1.2F, -1.48F, -0.18F, 0.32F, 2.96F, 0.36F, new CubeDeformation(0.0F)),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 64);
	}

	public void renderHandle(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		handle.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	public void renderHead(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		head.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	public void renderChainLink(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		chainLink.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		renderHandle(poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderHead(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
	}
}
