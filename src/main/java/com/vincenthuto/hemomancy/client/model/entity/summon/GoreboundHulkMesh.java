package com.vincenthuto.hemomancy.client.model.entity.summon;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Generated from GoreboundHulk.bbmodel by tools/circus/build_gorebound_hulk.py. */
final class GoreboundHulkMesh {
    private GoreboundHulkMesh() {}

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root",
                CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis",
                CubeListBuilder.create()
                        .texOffs(153, 47).addBox(-7F, -4F, -4F, 14F, 6F, 8F)
                        .texOffs(199, 47).addBox(-8F, -6F, -5F, 16F, 4F, 10F)
                        .texOffs(49, 93).addBox(-3F, -6F, -6F, 6F, 4F, 2F), PartPose.offset(0F, -10F, 0F));
        PartDefinition chest = pelvis.addOrReplaceChild("chest",
                CubeListBuilder.create()
                        .texOffs(149, 1).addBox(-7F, -8F, -5F, 14F, 8F, 10F)
                        .texOffs(1, 1).addBox(-10F, -16F, -6F, 20F, 11F, 13F)
                        .texOffs(33, 27).addBox(-8F, -19F, -1F, 16F, 7F, 10F)
                        .texOffs(1, 64).addBox(-2F, -18F, 6F, 4F, 10F, 4F)
                        .texOffs(171, 80).addBox(-6.5F, -20F, 7F, 3F, 4F, 3F)
                        .texOffs(197, 93).addBox(-5.5F, -19F, 9F, 1F, 2F, 1F)
                        .texOffs(185, 80).addBox(3.5F, -20F, 7F, 3F, 4F, 3F)
                        .texOffs(203, 93).addBox(4.5F, -19F, 9F, 1F, 2F, 1F), PartPose.offsetAndRotation(0F, -5F, 0F, 0.191986218F, 0F, 0F));
        PartDefinition chest_detail_4 = chest.addOrReplaceChild("chest_detail_4",
                CubeListBuilder.create()
                        .texOffs(25, 80).addBox(-4.25F, -3F, -2F, 9F, 6F, 4F), PartPose.offsetAndRotation(-5F, -11F, -6F, 0F, 0F, 0.13962634F));
        PartDefinition chest_detail_5 = chest.addOrReplaceChild("chest_detail_5",
                CubeListBuilder.create()
                        .texOffs(167, 27).addBox(-4F, -3F, -4F, 8F, 5F, 10F), PartPose.offsetAndRotation(-6F, -16F, 2F, 0F, 0F, -0.331612558F));
        PartDefinition chest_detail_8 = chest.addOrReplaceChild("chest_detail_8",
                CubeListBuilder.create()
                        .texOffs(53, 80).addBox(-4.75F, -3F, -2F, 9F, 6F, 4F), PartPose.offsetAndRotation(5F, -11F, -6F, 0F, 0F, -0.13962634F));
        PartDefinition chest_detail_9 = chest.addOrReplaceChild("chest_detail_9",
                CubeListBuilder.create()
                        .texOffs(205, 27).addBox(-4F, -3F, -4F, 8F, 5F, 10F), PartPose.offsetAndRotation(6F, -16F, 2F, 0F, 0F, 0.331612558F));
        PartDefinition head = chest.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(203, 64).addBox(-3F, -3F, -2F, 6F, 6F, 5F)
                        .texOffs(107, 64).addBox(-4F, -5F, -7F, 8F, 6F, 7F)
                        .texOffs(135, 93).addBox(-4.5F, -5F, -8F, 9F, 2F, 2F)
                        .texOffs(159, 93).addBox(-3F, -1F, -8F, 6F, 2F, 2F)
                        .texOffs(209, 93).addBox(-4.5F, -1F, -8F, 2F, 1F, 2F)
                        .texOffs(177, 93).addBox(-5.5F, -3F, -4F, 2F, 2F, 2F)
                        .texOffs(219, 93).addBox(2.5F, -1F, -8F, 2F, 1F, 2F)
                        .texOffs(187, 93).addBox(3.5F, -3F, -4F, 2F, 2F, 2F), PartPose.offsetAndRotation(0F, -12F, -8F, -0.191986218F, 0F, 0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw",
                CubeListBuilder.create()
                        .texOffs(81, 80).addBox(-3F, 0F, -6F, 6F, 2F, 6F), PartPose.offset(0F, 1F, -1F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(199, 1).addBox(-4.5F, 0F, -4F, 7F, 10F, 8F), PartPose.offsetAndRotation(-11F, -12F, 0F, -0.0872664626F, 0F, 0.13962634F));
        PartDefinition right_arm_detail_0 = right_arm.addOrReplaceChild("right_arm_detail_0",
                CubeListBuilder.create()
                        .texOffs(87, 27).addBox(-4.5F, -4F, -5F, 9F, 7F, 10F), PartPose.offsetAndRotation(0F, 1F, 0F, 0F, 0F, 0.226892803F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm",
                CubeListBuilder.create()
                        .texOffs(107, 80).addBox(-4.5F, -2F, -2F, 3F, 4F, 4F)
                        .texOffs(69, 1).addBox(-4.5F, -1F, -5F, 9F, 11F, 10F), PartPose.offsetAndRotation(-1F, 9F, -1F, -0.27925268F, 0F, -0.0872664626F));
        PartDefinition right_binding = right_forearm.addOrReplaceChild("right_binding",
                CubeListBuilder.create()
                        .texOffs(19, 64).addBox(-5F, 8F, -5.5F, 10F, 3F, 11F), PartPose.offset(0F, 0F, 0F));
        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand",
                CubeListBuilder.create()
                        .texOffs(1, 47).addBox(-5F, 0F, -6F, 10F, 6F, 9F), PartPose.offsetAndRotation(0F, 10F, 0F, 0.122173048F, 0F, 0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb",
                CubeListBuilder.create()
                        .texOffs(123, 80).addBox(-0.5F, -1.5F, -1F, 3F, 4F, 4F), PartPose.offsetAndRotation(4F, 3F, -3F, 0F, 0F, 0.296705973F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0",
                CubeListBuilder.create()
                        .texOffs(199, 80).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(-3.75F, 3F, -5F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1",
                CubeListBuilder.create()
                        .texOffs(211, 80).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(-1.25F, 3F, -5F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2",
                CubeListBuilder.create()
                        .texOffs(223, 80).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(1.25F, 3F, -5F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3",
                CubeListBuilder.create()
                        .texOffs(235, 80).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(3.75F, 3F, -5F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(1, 27).addBox(-2.5F, 0F, -4F, 7F, 10F, 8F), PartPose.offsetAndRotation(11F, -12F, 0F, -0.0872664626F, 0F, -0.13962634F));
        PartDefinition left_arm_detail_0 = left_arm.addOrReplaceChild("left_arm_detail_0",
                CubeListBuilder.create()
                        .texOffs(127, 27).addBox(-4.5F, -4F, -5F, 9F, 7F, 10F), PartPose.offsetAndRotation(0F, 1F, 0F, 0F, 0F, -0.226892803F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm",
                CubeListBuilder.create()
                        .texOffs(139, 80).addBox(1.5F, -2F, -2F, 3F, 4F, 4F)
                        .texOffs(109, 1).addBox(-4.5F, -1F, -5F, 9F, 11F, 10F), PartPose.offsetAndRotation(1F, 9F, -1F, -0.27925268F, 0F, 0.0872664626F));
        PartDefinition left_binding = left_forearm.addOrReplaceChild("left_binding",
                CubeListBuilder.create()
                        .texOffs(63, 64).addBox(-5F, 8F, -5.5F, 10F, 3F, 11F), PartPose.offset(0F, 0F, 0F));
        PartDefinition left_hand = left_forearm.addOrReplaceChild("left_hand",
                CubeListBuilder.create()
                        .texOffs(77, 47).addBox(-5F, 0F, -6F, 10F, 6F, 9F), PartPose.offsetAndRotation(0F, 10F, 0F, 0.122173048F, 0F, 0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb",
                CubeListBuilder.create()
                        .texOffs(155, 80).addBox(-2.5F, -1.5F, -1F, 3F, 4F, 4F), PartPose.offsetAndRotation(-4F, 3F, -3F, 0F, 0F, -0.296705973F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0",
                CubeListBuilder.create()
                        .texOffs(1, 93).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(-3.75F, 3F, -5F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1",
                CubeListBuilder.create()
                        .texOffs(13, 93).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(-1.25F, 3F, -5F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2",
                CubeListBuilder.create()
                        .texOffs(25, 93).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(1.25F, 3F, -5F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3",
                CubeListBuilder.create()
                        .texOffs(37, 93).addBox(-1F, -1F, -2.5F, 2F, 4F, 3F), PartPose.offset(3.75F, 3F, -5F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh",
                CubeListBuilder.create()
                        .texOffs(139, 64).addBox(-4F, 0F, -4F, 7F, 4F, 8F), PartPose.offsetAndRotation(-4F, -1F, 0F, 0F, 0F, 0.0872664626F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin",
                CubeListBuilder.create()
                        .texOffs(227, 64).addBox(-2.5F, -1F, -3F, 5F, 5F, 6F)
                        .texOffs(67, 93).addBox(-3.5F, -1F, -1.5F, 2F, 3F, 3F), PartPose.offset(-0.5F, 4F, 0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot",
                CubeListBuilder.create(), PartPose.offset(0F, 4F, 0F));
        PartDefinition right_foot_detail_0 = right_foot.addOrReplaceChild("right_foot_detail_0",
                CubeListBuilder.create()
                        .texOffs(41, 47).addBox(-3.5F, -3F, -6F, 7F, 5F, 10F), PartPose.offsetAndRotation(0F, 1F, 0F, 0F, 0.13962634F, 0F));
        PartDefinition right_foot_detail_1 = right_foot.addOrReplaceChild("right_foot_detail_1",
                CubeListBuilder.create()
                        .texOffs(79, 93).addBox(-3.5F, -1F, -7F, 7F, 3F, 3F), PartPose.offsetAndRotation(0F, 1F, 0F, 0F, 0.13962634F, 0F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh",
                CubeListBuilder.create()
                        .texOffs(171, 64).addBox(-3F, 0F, -4F, 7F, 4F, 8F), PartPose.offsetAndRotation(4F, -1F, 0F, 0F, 0F, -0.0872664626F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin",
                CubeListBuilder.create()
                        .texOffs(1, 80).addBox(-2.5F, -1F, -3F, 5F, 5F, 6F)
                        .texOffs(101, 93).addBox(1.5F, -1F, -1.5F, 2F, 3F, 3F), PartPose.offset(0.5F, 4F, 0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot",
                CubeListBuilder.create(), PartPose.offset(0F, 4F, 0F));
        PartDefinition left_foot_detail_0 = left_foot.addOrReplaceChild("left_foot_detail_0",
                CubeListBuilder.create()
                        .texOffs(117, 47).addBox(-3.5F, -3F, -6F, 7F, 5F, 10F), PartPose.offsetAndRotation(0F, 1F, 0F, 0F, -0.13962634F, 0F));
        PartDefinition left_foot_detail_1 = left_foot.addOrReplaceChild("left_foot_detail_1",
                CubeListBuilder.create()
                        .texOffs(113, 93).addBox(-3.5F, -1F, -7F, 7F, 3F, 3F), PartPose.offsetAndRotation(0F, 1F, 0F, 0F, -0.13962634F, 0F));
        return LayerDefinition.create(mesh, 256, 128);
    }
}
