package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.BloodlickerEntity;
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

/**
 * A low, spider-like humanoid inspired by the Bloodlicker's characteristic
 * silhouette: emaciated limbs surrounding a pendulous, blood-swollen abdomen.
 */
public final class BloodlickerModel extends HierarchicalModel<BloodlickerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("bloodlicker"), "main");

	private final ModelPart root;
	private final ModelPart abdomen;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart hair;
	private final ModelPart tongue;
	private final ModelPart tongue_tip;
	private final ModelPart left_arm;
	private final ModelPart left_forearm;
	private final ModelPart leftforefoot;
	private final ModelPart right_arm;
	private final ModelPart right_forearm;
	private final ModelPart rightforefoot;
	private final ModelPart left_leg;
	private final ModelPart left_shin;
	private final ModelPart leftbackfoot;
	private final ModelPart right_leg;
	private final ModelPart right_shin;
	private final ModelPart rightbackfoot;

	public BloodlickerModel(ModelPart bakedRoot) {
		this.root = bakedRoot.getChild("root");
		this.abdomen = this.root.getChild("abdomen");
		this.body = this.root.getChild("body");
		this.head = this.body.getChild("head");
		this.hair = this.head.getChild("hair");
		this.tongue = this.head.getChild("tongue");
		this.tongue_tip = this.tongue.getChild("tongue_tip");
		this.left_arm = this.body.getChild("left_arm");
		this.left_forearm = this.left_arm.getChild("left_forearm");
		this.leftforefoot = this.left_forearm.getChild("leftforefoot");
		this.right_arm = this.body.getChild("right_arm");
		this.right_forearm = this.right_arm.getChild("right_forearm");
		this.rightforefoot = this.right_forearm.getChild("rightforefoot");
		this.left_leg = this.root.getChild("left_leg");
		this.left_shin = this.left_leg.getChild("left_shin");
		this.leftbackfoot = this.left_shin.getChild("leftbackfoot");
		this.right_leg = this.root.getChild("right_leg");
		this.right_shin = this.right_leg.getChild("right_shin");
		this.rightbackfoot = this.right_shin.getChild("rightbackfoot");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.5F, 2.0F));

		PartDefinition abdomen = root.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -7.0F, -3.0F, 11.0F, 12.0F, 10.0F, new CubeDeformation(0.25F))
				.texOffs(44, 0).addBox(-4.5F, -8.0F, -2.0F, 9.0F, 2.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -5.0F, 3.5F, 0.18F, 0.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-4.0F, -7.5F, -3.0F, 8.0F, 9.0F, 5.0F, new CubeDeformation(-0.15F))
				.texOffs(28, 24).addBox(-5.5F, -7.0F, -2.5F, 11.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.0F, -1.0F, 0.72F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 42).addBox(-3.5F, -4.0F, -4.5F, 7.0F, 8.0F, 6.0F, new CubeDeformation(-0.2F))
				.texOffs(28, 32).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 2.0F, 4.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -6.0F, -4.0F, -0.38F, 0.0F, 0.0F));

		PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(64, 24).addBox(-4.0F, -4.5F, -5.0F, 8.0F, 10.0F, 1.0F, new CubeDeformation(0.05F))
				.texOffs(84, 24).addBox(3.5F, -4.5F, -3.8F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(98, 24).addBox(-4.5F, -4.5F, -3.8F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tongue = head.addOrReplaceChild("tongue", CubeListBuilder.create().texOffs(64, 44).addBox(-0.75F, -0.5F, -7.0F, 1.5F, 1.0F, 7.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(0.0F, 3.0F, -4.5F, 0.22F, 0.0F, 0.0F));

		PartDefinition tongue_tip = tongue.addOrReplaceChild("tongue_tip", CubeListBuilder.create().texOffs(64, 54).addBox(-0.6F, -0.45F, -7.0F, 1.2F, 0.9F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -6.8F, -0.18F, 0.0F, 0.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 60).addBox(-1.0F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(4.5F, -5.5F, -0.5F, -0.58F, 0.0F, -0.36F));

		PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(14, 60).addBox(-1.4F, -0.5F, -1.4F, 2.8F, 13.0F, 2.8F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(0.5F, 9.5F, 0.0F, -0.75F, 0.0F, 0.18F));

		PartDefinition leftforefoot = left_forearm.addOrReplaceChild("leftforefoot", CubeListBuilder.create().texOffs(24, 78).mirror().addBox(-2.0F, -1.2F, -3.5F, 4.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)).mirror(false), PartPose.offsetAndRotation(-0.05F, 12.5F, 0.0F, 0.6091F, -0.05F, 0.0715F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(26, 60).addBox(-2.0F, -1.5F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(-4.5F, -5.5F, -0.5F, -0.58F, 0.0F, 0.36F));

		PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(40, 60).addBox(-1.4F, -0.5F, -1.4F, 2.8F, 13.0F, 2.8F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(-0.5F, 9.5F, 0.0F, -0.75F, 0.0F, -0.18F));

		PartDefinition rightforefoot = right_forearm.addOrReplaceChild("rightforefoot", CubeListBuilder.create().texOffs(24, 78).addBox(-2.0F, -1.2F, -3.5F, 4.0F, 2.0F, 7.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.05F, 12.5F, 0.0F, 0.6091F, 0.05F, -0.0715F));

		PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(52, 64).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(3.7F, -6.0F, 3.0F, -1.02F, 0.18F, -0.26F));

		PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(66, 64).addBox(-1.3F, -0.5F, -1.3F, 2.6F, 12.0F, 2.6F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, 1.72F, 0.0F, 0.0F));

		PartDefinition leftbackfoot = left_shin.addOrReplaceChild("leftbackfoot", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2221F, 11.9948F, -0.6292F, 0.2182F, -0.0873F, 0.0F));

		PartDefinition left_shin_70_81_a940ec26_r1 = leftbackfoot.addOrReplaceChild("left_shin_70_81_a940ec26_r1", CubeListBuilder.create().texOffs(70, 81).mirror().addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(-0.15F)).mirror(false), PartPose.offsetAndRotation(0.5221F, -0.9948F, 0.6292F, -0.6859F, -0.0567F, 0.3922F));

		PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(78, 64).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 11.0F, 3.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-3.7F, -6.0F, 3.0F, -1.02F, -0.18F, 0.26F));

		PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(92, 64).addBox(-1.3F, -0.5F, -1.3F, 2.6F, 13.0F, 2.6F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, 1.72F, 0.0F, 0.0F));

		PartDefinition rightbackfoot = right_shin.addOrReplaceChild("rightbackfoot", CubeListBuilder.create(), PartPose.offsetAndRotation(0.2221F, 11.9948F, -0.6292F, 0.2182F, 0.0873F, 0.0F));

		PartDefinition right_shin_70_81_a940ec25_r1 = rightbackfoot.addOrReplaceChild("right_shin_70_81_a940ec25_r1", CubeListBuilder.create().texOffs(70, 81).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 6.0F, new CubeDeformation(-0.15F)), PartPose.offsetAndRotation(-0.5221F, -0.9948F, 0.6292F, -0.6859F, 0.0567F, -0.3922F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(BloodlickerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root.getAllParts().forEach(ModelPart::resetPose);
		float movement = Mth.clamp(limbSwingAmount * 2.2F, 0.0F, 1.0F);
		float stride = limbSwing * 0.9F;
		float idle = Mth.sin(ageInTicks * 0.09F);
		float fullness = entity.getBloodFullness();

		float abdomenScale = 0.82F + fullness * 0.46F;
		this.abdomen.xScale = abdomenScale;
		this.abdomen.yScale = 0.88F + fullness * 0.58F;
		this.abdomen.zScale = abdomenScale;
		this.abdomen.y += fullness * 1.5F + idle * 0.12F;

		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.35F;
		this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.2F;
		this.hair.zRot = idle * 0.025F;
		this.tongue.yRot = Mth.sin(ageInTicks * 0.13F) * 0.12F;
		this.tongue.xRot += Mth.cos(ageInTicks * 0.11F) * 0.05F;
		this.tongue_tip.yRot = Mth.sin(ageInTicks * 0.17F + 0.8F) * 0.2F;

		this.left_arm.xRot += Mth.sin(stride) * 0.38F * movement;
		this.right_arm.xRot -= Mth.sin(stride) * 0.38F * movement;
		this.left_forearm.xRot += Mth.sin(stride + 0.9F) * 0.22F * movement;
		this.right_forearm.xRot -= Mth.sin(stride + 0.9F) * 0.22F * movement;
		this.left_leg.xRot += Mth.sin(stride + Mth.PI) * 0.3F * movement;
		this.right_leg.xRot += Mth.sin(stride) * 0.3F * movement;
		this.left_shin.xRot += Mth.sin(stride) * 0.18F * movement;
		this.right_shin.xRot -= Mth.sin(stride) * 0.18F * movement;
		this.body.y += Mth.abs(Mth.sin(stride)) * 0.35F * movement;
	}
}
