package com.vincenthuto.hemomancy.model.item;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class BarbedShieldModel extends Model {
	public static final ModelLayerLocation barbed_shield = new ModelLayerLocation(
			Hemomancy.rloc("barbed_shield"), "main");

	public static LayerDefinition createLayers() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition shield3 = mesh.getRoot();
		PartDefinition bone = mesh.getRoot();
		PartDefinition bone9 = mesh.getRoot();
		PartDefinition bone10 = mesh.getRoot();
		PartDefinition bone2 = mesh.getRoot();
		PartDefinition bone3 = mesh.getRoot();
		PartDefinition bone4 = mesh.getRoot();
		PartDefinition bone5 = mesh.getRoot();
		PartDefinition bone6 = mesh.getRoot();
		PartDefinition bone7 = mesh.getRoot();
		PartDefinition bone11 = mesh.getRoot();
		PartDefinition bone12 = mesh.getRoot();
		PartDefinition bone8 = mesh.getRoot();
		shield3.addOrReplaceChild("0",
				CubeListBuilder.create().texOffs(14, 23).addBox(-1.0F, -15.5F, -3.0F, 2.0F, 6.0F, 6.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("1",
				CubeListBuilder.create().texOffs(0, 13).addBox(-4.5F, -17.0F, -4.75F, 9.0F, 9.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("2",
				CubeListBuilder.create().texOffs(20, 13).addBox(-4.0F, -16.5F, -5.0F, 8.0F, 8.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("3",
				CubeListBuilder.create().texOffs(37, 31).addBox(-2.0F, -14.5F, -6.0F, 4.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("4",
				CubeListBuilder.create().texOffs(37, 31).addBox(-1.0F, -13.5F, -6.5F, 2.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("5",
				CubeListBuilder.create().texOffs(37, 31).addBox(0.0F, -13.5F, -8.5F, 0.0F, 2.0F, 2.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("6",
				CubeListBuilder.create().texOffs(37, 31).addBox(0.0F, -13.0F, -9.5F, 0.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("7",
				CubeListBuilder.create().texOffs(37, 31).addBox(-1.0F, -12.5F, -8.5F, 2.0F, 0.0F, 2.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("8",
				CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -18.5F, -4.0F, 12.0F, 12.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("9",
				CubeListBuilder.create().texOffs(0, 24).addBox(-7.0F, -17.5F, -3.75F, 1.0F, 10.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("10",
				CubeListBuilder.create().texOffs(0, 24).addBox(6.0F, -17.5F, -3.75F, 1.0F, 10.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("11",
				CubeListBuilder.create().texOffs(24, 23).addBox(-5.0F, -6.5F, -3.75F, 10.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		shield3.addOrReplaceChild("12",
				CubeListBuilder.create().texOffs(24, 23).addBox(-5.0F, -19.5F, -3.75F, 10.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);

		bone.addOrReplaceChild("13",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone.addOrReplaceChild("14",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone.addOrReplaceChild("15",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone9.addOrReplaceChild("16",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone9.addOrReplaceChild("17",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone9.addOrReplaceChild("18",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone10.addOrReplaceChild("19",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone10.addOrReplaceChild("20",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone10.addOrReplaceChild("21",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone2.addOrReplaceChild("22",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone2.addOrReplaceChild("23",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone2.addOrReplaceChild("24",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone3.addOrReplaceChild("25",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone3.addOrReplaceChild("26",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone3.addOrReplaceChild("27",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone4.addOrReplaceChild("28",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone4.addOrReplaceChild("29",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone4.addOrReplaceChild("30",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone5.addOrReplaceChild("31",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone5.addOrReplaceChild("32",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone5.addOrReplaceChild("33",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone6.addOrReplaceChild("34",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone6.addOrReplaceChild("35",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone6.addOrReplaceChild("36",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone7.addOrReplaceChild("37",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone7.addOrReplaceChild("38",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone7.addOrReplaceChild("39",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone11.addOrReplaceChild("40",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone11.addOrReplaceChild("41",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone11.addOrReplaceChild("42",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone12.addOrReplaceChild("43",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone12.addOrReplaceChild("44",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone12.addOrReplaceChild("45",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		bone8.addOrReplaceChild("46",
				CubeListBuilder.create().texOffs(4, 24).addBox(-0.5F, -3.25F, -0.1667F, 1.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone8.addOrReplaceChild("47",
				CubeListBuilder.create().texOffs(8, 23).addBox(0.0F, -3.25F, -1.1667F, 0.0F, 4.0F, 1.0F, false),
				PartPose.ZERO);
		bone8.addOrReplaceChild("48",
				CubeListBuilder.create().texOffs(10, 23).addBox(0.0F, -4.5F, -0.1667F, 0.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 64);
	}

	private final List<ModelPart> parts = new ArrayList<>();

	public BarbedShieldModel(ModelPart part) {
		super(RenderType::entityTranslucent);
		for (int i = 0; i < 13; i++) {
			ModelPart shield3 = part.getChild(Integer.toString(i));
			parts.add(shield3);

		}
		for (int i = 13; i < 16; i++) {
			ModelPart bone = part.getChild(Integer.toString(i));
			bone.setPos(3, -17.75f, -4.58f);
			setRotationAngle(bone, -0.2618F, 0.0F, 0.48F);
			parts.add(bone);
		}

		for (int i = 16; i < 19; i++) {
			ModelPart bone9 = part.getChild(Integer.toString(i));
			bone9.setPos(0.0F, -18.25F, -4.5833F);
			setRotationAngle(bone9, -0.2618F, 0.0F, 0.0F);
			parts.add(bone9);
		}

		for (int i = 19; i < 22; i++) {
			ModelPart bone10 = part.getChild(Integer.toString(i));
			bone10.setPos(0.0F, -6.75F, -4.5833F);
			setRotationAngle(bone10, -0.2618F, 0.0F, -3.1416F);
			parts.add(bone10);
		}

		for (int i = 22; i < 25; i++) {
			ModelPart bone2 = part.getChild(Integer.toString(i));
			bone2.setPos(-3.0F, -17.75F, -4.5833F);
			setRotationAngle(bone2, -0.2618F, 0.0F, -0.48F);
			parts.add(bone2);
		}

		for (int i = 25; i < 28; i++) {
			ModelPart bone3 = part.getChild(Integer.toString(i));
			bone3.setPos(-3.0F, -7.25F, -4.5833F);
			setRotationAngle(bone3, -0.2618F, 0.0F, -2.6616F);
			parts.add(bone3);
		}

		for (int i = 28; i < 31; i++) {
			ModelPart bone4 = part.getChild(Integer.toString(i));
			bone4.setPos(3.0F, -7.25F, -4.5833F);
			setRotationAngle(bone4, -0.2618F, 0.0F, 2.6616F);
			parts.add(bone4);
		}

		for (int i = 31; i < 34; i++) {
			ModelPart bone5 = part.getChild(Integer.toString(i));
			bone5.setPos(-5.0F, -16.25F, -4.5833F);
			setRotationAngle(bone5, -0.2618F, 0.0F, -1.0908F);
			parts.add(bone5);
		}

		for (int i = 34; i < 37; i++) {
			ModelPart bone6 = part.getChild(Integer.toString(i));
			bone6.setPos(5.0F, -16.25F, -4.5833F);
			setRotationAngle(bone6, -0.2618F, 0.0F, 1.0908F);
			parts.add(bone6);
		}

		for (int i = 37; i < 40; i++) {
			ModelPart bone7 = part.getChild(Integer.toString(i));
			bone7.setPos(5.0F, -8.25F, -4.5833F);
			setRotationAngle(bone7, -0.2618F, 0.0F, 2.0508F);
			parts.add(bone7);
		}

		for (int i = 40; i < 43; i++) {
			ModelPart bone11 = part.getChild(Integer.toString(i));
			bone11.setPos(5.5F, -12.25F, -4.5833F);
			setRotationAngle(bone11, -0.2618F, 0.0F, 1.5708F);
			parts.add(bone11);
		}

		for (int i = 43; i < 46; i++) {
			ModelPart bone12 = part.getChild(Integer.toString(i));
			bone12.setPos(-5.5F, -12.25F, -4.5833F);
			setRotationAngle(bone12, -0.2618F, 0.0F, -1.5708F);
			parts.add(bone12);
		}

		for (int i = 46; i < 49; i++) {
			ModelPart bone8 = part.getChild(Integer.toString(i));
			bone8.setPos(-5.0F, -8.25F, -4.5833F);
			setRotationAngle(bone8, -0.2618F, 0.0F, -2.0508F);
			parts.add(bone8);
		}
	}

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha) {
		parts.forEach((model) -> model.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn));
	}

	public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}
