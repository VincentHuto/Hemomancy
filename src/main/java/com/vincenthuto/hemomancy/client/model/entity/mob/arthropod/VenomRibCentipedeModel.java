package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.VenomRibCentipedeEntity;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.VenomRibCentipedeSlitherRules;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class VenomRibCentipedeModel extends EntityModel<VenomRibCentipedeEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("venom_rib_centipede"), "main");

	private static final int SEGMENT_COUNT = VenomRibCentipedeSlitherRules.BODY_SEGMENT_COUNT;

	private final ModelPart head;
	private final ModelPart[] segments = new ModelPart[SEGMENT_COUNT];
	private final ModelPart[] leftLegs = new ModelPart[SEGMENT_COUNT];
	private final ModelPart[] rightLegs = new ModelPart[SEGMENT_COUNT];
	private final ModelPart[] ribs = new ModelPart[SEGMENT_COUNT];

	public VenomRibCentipedeModel(ModelPart root) {
		this.head = root.getChild("head");
		this.segments[0] = root.getChild("segment_0");
		for (int i = 0; i < SEGMENT_COUNT; i++) {
			if (i > 0) {
				this.segments[i] = this.segments[i - 1].getChild("segment_" + i);
			}
			this.leftLegs[i] = this.segments[i].getChild("left_legs_" + i);
			this.rightLegs[i] = this.segments[i].getChild("right_legs_" + i);
			this.ribs[i] = this.segments[i].getChild("rib_" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.5F, -1.85F, -4.2F, 5.0F, 2.75F, 4.2F, new CubeDeformation(0.0F))
						.texOffs(22, 0).addBox(-1.5F, -0.9F, -6.2F, 3.0F, 0.9F, 2.4F, new CubeDeformation(0.0F))
						.texOffs(34, 0).addBox(-2.2F, -3.1F, -1.7F, 4.4F, 1.6F, 2.4F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.6F, -9.0F));

		PartDefinition parent = root;
		for (int i = 0; i < SEGMENT_COUNT; i++) {
			float width = i < 3 ? 6.0F : i < 6 ? 5.2F : 4.4F;
			float height = i < 4 ? 3.0F : 2.5F;
			PartDefinition segment = parent.addOrReplaceChild("segment_" + i, CubeListBuilder.create()
							.texOffs(0, 12 + (i % 4) * 8).addBox(-width / 2.0F, -height / 2.0F, -1.5F,
									width, height, 3.5F, new CubeDeformation(0.0F)),
					i == 0 ? PartPose.offset(0.0F, 22.0F, -5.5F)
							: PartPose.offset(0.0F, 0.0F, VenomRibCentipedeSlitherRules.BODY_SEGMENT_SPACING));
			segment.addOrReplaceChild("rib_" + i, CubeListBuilder.create()
							.texOffs(52, 0 + (i % 4) * 4).addBox(-width / 2.0F - 0.4F, -2.4F, -1.2F,
									width + 0.8F, 1.0F, 2.4F, new CubeDeformation(0.0F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));
			segment.addOrReplaceChild("left_legs_" + i, CubeListBuilder.create()
							.texOffs(40, 22).addBox(0.0F, -0.25F, -1.3F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F))
							.texOffs(40, 25).addBox(0.0F, -0.25F, 0.9F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
					PartPose.offset(width / 2.0F - 0.2F, 0.8F, 0.0F));
			segment.addOrReplaceChild("right_legs_" + i, CubeListBuilder.create()
							.texOffs(40, 28).addBox(-6.0F, -0.25F, -1.3F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F))
							.texOffs(40, 31).addBox(-6.0F, -0.25F, 0.9F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
					PartPose.offset(-width / 2.0F + 0.2F, 0.8F, 0.0F));
			parent = segment;
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(VenomRibCentipedeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float movement = Math.max(limbSwingAmount, entity.areRibsRaised() ? 0.2F : 0.05F);
		float waveTime = limbSwing * 0.85F + ageInTicks * 0.08F;
		float strike = entity.getStrikeProgress(0.0F);
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.25F;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.2F - strike * 0.35F;
		for (int i = 0; i < SEGMENT_COUNT; i++) {
			this.segments[i].yRot = VenomRibCentipedeSlitherRules.segmentLocalYaw(i, waveTime, movement);
			this.segments[i].xRot = VenomRibCentipedeSlitherRules.segmentLocalPitch(i, waveTime, movement);
			this.leftLegs[i].zRot = VenomRibCentipedeSlitherRules.legPitch(i, true, waveTime, movement);
			this.rightLegs[i].zRot = -VenomRibCentipedeSlitherRules.legPitch(i, false, waveTime, movement);
			this.leftLegs[i].y = 0.8F + VenomRibCentipedeSlitherRules.legLift(i, true, waveTime, movement);
			this.rightLegs[i].y = 0.8F + VenomRibCentipedeSlitherRules.legLift(i, false, waveTime, movement);
			this.ribs[i].xRot = entity.areRibsRaised() ? -0.25F : 0.0F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		this.segments[0].render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
