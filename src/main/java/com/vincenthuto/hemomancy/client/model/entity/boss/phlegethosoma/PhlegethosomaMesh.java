package com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Geometry baked from PhlegethosomaModel.bbmodel by tools/model_export/phlegethosoma.py. */
final class PhlegethosomaMesh {
    private PhlegethosomaMesh() {}

    static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition arterial_fluke = root.addOrReplaceChild("arterial_fluke", CubeListBuilder.create(), PartPose.offset(0F, 19F, 6F));

        PartDefinition head = arterial_fluke.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-8F, -5F, -11F, 16F, 10F, 14F)
                .texOffs(62, 1).addBox(-10F, -6F, -17F, 20F, 12F, 13F)
                .texOffs(129, 1).addBox(-7F, -7F, -15F, 14F, 2F, 8F), PartPose.offsetAndRotation(0F, -1F, 1F, -0.139626F, -0.226893F, 0F));

        PartDefinition oral_sucker = head.addOrReplaceChild("oral_sucker", CubeListBuilder.create()
                .texOffs(174, 1).addBox(-8F, -5F, -1F, 16F, 10F, 2F)
                .texOffs(36, 27).addBox(-11F, -3F, -4F, 3F, 6F, 5F)
                .texOffs(91, 27).addBox(-9F, -4F, -3F, 2F, 8F, 3F)
                .texOffs(102, 27).addBox(-12F, -2F, -2F, 2F, 4F, 4F)
                .texOffs(115, 27).addBox(8F, -3F, -4F, 3F, 6F, 5F)
                .texOffs(170, 27).addBox(7F, -4F, -3F, 2F, 8F, 3F)
                .texOffs(181, 27).addBox(10F, -2F, -2F, 2F, 4F, 4F), PartPose.offset(0F, 0F, -17F));

        oral_sucker.addOrReplaceChild("upper_lip", CubeListBuilder.create()
                .texOffs(211, 1).addBox(-6F, -3F, -4F, 12F, 3F, 5F)
                .texOffs(53, 27).addBox(-10F, -2F, -4F, 4F, 3F, 5F)
                .texOffs(132, 27).addBox(6F, -2F, -4F, 4F, 3F, 5F), PartPose.offset(0F, -4F, 0F));
        oral_sucker.addOrReplaceChild("lower_lip", CubeListBuilder.create()
                .texOffs(1, 27).addBox(-6F, 0F, -4F, 12F, 3F, 5F)
                .texOffs(72, 27).addBox(-10F, -1F, -4F, 4F, 3F, 5F)
                .texOffs(151, 27).addBox(6F, -1F, -4F, 4F, 3F, 5F), PartPose.offset(0F, 4F, 0F));
        PartDefinition forebody = arterial_fluke.addOrReplaceChild("forebody", CubeListBuilder.create()
                .texOffs(194, 27).addBox(-8F, -5F, -2F, 16F, 10F, 14F)
                .texOffs(1, 52).addBox(-10F, -3F, 3F, 20F, 6F, 10F)
                .texOffs(62, 52).addBox(-6F, 4F, 6F, 12F, 3F, 10F)
                .texOffs(107, 52).addBox(-4F, 6F, 6F, 8F, 2F, 2F)
                .texOffs(128, 52).addBox(-4F, 6F, 14F, 8F, 2F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.0872665F, 0F));

        PartDefinition crop = forebody.addOrReplaceChild("crop", CubeListBuilder.create()
                .texOffs(149, 52).addBox(-10F, -7F, -2F, 20F, 14F, 21F), PartPose.offsetAndRotation(0F, 0F, 11F, 0.0349066F, 0.20944F, 0F));

        PartDefinition swim_segment_01 = crop.addOrReplaceChild("swim_segment_01", CubeListBuilder.create()
                .texOffs(37, 115).addBox(-10F, -5F, -2F, 20F, 10F, 13F)
                .texOffs(104, 115).addBox(-8F, -6F, 0F, 16F, 2F, 9F), PartPose.offsetAndRotation(0F, 0F, 17F, 0.0349066F, 0.174533F, 0F));

        PartDefinition swim_segment_02 = swim_segment_01.addOrReplaceChild("swim_segment_02", CubeListBuilder.create()
                .texOffs(31, 139).addBox(-9F, -4F, -2F, 18F, 8F, 13F)
                .texOffs(94, 139).addBox(-7F, -5F, 0F, 14F, 2F, 9F), PartPose.offsetAndRotation(0F, 0F, 10F, -0.0174533F, 0.0349066F, 0F));

        PartDefinition swim_segment_03 = swim_segment_02.addOrReplaceChild("swim_segment_03", CubeListBuilder.create()
                .texOffs(10, 161).addBox(-8F, -4F, -2F, 16F, 7F, 13F)
                .texOffs(69, 161).addBox(-6F, -5F, 0F, 12F, 2F, 9F), PartPose.offsetAndRotation(0F, 0F, 10F, -0.0349066F, -0.226893F, 0F));

        PartDefinition swim_segment_04 = swim_segment_03.addOrReplaceChild("swim_segment_04", CubeListBuilder.create()
                .texOffs(1, 182).addBox(-7F, -3F, -2F, 14F, 6F, 13F)
                .texOffs(56, 182).addBox(-5F, -4F, 0F, 10F, 2F, 9F), PartPose.offsetAndRotation(0F, 0F, 10F, -0.0174533F, -0.296706F, 0F));

        PartDefinition swim_segment_05 = swim_segment_04.addOrReplaceChild("swim_segment_05", CubeListBuilder.create()
                .texOffs(1, 202).addBox(-6F, -3F, -2F, 12F, 5F, 13F)
                .texOffs(52, 202).addBox(-4F, -4F, 0F, 8F, 2F, 9F), PartPose.offsetAndRotation(0F, 0F, 10F, 0.0349066F, -0.244346F, 0F));

        PartDefinition swim_segment_06 = swim_segment_05.addOrReplaceChild("swim_segment_06", CubeListBuilder.create()
                .texOffs(203, 202).addBox(-5F, -2F, -2F, 10F, 4F, 13F)
                .texOffs(1, 221).addBox(-3F, -3F, 0F, 6F, 2F, 9F), PartPose.offsetAndRotation(0F, 0F, 10F, 0.0523599F, -0.10472F, 0F));

        PartDefinition swim_segment_07 = swim_segment_06.addOrReplaceChild("swim_segment_07", CubeListBuilder.create()
                .texOffs(148, 221).addBox(-4F, -2F, -2F, 8F, 3F, 12F), PartPose.offsetAndRotation(0F, 0F, 10F, 0.0523599F, 0.15708F, 0F));

        PartDefinition swim_segment_08 = swim_segment_07.addOrReplaceChild("swim_segment_08", CubeListBuilder.create()
                .texOffs(18, 237).addBox(-3F, -1F, -2F, 6F, 2F, 11F), PartPose.offsetAndRotation(0F, 0F, 9F, -0.0349066F, 0.296706F, 0F));

        PartDefinition ribbon_tail = swim_segment_08.addOrReplaceChild("ribbon_tail", CubeListBuilder.create()
                .texOffs(117, 237).addBox(-3F, -2F, -2F, 6F, 2F, 10F)
                .texOffs(150, 237).addBox(-4F, -1F, 2F, 8F, 1F, 8F), PartPose.offsetAndRotation(0F, 0F, 7F, -0.0523599F, 0.279253F, 0F));

        ribbon_tail.addOrReplaceChild("ribbon_tip", CubeListBuilder.create()
                .texOffs(183, 237).addBox(-3F, -1F, -1F, 6F, 1F, 7F)
                .texOffs(210, 237).addBox(-2F, -1F, 5F, 4F, 1F, 4F)
                .texOffs(227, 237).addBox(-1F, -1F, 8F, 2F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 8F, -0.10472F, 0.174533F, 0F));
        swim_segment_08.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(53, 237).addBox(-3F, -1F, -1F, 3F, 1F, 5F)
                .texOffs(70, 237).addBox(-2F, -2F, 3F, 2F, 1F, 5F), PartPose.offset(-2F, 0F, 0F));
        swim_segment_08.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(85, 237).addBox(0F, -1F, -1F, 3F, 1F, 5F)
                .texOffs(102, 237).addBox(0F, -2F, 3F, 2F, 1F, 5F), PartPose.offset(2F, 0F, 0F));
        swim_segment_07.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(189, 221).addBox(-3F, -1F, -1F, 3F, 1F, 6F)
                .texOffs(208, 221).addBox(-2F, -2F, 4F, 2F, 1F, 6F), PartPose.offset(-3F, 0F, 0F));
        swim_segment_07.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(225, 221).addBox(0F, -1F, 0F, 3F, 1F, 6F)
                .texOffs(1, 237).addBox(0F, -2F, 5F, 2F, 1F, 6F), PartPose.offset(3F, 0F, 0F));
        swim_segment_06.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(32, 221).addBox(-4F, -1F, -1F, 4F, 1F, 6F)
                .texOffs(53, 221).addBox(-5F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(62, 221).addBox(-3F, -2F, 4F, 3F, 1F, 6F)
                .texOffs(81, 221).addBox(-4F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(-4F, 1F, 0F));
        swim_segment_06.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(90, 221).addBox(0F, -1F, -1F, 4F, 1F, 6F)
                .texOffs(111, 221).addBox(4F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(120, 221).addBox(0F, -2F, 4F, 3F, 1F, 6F)
                .texOffs(139, 221).addBox(3F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(4F, 1F, 0F));
        swim_segment_05.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(87, 202).addBox(-4F, -1F, -1F, 4F, 1F, 6F)
                .texOffs(108, 202).addBox(-5F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(117, 202).addBox(-3F, -2F, 4F, 3F, 1F, 6F)
                .texOffs(136, 202).addBox(-4F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(-5F, 1F, 0F));
        swim_segment_05.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(145, 202).addBox(0F, -1F, 0F, 4F, 1F, 6F)
                .texOffs(166, 202).addBox(4F, -1F, 1F, 1F, 1F, 3F)
                .texOffs(175, 202).addBox(0F, -2F, 5F, 3F, 1F, 6F)
                .texOffs(194, 202).addBox(3F, -2F, 6F, 1F, 1F, 3F), PartPose.offset(5F, 1F, 0F));
        swim_segment_04.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(95, 182).addBox(-4F, -1F, -1F, 4F, 1F, 6F)
                .texOffs(116, 182).addBox(-5F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(125, 182).addBox(-3F, -2F, 4F, 3F, 1F, 6F)
                .texOffs(144, 182).addBox(-4F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(-6F, 1F, 0F));
        swim_segment_04.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(153, 182).addBox(0F, -1F, -1F, 4F, 1F, 6F)
                .texOffs(174, 182).addBox(4F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(183, 182).addBox(0F, -2F, 4F, 3F, 1F, 6F)
                .texOffs(202, 182).addBox(3F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(6F, 1F, 0F));
        swim_segment_03.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(112, 161).addBox(-5F, -1F, -1F, 5F, 1F, 6F)
                .texOffs(135, 161).addBox(-6F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(144, 161).addBox(-4F, -2F, 4F, 4F, 1F, 6F)
                .texOffs(165, 161).addBox(-5F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(-7F, 1F, 0F));
        swim_segment_03.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(174, 161).addBox(0F, -1F, 0F, 5F, 1F, 6F)
                .texOffs(197, 161).addBox(5F, -1F, 1F, 1F, 1F, 3F)
                .texOffs(206, 161).addBox(0F, -2F, 5F, 4F, 1F, 6F)
                .texOffs(227, 161).addBox(4F, -2F, 6F, 1F, 1F, 3F), PartPose.offset(7F, 1F, 0F));
        swim_segment_02.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(141, 139).addBox(-5F, -1F, -1F, 5F, 1F, 6F)
                .texOffs(164, 139).addBox(-6F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(173, 139).addBox(-4F, -2F, 4F, 4F, 1F, 6F)
                .texOffs(194, 139).addBox(-5F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(-8F, 1F, 0F));
        swim_segment_02.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(203, 139).addBox(0F, -1F, -1F, 5F, 1F, 6F)
                .texOffs(226, 139).addBox(5F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(235, 139).addBox(0F, -2F, 4F, 4F, 1F, 6F)
                .texOffs(1, 161).addBox(4F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(8F, 1F, 0F));
        swim_segment_01.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(155, 115).addBox(-5F, -1F, -1F, 5F, 1F, 6F)
                .texOffs(178, 115).addBox(-6F, -1F, 0F, 1F, 1F, 3F)
                .texOffs(187, 115).addBox(-4F, -2F, 4F, 4F, 1F, 6F)
                .texOffs(208, 115).addBox(-5F, -2F, 5F, 1F, 1F, 3F), PartPose.offset(-9F, 1F, 0F));
        swim_segment_01.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(217, 115).addBox(0F, -1F, 0F, 5F, 1F, 6F)
                .texOffs(240, 115).addBox(5F, -1F, 1F, 1F, 1F, 3F)
                .texOffs(1, 139).addBox(0F, -2F, 5F, 4F, 1F, 6F)
                .texOffs(22, 139).addBox(4F, -2F, 6F, 1F, 1F, 3F), PartPose.offset(9F, 1F, 0F));
        crop.addOrReplaceChild("blood_sac", CubeListBuilder.create()
                .texOffs(1, 88).addBox(-12F, -7F, -7F, 24F, 12F, 14F)
                .texOffs(78, 88).addBox(-8F, -9F, -6F, 16F, 3F, 11F), PartPose.offset(0F, 1F, 9F));
        crop.addOrReplaceChild("left_frill", CubeListBuilder.create()
                .texOffs(133, 88).addBox(-4F, -1F, 1F, 4F, 1F, 8F)
                .texOffs(158, 88).addBox(-4.75F, -1F, 2F, 1F, 1F, 5F)
                .texOffs(171, 88).addBox(-3F, -2F, 8F, 3F, 1F, 8F)
                .texOffs(194, 88).addBox(-3.75F, -2F, 9F, 1F, 1F, 5F), PartPose.offset(-11F, 1F, 0F));
        crop.addOrReplaceChild("right_frill", CubeListBuilder.create()
                .texOffs(207, 88).addBox(0F, -1F, 2F, 4F, 1F, 8F)
                .texOffs(232, 88).addBox(3.75F, -1F, 3F, 1F, 1F, 5F)
                .texOffs(1, 115).addBox(0F, -2F, 9F, 3F, 1F, 8F)
                .texOffs(24, 115).addBox(2.75F, -2F, 10F, 1F, 1F, 5F), PartPose.offset(11F, 1F, 0F));

        return LayerDefinition.create(mesh, 256, 256);
    }
}
