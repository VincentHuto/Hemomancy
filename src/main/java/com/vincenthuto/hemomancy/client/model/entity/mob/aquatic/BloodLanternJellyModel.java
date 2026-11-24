package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BloodLanternJellyEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public class BloodLanternJellyModel extends EntityModel<BloodLanternJellyEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("blood_lantern_jelly"), "main");

	private static final String[] FILAMENT_NAMES = {
			"north_left", "north_right", "south_left", "south_right",
			"east_front", "east_back", "west_front", "west_back"
	};
	private static final String[] ARM_NAMES = {"left_arm", "right_arm", "rear_arm"};

	private final ModelPart body;
	private final ModelPart bell;
	private final ModelPart lantern;
	private final ModelPart wick;
	private final ModelPart suspension;
	private final ModelPart[] filaments = new ModelPart[FILAMENT_NAMES.length];
	private final ModelPart[] arms = new ModelPart[ARM_NAMES.length];

	public BloodLanternJellyModel(ModelPart root) {
		this.body = root.getChild("body");
		this.bell = body.getChild("bell");
		this.lantern = body.getChild("lantern");
		this.wick = lantern.getChild("wick");
		this.suspension = lantern.getChild("suspension");
		for (int i = 0; i < filaments.length; i++) {
			filaments[i] = bell.getChild(FILAMENT_NAMES[i]);
		}
		for (int i = 0; i < arms.length; i++) {
			arms[i] = lantern.getChild(ARM_NAMES[i]);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(),
				PartPose.offset(0.0F, 14.0F, 0.0F));
		PartDefinition bell = body.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(1, 1).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 1.0F, 6.0F)
				.texOffs(27, 1).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 2.0F, 8.0F)
				.texOffs(61, 1).addBox(-4.0F, -2.0F, -5.0F, 8.0F, 2.0F, 1.0F)
				.texOffs(81, 1).addBox(-4.0F, -2.0F, 4.0F, 8.0F, 2.0F, 1.0F)
				.texOffs(101, 1).addBox(4.0F, -2.0F, -4.0F, 1.0F, 2.0F, 8.0F)
				.texOffs(1, 13).addBox(-5.0F, -2.0F, -4.0F, 1.0F, 2.0F, 8.0F)
				.texOffs(21, 13).addBox(-4.0F, 0.0F, -5.0F, 8.0F, 1.0F, 1.0F)
				.texOffs(41, 13).addBox(-4.0F, 0.0F, 4.0F, 8.0F, 1.0F, 1.0F)
				.texOffs(61, 13).addBox(4.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F)
				.texOffs(81, 13).addBox(-5.0F, 0.0F, -4.0F, 1.0F, 1.0F, 8.0F),
				PartPose.ZERO);
		PartDefinition lantern = body.addOrReplaceChild("lantern", CubeListBuilder.create(),
				PartPose.offset(0.0F, -1.0F, 0.0F));
		lantern.addOrReplaceChild("suspension", CubeListBuilder.create()
				.texOffs(101, 13).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F),
				PartPose.ZERO);
		lantern.addOrReplaceChild("wick", CubeListBuilder.create()
				.texOffs(111, 13).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F)
				.texOffs(1, 25).addBox(-1.0F, 3.0F, -1.0F, 2.0F, 1.0F, 2.0F),
				PartPose.ZERO);
		lantern.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(11, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F),
				PartPose.offsetAndRotation(-1.25F, 3.0F, 0.0F, -0.122173F, 0.0F, 0.226893F));
		lantern.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(17, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F),
				PartPose.offsetAndRotation(1.0F, 3.0F, 0.0F, 0.191986F, 0.0F, -0.174533F));
		lantern.addOrReplaceChild("rear_arm", CubeListBuilder.create()
				.texOffs(23, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F),
				PartPose.offsetAndRotation(-0.25F, 3.0F, 1.5F, 0.331613F, 0.0F, 0.10472F));

		bell.addOrReplaceChild(FILAMENT_NAMES[0], CubeListBuilder.create()
				.texOffs(29, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F),
				PartPose.offsetAndRotation(-2.5F, 0.0F, -4.5F, -0.174533F, 0.0F, 0.122173F));
		bell.addOrReplaceChild(FILAMENT_NAMES[1], CubeListBuilder.create()
				.texOffs(35, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F),
				PartPose.offsetAndRotation(2.5F, 0.0F, -4.5F, -0.314159F, 0.0F, -0.122173F));
		bell.addOrReplaceChild(FILAMENT_NAMES[2], CubeListBuilder.create()
				.texOffs(41, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
				PartPose.offsetAndRotation(-2.5F, 0.0F, 4.5F, 0.226893F, 0.0F, 0.226893F));
		bell.addOrReplaceChild(FILAMENT_NAMES[3], CubeListBuilder.create()
				.texOffs(47, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F),
				PartPose.offsetAndRotation(2.5F, 0.0F, 4.5F, 0.15708F, 0.0F, -0.191986F));
		bell.addOrReplaceChild(FILAMENT_NAMES[4], CubeListBuilder.create()
				.texOffs(53, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F),
				PartPose.offsetAndRotation(4.5F, 0.0F, -1.5F, -0.05236F, 0.0F, -0.226893F));
		bell.addOrReplaceChild(FILAMENT_NAMES[5], CubeListBuilder.create()
				.texOffs(59, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F),
				PartPose.offsetAndRotation(4.5F, 0.0F, 2.5F, 0.279253F, 0.0F, -0.314159F));
		bell.addOrReplaceChild(FILAMENT_NAMES[6], CubeListBuilder.create()
				.texOffs(65, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F),
				PartPose.offsetAndRotation(-4.5F, 0.0F, -2.5F, -0.226893F, 0.0F, 0.296706F));
		bell.addOrReplaceChild(FILAMENT_NAMES[7], CubeListBuilder.create()
				.texOffs(71, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F),
				PartPose.offsetAndRotation(-4.5F, 0.0F, 1.5F, 0.10472F, 0.0F, 0.174533F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(BloodLanternJellyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float phase = ageInTicks * 0.16F;
		float pulse = Mth.sin(phase);
		body.y = body.getInitialPose().y - 0.35F * Mth.sin(phase - 0.6F);
		bell.xScale = bell.zScale = 1.0F - 0.09F * pulse;
		bell.yScale = 1.0F + 0.09F * pulse;

		lantern.xRot = Mth.sin(ageInTicks * 0.09F) * 0.06F;
		lantern.zRot = Mth.cos(ageInTicks * 0.11F) * 0.05F;
		wick.xScale = wick.zScale = 1.0F + 0.07F * Mth.sin(phase - 0.65F);

		for (int i = 0; i < arms.length; i++) {
			ModelPart arm = arms[i];
			PartPose rest = arm.getInitialPose();
			arm.xRot = rest.xRot + 0.10F * Mth.sin(phase - 0.9F + i * 1.7F);
			arm.zRot = rest.zRot + 0.07F * Mth.cos(phase - 0.9F + i * 1.7F);
		}
		for (int i = 0; i < filaments.length; i++) {
			ModelPart filament = filaments[i];
			PartPose rest = filament.getInitialPose();
			float trail = phase - 1.1F + i * 0.65F;
			filament.xRot = rest.xRot + 0.13F * Mth.sin(trail);
			filament.zRot = rest.zRot + 0.11F * Mth.cos(trail);
		}
	}

	@Override
	public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		pose.pushPose();
		body.translateAndRotate(pose);

		pose.pushPose();
		lantern.translateAndRotate(pose);
		wick.render(pose, buffer, LightTexture.FULL_BRIGHT, packedOverlay, packedColor);
		suspension.render(pose, buffer, packedLight, packedOverlay, packedColor);
		for (ModelPart arm : arms) {
			arm.render(pose, buffer, packedLight, packedOverlay, packedColor);
		}
		pose.popPose();
		bell.render(pose, buffer, packedLight, packedOverlay, packedColor);
		pose.popPose();
	}
}
