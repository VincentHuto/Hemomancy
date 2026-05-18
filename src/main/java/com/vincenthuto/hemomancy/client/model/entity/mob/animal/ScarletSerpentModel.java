package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentSlitherRules;
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

public class ScarletSerpentModel extends EntityModel<ScarletSerpentEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("scarlet_serpent"), "main");

	private static final int SEGMENT_COUNT = 9;

	private final ModelPart head;
	private final ModelPart neck;
	private final ModelPart leftHood;
	private final ModelPart rightHood;
	private final ModelPart tail;
	private final ModelPart[] segments = new ModelPart[SEGMENT_COUNT];

	public ScarletSerpentModel(ModelPart root) {
		this.head = root.getChild("head");
		this.neck = root.getChild("neck");
		this.leftHood = this.head.getChild("left_hood");
		this.rightHood = this.head.getChild("right_hood");
		this.segments[0] = root.getChild("segment_0");
		for (int i = 1; i < SEGMENT_COUNT; i++) {
			this.segments[i] = this.segments[i - 1].getChild("segment_" + i);
		}
		this.tail = this.segments[SEGMENT_COUNT - 1].getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot();

		root.addOrReplaceChild("neck", CubeListBuilder.create()
						.texOffs(0, 12).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.0F, -8.0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.0F, -1.5F, -4.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(16, 0).addBox(-1.0F, -0.5F, -6.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.75F, -11.0F));
		head.addOrReplaceChild("left_hood", CubeListBuilder.create()
						.texOffs(28, 0).addBox(0.0F, -4.0F, -1.0F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, 0.0F, -2.0F));
		head.addOrReplaceChild("right_hood", CubeListBuilder.create()
						.texOffs(38, 0).addBox(0.0F, -4.0F, -1.0F, 0.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, 0.0F, -2.0F));

		PartDefinition segment0 = root.addOrReplaceChild("segment_0", CubeListBuilder.create()
						.texOffs(0, 20).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 22.5F, -5.0F));
		PartDefinition segment1 = segment0.addOrReplaceChild("segment_1", CubeListBuilder.create()
						.texOffs(0, 26).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));
		PartDefinition segment2 = segment1.addOrReplaceChild("segment_2", CubeListBuilder.create()
						.texOffs(0, 32).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));
		PartDefinition segment3 = segment2.addOrReplaceChild("segment_3", CubeListBuilder.create()
						.texOffs(0, 38).addBox(-1.75F, -1.0F, -2.0F, 3.5F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));
		PartDefinition segment4 = segment3.addOrReplaceChild("segment_4", CubeListBuilder.create()
						.texOffs(0, 20).addBox(-1.75F, -1.0F, -2.0F, 3.5F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));
		PartDefinition segment5 = segment4.addOrReplaceChild("segment_5", CubeListBuilder.create()
						.texOffs(0, 26).addBox(-1.75F, -1.0F, -2.0F, 3.5F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));
		PartDefinition segment6 = segment5.addOrReplaceChild("segment_6", CubeListBuilder.create()
						.texOffs(0, 32).addBox(-1.5F, -0.75F, -2.0F, 3.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));
		PartDefinition segment7 = segment6.addOrReplaceChild("segment_7", CubeListBuilder.create()
						.texOffs(0, 38).addBox(-1.5F, -0.75F, -2.0F, 3.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));
		PartDefinition segment8 = segment7.addOrReplaceChild("segment_8", CubeListBuilder.create()
						.texOffs(0, 20).addBox(-1.5F, -0.75F, -2.0F, 3.0F, 1.5F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.25F));

		segment8.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(24, 22).addBox(-1.0F, -0.5F, 0.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 2.0F));

		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public void setupAnim(ScarletSerpentEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float movement = Math.max(limbSwingAmount, entity.isHoodFlared() ? 0.15F : 0.05F);
		float waveTime = limbSwing * 0.9F + ageInTicks * 0.08F;
		float strike = entity.getStrikeProgress(0.0F);
		float hood = entity.isHoodFlared() ? 1.0F : 0.0F;

		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
		this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.25F - strike * 0.45F;
		this.neck.xRot = -strike * 0.25F;
		this.neck.yRot = Mth.sin(waveTime + 0.35F) * 0.08F * movement;
		this.leftHood.yRot = Mth.lerp(hood, -0.2F, 0.9F);
		this.rightHood.yRot = Mth.lerp(hood, 0.2F, -0.9F);
		this.leftHood.zRot = Mth.lerp(hood, 0.0F, -0.12F);
		this.rightHood.zRot = Mth.lerp(hood, 0.0F, 0.12F);

		for (int i = 0; i < SEGMENT_COUNT; i++) {
			ModelPart segment = this.segments[i];
			segment.yRot = ScarletSerpentSlitherRules.segmentLocalYaw(i, waveTime, movement);
			segment.xRot = ScarletSerpentSlitherRules.segmentLocalPitch(i, waveTime, movement);
		}
		this.tail.yRot = ScarletSerpentSlitherRules.tailLocalYaw(waveTime, movement);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		this.neck.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		this.head.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		this.segments[0].render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
