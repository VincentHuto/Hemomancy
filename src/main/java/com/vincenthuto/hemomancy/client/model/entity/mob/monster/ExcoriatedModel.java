package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedEntity;
import com.vincenthuto.hemomancy.common.init.FluidInit;
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
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ExcoriatedModel extends EntityModel<ExcoriatedEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "excoriated"), "main");
    private final ModelPart root;
    private final ModelPart horse_body;
    private final ModelPart front_left;
    private final ModelPart front_left_lower;
    private final ModelPart front_left_hoof;
    private final ModelPart front_right;
    private final ModelPart front_right_lower;
    private final ModelPart front_right_hoof;
    private final ModelPart hind_left;
    private final ModelPart hind_left_lower;
    private final ModelPart hind_left_hoof;
    private final ModelPart hind_right;
    private final ModelPart hind_right_lower;
    private final ModelPart hind_right_hoof;
    private final ModelPart horse_neck;
    private final ModelPart horse_head;
    private final ModelPart tail;
    private final ModelPart human_torso;
    private final ModelPart human_head;
    private final ModelPart left_arm;
    private final ModelPart left_forearm;
    private final ModelPart fused_bow;
    private final ModelPart bowstring;
    private final ModelPart string_upper;
    private final ModelPart string_lower;
    private final ModelPart arrow;
    private final ModelPart right_arm;
    private final ModelPart right_forearm;
    private final ModelPart right_hand;
    private final Vector3f upperBind;
    private final Vector3f lowerBind;
    private final Vector3f nockBind;
    private final Vector3f topTip = new Vector3f(0, -19.2F, 2.1F);
    private final Vector3f bottomTip = new Vector3f(0, 19.5F, 2.1F);
    private final float topLength;
    private final float bottomLength;

    public ExcoriatedModel(ModelPart root) {
        this.root = root.getChild("root");
        this.horse_body = this.root.getChild("horse_body");
        this.front_left = this.horse_body.getChild("front_left");
        this.front_left_lower = this.front_left.getChild("front_left_lower");
        this.front_left_hoof = this.front_left_lower.getChild("front_left_hoof");
        this.front_right = this.horse_body.getChild("front_right");
        this.front_right_lower = this.front_right.getChild("front_right_lower");
        this.front_right_hoof = this.front_right_lower.getChild("front_right_hoof");
        this.hind_left = this.horse_body.getChild("hind_left");
        this.hind_left_lower = this.hind_left.getChild("hind_left_lower");
        this.hind_left_hoof = this.hind_left_lower.getChild("hind_left_hoof");
        this.hind_right = this.horse_body.getChild("hind_right");
        this.hind_right_lower = this.hind_right.getChild("hind_right_lower");
        this.hind_right_hoof = this.hind_right_lower.getChild("hind_right_hoof");
        this.horse_neck = this.horse_body.getChild("horse_neck");
        this.horse_head = this.horse_neck.getChild("horse_head");
        this.tail = this.horse_body.getChild("tail");
        this.human_torso = this.horse_body.getChild("human_torso");
        this.human_head = this.human_torso.getChild("human_head");
        this.left_arm = this.human_torso.getChild("left_arm");
        this.left_forearm = this.left_arm.getChild("left_forearm");
        this.fused_bow = this.left_forearm.getChild("fused_bow");
        this.bowstring = this.fused_bow.getChild("bowstring");
        this.string_upper = this.bowstring.getChild("string_upper_r1");
        this.string_lower = this.bowstring.getChild("string_lower_r1");
        this.arrow = this.bowstring.getChild("arrow");
        this.right_arm = this.human_torso.getChild("right_arm");
        this.right_forearm = this.right_arm.getChild("right_forearm");
        this.right_hand = this.right_forearm.getChild("right_hand");
        this.upperBind = new Vector3f(this.right_forearm.x, this.right_forearm.y, this.right_forearm.z);
        this.lowerBind = new Vector3f(this.right_hand.x, this.right_hand.y, this.right_hand.z);
        this.nockBind = new Vector3f(this.arrow.x, this.arrow.y, this.arrow.z);
        this.topLength = this.topTip.distance(this.nockBind) + .2F;
        this.bottomLength = this.bottomTip.distance(this.nockBind) + .2F;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition horse_body = root.addOrReplaceChild("horse_body", CubeListBuilder.create(), PartPose.offset(0.0F, -26.0F, 3.0F));

        PartDefinition horse_belly_r1 = horse_body.addOrReplaceChild("horse_belly_r1", CubeListBuilder.create().texOffs(138, 3).addBox(-3.65F, -1.7F, -7.5F, 7.3F, 3.4F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.8F, 0.8F, -0.0524F, 0.0F, 0.0F));

        PartDefinition horse_haunches_r1 = horse_body.addOrReplaceChild("horse_haunches_r1", CubeListBuilder.create().texOffs(103, 3).addBox(-4.25F, -5.4F, -3.7F, 8.5F, 10.0F, 7.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.4F, 12.8F, 0.1571F, 0.0F, 0.0F));

        PartDefinition horse_shoulders_r1 = horse_body.addOrReplaceChild("horse_shoulders_r1", CubeListBuilder.create().texOffs(68, 3).addBox(-5.9F, -4.5F, -3.0F, 10.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9F, -2.5F, -10.0F, 0.2094F, 0.0F, 0.0F));

        PartDefinition horse_ribcage_r1 = horse_body.addOrReplaceChild("horse_ribcage_r1", CubeListBuilder.create().texOffs(3, 3).addBox(-4.5F, -5.25F, -11.0F, 9.0F, 10.5F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, -1.0F, -0.0524F, 0.0F, 0.0F));

        PartDefinition hip_tendon_2_r1 = horse_body.addOrReplaceChild("hip_tendon_2_r1", CubeListBuilder.create().texOffs(84, 6).mirror().addBox(-0.1F, -2.45F, -0.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.1F, -8.8F, -10.9F, -0.0899F, 0.0F, 2.8674F));

        PartDefinition hip_tendon_2_r2 = horse_body.addOrReplaceChild("hip_tendon_2_r2", CubeListBuilder.create().texOffs(160, 4).mirror().addBox(-0.85F, -7.45F, -0.35F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.85F, -10.8F, -7.4F, -0.609F, -0.0547F, 2.8929F));

        PartDefinition hip_tendon_1_r1 = horse_body.addOrReplaceChild("hip_tendon_1_r1", CubeListBuilder.create().texOffs(37, 7).addBox(-0.15F, -7.45F, -0.35F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.85F, -10.8F, -7.4F, -0.609F, 0.0547F, -2.8929F));

        PartDefinition hip_tendon_1_r2 = horse_body.addOrReplaceChild("hip_tendon_1_r2", CubeListBuilder.create().texOffs(61, 57).addBox(-0.9F, -2.45F, -0.1F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1F, -8.8F, -10.9F, -0.0899F, 0.0F, -2.8674F));

        PartDefinition front_left = horse_body.addOrReplaceChild("front_left", CubeListBuilder.create(), PartPose.offset(4.0F, -1.0F, -10.0F));

        PartDefinition front_left_thigh_r1 = front_left.addOrReplaceChild("front_left_thigh_r1", CubeListBuilder.create().texOffs(183, 1).addBox(-1.75F, -5.0F, -1.5F, 3.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, -0.75F, 0.1487F, 0.0F, 3.0916F));

        PartDefinition front_left_lower = front_left.addOrReplaceChild("front_left_lower", CubeListBuilder.create(), PartPose.offset(0.5F, 10.0F, -1.5F));

        PartDefinition front_left_cannon_r1 = front_left_lower.addOrReplaceChild("front_left_cannon_r1", CubeListBuilder.create().texOffs(199, 3).addBox(-1.35F, -2.85F, -0.6F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, 6.85F, -0.9F, 0.1306F, 0.0F, 3.127F));

        PartDefinition front_left_hoof = front_left_lower.addOrReplaceChild("front_left_hoof", CubeListBuilder.create(), PartPose.offset(0.2F, 8.9F, -1.0F));

        PartDefinition front_left_hoof_r1 = front_left_hoof.addOrReplaceChild("front_left_hoof_r1", CubeListBuilder.create().texOffs(217, 3).addBox(-1.7F, -1.9F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0F, -2.25F, 0.1222F, 0.0F, 0.0F));

        PartDefinition front_left_pastern_r1 = front_left_hoof.addOrReplaceChild("front_left_pastern_r1", CubeListBuilder.create().texOffs(208, 2).addBox(-0.2F, -1.3F, -0.6F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.9F, -1.9F, 0.3081F, 0.0F, -3.1416F));

        PartDefinition front_right = horse_body.addOrReplaceChild("front_right", CubeListBuilder.create(), PartPose.offset(-4.0F, -1.0F, -10.0F));

        PartDefinition front_right_thigh_r1 = front_right.addOrReplaceChild("front_right_thigh_r1", CubeListBuilder.create().texOffs(183, 1).mirror().addBox(-1.25F, -5.0F, -1.5F, 3.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.0F, -0.75F, 0.1487F, 0.0F, -3.0916F));

        PartDefinition front_right_lower = front_right.addOrReplaceChild("front_right_lower", CubeListBuilder.create(), PartPose.offset(-0.5F, 10.0F, -1.5F));

        PartDefinition front_right_cannon_r1 = front_right_lower.addOrReplaceChild("front_right_cannon_r1", CubeListBuilder.create().texOffs(198, 1).mirror().addBox(-0.65F, -2.85F, -0.6F, 2.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.4F, 6.85F, -0.9F, 0.1306F, 0.0F, -3.127F));

        PartDefinition front_right_hoof = front_right_lower.addOrReplaceChild("front_right_hoof", CubeListBuilder.create(), PartPose.offset(-0.2F, 8.9F, -1.0F));

        PartDefinition front_right_hoof_r1 = front_right_hoof.addOrReplaceChild("front_right_hoof_r1", CubeListBuilder.create().texOffs(217, 3).mirror().addBox(-1.3F, -1.9F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 7.0F, -2.25F, 0.1222F, 0.0F, 0.0F));

        PartDefinition front_right_pastern_r1 = front_right_hoof.addOrReplaceChild("front_right_pastern_r1", CubeListBuilder.create().texOffs(208, 2).mirror().addBox(-0.8F, -1.3F, -0.6F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.9F, -1.9F, 0.3081F, 0.0F, 3.1416F));

        PartDefinition hind_left = horse_body.addOrReplaceChild("hind_left", CubeListBuilder.create(), PartPose.offset(3.5F, -1.0F, 12.0F));

        PartDefinition hind_left_thigh_r1 = hind_left.addOrReplaceChild("hind_left_thigh_r1", CubeListBuilder.create().texOffs(69, 77).addBox(-2.5F, -5.0F, -3.4F, 4.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 4.5F, -1.6F, 0.3411F, 0.0F, 3.0861F));

        PartDefinition hind_left_lower = hind_left.addOrReplaceChild("hind_left_lower", CubeListBuilder.create(), PartPose.offset(0.5F, 9.0F, -3.2F));

        PartDefinition hind_left_cannon_r1 = hind_left_lower.addOrReplaceChild("hind_left_cannon_r1", CubeListBuilder.create().texOffs(50, 38).addBox(-0.6F, -4.25F, -0.6F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, 11.75F, 5.3F, 0.1062F, 0.0F, 3.1149F));

        PartDefinition hind_left_hock_r1 = hind_left_lower.addOrReplaceChild("hind_left_hock_r1", CubeListBuilder.create().texOffs(39, 37).addBox(-1.25F, -6.0F, -0.65F, 2.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 4.0F, 2.85F, -0.6181F, 0.0F, 3.0792F));

        PartDefinition hind_left_hoof = hind_left_lower.addOrReplaceChild("hind_left_hoof", CubeListBuilder.create(), PartPose.offset(0.7F, 16.9F, 4.7F));

        PartDefinition hind_left_hoof_r1 = hind_left_hoof.addOrReplaceChild("hind_left_hoof_r1", CubeListBuilder.create().texOffs(58, 39).addBox(-1.7F, -1.9F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, 0.0F, 0.1222F, 0.0F, 0.0F));

        PartDefinition hind_right = horse_body.addOrReplaceChild("hind_right", CubeListBuilder.create(), PartPose.offset(-3.5F, -1.0F, 12.0F));

        PartDefinition hind_right_thigh_r1 = hind_right.addOrReplaceChild("hind_right_thigh_r1", CubeListBuilder.create().texOffs(69, 77).mirror().addBox(-1.5F, -5.0F, -3.4F, 4.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.25F, 4.5F, -1.6F, 0.3411F, 0.0F, -3.0861F));

        PartDefinition hind_right_lower = hind_right.addOrReplaceChild("hind_right_lower", CubeListBuilder.create(), PartPose.offset(-0.5F, 9.0F, -3.2F));

        PartDefinition hind_right_cannon_r1 = hind_right_lower.addOrReplaceChild("hind_right_cannon_r1", CubeListBuilder.create().texOffs(50, 38).mirror().addBox(-0.4F, -4.25F, -0.6F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.6F, 11.75F, 5.3F, 0.1062F, 0.0F, -3.1149F));

        PartDefinition hind_right_hock_r1 = hind_right_lower.addOrReplaceChild("hind_right_hock_r1", CubeListBuilder.create().texOffs(39, 37).mirror().addBox(-0.75F, -6.0F, -0.65F, 2.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.25F, 4.0F, 2.85F, -0.6181F, 0.0F, -3.0792F));

        PartDefinition hind_right_hoof = hind_right_lower.addOrReplaceChild("hind_right_hoof", CubeListBuilder.create(), PartPose.offset(-0.7F, 16.9F, 4.7F));

        PartDefinition hind_right_hoof_r1 = hind_right_hoof.addOrReplaceChild("hind_right_hoof_r1", CubeListBuilder.create().texOffs(58, 39).mirror().addBox(-1.3F, -1.9F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.25F, 0.0F, 0.0F, 0.1222F, 0.0F, 0.0F));

        PartDefinition horse_neck = horse_body.addOrReplaceChild("horse_neck", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -12.0F));

        PartDefinition horse_lower_neck_r1 = horse_neck.addOrReplaceChild("horse_lower_neck_r1", CubeListBuilder.create().texOffs(143, 38).addBox(-1.75F, -3.1785F, -2.05F, 3.5F, 6.5285F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.65F, -9.7F, 1.0222F, 0.0F, 3.1416F));

        PartDefinition horse_upper_neck_r1 = horse_neck.addOrReplaceChild("horse_upper_neck_r1", CubeListBuilder.create().texOffs(118, 37).addBox(-2.5F, -4.0F, -2.5F, 5.0F, 8.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.5F, -3.5F, 1.1659F, 0.0F, 3.1416F));

        PartDefinition horse_head = horse_neck.addOrReplaceChild("horse_head", CubeListBuilder.create(), PartPose.offset(0.0F, 5.0F, -13.0F));

        PartDefinition horse_ear_n2_r1 = horse_head.addOrReplaceChild("horse_ear_n2_r1", CubeListBuilder.create().texOffs(209, 39).mirror().addBox(-1.725F, -3.0F, -0.6F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.775F, -5.0F, 1.1F, -0.1475F, 0.0F, -0.1366F));

        PartDefinition horse_ear_n1_r1 = horse_head.addOrReplaceChild("horse_ear_n1_r1", CubeListBuilder.create().texOffs(209, 39).addBox(-0.275F, -3.0F, -0.6F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.775F, -5.0F, 1.1F, -0.1475F, 0.0F, 0.1366F));

        PartDefinition horse_muzzle_r1 = horse_head.addOrReplaceChild("horse_muzzle_r1", CubeListBuilder.create().texOffs(185, 39).addBox(-1.5F, -1.9F, -4.2F, 3.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.9F, -4.3F, 0.6632F, 0.0F, 0.0F));

        PartDefinition horse_cranium_r1 = horse_head.addOrReplaceChild("horse_cranium_r1", CubeListBuilder.create().texOffs(162, 38).addBox(-2.0F, -2.7F, -2.2F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.3F, -0.8F, 0.6632F, 0.0F, 0.0F));

        PartDefinition tail = horse_body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.2F, 16.2F));

        PartDefinition tail_tip_r1 = tail.addOrReplaceChild("tail_tip_r1", CubeListBuilder.create().texOffs(240, 38).addBox(-0.45F, -3.5F, -0.3F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.55F, 19.2F, 6.35F, -0.3465F, 0.0F, -3.0543F));

        PartDefinition tail_mid_r1 = tail.addOrReplaceChild("tail_mid_r1", CubeListBuilder.create().texOffs(232, 39).addBox(-1.4F, -5.75F, -0.55F, 2.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 10.2F, 3.85F, -0.2056F, 0.0F, -3.0222F));

        PartDefinition tail_root_r1 = tail.addOrReplaceChild("tail_root_r1", CubeListBuilder.create().texOffs(222, 39).addBox(-1.75F, -2.9F, -0.6F, 4.0F, 6.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.6F, 1.4F, -0.4939F, 0.0F, 3.1416F));

        PartDefinition human_torso = horse_body.addOrReplaceChild("human_torso", CubeListBuilder.create(), PartPose.offset(0.0F, -5.0F, -10.0F));

        PartDefinition human_ribcage_r1 = human_torso.addOrReplaceChild("human_ribcage_r1", CubeListBuilder.create().texOffs(140, 139).addBox(-8.3F, -5.55F, -2.2F, 8.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3F, -18.2F, -1.8F, 0.1047F, 0.0F, 0.0F));

        PartDefinition human_abdomen_r1 = human_torso.addOrReplaceChild("human_abdomen_r1", CubeListBuilder.create().texOffs(178, 127).addBox(-3.0F, -3.75F, -0.9F, 5.0F, 7.5F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -10.0F, -1.35F, 0.0873F, 0.0F, 0.0F));

        PartDefinition fused_waist_r1 = human_torso.addOrReplaceChild("fused_waist_r1", CubeListBuilder.create().texOffs(175, 147).addBox(-3.2F, -3.25F, -2.75F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -4.0F, -0.25F, 0.1396F, 0.0F, 0.0F));

        PartDefinition human_head = human_torso.addOrReplaceChild("human_head", CubeListBuilder.create(), PartPose.offset(0.0F, -23.2F, -2.4F));

        PartDefinition human_skull_r1 = human_head.addOrReplaceChild("human_skull_r1", CubeListBuilder.create().texOffs(151, 166).addBox(-5.5F, -5.2F, -1.85F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, -4.6F, -3.25F, 0.2094F, 0.0F, 0.0F));

        PartDefinition human_neck_r1 = human_head.addOrReplaceChild("human_neck_r1", CubeListBuilder.create().texOffs(172, 98).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.8F, -0.6F, 0.1732F, 0.0F, 0.0F));

        PartDefinition left_arm = human_torso.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(4.0F, -22.0F, -2.0F));

        PartDefinition left_upper_arm_r1 = left_arm.addOrReplaceChild("left_upper_arm_r1", CubeListBuilder.create().texOffs(132, 55).addBox(-0.925F, -5.7616F, -1.15F, 2.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.45F, 0.3F, -5.1F, 1.4486F, 0.0F, 2.0701F));

        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create(), PartPose.offset(1.0F, 0.6F, -10.2F));

        PartDefinition left_forearm_r1 = left_forearm.addOrReplaceChild("left_forearm_r1", CubeListBuilder.create().texOffs(144, 55).addBox(-1.15F, -5.4988F, -0.9F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 0.05F, -5.4F, 1.5501F, 0.0F, 2.0344F));

        PartDefinition fused_bow = left_forearm.addOrReplaceChild("fused_bow", CubeListBuilder.create(), PartPose.offset(0.2F, 0.1F, -10.8F));

        PartDefinition bow_barb_1_r1 = fused_bow.addOrReplaceChild("bow_barb_1_r1", CubeListBuilder.create().texOffs(231, 56).addBox(-0.25F, -2.0236F, -0.325F, 0.5F, 4.0471F, 0.65F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 9.7F, -5.1F, 1.0839F, 0.0F, -3.1416F));

        PartDefinition bow_barb_0_r1 = fused_bow.addOrReplaceChild("bow_barb_0_r1", CubeListBuilder.create().texOffs(225, 56).addBox(-0.25F, -1.9581F, -0.325F, 0.5F, 3.9162F, 0.65F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.95F, -5.1F, 1.1553F, 0.0F, 0.0F));

        PartDefinition bow_lower_3_r1 = fused_bow.addOrReplaceChild("bow_lower_3_r1", CubeListBuilder.create().texOffs(219, 55).addBox(-0.25F, -2.6016F, -0.225F, 0.5F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 17.9F, -0.05F, -0.8008F, 0.0F, -3.1416F));

        PartDefinition bow_lower_2_r1 = fused_bow.addOrReplaceChild("bow_lower_2_r1", CubeListBuilder.create().texOffs(212, 55).addBox(-0.525F, -2.6284F, -0.35F, 1.0F, 5.2567F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 14.0F, -2.75F, -0.4283F, 0.0F, -3.1416F));

        PartDefinition bow_lower_1_r1 = fused_bow.addOrReplaceChild("bow_lower_1_r1", CubeListBuilder.create().texOffs(205, 56).addBox(-0.35F, -2.8295F, -0.5F, 1.0F, 5.6589F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.15F, 9.0F, -3.4F, 0.1471F, 0.0F, -3.1416F));

        PartDefinition bow_lower_0_r1 = fused_bow.addOrReplaceChild("bow_lower_0_r1", CubeListBuilder.create().texOffs(196, 56).addBox(-0.85F, -3.03F, -0.625F, 1.7F, 6.06F, 1.25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.65F, -1.75F, 0.4408F, 0.0F, -3.1416F));

        PartDefinition bow_upper_3_r1 = fused_bow.addOrReplaceChild("bow_upper_3_r1", CubeListBuilder.create().texOffs(191, 56).addBox(-0.25F, -2.3299F, -0.225F, 0.5F, 4.6598F, 0.45F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -17.7F, -0.05F, -0.833F, 0.0F, 0.0F));

        PartDefinition bow_upper_2_r1 = fused_bow.addOrReplaceChild("bow_upper_2_r1", CubeListBuilder.create().texOffs(183, 56).addBox(-0.525F, -2.5829F, -0.35F, 1.0F, 5.1659F, 0.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.95F, -2.75F, -0.4366F, 0.0F, 0.0F));

        PartDefinition bow_upper_1_r1 = fused_bow.addOrReplaceChild("bow_upper_1_r1", CubeListBuilder.create().texOffs(176, 56).addBox(-0.65F, -2.879F, -0.5F, 1.3F, 5.7579F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.95F, -3.4F, 0.1444F, 0.0F, 0.0F));

        PartDefinition bow_upper_0_r1 = fused_bow.addOrReplaceChild("bow_upper_0_r1", CubeListBuilder.create().texOffs(167, 56).addBox(-0.85F, -3.03F, -0.625F, 1.7F, 6.06F, 1.25F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.55F, -1.75F, 0.4408F, 0.0F, 0.0F));

        PartDefinition grown_bow_junction_r1 = fused_bow.addOrReplaceChild("grown_bow_junction_r1", CubeListBuilder.create().texOffs(155, 56).addBox(-1.0F, -1.8F, -1.2F, 2.0F, 3.6F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.0873F, 0.0F, 0.0F));

        PartDefinition bowstring = fused_bow.addOrReplaceChild("bowstring", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -0.5F));

        PartDefinition string_lower_r1 = bowstring.addOrReplaceChild("string_lower_r1", CubeListBuilder.create().texOffs(27, 75).addBox(-0.05F, -12.135F, -0.05F, 0.1F, 24.27F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.275F, 9.75F, 9.15F, 0.6259F, 0.0F, 3.1134F));

        PartDefinition string_upper_r1 = bowstring.addOrReplaceChild("string_upper_r1", CubeListBuilder.create().texOffs(23, 75).addBox(-0.05F, -12.0138F, -0.05F, 0.1F, 24.0276F, 0.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.275F, -9.6F, 9.15F, -0.6332F, 0.0F, -3.113F));

        PartDefinition arrow = bowstring.addOrReplaceChild("arrow", CubeListBuilder.create(), PartPose.offset(-0.55F, 0.0F, 16.2F));

        PartDefinition arrow_point_b_r1 = arrow.addOrReplaceChild("arrow_point_b_r1", CubeListBuilder.create().texOffs(40, 75).addBox(-0.1F, -0.6853F, -0.14F, 0.2F, 1.3705F, 0.28F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, -0.2F, -34.45F, 1.222F, 0.0F, -3.1416F));

        PartDefinition arrow_point_a_r1 = arrow.addOrReplaceChild("arrow_point_a_r1", CubeListBuilder.create().texOffs(35, 75).addBox(-0.15F, -1.7124F, -0.225F, 0.3F, 3.4249F, 0.45F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.55F, -0.2F, -32.3F, 1.4464F, 0.0F, 0.0F));

        PartDefinition arrow_shaft_r1 = arrow.addOrReplaceChild("arrow_shaft_r1", CubeListBuilder.create().texOffs(31, 75).addBox(-0.085F, -15.9524F, -0.085F, 0.17F, 31.9048F, 0.17F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.275F, 0.0F, -15.85F, 1.5534F, 0.0F, 1.5708F));

        PartDefinition right_arm = human_torso.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-4.0F, -22.0F, -1.5F));

        PartDefinition right_upper_arm_r1 = right_arm.addOrReplaceChild("right_upper_arm_r1", CubeListBuilder.create().texOffs(237, 56).addBox(-1.25F, -4.7678F, -1.05F, 2.5F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.95F, -1.5F, -2.35F, 0.6049F, 0.0F, -1.1137F));

        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create(), PartPose.offset(-6.0F, -3.0F, -4.7F));

        PartDefinition right_drawing_forearm_r1 = right_forearm.addOrReplaceChild("right_drawing_forearm_r1", CubeListBuilder.create().texOffs(2, 74).addBox(-1.15F, -7.5144F, -0.85F, 2.0F, 15.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.15F, 1.85F, -0.05F, 0.0068F, 0.0F, 1.824F));

        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create(), PartPose.offset(14.3F, 3.7F, -0.1F));

        PartDefinition right_drawing_hand_r1 = right_hand.addOrReplaceChild("right_drawing_hand_r1", CubeListBuilder.create().texOffs(13, 75).addBox(-0.6F, -0.85F, -1.1F, 1.2F, 1.7F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 0.0F, -0.5F, 0.1396F, 0.2094F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(ExcoriatedEntity entity, float swing, float amount, float age, float headYaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partial = age - entity.tickCount;
        boolean wading = entity.getFluidTypeHeight(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get()) > .1;
        float stride = entity.clotted() ? .25F : wading ? .4F : .6F;
        front_left.xRot = Mth.cos(swing * .65F) * amount * stride;
        front_right.xRot = Mth.cos(swing * .65F + Mth.PI) * amount * stride;
        hind_left.xRot = Mth.cos(swing * .65F + Mth.PI) * amount * stride;
        hind_right.xRot = Mth.cos(swing * .65F) * amount * stride;
        if (wading) {
            front_left_lower.xRot = -Math.max(0, front_left.xRot) * .6F;
            front_right_lower.xRot = -Math.max(0, front_right.xRot) * .6F;
            hind_left_lower.xRot = -Math.max(0, hind_left.xRot) * .6F;
            hind_right_lower.xRot = -Math.max(0, hind_right.xRot) * .6F;
        }
        horse_body.y += Mth.sin(swing * 1.3F) * amount * .6F;
        horse_neck.xRot = Mth.sin(age * .05F) * .04F;
        tail.zRot = Mth.sin(age * .06F) * .13F;
        human_head.yRot = headYaw * Mth.DEG_TO_RAD * .65F;
        human_torso.yRot = headYaw * Mth.DEG_TO_RAD * .35F;
        human_head.xRot = pitch * Mth.DEG_TO_RAD;
        int state = entity.attackState();
        float time = entity.attackTime() + partial;
        float progress = Mth.clamp(time / 20F, 0, 1);
        float ready = armReadiness(state, time);
        float draw = state == ExcoriatedEntity.DRAW ? progress : Math.max(0, 1 - time / 5F);
        if (state != ExcoriatedEntity.DRAW && state != ExcoriatedEntity.FIRE) draw = 0;
        human_torso.xRot = pitch * Mth.DEG_TO_RAD * ready;
        human_head.xRot *= 1 - ready;
        animateArms(ready, draw, age, swing, amount);
        arrow.visible = state == ExcoriatedEntity.DRAW && ready >= .99F;
        if (state == ExcoriatedEntity.REAR || state == ExcoriatedEntity.BREATH) {
            float rear = state == ExcoriatedEntity.REAR ? progress : 1 - Mth.clamp(time / 12F, 0, 1);
            horse_body.xRot = -.35F * rear;
            front_left.xRot = -.9F * rear;
            front_right.xRot = -.9F * rear;
            human_head.xRot = -.2F;
        }
        if (entity.clotted()) {
            horse_body.y += 1.2F;
            human_torso.xRot += .12F;
            human_head.xRot += .2F;
        }
        if (entity.deathTime > 0) {
            human_head.xRot = .8F;
            human_torso.xRot += .2F;
        }
    }

    static float armReadiness(int state, float time) {
        float t = switch (state) {
            case ExcoriatedEntity.DRAW -> Mth.clamp(time / 8F, 0, 1);
            case ExcoriatedEntity.FIRE -> 1 - Mth.clamp((time - 2) / 6F, 0, 1);
            default -> 0;
        };
        return t * t * (3 - 2 * t);
    }

    /** Extend the relaxed sinews without thickening them or scaling the fused fused_bow. */
    void animateArms(float ready, float draw, float age, float swing, float amount) {
        animateDraw(draw);
        float sway = Mth.sin(age * .035F) * .015F + Mth.sin(swing * .65F) * amount * .025F;
        Vector3f leftUpper = new Vector3f(left_forearm.x, left_forearm.y, left_forearm.z);
        Vector3f leftLower = new Vector3f(fused_bow.x, fused_bow.y, fused_bow.z);
        Quaternionf leftRest = new Quaternionf().rotationTo(leftUpper, new Vector3f(.25F, 1, .08F + sway));
        Vector3f leftLowerRest = new Vector3f(.035F, 1, .12F + sway).rotate(new Quaternionf(leftRest).conjugate());
        Quaternionf leftForeRest = new Quaternionf().rotationTo(leftLower, leftLowerRest);
        Quaternionf rightRest = new Quaternionf().rotationTo(upperBind, new Vector3f(-.28F, 1, .10F - sway));
        Vector3f rightLowerRest = new Vector3f(-.045F, 1, .09F - sway).rotate(new Quaternionf(rightRest).conjugate());
        Quaternionf rightForeRest = new Quaternionf().rotationTo(lowerBind, rightLowerRest);
        Quaternionf bowRest = new Quaternionf(leftRest).mul(leftForeRest).conjugate()
                .mul(new Quaternionf().rotationX(Mth.HALF_PI - .25F));
        blendRotation(left_arm, leftRest, ready);
        blendRotation(left_forearm, leftForeRest, ready);
        blendRotation(right_arm, rightRest, ready);
        blendRotation(right_forearm, rightForeRest, ready);
        blendRotation(fused_bow, bowRest, ready);
        float extension = Mth.lerp(ready, 2.3F, 1);
        left_arm.xScale = left_arm.yScale = left_arm.zScale = extension;
        right_arm.xScale = right_arm.yScale = right_arm.zScale = extension;
        float inverseExtension = 1 / extension;
        ModelPart leftUpperArm = left_arm.getChild("left_upper_arm_r1");
        leftUpperArm.xScale = leftUpperArm.zScale = inverseExtension;
        ModelPart leftLowerArm = left_forearm.getChild("left_forearm_r1");
        leftLowerArm.xScale = leftLowerArm.zScale = inverseExtension;
        ModelPart rightUpperArm = right_arm.getChild("right_upper_arm_r1");
        rightUpperArm.xScale = rightUpperArm.zScale = inverseExtension;
        ModelPart rightLowerArm = right_forearm.getChild("right_drawing_forearm_r1");
        rightLowerArm.xScale = rightLowerArm.zScale = inverseExtension;
        fused_bow.xScale = fused_bow.yScale = fused_bow.zScale = 1 / extension;
        right_hand.xScale = right_hand.yScale = right_hand.zScale = 1 / extension;
        Vector3f nock = new Vector3f(0, 0, 2.1F).lerp(new Vector3f(nockBind).add(0, 0, -6F * (1 - draw)), ready);
        stringSegment(string_upper, topTip, nock, topLength);
        stringSegment(string_lower, nock, bottomTip, bottomLength);
    }

    private static void blendRotation(ModelPart part, Quaternionf resting, float ready) {
        Quaternionf aiming = new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot);
        orient(part, resting.slerp(aiming, ready));
    }

    /** The authored arm is already aimed; solve only its change in string reach. */
    void animateDraw(float draw) {
        Vector3f nock = new Vector3f(nockBind).add(0, 0, -6F * (1 - draw));
        stringSegment(string_upper, topTip, nock, topLength);
        stringSegment(string_lower, nock, bottomTip, bottomLength);
        arrow.z = nock.z;
        Vector3f arrowDirection = new Vector3f(-.55F, 0, -31.7F);
        orient(arrow, new Quaternionf().rotationTo(arrowDirection, new Vector3f(-.55F, 0, -31.7F + 6F * (1 - draw))));
        PoseStack local = new PoseStack();
        left_arm.translateAndRotate(local);
        left_forearm.translateAndRotate(local);
        fused_bow.translateAndRotate(local);
        bowstring.translateAndRotate(local);
        Vector3f target = local.last().pose().transformPosition(new Vector3f(nock).div(16)).mul(16);
        // Authored fingers sit just behind the nock, rather than inside the string.
        target.add(.35F, 0, 1).sub(right_arm.x, right_arm.y, right_arm.z);
        float upper = upperBind.length(), lower = lowerBind.length();
        float distance = Mth.clamp(target.length(), Math.abs(upper - lower) + .001F, upper + lower - .001F);
        Vector3f direction = new Vector3f(target).normalize();
        Vector3f bend = new Vector3f(upperBind).sub(new Vector3f(direction).mul(upperBind.dot(direction))).normalize();
        float along = (upper * upper - lower * lower + distance * distance) / (2 * distance);
        Vector3f elbow = new Vector3f(direction).mul(along).add(bend.mul((float) Math.sqrt(Math.max(0, upper * upper - along * along))));
        Quaternionf upperRotation = new Quaternionf().rotationTo(upperBind, elbow);
        orient(right_arm, upperRotation);
        Vector3f lowerDirection = new Vector3f(direction).mul(distance).sub(elbow).rotate(new Quaternionf(upperRotation).conjugate());
        orient(right_forearm, new Quaternionf().rotationTo(lowerBind, lowerDirection));
    }

    private static void stringSegment(ModelPart part, Vector3f from, Vector3f to, float bindLength) {
        Vector3f center = new Vector3f(from).add(to).mul(.5F);
        part.setPos(center.x, center.y, center.z);
        Vector3f direction = new Vector3f(to).sub(from);
        part.yScale = (direction.length() + .2F) / bindLength;
        orient(part, new Quaternionf().rotationTo(new Vector3f(0, 1, 0), direction));
    }

    private static void orient(ModelPart part, Quaternionf rotation) {
        // JOML 1.10.5 has the wrong sign in its ZYX X-angle denominator.
        float x = rotation.x, y = rotation.y, z = rotation.z, w = rotation.w;
        part.setRotation((float) Math.atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y)),
                (float) Math.asin(Mth.clamp(2 * (w * y - z * x), -1, 1)),
                (float) Math.atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z)));
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(pose, buffer, light, overlay, color);
    }

    public void renderVessels(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        pose.pushPose();
        root.translateAndRotate(pose);
        horse_body.translateAndRotate(pose);
        // Keep the pulse on the exposed belly; the haunches use the normal lit texture.
        horse_body.getChild("horse_belly_r1").render(pose, buffer, light, overlay, color);
        pose.popPose();
    }

    public void translateToBow(PoseStack pose) {
        root.translateAndRotate(pose);
        horse_body.translateAndRotate(pose);
        human_torso.translateAndRotate(pose);
        left_arm.translateAndRotate(pose);
        left_forearm.translateAndRotate(pose);
        fused_bow.translateAndRotate(pose);
    }
}



