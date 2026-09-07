package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class CircusCarouselModel extends EntityModel<CircusCarouselEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("circus_carousel"), "main");
	private static final float HORSE_BASE_Y = 68.0F;
	private final ModelPart root;
	private final ModelPart turntable;
	private final ModelPart[] horses;
	private final ModelPart[] anchors;

	public CircusCarouselModel(ModelPart root) {
		this.root = root;
		turntable = root.getChild("turntable");
		horses = new ModelPart[] {
				turntable.getChild("horse_0"), turntable.getChild("horse_1"), turntable.getChild("horse_2")
		};
		anchors = new ModelPart[] {
				horses[0].getChild("anchor_0"), horses[1].getChild("anchor_1"), horses[2].getChild("anchor_2")
		};
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition frame = root.addOrReplaceChild("frame", CubeListBuilder.create()
				.texOffs(0, 84).addBox(-4.0F, 14.0F, -4.0F, 8.0F, 94.0F, 8.0F), PartPose.ZERO);
		PartDefinition canopy = frame.addOrReplaceChild("canopy", CubeListBuilder.create()
				.texOffs(48, 84).addBox(-16.0F, 4.0F, -16.0F, 32.0F, 8.0F, 32.0F)
				.texOffs(0, 208).addBox(-10.0F, 1.0F, -10.0F, 20.0F, 4.0F, 20.0F)
				.texOffs(232, 8).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.ZERO);
		addCanopyBeam(canopy, 0, 0.0F);
		addCanopyBeam(canopy, 1, 1.5708F);
		addCanopyBeam(canopy, 2, 0.7854F);
		addCanopyBeam(canopy, 3, -0.7854F);
		addValance(canopy, 0, 0.0F);
		addValance(canopy, 1, 1.5708F);
		addValance(canopy, 2, 0.7854F);
		addValance(canopy, 3, -0.7854F);
		canopy.addOrReplaceChild("canopy_lights", CubeListBuilder.create()
				.texOffs(232, 0).addBox(-51.5F, 12.0F, -1.5F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(48.5F, 12.0F, -1.5F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(-1.5F, 12.0F, -51.5F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(-1.5F, 12.0F, 48.5F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(-36.8F, 12.0F, -36.8F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(33.8F, 12.0F, -36.8F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(-36.8F, 12.0F, 33.8F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(33.8F, 12.0F, 33.8F, 3.0F, 3.0F, 3.0F), PartPose.ZERO);

		PartDefinition turntable = root.addOrReplaceChild("turntable", CubeListBuilder.create()
				.texOffs(48, 84).addBox(-16.0F, 100.0F, -16.0F, 32.0F, 12.0F, 32.0F)
				.texOffs(80, 208).addBox(-10.0F, 96.0F, -10.0F, 20.0F, 5.0F, 20.0F), PartPose.ZERO);
		PartDefinition platform = turntable.addOrReplaceChild("platform", CubeListBuilder.create(), PartPose.ZERO);
		addPlatformBeam(platform, 0, 0.0F);
		addPlatformBeam(platform, 1, 1.5708F);
		addPlatformBeam(platform, 2, 0.7854F);
		addPlatformBeam(platform, 3, -0.7854F);
		platform.addOrReplaceChild("platform_lights", CubeListBuilder.create()
				.texOffs(232, 0).addBox(-51.5F, 101.0F, -1.5F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(48.5F, 101.0F, -1.5F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(-1.5F, 101.0F, -51.5F, 3.0F, 3.0F, 3.0F)
				.texOffs(232, 0).addBox(-1.5F, 101.0F, 48.5F, 3.0F, 3.0F, 3.0F), PartPose.ZERO);

		addPole(turntable, 0, 37.6F, 0.0F);
		addPole(turntable, 1, -18.8F, 32.56F);
		addPole(turntable, 2, -18.8F, -32.56F);
		for (int index = 0; index < 3; index++) {
			addHorse(turntable, index, index == 0 ? 37.6F : -18.8F,
					index == 0 ? 0.0F : index == 1 ? 32.56F : -32.56F,
					index == 0 ? 0.0F : index == 1 ? 4.1888F : 2.0944F,
					index == 0 ? 0.24F : index == 1 ? -0.32F : 0.45F);
		}
		return LayerDefinition.create(mesh, 256, 256);
	}

	private static void addCanopyBeam(PartDefinition canopy, int index, float rotation) {
		canopy.addOrReplaceChild("canopy_beam_" + index, CubeListBuilder.create().texOffs(0, 0)
				.addBox(-56.0F, 8.0F, -7.0F, 112.0F, 6.0F, 14.0F), PartPose.rotation(0.0F, rotation, 0.0F));
	}

	private static void addValance(PartDefinition canopy, int index, float rotation) {
		canopy.addOrReplaceChild("valance_" + index, CubeListBuilder.create().texOffs(0, 22)
				.addBox(-56.0F, 13.0F, -1.5F, 112.0F, 6.0F, 3.0F), PartPose.rotation(0.0F, rotation, 0.0F));
	}

	private static void addPlatformBeam(PartDefinition platform, int index, float rotation) {
		platform.addOrReplaceChild("platform_beam_" + index, CubeListBuilder.create()
				.texOffs(0, 34).addBox(-56.0F, 104.0F, -7.0F, 112.0F, 8.0F, 14.0F)
				.texOffs(0, 58).addBox(-56.0F, 101.0F, -1.5F, 112.0F, 4.0F, 3.0F),
				PartPose.rotation(0.0F, rotation, 0.0F));
	}

	private static void addPole(PartDefinition turntable, int index, float x, float z) {
		turntable.addOrReplaceChild("pole_" + index, CubeListBuilder.create()
				.texOffs(34, 84).addBox(-1.5F, 14.0F, -1.5F, 3.0F, 94.0F, 3.0F)
				.texOffs(232, 8).addBox(-2.5F, 12.0F, -2.5F, 5.0F, 5.0F, 5.0F)
				.texOffs(232, 18).addBox(-2.0F, 65.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.offset(x, 0.0F, z));
	}

	private static void addHorse(PartDefinition turntable, int index, float x, float z, float rotation, float scarTilt) {
		PartDefinition horse = turntable.addOrReplaceChild("horse_" + index, CubeListBuilder.create()
				.texOffs(48, 128).addBox(-6.0F, -6.0F, -13.0F, 12.0F, 10.0F, 26.0F)
				.texOffs(160, 208).addBox(-5.0F, -9.0F, -11.0F, 10.0F, 5.0F, 14.0F)
				.texOffs(108, 168).addBox(-6.4F, -2.0F, -8.0F, 0.8F, 5.0F, 17.0F),
				PartPose.offsetAndRotation(x, HORSE_BASE_Y, z, 0.0F, rotation, 0.0F));
		horse.addOrReplaceChild("neck_" + index, CubeListBuilder.create()
				.texOffs(128, 128).addBox(-3.0F, -17.0F, -12.0F, 6.0F, 14.0F, 8.0F)
				.texOffs(168, 154).addBox(-0.5F, -18.0F, -4.5F, 1.0F, 14.0F, 10.0F),
				PartPose.rotation(-0.18F, 0.0F, 0.0F));
		horse.addOrReplaceChild("head_" + index, CubeListBuilder.create()
				.texOffs(160, 128).addBox(-5.0F, -21.0F, -21.0F, 10.0F, 10.0F, 12.0F)
				.texOffs(208, 128).addBox(-3.0F, -17.5F, -27.0F, 6.0F, 5.0F, 8.0F)
				.texOffs(240, 128).addBox(-4.6F, -25.0F, -16.5F, 2.0F, 5.0F, 2.0F)
				.texOffs(240, 128).addBox(2.6F, -25.0F, -16.5F, 2.0F, 5.0F, 2.0F), PartPose.ZERO);
		horse.addOrReplaceChild("legs_" + index, CubeListBuilder.create()
				.texOffs(128, 154).addBox(-5.0F, 3.0F, -10.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(128, 154).addBox(2.0F, 3.0F, -10.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(128, 154).addBox(-5.0F, 3.0F, 6.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(128, 154).addBox(2.0F, 3.0F, 6.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(144, 154).addBox(-5.5F, 21.0F, -12.0F, 4.0F, 3.0F, 6.0F)
				.texOffs(144, 154).addBox(1.5F, 21.0F, -12.0F, 4.0F, 3.0F, 6.0F)
				.texOffs(144, 154).addBox(-5.5F, 21.0F, 5.0F, 4.0F, 3.0F, 6.0F)
				.texOffs(144, 154).addBox(1.5F, 21.0F, 5.0F, 4.0F, 3.0F, 6.0F), PartPose.ZERO);
		horse.addOrReplaceChild("tail_" + index, CubeListBuilder.create()
				.texOffs(192, 154).addBox(-1.0F, -3.0F, 12.0F, 2.0F, 4.0F, 18.0F)
				.texOffs(232, 154).addBox(-1.5F, -1.0F, 27.0F, 3.0F, 8.0F, 3.0F), PartPose.rotation(0.18F, 0.0F, 0.0F));
		horse.addOrReplaceChild("saddle_" + index, CubeListBuilder.create()
				.texOffs(48, 168).addBox(-6.0F, -8.0F, -7.5F, 12.0F, 2.0F, 17.0F)
				.texOffs(108, 168).addBox(-6.4F, -7.0F, 7.5F, 0.8F, 8.0F, 18.0F)
				.texOffs(108, 168).addBox(5.6F, -7.0F, 7.5F, 0.8F, 8.0F, 18.0F), PartPose.ZERO);
		horse.addOrReplaceChild("anchor_" + index, CubeListBuilder.create()
				.texOffs(200, 184).addBox(-3.0F, -10.0F, -17.0F, 6.0F, 6.0F, 6.0F)
				.texOffs(224, 184).addBox(-0.75F, -4.0F, -14.0F, 1.5F, 18.0F, 1.5F), PartPose.ZERO);
	}

	public void prepare(CircusCarouselEntity entity, float partialTick) {
		float rotation = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
		turntable.yRot = -rotation * Mth.DEG_TO_RAD;
		for (int horse = 0; horse < horses.length; horse++) {
			horses[horse].y = HORSE_BASE_Y - (float) CircusCarouselRules.horsePose(rotation, horse).bob() * 16.0F;
			horses[horse].visible = !entity.isDestroyed();
			anchors[horse].visible = entity.isRiderSevered(horse) && !entity.isAnchorBroken(horse);
		}
	}

	@Override
	public void setupAnim(CircusCarouselEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight,
			int packedOverlay, int color) {
		root.render(poseStack, consumer, packedLight, packedOverlay, color);
	}
}
