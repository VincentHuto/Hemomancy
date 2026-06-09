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
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class PrismCuttleModel extends HierarchicalModel<PrismCuttleEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("prism_cuttle"),
			"main");

	private final ModelPart root;
	private final ModelPart mantle;
	private final ModelPart[] leftFins = new ModelPart[PrismCuttleRules.FIN_SEGMENT_COUNT];
	private final ModelPart[] rightFins = new ModelPart[PrismCuttleRules.FIN_SEGMENT_COUNT];
	private final ModelPart[][] arms = new ModelPart[PrismCuttleRules.FACE_TENTACLE_COUNT][PrismCuttleRules.FACE_TENTACLE_SEGMENT_COUNT];

	public PrismCuttleModel(ModelPart root) {
		this.root = root.getChild("root");
		this.mantle = this.root.getChild("mantle");
		for (int i = 0; i < PrismCuttleRules.FIN_SEGMENT_COUNT; i++) {
			this.leftFins[i] = this.mantle.getChild("left_fin_" + i);
			this.rightFins[i] = this.mantle.getChild("right_fin_" + i);
		}
		for (int i = 0; i < PrismCuttleRules.FACE_TENTACLE_COUNT; i++) {
			ModelPart armRoot = this.root.getChild("arm_" + i);
			this.arms[i][0] = armRoot.getChild("arm_" + i + "_segment_0");
			for (int j = 1; j < PrismCuttleRules.FACE_TENTACLE_SEGMENT_COUNT; j++) {
				this.arms[i][j] = this.arms[i][j - 1].getChild("arm_" + i + "_segment_" + j);
			}
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

		PartDefinition mantle = root.addOrReplaceChild("mantle", CubeListBuilder.create().texOffs(1, 0).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 3.0F, 10.0F, new CubeDeformation(0.1F))
				.texOffs(23, 0).addBox(-3.0F, -3.0F, -8.0F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		for (int i = 0; i < PrismCuttleRules.FIN_SEGMENT_COUNT; i++) {
			float finZ = -3.5F + i * 1.8F;
			mantle.addOrReplaceChild("left_fin_" + i, CubeListBuilder.create().texOffs(28, 17)
							.addBox(0.0F, -0.25F, -0.1F, 0.0F, 4.0F, 2.05F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-3.25F, -1.8F, finZ, 0.0F, 0.0F, 0.12F));
			mantle.addOrReplaceChild("right_fin_" + i, CubeListBuilder.create().texOffs(40, 17)
							.addBox(0.0F, -0.25F, -0.1F, 0.0F, 4.0F, 2.05F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(3.25F, -1.8F, finZ, 0.0F, 0.0F, -0.12F));
		}

		for (int i = 0; i < PrismCuttleRules.FACE_TENTACLE_COUNT; i++) {
			float x = 2.8F - i * 0.8F;
			float yaw = (3.5F - i) * 0.012F;
			float segmentLength = PrismCuttleRules.TENTACLE_SEGMENT_LENGTH;
			PartDefinition arm = root.addOrReplaceChild("arm_" + i, CubeListBuilder.create(),
					PartPose.offsetAndRotation(x, -2.35F, -7.65F, PrismCuttleRules.TENTACLE_REST_X_ROT,
							(3.5F - i) * 0.045F, 0.0F));
			PartDefinition armSegment0 = arm.addOrReplaceChild("arm_" + i + "_segment_0",
					CubeListBuilder.create().texOffs(0, 28)
							.addBox(-0.25F, -0.25F, -segmentLength, 0.5F, 1.0F,
									segmentLength + 0.12F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));
			PartDefinition armSegment1 = armSegment0.addOrReplaceChild("arm_" + i + "_segment_1",
					CubeListBuilder.create().texOffs(0, 29)
							.addBox(-0.2275F, -0.2275F, -segmentLength, 0.455F, 1.0F,
									segmentLength + 0.12F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.0F, 0.0F, -segmentLength, 0.075F, yaw, 0.0F));
			PartDefinition armSegment2 = armSegment1.addOrReplaceChild("arm_" + i + "_segment_2",
					CubeListBuilder.create().texOffs(0, 30)
							.addBox(-0.205F, -0.205F, -segmentLength, 0.41F, 1.0F,
									segmentLength + 0.12F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.0F, 0.0F, -segmentLength, 0.15F, yaw, 0.0F));
			PartDefinition armSegment3 = armSegment2.addOrReplaceChild("arm_" + i + "_segment_3",
					CubeListBuilder.create().texOffs(0, 31)
							.addBox(-0.1825F, -0.1825F, -segmentLength, 0.365F, 1.0F,
									segmentLength + 0.12F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.0F, 0.0F, -segmentLength, 0.225F, yaw, 0.0F));
			armSegment3.addOrReplaceChild("arm_" + i + "_segment_4",
					CubeListBuilder.create().texOffs(0, 32)
							.addBox(-0.16F, -0.16F, -segmentLength, 0.32F, 1.0F,
									segmentLength + 0.12F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.0F, 0.0F, -segmentLength, 0.3F, yaw, 0.0F));
		}
		return LayerDefinition.create(meshdefinition, 64, 64);
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
		for (int i = 0; i < PrismCuttleRules.FIN_SEGMENT_COUNT; i++) {
			float wave = Mth.sin(ageInTicks * 0.3F + i * 0.75F);
			this.leftFins[i].zRot = 1.12F + wave * 0.18F;
			this.leftFins[i].xRot = wave * 0.055F;
			this.rightFins[i].zRot = -1.12F - wave * 0.18F;
			this.rightFins[i].xRot = wave * 0.055F;
		}
		for (int i = 0; i < PrismCuttleRules.FACE_TENTACLE_COUNT; i++) {
			for (int j = 0; j < PrismCuttleRules.FACE_TENTACLE_SEGMENT_COUNT; j++) {
				float segmentWeight = (j + 1.0F) / PrismCuttleRules.FACE_TENTACLE_SEGMENT_COUNT;
				float pitch = j * 0.075F;
				float yaw = j == 0 ? 0.0F : (3.5F - i) * 0.012F;
				float drift = Mth.sin(ageInTicks * 0.22F + i * 0.7F + j * 0.45F);
				this.arms[i][j].xRot = pitch + drift * 0.07F + flash * 0.04F * segmentWeight;
				this.arms[i][j].yRot = yaw + Mth.cos(ageInTicks * 0.16F + i + j * 0.5F) * 0.035F;
				this.arms[i][j].zRot = drift * 0.025F * segmentWeight;
			}
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
