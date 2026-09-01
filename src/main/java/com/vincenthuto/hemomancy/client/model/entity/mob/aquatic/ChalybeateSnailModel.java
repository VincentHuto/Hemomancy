package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.ChalybeateSnailEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ChalybeateSnailModel extends HierarchicalModel<ChalybeateSnailEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("chalybeate_snail"),
			"main");

	private final ModelPart root;
	private final ModelPart foot;
	private final ModelPart body;
	private final ModelPart shell;
	private final ModelPart sclerites;
	private final ModelPart right_plates;
	private final ModelPart right_plate_0;
	private final ModelPart right_plate_1;
	private final ModelPart right_plate_2;
	private final ModelPart right_plate_3;
	private final ModelPart right_plate_4;
	private final ModelPart right_plate_5;
	private final ModelPart right_plate_6;
	private final ModelPart left_plates;
	private final ModelPart left_plate_0;
	private final ModelPart left_plate_1;
	private final ModelPart left_plate_2;
	private final ModelPart left_plate_3;
	private final ModelPart left_plate_4;
	private final ModelPart left_plate_5;
	private final ModelPart left_plate_6;
	private final ModelPart front_plates;
	private final ModelPart middle_plate;
	private final ModelPart front_left_plates;
	private final ModelPart front_plate_0;
	private final ModelPart front_plate_1;
	private final ModelPart front_right_plates;
	private final ModelPart front_plate_2;
	private final ModelPart front_plate_3;
	private final ModelPart back_plates;
	private final ModelPart middle_plate2;
	private final ModelPart back_left_plates;
	private final ModelPart back_plate_0;
	private final ModelPart back_plate_1;
	private final ModelPart back_right_plates;
	private final ModelPart back_plate_2;
	private final ModelPart back_plate_3;
	private final ModelPart back_plate_4;
	private final ModelPart left_tentacle;
	private final ModelPart right_tentacle;

	public ChalybeateSnailModel(ModelPart root) {
		this.root = root.getChild("root");
		this.foot = this.root.getChild("foot");
		this.body = this.root.getChild("body");
		this.shell = this.root.getChild("shell");
		this.sclerites = this.root.getChild("sclerites");
		this.right_plates = this.sclerites.getChild("right_plates");
		this.right_plate_0 = this.right_plates.getChild("right_plate_0");
		this.right_plate_1 = this.right_plates.getChild("right_plate_1");
		this.right_plate_2 = this.right_plates.getChild("right_plate_2");
		this.right_plate_3 = this.right_plates.getChild("right_plate_3");
		this.right_plate_4 = this.right_plates.getChild("right_plate_4");
		this.right_plate_5 = this.right_plates.getChild("right_plate_5");
		this.right_plate_6 = this.right_plates.getChild("right_plate_6");
		this.left_plates = this.sclerites.getChild("left_plates");
		this.left_plate_0 = this.left_plates.getChild("left_plate_0");
		this.left_plate_1 = this.left_plates.getChild("left_plate_1");
		this.left_plate_2 = this.left_plates.getChild("left_plate_2");
		this.left_plate_3 = this.left_plates.getChild("left_plate_3");
		this.left_plate_4 = this.left_plates.getChild("left_plate_4");
		this.left_plate_5 = this.left_plates.getChild("left_plate_5");
		this.left_plate_6 = this.left_plates.getChild("left_plate_6");
		this.front_plates = this.sclerites.getChild("front_plates");
		this.middle_plate = this.front_plates.getChild("middle_plate");
		this.front_left_plates = this.front_plates.getChild("front_left_plates");
		this.front_plate_0 = this.front_left_plates.getChild("front_plate_0");
		this.front_plate_1 = this.front_left_plates.getChild("front_plate_1");
		this.front_right_plates = this.front_plates.getChild("front_right_plates");
		this.front_plate_2 = this.front_right_plates.getChild("front_plate_2");
		this.front_plate_3 = this.front_right_plates.getChild("front_plate_3");
		this.back_plates = this.sclerites.getChild("back_plates");
		this.middle_plate2 = this.back_plates.getChild("middle_plate2");
		this.back_left_plates = this.back_plates.getChild("back_left_plates");
		this.back_plate_0 = this.back_left_plates.getChild("back_plate_0");
		this.back_plate_1 = this.back_left_plates.getChild("back_plate_1");
		this.back_right_plates = this.back_plates.getChild("back_right_plates");
		this.back_plate_2 = this.back_right_plates.getChild("back_plate_2");
		this.back_plate_3 = this.back_right_plates.getChild("back_plate_3");
		this.back_plate_4 = this.back_right_plates.getChild("back_plate_4");
		this.left_tentacle = this.root.getChild("left_tentacle");
		this.right_tentacle = this.root.getChild("right_tentacle");
	}


	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition foot = root.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.0F, -8.5F, 10.0F, 2.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(41, 35).addBox(-4.0F, -5.0F, -7.0F, 8.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.75F, -0.5F));

		PartDefinition shell = root.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 35).addBox(-5.5F, -10.0F, -4.0F, 11.0F, 9.0F, 10.0F, new CubeDeformation(0.2F))
				.texOffs(45, 19).addBox(-5.0F, -9.0F, -8.0F, 10.0F, 5.0F, 8.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, -2.0F, 1.5F));

		PartDefinition sclerites = root.addOrReplaceChild("sclerites", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -0.5F));

		PartDefinition right_plates = sclerites.addOrReplaceChild("right_plates", CubeListBuilder.create(), PartPose.offset(-5.0F, 0.0F, -6.0F));

		PartDefinition right_plate_0 = right_plates.addOrReplaceChild("right_plate_0", CubeListBuilder.create().texOffs(53, 8).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.28F, 0.3F));

		PartDefinition right_plate_1 = right_plates.addOrReplaceChild("right_plate_1", CubeListBuilder.create().texOffs(0, 55).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, 0.28F, 0.3F));

		PartDefinition right_plate_2 = right_plates.addOrReplaceChild("right_plate_2", CubeListBuilder.create().texOffs(9, 55).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 0.28F, 0.3F));

		PartDefinition right_plate_3 = right_plates.addOrReplaceChild("right_plate_3", CubeListBuilder.create().texOffs(18, 55).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, 0.0F, 0.28F, 0.3F));

		PartDefinition right_plate_4 = right_plates.addOrReplaceChild("right_plate_4", CubeListBuilder.create().texOffs(27, 55).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.0F, 0.28F, 0.3F));

		PartDefinition right_plate_5 = right_plates.addOrReplaceChild("right_plate_5", CubeListBuilder.create().texOffs(56, 52).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 10.0F, 0.0F, 0.28F, 0.3F));

		PartDefinition right_plate_6 = right_plates.addOrReplaceChild("right_plate_6", CubeListBuilder.create().texOffs(56, 59).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 12.0F, 0.0F, 0.2364F, 0.3F));

		PartDefinition left_plates = sclerites.addOrReplaceChild("left_plates", CubeListBuilder.create(), PartPose.offset(5.0F, 0.0F, -6.0F));

		PartDefinition left_plate_0 = left_plates.addOrReplaceChild("left_plate_0", CubeListBuilder.create().texOffs(36, 60).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.28F, -0.3F));

		PartDefinition left_plate_1 = left_plates.addOrReplaceChild("left_plate_1", CubeListBuilder.create().texOffs(45, 60).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.0F, 0.0F, -0.28F, -0.3F));

		PartDefinition left_plate_2 = left_plates.addOrReplaceChild("left_plate_2", CubeListBuilder.create().texOffs(0, 62).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, -0.28F, -0.3F));

		PartDefinition left_plate_3 = left_plates.addOrReplaceChild("left_plate_3", CubeListBuilder.create().texOffs(62, 8).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 6.0F, 0.0F, -0.28F, -0.3F));

		PartDefinition left_plate_4 = left_plates.addOrReplaceChild("left_plate_4", CubeListBuilder.create().texOffs(9, 62).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 8.0F, 0.0F, -0.28F, -0.3F));

		PartDefinition left_plate_5 = left_plates.addOrReplaceChild("left_plate_5", CubeListBuilder.create().texOffs(18, 62).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 10.0F, 0.0F, -0.28F, -0.3F));

		PartDefinition left_plate_6 = left_plates.addOrReplaceChild("left_plate_6", CubeListBuilder.create().texOffs(27, 62).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 12.0F, 0.0F, -0.1927F, -0.3F));

		PartDefinition front_plates = sclerites.addOrReplaceChild("front_plates", CubeListBuilder.create(), PartPose.offset(-2.0F, 0.0F, -7.1F));

		PartDefinition middle_plate = front_plates.addOrReplaceChild("middle_plate", CubeListBuilder.create().texOffs(36, 55).addBox(-1.5F, -0.25F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));

		PartDefinition front_left_plates = front_plates.addOrReplaceChild("front_left_plates", CubeListBuilder.create(), PartPose.offset(1.0F, 0.0F, 0.0F));

		PartDefinition front_plate_0 = front_left_plates.addOrReplaceChild("front_plate_0", CubeListBuilder.create().texOffs(65, 52).addBox(-2.5F, -0.25F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));

		PartDefinition front_plate_1 = front_left_plates.addOrReplaceChild("front_plate_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.4815F, 0.0774F, -0.0404F));

		PartDefinition front_plate_2_0_70_577dbb65_r1 = front_plate_1.addOrReplaceChild("front_plate_2_0_70_577dbb65_r1", CubeListBuilder.create().texOffs(65, 57).addBox(-2.5F, -1.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.657F, 0.7109F, 0.9393F, 0.0F, 0.0F, 0.3927F));

		PartDefinition front_right_plates = front_plates.addOrReplaceChild("front_right_plates", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition front_plate_2 = front_right_plates.addOrReplaceChild("front_plate_2", CubeListBuilder.create().texOffs(65, 62).addBox(-0.5F, -0.25F, -0.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F));

		PartDefinition front_plate_3 = front_right_plates.addOrReplaceChild("front_plate_3", CubeListBuilder.create(), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.4815F, -0.0774F, 0.0404F));

		PartDefinition front_plate_2_0_70_577dbb66_r1 = front_plate_3.addOrReplaceChild("front_plate_2_0_70_577dbb66_r1", CubeListBuilder.create().texOffs(54, 66).addBox(-0.5F, -1.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.657F, 0.7109F, 0.9393F, 0.0F, 0.0F, -0.3927F));

		PartDefinition back_plates = sclerites.addOrReplaceChild("back_plates", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, -1.5F, 8.5F, -0.0873F, 0.0F, 0.0F));

		PartDefinition middle_plate2 = back_plates.addOrReplaceChild("middle_plate2", CubeListBuilder.create().texOffs(0, 69).addBox(-1.5F, 0.4564F, -0.4692F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.48F, 0.0F, 0.0F));

		PartDefinition back_left_plates = back_plates.addOrReplaceChild("back_left_plates", CubeListBuilder.create(), PartPose.offset(1.0F, 0.0F, 0.0F));

		PartDefinition back_plate_0 = back_left_plates.addOrReplaceChild("back_plate_0", CubeListBuilder.create().texOffs(36, 67).addBox(-2.5F, 0.4564F, -0.4692F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.48F, 0.0F, 0.0F));

		PartDefinition back_plate_1 = back_left_plates.addOrReplaceChild("back_plate_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.4815F, -0.0774F, -0.0404F));

		PartDefinition back_right_plates = back_plates.addOrReplaceChild("back_right_plates", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition back_plate_2 = back_right_plates.addOrReplaceChild("back_plate_2", CubeListBuilder.create().texOffs(45, 67).addBox(-0.5F, 0.4564F, -0.4692F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.48F, 0.0F, 0.0F));

		PartDefinition back_plate_3 = back_right_plates.addOrReplaceChild("back_plate_3", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.4815F, 0.0774F, 0.0404F));

		PartDefinition back_plate_2_0_70_577dbb67_r1 = back_plate_3.addOrReplaceChild("back_plate_2_0_70_577dbb67_r1", CubeListBuilder.create().texOffs(63, 67).addBox(-0.7728F, -0.3484F, 0.0307F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.407F, 0.7109F, -0.9393F, 0.0F, 0.0F, -0.3927F));

		PartDefinition back_plate_4 = back_right_plates.addOrReplaceChild("back_plate_4", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.4815F, -0.0774F, -0.0404F));

		PartDefinition back_plate_2_0_70_577dbb68_r1 = back_plate_4.addOrReplaceChild("back_plate_2_0_70_577dbb68_r1", CubeListBuilder.create().texOffs(68, 0).addBox(-1.2272F, -0.3484F, 0.0307F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.407F, 0.7109F, -0.9393F, 0.0F, 0.0F, 0.3927F));

		PartDefinition left_tentacle = root.addOrReplaceChild("left_tentacle", CubeListBuilder.create().texOffs(41, 52).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.25F, -4.0F, -7.0F, 0.25F, 0.2F, 0.0F));

		PartDefinition right_tentacle = root.addOrReplaceChild("right_tentacle", CubeListBuilder.create().texOffs(53, 0).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.25F, -4.0F, -7.0F, 0.25F, -0.2F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(ChalybeateSnailEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		boolean retracted = entity.isRetracted();
		float crawl = Mth.sin(ageInTicks * 0.18F) * 0.08F;

		this.left_tentacle.visible = !retracted;
		this.right_tentacle.visible = !retracted;
		this.foot.visible = !retracted;
		this.body.visible = !retracted;
		this.shell.xScale = 1.0F;
		this.shell.yScale = 1.0F;
		this.shell.zScale = 1.0F;

		if (retracted) {
			this.body.y+= 1.3F;
			this.sclerites.y += 1.0F;
			this.shell.y += 4.5F;
			this.shell.xScale = 1.05F;
			this.shell.yScale = 1.04F;
			this.shell.zScale = 1.08F;
		} else {
			this.root.y += crawl;
			this.left_tentacle.yRot = -0.25F + Mth.sin(ageInTicks * 0.2F) * 0.12F;
			this.right_tentacle.yRot = 0.25F - Mth.sin(ageInTicks * 0.2F) * 0.12F;
			this.sclerites.zRot = Mth.sin(ageInTicks * 0.12F) * 0.025F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
							   int packedColor) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
