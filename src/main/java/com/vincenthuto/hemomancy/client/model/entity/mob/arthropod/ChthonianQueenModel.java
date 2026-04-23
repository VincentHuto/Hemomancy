package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;
// Made with Blockbench 4.1.1

// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports

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
import net.minecraft.world.entity.Entity;

public class ChthonianQueenModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelchthonianqueen"), "main");
	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.5F, 19.5F, -4.0F));

		PartDefinition abdomen = whole.addOrReplaceChild("abdomen", CubeListBuilder.create(),
				PartPose.offset(-0.5F, 0.0F, 7.5F));

		PartDefinition bone3 = abdomen.addOrReplaceChild("bone3",
				CubeListBuilder.create().texOffs(0, 9)
						.addBox(-2.5F, -3.1F, 2.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-3.0F, -4.0F, 0.0F, 5.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5F, 0.75F, 0.0F));

		PartDefinition bone = bone3.addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(0, 9)
						.addBox(-2.5F, -3.1F, 2.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-3.0F, -4.0F, 0.0F, 5.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 5.0F));

		PartDefinition bone2 = bone.addOrReplaceChild("bone2",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-2.5F, -3.4F, -0.3333F, 5.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 17)
						.addBox(-1.5F, -1.6F, 3.6667F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 9)
						.addBox(-2.0F, -2.5F, 1.6667F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.5F, -0.6F, 5.3333F));

		PartDefinition thorax = whole.addOrReplaceChild("thorax",
				CubeListBuilder.create().texOffs(11, 14)
						.addBox(-2.5F, -2.25F, -1.5F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(14, 25)
						.addBox(-1.5F, -2.75F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(22, 6)
						.addBox(-1.5F, 1.25F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(13, 6)
						.addBox(-2.0F, -1.5F, 2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 24)
						.addBox(-1.5F, -2.0F, 2.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(22, 12)
						.addBox(-1.5F, 1.0F, 2.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(17, 29)
						.addBox(-1.0F, -0.25F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 29)
						.addBox(-1.0F, -0.25F, 1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(28, 3)
						.addBox(-1.0F, -0.25F, 4.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 2.0F));

		PartDefinition rLegs = thorax.addOrReplaceChild("rLegs", CubeListBuilder.create(),
				PartPose.offset(-2.0F, -1.0F, 2.0F));

		PartDefinition rFrontLeg = rLegs.addOrReplaceChild("rFrontLeg",
				CubeListBuilder.create().texOffs(28, 12).addBox(-1.5559F, 0.2244F, -0.5F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.25F, 0.5F, -2.5F, 0.0F, 0.0F, 0.2618F));

		PartDefinition rFrontLeg2 = rFrontLeg.addOrReplaceChild("rFrontLeg2",
				CubeListBuilder.create().texOffs(25, 17).addBox(-3.375F, 0.1495F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition rFrontLeg3 = rFrontLeg2.addOrReplaceChild(
				"rFrontLeg3", CubeListBuilder.create().texOffs(25, 9).addBox(-3.3326F, 0.1495F, -0.3268F, 3.0F, 1.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, -0.48F, 0.0F));

		PartDefinition rFrontLeg4 = rFrontLeg3.addOrReplaceChild("rFrontLeg4", CubeListBuilder.create().texOffs(0, 23)
				.addBox(-3.3471F, 1.1634F, -0.4564F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition rMidLeg = rLegs.addOrReplaceChild("rMidLeg",
				CubeListBuilder.create().texOffs(28, 6).addBox(-1.75F, 0.25F, -0.5F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.25F, 0.5F, -0.5F, 0.0F, 0.0F, 0.2618F));

		PartDefinition rMidLeg2 = rMidLeg.addOrReplaceChild("rMidLeg2",
				CubeListBuilder.create().texOffs(25, 0).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition rMidLeg3 = rMidLeg2.addOrReplaceChild("rMidLeg3",
				CubeListBuilder.create().texOffs(20, 25).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition rMidLeg4 = rMidLeg3.addOrReplaceChild("rMidLeg4", CubeListBuilder.create().texOffs(16, 22)
				.addBox(-3.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition rBackLeg = rLegs.addOrReplaceChild("rBackLeg",
				CubeListBuilder.create().texOffs(7, 26).addBox(-1.75F, 0.25F, -0.5F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.25F, 0.5F, 2.25F, 0.0F, 0.0F, 0.2618F));

		PartDefinition rBackLeg2 = rBackLeg.addOrReplaceChild("rBackLeg2",
				CubeListBuilder.create().texOffs(22, 15).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition rBackLeg3 = rBackLeg2
				.addOrReplaceChild("rBackLeg3",
						CubeListBuilder.create().texOffs(14, 12).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition rBackLeg4 = rBackLeg3.addOrReplaceChild("rBackLeg4", CubeListBuilder.create().texOffs(16, 21)
				.addBox(-3.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition lLegs = thorax.addOrReplaceChild("lLegs", CubeListBuilder.create(),
				PartPose.offset(1.5F, -1.0F, 2.0F));

		PartDefinition lFrontLeg = lLegs.addOrReplaceChild("lFrontLeg",
				CubeListBuilder.create().texOffs(21, 29).addBox(-0.1941F, 0.2244F, -0.55F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.5F, -2.45F, 0.0F, 0.0F, -0.2618F));

		PartDefinition lFrontLeg2 = lFrontLeg.addOrReplaceChild("lFrontLeg2",
				CubeListBuilder.create().texOffs(25, 21).addBox(0.375F, 0.1495F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.0F, -0.05F, 0.0F, 0.0F, 0.7854F));

		PartDefinition lFrontLeg3 = lFrontLeg2.addOrReplaceChild(
				"lFrontLeg3", CubeListBuilder.create().texOffs(25, 19).addBox(0.3326F, 0.1495F, -0.3268F, 3.0F, 1.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.48F, 0.0F));

		PartDefinition lFrontLeg4 = lFrontLeg3.addOrReplaceChild("lFrontLeg4", CubeListBuilder.create().texOffs(16, 23)
				.addBox(0.3471F, 1.1634F, -0.4564F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition lMidLeg = lLegs.addOrReplaceChild("lMidLeg",
				CubeListBuilder.create().texOffs(29, 14).addBox(0.0F, 0.25F, -0.55F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.5F, -0.45F, 0.0F, 0.0F, -0.2618F));

		PartDefinition lMidLeg2 = lMidLeg.addOrReplaceChild("lMidLeg2",
				CubeListBuilder.create().texOffs(21, 27).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.0F, -0.05F, 0.0F, 0.0F, 0.7854F));

		PartDefinition lMidLeg3 = lMidLeg2
				.addOrReplaceChild("lMidLeg3",
						CubeListBuilder.create().texOffs(0, 27).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition lMidLeg4 = lMidLeg3.addOrReplaceChild("lMidLeg4", CubeListBuilder.create().texOffs(24, 2).addBox(
				0.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition lBackLeg = lLegs.addOrReplaceChild("lBackLeg",
				CubeListBuilder.create().texOffs(6, 30).addBox(0.0F, 0.25F, -0.55F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.5F, 2.3F, 0.0F, 0.0F, -0.2618F));

		PartDefinition lBackLeg2 = lBackLeg.addOrReplaceChild("lBackLeg2",
				CubeListBuilder.create().texOffs(7, 28).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.0F, -0.05F, 0.0F, 0.0F, 0.7854F));

		PartDefinition lBackLeg3 = lBackLeg2
				.addOrReplaceChild("lBackLeg3",
						CubeListBuilder.create().texOffs(27, 24).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition lBackLeg4 = lBackLeg3.addOrReplaceChild("lBackLeg4", CubeListBuilder.create().texOffs(24, 11)
				.addBox(0.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition skull = whole.addOrReplaceChild("skull",
				CubeListBuilder.create().texOffs(16, 0)
						.addBox(-1.5F, -1.25F, -3.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(6, 21)
						.addBox(-0.5F, -2.0F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(6, 21)
						.addBox(-0.5F, -3.5F, -2.25F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(13, 0)
						.addBox(-0.5F, -1.0F, -4.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(13, 0)
						.addBox(-1.0F, -2.5F, -2.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-0.5F, 0.0F, 0.0F));

		PartDefinition lEye = skull.addOrReplaceChild("lEye",
				CubeListBuilder.create().texOffs(13, 28).addBox(-0.5F, 0.2153F, -1.2255F, 1.0F, 1.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -1.0F, -1.5F, 0.3054F, 0.0F, 0.0F));

		PartDefinition rEye = skull.addOrReplaceChild("rEye",
				CubeListBuilder.create().texOffs(27, 27).addBox(-0.5F, 0.2153F, -1.2255F, 1.0F, 1.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5F, -1.0F, -1.5F, 0.3054F, 0.0F, 0.0F));

		PartDefinition rMandible = skull.addOrReplaceChild("rMandible",
				CubeListBuilder.create().texOffs(20, 21)
						.addBox(-1.0F, -0.375F, -3.25F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(26, 30)
						.addBox(-0.5F, -0.625F, -3.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.5F, 20.625F, -5.75F));

		PartDefinition lMandible = skull.addOrReplaceChild("lMandible",
				CubeListBuilder.create().texOffs(12, 21)
						.addBox(0.0F, -0.45F, -2.75F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(6, 23)
						.addBox(-0.5F, -0.7F, -3.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.5F, 20.7F, -6.25F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	private final ModelPart whole;
	private final ModelPart skull;
	private final ModelPart lMandible;
	private final ModelPart rMandible;
	// Abdomen segments (for egg pulse animation)
	private final ModelPart abdomen;
	private final ModelPart abdomenSeg1; // bone3
	private final ModelPart abdomenSeg2; // bone
	private final ModelPart abdomenSeg3; // bone2
	// Right leg group
	private final ModelPart rFrontLeg;
	private final ModelPart rMidLeg;
	private final ModelPart rBackLeg;
	// Left leg group
	private final ModelPart lFrontLeg;
	private final ModelPart lMidLeg;
	private final ModelPart lBackLeg;

	public ChthonianQueenModel(ModelPart root) {
		this.whole = root.getChild("whole");
		this.skull = this.whole.getChild("skull");
		this.lMandible = this.skull.getChild("lMandible");
		this.rMandible = this.skull.getChild("rMandible");
		this.abdomen = this.whole.getChild("abdomen");
		this.abdomenSeg1 = this.abdomen.getChild("bone3");
		this.abdomenSeg2 = this.abdomenSeg1.getChild("bone");
		this.abdomenSeg3 = this.abdomenSeg2.getChild("bone2");
		ModelPart thorax = this.whole.getChild("thorax");
		ModelPart rLegs = thorax.getChild("rLegs");
		this.rFrontLeg = rLegs.getChild("rFrontLeg");
		this.rMidLeg = rLegs.getChild("rMidLeg");
		this.rBackLeg = rLegs.getChild("rBackLeg");
		ModelPart lLegs = thorax.getChild("lLegs");
		this.lFrontLeg = lLegs.getChild("lFrontLeg");
		this.lMidLeg = lLegs.getChild("lMidLeg");
		this.lBackLeg = lLegs.getChild("lBackLeg");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		whole.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		// Head tracking
		this.skull.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.skull.xRot = headPitch * ((float) Math.PI / 180F);

		// Mandible idle chewing motion
		float mandibleChew = (float) Math.sin(ageInTicks * 0.3F) * 0.15F;
		this.lMandible.yRot = mandibleChew;
		this.rMandible.yRot = -mandibleChew;

		// --- Abdomen egg pulse ---
		// Slow overall breathing pulse on the whole abdomen
		float breathPulse = 1.0F + (float) Math.sin(ageInTicks * 0.08F) * 0.06F;
		this.abdomen.xScale = breathPulse;
		this.abdomen.yScale = breathPulse;
		this.abdomen.zScale = breathPulse;

		// Peristaltic wave through the 3 segments — each offset in phase
		// so it looks like eggs are being pushed toward the rear
		float pulseSpeed = 0.15F;
		float pulseStrength = 0.12F;
		float seg1Pulse = 1.0F + (float) Math.sin(ageInTicks * pulseSpeed) * pulseStrength;
		float seg2Pulse = 1.0F + (float) Math.sin(ageInTicks * pulseSpeed - 1.2F) * pulseStrength;
		float seg3Pulse = 1.0F + (float) Math.sin(ageInTicks * pulseSpeed - 2.4F) * pulseStrength;

		this.abdomenSeg1.xScale = seg1Pulse;
		this.abdomenSeg1.yScale = seg1Pulse;
		this.abdomenSeg2.xScale = seg2Pulse;
		this.abdomenSeg2.yScale = seg2Pulse;
		this.abdomenSeg3.xScale = seg3Pulse;
		this.abdomenSeg3.yScale = seg3Pulse;

		// Subtle abdomen droop / sway for organic feel
		this.abdomen.xRot = (float) Math.sin(ageInTicks * 0.05F) * 0.03F;

		// Leg walking animation — spider-style zRot
		float legSpeed = 0.6662F;
		float legAmplitude = 0.6F;

		float swing1 = (float) Math.cos(limbSwing * legSpeed) * legAmplitude * limbSwingAmount;
		float swing2 = (float) Math.cos(limbSwing * legSpeed + (float) Math.PI) * legAmplitude * limbSwingAmount;

		// Right legs (base zRot = 0.2618): front & back in phase, mid anti-phase
		this.rFrontLeg.zRot = 0.2618F + swing1;
		this.rMidLeg.zRot   = 0.2618F + swing2;
		this.rBackLeg.zRot  = 0.2618F + swing1;

		// Left legs (base zRot = -0.2618): mirrored
		this.lFrontLeg.zRot = -0.2618F - swing2;
		this.lMidLeg.zRot   = -0.2618F - swing1;
		this.lBackLeg.zRot  = -0.2618F - swing2;

		// Slight yRot on front/back legs for forward/backward reach
		float ySwing = (float) Math.cos(limbSwing * legSpeed) * 0.3F * limbSwingAmount;
		this.rFrontLeg.yRot = -ySwing;
		this.lFrontLeg.yRot = ySwing;
		this.rBackLeg.yRot = ySwing;
		this.lBackLeg.yRot = -ySwing;
	}
}