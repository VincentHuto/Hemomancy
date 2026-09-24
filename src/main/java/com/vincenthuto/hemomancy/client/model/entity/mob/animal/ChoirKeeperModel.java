package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Generated from ChoirKeeperModel.bbmodel by export_choir_keeper_java.py. */
public final class ChoirKeeperModel extends EntityModel<ChoirKeeperEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("choir_keeper"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tailFan;
    private final ModelPart[] eyeFeathers = new ModelPart[15];

    public ChoirKeeperModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.tailFan = body.getChild("tail_fan");
        for (int i = 0; i < eyeFeathers.length; i++) {
            eyeFeathers[i] = tailFan.getChild(String.format("eye_feather_%02d", i));
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition part1 = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -22F, 4F, 0.000000F, 0.000000F, 0.000000F));
        part1.addOrReplaceChild("narrow_mantle_1e576282", CubeListBuilder.create().texOffs(1, 1).addBox(-10F, -15F, -11F, 20F, 30F, 21F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -5F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part1.addOrReplaceChild("slim_breast_33d7dcf5", CubeListBuilder.create().texOffs(85, 1).addBox(-8F, -9F, -4F, 16F, 18F, 8F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, -14F, 0.000000F, 0.000000F, 0.000000F));
        part1.addOrReplaceChild("tapered_rump_17c6ad7f", CubeListBuilder.create().texOffs(135, 1).addBox(-8F, -6F, -6F, 16F, 12F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 10F, 11F, 0.000000F, 0.000000F, 0.000000F));
        part1.addOrReplaceChild("tapered_rump_6c8644f1", CubeListBuilder.create().texOffs(413, 362).addBox(-8F, -6F, -6F, 16F, 12F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -10F, 11F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part6 = part1.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -18F, -14F, 0.000000F, 0.000000F, 0.000000F));
        part6.addOrReplaceChild("long_bare_neck_lower_ebb86d1c", CubeListBuilder.create().texOffs(193, 1).addBox(-4F, -11F, -3F, 8F, 21F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -6F, 1F, 0.209440F, 0.000000F, 0.000000F));
        part6.addOrReplaceChild("long_bare_neck_upper_47ffe5e5", CubeListBuilder.create().texOffs(223, 1).addBox(-3F, -8F, -3F, 6F, 16F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -19F, -5F, 0.296706F, 0.000000F, 0.000000F));
        PartDefinition part9 = part6.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -20F, -6F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("small_vulture_head_5678963c", CubeListBuilder.create().texOffs(249, 1).addBox(-5F, -5F, -6F, 10F, 10F, 11F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -7F, -2F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("beak_base_63900f4b", CubeListBuilder.create().texOffs(293, 1).addBox(-3F, -2F, -4F, 6F, 4F, 8F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -5F, -11F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("hooked_beak_tip_364adafb", CubeListBuilder.create().texOffs(323, 1).addBox(-2F, -3F, -2F, 4F, 6F, 4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -2F, -16F, 0.314159F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("left_eye_5d85183e", CubeListBuilder.create().texOffs(341, 1).addBox(0F, -1F, -1F, 1F, 3F, 3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6F, -8F, -4F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("right_eye_01291eae", CubeListBuilder.create().texOffs(351, 1).addBox(-1F, -1F, -1F, 1F, 3F, 3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6F, -8F, -4F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part15 = part1.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(-12F, -12F, -4F, 0.000000F, 0.000000F, 0.000000F));
        part15.addOrReplaceChild("left_folded_wing_d3fa7f42", CubeListBuilder.create().texOffs(361, 1).addBox(-3F, -10F, -13F, 5F, 19F, 25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2F, 6F, 6F, 0.000000F, 0.000000F, 0.104720F));
        part15.addOrReplaceChild("left_flight_feather_0_27fca612", CubeListBuilder.create().texOffs(423, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 16F, -2F, 0.174533F, 0.000000F, 0.069813F));
        part15.addOrReplaceChild("left_flight_feather_1_783c0262", CubeListBuilder.create().texOffs(451, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 17F, 1F, 0.174533F, 0.000000F, 0.104720F));
        part15.addOrReplaceChild("left_flight_feather_2_2efd1acb", CubeListBuilder.create().texOffs(479, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 18F, 4F, 0.174533F, 0.000000F, 0.139626F));
        part15.addOrReplaceChild("left_flight_feather_3_b8bc5fc4", CubeListBuilder.create().texOffs(507, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 7F, 0.174533F, 0.000000F, 0.174533F));
        part15.addOrReplaceChild("left_flight_feather_4_5439a4df", CubeListBuilder.create().texOffs(535, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 20F, 10F, 0.174533F, 0.000000F, 0.209440F));
        PartDefinition part22 = part1.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(12F, -12F, -4F, 0.000000F, 0.000000F, 0.000000F));
        part22.addOrReplaceChild("right_folded_wing_a0d813bd", CubeListBuilder.create().texOffs(563, 1).addBox(-2F, -10F, -13F, 5F, 19F, 25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2F, 6F, 6F, 0.000000F, 0.000000F, -0.104720F));
        part22.addOrReplaceChild("right_flight_feather_0_4adaa471", CubeListBuilder.create().texOffs(625, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 16F, -2F, 0.174533F, 0.000000F, -0.069813F));
        part22.addOrReplaceChild("right_flight_feather_1_93e3d561", CubeListBuilder.create().texOffs(653, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 17F, 1F, 0.174533F, 0.000000F, -0.104720F));
        part22.addOrReplaceChild("right_flight_feather_2_4f1077ca", CubeListBuilder.create().texOffs(681, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 18F, 4F, 0.174533F, 0.000000F, -0.139626F));
        part22.addOrReplaceChild("right_flight_feather_3_66544be5", CubeListBuilder.create().texOffs(709, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 7F, 0.174533F, 0.000000F, -0.174533F));
        part22.addOrReplaceChild("right_flight_feather_4_eb6009cd", CubeListBuilder.create().texOffs(737, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 20F, 10F, 0.174533F, 0.000000F, -0.209440F));
        PartDefinition part29 = part1.addOrReplaceChild("tail_fan", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 13F, 7F, -0.392699F, 0.000000F, 0.000000F));
        PartDefinition part30 = part29.addOrReplaceChild("eye_feather_00", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.366519F, 0.000000F));
        part30.addOrReplaceChild("tail_00_shaft_f32f8bf4", CubeListBuilder.create().texOffs(85, 54).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part30.addOrReplaceChild("tail_00_vane_4dc000b6", CubeListBuilder.create().texOffs(237, 54).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part30.addOrReplaceChild("tail_00_eye_2e8030d6", CubeListBuilder.create().texOffs(323, 54).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part34 = part29.addOrReplaceChild("eye_feather_01", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.314159F, 0.000000F));
        part34.addOrReplaceChild("tail_01_shaft_e17c4fa2", CubeListBuilder.create().texOffs(367, 54).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part34.addOrReplaceChild("tail_01_vane_c9369901", CubeListBuilder.create().texOffs(519, 54).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part34.addOrReplaceChild("tail_01_eye_34150607", CubeListBuilder.create().texOffs(605, 54).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part38 = part29.addOrReplaceChild("eye_feather_02", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.261799F, 0.000000F));
        part38.addOrReplaceChild("tail_02_shaft_13b96f69", CubeListBuilder.create().texOffs(649, 54).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part38.addOrReplaceChild("tail_02_vane_f937d0e4", CubeListBuilder.create().texOffs(801, 54).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part38.addOrReplaceChild("tail_02_eye_ecd2329e", CubeListBuilder.create().texOffs(887, 54).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part42 = part29.addOrReplaceChild("eye_feather_03", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.209440F, 0.000000F));
        part42.addOrReplaceChild("tail_03_shaft_7e5ba51d", CubeListBuilder.create().texOffs(1, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part42.addOrReplaceChild("tail_03_vane_e1d7f934", CubeListBuilder.create().texOffs(153, 131).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part42.addOrReplaceChild("tail_03_eye_ac0082b0", CubeListBuilder.create().texOffs(239, 131).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part46 = part29.addOrReplaceChild("eye_feather_04", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.157080F, 0.000000F));
        part46.addOrReplaceChild("tail_04_shaft_763db5ef", CubeListBuilder.create().texOffs(283, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part46.addOrReplaceChild("tail_04_vane_cf901c7a", CubeListBuilder.create().texOffs(435, 131).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part46.addOrReplaceChild("tail_04_eye_f3b09d37", CubeListBuilder.create().texOffs(521, 131).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part50 = part29.addOrReplaceChild("eye_feather_05", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.104720F, 0.000000F));
        part50.addOrReplaceChild("tail_05_shaft_67905d1f", CubeListBuilder.create().texOffs(565, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part50.addOrReplaceChild("tail_05_vane_3ef0541f", CubeListBuilder.create().texOffs(717, 131).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part50.addOrReplaceChild("tail_05_eye_0176c52d", CubeListBuilder.create().texOffs(803, 131).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part54 = part29.addOrReplaceChild("eye_feather_06", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.052360F, 0.000000F));
        part54.addOrReplaceChild("tail_06_shaft_ddbba029", CubeListBuilder.create().texOffs(847, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part54.addOrReplaceChild("tail_06_vane_8a6e32ca", CubeListBuilder.create().texOffs(1, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part54.addOrReplaceChild("tail_06_eye_5ae717bb", CubeListBuilder.create().texOffs(87, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part58 = part29.addOrReplaceChild("eye_feather_07", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part58.addOrReplaceChild("tail_07_shaft_9f316257", CubeListBuilder.create().texOffs(131, 208).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part58.addOrReplaceChild("tail_07_vane_beba6095", CubeListBuilder.create().texOffs(283, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part58.addOrReplaceChild("tail_07_eye_9f2e8988", CubeListBuilder.create().texOffs(369, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part62 = part29.addOrReplaceChild("eye_feather_08", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.052360F, 0.000000F));
        part62.addOrReplaceChild("tail_08_shaft_c3577c20", CubeListBuilder.create().texOffs(413, 208).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part62.addOrReplaceChild("tail_08_vane_8c887003", CubeListBuilder.create().texOffs(565, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part62.addOrReplaceChild("tail_08_eye_cb5f9509", CubeListBuilder.create().texOffs(651, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part66 = part29.addOrReplaceChild("eye_feather_09", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.104720F, 0.000000F));
        part66.addOrReplaceChild("tail_09_shaft_a9a9f99f", CubeListBuilder.create().texOffs(695, 208).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part66.addOrReplaceChild("tail_09_vane_02412c35", CubeListBuilder.create().texOffs(847, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part66.addOrReplaceChild("tail_09_eye_6b444aee", CubeListBuilder.create().texOffs(933, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part70 = part29.addOrReplaceChild("eye_feather_10", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.157080F, 0.000000F));
        part70.addOrReplaceChild("tail_10_shaft_004e0003", CubeListBuilder.create().texOffs(1, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part70.addOrReplaceChild("tail_10_vane_e2c8dbc9", CubeListBuilder.create().texOffs(153, 285).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part70.addOrReplaceChild("tail_10_eye_7ccd298b", CubeListBuilder.create().texOffs(239, 285).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part74 = part29.addOrReplaceChild("eye_feather_11", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.209440F, 0.000000F));
        part74.addOrReplaceChild("tail_11_shaft_48304eec", CubeListBuilder.create().texOffs(283, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part74.addOrReplaceChild("tail_11_vane_fa535e6f", CubeListBuilder.create().texOffs(435, 285).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part74.addOrReplaceChild("tail_11_eye_7338af15", CubeListBuilder.create().texOffs(521, 285).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part78 = part29.addOrReplaceChild("eye_feather_12", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.261799F, 0.000000F));
        part78.addOrReplaceChild("tail_12_shaft_58b779ca", CubeListBuilder.create().texOffs(565, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part78.addOrReplaceChild("tail_12_vane_e699a669", CubeListBuilder.create().texOffs(717, 285).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part78.addOrReplaceChild("tail_12_eye_7d3ca23f", CubeListBuilder.create().texOffs(803, 285).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part82 = part29.addOrReplaceChild("eye_feather_13", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.314159F, 0.000000F));
        part82.addOrReplaceChild("tail_13_shaft_d309819e", CubeListBuilder.create().texOffs(847, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part82.addOrReplaceChild("tail_13_vane_3cf16bdf", CubeListBuilder.create().texOffs(1, 362).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part82.addOrReplaceChild("tail_13_eye_15a5d0ee", CubeListBuilder.create().texOffs(87, 362).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part86 = part29.addOrReplaceChild("eye_feather_14", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.366519F, 0.000000F));
        part86.addOrReplaceChild("tail_14_shaft_c7130604", CubeListBuilder.create().texOffs(131, 362).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part86.addOrReplaceChild("tail_14_vane_3b677825", CubeListBuilder.create().texOffs(283, 362).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part86.addOrReplaceChild("tail_14_eye_23de5860", CubeListBuilder.create().texOffs(369, 362).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part90 = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-6F, -8F, 7F, 0.000000F, 0.000000F, 0.000000F));
        part90.addOrReplaceChild("left_thigh_9bf87ee3", CubeListBuilder.create().texOffs(765, 1).addBox(-3F, -7F, -3F, 6F, 15F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 4F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part90.addOrReplaceChild("left_shank_e7cae777", CubeListBuilder.create().texOffs(791, 1).addBox(-2F, -9F, -2F, 4F, 18F, 4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 1F, 0.000000F, 0.000000F, 0.000000F));
        part90.addOrReplaceChild("left_front_talon_0_984bf9b6", CubeListBuilder.create().texOffs(809, 1).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part90.addOrReplaceChild("left_front_talon_1_95cec5f7", CubeListBuilder.create().texOffs(839, 1).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part90.addOrReplaceChild("left_front_talon_2_c7ebe945", CubeListBuilder.create().texOffs(869, 1).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part90.addOrReplaceChild("left_rear_talon_6e591c9f", CubeListBuilder.create().texOffs(899, 1).addBox(-1F, -3F, -4F, 2F, 5F, 9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 30F, 5F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part97 = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(6F, -8F, 7F, 0.000000F, 0.000000F, 0.000000F));
        part97.addOrReplaceChild("right_thigh_ccc07ff8", CubeListBuilder.create().texOffs(923, 1).addBox(-3F, -7F, -3F, 6F, 15F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 4F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part97.addOrReplaceChild("right_shank_4e6a256f", CubeListBuilder.create().texOffs(949, 1).addBox(-2F, -9F, -2F, 4F, 18F, 4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 1F, 0.000000F, 0.000000F, 0.000000F));
        part97.addOrReplaceChild("right_front_talon_0_388e9d59", CubeListBuilder.create().texOffs(967, 1).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part97.addOrReplaceChild("right_front_talon_1_40965953", CubeListBuilder.create().texOffs(1, 54).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part97.addOrReplaceChild("right_front_talon_2_cc7b2222", CubeListBuilder.create().texOffs(31, 54).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part97.addOrReplaceChild("right_rear_talon_d9fdc730", CubeListBuilder.create().texOffs(61, 54).addBox(-1F, -3F, -4F, 2F, 5F, 9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 30F, 5F, 0.000000F, 0.000000F, 0.000000F));
        return LayerDefinition.create(mesh, 1024, 512);
    }

    @Override
    public void setupAnim(ChoirKeeperEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        float flare = entity.getFlareAmount(partialTick);
        float flight = entity.getFlightPoseAmount(partialTick);
        float dive = Mth.clamp((float) -entity.getDeltaMovement().y * 0.75F, -0.15F, 0.30F);
        body.xRot = flight * (0.58F + dive);
        neck.xRot = flight * 0.55F;
        neck.z = -14.0F - flight * 8.0F;
        head.xRot = -flight * 0.32F;
        leftLeg.xRot = flight * 0.70F;
        rightLeg.xRot = flight * 0.70F;
        tailFan.xRot = Mth.lerp(flare, -0.3926991F, 0.9599311F);
        for (int i = 0; i < eyeFeathers.length; i++) {
            float restYaw = (i - 7) * 0.05235988F;
            eyeFeathers[i].yRot = restYaw * (1.0F + flare * 2.35F);
        }
        head.yRot = Mth.clamp(netHeadYaw, -40.0F, 40.0F) * Mth.DEG_TO_RAD;
        float flap = flight * Mth.sin(ageInTicks * 1.2F) * 0.42F;
        leftWing.zRot = 0.10472F + flap;
        rightWing.zRot = -0.10472F - flap;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }

}
