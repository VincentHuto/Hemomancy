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
	private static final int ANTENNA_SEGMENT_COUNT = VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_COUNT;
	private static final int TAIL_FEELER_SEGMENT_COUNT = VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_COUNT;

	private final ModelPart head;
	private final ModelPart jaws;
	private final ModelPart jawleft;
	private final ModelPart jawright;
	private final ModelPart[] leftAntenna = new ModelPart[ANTENNA_SEGMENT_COUNT];
	private final ModelPart[] rightAntenna = new ModelPart[ANTENNA_SEGMENT_COUNT];
	private final ModelPart[] segments = new ModelPart[SEGMENT_COUNT];
	private final ModelPart[] leftLegs = new ModelPart[SEGMENT_COUNT];
	private final ModelPart[] rightLegs = new ModelPart[SEGMENT_COUNT];
	private final ModelPart[] ribs = new ModelPart[SEGMENT_COUNT];
	private final ModelPart tail;
	private final ModelPart[] leftTailFeeler = new ModelPart[TAIL_FEELER_SEGMENT_COUNT];
	private final ModelPart[] rightTailFeeler = new ModelPart[TAIL_FEELER_SEGMENT_COUNT];

	public VenomRibCentipedeModel(ModelPart root) {
		this.head = root.getChild("head");
		this.jaws = this.head.getChild("jaws");
		this.jawleft = this.jaws.getChild("jawleft");
		this.jawright = this.jaws.getChild("jawright");
		this.leftAntenna[0] = this.head.getChild("left_antenna_0");
		this.rightAntenna[0] = this.head.getChild("right_antenna_0");
		for (int i = 1; i < ANTENNA_SEGMENT_COUNT; i++) {
			this.leftAntenna[i] = this.leftAntenna[i - 1].getChild("left_antenna_" + i);
			this.rightAntenna[i] = this.rightAntenna[i - 1].getChild("right_antenna_" + i);
		}
		this.segments[0] = root.getChild("segment_0");
		for (int i = 0; i < SEGMENT_COUNT; i++) {
			if (i > 0) {
				this.segments[i] = this.segments[i - 1].getChild("segment_" + i);
			}
			this.leftLegs[i] = this.segments[i].getChild("left_legs_" + i);
			this.rightLegs[i] = this.segments[i].getChild("right_legs_" + i);
			this.ribs[i] = this.segments[i].getChild("rib_" + i);
		}
		this.tail = this.segments[SEGMENT_COUNT - 1].getChild("tail");
		this.leftTailFeeler[0] = this.tail.getChild("left_tail_feeler_0");
		this.rightTailFeeler[0] = this.tail.getChild("right_tail_feeler_0");
		for (int i = 1; i < TAIL_FEELER_SEGMENT_COUNT; i++) {
			this.leftTailFeeler[i] = this.leftTailFeeler[i - 1].getChild("left_tail_feeler_" + i);
			this.rightTailFeeler[i] = this.rightTailFeeler[i - 1].getChild("right_tail_feeler_" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -0.85F, -1.2F, 5.0F, 2.75F, 4.2F, new CubeDeformation(0.0F))
				.texOffs(22, 0).addBox(-2.2F, -1.1F, -0.7F, 4.4F, 1.6F, 2.4F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.6F, -9.0F));
		PartDefinition jaws = head.addOrReplaceChild("jaws", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		jaws.addOrReplaceChild("jawleft", CubeListBuilder.create().texOffs(50, 50).addBox(-0.75F, -0.5F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(49, 57).addBox(-1.25F, -0.5F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.75F, 1.0F, 0.0F));

		jaws.addOrReplaceChild("jawright", CubeListBuilder.create().texOffs(49, 57).mirror().addBox(-0.75F, -0.5F, -5.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(50, 50).mirror().addBox(-1.25F, -0.5F, -2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.75F, 1.0F, 0.0F));

		PartDefinition leftAntennaParent = head;
		PartDefinition rightAntennaParent = head;
		for (int i = 0; i < ANTENNA_SEGMENT_COUNT; i++) {
			float segmentLength = VenomRibCentipedeSlitherRules.ANTENNA_SEGMENT_LENGTH;
			float taper = 0.28F - i * 0.025F;
			float curlYaw = 0.18F + i * 0.055F;
			float pitch = -0.1F + i * 0.015F;
			leftAntennaParent = leftAntennaParent.addOrReplaceChild("left_antenna_" + i,
					CubeListBuilder.create().texOffs(34, 38 + i)
							.addBox(-taper / 2.0F, -taper / 2.0F, -segmentLength,
									taper, taper, segmentLength + 0.08F, new CubeDeformation(0.0F)),
					i == 0 ? PartPose.offsetAndRotation(1.35F, -0.85F, -0.9F, pitch, -curlYaw, 0.08F)
							: PartPose.offsetAndRotation(0.0F, 0.0F, -segmentLength, pitch, -curlYaw, 0.03F));
			rightAntennaParent = rightAntennaParent.addOrReplaceChild("right_antenna_" + i,
					CubeListBuilder.create().texOffs(44, 38 + i)
							.addBox(-taper / 2.0F, -taper / 2.0F, -segmentLength,
									taper, taper, segmentLength + 0.08F, new CubeDeformation(0.0F)),
					i == 0 ? PartPose.offsetAndRotation(-1.35F, -0.85F, -0.9F, pitch, curlYaw, -0.08F)
							: PartPose.offsetAndRotation(0.0F, 0.0F, -segmentLength, pitch, curlYaw, -0.03F));
		}

		PartDefinition parent = root;
		for (int i = 0; i < SEGMENT_COUNT; i++) {
			float width = i < 3 ? 6.0F : i < 6 ? 5.2F : 4.4F;
			float height = i < 4 ? 3.0F : 2.5F;
			PartDefinition segment = parent.addOrReplaceChild("segment_" + i, CubeListBuilder.create()
							.texOffs(0, 12).addBox(-width / 2.0F, -height / 2.0F, -1.5F,
									width, height, 3.5F, new CubeDeformation(0.0F)),
					i == 0 ? PartPose.offset(0.0F, 22.0F, -5.5F)
							: PartPose.offset(0.0F, 0.0F, VenomRibCentipedeSlitherRules.BODY_SEGMENT_SPACING));
			segment.addOrReplaceChild("rib_" + i, CubeListBuilder.create()
							.texOffs(5, 35 ).addBox(-width / 2.0F - 0.4F, -2.15F, -1.2F,
									width + 0.8F, 1.0F, 2.4F, new CubeDeformation(0.0F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));
			segment.addOrReplaceChild("left_legs_" + i, CubeListBuilder.create()
							.texOffs(46, 33).addBox(0.0F, -0.25F, -1.3F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F))
							.texOffs(46, 33).addBox(0.0F, -0.25F, 0.9F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
					PartPose.offset(width / 2.0F - 0.2F, 0.8F, 0.0F));
			segment.addOrReplaceChild("right_legs_" + i, CubeListBuilder.create()
							.texOffs(40, 28).addBox(-6.0F, -0.25F, -1.3F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F))
							.texOffs(40, 31).addBox(-6.0F, -0.25F, 0.9F, 6.0F, 0.5F, 0.5F, new CubeDeformation(0.0F)),
					PartPose.offset(-width / 2.0F + 0.2F, 0.8F, 0.0F));
			parent = segment;
		}
		PartDefinition tail = parent.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(0, 54).addBox(-1.8F, -1.1F, -2.2F, 3.6F, 2.1F, 2.7F,
								new CubeDeformation(0.0F))
						.texOffs(32, 31).addBox(-1.35F, -1.15F, 0.2F, 2.7F, 1.45F, 1.4F,
								new CubeDeformation(0.0F)),
				PartPose.offset(-0.25F, 0.45F, VenomRibCentipedeSlitherRules.BODY_SEGMENT_SPACING - 0.95F));
		PartDefinition leftTailParent = tail;
		PartDefinition rightTailParent = tail;
		for (int i = 0; i < TAIL_FEELER_SEGMENT_COUNT; i++) {
			float segmentLength = VenomRibCentipedeSlitherRules.TAIL_FEELER_SEGMENT_LENGTH;
			float taper = 0.3F - i * 0.022F;
			float yaw = 0.16F + i * 0.032F;
			float pitch = 0.05F + i * 0.015F;
			leftTailParent = leftTailParent.addOrReplaceChild("left_tail_feeler_" + i,
					CubeListBuilder.create().texOffs(50, 38 + i)
							.addBox(-taper / 2.0F, -taper / 2.0F, -0.12F,
									taper, taper, segmentLength + 0.18F, new CubeDeformation(0.0F)),
					i == 0 ? PartPose.offsetAndRotation(0.95F, 0.35F, 1.45F, pitch, yaw, 0.1F)
							: PartPose.offsetAndRotation(0.0F, 0.0F, segmentLength, pitch, yaw, 0.025F));
			rightTailParent = rightTailParent.addOrReplaceChild("right_tail_feeler_" + i,
					CubeListBuilder.create().texOffs(58, 38 + i)
							.addBox(-taper / 2.0F, -taper / 2.0F, -0.12F,
									taper, taper, segmentLength + 0.18F, new CubeDeformation(0.0F)),
					i == 0 ? PartPose.offsetAndRotation(-0.95F, 0.35F, 1.45F, pitch, -yaw, -0.1F)
							: PartPose.offsetAndRotation(0.0F, 0.0F, segmentLength, pitch, -yaw, -0.025F));
		}
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(VenomRibCentipedeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float movement = Math.max(limbSwingAmount, entity.areRibsRaised() ? 0.2F : 0.05F);
		float waveTime = limbSwing * 0.85F + ageInTicks * 0.08F;
		float strike = entity.getStrikeProgress(0.0F);
		float jawClack = (float) Math.sin(ageInTicks * 0.3F) * 0.15F + strike * 0.2F;
		float antennaSweep = Mth.clamp(limbSwingAmount, 0.0F, 1.0F) * VenomRibCentipedeSlitherRules.ANTENNA_BACK_SWEEP;
		float leftTwitch = VenomRibCentipedeSlitherRules.antennaTwitch(ageInTicks, true);
		float rightTwitch = VenomRibCentipedeSlitherRules.antennaTwitch(ageInTicks, false);
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.25F;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.2F - strike * 0.35F;
		this.jaws.xRot = 0.0F;
		this.jaws.yRot = 0.0F;
		this.jawleft.yRot = jawClack;
		this.jawright.yRot = -jawClack;
		for (int i = 0; i < ANTENNA_SEGMENT_COUNT; i++) {
			float segmentWeight = (i + 1.0F) / ANTENNA_SEGMENT_COUNT;
			float leftCurl = VenomRibCentipedeSlitherRules.antennaCurlYaw(i, true);
			float rightCurl = VenomRibCentipedeSlitherRules.antennaCurlYaw(i, false);
			float pitch = VenomRibCentipedeSlitherRules.antennaCurlPitch(i);
			float localLeftTwitch = leftTwitch * (0.55F + segmentWeight) + Mth.sin(ageInTicks * 0.21F + i * 0.9F) * 0.025F;
			float localRightTwitch = rightTwitch * (0.55F + segmentWeight) + Mth.sin(ageInTicks * 0.19F + i * 0.85F + 1.4F) * 0.025F;
			this.leftAntenna[i].xRot = pitch - antennaSweep * (0.08F + segmentWeight * 0.12F) + localLeftTwitch * 0.22F;
			this.leftAntenna[i].yRot = leftCurl - antennaSweep * segmentWeight + localLeftTwitch;
			this.leftAntenna[i].zRot = (i == 0 ? 0.08F : 0.03F) + localLeftTwitch * 0.45F;
			this.rightAntenna[i].xRot = pitch - antennaSweep * (0.08F + segmentWeight * 0.12F) + localRightTwitch * 0.22F;
			this.rightAntenna[i].yRot = rightCurl + antennaSweep * segmentWeight - localRightTwitch;
			this.rightAntenna[i].zRot = (i == 0 ? -0.08F : -0.03F) - localRightTwitch * 0.45F;
		}
		for (int i = 0; i < SEGMENT_COUNT; i++) {
			this.segments[i].yRot = VenomRibCentipedeSlitherRules.segmentLocalYaw(i, waveTime, movement);
			this.segments[i].xRot = VenomRibCentipedeSlitherRules.segmentLocalPitch(i, waveTime, movement);
			this.leftLegs[i].zRot = VenomRibCentipedeSlitherRules.legPitch(i, true, waveTime, movement);
			this.rightLegs[i].zRot = -VenomRibCentipedeSlitherRules.legPitch(i, false, waveTime, movement);
			this.leftLegs[i].y = 0.8F + VenomRibCentipedeSlitherRules.legLift(i, true, waveTime, movement);
			this.rightLegs[i].y = 0.8F + VenomRibCentipedeSlitherRules.legLift(i, false, waveTime, movement);
			this.ribs[i].xRot = entity.areRibsRaised() ? -0.25F : 0.0F;
		}
		this.tail.yRot = VenomRibCentipedeSlitherRules.segmentLocalYaw(SEGMENT_COUNT, waveTime, movement) * 0.65F;
		this.tail.xRot = VenomRibCentipedeSlitherRules.segmentLocalPitch(SEGMENT_COUNT, waveTime, movement);
		for (int i = 0; i < TAIL_FEELER_SEGMENT_COUNT; i++) {
			float segmentWeight = (i + 1.0F) / TAIL_FEELER_SEGMENT_COUNT;
			float leftTwitchTail = VenomRibCentipedeSlitherRules.tailFeelerTwitch(i, ageInTicks, true);
			float rightTwitchTail = VenomRibCentipedeSlitherRules.tailFeelerTwitch(i, ageInTicks, false);
			float yaw = 0.16F + i * 0.032F;
			float pitch = 0.05F + i * 0.015F;
			this.leftTailFeeler[i].xRot = pitch + leftTwitchTail * 0.4F;
			this.leftTailFeeler[i].yRot = yaw + movement * 0.08F * segmentWeight + leftTwitchTail;
			this.leftTailFeeler[i].zRot = (i == 0 ? 0.1F : 0.025F) + leftTwitchTail * 0.35F;
			this.rightTailFeeler[i].xRot = pitch + rightTwitchTail * 0.4F;
			this.rightTailFeeler[i].yRot = -yaw - movement * 0.08F * segmentWeight - rightTwitchTail;
			this.rightTailFeeler[i].zRot = -(i == 0 ? -0.1F : -0.025F) - rightTwitchTail * 0.35F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		this.segments[0].render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
