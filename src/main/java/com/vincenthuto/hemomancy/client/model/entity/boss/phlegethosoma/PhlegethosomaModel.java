package com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaSwimmingRules;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/** Articulated blood-fluke ribbon: every joint carries the following body sections. */
public final class PhlegethosomaModel extends EntityModel<PhlegethosomaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("phlegethosoma"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart mouth;
    private final ModelPart[] segments = new ModelPart[PhlegethosomaSwimmingRules.SEGMENTS];

    public PhlegethosomaModel(ModelPart baked) {
        root = baked.getChild("root");
        head = root.getChild("head");
        mouth = head.getChild("mouth");
        ModelPart parent = root;
        for (int i = 0; i < segments.length; i++) {
            segments[i] = parent.getChild("segment_" + i);
            parent = segments[i];
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, 18.5F, 0));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(144, 0).addBox(-10.00F, -5.00F, -6.00F, 20.00F, 10.00F, 12.00F).texOffs(144, 0).addBox(-8.00F, -6.00F, -8.00F, 16.00F, 12.00F, 8.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(0, 128).addBox(-7.00F, -4.50F, -1.00F, 14.00F, 9.00F, 2.00F), PartPose.offset(0.00F, 0.00F, -10.00F));
        mouth.addOrReplaceChild("upper_lip", CubeListBuilder.create().texOffs(144, 40).addBox(-6.00F, -6.00F, -2.00F, 12.00F, 2.00F, 3.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        mouth.addOrReplaceChild("lower_lip", CubeListBuilder.create().texOffs(144, 40).addBox(-6.00F, 4.00F, -2.00F, 12.00F, 2.00F, 3.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        mouth.addOrReplaceChild("left_lip", CubeListBuilder.create().texOffs(144, 40).addBox(-9.00F, -3.00F, -2.00F, 3.00F, 6.00F, 3.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        mouth.addOrReplaceChild("right_lip", CubeListBuilder.create().texOffs(144, 40).addBox(6.00F, -3.00F, -2.00F, 3.00F, 6.00F, 3.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        mouth.addOrReplaceChild("lip_corner_7", CubeListBuilder.create().texOffs(144, 40).addBox(-1.50F, -1.50F, -1.75F, 3.00F, 3.00F, 3.00F), PartPose.offset(-6.50F, -3.75F, 0.00F));
        mouth.addOrReplaceChild("lip_corner_8", CubeListBuilder.create().texOffs(144, 40).addBox(-1.50F, -1.50F, -1.75F, 3.00F, 3.00F, 3.00F), PartPose.offset(-6.50F, 3.75F, 0.00F));
        mouth.addOrReplaceChild("lip_corner_9", CubeListBuilder.create().texOffs(144, 40).addBox(-1.50F, -1.50F, -1.75F, 3.00F, 3.00F, 3.00F), PartPose.offset(6.50F, -3.75F, 0.00F));
        mouth.addOrReplaceChild("lip_corner_10", CubeListBuilder.create().texOffs(144, 40).addBox(-1.50F, -1.50F, -1.75F, 3.00F, 3.00F, 3.00F), PartPose.offset(6.50F, 3.75F, 0.00F));
        mouth.addOrReplaceChild("upper_rasp_0", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(-5.00F, -4.00F, -1.50F));
        mouth.addOrReplaceChild("lower_rasp_0", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(-5.00F, 2.00F, -1.50F));
        mouth.addOrReplaceChild("upper_rasp_1", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(-3.00F, -4.00F, -1.50F));
        mouth.addOrReplaceChild("lower_rasp_1", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(-3.00F, 2.00F, -1.50F));
        mouth.addOrReplaceChild("upper_rasp_2", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(-1.00F, -4.00F, -1.50F));
        mouth.addOrReplaceChild("lower_rasp_2", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(-1.00F, 2.00F, -1.50F));
        mouth.addOrReplaceChild("upper_rasp_3", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(1.00F, -4.00F, -1.50F));
        mouth.addOrReplaceChild("lower_rasp_3", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(1.00F, 2.00F, -1.50F));
        mouth.addOrReplaceChild("upper_rasp_4", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(3.00F, -4.00F, -1.50F));
        mouth.addOrReplaceChild("lower_rasp_4", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(3.00F, 2.00F, -1.50F));
        mouth.addOrReplaceChild("upper_rasp_5", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(5.00F, -4.00F, -1.50F));
        mouth.addOrReplaceChild("lower_rasp_5", CubeListBuilder.create().texOffs(224, 0).addBox(-0.50F, 0.00F, -1.25F, 1.00F, 2.00F, 2.00F), PartPose.offset(5.00F, 2.00F, -1.50F));
        mouth.addOrReplaceChild("left_rasp_0", CubeListBuilder.create().texOffs(224, 0).addBox(0.00F, -0.50F, -1.00F, 2.00F, 1.00F, 2.00F), PartPose.offset(-6.50F, -2.00F, -1.50F));
        mouth.addOrReplaceChild("left_rasp_1", CubeListBuilder.create().texOffs(224, 0).addBox(0.00F, -0.50F, -1.00F, 2.00F, 1.00F, 2.00F), PartPose.offset(-6.50F, 0.00F, -1.50F));
        mouth.addOrReplaceChild("left_rasp_2", CubeListBuilder.create().texOffs(224, 0).addBox(0.00F, -0.50F, -1.00F, 2.00F, 1.00F, 2.00F), PartPose.offset(-6.50F, 2.00F, -1.50F));
        mouth.addOrReplaceChild("right_rasp_0", CubeListBuilder.create().texOffs(224, 0).addBox(0.00F, -0.50F, -1.00F, 2.00F, 1.00F, 2.00F), PartPose.offset(4.50F, -2.00F, -1.50F));
        mouth.addOrReplaceChild("right_rasp_1", CubeListBuilder.create().texOffs(224, 0).addBox(0.00F, -0.50F, -1.00F, 2.00F, 1.00F, 2.00F), PartPose.offset(4.50F, 0.00F, -1.50F));
        mouth.addOrReplaceChild("right_rasp_2", CubeListBuilder.create().texOffs(224, 0).addBox(0.00F, -0.50F, -1.00F, 2.00F, 1.00F, 2.00F), PartPose.offset(4.50F, 2.00F, -1.50F));
        head.addOrReplaceChild("ventral_sucker", CubeListBuilder.create().texOffs(144, 96).addBox(-6.00F, 4.50F, -2.00F, 12.00F, 1.00F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment0 = root.addOrReplaceChild("segment_0", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 4.50F));
        segment0.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 0).addBox(-8.50F, -4.50F, -1.00F, 17.00F, 9.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment0.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-4.25F, 4.25F, -0.50F, 8.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment0.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -5.00F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment0.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-12.00F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment0.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(8.25F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment1 = segment0.addOrReplaceChild("segment_1", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment1.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 24).addBox(-9.00F, -5.00F, -1.00F, 18.00F, 10.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment1.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-4.50F, 4.75F, -0.50F, 9.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment1.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -5.50F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment1.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-12.50F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment1.addOrReplaceChild("blood_vesicle_-1", CubeListBuilder.create().texOffs(64, 32).addBox(-10.00F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment1.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(8.75F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment1.addOrReplaceChild("blood_vesicle_1", CubeListBuilder.create().texOffs(64, 32).addBox(8.00F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment2 = segment1.addOrReplaceChild("segment_2", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment2.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 48).addBox(-9.00F, -5.00F, -1.00F, 18.00F, 10.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment2.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-4.50F, 4.75F, -0.50F, 9.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment2.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -5.50F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment2.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-12.50F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment2.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(8.75F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment3 = segment2.addOrReplaceChild("segment_3", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment3.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 0).addBox(-8.50F, -4.75F, -1.00F, 17.00F, 9.50F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment3.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-4.25F, 4.50F, -0.50F, 8.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment3.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -5.25F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment3.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-12.00F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment3.addOrReplaceChild("blood_vesicle_-1", CubeListBuilder.create().texOffs(64, 32).addBox(-9.50F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment3.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(8.25F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment3.addOrReplaceChild("blood_vesicle_1", CubeListBuilder.create().texOffs(64, 32).addBox(7.50F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment4 = segment3.addOrReplaceChild("segment_4", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment4.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 24).addBox(-8.00F, -4.50F, -1.00F, 16.00F, 9.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment4.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-4.00F, 4.25F, -0.50F, 8.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment4.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -5.00F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment4.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-11.50F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment4.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(7.75F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment5 = segment4.addOrReplaceChild("segment_5", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment5.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 48).addBox(-7.50F, -4.25F, -1.00F, 15.00F, 8.50F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment5.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-3.75F, 4.00F, -0.50F, 7.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment5.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -4.75F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment5.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-11.00F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment5.addOrReplaceChild("blood_vesicle_-1", CubeListBuilder.create().texOffs(64, 32).addBox(-8.50F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment5.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(7.25F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment5.addOrReplaceChild("blood_vesicle_1", CubeListBuilder.create().texOffs(64, 32).addBox(6.50F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment6 = segment5.addOrReplaceChild("segment_6", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment6.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 0).addBox(-6.75F, -4.00F, -1.00F, 13.50F, 8.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment6.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-3.50F, 3.75F, -0.50F, 6.75F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment6.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -4.50F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment6.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-10.25F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment6.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(6.50F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment7 = segment6.addOrReplaceChild("segment_7", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment7.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 24).addBox(-6.00F, -3.50F, -1.00F, 12.00F, 7.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment7.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-3.00F, 3.25F, -0.50F, 6.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment7.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -4.00F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment7.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-9.50F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment7.addOrReplaceChild("blood_vesicle_-1", CubeListBuilder.create().texOffs(64, 32).addBox(-7.00F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment7.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(5.75F, 0.50F, -0.50F, 3.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment7.addOrReplaceChild("blood_vesicle_1", CubeListBuilder.create().texOffs(64, 32).addBox(5.00F, -1.00F, 2.00F, 2.00F, 3.00F, 4.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment8 = segment7.addOrReplaceChild("segment_8", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment8.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 48).addBox(-5.00F, -3.00F, -1.00F, 10.00F, 6.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment8.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-2.50F, 2.75F, -0.50F, 5.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment8.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -3.50F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment8.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-7.00F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment8.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(4.75F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment9 = segment8.addOrReplaceChild("segment_9", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment9.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 0).addBox(-4.00F, -2.50F, -1.00F, 8.00F, 5.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment9.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-2.00F, 2.25F, -0.50F, 4.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment9.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -3.00F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment9.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-6.00F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment9.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(3.75F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment10 = segment9.addOrReplaceChild("segment_10", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment10.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 24).addBox(-3.00F, -2.00F, -1.00F, 6.00F, 4.00F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment10.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-1.50F, 1.75F, -0.50F, 3.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment10.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -2.50F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment10.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-5.00F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment10.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(2.75F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        PartDefinition segment11 = segment10.addOrReplaceChild("segment_11", CubeListBuilder.create(), PartPose.offset(0.00F, 0.00F, 9.50F));
        segment11.addOrReplaceChild("flesh", CubeListBuilder.create().texOffs(0, 48).addBox(-2.00F, -1.25F, -1.00F, 4.00F, 2.50F, 11.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment11.addOrReplaceChild("ventral_band", CubeListBuilder.create().texOffs(64, 0).addBox(-1.00F, 1.00F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment11.addOrReplaceChild("artery", CubeListBuilder.create().texOffs(112, 0).addBox(-0.75F, -1.75F, -0.50F, 1.50F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment11.addOrReplaceChild("frill_-1", CubeListBuilder.create().texOffs(0, 88).addBox(-4.00F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment11.addOrReplaceChild("frill_1", CubeListBuilder.create().texOffs(0, 88).addBox(1.75F, 0.50F, -0.50F, 2.00F, 0.50F, 10.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        segment11.addOrReplaceChild("tail_ribbon", CubeListBuilder.create().texOffs(0, 88).addBox(-1.50F, -0.50F, 8.00F, 3.00F, 1.00F, 13.00F).texOffs(112, 0).addBox(-0.50F, -0.75F, 8.00F, 1.00F, 0.50F, 12.00F), PartPose.offset(0.00F, 0.00F, 0.00F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(PhlegethosomaEntity entity, float limbSwing, float limbAmount, float age,
                          float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partial = age - entity.tickCount;
        float attackTime = entity.animationTime(partial);
        float movement = (entity.isIchorSwimming() || entity.isBreachAttack()) ? entity.swimAmount() : Math.min(.6F, limbAmount * 2);
        if (entity.action() == PhlegethosomaEntity.Action.LUNGE && attackTime >= 20 && attackTime < 28) movement = 1;
        root.xRot = (entity.isIchorSwimming() || entity.isBreachAttack()) ? entity.swimPitch() : 0;
        root.y += entity.isIchorSwimming() ? Mth.sin(age * .17F) * .5F : 0;
        if (entity.introTime() >= 0) head.xRot = -.15F * Mth.sin(entity.introTime() / (float) PhlegethosomaCombatRules.INTRO_TICKS * Mth.PI);
        head.yRot = Mth.clamp(yaw * Mth.DEG_TO_RAD, -.3F, .3F);
        for (int i = 0; i < segments.length; i++) {
            ModelPart segment = segments[i];
            segment.yRot = PhlegethosomaSwimmingRules.segmentYaw(i, age, movement);
            segment.xRot = Mth.sin(age * .25F - i * .6F) * .025F * movement;
            ModelPart flesh = segment.getChild("flesh");
            if (entity.swollen() && i >= 1 && i <= 4) {
                flesh.xScale = 1.12F;
                flesh.yScale = 1.20F + Mth.sin(age * .18F - i) * .03F;
            }
            if (entity.phase() == 3) {
                flesh.yScale *= .88F;
                for (String side : new String[]{"frill_-1", "frill_1"})
                    segment.getChild(side).zRot = Mth.sin(age * .5F - i) * .12F;
            }
        }
        boolean suction = entity.action() == PhlegethosomaEntity.Action.SUCTION_WINDUP
                || entity.action() == PhlegethosomaEntity.Action.SUCTION_ACTIVE;
        mouth.xScale = suction || entity.action() == PhlegethosomaEntity.Action.BREACH ? 1.25F : 1;
        mouth.yScale = suction || entity.action() == PhlegethosomaEntity.Action.BREACH ? 1.35F : 1;
        if (entity.action() == PhlegethosomaEntity.Action.LATCH_ATTACK) mouth.z = -10 - Mth.sin(Math.min(attackTime,30)/30*Mth.PI)*3;
        if (entity.action() == PhlegethosomaEntity.Action.STAGGERED) head.zRot = Mth.sin(attackTime * .8F) * .12F;
        head.xRot += Mth.sin(entity.hurtTime * .6F) * .1F;
        if (entity.deathTime > 0) root.zRot = Math.min(1, entity.deathTime / 20F) * .9F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) {
        root.render(pose, vertices, light, overlay, color);
    }
}
