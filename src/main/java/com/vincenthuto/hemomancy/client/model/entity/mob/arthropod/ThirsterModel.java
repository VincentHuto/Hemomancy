package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class ThirsterModel<T extends Mob> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("modelthirster"), "main");

	public static LayerDefinition createBodyLayer() {
		// Authored in docs/art/thirster/Thirster.bbmodel. The rig places its ground plane at model Y=24.
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition rig = root.addOrReplaceChild("rig", CubeListBuilder.create(), PartPose.offset(0.0F, 24F, 0.0F));

		PartDefinition torso = rig.addOrReplaceChild("torso", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4F, -12F, -2F, 8F, 12F, 4F)
				.texOffs(25, 0).addBox(-4F, 0.0F, -2F, 3F, 2F, 1F)
				.texOffs(34, 0).addBox(0.0F, 0.0F, 1F, 3F, 3F, 1F)
				.texOffs(43, 0).addBox(-3F, -13F, 2F, 6F, 2F, 1F)
				.texOffs(58, 0).addBox(-2F, -14F, -2F, 4F, 3F, 3F), PartPose.offsetAndRotation(0.0F, -12F, 0.0F, 0.5061455F, 0.0F, 0.0872665F));

		torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 17).addBox(-3F, -2F, -2F, 4F, 7F, 4F)
				.texOffs(17, 17).addBox(-3F, 5F, -2F, 4F, 5F, 4F)
				.texOffs(34, 17).addBox(-3F, 4F, -3F, 2F, 3F, 1F), PartPose.offsetAndRotation(-5F, -10F, 0.0F, -0.1396263F, -0.0872665F, 0.1745329F));

		torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(82, 17).addBox(-1F, -2F, -2F, 4F, 5F, 4F)
				.texOffs(99, 17).addBox(-1F, 3F, -2F, 4F, 7F, 4F)
				.texOffs(116, 17).addBox(-1F, 2F, -3F, 2F, 3F, 1F), PartPose.offsetAndRotation(5F, -10F, 0.0F, 0.122173F, 0.0872665F, -0.1745329F));

		PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-4F, -8F, -4F, 8F, 3F, 8F)
				.texOffs(33, 30).addBox(-4F, -5F, -4F, 1F, 4F, 8F)
				.texOffs(52, 30).addBox(3F, -5F, -4F, 1F, 4F, 8F)
				.texOffs(71, 30).addBox(-3F, -5F, 1F, 6F, 4F, 3F)
				.texOffs(90, 30).addBox(-1F, -5F, -5F, 2F, 1F, 1F)
				.texOffs(68, 43).addBox(-3F, -5F, -4F, 2F, 1F, 1F)
				.texOffs(75, 43).addBox(0.0F, -5F, -4F, 3F, 1F, 1F), PartPose.offsetAndRotation(0.0F, -12F, -2F, -0.3316126F, -0.1396263F, -0.0698132F));

		head.addOrReplaceChild("jaw", CubeListBuilder.create()
				.texOffs(0, 43).addBox(-4F, 1F, -7F, 8F, 1F, 8F)
				.texOffs(33, 43).addBox(-3F, 0.0F, -6F, 6F, 1F, 5F)
				.texOffs(56, 43).addBox(-3F, -1F, -6F, 2F, 1F, 1F)
				.texOffs(63, 43).addBox(1F, -1F, -6F, 1F, 1F, 1F), PartPose.offsetAndRotation(0.0F, -2F, 2F, 0.6108652F, 0.0F, 0.1570796F));

		PartDefinition mosquito = head.addOrReplaceChild("mosquito", CubeListBuilder.create()
				.texOffs(84, 43).addBox(-2F, -2F, 0.0F, 4F, 5F, 2F)
				.texOffs(97, 43).addBox(-3F, -3F, 2F, 6F, 5F, 3F)
				.texOffs(116, 43).addBox(-3F, -2F, 5F, 2F, 3F, 1F)
				.texOffs(0, 53).addBox(1F, -2F, 5F, 2F, 3F, 1F)
				.texOffs(12, 53).addBox(-1F, 0.0F, 5F, 2F, 2F, 2F), PartPose.offsetAndRotation(0.0F, -5F, 4F, 0.1396263F, 0.0F, 0.0F));
		mosquito.addOrReplaceChild("antenna_1", CubeListBuilder.create()
				.texOffs(123, 43).addBox(0.0F, -3F, 0.0F, 1F, 4F, 1F), PartPose.offsetAndRotation(-2F, -3F, 4F, -0.5061455F, 0.0F, -0.4014257F));
		mosquito.addOrReplaceChild("antenna_2", CubeListBuilder.create()
				.texOffs(7, 53).addBox(0.0F, -3F, 0.0F, 1F, 4F, 1F), PartPose.offsetAndRotation(2F, -3F, 4F, -0.5061455F, 0.0F, 0.4014257F));
		mosquito.addOrReplaceChild("proboscis", CubeListBuilder.create()
				.texOffs(21, 53).addBox(-1F, -1F, 0.0F, 1F, 1F, 6F), PartPose.offsetAndRotation(0.0F, 2F, 7F, -0.4014257F, 0.0F, 0.0F));
		mosquito.addOrReplaceChild("palp", CubeListBuilder.create()
				.texOffs(36, 53).addBox(0.0F, -1F, 0.0F, 1F, 1F, 3F), PartPose.offsetAndRotation(1F, 2F, 6F, -0.296706F, -0.2094395F, 0.0F));
		rig.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(73, 0).addBox(-2F, 0.0F, -2F, 4F, 8F, 4F)
				.texOffs(90, 0).addBox(-2F, 8F, -2F, 4F, 4F, 4F)
				.texOffs(107, 0).addBox(-2F, 8F, 1F, 2F, 1F, 1F), PartPose.offsetAndRotation(-2F, -12F, 0.0F, 0.1396263F, -0.0523599F, 0.0698132F));

		rig.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(41, 17).addBox(-2F, 0.0F, -2F, 4F, 8F, 4F)
				.texOffs(58, 17).addBox(-2F, 8F, -2F, 4F, 4F, 4F)
				.texOffs(75, 17).addBox(0.0F, 8F, 1F, 2F, 1F, 1F), PartPose.offsetAndRotation(2F, -12F, 0.0F, -0.1919862F, 0.0523599F, -0.0698132F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	private final ModelPart rig;
	private final ModelPart torso;
	private final ModelPart head;
	private final ModelPart jaw;
	private final ModelPart mosquito;
	private final ModelPart firstAntenna;
	private final ModelPart secondAntenna;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart rightLeg;
	private final ModelPart leftLeg;

	public ThirsterModel(ModelPart root) {
		rig = root.getChild("rig");
		torso = rig.getChild("torso");
		head = torso.getChild("head");
		jaw = head.getChild("jaw");
		mosquito = head.getChild("mosquito");
		firstAntenna = mosquito.getChild("antenna_1");
		secondAntenna = mosquito.getChild("antenna_2");
		rightArm = torso.getChild("right_arm");
		leftArm = torso.getChild("left_arm");
		rightLeg = rig.getChild("right_leg");
		leftLeg = rig.getChild("left_leg");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		rig.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		rig.getAllParts().forEach(ModelPart::resetPose);

		float stride = Mth.clamp(limbSwingAmount * 1.5F, 0.0F, 1.0F);
		float phase = limbSwing * 0.6662F;
		float step = Mth.cos(phase);
		float breath = Mth.sin(ageInTicks * 0.16F);

		rightLeg.xRot += step * 0.80F * stride;
		leftLeg.xRot -= step * 0.63F * stride;
		rightArm.xRot -= step * 0.43F * stride;
		leftArm.xRot += step * 0.31F * stride;
		rightArm.zRot += breath * 0.028F;
		leftArm.zRot -= breath * 0.022F;
		torso.xRot += breath * 0.021F + 0.06F * stride;
		torso.zRot += step * 0.065F * stride;
		head.xRot += Mth.clamp(headPitch, -45.0F, 45.0F) * Mth.DEG_TO_RAD - breath * 0.03F;
		head.yRot += Mth.clamp(netHeadYaw, -65.0F, 65.0F) * Mth.DEG_TO_RAD;
		head.zRot -= step * 0.035F * stride;

		float chatter = Mth.sin(ageInTicks * 1.45F) * Mth.sin(ageInTicks * 0.31F);
		jaw.xRot += entity.isAggressive() ? chatter * 0.10F : breath * 0.025F;
		jaw.zRot += step * 0.028F * stride;
		mosquito.xRot += breath * 0.04F - step * 0.025F * stride;
		firstAntenna.xRot += Mth.sin(ageInTicks * 0.37F) * 0.12F;
		secondAntenna.xRot += Mth.sin(ageInTicks * 0.37F + 1.7F) * 0.15F;

		float reach = Mth.sin(Mth.PI * attackTime);
		rightArm.xRot = Mth.lerp(reach, rightArm.xRot, -1.65F);
		leftArm.xRot = Mth.lerp(reach, leftArm.xRot, -1.52F);
		rightArm.yRot -= reach * 0.19F;
		leftArm.yRot += reach * 0.23F;
		torso.xRot += reach * 0.24F;
		head.xRot -= reach * 0.26F;
		jaw.xRot += reach * 0.17F;
		mosquito.xRot -= reach * 0.10F;
	}
}
