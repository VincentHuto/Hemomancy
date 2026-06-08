package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.PrismCuttleRules;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class PrismCuttleModel extends HierarchicalModel<PrismCuttleEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("prism_cuttle"),
			"main");

	private final ModelPart root;
	private final ModelPart mantle;
	private final ModelPart leftFin;
	private final ModelPart rightFin;
	private final ModelPart[] arms = new ModelPart[8];

	public PrismCuttleModel(ModelPart root) {
		this.root = root.getChild("root");
		this.mantle = this.root.getChild("mantle");
		this.leftFin = this.mantle.getChild("left_fin");
		this.rightFin = this.mantle.getChild("right_fin");
		for (int i = 0; i < this.arms.length; i++) {
			this.arms[i] = this.root.getChild("arm_" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();
		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.offset(0.0F, 21.0F, 0.0F));
		PartDefinition mantle = root.addOrReplaceChild("mantle", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -3.0F, -5.0F, 8.0F, 6.0F, 10.0F, new CubeDeformation(0.1F))
						.texOffs(36, 0).addBox(-3.0F, -2.0F, -8.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(0, 18).addBox(-2.0F, -3.5F, 2.5F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		mantle.addOrReplaceChild("left_fin", CubeListBuilder.create()
						.texOffs(44, 18).addBox(0.0F, -0.25F, -4.0F, 0.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.25F, -0.8F, 0.0F, 0.0F, 0.0F, -0.12F));
		mantle.addOrReplaceChild("right_fin", CubeListBuilder.create()
						.texOffs(62, 18).addBox(0.0F, -0.25F, -4.0F, 0.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.25F, -0.8F, 0.0F, 0.0F, 0.0F, 0.12F));
		for (int i = 0; i < 8; i++) {
			float x = -2.8F + i * 0.8F;
					root.addOrReplaceChild("arm_" + i, CubeListBuilder.create()
							.texOffs(0, 28 + i % 4 * 4).addBox(-0.25F, -0.25F, -0.5F, 0.5F, 0.5F, 7.0F,
									new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(x, 1.8F, -7.0F, PrismCuttleRules.TENTACLE_REST_X_ROT,
							(i - 3.5F) * 0.06F, 0.0F));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(PrismCuttleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float flash = entity.isFlashing() ? 1.0F : 0.0F;
		float pulse = Mth.sin(ageInTicks * 0.18F);
		this.mantle.y += pulse * 0.15F;
		this.mantle.xScale = 1.0F + flash * 0.08F;
		this.mantle.yScale = 1.0F + flash * 0.08F;
		this.leftFin.zRot = -0.18F + Mth.sin(ageInTicks * 0.25F) * 0.22F;
		this.rightFin.zRot = 0.18F - Mth.sin(ageInTicks * 0.25F) * 0.22F;
		for (int i = 0; i < this.arms.length; i++) {
			this.arms[i].xRot = PrismCuttleRules.TENTACLE_REST_X_ROT
					+ Mth.sin(ageInTicks * 0.22F + i * 0.7F) * 0.12F;
			this.arms[i].yRot = (i - 3.5F) * 0.06F + Mth.cos(ageInTicks * 0.16F + i) * 0.08F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
