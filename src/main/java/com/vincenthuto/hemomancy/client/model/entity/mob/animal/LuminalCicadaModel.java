package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LuminalCicadaRules;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public final class LuminalCicadaModel extends EntityModel<LuminalCicadaEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("luminal_cicada"), "main");
	private final ModelPart root;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart abdomen;
	private final ModelPart leftFeeler;
	private final ModelPart rightFeeler;
	private final ModelPart[] leftLegs;
	private final ModelPart[] rightLegs;

	public LuminalCicadaModel(ModelPart root) {
		super(RenderType::entityCutoutNoCull);
		this.root = root.getChild("luminal_cicada");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.abdomen = this.root.getChild("abdomen");
		ModelPart head = this.root.getChild("head");
		this.leftFeeler = head.getChild("left_feeler");
		this.rightFeeler = head.getChild("right_feeler");
		this.leftLegs = new ModelPart[] {
				this.root.getChild("left_leg_1"),
				this.root.getChild("left_leg_2"),
				this.root.getChild("left_leg_3")
		};
		this.rightLegs = new ModelPart[] {
				this.root.getChild("right_leg_1"),
				this.root.getChild("right_leg_2"),
				this.root.getChild("right_leg_3")
		};
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition luminalCicada = mesh.getRoot().addOrReplaceChild("luminal_cicada", CubeListBuilder.create(),
				PartPose.offset(0F, 24F, 0F));
		PartDefinition head = luminalCicada.addOrReplaceChild("head", CubeListBuilder.create(),
				PartPose.offset(0F, -6F, -6F));
		head.addOrReplaceChild("broad_head", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4F, -10F, -12F, 8F, 6F, 6F),
				PartPose.offset(0F, 6F, 6F));
		head.addOrReplaceChild("left_compound_eye", CubeListBuilder.create().texOffs(40, 15)
				.addBox(-6F, -10F, -12F, 2F, 4F, 4F),
				PartPose.offset(0F, 6F, 6F));
		head.addOrReplaceChild("left_feeler", CubeListBuilder.create().texOffs(53, 15)
				.addBox(-1F, -1F, -5F, 2F, 2F, 5F),
				PartPose.offsetAndRotation(-3F, -3F, -6F, -0.2617994F, 0.4799655F, 0F));
		head.addOrReplaceChild("right_compound_eye", CubeListBuilder.create().texOffs(91, 30)
				.addBox(4F, -10F, -12F, 2F, 4F, 4F),
				PartPose.offset(0F, 6F, 6F));
		head.addOrReplaceChild("right_feeler", CubeListBuilder.create().texOffs(104, 30)
				.addBox(-1F, -1F, -5F, 2F, 2F, 5F),
				PartPose.offsetAndRotation(3F, -3F, -6F, -0.2617994F, -0.4799655F, 0F));
		PartDefinition thorax = luminalCicada.addOrReplaceChild("thorax", CubeListBuilder.create(),
				PartPose.offset(0F, -4F, -4F));
		thorax.addOrReplaceChild("pronotum", CubeListBuilder.create().texOffs(29, 0)
				.addBox(-6F, -10F, -6F, 12F, 6F, 6F),
				PartPose.offset(0F, 4F, 4F));
		thorax.addOrReplaceChild("wing_saddle", CubeListBuilder.create().texOffs(66, 0)
				.addBox(-4F, -8F, 0F, 8F, 2F, 4F),
				PartPose.offset(0F, 4F, 4F));
		PartDefinition abdomen = luminalCicada.addOrReplaceChild("abdomen", CubeListBuilder.create(),
				PartPose.offset(0F, -4F, 0F));
		abdomen.addOrReplaceChild("segmented_abdomen", CubeListBuilder.create().texOffs(91, 0)
				.addBox(-4F, -8F, 0F, 8F, 6F, 8F),
				PartPose.offset(0F, 4F, 0F));
		abdomen.addOrReplaceChild("lantern_ring", CubeListBuilder.create().texOffs(0, 15)
				.addBox(-4F, -6F, 8F, 8F, 4F, 2F),
				PartPose.offset(0F, 4F, 0F));
		abdomen.addOrReplaceChild("lantern_tip", CubeListBuilder.create().texOffs(21, 15)
				.addBox(-3F, -6F, 10F, 6F, 4F, 3F),
				PartPose.offset(0F, 4F, 0F));
		PartDefinition leftWing = luminalCicada.addOrReplaceChild("left_wing", CubeListBuilder.create(),
				PartPose.offset(-3F, -8F, -4F));
		leftWing.addOrReplaceChild("left_forewing", CubeListBuilder.create().texOffs(68, 15)
				.addBox(-4F, -2F, 0F, 7F, 2F, 12F),
				PartPose.offsetAndRotation(0F, 0F, 0F, -0.122173F, -0.0872665F, -0.2617994F));
		leftWing.addOrReplaceChild("left_forewing_tip", CubeListBuilder.create().texOffs(107, 15)
				.addBox(-2F, -2F, 12F, 5F, 2F, 4F),
				PartPose.offsetAndRotation(0F, 0F, 0F, -0.122173F, -0.0872665F, -0.2617994F));
		leftWing.addOrReplaceChild("left_hindwing", CubeListBuilder.create().texOffs(0, 30)
				.addBox(-3F, 0F, 2F, 2F, 2F, 10F),
				PartPose.offsetAndRotation(0F, 0F, 0F, -0.0872665F, -0.1570796F, -0.2094395F));
		PartDefinition leftLeg1 = luminalCicada.addOrReplaceChild("left_leg_1", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4F, -4F, -5F, 0F, -0.4886922F, 0F));
		leftLeg1.addOrReplaceChild("left_leg_1_femur", CubeListBuilder.create().texOffs(25, 30)
				.addBox(-4F, 0F, -1F, 4F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.1396263F));
		leftLeg1.addOrReplaceChild("left_leg_1_claw", CubeListBuilder.create().texOffs(38, 30)
				.addBox(-4F, 0F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.1396263F));
		PartDefinition leftLeg2 = luminalCicada.addOrReplaceChild("left_leg_2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4F, -4F, -1F, 0F, 0.0872665F, 0F));
		leftLeg2.addOrReplaceChild("left_leg_2_femur", CubeListBuilder.create().texOffs(47, 30)
				.addBox(-4F, 0F, -1F, 4F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.1396263F));
		leftLeg2.addOrReplaceChild("left_leg_2_claw", CubeListBuilder.create().texOffs(60, 30)
				.addBox(-4F, 0F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.1396263F));
		PartDefinition leftLeg3 = luminalCicada.addOrReplaceChild("left_leg_3", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4F, -4F, 3F, 0F, 0.5235988F, 0F));
		leftLeg3.addOrReplaceChild("left_leg_3_femur", CubeListBuilder.create().texOffs(69, 30)
				.addBox(-4F, 0F, -1F, 4F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.1396263F));
		leftLeg3.addOrReplaceChild("left_leg_3_claw", CubeListBuilder.create().texOffs(82, 30)
				.addBox(-4F, 0F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.1396263F));
		PartDefinition rightWing = luminalCicada.addOrReplaceChild("right_wing", CubeListBuilder.create(),
				PartPose.offset(3F, -8F, -4F));
		rightWing.addOrReplaceChild("right_forewing", CubeListBuilder.create().texOffs(0, 43)
				.addBox(-3F, -2F, 0F, 7F, 2F, 12F),
				PartPose.offsetAndRotation(0F, 0F, 0F, -0.122173F, 0.0872665F, 0.2617994F));
		rightWing.addOrReplaceChild("right_forewing_tip", CubeListBuilder.create().texOffs(39, 43)
				.addBox(-3F, -2F, 12F, 5F, 2F, 4F),
				PartPose.offsetAndRotation(0F, 0F, 0F, -0.122173F, 0.0872665F, 0.2617994F));
		rightWing.addOrReplaceChild("right_hindwing", CubeListBuilder.create().texOffs(58, 43)
				.addBox(1F, 0F, 2F, 2F, 2F, 10F),
				PartPose.offsetAndRotation(0F, 0F, 0F, -0.0872665F, 0.1570796F, 0.2094395F));
		PartDefinition rightLeg1 = luminalCicada.addOrReplaceChild("right_leg_1", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4F, -4F, -5F, 0F, 0.4886922F, 0F));
		rightLeg1.addOrReplaceChild("right_leg_1_femur", CubeListBuilder.create().texOffs(83, 43)
				.addBox(0F, 0F, -1F, 4F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.1396263F));
		rightLeg1.addOrReplaceChild("right_leg_1_claw", CubeListBuilder.create().texOffs(96, 43)
				.addBox(2F, 0F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.1396263F));
		PartDefinition rightLeg2 = luminalCicada.addOrReplaceChild("right_leg_2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4F, -4F, -1F, 0F, -0.0872665F, 0F));
		rightLeg2.addOrReplaceChild("right_leg_2_femur", CubeListBuilder.create().texOffs(105, 43)
				.addBox(0F, 0F, -1F, 4F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.1396263F));
		rightLeg2.addOrReplaceChild("right_leg_2_claw", CubeListBuilder.create().texOffs(118, 43)
				.addBox(2F, 0F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.1396263F));
		PartDefinition rightLeg3 = luminalCicada.addOrReplaceChild("right_leg_3", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4F, -4F, 3F, 0F, -0.5235988F, 0F));
		rightLeg3.addOrReplaceChild("right_leg_3_femur", CubeListBuilder.create().texOffs(0, 58)
				.addBox(0F, 0F, -1F, 4F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.1396263F));
		rightLeg3.addOrReplaceChild("right_leg_3_claw", CubeListBuilder.create().texOffs(13, 58)
				.addBox(2F, 0F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.1396263F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(LuminalCicadaEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.getAllParts().forEach(ModelPart::resetPose);
		boolean clinging = entity.getClingFace() != null;
		float time = ageInTicks + entity.getId() * 0.73F;
		float flash = entity.getFlashStrength(ageInTicks - entity.tickCount);
		float wingBeat = Mth.sin(time * 2.65F);
		float wingSpread = clinging ? 0.24F * flash : 0.98F;
		float wingLift = clinging ? 0.09F * flash : 0.26F + 0.52F * wingBeat;
		this.leftWing.yRot -= wingSpread;
		this.rightWing.yRot += wingSpread;
		this.leftWing.zRot += wingLift;
		this.rightWing.zRot -= wingLift;

		for (int i = 0; i < this.leftLegs.length; i++) {
			float tuck = clinging ? 0.0F : 0.48F + 0.07F * Mth.sin(time * 0.9F + i);
			this.leftLegs[i].zRot += tuck;
			this.rightLegs[i].zRot -= tuck;
		}
		this.leftFeeler.yRot += 0.09F * Mth.sin(time * 0.32F);
		this.rightFeeler.yRot -= 0.09F * Mth.sin(time * 0.32F + 0.8F);
		this.abdomen.xRot += LuminalCicadaRules.abdomenPitch(clinging, time) + 0.20F * flash;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		this.root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
