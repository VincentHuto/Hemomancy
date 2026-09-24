package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class MyelinBorerModel extends EntityModel<MyelinBorerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("myelin_borer"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart[] rings = new ModelPart[6];
	private final ModelPart[][] legs = new ModelPart[4][2];
	private final ModelPart[] palps = new ModelPart[2];

	public MyelinBorerModel(ModelPart root) {
		body = root.getChild("body");
		head = body.getChild("head");
		ModelPart ring = body;
		for (int i = 0; i < rings.length; i++) {
			ring = ring.getChild("ring" + i);
			rings[i] = ring;
			if (i < legs.length) {
				legs[i][0] = ring.getChild("leftLeg");
				legs[i][1] = ring.getChild("rightLeg");
			}
		}
		palps[0] = head.getChild("leftPalp");
		palps[1] = head.getChild("rightPalp");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 6).addBox(-1.5F, -1.5F, -2.8F, 3.0F, 2.5F, 3.8F)
				.texOffs(16, 6).addBox(-1.0F, -1.8F, -2.4F, 2.0F, 0.5F, 2.4F),
				PartPose.offset(0.0F, 21.8F, -2.5F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.15F, -1.3F, -2.3F, 2.3F, 2.0F, 2.8F)
				.texOffs(14, 0).addBox(-0.65F, -0.7F, -2.9F, 1.3F, 1.0F, 1.0F)
				.texOffs(20, 0).addBox(-0.35F, -0.4F, -3.4F, 0.7F, 0.7F, 0.7F),
				PartPose.offset(0.0F, 0.1F, -2.4F));
		for (int side : new int[] { -1, 1 }) {
			String name = side < 0 ? "leftPalp" : "rightPalp";
			PartDefinition palp = head.addOrReplaceChild(name, CubeListBuilder.create()
					.texOffs(24, 0).addBox(side < 0 ? -0.8F : 0.0F, -0.25F, -1.8F, 0.8F, 0.5F, 2.0F),
					PartPose.offset(side * 0.8F, 0.15F, -1.8F));
			palp.addOrReplaceChild("tip", CubeListBuilder.create()
					.texOffs(24, 4).addBox(side < 0 ? -0.35F : 0.0F, -0.2F, -1.2F, 0.35F, 0.35F, 1.4F),
					PartPose.offset(side * 0.6F, 0.0F, -1.6F));
		}

		PartDefinition parent = body;
		float[] widths = { 2.0F, 2.35F, 2.25F, 1.9F, 1.45F, 0.95F };
		for (int i = 0; i < widths.length; i++) {
			float width = widths[i];
			float depth = i == 5 ? 2.6F : 2.8F;
			CubeListBuilder shell = CubeListBuilder.create()
					.texOffs(0, 12).addBox(-width, -1.55F, -0.7F, width * 2.0F, 2.5F, depth)
					.texOffs(0, 20).addBox(-width + 0.25F, -1.85F, 0.1F, width * 2.0F - 0.5F, 0.4F, 1.5F)
					.texOffs(16, 13).addBox(-width + 0.4F, 0.85F, -0.3F, width * 2.0F - 0.8F, 0.35F, 1.8F);
			parent = parent.addOrReplaceChild("ring" + i, shell, PartPose.offset(0.0F, 0.0F, i == 0 ? 0.7F : 2.1F));
			if (i < 4) {
				for (int side : new int[] { -1, 1 }) {
					String name = side < 0 ? "leftLeg" : "rightLeg";
					PartDefinition leg = parent.addOrReplaceChild(name, CubeListBuilder.create()
							.texOffs(20, 20).addBox(side < 0 ? -1.6F : 0.0F, -0.2F, -0.35F, 1.6F, 0.7F, 0.7F),
							PartPose.offset(side * (width - 0.15F), 0.35F, 0.5F));
					leg.addOrReplaceChild("claw", CubeListBuilder.create()
							.texOffs(20, 24).addBox(side < 0 ? -0.45F : 0.0F, 0.0F, -0.2F, 0.45F, 1.15F, 0.45F),
							PartPose.offset(side * 1.45F, 0.2F, 0.0F));
				}
			}
			if (i < 5) {
				for (int side : new int[] { -1, 1 }) {
					parent.addOrReplaceChild(side < 0 ? "leftBristle" : "rightBristle", CubeListBuilder.create()
							.texOffs(28, 20).addBox(side < 0 ? -0.45F : 0.0F, -1.5F, -0.2F, 0.45F, 1.6F, 0.45F),
							PartPose.offsetAndRotation(side * (width - 0.25F), -1.0F, 0.9F, 0.0F, 0.0F, side * 0.35F));
				}
			}
		}
		parent.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(0, 25).addBox(-0.65F, -0.95F, -0.3F, 1.3F, 1.7F, 2.1F)
				.texOffs(10, 25).addBox(-0.3F, -0.55F, 1.5F, 0.6F, 0.8F, 1.3F),
				PartPose.offset(0.0F, 0.15F, 1.9F));

		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public void setupAnim(MyelinBorerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float crawling = entity.isCrawling() ? 1.0F : Math.min(1.0F, limbSwingAmount * 2.0F);
		float phase = ageInTicks * 0.55F;
		body.y = 21.8F - crawling * (float) Math.sin(phase) * 0.25F;
		head.yRot = (float) Math.sin(phase + 0.9F) * (0.06F + 0.12F * crawling);
		head.xRot = (float) Math.sin(phase * 0.7F) * 0.04F;
		for (int i = 0; i < rings.length; i++) {
			float wave = (float) Math.sin(phase - i * 0.75F);
			rings[i].yRot = wave * (0.035F + crawling * 0.09F);
			rings[i].xRot = (float) Math.sin(phase - i * 0.75F - 0.6F) * (0.025F + crawling * 0.055F);
			if (i < legs.length) {
				for (int side = 0; side < 2; side++) {
					float step = (float) Math.sin(phase * 1.6F + i * 1.4F + side * (float) Math.PI);
					legs[i][side].yRot = step * (0.08F + crawling * 0.28F) * (side == 0 ? -1.0F : 1.0F);
					legs[i][side].zRot = step * crawling * 0.1F;
				}
			}
		}
		for (int side = 0; side < palps.length; side++) {
			palps[side].yRot = (side == 0 ? -1.0F : 1.0F) * (0.18F + (float) Math.sin(phase * 1.3F + side) * 0.12F);
			palps[side].getChild("tip").xRot = (float) Math.sin(phase * 1.6F + side) * 0.18F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
