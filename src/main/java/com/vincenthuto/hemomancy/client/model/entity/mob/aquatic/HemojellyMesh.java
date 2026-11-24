package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Generated from HemojellyManOWar.bbmodel by tools/model_export/hemojelly.py. */
public final class HemojellyMesh {
	private HemojellyMesh() {
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition surface_float = root.addOrReplaceChild("surface_float", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-9.0F, -3.0F, -3.5F, 17.0F, 4.0F, 7.0F)
				.texOffs(0, 12).addBox(-8.0F, -4.0F, -2.5F, 15.0F, 1.0F, 5.0F)
				.texOffs(41, 17).addBox(-7.0F, 1.0F, -2.0F, 13.0F, 2.0F, 4.0F), PartPose.offset(0.0F, -10.0F, 0.0F));
		surface_float.addOrReplaceChild("float_bow", CubeListBuilder.create()
				.texOffs(74, 9).addBox(-1.75F, -2.0F, -2.0F, 3.0F, 3.0F, 4.0F), PartPose.offsetAndRotation(-9.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.17453293F));
		surface_float.addOrReplaceChild("float_stern", CubeListBuilder.create()
				.texOffs(109, 16).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 2.0F, 4.0F), PartPose.offsetAndRotation(8.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.20943951F));
		PartDefinition sail_crest = surface_float.addOrReplaceChild("sail_crest", CubeListBuilder.create(), PartPose.offset(-1.0F, -3.0F, 0.5F));
		sail_crest.addOrReplaceChild("sail_membrane", CubeListBuilder.create()
				.texOffs(76, 17).addBox(-7.0F, -2.0F, -1.0F, 14.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.34906585F));
		PartDefinition feeding_colony = root.addOrReplaceChild("feeding_colony", CubeListBuilder.create()
				.texOffs(0, 26).addBox(-5.0F, 0.0F, -1.0F, 11.0F, 2.0F, 2.0F)
				.texOffs(0, 19).addBox(-3.0F, 1.0F, -2.0F, 6.0F, 2.0F, 4.0F), PartPose.offset(-1.0F, -8.0F, 0.0F));
		PartDefinition feeding_polyp_01 = feeding_colony.addOrReplaceChild("feeding_polyp_01", CubeListBuilder.create(), PartPose.offset(-4.0F, 2.0F, -1.0F));
		PartDefinition feeding_polyp_01_01 = feeding_polyp_01.addOrReplaceChild("feeding_polyp_01_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		feeding_polyp_01_01.addOrReplaceChild("feeding_polyp_01_01_membrane", CubeListBuilder.create()
				.texOffs(109, 23).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.33161256F));
		PartDefinition feeding_polyp_01_02 = feeding_polyp_01_01.addOrReplaceChild("feeding_polyp_01_02", CubeListBuilder.create(), PartPose.offset(-1.0F, 2.75F, 0.0F));
		feeding_polyp_01_02.addOrReplaceChild("feeding_polyp_01_02_membrane", CubeListBuilder.create()
				.texOffs(93, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2443461F));
		PartDefinition feeding_polyp_01_03 = feeding_polyp_01_02.addOrReplaceChild("feeding_polyp_01_03", CubeListBuilder.create(), PartPose.offset(0.5F, 2.0F, 0.0F));
		feeding_polyp_01_03.addOrReplaceChild("feeding_polyp_01_03_membrane", CubeListBuilder.create()
				.texOffs(103, 29).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.52359878F));
		PartDefinition feeding_polyp_02 = feeding_colony.addOrReplaceChild("feeding_polyp_02", CubeListBuilder.create(), PartPose.offset(-2.0F, 2.0F, 1.0F));
		PartDefinition feeding_polyp_02_01 = feeding_polyp_02.addOrReplaceChild("feeding_polyp_02_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		feeding_polyp_02_01.addOrReplaceChild("feeding_polyp_02_01_membrane", CubeListBuilder.create()
				.texOffs(116, 23).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.19198622F));
		PartDefinition feeding_polyp_02_02 = feeding_polyp_02_01.addOrReplaceChild("feeding_polyp_02_02", CubeListBuilder.create(), PartPose.offset(-0.5F, 3.0F, 0.0F));
		feeding_polyp_02_02.addOrReplaceChild("feeding_polyp_02_02_membrane", CubeListBuilder.create()
				.texOffs(36, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.34906585F));
		PartDefinition feeding_polyp_02_03 = feeding_polyp_02_02.addOrReplaceChild("feeding_polyp_02_03", CubeListBuilder.create(), PartPose.offset(1.0F, 2.75F, 0.0F));
		feeding_polyp_02_03.addOrReplaceChild("feeding_polyp_02_03_membrane", CubeListBuilder.create()
				.texOffs(108, 29).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.50614548F));
		PartDefinition feeding_polyp_03 = feeding_colony.addOrReplaceChild("feeding_polyp_03", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, -1.0F));
		PartDefinition feeding_polyp_03_01 = feeding_polyp_03.addOrReplaceChild("feeding_polyp_03_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		feeding_polyp_03_01.addOrReplaceChild("feeding_polyp_03_01_membrane", CubeListBuilder.create()
				.texOffs(43, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.59341195F, 0.0F, 0.0F));
		PartDefinition feeding_polyp_03_02 = feeding_polyp_03_01.addOrReplaceChild("feeding_polyp_03_02", CubeListBuilder.create(), PartPose.offset(0.0F, 2.5F, -1.75F));
		feeding_polyp_03_02.addOrReplaceChild("feeding_polyp_03_02_membrane", CubeListBuilder.create()
				.texOffs(50, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.33161256F, 0.0F, 0.0F));
		PartDefinition feeding_polyp_03_03 = feeding_polyp_03_02.addOrReplaceChild("feeding_polyp_03_03", CubeListBuilder.create(), PartPose.offset(0.0F, 2.75F, 1.0F));
		feeding_polyp_03_03.addOrReplaceChild("feeding_polyp_03_03_membrane", CubeListBuilder.create()
				.texOffs(113, 29).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.57595865F, 0.0F, 0.0F));
		PartDefinition feeding_polyp_04 = feeding_colony.addOrReplaceChild("feeding_polyp_04", CubeListBuilder.create(), PartPose.offset(2.0F, 2.0F, 1.0F));
		PartDefinition feeding_polyp_04_01 = feeding_polyp_04.addOrReplaceChild("feeding_polyp_04_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		feeding_polyp_04_01.addOrReplaceChild("feeding_polyp_04_01_membrane", CubeListBuilder.create()
				.texOffs(57, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.41887902F, 0.0F, 0.0F));
		PartDefinition feeding_polyp_04_02 = feeding_polyp_04_01.addOrReplaceChild("feeding_polyp_04_02", CubeListBuilder.create(), PartPose.offset(0.0F, 2.75F, 1.25F));
		feeding_polyp_04_02.addOrReplaceChild("feeding_polyp_04_02_membrane", CubeListBuilder.create()
				.texOffs(64, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.33161256F, 0.0F, 0.0F));
		PartDefinition feeding_polyp_04_03 = feeding_polyp_04_02.addOrReplaceChild("feeding_polyp_04_03", CubeListBuilder.create(), PartPose.offset(0.0F, 2.75F, -1.0F));
		feeding_polyp_04_03.addOrReplaceChild("feeding_polyp_04_03_membrane", CubeListBuilder.create()
				.texOffs(118, 29).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.61086524F, 0.0F, 0.0F));
		PartDefinition feeding_polyp_05 = feeding_colony.addOrReplaceChild("feeding_polyp_05", CubeListBuilder.create(), PartPose.offset(4.0F, 1.0F, -1.0F));
		PartDefinition feeding_polyp_05_01 = feeding_polyp_05.addOrReplaceChild("feeding_polyp_05_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		feeding_polyp_05_01.addOrReplaceChild("feeding_polyp_05_01_membrane", CubeListBuilder.create()
				.texOffs(71, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.50614548F));
		PartDefinition feeding_polyp_05_02 = feeding_polyp_05_01.addOrReplaceChild("feeding_polyp_05_02", CubeListBuilder.create(), PartPose.offset(1.5F, 2.5F, 0.0F));
		feeding_polyp_05_02.addOrReplaceChild("feeding_polyp_05_02_membrane", CubeListBuilder.create()
				.texOffs(100, 24).addBox(-1.0F, -0.5F, -0.5F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.15707963F));
		PartDefinition feeding_polyp_05_03 = feeding_polyp_05_02.addOrReplaceChild("feeding_polyp_05_03", CubeListBuilder.create(), PartPose.offset(0.25F, 2.0F, 0.0F));
		feeding_polyp_05_03.addOrReplaceChild("feeding_polyp_05_03_membrane", CubeListBuilder.create()
				.texOffs(123, 29).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.45378561F));
		PartDefinition feeding_polyp_06 = feeding_colony.addOrReplaceChild("feeding_polyp_06", CubeListBuilder.create(), PartPose.offset(5.0F, 1.0F, 0.0F));
		PartDefinition feeding_polyp_06_01 = feeding_polyp_06.addOrReplaceChild("feeding_polyp_06_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		feeding_polyp_06_01.addOrReplaceChild("feeding_polyp_06_01_membrane", CubeListBuilder.create()
				.texOffs(123, 23).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.31415927F));
		PartDefinition feeding_polyp_06_02 = feeding_polyp_06_01.addOrReplaceChild("feeding_polyp_06_02", CubeListBuilder.create(), PartPose.offset(1.0F, 2.75F, 0.0F));
		feeding_polyp_06_02.addOrReplaceChild("feeding_polyp_06_02_membrane", CubeListBuilder.create()
				.texOffs(78, 24).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.55850536F));
		PartDefinition feeding_polyp_06_03 = feeding_polyp_06_02.addOrReplaceChild("feeding_polyp_06_03", CubeListBuilder.create(), PartPose.offset(1.5F, 2.5F, 0.0F));
		feeding_polyp_06_03.addOrReplaceChild("feeding_polyp_06_03_membrane", CubeListBuilder.create()
				.texOffs(32, 30).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.20943951F));
		PartDefinition capture_tendrils = root.addOrReplaceChild("capture_tendrils", CubeListBuilder.create(), PartPose.offset(0.0F, -7.0F, 0.0F));
		PartDefinition capture_tendril_01 = capture_tendrils.addOrReplaceChild("capture_tendril_01", CubeListBuilder.create(), PartPose.offset(-6.0F, 0.0F, -1.0F));
		PartDefinition capture_tendril_01_01 = capture_tendril_01.addOrReplaceChild("capture_tendril_01_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		capture_tendril_01_01.addOrReplaceChild("capture_tendril_01_01_membrane", CubeListBuilder.create()
				.texOffs(74, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2443461F));
		PartDefinition capture_tendril_01_02 = capture_tendril_01_01.addOrReplaceChild("capture_tendril_01_02", CubeListBuilder.create(), PartPose.offset(-1.5F, 5.75F, 0.0F));
		capture_tendril_01_02.addOrReplaceChild("capture_tendril_01_02_membrane", CubeListBuilder.create()
				.texOffs(79, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.40142573F));
		PartDefinition capture_tendril_01_03 = capture_tendril_01_02.addOrReplaceChild("capture_tendril_01_03", CubeListBuilder.create(), PartPose.offset(-2.25F, 5.5F, 0.0F));
		capture_tendril_01_03.addOrReplaceChild("capture_tendril_01_03_membrane", CubeListBuilder.create()
				.texOffs(114, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.06981317F));
		PartDefinition capture_tendril_01_04 = capture_tendril_01_03.addOrReplaceChild("capture_tendril_01_04", CubeListBuilder.create(), PartPose.offset(0.25F, 5.0F, 0.0F));
		capture_tendril_01_04.addOrReplaceChild("capture_tendril_01_04_membrane", CubeListBuilder.create()
				.texOffs(21, 19).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.43633231F));
		PartDefinition capture_tendril_01_05 = capture_tendril_01_04.addOrReplaceChild("capture_tendril_01_05", CubeListBuilder.create(), PartPose.offset(1.75F, 3.75F, 0.0F));
		capture_tendril_01_05.addOrReplaceChild("capture_tendril_01_05_membrane", CubeListBuilder.create()
				.texOffs(37, 30).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1875F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.62831853F));
		PartDefinition capture_tendril_02 = capture_tendrils.addOrReplaceChild("capture_tendril_02", CubeListBuilder.create(), PartPose.offset(-4.0F, 1.0F, 1.0F));
		PartDefinition capture_tendril_02_01 = capture_tendril_02.addOrReplaceChild("capture_tendril_02_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		capture_tendril_02_01.addOrReplaceChild("capture_tendril_02_01_membrane", CubeListBuilder.create()
				.texOffs(49, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.13962634F));
		PartDefinition capture_tendril_02_02 = capture_tendril_02_01.addOrReplaceChild("capture_tendril_02_02", CubeListBuilder.create(), PartPose.offset(-1.0F, 7.0F, 0.0F));
		capture_tendril_02_02.addOrReplaceChild("capture_tendril_02_02_membrane", CubeListBuilder.create()
				.texOffs(54, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.17453293F));
		PartDefinition capture_tendril_02_03 = capture_tendril_02_02.addOrReplaceChild("capture_tendril_02_03", CubeListBuilder.create(), PartPose.offset(1.25F, 7.0F, 0.0F));
		capture_tendril_02_03.addOrReplaceChild("capture_tendril_02_03_membrane", CubeListBuilder.create()
				.texOffs(84, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.36651914F));
		PartDefinition capture_tendril_02_04 = capture_tendril_02_03.addOrReplaceChild("capture_tendril_02_04", CubeListBuilder.create(), PartPose.offset(2.25F, 5.5F, 0.0F));
		capture_tendril_02_04.addOrReplaceChild("capture_tendril_02_04_membrane", CubeListBuilder.create()
				.texOffs(89, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.13962634F));
		PartDefinition capture_tendril_02_05 = capture_tendril_02_04.addOrReplaceChild("capture_tendril_02_05", CubeListBuilder.create(), PartPose.offset(-0.75F, 6.0F, 0.0F));
		capture_tendril_02_05.addOrReplaceChild("capture_tendril_02_05_membrane", CubeListBuilder.create()
				.texOffs(119, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.43633231F));
		PartDefinition capture_tendril_02_06 = capture_tendril_02_05.addOrReplaceChild("capture_tendril_02_06", CubeListBuilder.create(), PartPose.offset(-2.0F, 4.5F, 0.0F));
		capture_tendril_02_06.addOrReplaceChild("capture_tendril_02_06_membrane", CubeListBuilder.create()
				.texOffs(27, 26).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.1875F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.73303829F));
		PartDefinition capture_tendril_03 = capture_tendrils.addOrReplaceChild("capture_tendril_03", CubeListBuilder.create(), PartPose.offset(-1.0F, 1.0F, -1.0F));
		PartDefinition capture_tendril_03_01 = capture_tendril_03.addOrReplaceChild("capture_tendril_03_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		capture_tendril_03_01.addOrReplaceChild("capture_tendril_03_01_membrane", CubeListBuilder.create()
				.texOffs(94, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2268928F, 0.0F, 0.0F));
		PartDefinition capture_tendril_03_02 = capture_tendril_03_01.addOrReplaceChild("capture_tendril_03_02", CubeListBuilder.create(), PartPose.offset(0.0F, 5.75F, -1.25F));
		capture_tendril_03_02.addOrReplaceChild("capture_tendril_03_02_membrane", CubeListBuilder.create()
				.texOffs(114, 8).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.50614548F, 0.0F, 0.0F));
		PartDefinition capture_tendril_03_03 = capture_tendril_03_02.addOrReplaceChild("capture_tendril_03_03", CubeListBuilder.create(), PartPose.offset(0.0F, 4.25F, -2.5F));
		capture_tendril_03_03.addOrReplaceChild("capture_tendril_03_03_membrane", CubeListBuilder.create()
				.texOffs(59, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.15707963F, 0.0F, 0.0F));
		PartDefinition capture_tendril_03_04 = capture_tendril_03_03.addOrReplaceChild("capture_tendril_03_04", CubeListBuilder.create(), PartPose.offset(0.0F, 7.0F, -1.0F));
		capture_tendril_03_04.addOrReplaceChild("capture_tendril_03_04_membrane", CubeListBuilder.create()
				.texOffs(26, 19).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.34906585F, 0.0F, 0.0F));
		PartDefinition capture_tendril_03_05 = capture_tendril_03_04.addOrReplaceChild("capture_tendril_03_05", CubeListBuilder.create(), PartPose.offset(0.0F, 3.75F, 1.25F));
		capture_tendril_03_05.addOrReplaceChild("capture_tendril_03_05_membrane", CubeListBuilder.create()
				.texOffs(93, 29).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.1875F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.61086524F, 0.0F, 0.0F));
		PartDefinition capture_tendril_04 = capture_tendrils.addOrReplaceChild("capture_tendril_04", CubeListBuilder.create(), PartPose.offset(1.0F, 1.0F, 1.0F));
		PartDefinition capture_tendril_04_01 = capture_tendril_04.addOrReplaceChild("capture_tendril_04_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		capture_tendril_04_01.addOrReplaceChild("capture_tendril_04_01_membrane", CubeListBuilder.create()
				.texOffs(119, 8).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.27925268F));
		PartDefinition capture_tendril_04_02 = capture_tendril_04_01.addOrReplaceChild("capture_tendril_04_02", CubeListBuilder.create(), PartPose.offset(1.5F, 4.75F, 0.0F));
		capture_tendril_04_02.addOrReplaceChild("capture_tendril_04_02_membrane", CubeListBuilder.create()
				.texOffs(64, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4712389F));
		PartDefinition capture_tendril_04_03 = capture_tendril_04_02.addOrReplaceChild("capture_tendril_04_03", CubeListBuilder.create(), PartPose.offset(3.25F, 6.25F, 0.0F));
		capture_tendril_04_03.addOrReplaceChild("capture_tendril_04_03_membrane", CubeListBuilder.create()
				.texOffs(99, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.19198622F));
		PartDefinition capture_tendril_04_04 = capture_tendril_04_03.addOrReplaceChild("capture_tendril_04_04", CubeListBuilder.create(), PartPose.offset(1.25F, 6.0F, 0.0F));
		capture_tendril_04_04.addOrReplaceChild("capture_tendril_04_04_membrane", CubeListBuilder.create()
				.texOffs(104, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.19198622F));
		PartDefinition capture_tendril_04_05 = capture_tendril_04_04.addOrReplaceChild("capture_tendril_04_05", CubeListBuilder.create(), PartPose.offset(-1.25F, 6.0F, 0.0F));
		capture_tendril_04_05.addOrReplaceChild("capture_tendril_04_05_membrane", CubeListBuilder.create()
				.texOffs(83, 24).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.1875F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.59341195F));
		PartDefinition capture_tendril_05 = capture_tendrils.addOrReplaceChild("capture_tendril_05", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, -1.0F));
		PartDefinition capture_tendril_05_01 = capture_tendril_05.addOrReplaceChild("capture_tendril_05_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		capture_tendril_05_01.addOrReplaceChild("capture_tendril_05_01_membrane", CubeListBuilder.create()
				.texOffs(89, 9).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.26179939F, 0.0F, 0.0F));
		PartDefinition capture_tendril_05_02 = capture_tendril_05_01.addOrReplaceChild("capture_tendril_05_02", CubeListBuilder.create(), PartPose.offset(0.0F, 4.75F, -1.25F));
		capture_tendril_05_02.addOrReplaceChild("capture_tendril_05_02_membrane", CubeListBuilder.create()
				.texOffs(69, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.19198622F, 0.0F, 0.0F));
		PartDefinition capture_tendril_05_03 = capture_tendril_05_02.addOrReplaceChild("capture_tendril_05_03", CubeListBuilder.create(), PartPose.offset(0.0F, 6.75F, 1.25F));
		capture_tendril_05_03.addOrReplaceChild("capture_tendril_05_03_membrane", CubeListBuilder.create()
				.texOffs(94, 9).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.45378561F, 0.0F, 0.0F));
		PartDefinition capture_tendril_05_04 = capture_tendril_05_03.addOrReplaceChild("capture_tendril_05_04", CubeListBuilder.create(), PartPose.offset(0.0F, 4.5F, 2.25F));
		capture_tendril_05_04.addOrReplaceChild("capture_tendril_05_04_membrane", CubeListBuilder.create()
				.texOffs(88, 24).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.1875F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.61086524F, 0.0F, 0.0F));
		PartDefinition capture_tendril_06 = capture_tendrils.addOrReplaceChild("capture_tendril_06", CubeListBuilder.create(), PartPose.offset(5.0F, 0.0F, 0.0F));
		PartDefinition capture_tendril_06_01 = capture_tendril_06.addOrReplaceChild("capture_tendril_06_01", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
		capture_tendril_06_01.addOrReplaceChild("capture_tendril_06_01_membrane", CubeListBuilder.create()
				.texOffs(99, 9).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.41887902F));
		PartDefinition capture_tendril_06_02 = capture_tendril_06_01.addOrReplaceChild("capture_tendril_06_02", CubeListBuilder.create(), PartPose.offset(2.0F, 4.5F, 0.0F));
		capture_tendril_06_02.addOrReplaceChild("capture_tendril_06_02_membrane", CubeListBuilder.create()
				.texOffs(104, 9).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.27925268F));
		PartDefinition capture_tendril_06_03 = capture_tendril_06_02.addOrReplaceChild("capture_tendril_06_03", CubeListBuilder.create(), PartPose.offset(1.5F, 4.75F, 0.0F));
		capture_tendril_06_03.addOrReplaceChild("capture_tendril_06_03_membrane", CubeListBuilder.create()
				.texOffs(109, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.13962634F));
		PartDefinition capture_tendril_06_04 = capture_tendril_06_03.addOrReplaceChild("capture_tendril_06_04", CubeListBuilder.create(), PartPose.offset(-0.75F, 6.0F, 0.0F));
		capture_tendril_06_04.addOrReplaceChild("capture_tendril_06_04_membrane", CubeListBuilder.create()
				.texOffs(31, 19).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.31415927F));
		PartDefinition capture_tendril_06_05 = capture_tendril_06_04.addOrReplaceChild("capture_tendril_06_05", CubeListBuilder.create(), PartPose.offset(-1.25F, 3.75F, 0.0F));
		capture_tendril_06_05.addOrReplaceChild("capture_tendril_06_05_membrane", CubeListBuilder.create()
				.texOffs(98, 29).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.1875F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.48869219F));
		return LayerDefinition.create(mesh, 128, 64);
	}
}
