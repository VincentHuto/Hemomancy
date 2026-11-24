package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class SanguineHoundModel extends HierarchicalModel<SanguineHoundEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("sanguine_hound"), "main");

	private final ModelPart root;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart tail;
	private final ModelPart leftEar;
	private final ModelPart rightEar;
	private final ModelPart foreLeft;
	private final ModelPart foreRight;
	private final ModelPart hindLeft;
	private final ModelPart hindRight;

	public SanguineHoundModel(ModelPart bakedRoot) {
		root = bakedRoot.getChild("hound");
		ModelPart body = root.getChild("body");
		neck = body.getChild("neck");
		head = neck.getChild("head");
		jaw = head.getChild("jaw");
		tail = body.getChild("tail");
		leftEar = head.getChild("left_ear");
		rightEar = head.getChild("right_ear");
		foreLeft = body.getChild("fore_left");
		foreRight = body.getChild("fore_right");
		hindLeft = body.getChild("hind_left");
		hindRight = body.getChild("hind_right");
	}

	// BEGIN GENERATED HOUND GEOMETRY
	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition hound = mesh.getRoot().addOrReplaceChild("hound", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
		PartDefinition body = hound.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(1, 1).addBox(-3F, -4F, -7F, 6F, 8F, 8F)
				.texOffs(30, 1).addBox(-2F, -3F, 1F, 4F, 4F, 5F)
				.texOffs(49, 1).addBox(-2.5F, -3F, 6F, 5F, 5F, 5F)
				.texOffs(70, 1).addBox(-2F, -5F, -6F, 4F, 1F, 6F)
				.texOffs(91, 1).addBox(-1F, -4F, 0F, 2F, 1F, 10F)
				.texOffs(1, 18).addBox(-3.5F, -5.5F, -4F, 7F, 10F, 2F)
				.texOffs(20, 18).addBox(-3F, -3.5F, 7F, 6F, 6F, 1F)
				.texOffs(35, 18).addBox(-4F, -2F, -4.5F, 1F, 2F, 3F)
				.texOffs(44, 18).addBox(3F, -2F, -4.5F, 1F, 2F, 3F), PartPose.offset(0F, -13F, 0F));
		PartDefinition string_anchor__4 = body.addOrReplaceChild("string_anchor_-4", CubeListBuilder.create()
				.texOffs(53, 18).addBox(-1F, 0F, 0F, 2F, 1F, 2F)
				.texOffs(62, 18).addBox(-0.5F, -1F, 0F, 1F, 1F, 1F), PartPose.offset(0F, -6F, -4F));
		PartDefinition string_anchor_7 = body.addOrReplaceChild("string_anchor_7", CubeListBuilder.create()
				.texOffs(67, 18).addBox(-1F, 0F, 0F, 2F, 1F, 2F)
				.texOffs(76, 18).addBox(-0.5F, -1F, 0F, 1F, 1F, 1F), PartPose.offset(0F, -4F, 7F));
		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
				.texOffs(81, 18).addBox(-2F, -6F, -2F, 4F, 7F, 4F)
				.texOffs(98, 18).addBox(-2.5F, -4F, -2.5F, 5F, 2F, 5F), PartPose.offsetAndRotation(0F, -2F, -6F, 0.4712389F, 0F, -0F));
		PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(1, 31).addBox(-2F, -2.5F, -3F, 4F, 4F, 5F)
				.texOffs(20, 31).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 4F)
				.texOffs(53, 31).addBox(-2F, -2F, -6F, 4F, 4F, 1F)
				.texOffs(95, 31).addBox(-2F, 0.5F, -6F, 1F, 1F, 5F)
				.texOffs(108, 31).addBox(-2.5F, 0.5F, -2F, 1F, 1F, 1F)
				.texOffs(86, 42).addBox(1F, 0.5F, -6F, 1F, 1F, 5F)
				.texOffs(99, 42).addBox(1.5F, 0.5F, -2F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, -5F, -3F, -0.4712389F, 0F, -0F));
		PartDefinition snout_hook = head.addOrReplaceChild("snout_hook", CubeListBuilder.create()
				.texOffs(1, 115).addBox(-1F, -0.75F, -4.25F, 2F, 2F, 5F), PartPose.offsetAndRotation(0F, 0F, -7F, 0.36651914F, 0F, -0F));
		PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create()
				.texOffs(64, 31).addBox(-1.5F, 0.5F, -6F, 3F, 1F, 6F), PartPose.offset(0F, 1F, 0F));
		PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create()
				.texOffs(83, 31).addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F)
				.texOffs(88, 31).addBox(-0.5F, -6F, -1F, 1F, 1F, 2F), PartPose.offsetAndRotation(-1.25F, -2F, 0F, 0.17453293F, 0F, -0.13962634F));
		PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create()
				.texOffs(74, 42).addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F)
				.texOffs(79, 42).addBox(-0.5F, -6F, -1F, 1F, 1F, 2F), PartPose.offsetAndRotation(1.25F, -2F, 0F, 0.17453293F, 0F, 0.13962634F));
		PartDefinition fore_right = body.addOrReplaceChild("fore_right", CubeListBuilder.create()
				.texOffs(113, 31).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(-2.5F, 2F, -5F));
		PartDefinition fore_right_shin = fore_right.addOrReplaceChild("fore_right_shin", CubeListBuilder.create()
				.texOffs(1, 42).addBox(-1F, 0F, -1F, 2F, 2F, 2F)
				.texOffs(10, 42).addBox(-2F, 0.5F, -0.5F, 1F, 1F, 1F)
				.texOffs(15, 42).addBox(-0.5F, 2F, -0.5F, 1F, 4F, 1F)
				.texOffs(20, 42).addBox(-1F, 6F, -2F, 2F, 1F, 3F), PartPose.offset(0F, 4F, 0F));
		PartDefinition hind_right = body.addOrReplaceChild("hind_right", CubeListBuilder.create()
				.texOffs(31, 42).addBox(-1.5F, -1F, -1.5F, 3F, 7F, 3F), PartPose.offsetAndRotation(-2F, 1F, 8F, 0.2268928F, 0F, -0F));
		PartDefinition hind_right_shin = hind_right.addOrReplaceChild("hind_right_shin", CubeListBuilder.create()
				.texOffs(44, 42).addBox(-1F, 0F, -1F, 2F, 2F, 2F)
				.texOffs(53, 42).addBox(-2F, 0.5F, -0.5F, 1F, 1F, 1F)
				.texOffs(58, 42).addBox(-0.5F, 2F, -0.5F, 1F, 4F, 1F)
				.texOffs(63, 42).addBox(-1F, 6F, -2F, 2F, 1F, 3F), PartPose.offsetAndRotation(0F, 5F, 0F, -0.45378561F, 0F, -0F));
		PartDefinition fore_left = body.addOrReplaceChild("fore_left", CubeListBuilder.create()
				.texOffs(104, 42).addBox(-1.5F, -2F, -1.5F, 3F, 7F, 3F), PartPose.offset(2.5F, 2F, -5F));
		PartDefinition fore_left_shin = fore_left.addOrReplaceChild("fore_left_shin", CubeListBuilder.create()
				.texOffs(117, 42).addBox(-1F, 0F, -1F, 2F, 2F, 2F)
				.texOffs(1, 53).addBox(1F, 0.5F, -0.5F, 1F, 1F, 1F)
				.texOffs(6, 53).addBox(-0.5F, 2F, -0.5F, 1F, 4F, 1F)
				.texOffs(11, 53).addBox(-1F, 6F, -2F, 2F, 1F, 3F), PartPose.offset(0F, 4F, 0F));
		PartDefinition hind_left = body.addOrReplaceChild("hind_left", CubeListBuilder.create()
				.texOffs(22, 53).addBox(-1.5F, -1F, -1.5F, 3F, 7F, 3F), PartPose.offsetAndRotation(2F, 1F, 8F, 0.2268928F, 0F, -0F));
		PartDefinition hind_left_shin = hind_left.addOrReplaceChild("hind_left_shin", CubeListBuilder.create()
				.texOffs(35, 53).addBox(-1F, 0F, -1F, 2F, 2F, 2F)
				.texOffs(44, 53).addBox(1F, 0.5F, -0.5F, 1F, 1F, 1F)
				.texOffs(49, 53).addBox(-0.5F, 2F, -0.5F, 1F, 4F, 1F)
				.texOffs(54, 53).addBox(-1F, 6F, -2F, 2F, 1F, 3F), PartPose.offsetAndRotation(0F, 5F, 0F, -0.45378561F, 0F, -0F));
		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(65, 53).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 8F), PartPose.offsetAndRotation(0F, -1F, 10F, 0.73303829F, 0F, -0F));
		PartDefinition tail_fork = tail.addOrReplaceChild("tail_fork", CubeListBuilder.create()
				.texOffs(84, 53).addBox(-1.5F, -0.5F, -1F, 3F, 1F, 1F), PartPose.offsetAndRotation(0F, 0F, 8F, -0.17453293F, 0F, -0F));
		PartDefinition tine__1 = tail_fork.addOrReplaceChild("tine_-1", CubeListBuilder.create()
				.texOffs(93, 53).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 4F)
				.texOffs(104, 53).addBox(-0.5F, -0.5F, 4F, 1F, 1F, 1F), PartPose.offsetAndRotation(-1F, 0F, 0F, -0F, -0.27925268F, -0F));
		PartDefinition tine_1 = tail_fork.addOrReplaceChild("tine_1", CubeListBuilder.create()
				.texOffs(109, 53).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 4F)
				.texOffs(120, 53).addBox(-0.5F, -0.5F, 4F, 1F, 1F, 1F), PartPose.offsetAndRotation(1F, 0F, 0F, -0F, 0.27925268F, -0F));
		return LayerDefinition.create(mesh, 128, 128);
	}
	// END GENERATED HOUND GEOMETRY

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(SanguineHoundEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float stride = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
		float phase = limbSwing * 0.6662F;
		step(foreLeft, "fore_left_shin", phase, stride, false);
		step(foreRight, "fore_right_shin", phase + Mth.PI, stride, false);
		step(hindLeft, "hind_left_shin", phase + Mth.PI + 0.25F, stride, true);
		step(hindRight, "hind_right_shin", phase + 0.25F, stride, true);

		float bite = Mth.sin(attackTime * Mth.PI);
		float breath = Mth.sin(ageInTicks * 0.14F);
		neck.xRot += 0.05F * breath * (1.0F - stride) + 0.17F * bite;
		head.yRot += Mth.clamp(netHeadYaw, -55.0F, 55.0F) * Mth.DEG_TO_RAD;
		head.xRot += Mth.clamp(headPitch, -35.0F, 35.0F) * Mth.DEG_TO_RAD - 0.15F * bite;
		jaw.xRot += 0.24F * bite;
		tail.yRot += Mth.sin(ageInTicks * 0.12F + stride * Mth.sin(phase)) * (0.10F + 0.15F * stride);
		leftEar.xRot += 0.05F * Mth.sin(ageInTicks * 0.17F) + 0.16F * stride;
		rightEar.xRot += 0.05F * Mth.sin(ageInTicks * 0.17F + 0.8F) + 0.16F * stride;
	}

	private static void step(ModelPart leg, String shinName, float phase, float stride, boolean hind) {
		leg.xRot += Mth.cos(phase) * 0.70F * stride;
		float lift = Math.max(0.0F, -Mth.sin(phase));
		leg.getChild(shinName).xRot += lift * stride * (hind ? -0.40F : 0.50F);
	}
}
