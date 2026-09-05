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
				.texOffs(0, 0).addBox(-5.0F, 4.0F, -5.0F, 10.0F, 108.0F, 10.0F)
				.texOffs(40, 0).addBox(-40.0F, 0.0F, -40.0F, 80.0F, 8.0F, 80.0F)
				.texOffs(0, 120).addBox(-56.0F, 8.0F, -8.0F, 112.0F, 5.0F, 16.0F)
				.texOffs(0, 142).addBox(-8.0F, 8.0F, -56.0F, 16.0F, 5.0F, 112.0F), PartPose.ZERO);
		frame.addOrReplaceChild("canopy_diagonal", CubeListBuilder.create()
				.texOffs(40, 0).addBox(-40.0F, 0.0F, -40.0F, 80.0F, 8.0F, 80.0F),
				PartPose.rotation(0.0F, 0.7854F, 0.0F));

		PartDefinition turntable = root.addOrReplaceChild("turntable", CubeListBuilder.create()
				.texOffs(0, 80).addBox(-40.0F, 104.0F, -40.0F, 80.0F, 8.0F, 80.0F)
				.texOffs(0, 168).addBox(-56.0F, 104.0F, -7.0F, 112.0F, 6.0F, 14.0F)
				.texOffs(0, 188).addBox(-7.0F, 104.0F, -56.0F, 14.0F, 6.0F, 112.0F), PartPose.ZERO);
		turntable.addOrReplaceChild("platform_diagonal", CubeListBuilder.create()
				.texOffs(0, 80).addBox(-40.0F, 104.0F, -40.0F, 80.0F, 8.0F, 80.0F),
				PartPose.rotation(0.0F, 0.7854F, 0.0F));
		turntable.addOrReplaceChild("pole_0", CubeListBuilder.create().texOffs(224, 0)
				.addBox(-1.5F, 12.0F, -1.5F, 3.0F, 96.0F, 3.0F), PartPose.offset(37.6F, 0.0F, 0.0F));
		turntable.addOrReplaceChild("pole_1", CubeListBuilder.create().texOffs(224, 0)
				.addBox(-1.5F, 12.0F, -1.5F, 3.0F, 96.0F, 3.0F), PartPose.offset(-18.8F, 0.0F, 32.56F));
		turntable.addOrReplaceChild("pole_2", CubeListBuilder.create().texOffs(224, 0)
				.addBox(-1.5F, 12.0F, -1.5F, 3.0F, 96.0F, 3.0F), PartPose.offset(-18.8F, 0.0F, -32.56F));

		PartDefinition horse0 = turntable.addOrReplaceChild("horse_0", CubeListBuilder.create()
				.texOffs(0, 200).addBox(-6.0F, -6.0F, -14.0F, 12.0F, 12.0F, 28.0F)
				.texOffs(82, 198).addBox(-5.0F, -20.0F, -20.0F, 10.0F, 16.0F, 12.0F)
				.texOffs(0, 184).addBox(-4.0F, 5.0F, -11.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(14, 184).addBox(1.0F, 5.0F, -11.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(28, 184).addBox(-4.0F, 5.0F, 7.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(42, 184).addBox(1.0F, 5.0F, 7.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(128, 198).addBox(-1.5F, -17.0F, -24.0F, 3.0F, 4.0F, 6.0F)
				.texOffs(144, 198).addBox(-1.0F, -3.0F, 13.0F, 2.0F, 4.0F, 18.0F),
				PartPose.offsetAndRotation(37.6F, HORSE_BASE_Y, 0.0F, 0.0F, 1.5708F, 0.0F));
		horse0.addOrReplaceChild("scar_0", CubeListBuilder.create().texOffs(184, 198)
				.addBox(-5.5F, -15.0F, -20.7F, 11.0F, 2.0F, 1.0F), PartPose.rotation(0.0F, 0.0F, 0.28F));
		addAnchor(horse0, 0);

		PartDefinition horse1 = turntable.addOrReplaceChild("horse_1", CubeListBuilder.create()
				.texOffs(0, 200).addBox(-6.0F, -6.0F, -14.0F, 12.0F, 12.0F, 28.0F)
				.texOffs(82, 198).addBox(-5.0F, -20.0F, -20.0F, 10.0F, 16.0F, 12.0F)
				.texOffs(0, 184).addBox(-4.0F, 5.0F, -11.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(14, 184).addBox(1.0F, 5.0F, -11.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(28, 184).addBox(-4.0F, 5.0F, 7.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(42, 184).addBox(1.0F, 5.0F, 7.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(128, 198).addBox(-1.5F, -17.0F, -24.0F, 3.0F, 4.0F, 6.0F)
				.texOffs(144, 198).addBox(-1.0F, -3.0F, 13.0F, 2.0F, 4.0F, 18.0F),
				PartPose.offsetAndRotation(-18.8F, HORSE_BASE_Y, 32.56F, 0.0F, 3.6652F, 0.0F));
		horse1.addOrReplaceChild("scar_1", CubeListBuilder.create().texOffs(184, 202)
				.addBox(-5.5F, -9.0F, -14.7F, 11.0F, 2.0F, 1.0F), PartPose.rotation(0.0F, 0.0F, -0.36F));
		addAnchor(horse1, 1);

		PartDefinition horse2 = turntable.addOrReplaceChild("horse_2", CubeListBuilder.create()
				.texOffs(0, 200).addBox(-6.0F, -6.0F, -14.0F, 12.0F, 12.0F, 28.0F)
				.texOffs(82, 198).addBox(-5.0F, -20.0F, -20.0F, 10.0F, 16.0F, 12.0F)
				.texOffs(0, 184).addBox(-4.0F, 5.0F, -11.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(14, 184).addBox(1.0F, 5.0F, -11.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(28, 184).addBox(-4.0F, 5.0F, 7.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(42, 184).addBox(1.0F, 5.0F, 7.0F, 3.0F, 20.0F, 4.0F)
				.texOffs(128, 198).addBox(-1.5F, -17.0F, -24.0F, 3.0F, 4.0F, 6.0F)
				.texOffs(144, 198).addBox(-1.0F, -3.0F, 13.0F, 2.0F, 4.0F, 18.0F),
				PartPose.offsetAndRotation(-18.8F, HORSE_BASE_Y, -32.56F, 0.0F, 5.7596F, 0.0F));
		horse2.addOrReplaceChild("scar_2", CubeListBuilder.create().texOffs(184, 206)
				.addBox(-5.5F, -18.0F, -17.7F, 11.0F, 2.0F, 1.0F), PartPose.rotation(0.0F, 0.0F, 0.5F));
		addAnchor(horse2, 2);
		return LayerDefinition.create(mesh, 256, 256);
	}

	private static void addAnchor(PartDefinition horse, int index) {
		horse.addOrReplaceChild("anchor_" + index, CubeListBuilder.create()
				.texOffs(238, 42).addBox(-3.0F, -10.0F, -17.0F, 6.0F, 6.0F, 6.0F)
				.texOffs(246, 0).addBox(-0.75F, -4.0F, -14.0F, 1.5F, 18.0F, 1.5F), PartPose.ZERO);
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
