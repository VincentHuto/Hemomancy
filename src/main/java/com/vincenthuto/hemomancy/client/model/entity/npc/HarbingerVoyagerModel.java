package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class HarbingerVoyagerModel<T extends HarbingerVoyagerEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("harbinger_voyager"), "main");
    private final ModelPart whole;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart bone2;
    private final ModelPart bone;
    private final ModelPart body;
    private final ModelPart lamp;
    private final ModelPart rope2;
    private final ModelPart bone3;
    private final ModelPart bone4;
    private final ModelPart gourd;
    private final ModelPart rope4;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart harpoon;
    private final ModelPart chain;
    private final ModelPart SideclothR;
    private final ModelPart SideclothR4;
    private final ModelPart SideclothR5;
    private final ModelPart SideclothR6;
    private final ModelPart SideclothL;
    private final ModelPart SideclothR1;
    private final ModelPart SideclothR2;
    private final ModelPart SideclothR3;
    private final ModelPart ClothBack;
    private final ModelPart ClothBack1;
    private final ModelPart ClothBackR1;
    private final ModelPart ClothBackL1;
    private final ModelPart ClothBack2;
    private final ModelPart ClothBackR2;
    private final ModelPart ClothBackR3;
    private final ModelPart ClothBackL2;
    private final ModelPart ClothBackL3;
    private final ModelPart leftArm;
    private final ModelPart body2;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public HarbingerVoyagerModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.head = this.whole.getChild("head");
        this.hat = this.head.getChild("hat");
        this.bone2 = this.hat.getChild("bone2");
        this.bone = this.hat.getChild("bone");
        this.body = this.whole.getChild("body");
        this.lamp = this.body.getChild("lamp");
        this.rope2 = this.lamp.getChild("rope2");
        this.bone3 = this.rope2.getChild("bone3");
        this.bone4 = this.rope2.getChild("bone4");
        this.gourd = this.body.getChild("gourd");
        this.rope4 = this.gourd.getChild("rope4");
        this.bone5 = this.rope4.getChild("bone5");
        this.bone6 = this.rope4.getChild("bone6");
        this.harpoon = this.body.getChild("harpoon");
        this.chain = this.harpoon.getChild("chain");
        this.SideclothR = this.body.getChild("SideclothR");
        this.SideclothR4 = this.SideclothR.getChild("SideclothR4");
        this.SideclothR5 = this.SideclothR4.getChild("SideclothR5");
        this.SideclothR6 = this.SideclothR5.getChild("SideclothR6");
        this.SideclothL = this.body.getChild("SideclothL");
        this.SideclothR1 = this.SideclothL.getChild("SideclothR1");
        this.SideclothR2 = this.SideclothR1.getChild("SideclothR2");
        this.SideclothR3 = this.SideclothR2.getChild("SideclothR3");
        this.ClothBack = this.body.getChild("ClothBack");
        this.ClothBack1 = this.ClothBack.getChild("ClothBack1");
        this.ClothBackR1 = this.ClothBack1.getChild("ClothBackR1");
        this.ClothBackL1 = this.ClothBack1.getChild("ClothBackL1");
        this.ClothBack2 = this.ClothBack1.getChild("ClothBack2");
        this.ClothBackR2 = this.ClothBack2.getChild("ClothBackR2");
        this.ClothBackR3 = this.ClothBack2.getChild("ClothBackR3");
        this.ClothBackL2 = this.ClothBack2.getChild("ClothBackL2");
        this.ClothBackL3 = this.ClothBack2.getChild("ClothBackL3");
        this.leftArm = this.whole.getChild("leftArm");
        this.body2 = this.leftArm.getChild("body2");
        this.rightArm = this.whole.getChild("rightArm");
        this.leftLeg = this.whole.getChild("leftLeg");
        this.rightLeg = this.whole.getChild("rightLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create().texOffs(78, 2).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.1F, 0.0F));

        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -7.5382F, -2.1563F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r1 = hat.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(59, 119).addBox(-4.0F, -2.0F, 2.5F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 1.6176F, -6.037F, 0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r2 = hat.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(91, 89).addBox(-5.0F, -2.0F, -2.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.1F))
                .texOffs(109, 77).addBox(-12.8F, -2.0F, -3.5F, 1.0F, 3.0F, 8.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(8.4F, 1.2882F, 2.6563F, 0.2618F, 0.0F, 0.0F));

        PartDefinition cube_r3 = hat.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(101, 105).addBox(-4.0F, -1.0F, 0.5F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -0.6618F, 2.8563F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone2 = hat.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -0.4323F, 2.1733F, -0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(89, 114).addBox(-4.0F, -1.0F, -5.5F, 8.0F, 2.0F, 9.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -0.5294F, 0.483F, 0.2618F, 0.0F, 0.0F));

        PartDefinition bone = hat.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, -1.6118F, 2.6563F));

        PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(100, 52).mirror().addBox(-1.0F, -1.5F, -5.0F, 2.0F, 3.0F, 12.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(3.9023F, 0.7759F, -1.4686F, 0.2657F, 0.1685F, 0.0456F));

        PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(66, 70).addBox(-1.0F, -1.5F, -5.0F, 2.0F, 3.0F, 12.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-3.9023F, 0.7759F, -1.4686F, 0.2657F, -0.1685F, -0.0456F));

        PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 73).addBox(-4.0F, -1.0F, -2.0F, 8.0F, 13.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(3.3498F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-4.2923F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(4, 93).addBox(-3.9F, -0.5F, 1.85F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 85).addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ClothchestR_108_38_cf815bb5_r1 = body.addOrReplaceChild("ClothchestR_108_38_cf815bb5_r1", CubeListBuilder.create().texOffs(37, 68).addBox(2.1F, 0.5F, -3.5F, 2.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 36).addBox(-4.1F, 0.5F, -3.5F, 2.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.75F, 0.0F, 0.0F, 0.0F));

        PartDefinition lamp = body.addOrReplaceChild("lamp", CubeListBuilder.create().texOffs(78, 95).addBox(-1.5F, -2.7222F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(85, 99).addBox(-1.5F, -0.7222F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(66, 101).addBox(-1.5F, -3.7222F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F))
                .texOffs(66, 106).addBox(-1.5F, -2.2222F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(-5.75F, 12.5722F, 0.75F, 0.7418F, 0.0F, 0.0F));

        PartDefinition rope2 = lamp.addOrReplaceChild("rope2", CubeListBuilder.create().texOffs(78, 103).addBox(-1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.05F))
                .texOffs(78, 106).addBox(-1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.05F)), PartPose.offset(0.1F, -0.8389F, 0.0333F));

        PartDefinition bone3 = rope2.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(78, 99).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(78, 101).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.3F, -0.0333F, 1.0667F, 0.0F, -0.3927F, 0.0F));

        PartDefinition bone4 = rope2.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(78, 100).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
                .texOffs(78, 102).addBox(0.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(-1.3F, -0.0333F, -1.0333F, -0.0319F, -0.0064F, -0.0646F));

        PartDefinition gourd = body.addOrReplaceChild("gourd", CubeListBuilder.create(), PartPose.offsetAndRotation(5.25F, 12.5722F, 0.65F, 0.7418F, 0.0F, 0.0F));

        PartDefinition rope4 = gourd.addOrReplaceChild("rope4", CubeListBuilder.create(), PartPose.offset(-0.1F, -0.8389F, 0.0333F));

        PartDefinition bone5 = rope4.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(1.3F, -0.0333F, 1.0667F, 0.0F, 0.3927F, 0.0F));

        PartDefinition bone6 = rope4.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(1.3F, -0.0333F, -1.0333F, -0.0319F, 0.0064F, 0.0646F));

        PartDefinition harpoon = body.addOrReplaceChild("harpoon", CubeListBuilder.create().texOffs(0, 106).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, 72).addBox(-0.5F, -16.0F, -0.4F, 1.0F, 32.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(32, 73).addBox(-1.5F, -15.0F, 0.1F, 1.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(32, 87).addBox(-2.5F, -13.0F, 0.1F, 1.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 3.75F, 0.0F, 0.0F, -0.6981F));

        PartDefinition chain = harpoon.addOrReplaceChild("chain", CubeListBuilder.create().texOffs(17, 106).addBox(-2.0F, -1.25F, -1.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 106).addBox(-5.0F, -0.25F, -1.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 106).addBox(-4.0F, -1.0F, -1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(18, 106).addBox(-4.0F, 1.75F, -1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 115).addBox(-1.0F, -2.25F, -1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 118).addBox(0.0F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 9.0F, 0.0F, 0.0F, 0.0F, 0.6981F));

        PartDefinition SideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(), PartPose.offset(-3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR4 = SideclothR.addOrReplaceChild("SideclothR4", CubeListBuilder.create().texOffs(57, 57).mirror().addBox(-1.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition SideclothR5 = SideclothR4.addOrReplaceChild("SideclothR5", CubeListBuilder.create().texOffs(63, 24).mirror().addBox(-0.291F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, 0.2967F));

        PartDefinition SideclothR6 = SideclothR5.addOrReplaceChild("SideclothR6", CubeListBuilder.create().texOffs(63, 33).mirror().addBox(-0.866F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR1 = SideclothL.addOrReplaceChild("SideclothR1", CubeListBuilder.create().texOffs(57, 57).addBox(0.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

        PartDefinition SideclothR2 = SideclothR1.addOrReplaceChild("SideclothR2", CubeListBuilder.create().texOffs(63, 24).addBox(-0.709F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, -0.2967F));

        PartDefinition SideclothR3 = SideclothR2.addOrReplaceChild("SideclothR3", CubeListBuilder.create().texOffs(63, 33).addBox(-0.134F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(0.0F, 12.3F, 4.4F));

        PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, -1.0F));

        PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(26, 60).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(63, 42).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 8.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition ClothBackR2 = ClothBack2.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(58, 68).addBox(-1.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackR3 = ClothBack2.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(37, 13).addBox(-4.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL2 = ClothBack2.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(63, 68).addBox(-4.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL3 = ClothBack2.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(63, 52).addBox(-3.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition leftArm = whole.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(39, 1).addBox(-0.5F, 2.5F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(48, 24).addBox(-0.5F, 7.5F, 2.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(58, 9).addBox(0.0F, 5.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r1 = leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r1", CubeListBuilder.create().texOffs(80, 36).addBox(-0.5F, -2.5F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0637F, 1.2136F, 0.0125F, 0.0F, 0.0F, -1.1345F));

        PartDefinition ShoulderR_16_45_0eedefc6_r2 = leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r2", CubeListBuilder.create().texOffs(78, 54).addBox(-0.5F, 0.5F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.125F)), PartPose.offsetAndRotation(0.3564F, 1.4087F, -0.0125F, 0.0F, 0.0F, -1.1345F));

        PartDefinition ShoulderR_16_45_0eedefc6_r3 = leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r3", CubeListBuilder.create().texOffs(102, 36).addBox(-1.5F, -3.5F, -2.4875F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0625F)), PartPose.offsetAndRotation(0.2159F, 2.6021F, -0.025F, 0.0F, 0.0F, 0.0F));

        PartDefinition body2 = leftArm.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(-5.0F, -2.0F, 1.0F));

        PartDefinition rightArm = whole.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(43, 31).addBox(-3.5F, 2.5F, -2.5F, 4.0F, 8.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-3.5F, 7.5F, 2.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(67, 9).addBox(-3.0F, 5.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r4 = rightArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r4", CubeListBuilder.create().texOffs(102, 36).mirror().addBox(-3.5F, -3.5F, -2.4875F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0625F)).mirror(false), PartPose.offsetAndRotation(-0.2159F, 2.6021F, -0.025F, 0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r5 = rightArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r5", CubeListBuilder.create().texOffs(78, 54).mirror().addBox(-3.5F, 0.5F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.125F)).mirror(false), PartPose.offsetAndRotation(-0.3564F, 1.4087F, -0.0125F, 0.0F, 0.0F, 1.1345F));

        PartDefinition ShoulderR_16_45_0eedefc6_r6 = rightArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r6", CubeListBuilder.create().texOffs(80, 36).mirror().addBox(-3.5F, -2.5F, -2.5F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(-0.0637F, 1.2136F, 0.0125F, 0.0F, 0.0F, 1.1345F));

        PartDefinition leftLeg = whole.addOrReplaceChild("leftLeg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition right_leg_0_16_c9b3f04f_r1 = leftLeg.addOrReplaceChild("right_leg_0_16_c9b3f04f_r1", CubeListBuilder.create().texOffs(25, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition rightLeg = whole.addOrReplaceChild("rightLeg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition left_leg_0_16_90ee26c7_r1 = rightLeg.addOrReplaceChild("left_leg_0_16_90ee26c7_r1", CubeListBuilder.create().texOffs(0, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        whole.render(poseStack, buffer, packedLight, packedOverlay);

    }

    protected float rotlerpRad(float angle, float maxAngle, float mul) {
        float f = (mul - maxAngle) % ((float) Math.PI * 2F);
        if (f < -(float) Math.PI) {
            f += ((float) Math.PI * 2F);
        }

        if (f >= (float) Math.PI) {
            f -= ((float) Math.PI * 2F);
        }

        return maxAngle + angle * f;
    }

    @Override
    public void setupAnim(HarbingerVoyagerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.body.yRot = 0.0F;
        this.rightArm.z = 0.0F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        float f = 1.0F;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
        this.rightLeg.yRot = 0.005F;
        this.leftLeg.yRot = -0.005F;
        this.rightLeg.zRot = 0.005F;
        this.leftLeg.zRot = -0.005F;

        float frame = entity.tickCount + HLClientUtils.getPartialTicks();
        float idleWave = Mth.sin(frame * 0.067F);
        float walkWave = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;

        // Back cloth panels - x axis flutter
        this.ClothBackR1.xRot = 0.1047F + idleWave * 0.03F + walkWave * 0.07F;
        this.ClothBackR2.xRot = 0.2269F + Mth.sin(frame * 0.067F + 0.6F) * 0.035F + walkWave * 0.08F;
        this.ClothBackR3.xRot = 0.2269F + Mth.sin(frame * 0.067F + 1.0F) * 0.04F + walkWave * 0.10F;
        this.ClothBackL1.xRot = 0.1047F + Mth.sin(frame * 0.067F + 0.3F) * 0.03F + walkWave * 0.07F;
        this.ClothBackL2.xRot = 0.2269F + Mth.sin(frame * 0.067F + 0.9F) * 0.035F + walkWave * 0.08F;
        this.ClothBackL3.xRot = 0.2269F + Mth.sin(frame * 0.067F + 1.3F) * 0.04F + walkWave * 0.10F;

        // Side cloth strips - z axis sway
        float sideSway = Mth.sin(frame * 0.09F) * 0.05F + walkWave * 0.05F;
        this.SideclothL.zRot = -0.08F + sideSway;
        this.SideclothR.zRot = 0.08F - sideSway;

        this.SideclothR1.zRot = -0.1222F + Mth.sin(frame * 0.067F + 0.2F) * 0.03F + walkWave * 0.04F;
        this.SideclothR2.zRot = -0.2967F + Mth.sin(frame * 0.067F + 0.8F) * 0.035F + walkWave * 0.05F;
        this.SideclothR3.zRot = -0.5236F + Mth.sin(frame * 0.067F + 1.2F) * 0.04F + walkWave * 0.06F;
        this.SideclothR4.zRot = 0.1222F + Mth.sin(frame * 0.067F + 0.5F) * 0.03F + walkWave * 0.04F;
        this.SideclothR5.zRot = 0.2967F + Mth.sin(frame * 0.067F + 1.0F) * 0.035F + walkWave * 0.05F;
        this.SideclothR6.zRot = 0.5236F + Mth.sin(frame * 0.067F + 1.4F) * 0.04F + walkWave * 0.06F;
    }


}
