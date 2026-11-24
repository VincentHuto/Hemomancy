package com.vincenthuto.hemomancy.client.model.entity.summon;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Generated from CinderBellowsPuppet.bbmodel by tools/circus/build_cinder_bellows.py. */
final class CinderBellowsMesh {
    private CinderBellowsMesh() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root",
                CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition coil_01 = root.addOrReplaceChild("coil_01",
                CubeListBuilder.create()
                        .texOffs(50, 0).addBox(-4F, -6F, -2F, 8F, 6F, 5F)
                        .texOffs(69, 59).addBox(-3F, -7F, -1F, 6F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 2F, 0F, 1.25663706F, 0F));
        PartDefinition coil_02 = coil_01.addOrReplaceChild("coil_02",
                CubeListBuilder.create()
                        .texOffs(50, 12).addBox(-4F, -5F, -2F, 8F, 5F, 5F)
                        .texOffs(88, 59).addBox(-3F, -6F, -1F, 6F, 1F, 3F)
                        .texOffs(69, 54).addBox(-4.5F, -3F, -1F, 1F, 2F, 2F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.471238898F, 0F));
        PartDefinition coil_03 = coil_02.addOrReplaceChild("coil_03",
                CubeListBuilder.create()
                        .texOffs(103, 0).addBox(-3.5F, -5F, -2F, 7F, 5F, 5F)
                        .texOffs(107, 59).addBox(-2.5F, -6F, -1F, 5F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.593411946F, 0F));
        PartDefinition coil_04 = coil_03.addOrReplaceChild("coil_04",
                CubeListBuilder.create()
                        .texOffs(103, 11).addBox(-3.5F, -4F, -2F, 7F, 4F, 5F)
                        .texOffs(15, 60).addBox(-2.5F, -5F, -1F, 5F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.645771823F, 0F));
        PartDefinition coil_05 = coil_04.addOrReplaceChild("coil_05",
                CubeListBuilder.create()
                        .texOffs(77, 12).addBox(-3F, -4F, -2F, 6F, 4F, 5F)
                        .texOffs(32, 60).addBox(-2F, -5F, -1F, 4F, 1F, 3F)
                        .texOffs(76, 54).addBox(-3.5F, -3F, -1F, 1F, 2F, 2F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.680678408F, 0F));
        PartDefinition coil_06 = coil_05.addOrReplaceChild("coil_06",
                CubeListBuilder.create()
                        .texOffs(29, 13).addBox(-2.5F, -4F, -2F, 5F, 4F, 5F)
                        .texOffs(47, 60).addBox(-1.5F, -5F, -1F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.645771823F, 0F));
        PartDefinition coil_07 = coil_06.addOrReplaceChild("coil_07",
                CubeListBuilder.create()
                        .texOffs(100, 21).addBox(-2.5F, -3F, -2F, 5F, 3F, 5F)
                        .texOffs(0, 61).addBox(-1.5F, -4F, -1F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.541052068F, 0F));
        PartDefinition coil_08 = coil_07.addOrReplaceChild("coil_08",
                CubeListBuilder.create()
                        .texOffs(77, 22).addBox(-2F, -3F, -2F, 4F, 3F, 5F)
                        .texOffs(15, 55).addBox(-1F, -4F, -1F, 2F, 1F, 3F)
                        .texOffs(60, 60).addBox(-2.5F, -3F, -1F, 1F, 2F, 2F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.41887902F, 0F));
        PartDefinition coil_09 = coil_08.addOrReplaceChild("coil_09",
                CubeListBuilder.create()
                        .texOffs(28, 23).addBox(-2F, -3F, -2F, 4F, 3F, 5F)
                        .texOffs(67, 64).addBox(-1F, -4F, -1F, 2F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.34906585F, 0F));
        PartDefinition coil_10 = coil_09.addOrReplaceChild("coil_10",
                CubeListBuilder.create()
                        .texOffs(77, 31).addBox(-1.5F, -2F, -2F, 3F, 2F, 5F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.27925268F, 0F));
        PartDefinition coil_11 = coil_10.addOrReplaceChild("coil_11",
                CubeListBuilder.create()
                        .texOffs(45, 40).addBox(-1F, -2F, -2F, 2F, 2F, 4F), PartPose.offsetAndRotation(0F, 0F, 4F, 0F, 0.20943951F, 0F));
        PartDefinition raised_tail_tip = coil_11.addOrReplaceChild("raised_tail_tip",
                CubeListBuilder.create()
                        .texOffs(0, 43).addBox(-0.5F, -0.5F, -1F, 1F, 1F, 4F), PartPose.offsetAndRotation(0F, -1F, 4F, 0.383972435F, 0.174532925F, 0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -5F, -4F, 7F, 8F, 7F)
                        .texOffs(47, 23).addBox(-2.5F, -5F, -5F, 5F, 7F, 1F), PartPose.offsetAndRotation(0F, -4F, 2F, 0.191986218F, 0F, 0F));
        PartDefinition abdomen = pelvis.addOrReplaceChild("abdomen",
                CubeListBuilder.create()
                        .texOffs(29, 0).addBox(-2.5F, -6F, -3F, 5F, 7F, 5F)
                        .texOffs(18, 26).addBox(-1.5F, -6F, -4F, 3F, 6F, 1F)
                        .texOffs(121, 21).addBox(-0.5F, -6F, 2F, 1F, 6F, 1F), PartPose.offsetAndRotation(0F, -4F, -1F, -0.331612558F, 0F, 0F));
        PartDefinition chest = abdomen.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(77, 0).addBox(-3.5F, -8F, 0F, 7F, 8F, 3F)
                        .texOffs(27, 32).addBox(-5F, -9F, -2F, 10F, 2F, 5F)
                        .texOffs(60, 23).addBox(-3F, -7F, -2F, 6F, 6F, 2F)
                        .texOffs(0, 16).addBox(-0.5F, -8F, -4F, 1F, 8F, 1F), PartPose.offsetAndRotation(0F, -5F, -1F, 0.20943951F, 0F, 0F));
        PartDefinition right_rib_1 = chest.addOrReplaceChild("right_rib_1",
                CubeListBuilder.create()
                        .texOffs(83, 47).addBox(-1.25F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(-3F, -1F, -1F, 0F, -0.331612558F, 0.174532925F));
        PartDefinition right_rib_2 = chest.addOrReplaceChild("right_rib_2",
                CubeListBuilder.create()
                        .texOffs(96, 47).addBox(-1.25F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(-3F, -2.75F, -1F, 0F, -0.331612558F, 0.174532925F));
        PartDefinition right_rib_3 = chest.addOrReplaceChild("right_rib_3",
                CubeListBuilder.create()
                        .texOffs(109, 47).addBox(-1.25F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(-3F, -4.5F, -1F, 0F, -0.331612558F, 0.174532925F));
        PartDefinition right_rib_4 = chest.addOrReplaceChild("right_rib_4",
                CubeListBuilder.create()
                        .texOffs(26, 48).addBox(-1.25F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(-3F, -6.25F, -1F, 0F, -0.331612558F, 0.174532925F));
        PartDefinition left_rib_1 = chest.addOrReplaceChild("left_rib_1",
                CubeListBuilder.create()
                        .texOffs(13, 49).addBox(-0.75F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(3F, -1F, -1F, 0F, 0.331612558F, -0.174532925F));
        PartDefinition left_rib_2 = chest.addOrReplaceChild("left_rib_2",
                CubeListBuilder.create()
                        .texOffs(83, 53).addBox(-0.75F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(3F, -2.75F, -1F, 0F, 0.331612558F, -0.174532925F));
        PartDefinition left_rib_3 = chest.addOrReplaceChild("left_rib_3",
                CubeListBuilder.create()
                        .texOffs(96, 53).addBox(-0.75F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(3F, -4.5F, -1F, 0F, 0.331612558F, -0.174532925F));
        PartDefinition left_rib_4 = chest.addOrReplaceChild("left_rib_4",
                CubeListBuilder.create()
                        .texOffs(109, 53).addBox(-0.75F, -0.75F, -1.75F, 2F, 1F, 4F), PartPose.offsetAndRotation(3F, -6.25F, -1F, 0F, 0.331612558F, -0.174532925F));
        PartDefinition right_bellows_lung = chest.addOrReplaceChild("right_bellows_lung",
                CubeListBuilder.create()
                        .texOffs(0, 26).addBox(-0.75F, -3F, -1.75F, 2F, 6F, 2F)
                        .texOffs(13, 65).addBox(-1.5F, 2F, -2F, 3F, 1F, 2F)
                        .texOffs(24, 65).addBox(-1.5F, 0.5F, -2.5F, 3F, 1F, 2F)
                        .texOffs(35, 65).addBox(-1.5F, -1F, -2.5F, 3F, 1F, 2F)
                        .texOffs(46, 65).addBox(-1.5F, -2.5F, -2F, 3F, 1F, 2F), PartPose.offset(-2F, -4F, -2F));
        PartDefinition right_upper_arm = chest.addOrReplaceChild("right_upper_arm",
                CubeListBuilder.create()
                        .texOffs(58, 40).addBox(-2F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(71, 44).addBox(-1F, -2.5F, -0.5F, 1F, 1F, 1F)
                        .texOffs(119, 30).addBox(-1.5F, 1F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(-5F, -7F, 0F, 0.122173048F, 0F, 0.331612558F));
        PartDefinition right_forearm = right_upper_arm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create()
                        .texOffs(0, 49).addBox(-1.5F, -1F, -1.5F, 3F, 2F, 3F)
                        .texOffs(58, 32).addBox(-1F, -0F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(-0.5F, 5.5F, 0F, -0.942477796F, 0F, -0.244346095F));
        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand",
                CubeListBuilder.create()
                        .texOffs(119, 64).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
                        .texOffs(78, 64).addBox(-1F, 0.5F, -1.5F, 2F, 2F, 2F)
                        .texOffs(57, 65).addBox(0F, 2F, -2.5F, 1F, 1F, 2F)
                        .texOffs(0, 66).addBox(-1F, 2F, -2.5F, 1F, 1F, 2F), PartPose.offsetAndRotation(0F, 5F, 0F, 0.261799388F, 0F, 0F));
        PartDefinition right_hand_detail_2 = right_hand.addOrReplaceChild("right_hand_detail_2",
                CubeListBuilder.create()
                        .texOffs(71, 40).addBox(0.5F, 0.5F, -2F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, -0F, 0F, 0F, 0F, 0.314159265F));
        PartDefinition wick_torch = right_hand.addOrReplaceChild("wick_torch",
                CubeListBuilder.create()
                        .texOffs(98, 0).addBox(-0.5F, -6F, -1F, 1F, 10F, 1F)
                        .texOffs(87, 64).addBox(-1F, -7F, -1.5F, 2F, 2F, 2F)
                        .texOffs(122, 43).addBox(-0.5F, -9F, -1F, 1F, 2F, 1F)
                        .texOffs(119, 68).addBox(-1F, -8F, -1.5F, 2F, 1F, 2F), PartPose.offsetAndRotation(0F, 1.5F, -2F, -0.226892803F, 0F, -0.436332313F));
        PartDefinition left_bellows_lung = chest.addOrReplaceChild("left_bellows_lung",
                CubeListBuilder.create()
                        .texOffs(9, 26).addBox(-1.25F, -3F, -1.75F, 2F, 6F, 2F)
                        .texOffs(7, 69).addBox(-1.5F, 2F, -2F, 3F, 1F, 2F)
                        .texOffs(18, 69).addBox(-1.5F, 0.5F, -2.5F, 3F, 1F, 2F)
                        .texOffs(29, 69).addBox(-1.5F, -1F, -2.5F, 3F, 1F, 2F)
                        .texOffs(40, 69).addBox(-1.5F, -2.5F, -2F, 3F, 1F, 2F), PartPose.offset(2F, -4F, -2F));
        PartDefinition left_upper_arm = chest.addOrReplaceChild("left_upper_arm",
                CubeListBuilder.create()
                        .texOffs(13, 42).addBox(-1F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(122, 55).addBox(0F, -2.5F, -0.5F, 1F, 1F, 1F)
                        .texOffs(67, 32).addBox(-0.5F, 1F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(5F, -7F, 0F, 0.122173048F, 0F, -0.331612558F));
        PartDefinition left_forearm = left_upper_arm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create()
                        .texOffs(26, 54).addBox(-1.5F, -1F, -1.5F, 3F, 2F, 3F)
                        .texOffs(18, 34).addBox(-1F, -0F, -1F, 2F, 5F, 2F), PartPose.offsetAndRotation(0.5F, 5.5F, 0F, -0.488692191F, 0F, 0.244346095F));
        PartDefinition left_hand = left_forearm.addOrReplaceChild("left_hand",
                CubeListBuilder.create()
                        .texOffs(51, 69).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
                        .texOffs(96, 64).addBox(-1F, 0.5F, -1.5F, 2F, 2F, 2F)
                        .texOffs(60, 69).addBox(-1F, 2F, -2.5F, 1F, 1F, 2F)
                        .texOffs(67, 69).addBox(0F, 2F, -2.5F, 1F, 1F, 2F), PartPose.offsetAndRotation(0F, 5F, 0F, 0.261799388F, 0F, 0F));
        PartDefinition left_hand_detail_2 = left_hand.addOrReplaceChild("left_hand_detail_2",
                CubeListBuilder.create()
                        .texOffs(122, 47).addBox(-1.5F, 0.5F, -2F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, -0F, 0F, 0F, 0F, -0.314159265F));
        PartDefinition neck = chest.addOrReplaceChild("neck",
                CubeListBuilder.create()
                        .texOffs(0, 35).addBox(-1.5F, -4F, -1.5F, 3F, 4F, 3F)
                        .texOffs(76, 39).addBox(-3.5F, -1F, -3F, 7F, 1F, 6F), PartPose.offsetAndRotation(0F, -8F, 0F, 0.0872664626F, 0F, 0F));
        PartDefinition neck_detail_2 = neck.addOrReplaceChild("neck_detail_2",
                CubeListBuilder.create()
                        .texOffs(39, 54).addBox(-4.5F, -1F, -1.5F, 3F, 1F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.27925268F, 0.296705973F));
        PartDefinition neck_detail_3 = neck.addOrReplaceChild("neck_detail_3",
                CubeListBuilder.create()
                        .texOffs(54, 54).addBox(1.5F, -1F, -1.5F, 3F, 1F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.27925268F, -0.296705973F));
        PartDefinition head = neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(5, 16).addBox(-3F, -3.5F, -2.5F, 6F, 4F, 5F)
                        .texOffs(45, 47).addBox(-3F, -4.5F, -2.5F, 6F, 2F, 4F)
                        .texOffs(103, 39).addBox(-2F, -1.5F, -6.5F, 4F, 2F, 5F)
                        .texOffs(0, 55).addBox(-1.5F, 0.25F, -6F, 3F, 1F, 4F)
                        .texOffs(74, 69).addBox(-0.5F, -5.5F, 0F, 1F, 1F, 2F)
                        .texOffs(81, 69).addBox(-3F, -1.5F, -3F, 2F, 1F, 2F)
                        .texOffs(39, 48).addBox(-2F, 0.5F, -5.5F, 1F, 2F, 1F)
                        .texOffs(108, 69).addBox(1F, -1.5F, -3F, 2F, 1F, 2F)
                        .texOffs(122, 51).addBox(1F, 0.5F, -5.5F, 1F, 2F, 1F)
                        .texOffs(7, 66).addBox(-0.5F, -6.5F, 0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, -3F, -0.5F, -0.0523598776F, -0.157079633F, 0F));
        PartDefinition head_detail_6 = head.addOrReplaceChild("head_detail_6",
                CubeListBuilder.create()
                        .texOffs(90, 69).addBox(-3.5F, -2.5F, -3.25F, 3F, 1F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.20943951F));
        PartDefinition head_detail_9 = head.addOrReplaceChild("head_detail_9",
                CubeListBuilder.create()
                        .texOffs(117, 72).addBox(0.5F, -2.5F, -3.25F, 3F, 1F, 2F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.20943951F));
        PartDefinition right_folded_hood = head.addOrReplaceChild("right_folded_hood",
                CubeListBuilder.create()
                        .texOffs(27, 40).addBox(-2F, -4F, -0.75F, 2F, 5F, 2F)
                        .texOffs(13, 35).addBox(-3F, -3F, -0.25F, 1F, 3F, 1F)
                        .texOffs(101, 69).addBox(-2F, 0F, 0F, 2F, 2F, 1F), PartPose.offsetAndRotation(-2.5F, 0F, 1F, 0F, -0.296705973F, -0.13962634F));
        PartDefinition left_folded_hood = head.addOrReplaceChild("left_folded_hood",
                CubeListBuilder.create()
                        .texOffs(36, 40).addBox(0F, -4F, -0.75F, 2F, 5F, 2F)
                        .texOffs(122, 38).addBox(2F, -3F, -0.25F, 1F, 3F, 1F)
                        .texOffs(0, 70).addBox(0F, 0F, 0F, 2F, 2F, 1F), PartPose.offsetAndRotation(2.5F, 0F, 1F, 0F, 0.296705973F, 0.13962634F));
        PartDefinition hinged_jaw = head.addOrReplaceChild("hinged_jaw",
                CubeListBuilder.create()
                        .texOffs(96, 30).addBox(-2F, 1F, -6.5F, 4F, 1F, 7F)
                        .texOffs(66, 47).addBox(-1.5F, 0F, -5.5F, 3F, 1F, 5F)
                        .texOffs(105, 64).addBox(-3F, -1F, -1.5F, 1F, 2F, 2F)
                        .texOffs(112, 64).addBox(2F, -1F, -1.5F, 1F, 2F, 2F), PartPose.offsetAndRotation(0F, 1F, 0.5F, -0.20943951F, 0F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
