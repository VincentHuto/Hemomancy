package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class VesperTheEveningStarModel extends EntityModel<VesperTheEveningStarEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in
    // the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("vesper_evening_star"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
                PartPose.offset(0.0F, -15.0F, -15.0F));

        PartDefinition upperBody = partdefinition.addOrReplaceChild("upperBody", CubeListBuilder.create(),
                PartPose.offset(0.1F, -19.0F, -19.0F));

        PartDefinition hasturForm = upperBody.addOrReplaceChild("hasturForm", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = hasturForm.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(191, 242)
                        .addBox(-4.1F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(144, 57)
                        .addBox(-4.1F, -10.0F, 4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(144, 55)
                        .addBox(-4.1F, -10.0F, -5.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(124, 178)
                        .addBox(-5.1F, -14.0F, -5.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(176, 103)
                        .addBox(3.9F, -14.0F, 4.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(176, 64)
                        .addBox(-5.1F, -13.0F, 4.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(142, 20)
                        .addBox(-5.1F, -13.0F, -4.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(134, 113)
                        .addBox(-5.1F, -12.0F, 3.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(66, 103)
                        .addBox(3.9F, -13.0F, 3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(143, 2)
                        .addBox(-4.1F, -13.0F, -5.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(-4.1F, -11.0F, 4.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(36, 0)
                        .addBox(2.9F, -13.0F, 4.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(72, 230)
                        .addBox(-5.1F, -10.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(185, 103)
                        .addBox(-6.1F, -10.0F, -5.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(185, 64)
                        .addBox(-6.1F, -10.0F, 2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(173, 54)
                        .addBox(4.9F, -10.0F, 2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(187, 186)
                        .addBox(-5.1F, -10.0F, -6.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(146, 154)
                        .addBox(-5.1F, -10.0F, 5.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 166)
                        .addBox(1.9F, -10.0F, 5.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(200, 90)
                        .addBox(3.9F, -10.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 28)
                        .addBox(-5.1F, -9.0F, -5.0F, 9.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(202, 169)
                        .addBox(-5.1F, -8.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(173, 58)
                        .addBox(-5.1F, -8.0F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(185, 68)
                        .addBox(2.9F, -8.0F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 12.0F, 21.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition body = hasturForm.addOrReplaceChild("body", CubeListBuilder.create().texOffs(112, 137)
                        .addBox(-4.0F, -6.1615F, -1.1846F, 8.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(112, 137)
                        .addBox(-4.0F, 7.7385F, -1.9346F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(111, 270)
                        .addBox(-4.0F, -6.1615F, -2.2846F, 8.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(112, 137)
                        .addBox(-4.0F, 7.7385F, 2.5654F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(112, 137)
                        .addBox(3.75F, 7.7385F, -1.1846F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(112, 137)
                        .addBox(3.35F, -0.1615F, -1.1846F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(112, 137)
                        .addBox(-4.35F, -0.1615F, -1.1846F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(112, 137)
                        .addBox(-4.75F, 7.7385F, -1.1846F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.1F, 18.1615F, 20.1846F));

        PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(134, 113)
                        .addBox(-2.0F, -1.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.1F, 8.8385F, 0.8154F));

        PartDefinition rightLeg2 = rightLeg.addOrReplaceChild("rightLeg2",
                CubeListBuilder.create().texOffs(134, 113)
                        .addBox(-2.0F, 0.0F, -0.1F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(28, 212)
                        .addBox(-2.0F, 7.0F, -1.1F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, -2.0F));

        PartDefinition leftLeg = body.addOrReplaceChild("leftLeg",
                CubeListBuilder.create().texOffs(134, 113).mirror()
                        .addBox(-1.9F, -1.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(2.0F, 8.8385F, 0.8154F));

        PartDefinition leftLeg2 = leftLeg.addOrReplaceChild("leftLeg2",
                CubeListBuilder.create().texOffs(134, 113).mirror()
                        .addBox(-0.9F, 0.0F, -0.1F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(27, 204).mirror()
                        .addBox(-0.9F, 7.0F, -1.1F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(-1.0F, 7.0F, -2.0F));

        PartDefinition cape = body.addOrReplaceChild("cape", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -6.1615F, 3.8154F, -0.5672F, 0.0F, 0.0F));

        PartDefinition Body_r1 = cape.addOrReplaceChild("Body_r1", CubeListBuilder.create().texOffs(152, 203)
                        .addBox(1.0F, -0.9319F, -0.4824F, 3.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(152, 203)
                        .addBox(-10.0F, -0.9319F, -0.4824F, 3.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(217, 202)
                        .addBox(-7.0F, -1.9319F, -0.4824F, 8.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 1.6868F, 0.0746F, 0.829F, 0.0F, 0.0F));

        PartDefinition bone3 = cape.addOrReplaceChild("bone3", CubeListBuilder.create(),
                PartPose.offset(0.0F, 8.0F, 8.0F));

        PartDefinition Body_r2 = bone3.addOrReplaceChild("Body_r2",
                CubeListBuilder.create().texOffs(155, 214).addBox(-4.0F, -1.9319F, -0.4824F, 8.0F, 5.0F, 2.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.6868F, 1.0746F, 0.829F, 0.0F, 0.0F));

        PartDefinition cape2 = bone3.addOrReplaceChild("cape2", CubeListBuilder.create(),
                PartPose.offset(0.0F, 3.0F, 4.0F));

        PartDefinition Body_r3 = cape2.addOrReplaceChild("Body_r3", CubeListBuilder.create().texOffs(152, 203)
                        .addBox(1.0F, -0.9319F, -0.4824F, 3.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(152, 203)
                        .addBox(12.0F, -0.9319F, -0.4824F, 3.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-8.0F, 1.3891F, 0.0237F, 0.829F, 0.0F, 0.0F));

        PartDefinition Body_r4 = cape2.addOrReplaceChild("Body_r4",
                CubeListBuilder.create().texOffs(182, 205).addBox(-4.0F, -1.9319F, -0.4824F, 8.0F, 12.0F, 2.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 2.0647F, 0.761F, 0.829F, 0.0F, 0.0F));

        PartDefinition bone2 = cape2.addOrReplaceChild("bone2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 8.485F, 8.5337F, 0.7418F, 0.0F, 0.0F));

        PartDefinition Body_r5 = bone2.addOrReplaceChild("Body_r5",
                CubeListBuilder.create().texOffs(152, 224).addBox(-9.0F, -2.0746F, 1.6868F, 18.0F, 6.0F, 1.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 1.9696F, -0.3473F, 0.829F, 0.0F, 0.0F));

        PartDefinition leftArm = body.addOrReplaceChild("leftArm",
                CubeListBuilder.create().texOffs(281, 22)
                        .addBox(-0.4751F, -2.1144F, -1.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)).texOffs(92, 230)
                        .addBox(-0.4751F, -2.1144F, -1.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(4.1F, -4.1615F, -0.1846F, 0.0F, 0.0F, -0.3927F));

        PartDefinition leftElbow = leftArm.addOrReplaceChild("leftElbow",
                CubeListBuilder.create().texOffs(249, 266)
                        .addBox(-2.0924F, 2.1809F, -1.5331F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 52)
                        .addBox(-1.0924F, -0.8191F, -0.5331F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.6173F, 8.7239F, 0.5F, -1.0472F, 0.0F, 0.0F));

        PartDefinition rightArm = body.addOrReplaceChild("rightArm",
                CubeListBuilder.create().texOffs(54, 103)
                        .addBox(-4.6357F, -2.3177F, 0.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)).texOffs(82, 281)
                        .addBox(-4.6357F, -2.3177F, 0.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.1F, -4.1615F, -1.1846F, 0.0F, 0.0F, 0.3054F));

        PartDefinition rElbow = rightArm.addOrReplaceChild("rElbow", CubeListBuilder.create().texOffs(223, 266)
                        .addBox(-2.3961F, 2.5384F, -2.6233F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(15, 52)
                        .addBox(-1.3961F, -0.4616F, -1.6233F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.2396F, 9.3559F, 2.5F, -1.0908F, 0.0F, 0.0F));

        PartDefinition staff = rElbow.addOrReplaceChild("staff", CubeListBuilder.create().texOffs(66, 66)
                        .addBox(7.768F, -23.0327F, 88.2578F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(241, 256)
                        .addBox(6.5935F, -23.8204F, 89.4705F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(124, 86)
                        .addBox(7.3865F, -23.9316F, 96.3771F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(103, 139)
                        .addBox(11.3865F, -24.9316F, 91.3771F, 2.0F, 1.0F, 17.0F, new CubeDeformation(0.0F)).texOffs(240, 240)
                        .addBox(11.3865F, -23.9316F, 98.3771F, 2.0F, 1.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(198, 28)
                        .addBox(11.3865F, -22.9316F, 98.3771F, 2.0F, 1.0F, 24.0F, new CubeDeformation(0.0F)).texOffs(208, 28)
                        .addBox(11.3865F, -23.9316F, 118.3771F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(143, 2)
                        .addBox(11.3865F, -21.9316F, 118.3771F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-12.9947F, 73.0542F, -92.1387F, 0.3927F, 0.0F, 0.0F));

        PartDefinition bone = upperBody.addOrReplaceChild("bone", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.5F, 17.5F, -3.6F, 0.6109F, 0.0F, 0.0F));

        PartDefinition crest = upperBody.addOrReplaceChild("crest",
                CubeListBuilder.create().texOffs(0, 256)
                        .addBox(6.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(67, 245)
                        .addBox(-7.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(207, 197)
                        .addBox(-3.5F, 6.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(156, 64)
                        .addBox(-4.5F, 5.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(127, 153)
                        .addBox(-4.5F, -6.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(108, 230)
                        .addBox(5.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(142, 218)
                        .addBox(-6.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(279, 183)
                        .addBox(-5.5F, -5.5F, -1.3889F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(207, 195)
                        .addBox(-3.5F, -7.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-21.6F, -3.5F, 28.3889F));

        PartDefinition crest2 = upperBody.addOrReplaceChild("crest2",
                CubeListBuilder.create().texOffs(63, 245)
                        .addBox(6.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(54, 112)
                        .addBox(-7.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(198, 49)
                        .addBox(-3.5F, 6.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(127, 151)
                        .addBox(-4.5F, 5.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(143, 0)
                        .addBox(-4.5F, -6.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(122, 218)
                        .addBox(5.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(153, 8)
                        .addBox(-6.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(262, 279)
                        .addBox(-5.5F, -5.5F, -1.3889F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(172, 197)
                        .addBox(-3.5F, -7.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(21.4F, -3.5F, 28.3889F));

        PartDefinition crest3 = upperBody.addOrReplaceChild("crest3",
                CubeListBuilder.create().texOffs(52, 245)
                        .addBox(6.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 241)
                        .addBox(-7.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(82, 197)
                        .addBox(-3.5F, 6.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(134, 132)
                        .addBox(-4.5F, 5.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(80, 101)
                        .addBox(-4.5F, -6.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(8, 52)
                        .addBox(5.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(23, 150)
                        .addBox(-6.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(238, 279)
                        .addBox(-5.5F, -5.5F, -1.3889F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(82, 195)
                        .addBox(-3.5F, -7.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.6F, -11.5F, 24.3889F));

        PartDefinition crest4 = upperBody.addOrReplaceChild("crest4",
                CubeListBuilder.create().texOffs(62, 195)
                        .addBox(-6.625F, -3.5F, -0.375F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(34, 185)
                        .addBox(-2.625F, 6.5F, -0.375F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(134, 130)
                        .addBox(-3.625F, 5.5F, -0.375F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(125, 56)
                        .addBox(-3.625F, -6.5F, -0.375F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 141)
                        .addBox(6.375F, -4.5F, -0.375F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(125, 28)
                        .addBox(-5.625F, -4.5F, -0.375F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(198, 37)
                        .addBox(-4.625F, -5.5F, -1.375F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(163, 0)
                        .addBox(-2.625F, -7.5F, -0.375F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(17.525F, 16.5F, 25.375F));

        PartDefinition crest5 = upperBody.addOrReplaceChild("crest5",
                CubeListBuilder.create().texOffs(124, 189)
                        .addBox(6.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(79, 151)
                        .addBox(-7.5F, -3.5F, -0.3889F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(182, 17)
                        .addBox(-3.5F, 6.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(125, 54)
                        .addBox(-4.5F, 5.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 125)
                        .addBox(-4.5F, -6.5F, -0.3889F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(23, 52)
                        .addBox(5.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(98, 28)
                        .addBox(-6.5F, -4.5F, -0.3889F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(214, 279)
                        .addBox(-5.5F, -5.5F, -1.3889F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(182, 15)
                        .addBox(-3.5F, -7.5F, -0.3889F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-18.6F, 16.5F, 26.3889F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }
    private final ModelPart whole;
    private final ModelPart upperBody;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart rightLeg2;
    private final ModelPart leftLeg;
    private final ModelPart leftLeg2;
    private final ModelPart cape;
    private final ModelPart bone3;
    private final ModelPart cape2;
    private final ModelPart bone2;

    public VesperTheEveningStarModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.upperBody = root.getChild("upperBody");
        ModelPart hasturForm = this.upperBody.getChild("hasturForm");
        this.head = hasturForm.getChild("head");
        this.body = hasturForm.getChild("body");
        this.rightArm = this.body.getChild("rightArm");
        this.leftArm = this.body.getChild("leftArm");
        this.rightLeg = this.body.getChild("rightLeg");
        this.rightLeg2 = this.rightLeg.getChild("rightLeg2");
        this.leftLeg = this.body.getChild("leftLeg");
        this.leftLeg2 = this.leftLeg.getChild("leftLeg2");
        this.cape = this.body.getChild("cape");
        this.bone3 = this.cape.getChild("bone3");
        this.cape2 = this.bone3.getChild("cape2");
        this.bone2 = this.cape2.getChild("bone2");
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               int packedColor) {
        whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        upperBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    @Override
    public void setupAnim(VesperTheEveningStarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {
        netHeadYaw = Mth.wrapDegrees(netHeadYaw);
        float frame = entity.tickCount + HLClientUtils.getPartialTicks();

        // Head
        this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.75f;
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.75f;

        // Arms
        this.rightArm.xRot = (float) (Math.sin((frame) * 0.04f) * 0.0325)
                + Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.rightArm.zRot = (float) Math.abs(((Math.cos((frame) * 0.04f) * 0.0525) + Math.toRadians(22.5)));
        this.leftArm.zRot = (float) -Math.abs(((Math.sin((frame) * 0.04f) * 0.0525) + Math.toRadians(-22.5)));

        // Legs
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.9662F) * 1.4F * limbSwingAmount / 2;
        this.rightLeg2.xRot = Math
                .abs(Mth.cos(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.9662F + (float) Math.PI) * 1.4F * limbSwingAmount / 2;
        this.leftLeg2.xRot = Math
                .abs(Mth.sin(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
        // Cape
        this.cape.xRot = (float) Math.sin((frame) * 0.3f) * 0.05f + 05.75f;
        this.bone3.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f + 25.25f;
        this.cape2.xRot = (float) Math.sin((frame) * 0.7f) * 0.15f + 25.25f;
        this.bone2.xRot = (float) Math.sin((frame) * 0.8f) * 0.25f + 25.5f;
    }
}
