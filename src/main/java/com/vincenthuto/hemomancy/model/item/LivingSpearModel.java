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

public class LivingSpearModel extends Model {
	public static final ModelLayerLocation living_spear = new ModelLayerLocation(
			Hemomancy.rloc("living_spear"), "main");

	public static LayerDefinition createLayers() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition grip = mesh.getRoot();
		grip.addOrReplaceChild("0",
				CubeListBuilder.create().texOffs(4, 27).addBox(-2.5F, -8.0F, 6.5F, 2.0F, 6.0F, 2.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("1",
				CubeListBuilder.create().texOffs(15, 6).addBox(-2.75F, -3.9F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("2",
				CubeListBuilder.create().texOffs(15, 0).addBox(-2.75F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("3",
				CubeListBuilder.create().texOffs(0, 9).addBox(-2.75F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("4",
				CubeListBuilder.create().texOffs(0, 6).addBox(-1.25F, -3.9F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("5",
				CubeListBuilder.create().texOffs(0, 28).addBox(-2.0F, -3.6F, 6.25F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("6",
				CubeListBuilder.create().texOffs(27, 0).addBox(-2.0F, -7.6F, 6.25F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("7",
				CubeListBuilder.create().texOffs(28, 15).addBox(-2.0F, -5.1F, 6.25F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("8",
				CubeListBuilder.create().texOffs(0, 3).addBox(-1.25F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("9",
				CubeListBuilder.create().texOffs(12, 12).addBox(-1.25F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("10",
				CubeListBuilder.create().texOffs(23, 24).addBox(-2.0F, -3.6F, 7.75F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("11",
				CubeListBuilder.create().texOffs(27, 21).addBox(-2.0F, -5.1F, 7.75F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip.addOrReplaceChild("12",
				CubeListBuilder.create().texOffs(12, 17).addBox(-2.0F, -7.6F, 7.75F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		PartDefinition grip2 = mesh.getRoot();
		grip2.addOrReplaceChild("13",
				CubeListBuilder.create().texOffs(4, 27).addBox(-2.5F, -8.0F, 6.5F, 2.0F, 6.0F, 2.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("14",
				CubeListBuilder.create().texOffs(39, 0).addBox(-2.0F, -38.0F, 7.0F, 1.0F, 40.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("15",
				CubeListBuilder.create().texOffs(15, 6).addBox(-2.75F, -3.9F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("16",
				CubeListBuilder.create().texOffs(15, 0).addBox(-2.75F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("17",
				CubeListBuilder.create().texOffs(0, 9).addBox(-2.75F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("18",
				CubeListBuilder.create().texOffs(0, 6).addBox(-1.25F, -3.9F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("19",
				CubeListBuilder.create().texOffs(0, 28).addBox(-2.0F, -3.6F, 6.25F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("20",
				CubeListBuilder.create().texOffs(27, 0).addBox(-2.0F, -7.6F, 6.25F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("21",
				CubeListBuilder.create().texOffs(28, 15).addBox(-2.0F, -5.1F, 6.25F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("22",
				CubeListBuilder.create().texOffs(0, 3).addBox(-1.25F, -5.5F, 7.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("23",
				CubeListBuilder.create().texOffs(12, 12).addBox(-1.25F, -7.75F, 7.0F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("24",
				CubeListBuilder.create().texOffs(23, 24).addBox(-2.0F, -3.6F, 7.75F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("25",
				CubeListBuilder.create().texOffs(27, 21).addBox(-2.0F, -5.1F, 7.75F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		grip2.addOrReplaceChild("26",
				CubeListBuilder.create().texOffs(12, 17).addBox(-2.0F, -7.6F, 7.75F, 1.0F, 2.0F, 1.0F, false),
				PartPose.ZERO);

		PartDefinition pommel = mesh.getRoot();
		pommel.addOrReplaceChild("27",
				CubeListBuilder.create().texOffs(34, 22).addBox(-3.0F, -1.6F, 7.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		pommel.addOrReplaceChild("28",
				CubeListBuilder.create().texOffs(33, 22).addBox(-2.0F, -1.6F, 8.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		pommel.addOrReplaceChild("29",
				CubeListBuilder.create().texOffs(26, 11).addBox(-2.5F, -2.0F, 6.5F, 2.0F, 2.0F, 2.0F, false),
				PartPose.ZERO);
		pommel.addOrReplaceChild("30",
				CubeListBuilder.create().texOffs(6, 3).addBox(-2.0F, -1.5F, 7.0F, 1.0F, 3.0F, 1.0F, false),
				PartPose.ZERO);
		pommel.addOrReplaceChild("31",
				CubeListBuilder.create().texOffs(34, 22).addBox(-1.0F, -1.6F, 7.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		pommel.addOrReplaceChild("32",
				CubeListBuilder.create().texOffs(33, 22).addBox(-2.0F, -1.6F, 6.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);

		PartDefinition swell = mesh.getRoot();
		swell.addOrReplaceChild("33",
				CubeListBuilder.create().texOffs(24, 11).addBox(-3.0F, -9.1F, 5.7F, 3.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("34",
				CubeListBuilder.create().texOffs(24, 17).addBox(-3.0F, -9.0F, 6.0F, 3.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("35",
				CubeListBuilder.create().texOffs(24, 17).addBox(-2.5F, -8.0F, 6.0F, 2.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("36",
				CubeListBuilder.create().texOffs(27, 6).addBox(-3.25F, -9.1F, 6.0F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("37",
				CubeListBuilder.create().texOffs(21, 24).addBox(-3.0F, -9.1F, 6.3F, 3.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("38",
				CubeListBuilder.create().texOffs(24, 28).addBox(-0.75F, -9.1F, 6.0F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("39",
				CubeListBuilder.create().texOffs(19, 1).addBox(-3.2F, -11.0F, 6.0F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("40",
				CubeListBuilder.create().texOffs(19, 1).addBox(-2.95F, -11.0F, 8.25F, 3.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("41",
				CubeListBuilder.create().texOffs(19, 1).addBox(-2.95F, -11.0F, 5.75F, 3.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("42",
				CubeListBuilder.create().texOffs(20, 1).addBox(-0.7F, -11.0F, 6.0F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("43",
				CubeListBuilder.create().texOffs(4, 7).addBox(-3.5F, -12.0F, 5.5F, 4.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("44",
				CubeListBuilder.create().texOffs(22, 6).addBox(-3.0F, -12.5F, 6.0F, 3.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("45",
				CubeListBuilder.create().texOffs(6, 4).addBox(-2.0F, -14.75F, 8.5F, 1.0F, 1.0F, 2.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("46",
				CubeListBuilder.create().texOffs(6, 4).addBox(-2.0F, -14.75F, 4.5F, 1.0F, 1.0F, 2.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("47",
				CubeListBuilder.create().texOffs(7, 6).addBox(-2.0F, -13.5F, 5.0F, 1.0F, 1.0F, 2.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("48",
				CubeListBuilder.create().texOffs(6, 5).addBox(-2.0F, -13.5F, 8.0F, 1.0F, 1.0F, 2.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("49",
				CubeListBuilder.create().texOffs(29, 29).addBox(-3.0F, -14.0F, 6.0F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("50",
				CubeListBuilder.create().texOffs(29, 29).addBox(-1.0F, -14.0F, 6.0F, 1.0F, 1.0F, 3.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("51",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -12.5F, 4.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("52",
				CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -12.5F, 10.0F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("53",
				CubeListBuilder.create().texOffs(7, 4).addBox(-2.9F, -13.0F, 6.25F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("54",
				CubeListBuilder.create().texOffs(7, 4).addBox(-2.9F, -13.0F, 8.25F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("55",
				CubeListBuilder.create().texOffs(7, 4).addBox(-1.1F, -13.0F, 6.25F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("56",
				CubeListBuilder.create().texOffs(7, 4).addBox(-1.1F, -13.0F, 8.25F, 1.0F, 1.0F, 1.0F, false),
				PartPose.ZERO);
		swell.addOrReplaceChild("143",
				CubeListBuilder.create().texOffs(4, 7).addBox(-3.5F, -10.0F, 5.5F, 4.0F, 1.0F, 4.0F, false),
				PartPose.ZERO);

		PartDefinition blade = mesh.getRoot();
		blade.addOrReplaceChild("57",
				CubeListBuilder.create().texOffs(9, 29).addBox(-1.5F, -44.0F, 15.0F, 0.02F, 10.0F, 7.0F, false),
				PartPose.ZERO);
		blade.addOrReplaceChild("58",
				CubeListBuilder.create().texOffs(6, 49).addBox(-1.5F, -44.5F, 17.75F, 0.02F, 1.0F, 1.0F, false),
				PartPose.ZERO);

		PartDefinition blade2 = mesh.getRoot();
		blade2.addOrReplaceChild("59",
				CubeListBuilder.create().texOffs(9, 29).addBox(-1.5F, -44.0F, 15.0F, 0.02F, 10.0F, 7.0F, true),
				PartPose.ZERO);
		blade2.addOrReplaceChild("60",
				CubeListBuilder.create().texOffs(6, 49).addBox(-1.5F, -44.5F, 17.75F, 0.02F, 1.0F, 1.0F, true),
				PartPose.ZERO);

		return LayerDefinition.create(mesh, 64, 64);
	}

	private final List<ModelPart> parts = new ArrayList<>();

	public LivingSpearModel(ModelPart part) {
		super(RenderType::entityTranslucent);
		for (int i = 0; i < 13; i++) {
			ModelPart grip = part.getChild(Integer.toString(i));
			grip.setPos(0, -14, 0);
			parts.add(grip);
		}
		for (int i = 13; i < 28; i++) {
			ModelPart grip2 = part.getChild(Integer.toString(i));
			grip2.setPos(0, -1, 0);
			parts.add(grip2);

		}

		for (int i = 27; i < 33; i++) {
			ModelPart pommel = part.getChild(Integer.toString(i));
			pommel.setPos(0, 2, 0);
			parts.add(pommel);

		}
		for (int i = 33; i < 57; i++) {
			ModelPart swell = part.getChild(Integer.toString(i));
			swell.setPos(0, -23, 0);
			parts.add(swell);
		}
		ModelPart grip3 = part.getChild("143");
		grip3.setPos(0, -23, 0);
		parts.add(grip3);
		for (int i = 57; i < 59; i++) {
			ModelPart blade = part.getChild(Integer.toString(i));
			blade.setPos(0, -3, -11);
			parts.add(blade);

		}
		for (int i = 59; i < 61; i++) {
			ModelPart blade2 = part.getChild(Integer.toString(i));
			blade2.setPos(0, -3, -11);
			parts.add(blade2);
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
