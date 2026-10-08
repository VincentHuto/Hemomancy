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
        part1.addOrReplaceChild("slim_breast_33d7dcf5", CubeListBuilder.create().texOffs(85, 1).addBox(-9F, -14F, -5F, 18F, 23F, 10F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, -14F, 0.000000F, 0.000000F, 0.000000F));
        part1.addOrReplaceChild("tapered_rump_17c6ad7f", CubeListBuilder.create().texOffs(143, 1).addBox(-8F, -6F, -6F, 16F, 12F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 10F, 11F, 0.000000F, 0.000000F, 0.000000F));
        part1.addOrReplaceChild("tapered_rump_6c8644f1", CubeListBuilder.create().texOffs(413, 362).addBox(-8F, -6F, -6F, 16F, 12F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -10F, 11F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part6 = part1.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -18F, -14F, 0.000000F, 0.000000F, 0.000000F));
        part6.addOrReplaceChild("feathered_neck_lower_ebb86d1c", CubeListBuilder.create().texOffs(201, 1).addBox(-7F, -5F, -6F, 14F, 11F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -2F, 2F, 0.139626F, 0.000000F, 0.000000F));
        part6.addOrReplaceChild("feathered_neck_upper_47ffe5e5", CubeListBuilder.create().texOffs(255, 1).addBox(-6F, -5F, -6F, 12F, 9F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -7F, -1F, 0.139626F, 0.000000F, 0.000000F));
        PartDefinition part9 = part6.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -8F, -3F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("broad_owl_head_5678963c", CubeListBuilder.create().texOffs(305, 1).addBox(-10F, -8F, -8F, 20F, 16F, 15F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -6F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("short_owl_beak_63900f4b", CubeListBuilder.create().texOffs(377, 1).addBox(-2F, -3F, -2F, 4F, 5F, 4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, -9F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("owl_beak_tip_364adafb", CubeListBuilder.create().texOffs(395, 1).addBox(-1F, -1F, -2F, 2F, 3F, 3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, -10F, 0.209440F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("left_forward_eye_5d85183e", CubeListBuilder.create().texOffs(407, 1).addBox(-3F, -3F, -1F, 6F, 5F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5F, -6F, -9F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("right_forward_eye_01291eae", CubeListBuilder.create().texOffs(423, 1).addBox(-3F, -3F, -1F, 6F, 5F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5F, -6F, -9F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("left_facial_disc_f61a6c31", CubeListBuilder.create().texOffs(471, 362).addBox(-4F, -6F, 0F, 8F, 11F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5F, -6F, -9F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("left_owl_brow_b000b8c6", CubeListBuilder.create().texOffs(491, 362).addBox(-4F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5F, -11F, -9F, 0.000000F, 0.000000F, 0.174533F));
        part9.addOrReplaceChild("left_ear_tuft_cfc49e17", CubeListBuilder.create().texOffs(513, 362).addBox(-2F, -3F, -3F, 4F, 7F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7F, -16F, 1F, 0.000000F, 0.000000F, -0.261799F));
        part9.addOrReplaceChild("left_ear_tuft_tip_3b8199dd", CubeListBuilder.create().texOffs(533, 362).addBox(-1F, -1F, -1F, 2F, 3F, 2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8F, -20F, 1F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("right_facial_disc_01d6b239", CubeListBuilder.create().texOffs(543, 362).addBox(-4F, -6F, 0F, 8F, 11F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5F, -6F, -9F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("right_owl_brow_a8f00c5e", CubeListBuilder.create().texOffs(563, 362).addBox(-4F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5F, -11F, -9F, 0.000000F, 0.000000F, -0.174533F));
        part9.addOrReplaceChild("right_ear_tuft_cb6c5ac8", CubeListBuilder.create().texOffs(585, 362).addBox(-2F, -3F, -3F, 4F, 7F, 5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7F, -16F, 1F, 0.000000F, 0.000000F, 0.261799F));
        part9.addOrReplaceChild("right_ear_tuft_tip_a5880d11", CubeListBuilder.create().texOffs(605, 362).addBox(-1F, -1F, -1F, 2F, 3F, 2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8F, -20F, 1F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("facial_ruff_chin_d30b326d", CubeListBuilder.create().texOffs(615, 362).addBox(-7F, -2F, 0F, 14F, 4F, 1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, -9F, 0.000000F, 0.000000F, 0.000000F));
        part9.addOrReplaceChild("facial_disc_bridge_3ce2b34c", CubeListBuilder.create().texOffs(647, 362).addBox(-1F, -4F, -1F, 2F, 9F, 2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -8F, -9F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part25 = part1.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(-12F, -12F, -4F, 0.000000F, 0.000000F, 0.000000F));
        part25.addOrReplaceChild("left_folded_wing_d3fa7f42", CubeListBuilder.create().texOffs(439, 1).addBox(-3F, -10F, -13F, 5F, 19F, 25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2F, 6F, 6F, 0.000000F, 0.000000F, 0.104720F));
        part25.addOrReplaceChild("left_flight_feather_0_27fca612", CubeListBuilder.create().texOffs(501, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 16F, -2F, 0.174533F, 0.000000F, 0.069813F));
        part25.addOrReplaceChild("left_flight_feather_1_783c0262", CubeListBuilder.create().texOffs(529, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 17F, 1F, 0.174533F, 0.000000F, 0.104720F));
        part25.addOrReplaceChild("left_flight_feather_2_2efd1acb", CubeListBuilder.create().texOffs(557, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 18F, 4F, 0.174533F, 0.000000F, 0.139626F));
        part25.addOrReplaceChild("left_flight_feather_3_b8bc5fc4", CubeListBuilder.create().texOffs(585, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 7F, 0.174533F, 0.000000F, 0.174533F));
        part25.addOrReplaceChild("left_flight_feather_4_5439a4df", CubeListBuilder.create().texOffs(613, 1).addBox(-3F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 20F, 10F, 0.174533F, 0.000000F, 0.209440F));
        PartDefinition part32 = part1.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offsetAndRotation(12F, -12F, -4F, 0.000000F, 0.000000F, 0.000000F));
        part32.addOrReplaceChild("right_folded_wing_a0d813bd", CubeListBuilder.create().texOffs(641, 1).addBox(-2F, -10F, -13F, 5F, 19F, 25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2F, 6F, 6F, 0.000000F, 0.000000F, -0.104720F));
        part32.addOrReplaceChild("right_flight_feather_0_4adaa471", CubeListBuilder.create().texOffs(703, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 16F, -2F, 0.174533F, 0.000000F, -0.069813F));
        part32.addOrReplaceChild("right_flight_feather_1_93e3d561", CubeListBuilder.create().texOffs(731, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 17F, 1F, 0.174533F, 0.000000F, -0.104720F));
        part32.addOrReplaceChild("right_flight_feather_2_4f1077ca", CubeListBuilder.create().texOffs(759, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 18F, 4F, 0.174533F, 0.000000F, -0.139626F));
        part32.addOrReplaceChild("right_flight_feather_3_66544be5", CubeListBuilder.create().texOffs(787, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 7F, 0.174533F, 0.000000F, -0.174533F));
        part32.addOrReplaceChild("right_flight_feather_4_eb6009cd", CubeListBuilder.create().texOffs(815, 1).addBox(-4F, -10F, -3F, 7F, 20F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 20F, 10F, 0.174533F, 0.000000F, -0.209440F));
        PartDefinition part39 = part1.addOrReplaceChild("tail_fan", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 13F, 7F, -0.392699F, 0.000000F, 0.000000F));
        PartDefinition part40 = part39.addOrReplaceChild("eye_feather_00", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.366519F, 0.000000F));
        part40.addOrReplaceChild("tail_00_shaft_f32f8bf4", CubeListBuilder.create().texOffs(159, 54).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part40.addOrReplaceChild("tail_00_vane_4dc000b6", CubeListBuilder.create().texOffs(311, 54).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part40.addOrReplaceChild("tail_00_eye_2e8030d6", CubeListBuilder.create().texOffs(397, 54).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part44 = part39.addOrReplaceChild("eye_feather_01", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.314159F, 0.000000F));
        part44.addOrReplaceChild("tail_01_shaft_e17c4fa2", CubeListBuilder.create().texOffs(441, 54).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part44.addOrReplaceChild("tail_01_vane_c9369901", CubeListBuilder.create().texOffs(593, 54).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part44.addOrReplaceChild("tail_01_eye_34150607", CubeListBuilder.create().texOffs(679, 54).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part48 = part39.addOrReplaceChild("eye_feather_02", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.261799F, 0.000000F));
        part48.addOrReplaceChild("tail_02_shaft_13b96f69", CubeListBuilder.create().texOffs(723, 54).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part48.addOrReplaceChild("tail_02_vane_f937d0e4", CubeListBuilder.create().texOffs(875, 54).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part48.addOrReplaceChild("tail_02_eye_ecd2329e", CubeListBuilder.create().texOffs(961, 54).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part52 = part39.addOrReplaceChild("eye_feather_03", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.209440F, 0.000000F));
        part52.addOrReplaceChild("tail_03_shaft_7e5ba51d", CubeListBuilder.create().texOffs(1, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part52.addOrReplaceChild("tail_03_vane_e1d7f934", CubeListBuilder.create().texOffs(153, 131).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part52.addOrReplaceChild("tail_03_eye_ac0082b0", CubeListBuilder.create().texOffs(239, 131).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part56 = part39.addOrReplaceChild("eye_feather_04", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.157080F, 0.000000F));
        part56.addOrReplaceChild("tail_04_shaft_763db5ef", CubeListBuilder.create().texOffs(283, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part56.addOrReplaceChild("tail_04_vane_cf901c7a", CubeListBuilder.create().texOffs(435, 131).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part56.addOrReplaceChild("tail_04_eye_f3b09d37", CubeListBuilder.create().texOffs(521, 131).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part60 = part39.addOrReplaceChild("eye_feather_05", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.104720F, 0.000000F));
        part60.addOrReplaceChild("tail_05_shaft_67905d1f", CubeListBuilder.create().texOffs(565, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part60.addOrReplaceChild("tail_05_vane_3ef0541f", CubeListBuilder.create().texOffs(717, 131).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part60.addOrReplaceChild("tail_05_eye_0176c52d", CubeListBuilder.create().texOffs(803, 131).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part64 = part39.addOrReplaceChild("eye_feather_06", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, -0.052360F, 0.000000F));
        part64.addOrReplaceChild("tail_06_shaft_ddbba029", CubeListBuilder.create().texOffs(847, 131).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part64.addOrReplaceChild("tail_06_vane_8a6e32ca", CubeListBuilder.create().texOffs(1, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part64.addOrReplaceChild("tail_06_eye_5ae717bb", CubeListBuilder.create().texOffs(87, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part68 = part39.addOrReplaceChild("eye_feather_07", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part68.addOrReplaceChild("tail_07_shaft_9f316257", CubeListBuilder.create().texOffs(131, 208).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part68.addOrReplaceChild("tail_07_vane_beba6095", CubeListBuilder.create().texOffs(283, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part68.addOrReplaceChild("tail_07_eye_9f2e8988", CubeListBuilder.create().texOffs(369, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part72 = part39.addOrReplaceChild("eye_feather_08", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.052360F, 0.000000F));
        part72.addOrReplaceChild("tail_08_shaft_c3577c20", CubeListBuilder.create().texOffs(413, 208).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part72.addOrReplaceChild("tail_08_vane_8c887003", CubeListBuilder.create().texOffs(565, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part72.addOrReplaceChild("tail_08_eye_cb5f9509", CubeListBuilder.create().texOffs(651, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part76 = part39.addOrReplaceChild("eye_feather_09", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.104720F, 0.000000F));
        part76.addOrReplaceChild("tail_09_shaft_a9a9f99f", CubeListBuilder.create().texOffs(695, 208).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part76.addOrReplaceChild("tail_09_vane_02412c35", CubeListBuilder.create().texOffs(847, 208).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part76.addOrReplaceChild("tail_09_eye_6b444aee", CubeListBuilder.create().texOffs(933, 208).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part80 = part39.addOrReplaceChild("eye_feather_10", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.157080F, 0.000000F));
        part80.addOrReplaceChild("tail_10_shaft_004e0003", CubeListBuilder.create().texOffs(1, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part80.addOrReplaceChild("tail_10_vane_e2c8dbc9", CubeListBuilder.create().texOffs(153, 285).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part80.addOrReplaceChild("tail_10_eye_7ccd298b", CubeListBuilder.create().texOffs(239, 285).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part84 = part39.addOrReplaceChild("eye_feather_11", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.209440F, 0.000000F));
        part84.addOrReplaceChild("tail_11_shaft_48304eec", CubeListBuilder.create().texOffs(283, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part84.addOrReplaceChild("tail_11_vane_fa535e6f", CubeListBuilder.create().texOffs(435, 285).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part84.addOrReplaceChild("tail_11_eye_7338af15", CubeListBuilder.create().texOffs(521, 285).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part88 = part39.addOrReplaceChild("eye_feather_12", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.261799F, 0.000000F));
        part88.addOrReplaceChild("tail_12_shaft_58b779ca", CubeListBuilder.create().texOffs(565, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part88.addOrReplaceChild("tail_12_vane_e699a669", CubeListBuilder.create().texOffs(717, 285).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part88.addOrReplaceChild("tail_12_eye_7d3ca23f", CubeListBuilder.create().texOffs(803, 285).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part92 = part39.addOrReplaceChild("eye_feather_13", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.314159F, 0.000000F));
        part92.addOrReplaceChild("tail_13_shaft_d309819e", CubeListBuilder.create().texOffs(847, 285).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part92.addOrReplaceChild("tail_13_vane_3cf16bdf", CubeListBuilder.create().texOffs(1, 362).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part92.addOrReplaceChild("tail_13_eye_15a5d0ee", CubeListBuilder.create().texOffs(87, 362).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part96 = part39.addOrReplaceChild("eye_feather_14", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, -0F, 0F, 0.000000F, 0.366519F, 0.000000F));
        part96.addOrReplaceChild("tail_14_shaft_c7130604", CubeListBuilder.create().texOffs(131, 362).addBox(-1F, -1F, -37F, 2F, 2F, 73F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -0F, 37F, 0.000000F, 0.000000F, 0.000000F));
        part96.addOrReplaceChild("tail_14_vane_3b677825", CubeListBuilder.create().texOffs(283, 362).addBox(-4F, -1F, -17F, 8F, 3F, 34F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -1F, 64F, 0.000000F, 0.000000F, 0.000000F));
        part96.addOrReplaceChild("tail_14_eye_23de5860", CubeListBuilder.create().texOffs(369, 362).addBox(-4F, 0F, -7F, 8F, 1F, 13F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, -3F, 71F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part100 = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(-6F, -8F, 7F, 0.000000F, 0.000000F, 0.000000F));
        part100.addOrReplaceChild("left_thigh_9bf87ee3", CubeListBuilder.create().texOffs(843, 1).addBox(-3F, -7F, -3F, 6F, 15F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 4F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part100.addOrReplaceChild("left_shank_e7cae777", CubeListBuilder.create().texOffs(869, 1).addBox(-2F, -9F, -2F, 4F, 18F, 4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 1F, 0.000000F, 0.000000F, 0.000000F));
        part100.addOrReplaceChild("left_front_talon_0_984bf9b6", CubeListBuilder.create().texOffs(887, 1).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part100.addOrReplaceChild("left_front_talon_1_95cec5f7", CubeListBuilder.create().texOffs(917, 1).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part100.addOrReplaceChild("left_front_talon_2_c7ebe945", CubeListBuilder.create().texOffs(947, 1).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part100.addOrReplaceChild("left_rear_talon_6e591c9f", CubeListBuilder.create().texOffs(977, 1).addBox(-1F, -3F, -4F, 2F, 5F, 9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 30F, 5F, 0.000000F, 0.000000F, 0.000000F));
        PartDefinition part107 = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offsetAndRotation(6F, -8F, 7F, 0.000000F, 0.000000F, 0.000000F));
        part107.addOrReplaceChild("right_thigh_ccc07ff8", CubeListBuilder.create().texOffs(1, 54).addBox(-3F, -7F, -3F, 6F, 15F, 6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 4F, 0F, 0.000000F, 0.000000F, 0.000000F));
        part107.addOrReplaceChild("right_shank_4e6a256f", CubeListBuilder.create().texOffs(27, 54).addBox(-2F, -9F, -2F, 4F, 18F, 4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 19F, 1F, 0.000000F, 0.000000F, 0.000000F));
        part107.addOrReplaceChild("right_front_talon_0_388e9d59", CubeListBuilder.create().texOffs(45, 54).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part107.addOrReplaceChild("right_front_talon_1_40965953", CubeListBuilder.create().texOffs(75, 54).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part107.addOrReplaceChild("right_front_talon_2_cc7b2222", CubeListBuilder.create().texOffs(105, 54).addBox(-1F, -3F, -6F, 2F, 6F, 12F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3F, 29F, -5F, 0.191986F, 0.000000F, 0.000000F));
        part107.addOrReplaceChild("right_rear_talon_d9fdc730", CubeListBuilder.create().texOffs(135, 54).addBox(-1F, -3F, -4F, 2F, 5F, 9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0F, 30F, 5F, 0.000000F, 0.000000F, 0.000000F));
        return LayerDefinition.create(mesh, 1024, 512);
    }

    @Override
    public void setupAnim(ChoirKeeperEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        float flare = entity.getFlareAmount(partialTick);
        float flight = entity.getFlightPoseAmount(partialTick);
        float dive = Mth.clamp((float) -entity.getDeltaMovement().y * 0.75F, -0.15F, 0.30F);
        root.getAllParts().forEach(ModelPart::resetPose);
        body.xRot = flight * (0.58F + dive);
        neck.xRot = flight * 0.55F;
        neck.z -= flight * 4.0F;
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
