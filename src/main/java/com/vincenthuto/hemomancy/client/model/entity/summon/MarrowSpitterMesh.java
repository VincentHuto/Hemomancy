package com.vincenthuto.hemomancy.client.model.entity.summon;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Generated from MarrowJuggler.bbmodel by tools/circus/build_marrow_juggler.py. */
final class MarrowSpitterMesh {
    private MarrowSpitterMesh() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root",
                CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create(), PartPose.offset(0F, -18F, 1F));
        PartDefinition chest = body.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(19, 1).addBox(-4F, -3F, -3F, 8F, 8F, 5F)
                        .texOffs(47, 1).addBox(-2F, -4F, 2F, 4F, 9F, 1F)
                        .texOffs(59, 1).addBox(-7F, -5F, -1F, 14F, 3F, 4F)
                        .texOffs(97, 1).addBox(-5F, -6F, -1F, 10F, 1F, 3F)
                        .texOffs(157, 1).addBox(-1F, 5F, -5F, 2F, 2F, 1F)
                        .texOffs(165, 1).addBox(-2F, -2F, 4F, 4F, 5F, 1F), PartPose.offset(0F, -4F, 0F));
        PartDefinition chest_detail_0 = chest.addOrReplaceChild("chest_detail_0",
                CubeListBuilder.create()
                        .texOffs(1, 1).addBox(-2F, -5F, 0F, 4F, 16F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.13962634F, 0F, 0F));
        PartDefinition chest_detail_5 = chest.addOrReplaceChild("chest_detail_5",
                CubeListBuilder.create()
                        .texOffs(125, 1).addBox(-4F, -1F, -3F, 8F, 2F, 7F), PartPose.offsetAndRotation(0F, 6F, -1F, 0F, 0F, -0.104719755F));
        PartDefinition chest_detail_8 = chest.addOrReplaceChild("chest_detail_8",
                CubeListBuilder.create()
                        .texOffs(177, 1).addBox(-1F, -4F, 5F, 2F, 13F, 1F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.13962634F, 0F, 0F));
        PartDefinition hood = body.addOrReplaceChild("hood",
                CubeListBuilder.create()
                        .texOffs(1, 23).addBox(-4F, -2F, -4F, 8F, 6F, 5F)
                        .texOffs(93, 23).addBox(-1F, -6F, -1F, 2F, 1F, 2F)
                        .texOffs(103, 23).addBox(-1F, -7F, -1F, 2F, 1F, 2F), PartPose.offset(0F, -10F, 0F));
        PartDefinition hood_detail_0 = hood.addOrReplaceChild("hood_detail_0",
                CubeListBuilder.create()
                        .texOffs(185, 1).addBox(-5F, -4F, -3F, 10F, 4F, 6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.20943951F, 0F, 0.0698131701F));
        PartDefinition hood_detail_1 = hood.addOrReplaceChild("hood_detail_1",
                CubeListBuilder.create()
                        .texOffs(219, 1).addBox(-3F, -6F, -2F, 6F, 2F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.296705973F, 0F, 0.174532925F));
        PartDefinition hood_detail_3 = hood.addOrReplaceChild("hood_detail_3",
                CubeListBuilder.create()
                        .texOffs(29, 23).addBox(-2F, -3F, -2F, 3F, 7F, 6F), PartPose.offsetAndRotation(-4F, 1F, -1F, 0.226892803F, -0.296705973F, 0.331612558F));
        PartDefinition hood_detail_4 = hood.addOrReplaceChild("hood_detail_4",
                CubeListBuilder.create()
                        .texOffs(49, 23).addBox(-1F, -3F, -2F, 3F, 7F, 6F), PartPose.offsetAndRotation(4F, 0F, -1F, 0.157079633F, 0.314159265F, -0.401425728F));
        PartDefinition hood_detail_5 = hood.addOrReplaceChild("hood_detail_5",
                CubeListBuilder.create()
                        .texOffs(69, 23).addBox(-4F, -3F, -1F, 8F, 9F, 3F), PartPose.offsetAndRotation(0F, 1F, 3F, -0.244346095F, 0F, -0.104719755F));
        PartDefinition hood_detail_8 = hood.addOrReplaceChild("hood_detail_8",
                CubeListBuilder.create()
                        .texOffs(113, 23).addBox(0F, -10F, -1F, 1F, 3F, 1F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.157079633F, 0F, 0.157079633F));
        PartDefinition mask_comedy = hood.addOrReplaceChild("mask_comedy",
                CubeListBuilder.create()
                        .texOffs(119, 23).addBox(-3F, -2F, 0F, 6F, 1F, 4F)
                        .texOffs(141, 23).addBox(-2.5F, -4F, -2F, 5F, 7F, 2F)
                        .texOffs(157, 23).addBox(-1.5F, -5F, -1F, 3F, 1F, 1F)
                        .texOffs(167, 23).addBox(-1.5F, 3F, -1F, 3F, 1F, 1F)
                        .texOffs(177, 23).addBox(2.5F, -2F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(-3F, -1F, -4F, -0.034906585F, 0.244346095F, 0.20943951F));
        PartDefinition mask_tragedy = hood.addOrReplaceChild("mask_tragedy",
                CubeListBuilder.create()
                        .texOffs(183, 23).addBox(-3F, -2F, 0F, 6F, 1F, 4F)
                        .texOffs(205, 23).addBox(-2.5F, -4F, -2F, 5F, 7F, 2F)
                        .texOffs(221, 23).addBox(-1.5F, -5F, -1F, 3F, 1F, 1F)
                        .texOffs(231, 23).addBox(-1.5F, 3F, -1F, 3F, 1F, 1F)
                        .texOffs(241, 23).addBox(2.5F, -2F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(3F, 1F, -5F, -0.034906585F, -0.27925268F, -0.20943951F));
        PartDefinition skirt = body.addOrReplaceChild("skirt",
                CubeListBuilder.create(), PartPose.offset(0F, 2F, 0F));
        PartDefinition front_left = skirt.addOrReplaceChild("front_left",
                CubeListBuilder.create()
                        .texOffs(1, 38).addBox(-2F, -1F, -1F, 4F, 12F, 1F)
                        .texOffs(13, 38).addBox(-1F, 11F, -1F, 1F, 2F, 1F)
                        .texOffs(19, 38).addBox(0F, 11F, -1F, 1F, 1F, 1F), PartPose.offsetAndRotation(-2F, 1F, -3F, 0.157079633F, -0.122173048F, 0.20943951F));
        PartDefinition front_right = skirt.addOrReplaceChild("front_right",
                CubeListBuilder.create()
                        .texOffs(25, 38).addBox(-2F, -1F, -1F, 4F, 10F, 1F)
                        .texOffs(37, 38).addBox(-1F, 9F, -1F, 1F, 2F, 1F)
                        .texOffs(43, 38).addBox(0F, 9F, -1F, 1F, 1F, 1F), PartPose.offsetAndRotation(2F, 1F, -3F, 0.226892803F, 0.174532925F, -0.296705973F));
        PartDefinition side_left = skirt.addOrReplaceChild("side_left",
                CubeListBuilder.create()
                        .texOffs(49, 38).addBox(-2F, 0F, -1F, 4F, 9F, 2F)
                        .texOffs(63, 38).addBox(-1F, 9F, -1F, 1F, 2F, 1F)
                        .texOffs(69, 38).addBox(0F, 9F, -1F, 1F, 1F, 1F), PartPose.offsetAndRotation(-4F, 1F, -2F, -0.034906585F, -0.401425728F, 0.34906585F));
        PartDefinition side_right = skirt.addOrReplaceChild("side_right",
                CubeListBuilder.create()
                        .texOffs(75, 38).addBox(-2F, 0F, -1F, 4F, 10F, 2F)
                        .texOffs(89, 38).addBox(-1F, 10F, -1F, 1F, 2F, 1F)
                        .texOffs(95, 38).addBox(0F, 10F, -1F, 1F, 1F, 1F), PartPose.offsetAndRotation(4F, 1F, -1F, -0.157079633F, 0.436332313F, -0.331612558F));
        PartDefinition rear_left = skirt.addOrReplaceChild("rear_left",
                CubeListBuilder.create()
                        .texOffs(101, 38).addBox(-2F, -1F, -1F, 4F, 15F, 2F)
                        .texOffs(115, 38).addBox(-1F, 14F, -1F, 1F, 2F, 1F)
                        .texOffs(121, 38).addBox(0F, 14F, -1F, 1F, 1F, 1F), PartPose.offsetAndRotation(-2F, 1F, 3F, -0.453785606F, -0.13962634F, 0.174532925F));
        PartDefinition rear_right = skirt.addOrReplaceChild("rear_right",
                CubeListBuilder.create()
                        .texOffs(127, 38).addBox(-2F, -1F, -1F, 4F, 13F, 2F)
                        .texOffs(141, 38).addBox(-1F, 12F, -1F, 1F, 2F, 1F)
                        .texOffs(147, 38).addBox(0F, 12F, -1F, 1F, 1F, 1F), PartPose.offsetAndRotation(2F, 1F, 3F, -0.383972435F, 0.174532925F, -0.174532925F));
        PartDefinition arms = body.addOrReplaceChild("arms",
                CubeListBuilder.create(), PartPose.offset(0F, -5F, 0F));
        PartDefinition mount_0 = arms.addOrReplaceChild("mount_0",
                CubeListBuilder.create()
                        .texOffs(153, 38).addBox(0F, -1F, 0F, 5F, 2F, 2F), PartPose.offset(-8F, -2F, 2F));
        PartDefinition arm_0 = mount_0.addOrReplaceChild("arm_0",
                CubeListBuilder.create()
                        .texOffs(169, 38).addBox(-1.5F, -1F, -1.5F, 3F, 3F, 3F)
                        .texOffs(183, 38).addBox(-1F, 0F, -2.5F, 2F, 2F, 1F)
                        .texOffs(191, 38).addBox(-1F, 1F, -1F, 2F, 4F, 2F)
                        .texOffs(201, 38).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.34906585F, -0.157079633F, 1.83259571F));
        PartDefinition forearm_0 = arm_0.addOrReplaceChild("forearm_0",
                CubeListBuilder.create()
                        .texOffs(215, 38).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(229, 38).addBox(-1F, 0F, -0.5F, 1F, 6F, 1F)
                        .texOffs(241, 38).addBox(-1.5F, 4F, -1.5F, 3F, 2F, 3F), PartPose.offsetAndRotation(0F, 5F, 0F, -0.314159265F, 0.0523598776F, 1.01229097F));
        PartDefinition forearm_0_detail_2 = forearm_0.addOrReplaceChild("forearm_0_detail_2",
                CubeListBuilder.create()
                        .texOffs(235, 38).addBox(0F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0F, 0.157079633F));
        PartDefinition hand_0 = forearm_0.addOrReplaceChild("hand_0",
                CubeListBuilder.create()
                        .texOffs(1, 57).addBox(-1.5F, 0F, -1F, 3F, 2F, 2F)
                        .texOffs(37, 57).addBox(-0.5F, -1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, -0.453785606F, -0.0872664626F));
        PartDefinition hand_0_detail_1 = hand_0.addOrReplaceChild("hand_0_detail_1",
                CubeListBuilder.create()
                        .texOffs(31, 57).addBox(-1F, -0.25F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 1F, 0F, -0.383972435F, 0F, 0.558505361F));
        PartDefinition index_0 = hand_0.addOrReplaceChild("index_0",
                CubeListBuilder.create()
                        .texOffs(13, 57).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 2F, 0F, -0.122173048F, 0F, -0.174532925F));
        PartDefinition middle_0 = hand_0.addOrReplaceChild("middle_0",
                CubeListBuilder.create()
                        .texOffs(19, 57).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, 2F, 0F, -0.20943951F, 0F, 0F));
        PartDefinition ring_0 = hand_0.addOrReplaceChild("ring_0",
                CubeListBuilder.create()
                        .texOffs(25, 57).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 2F, 0F, -0.296705973F, 0F, 0.174532925F));
        PartDefinition mount_1 = arms.addOrReplaceChild("mount_1",
                CubeListBuilder.create()
                        .texOffs(43, 57).addBox(-5F, -1F, 0F, 5F, 2F, 2F), PartPose.offset(8F, -2F, 2F));
        PartDefinition arm_1 = mount_1.addOrReplaceChild("arm_1",
                CubeListBuilder.create()
                        .texOffs(59, 57).addBox(-1.5F, -1F, -1.5F, 3F, 3F, 3F)
                        .texOffs(73, 57).addBox(-1F, 0F, -2.5F, 2F, 2F, 1F)
                        .texOffs(81, 57).addBox(-1F, 1F, -1F, 2F, 4F, 2F)
                        .texOffs(91, 57).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0.20943951F, 0.13962634F, -1.88495559F));
        PartDefinition forearm_1 = arm_1.addOrReplaceChild("forearm_1",
                CubeListBuilder.create()
                        .texOffs(105, 57).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(119, 57).addBox(-1F, 0F, -0.5F, 1F, 6F, 1F)
                        .texOffs(131, 57).addBox(-1.5F, 4F, -1.5F, 3F, 2F, 3F), PartPose.offsetAndRotation(0F, 5F, 0F, -0.34906585F, -0.122173048F, -0.959931089F));
        PartDefinition forearm_1_detail_2 = forearm_1.addOrReplaceChild("forearm_1_detail_2",
                CubeListBuilder.create()
                        .texOffs(125, 57).addBox(0F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0F, -0.157079633F));
        PartDefinition hand_1 = forearm_1.addOrReplaceChild("hand_1",
                CubeListBuilder.create()
                        .texOffs(145, 57).addBox(-1.5F, 0F, -1F, 3F, 2F, 2F)
                        .texOffs(181, 57).addBox(-0.5F, -1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0.436332313F, 0.20943951F));
        PartDefinition hand_1_detail_1 = hand_1.addOrReplaceChild("hand_1_detail_1",
                CubeListBuilder.create()
                        .texOffs(175, 57).addBox(0F, -0.25F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 1F, 0F, -0.383972435F, 0F, -0.558505361F));
        PartDefinition index_1 = hand_1.addOrReplaceChild("index_1",
                CubeListBuilder.create()
                        .texOffs(157, 57).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 2F, 0F, -0.122173048F, 0F, -0.174532925F));
        PartDefinition middle_1 = hand_1.addOrReplaceChild("middle_1",
                CubeListBuilder.create()
                        .texOffs(163, 57).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, 2F, 0F, -0.20943951F, 0F, 0F));
        PartDefinition ring_1 = hand_1.addOrReplaceChild("ring_1",
                CubeListBuilder.create()
                        .texOffs(169, 57).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 2F, 0F, -0.296705973F, 0F, 0.174532925F));
        PartDefinition mount_2 = arms.addOrReplaceChild("mount_2",
                CubeListBuilder.create()
                        .texOffs(187, 57).addBox(0F, -1F, 0F, 4F, 2F, 2F), PartPose.offset(-7F, 1F, -1F));
        PartDefinition arm_2 = mount_2.addOrReplaceChild("arm_2",
                CubeListBuilder.create()
                        .texOffs(201, 57).addBox(-1.5F, -1F, -1.5F, 3F, 3F, 3F)
                        .texOffs(215, 57).addBox(-1F, 0F, -2.5F, 2F, 2F, 1F)
                        .texOffs(223, 57).addBox(-1F, 1F, -1F, 2F, 5F, 2F)
                        .texOffs(233, 57).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.314159265F, 0F, 1.11701072F));
        PartDefinition forearm_2 = arm_2.addOrReplaceChild("forearm_2",
                CubeListBuilder.create()
                        .texOffs(1, 66).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(15, 66).addBox(-1F, 0F, -0.5F, 1F, 6F, 1F)
                        .texOffs(27, 66).addBox(-1.5F, 4F, -1.5F, 3F, 2F, 3F), PartPose.offsetAndRotation(0F, 6F, 0F, -0.261799388F, 0.157079633F, -0.959931089F));
        PartDefinition forearm_2_detail_2 = forearm_2.addOrReplaceChild("forearm_2_detail_2",
                CubeListBuilder.create()
                        .texOffs(21, 66).addBox(0F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0F, 0.157079633F));
        PartDefinition hand_2 = forearm_2.addOrReplaceChild("hand_2",
                CubeListBuilder.create()
                        .texOffs(41, 66).addBox(-1.5F, 0F, -1F, 3F, 2F, 2F)
                        .texOffs(77, 66).addBox(-0.5F, -1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 6F, 0F, 0.244346095F, -0.261799388F, 0.436332313F));
        PartDefinition hand_2_detail_1 = hand_2.addOrReplaceChild("hand_2_detail_1",
                CubeListBuilder.create()
                        .texOffs(71, 66).addBox(-1F, -0.25F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 1F, 0F, -0.383972435F, 0F, 0.558505361F));
        PartDefinition index_2 = hand_2.addOrReplaceChild("index_2",
                CubeListBuilder.create()
                        .texOffs(53, 66).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 2F, 0F, -0.122173048F, 0F, -0.174532925F));
        PartDefinition middle_2 = hand_2.addOrReplaceChild("middle_2",
                CubeListBuilder.create()
                        .texOffs(59, 66).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, 2F, 0F, -0.20943951F, 0F, 0F));
        PartDefinition ring_2 = hand_2.addOrReplaceChild("ring_2",
                CubeListBuilder.create()
                        .texOffs(65, 66).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 2F, 0F, -0.296705973F, 0F, 0.174532925F));
        PartDefinition mount_3 = arms.addOrReplaceChild("mount_3",
                CubeListBuilder.create()
                        .texOffs(83, 66).addBox(-4F, -1F, 0F, 4F, 2F, 2F), PartPose.offset(7F, 1F, -1F));
        PartDefinition arm_3 = mount_3.addOrReplaceChild("arm_3",
                CubeListBuilder.create()
                        .texOffs(97, 66).addBox(-1.5F, -1F, -1.5F, 3F, 3F, 3F)
                        .texOffs(111, 66).addBox(-1F, 0F, -2.5F, 2F, 2F, 1F)
                        .texOffs(119, 66).addBox(-1F, 1F, -1F, 2F, 5F, 2F)
                        .texOffs(129, 66).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.226892803F, 0F, -1.3962634F));
        PartDefinition forearm_3 = arm_3.addOrReplaceChild("forearm_3",
                CubeListBuilder.create()
                        .texOffs(143, 66).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(157, 66).addBox(-1F, 0F, -0.5F, 1F, 6F, 1F)
                        .texOffs(169, 66).addBox(-1.5F, 4F, -1.5F, 3F, 2F, 3F), PartPose.offsetAndRotation(0F, 6F, 0F, -0.314159265F, -0.314159265F, 0.767944871F));
        PartDefinition forearm_3_detail_2 = forearm_3.addOrReplaceChild("forearm_3_detail_2",
                CubeListBuilder.create()
                        .texOffs(163, 66).addBox(0F, -4F, -0.5F, 1F, 4F, 1F), PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0F, -0.157079633F));
        PartDefinition hand_3 = forearm_3.addOrReplaceChild("hand_3",
                CubeListBuilder.create()
                        .texOffs(183, 66).addBox(-1.5F, 0F, -1F, 3F, 2F, 2F)
                        .texOffs(237, 66).addBox(-0.5F, -1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 6F, 0F, 0.244346095F, 0F, -0.174532925F));
        PartDefinition hand_3_detail_1 = hand_3.addOrReplaceChild("hand_3_detail_1",
                CubeListBuilder.create()
                        .texOffs(231, 66).addBox(0F, -0.25F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 1F, 0F, -0.383972435F, 0F, -0.558505361F));
        PartDefinition index_3 = hand_3.addOrReplaceChild("index_3",
                CubeListBuilder.create()
                        .texOffs(195, 66).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F)
                        .texOffs(201, 66).addBox(-0.5F, 1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(-1F, 2F, 0F, -0.785398163F, 0F, 0F));
        PartDefinition middle_3 = hand_3.addOrReplaceChild("middle_3",
                CubeListBuilder.create()
                        .texOffs(207, 66).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F)
                        .texOffs(213, 66).addBox(-0.5F, 1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 2F, 0F, -0.785398163F, 0F, 0F));
        PartDefinition ring_3 = hand_3.addOrReplaceChild("ring_3",
                CubeListBuilder.create()
                        .texOffs(219, 66).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F)
                        .texOffs(225, 66).addBox(-0.5F, 1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(1F, 2F, 0F, -0.785398163F, 0F, 0F));
        PartDefinition mount_4 = arms.addOrReplaceChild("mount_4",
                CubeListBuilder.create()
                        .texOffs(25, 75).addBox(0F, -1F, 0F, 3F, 2F, 2F), PartPose.offset(-6F, 4F, 1F));
        PartDefinition arm_4 = mount_4.addOrReplaceChild("arm_4",
                CubeListBuilder.create()
                        .texOffs(37, 75).addBox(-1.5F, -1F, -1.5F, 3F, 3F, 3F)
                        .texOffs(51, 75).addBox(-1F, 0F, -2.5F, 2F, 2F, 1F)
                        .texOffs(59, 75).addBox(-1F, 1F, -1F, 2F, 4F, 2F)
                        .texOffs(69, 75).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.314159265F, -0.0698131701F, 0.610865238F));
        PartDefinition forearm_4 = arm_4.addOrReplaceChild("forearm_4",
                CubeListBuilder.create()
                        .texOffs(83, 75).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(97, 75).addBox(-1F, 0F, -0.5F, 1F, 5F, 1F)
                        .texOffs(109, 75).addBox(-1.5F, 3F, -1.5F, 3F, 2F, 3F), PartPose.offsetAndRotation(0F, 5F, 0F, 0.488692191F, 0F, 0.157079633F));
        PartDefinition forearm_4_detail_2 = forearm_4.addOrReplaceChild("forearm_4_detail_2",
                CubeListBuilder.create()
                        .texOffs(103, 75).addBox(0F, -3F, -0.5F, 1F, 3F, 1F), PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0.157079633F));
        PartDefinition hand_4 = forearm_4.addOrReplaceChild("hand_4",
                CubeListBuilder.create()
                        .texOffs(123, 75).addBox(-1.5F, 0F, -1F, 3F, 2F, 2F)
                        .texOffs(159, 75).addBox(-0.5F, -1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 5F, 0F, 0.41887902F, -0.261799388F, 0.296705973F));
        PartDefinition hand_4_detail_1 = hand_4.addOrReplaceChild("hand_4_detail_1",
                CubeListBuilder.create()
                        .texOffs(153, 75).addBox(-1F, -0.25F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 1F, 0F, -0.383972435F, 0F, 0.558505361F));
        PartDefinition index_4 = hand_4.addOrReplaceChild("index_4",
                CubeListBuilder.create()
                        .texOffs(135, 75).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 2F, 0F, -0.122173048F, 0F, -0.174532925F));
        PartDefinition middle_4 = hand_4.addOrReplaceChild("middle_4",
                CubeListBuilder.create()
                        .texOffs(141, 75).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, 2F, 0F, -0.20943951F, 0F, 0F));
        PartDefinition ring_4 = hand_4.addOrReplaceChild("ring_4",
                CubeListBuilder.create()
                        .texOffs(147, 75).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 2F, 0F, -0.296705973F, 0F, 0.174532925F));
        PartDefinition mount_5 = arms.addOrReplaceChild("mount_5",
                CubeListBuilder.create()
                        .texOffs(165, 75).addBox(-3F, -1F, 0F, 3F, 2F, 2F), PartPose.offset(6F, 4F, 1F));
        PartDefinition arm_5 = mount_5.addOrReplaceChild("arm_5",
                CubeListBuilder.create()
                        .texOffs(177, 75).addBox(-1.5F, -1F, -1.5F, 3F, 3F, 3F)
                        .texOffs(191, 75).addBox(-1F, 0F, -2.5F, 2F, 2F, 1F)
                        .texOffs(199, 75).addBox(-1F, 1F, -1F, 2F, 5F, 2F)
                        .texOffs(209, 75).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, -0.191986218F, 0.0523598776F, -0.506145483F));
        PartDefinition forearm_5 = arm_5.addOrReplaceChild("forearm_5",
                CubeListBuilder.create()
                        .texOffs(223, 75).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F)
                        .texOffs(237, 75).addBox(-1F, 0F, -0.5F, 1F, 5F, 1F)
                        .texOffs(1, 84).addBox(-1.5F, 3F, -1.5F, 3F, 2F, 3F), PartPose.offsetAndRotation(0F, 6F, 0F, 0.645771823F, -0.20943951F, -0.34906585F));
        PartDefinition forearm_5_detail_2 = forearm_5.addOrReplaceChild("forearm_5_detail_2",
                CubeListBuilder.create()
                        .texOffs(243, 75).addBox(0F, -3F, -0.5F, 1F, 3F, 1F), PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, -0.157079633F));
        PartDefinition hand_5 = forearm_5.addOrReplaceChild("hand_5",
                CubeListBuilder.create()
                        .texOffs(15, 84).addBox(-1.5F, 0F, -1F, 3F, 2F, 2F)
                        .texOffs(51, 84).addBox(-0.5F, -1F, -1.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, 5F, 0F, 0.34906585F, 0.314159265F, -0.436332313F));
        PartDefinition hand_5_detail_1 = hand_5.addOrReplaceChild("hand_5_detail_1",
                CubeListBuilder.create()
                        .texOffs(45, 84).addBox(0F, -0.25F, -1F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 1F, 0F, -0.383972435F, 0F, -0.558505361F));
        PartDefinition index_5 = hand_5.addOrReplaceChild("index_5",
                CubeListBuilder.create()
                        .texOffs(27, 84).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(-1F, 2F, 0F, -0.122173048F, 0F, -0.174532925F));
        PartDefinition middle_5 = hand_5.addOrReplaceChild("middle_5",
                CubeListBuilder.create()
                        .texOffs(33, 84).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, 2F, 0F, -0.20943951F, 0F, 0F));
        PartDefinition ring_5 = hand_5.addOrReplaceChild("ring_5",
                CubeListBuilder.create()
                        .texOffs(39, 84).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F), PartPose.offsetAndRotation(1F, 2F, 0F, -0.296705973F, 0F, 0.174532925F));
        PartDefinition props = root.addOrReplaceChild("props",
                CubeListBuilder.create(), PartPose.offset(0F, -23F, 0F));
        PartDefinition knife_0 = props.addOrReplaceChild("knife_0",
                CubeListBuilder.create()
                        .texOffs(57, 84).addBox(-0.5F, -1F, -0.5F, 1F, 3F, 1F)
                        .texOffs(63, 84).addBox(-0.5F, 2F, -0.5F, 1F, 1F, 1F)
                        .texOffs(69, 84).addBox(-1.5F, -2F, -0.5F, 3F, 1F, 1F)
                        .texOffs(79, 84).addBox(-1F, -6F, -0.5F, 2F, 4F, 1F), PartPose.offsetAndRotation(-11.5F, -14F, -2F, 0.226892803F, 0.34906585F, -1.13446401F));
        PartDefinition knife_0_detail_4 = knife_0.addOrReplaceChild("knife_0_detail_4",
                CubeListBuilder.create()
                        .texOffs(87, 84).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, -0.593411946F));
        PartDefinition knife_2 = props.addOrReplaceChild("knife_2",
                CubeListBuilder.create()
                        .texOffs(93, 84).addBox(-0.5F, -1F, -0.5F, 1F, 3F, 1F)
                        .texOffs(99, 84).addBox(-0.5F, 2F, -0.5F, 1F, 1F, 1F)
                        .texOffs(105, 84).addBox(-1.5F, -2F, -0.5F, 3F, 1F, 1F)
                        .texOffs(115, 84).addBox(-1F, -6F, -0.5F, 2F, 4F, 1F), PartPose.offsetAndRotation(16F, -15F, -5F, -0.27925268F, -0.261799388F, 1.90240888F));
        PartDefinition knife_2_detail_4 = knife_2.addOrReplaceChild("knife_2_detail_4",
                CubeListBuilder.create()
                        .texOffs(123, 84).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, -0.593411946F));
        PartDefinition knife_1 = props.addOrReplaceChild("knife_1",
                CubeListBuilder.create()
                        .texOffs(243, 66).addBox(-0.5F, -1F, -0.5F, 1F, 3F, 1F)
                        .texOffs(249, 66).addBox(-0.5F, 2F, -0.5F, 1F, 1F, 1F)
                        .texOffs(1, 75).addBox(-1.5F, -2F, -0.5F, 3F, 1F, 1F)
                        .texOffs(11, 75).addBox(-1F, -6F, -0.5F, 2F, 4F, 1F), PartPose.offsetAndRotation(7F, 15F, -1.25F, -0.157079633F, 0F, 1.18682389F));
        PartDefinition knife_1_detail_4 = knife_1.addOrReplaceChild("knife_1_detail_4",
                CubeListBuilder.create()
                        .texOffs(19, 75).addBox(-0.5F, -1F, -0.5F, 1F, 1F, 1F), PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, -0.593411946F));
        return LayerDefinition.create(mesh, 256, 256);
    }
}
