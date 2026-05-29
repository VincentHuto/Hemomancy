package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MycophantModel extends HierarchicalModel<MycophantEntity> {
    // This layer location should be baked with EntityRendererProvider.Context in
    // the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("mycophant"), "main");
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition wholeBody = root.addOrReplaceChild("wholeBody", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lowerColumn = wholeBody.addOrReplaceChild("lowerColumn", CubeListBuilder.create()
                        .texOffs(118, 138).addBox(-3.0F, -3.0455F, -4.2727F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(24, 136).addBox(-3.0F, -6.0455F, -6.2727F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(112, 180).addBox(3.0F, -4.0455F, -5.2727F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(150, 173).addBox(-4.0F, -4.0455F, -5.2727F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(91, 180).addBox(3.0F, -7.0455F, -5.2727F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(172, 34).addBox(-4.0F, -7.0455F, -5.2727F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(178, 50).addBox(3.0F, -10.0455F, -5.2727F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(94, 132).addBox(-3.0F, -9.0455F, -7.2727F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(79, 172).addBox(-4.0F, -10.0455F, -5.2727F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                        .texOffs(106, 79).addBox(-1.0F, -2.0455F, 1.7273F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(76, 89).addBox(-1.0F, -5.0455F, -3.2727F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -8.9545F, 13.8727F));

        PartDefinition upperColumn = lowerColumn.addOrReplaceChild("upperColumn",
                CubeListBuilder.create().texOffs(0, 178)
                        .addBox(3.0F, -3.7F, -4.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(58, 172)
                        .addBox(-4.0F, -3.7F, -4.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(176, 134)
                        .addBox(3.0F, -6.7F, -4.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(171, 89)
                        .addBox(-4.0F, -6.7F, -4.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(176, 62)
                        .addBox(3.0F, -9.7F, -4.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(129, 171)
                        .addBox(-4.0F, -9.7F, -4.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(168, 153)
                        .addBox(-1.0F, -13.7F, -0.7333F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(175, 116)
                        .addBox(3.0F, -11.7F, -0.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(175, 175)
                        .addBox(-4.0F, -11.7F, -0.7333F, 1.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 132)
                        .addBox(-3.0F, -2.7F, -5.7333F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(70, 126)
                        .addBox(-3.0F, -8.7F, -5.7333F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(130, 68)
                        .addBox(-3.0F, -5.7F, -6.7333F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(120, 102)
                        .addBox(-3.0F, -11.7F, -6.7333F, 6.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(110, 32)
                        .addBox(-4.0F, -14.7F, -8.7333F, 8.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(48, 105)
                        .addBox(-5.0F, -16.7F, -8.7333F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -9.3455F, -0.5394F));

        PartDefinition upperTorso = upperColumn.addOrReplaceChild("upperTorso", CubeListBuilder.create(),
                PartPose.offset(0.0F, -13.7F, -3.3333F));

        PartDefinition neck = upperTorso.addOrReplaceChild("neck", CubeListBuilder.create(),
                PartPose.offset(-1.0F, -9.0F, -16.0F));

        PartDefinition neck2 = neck.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(102, 111)
                        .addBox(-0.5F, -6.4466F, -13.7689F, 1.0F, 5.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(88, 89)
                        .addBox(-3.0F, -2.8485F, -13.0009F, 6.0F, 8.0F, 13.0F, new CubeDeformation(0.0F)).texOffs(28, 105)
                        .addBox(-1.0F, -4.8485F, -14.2689F, 2.0F, 5.0F, 15.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, 0.2851F, -1.6844F, -0.6109F, 0.0F, 0.0F));

        PartDefinition neck1 = neck2.addOrReplaceChild("neck1", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -4.9597F, -14.479F, 0.5236F, 0.0F, 0.0F));

        PartDefinition cube_r2 = neck1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(114, 79)
                        .addBox(-6.5F, -3.6405F, -12.0595F, 1.0F, 9.0F, 13.0F, new CubeDeformation(0.0F)).texOffs(120, 120)
                        .addBox(-8.5F, -3.6405F, -12.0595F, 1.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(48, 118)
                        .addBox(-4.5F, -3.6405F, -12.0595F, 1.0F, 2.0F, 15.0F, new CubeDeformation(0.0F)).texOffs(67, 141)
                        .addBox(-5.0F, -1.6405F, -12.0595F, 2.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(142, 142)
                        .addBox(-9.0F, -1.6405F, -12.0595F, 2.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(144, 83)
                        .addBox(-7.0F, -1.6405F, -12.0595F, 2.0F, 8.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(94, 147)
                        .addBox(-2.0F, -1.6405F, -12.0595F, 1.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(22, 151)
                        .addBox(-11.0F, -1.6405F, -12.0595F, 1.0F, 9.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(65, 68)
                        .addBox(-10.0F, -0.6405F, -12.0595F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.0F, 1.7833F, 0.8289F, 0.5672F, 0.0F, 0.0F));

        PartDefinition lGills = neck1.addOrReplaceChild("lGills", CubeListBuilder.create(),
                PartPose.offset(2.0F, 7.4698F, -1.3881F));

        PartDefinition cube_r3 = lGills.addOrReplaceChild("cube_r3",
                CubeListBuilder.create().texOffs(86, 197).addBox(-4.0F, -9.0F, -5.0F, 3.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 1.8135F, 7.017F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r4 = lGills.addOrReplaceChild("cube_r4",
                CubeListBuilder.create().texOffs(114, 199).addBox(-4.0F, -9.0F, -5.0F, 2.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 1.3135F, 6.217F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r5 = lGills.addOrReplaceChild("cube_r5",
                CubeListBuilder.create().texOffs(197, 197).addBox(-4.0F, -9.0F, -5.0F, 3.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 2.3135F, 6.217F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r6 = lGills.addOrReplaceChild("cube_r6",
                CubeListBuilder.create().texOffs(119, 199).addBox(-4.0F, -9.0F, -5.0F, 2.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 2.3135F, 5.217F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r7 = lGills.addOrReplaceChild("cube_r7",
                CubeListBuilder.create().texOffs(91, 161).addBox(-4.0F, -9.0F, -5.0F, 1.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 2.5135F, 4.217F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r8 = lGills.addOrReplaceChild("cube_r8",
                CubeListBuilder.create().texOffs(102, 147).addBox(-4.0F, -9.0F, -5.0F, 1.0F, 8.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.0F, 2.2135F, 7.817F, 0.5672F, 0.0F, 0.0F));

        PartDefinition rGills = neck1.addOrReplaceChild("rGills", CubeListBuilder.create(),
                PartPose.offset(-2.0F, 7.2833F, -2.3711F));

        PartDefinition cube_r9 = rGills
                .addOrReplaceChild("cube_r9",
                        CubeListBuilder.create().texOffs(28, 64).addBox(-4.0F, -9.0F, -5.0F, 3.0F, 9.0F, 0.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(-2.0F, 2.0F, 8.0F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r10 = rGills
                .addOrReplaceChild("cube_r10",
                        CubeListBuilder.create().texOffs(64, 136).addBox(-4.0F, -9.0F, -5.0F, 2.0F, 9.0F, 0.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(-1.0F, 1.5F, 7.2F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r11 = rGills
                .addOrReplaceChild("cube_r11",
                        CubeListBuilder.create().texOffs(173, 30).addBox(-4.0F, -9.0F, -5.0F, 3.0F, 9.0F, 0.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(-2.0F, 2.5F, 7.2F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r12 = rGills
                .addOrReplaceChild("cube_r12",
                        CubeListBuilder.create().texOffs(17, 160).addBox(-4.0F, -9.0F, -5.0F, 2.0F, 9.0F, 0.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(-1.0F, 2.5F, 6.2F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r13 = rGills
                .addOrReplaceChild("cube_r13",
                        CubeListBuilder.create().texOffs(127, 102).addBox(-4.0F, -9.0F, -5.0F, 1.0F, 9.0F, 0.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(0.0F, 2.7F, 5.2F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r14 = rGills
                .addOrReplaceChild("cube_r14",
                        CubeListBuilder.create().texOffs(62, 31).addBox(-4.0F, -9.0F, -5.0F, 1.0F, 8.0F, 0.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(0.0F, 2.4F, 8.8F, 0.5672F, 0.0F, 0.0F));

        PartDefinition face = neck1.addOrReplaceChild("face", CubeListBuilder.create().texOffs(138, 28).addBox(-6.0F,
                        -5.3562F, -4.8409F, 12.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 11.826F, -5.5472F));

        PartDefinition eyes = face.addOrReplaceChild("eyes", CubeListBuilder.create(),
                PartPose.offset(-4.5F, 12.1438F, 7.6591F));

        PartDefinition eye2 = eyes.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(73, 197).addBox(-1.5F,
                -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(9.1F, -15.0F, -13.0F));

        PartDefinition eye3 = eyes.addOrReplaceChild("eye3", CubeListBuilder.create().texOffs(196, 165).addBox(-1.5F,
                -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, -18.0F, -13.0F));

        PartDefinition eye11 = eyes.addOrReplaceChild("eye11", CubeListBuilder.create().texOffs(196, 113).addBox(-1.5F,
                -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.5F, -8.0F, -13.0F));

        PartDefinition eye6 = eyes.addOrReplaceChild("eye6", CubeListBuilder.create().texOffs(44, 202).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -14.5F, -13.5F));

        PartDefinition eye4 = eyes.addOrReplaceChild("eye4", CubeListBuilder.create().texOffs(201, 152).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -11.5F, -13.5F));

        PartDefinition eye5 = eyes.addOrReplaceChild("eye5", CubeListBuilder.create().texOffs(201, 134).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -15.5F, -13.5F));

        PartDefinition eye12 = eyes.addOrReplaceChild("eye12", CubeListBuilder.create().texOffs(157, 201).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.7F, -11.9F, -12.5F));

        PartDefinition eye7 = eyes.addOrReplaceChild("eye7", CubeListBuilder.create().texOffs(201, 91).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, -17.5F, -12.5F));

        PartDefinition eye10 = eyes.addOrReplaceChild("eye10", CubeListBuilder.create().texOffs(166, 201).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.5F, -12.5F));

        PartDefinition eye9 = eyes.addOrReplaceChild("eye9", CubeListBuilder.create().texOffs(175, 201).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, -10.5F, -12.5F));

        PartDefinition eye8 = eyes.addOrReplaceChild("eye8", CubeListBuilder.create().texOffs(201, 27).addBox(-1.0F,
                -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, -10.5F, -12.5F));

        PartDefinition eye = eyes.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(184, 197).addBox(-1.5F,
                -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.3F, -16.2F, -13.0F));

        PartDefinition tentacles = face.addOrReplaceChild("tentacles", CubeListBuilder.create(),
                PartPose.offset(0.0F, 0.6438F, -4.8409F));

        PartDefinition tent6 = tentacles
                .addOrReplaceChild("tent6",
                        CubeListBuilder.create().texOffs(0, 197).addBox(-1.0F, -1.9235F, -3.774F, 2.0F, 2.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(-5.9F, -1.0F, 1.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent7 = tent6.addOrReplaceChild("tent7",
                CubeListBuilder.create().texOffs(196, 180).addBox(-1.09F, -1.5649F, -3.9195F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.4982F, -3.1228F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent8 = tent7.addOrReplaceChild("tent8",
                CubeListBuilder.create().texOffs(196, 173).addBox(-1.0F, -1.2695F, -3.6435F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1F, -0.2873F, -3.5936F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent9 = tent8.addOrReplaceChild("tent9",
                CubeListBuilder.create().texOffs(196, 120).addBox(-1.09F, -1.0142F, -3.5778F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.2079F, -3.361F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent10 = tent9.addOrReplaceChild("tent10",
                CubeListBuilder.create().texOffs(53, 202).addBox(-0.7F, -0.4611F, -2.1723F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0749F, -3.6048F, 0.2182F, 0.0F, 0.0F));

        PartDefinition tent22 = tentacles
                .addOrReplaceChild("tent22",
                        CubeListBuilder.create().texOffs(0, 197).addBox(-1.0F, -1.9235F, -3.774F, 2.0F, 2.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(6.1F, -1.0F, 1.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent23 = tent22.addOrReplaceChild("tent23",
                CubeListBuilder.create().texOffs(196, 180).addBox(-1.09F, -1.5649F, -3.9195F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.4982F, -3.1228F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent24 = tent23.addOrReplaceChild("tent24",
                CubeListBuilder.create().texOffs(196, 173).addBox(-1.0F, -1.2695F, -3.6435F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1F, -0.2873F, -3.5936F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent25 = tent24.addOrReplaceChild("tent25",
                CubeListBuilder.create().texOffs(196, 120).addBox(-1.09F, -1.0142F, -3.5778F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.2079F, -3.361F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent26 = tent25.addOrReplaceChild("tent26",
                CubeListBuilder.create().texOffs(53, 202).addBox(-0.7F, -0.4611F, -2.1723F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0749F, -3.6048F, 0.2182F, 0.0F, 0.0F));

        PartDefinition tent2 = tentacles
                .addOrReplaceChild("tent2",
                        CubeListBuilder.create().texOffs(0, 197).addBox(-1.0F, -1.9235F, -3.774F, 2.0F, 2.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(2.2F, -1.6F, 1.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent3 = tent2.addOrReplaceChild("tent3",
                CubeListBuilder.create().texOffs(196, 180).addBox(-1.09F, -1.5649F, -3.9195F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.4982F, -3.1228F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent4 = tent3.addOrReplaceChild("tent4",
                CubeListBuilder.create().texOffs(196, 173).addBox(-1.0F, -1.2695F, -3.6435F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1F, -0.2873F, -3.5936F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent5 = tent4.addOrReplaceChild("tent5",
                CubeListBuilder.create().texOffs(196, 120).addBox(-1.09F, -1.0142F, -3.5778F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.2079F, -3.361F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent11 = tent5.addOrReplaceChild("tent11",
                CubeListBuilder.create().texOffs(53, 202).addBox(-0.7F, -0.4611F, -2.1723F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0749F, -3.6048F, 0.2182F, 0.0F, 0.0F));

        PartDefinition tent12 = tentacles
                .addOrReplaceChild("tent12",
                        CubeListBuilder.create().texOffs(0, 197).addBox(-1.0F, -1.9235F, -3.774F, 2.0F, 2.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(-2.5F, 2.7F, 1.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent13 = tent12.addOrReplaceChild("tent13",
                CubeListBuilder.create().texOffs(196, 180).addBox(-1.09F, -1.5649F, -3.9195F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.4982F, -3.1228F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent14 = tent13.addOrReplaceChild("tent14",
                CubeListBuilder.create().texOffs(196, 173).addBox(-1.0F, 0.7229F, -3.4692F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1F, -1.9256F, -4.7407F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent15 = tent14.addOrReplaceChild("tent15",
                CubeListBuilder.create().texOffs(196, 120).addBox(-1.09F, -1.0142F, -3.5778F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, 1.7845F, -3.1867F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent16 = tent15.addOrReplaceChild("tent16",
                CubeListBuilder.create().texOffs(53, 202).addBox(-0.7F, -0.4611F, -2.1723F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0749F, -3.6048F, 0.2182F, 0.0F, 0.0F));

        PartDefinition tent17 = tentacles
                .addOrReplaceChild("tent17",
                        CubeListBuilder.create().texOffs(0, 197).addBox(-1.0F, -1.9235F, -3.774F, 2.0F, 2.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                        PartPose.offsetAndRotation(-0.9F, 0.0F, 1.0F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent18 = tent17.addOrReplaceChild("tent18",
                CubeListBuilder.create().texOffs(196, 180).addBox(-1.09F, -1.5649F, -3.9195F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.4982F, -3.1228F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent19 = tent18.addOrReplaceChild("tent19",
                CubeListBuilder.create().texOffs(196, 173).addBox(-1.0F, -1.2695F, -3.6435F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1F, -0.2873F, -3.5936F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent20 = tent19.addOrReplaceChild("tent20",
                CubeListBuilder.create().texOffs(196, 120).addBox(-1.09F, -1.0142F, -3.5778F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.1F, -0.2079F, -3.361F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent21 = tent20.addOrReplaceChild("tent21",
                CubeListBuilder.create().texOffs(53, 202).addBox(-0.7F, -0.4611F, -2.1723F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.0749F, -3.6048F, 0.2182F, 0.0F, 0.0F));

        PartDefinition armTent = upperTorso.addOrReplaceChild("armTent", CubeListBuilder.create(),
                PartPose.offset(7.5F, -3.9333F, -8.5F));

        PartDefinition shoulder = armTent.addOrReplaceChild("shoulder",
                CubeListBuilder.create().texOffs(68, 184)
                        .addBox(0.0F, -1.8F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(150, 185)
                        .addBox(0.5F, -2.2F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(47, 161)
                        .addBox(0.5F, -1.3667F, 2.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(157, 114)
                        .addBox(0.5F, -1.3667F, -3.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 132)
                        .addBox(1.6F, -1.6667F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(175, 146)
                        .addBox(1.8F, -0.6667F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -0.2F, 0.0F));

        PartDefinition armEye = shoulder.addOrReplaceChild("armEye", CubeListBuilder.create().texOffs(150, 179)
                        .addBox(-0.4F, -10.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.7F, 10.4333F, 0.0F));

        PartDefinition seg3 = shoulder.addOrReplaceChild("seg3",
                CubeListBuilder.create().texOffs(48, 136)
                        .addBox(-0.121F, 0.0566F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(183, 146)
                        .addBox(0.379F, 0.0566F, 0.2F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(198, 187)
                        .addBox(0.379F, 0.0566F, -1.5F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(202, 66)
                        .addBox(1.179F, -0.7434F, 0.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.621F, 2.0767F, -0.5F));

        PartDefinition bone3 = seg3.addOrReplaceChild("bone3", CubeListBuilder.create(),
                PartPose.offset(0.9981F, 5.0712F, 0.5F));

        PartDefinition seg2 = bone3.addOrReplaceChild("seg2",
                CubeListBuilder.create().texOffs(0, 160)
                        .addBox(-1.5F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(107, 199)
                        .addBox(-1.0F, 0.0F, -1.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(37, 203)
                        .addBox(-0.2F, 0.2F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.381F, -0.0145F, 0.0F));

        PartDefinition seg1 = seg2.addOrReplaceChild("seg1",
                CubeListBuilder.create().texOffs(100, 199)
                        .addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(93, 199)
                        .addBox(-1.0F, 0.0F, -0.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 147)
                        .addBox(-1.5F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(72, 204)
                        .addBox(-0.2F, 1.2F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition hand = seg1.addOrReplaceChild("hand", CubeListBuilder.create().texOffs(86, 184)
                        .addBox(-0.8333F, 1.0533F, 0.5533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(134, 183)
                        .addBox(-0.8333F, 2.6533F, 0.7533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(182, 158)
                        .addBox(-0.8333F, 3.2533F, -1.2467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(181, 170)
                        .addBox(-0.8333F, 0.4533F, -1.1467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(181, 74)
                        .addBox(-0.8333F, 4.5533F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(180, 134)
                        .addBox(-0.8333F, 2.0533F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(124, 180)
                        .addBox(-0.8333F, 1.7533F, -1.7467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(190, 190)
                        .addBox(-0.4333F, 1.0533F, -2.4467F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(35, 196)
                        .addBox(-0.4333F, 2.0533F, -1.9467F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(139, 201)
                        .addBox(-0.4333F, 4.0533F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(130, 201)
                        .addBox(-0.4333F, 0.0533F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(10, 21)
                        .addBox(0.0667F, 0.8533F, -0.9467F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(171, 94)
                        .addBox(-0.4333F, 5.0533F, -0.9467F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(179, 116)
                        .addBox(0.0667F, 4.8533F, -0.5467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(77, 204)
                        .addBox(0.3667F, 0.2533F, -0.5467F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.4667F, 4.9467F, 0.0467F));

        PartDefinition armTent2 = upperTorso.addOrReplaceChild("armTent2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-8.5F, -3.9333F, -8.5F, 0.0F, 3.1416F, 0.4363F));

        PartDefinition shoulder2 = armTent2.addOrReplaceChild("shoulder2",
                CubeListBuilder.create().texOffs(133, 183)
                        .addBox(0.0F, -1.9333F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(108, 148)
                        .addBox(0.5F, -2.3333F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(119, 157)
                        .addBox(0.5F, -1.5F, 2.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(146, 117)
                        .addBox(0.5F, -1.5F, -3.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(94, 128)
                        .addBox(1.6F, -1.8F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(19, 173)
                        .addBox(1.8F, -0.8F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -0.0667F, 0.0F));

        PartDefinition armEye2 = shoulder2.addOrReplaceChild("armEye2", CubeListBuilder.create().texOffs(70, 178)
                        .addBox(-0.4F, -10.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.7F, 10.3F, 0.0F));

        PartDefinition seg4 = shoulder2.addOrReplaceChild("seg4",
                CubeListBuilder.create().texOffs(106, 111)
                        .addBox(-0.9662F, 1.8692F, -1.5F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(180, 50)
                        .addBox(-0.4662F, 1.8692F, -0.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(12, 178)
                        .addBox(-0.4662F, 1.8692F, -2.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(67, 202)
                        .addBox(0.3338F, 1.0692F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.4662F, 0.1308F, 0.0F));

        PartDefinition bone2 = seg4.addOrReplaceChild("bone2", CubeListBuilder.create(),
                PartPose.offset(0.1529F, 6.8838F, 0.0F));

        PartDefinition seg5 = bone2.addOrReplaceChild("seg5",
                CubeListBuilder.create().texOffs(128, 0)
                        .addBox(-0.7717F, 0.2698F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(169, 153)
                        .addBox(-0.2717F, 0.2698F, -1.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(202, 139)
                        .addBox(0.5283F, 0.4698F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.3473F, -0.2843F, 0.0F));

        PartDefinition seg6 = seg5.addOrReplaceChild("seg6",
                CubeListBuilder.create().texOffs(158, 143)
                        .addBox(-0.2717F, 0.2698F, -2.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(83, 141)
                        .addBox(-0.2717F, 0.2698F, -0.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 118)
                        .addBox(-0.7717F, 0.2698F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 204)
                        .addBox(0.5283F, 1.4698F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition hand2 = seg6.addOrReplaceChild("hand2", CubeListBuilder.create().texOffs(176, 170)
                        .addBox(-1.0113F, 0.9005F, 0.5533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(175, 129)
                        .addBox(-1.0113F, 2.5005F, 0.7533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 174)
                        .addBox(-1.0113F, 3.1005F, -1.2467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(111, 174)
                        .addBox(-1.0113F, 0.3005F, -1.1467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(106, 174)
                        .addBox(-1.0113F, 4.4005F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(173, 40)
                        .addBox(-1.0113F, 1.9005F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(60, 172)
                        .addBox(-1.0113F, 1.6005F, -1.7467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(80, 190)
                        .addBox(-0.6113F, 0.9005F, -2.4467F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(142, 142)
                        .addBox(-0.6113F, 1.9005F, -1.9467F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(193, 84)
                        .addBox(-0.6113F, 3.9005F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(158, 185)
                        .addBox(-0.6113F, -0.0995F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 21)
                        .addBox(-0.1113F, 0.7005F, -0.9467F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(134, 153)
                        .addBox(-0.6113F, 4.9005F, -0.9467F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(41, 172)
                        .addBox(-0.1113F, 4.7005F, -0.5467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(5, 204)
                        .addBox(0.1887F, 0.1005F, -0.5467F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.4396F, 5.3693F, 0.0467F));

        PartDefinition armTent3 = upperTorso.addOrReplaceChild("armTent3", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.5F, -0.9333F, -2.5F, 0.0F, 3.1416F, 0.4363F));

        PartDefinition shoulder3 = armTent3.addOrReplaceChild("shoulder3",
                CubeListBuilder.create().texOffs(182, 11)
                        .addBox(0.0F, -1.9333F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(64, 141)
                        .addBox(0.5F, -2.3333F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(72, 141)
                        .addBox(0.5F, -1.5F, 2.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(138, 117)
                        .addBox(0.5F, -1.5F, -3.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(32, 103)
                        .addBox(1.6F, -1.8F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(128, 117)
                        .addBox(1.8F, -0.8F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -0.0667F, 0.0F));

        PartDefinition armEye3 = shoulder3.addOrReplaceChild("armEye3", CubeListBuilder.create().texOffs(168, 30)
                        .addBox(-0.4F, -10.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.7F, 10.3F, 0.0F));

        PartDefinition seg7 = shoulder3.addOrReplaceChild("seg7",
                CubeListBuilder.create().texOffs(0, 103)
                        .addBox(-0.5436F, 0.9629F, -1.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(118, 67)
                        .addBox(-0.0436F, 0.9629F, 0.2F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(113, 14)
                        .addBox(-0.0436F, 0.9629F, -1.5F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(62, 202)
                        .addBox(0.7564F, 0.1629F, 0.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0436F, 1.0371F, -0.5F, -0.3054F, 0.0F, 0.0F));

        PartDefinition bone4 = seg7.addOrReplaceChild("bone4", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.5755F, 5.9775F, 0.5F, 0.2182F, 0.0F, 0.0F));

        PartDefinition seg8 = bone4.addOrReplaceChild("seg8",
                CubeListBuilder.create().texOffs(79, 105)
                        .addBox(-1.5F, -10.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(101, 0)
                        .addBox(-1.0F, -10.0F, -1.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(46, 187)
                        .addBox(-0.2F, -9.8F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.381F, 9.9855F, 0.0F));

        PartDefinition seg9 = seg8.addOrReplaceChild("seg9",
                CubeListBuilder.create().texOffs(98, 20)
                        .addBox(-0.2717F, 0.2698F, -2.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(69, 66)
                        .addBox(-0.2717F, 0.2698F, -0.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(48, 105)
                        .addBox(-0.7717F, 0.2698F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(108, 147)
                        .addBox(0.5283F, 1.4698F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.7283F, -5.2698F, 0.0F));

        PartDefinition hand3 = seg9.addOrReplaceChild("hand3", CubeListBuilder.create().texOffs(140, 168)
                        .addBox(-1.0113F, 0.9005F, 0.5533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(135, 168)
                        .addBox(-1.0113F, 2.5005F, 0.7533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(94, 168)
                        .addBox(-1.0113F, 3.1005F, -1.2467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(146, 162)
                        .addBox(-1.0113F, 0.3005F, -1.1467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(154, 43)
                        .addBox(-1.0113F, 4.4005F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(153, 137)
                        .addBox(-1.0113F, 1.9005F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(152, 103)
                        .addBox(-1.0113F, 1.6005F, -1.7467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(38, 189)
                        .addBox(-0.6113F, 0.9005F, -2.4467F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(24, 136)
                        .addBox(-0.6113F, 1.9005F, -1.9467F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(184, 106)
                        .addBox(-0.6113F, 3.9005F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(90, 184)
                        .addBox(-0.6113F, -0.0995F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(10, 0)
                        .addBox(-0.1113F, 0.7005F, -0.9467F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(41, 132)
                        .addBox(-0.6113F, 4.9005F, -0.9467F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(61, 152)
                        .addBox(-0.1113F, 4.7005F, -0.5467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(182, 11)
                        .addBox(0.1887F, 0.1005F, -0.5467F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.4396F, 5.3693F, 0.0467F));

        PartDefinition armTent4 = upperTorso.addOrReplaceChild("armTent4", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.5F, -7.9333F, -4.5F, 0.0F, 3.1416F, 2.138F));

        PartDefinition shoulder4 = armTent4.addOrReplaceChild("shoulder4",
                CubeListBuilder.create().texOffs(94, 61)
                        .addBox(0.0F, -1.9333F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(120, 117)
                        .addBox(0.5F, -2.3333F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(120, 117)
                        .addBox(0.5F, -1.5F, 2.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(60, 80)
                        .addBox(0.5F, -1.5F, -3.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(90, 89)
                        .addBox(1.6F, -1.8F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(56, 126)
                        .addBox(1.8F, -0.8F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -0.0667F, 0.0F));

        PartDefinition armEye4 = shoulder4.addOrReplaceChild("armEye4", CubeListBuilder.create().texOffs(147, 128)
                        .addBox(-0.4F, -10.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(2.7F, 10.3F, 0.0F));

        PartDefinition seg11 = shoulder4.addOrReplaceChild("seg11",
                CubeListBuilder.create().texOffs(74, 46)
                        .addBox(-0.121F, 0.0566F, -2.0F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(11, 64)
                        .addBox(0.379F, 0.0566F, -0.8F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 64)
                        .addBox(0.379F, 0.0566F, -2.5F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(66, 118)
                        .addBox(1.179F, -0.7434F, -1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.621F, 1.9434F, 0.5F));

        PartDefinition bone6 = seg11.addOrReplaceChild("bone6", CubeListBuilder.create(),
                PartPose.offset(0.9981F, 5.0712F, -0.5F));

        PartDefinition seg12 = bone6.addOrReplaceChild("seg12",
                CubeListBuilder.create().texOffs(76, 89)
                        .addBox(-0.7378F, -0.0291F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(43, 21)
                        .addBox(-0.2378F, -0.0291F, -1.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 53)
                        .addBox(0.5622F, 0.1709F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.3812F, 0.0146F, 0.0F));

        PartDefinition seg13 = seg12.addOrReplaceChild("seg13",
                CubeListBuilder.create().texOffs(42, 0)
                        .addBox(-0.2378F, -0.0291F, -2.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 40)
                        .addBox(-0.2378F, -0.0291F, -0.3F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 84)
                        .addBox(-0.7378F, -0.0291F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(105, 61)
                        .addBox(0.5622F, 1.1709F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition hand4 = seg13.addOrReplaceChild("hand4", CubeListBuilder.create().texOffs(146, 50)
                        .addBox(-0.8301F, 0.5407F, 0.5533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(139, 47)
                        .addBox(-0.8301F, 2.1407F, 0.7533F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(124, 138)
                        .addBox(-0.8301F, 2.7407F, -1.2467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(132, 52)
                        .addBox(-0.8301F, -0.0593F, -1.1467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(127, 132)
                        .addBox(-0.8301F, 4.0407F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(122, 28)
                        .addBox(-0.8301F, 1.5407F, -0.4467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(50, 88)
                        .addBox(-0.8301F, 1.2407F, -1.7467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(168, 71)
                        .addBox(-0.4301F, 0.5407F, -2.4467F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(54, 53)
                        .addBox(-0.4301F, 1.5407F, -1.9467F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(183, 93)
                        .addBox(-0.4301F, 3.5407F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(144, 183)
                        .addBox(-0.4301F, -0.4593F, -1.4467F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(0.0699F, 0.3407F, -0.9467F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(34, 84)
                        .addBox(-0.4301F, 4.5407F, -0.9467F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(41, 80)
                        .addBox(0.0699F, 4.3407F, -0.5467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(106, 79)
                        .addBox(0.3699F, -0.2593F, -0.5467F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.2924F, 5.4302F, 0.0467F));

        PartDefinition body = upperTorso.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(42, 0)
                        .addBox(-12.0F, -12.0F, -1.0F, 25.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(43, 21)
                        .addBox(-11.0F, -13.0F, -1.0F, 23.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(154, 60)
                        .addBox(-3.0F, -16.0F, 0.0F, 8.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(168, 13)
                        .addBox(0.0F, -5.0F, 17.6F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(115, 168)
                        .addBox(0.0F, -8.0F, 14.6F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(92, 168)
                        .addBox(0.0F, -12.0F, 14.6F, 2.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(83, 141)
                        .addBox(-1.0F, -18.0F, 0.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(182, 146)
                        .addBox(-7.0F, -15.0F, 0.0F, 1.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(163, 182)
                        .addBox(7.0F, -15.0F, 0.0F, 1.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 21)
                        .addBox(-3.0F, -14.0F, 7.0F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
                        .addBox(4.0F, -14.0F, 7.0F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(184, 34)
                        .addBox(-6.0F, -14.0F, 7.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(182, 25)
                        .addBox(6.0F, -14.0F, 7.0F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(183, 82)
                        .addBox(-1.0F, -13.0F, 14.0F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(118, 67)
                        .addBox(2.0F, -18.0F, 0.0F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(51, 184)
                        .addBox(2.0F, -13.0F, 14.0F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(165, 0)
                        .addBox(-2.0F, -13.0F, 7.0F, 6.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(0, 53)
                        .addBox(-11.0F, -7.0F, 0.0F, 23.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(151, 43)
                        .addBox(-2.0F, -11.0F, 14.0F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 40)
                        .addBox(-11.0F, -11.0F, -1.0F, 24.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(28, 66)
                        .addBox(-7.0F, -8.0F, 7.6F, 16.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(57, 32)
                        .addBox(-8.0F, -12.0F, 5.0F, 18.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(113, 14)
                        .addBox(-4.5F, -7.0F, 5.0F, 11.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(118, 55)
                        .addBox(-5.5F, -3.0F, 10.5F, 13.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 84)
                        .addBox(-5.0F, -10.0F, 9.6F, 12.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-1.0F, 1.0F, -19.0F));

        PartDefinition backEye = body.addOrReplaceChild("backEye",
                CubeListBuilder.create().texOffs(93, 192)
                        .addBox(-1.5F, -2.3F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 168)
                        .addBox(-1.5F, -0.3F, 1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(155, 6)
                        .addBox(-1.5F, -0.3F, -2.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 201)
                        .addBox(-2.5F, -0.3F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(19, 201)
                        .addBox(1.5F, -0.3F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, -13.7F, 10.0F));

        PartDefinition backEye2 = body.addOrReplaceChild("backEye2",
                CubeListBuilder.create().texOffs(191, 158)
                        .addBox(-1.5F, -2.4F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(154, 76)
                        .addBox(-1.5F, -0.4F, 1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(147, 132)
                        .addBox(-1.5F, -0.4F, -2.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(74, 165)
                        .addBox(-2.5F, -0.4F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(10, 201)
                        .addBox(1.5F, -0.4F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-9.5F, -13.6F, 3.0F));

        PartDefinition backEye4 = body.addOrReplaceChild("backEye4",
                CubeListBuilder.create().texOffs(61, 184)
                        .addBox(-1.5F, 0.2F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(138, 128)
                        .addBox(-1.5F, -0.8F, 1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(118, 132)
                        .addBox(-1.5F, -0.8F, -2.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(200, 54)
                        .addBox(-2.5F, -0.8F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(200, 47)
                        .addBox(1.5F, -0.8F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-7.5F, -3.2F, 3.0F));

        PartDefinition backEye5 = body.addOrReplaceChild("backEye5",
                CubeListBuilder.create().texOffs(124, 183)
                        .addBox(-1.5F, 0.2F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(123, 52)
                        .addBox(-1.5F, -0.8F, 1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(113, 28)
                        .addBox(-1.5F, -0.8F, -2.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(199, 19)
                        .addBox(-2.5F, -0.8F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(199, 14)
                        .addBox(1.5F, -0.8F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.5F, -3.2F, 3.0F));

        PartDefinition backEye6 = body.addOrReplaceChild("backEye6",
                CubeListBuilder.create().texOffs(59, 15)
                        .addBox(-1.5F, 0.2F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(110, 39)
                        .addBox(-1.5F, -0.8F, 1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(96, 14)
                        .addBox(-1.5F, -0.8F, -2.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(124, 199)
                        .addBox(-2.5F, -0.8F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(193, 149)
                        .addBox(1.5F, -0.8F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.5F, -3.2F, 3.0F));

        PartDefinition backEye3 = body.addOrReplaceChild("backEye3",
                CubeListBuilder.create().texOffs(190, 50)
                        .addBox(-1.5F, -2.4F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(144, 110)
                        .addBox(-1.5F, -0.4F, 1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(138, 132)
                        .addBox(-1.5F, -0.4F, -2.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(200, 86)
                        .addBox(-2.5F, -0.4F, -1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(23, 151)
                        .addBox(1.5F, -0.4F, -1.5F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(10.5F, -13.6F, 3.0F));

        PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create(),
                PartPose.offset(14.0F, -7.0F, 0.0F));

        PartDefinition lShoulder = leftArm.addOrReplaceChild("lShoulder",
                CubeListBuilder.create().texOffs(129, 67)
                        .addBox(5.0F, -2.0F, -1.7F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(138, 117)
                        .addBox(4.0F, -3.0F, -2.7F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(165, 165)
                        .addBox(1.0F, -4.5F, -4.2F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(39, 172)
                        .addBox(2.0F, -4.0F, -3.7F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -1.5F, 3.2F, -1.1345F, 0.0F, 0.0F));

        PartDefinition lArmGills = lShoulder.addOrReplaceChild("lArmGills", CubeListBuilder.create(),
                PartPose.offsetAndRotation(17.0F, -4.5F, -1.2F, 0.2521F, -0.0017F, -0.1366F));

        PartDefinition cube_r15 = lArmGills.addOrReplaceChild("cube_r15",
                CubeListBuilder.create().texOffs(86, 197).addBox(-6.7691F, 2.1034F, -2.9381F, 3.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1812F, -1.155F, 2.6108F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r16 = lArmGills.addOrReplaceChild("cube_r16",
                CubeListBuilder.create().texOffs(114, 199).addBox(-6.2691F, 2.1034F, -2.9381F, 2.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.6812F, -1.655F, 1.8108F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r17 = lArmGills.addOrReplaceChild("cube_r17",
                CubeListBuilder.create().texOffs(197, 197).addBox(-6.7691F, 2.1034F, -2.9381F, 3.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1812F, -0.655F, 1.8108F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r18 = lArmGills.addOrReplaceChild("cube_r18",
                CubeListBuilder.create().texOffs(119, 199).addBox(-6.2691F, 2.1034F, -2.9381F, 2.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.6812F, -0.655F, 0.8108F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r19 = lArmGills.addOrReplaceChild("cube_r19",
                CubeListBuilder.create().texOffs(91, 161).addBox(-5.7691F, 2.1034F, -2.9381F, 1.0F, 9.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.1812F, -0.455F, -0.1892F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r20 = lArmGills.addOrReplaceChild("cube_r20",
                CubeListBuilder.create().texOffs(102, 147).addBox(-5.7691F, 2.6034F, -2.9381F, 1.0F, 8.0F, 0.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.1812F, -1.1767F, 3.1422F, 0.5672F, 0.0F, 0.0F));

        PartDefinition lBicep = lShoulder.addOrReplaceChild("lBicep", CubeListBuilder.create().texOffs(146, 162)
                        .addBox(-0.3954F, -2.4176F, -2.5F, 8.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(197, 127)
                        .addBox(-1.3954F, -1.4176F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(160, 78)
                        .addBox(-0.3954F, -2.4176F, -2.5F, 8.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(180, 101)
                        .addBox(0.6046F, -2.9176F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(176, 165)
                        .addBox(0.6046F, 2.0824F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(180, 128)
                        .addBox(0.1046F, -1.9176F, -3.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(103, 180)
                        .addBox(0.1046F, -1.9176F, 2.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(8.0F, 0.5F, -0.2F, 0.0F, -0.829F, 1.309F));

        PartDefinition lElbow = lBicep.addOrReplaceChild("lElbow",
                CubeListBuilder.create().texOffs(102, 32)
                        .addBox(0.0046F, -1.4176F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(172, 46)
                        .addBox(1.0046F, 1.0824F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(142, 138)
                        .addBox(1.0046F, -1.9176F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(69, 161)
                        .addBox(0.5046F, -0.9176F, -2.0F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(121, 153)
                        .addBox(0.5046F, -0.9176F, 1.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(7.6F, 0.0F, 0.0F, -0.5672F, 0.0F, 0.0F));

        PartDefinition lFore = lElbow.addOrReplaceChild("lFore",
                CubeListBuilder.create().texOffs(47, 161)
                        .addBox(0.0046F, -2.4176F, -2.5F, 8.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 173)
                        .addBox(1.0046F, -2.9176F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(168, 25)
                        .addBox(1.0046F, 2.0824F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(141, 173)
                        .addBox(0.5046F, -1.9176F, -3.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(70, 172)
                        .addBox(0.5046F, -1.9176F, 2.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(198, 37)
                        .addBox(7.5046F, -1.4176F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.3491F, 0.8727F));

        PartDefinition lHand = lFore.addOrReplaceChild("lHand", CubeListBuilder.create().texOffs(198, 0).addBox(0.0F,
                -1.4176F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.5046F, 0.0F, -0.5F));

        PartDefinition lUpperClaw = lHand.addOrReplaceChild("lUpperClaw",
                CubeListBuilder.create().texOffs(181, 111)
                        .addBox(0.6046F, -2.7176F, -1.0F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 12)
                        .addBox(0.6046F, 1.2824F, -1.0F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(144, 103)
                        .addBox(5.6046F, -1.7176F, -2.0F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(117, 79)
                        .addBox(7.6046F, -1.3176F, -2.0F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(152, 22)
                        .addBox(0.6046F, -1.7176F, -2.0F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(37, 126)
                        .addBox(0.6046F, 0.2824F, -2.0F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(28, 80)
                        .addBox(-0.3954F, -0.7176F, -2.3F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 80)
                        .addBox(4.6046F, -0.7176F, -2.3F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.3954F, 0.3F, 0.0F));

        PartDefinition lLowerClaw = lHand.addOrReplaceChild("lLowerClaw",
                CubeListBuilder.create().texOffs(178, 62)
                        .addBox(-0.3954F, -0.7176F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(69, 15)
                        .addBox(0.6046F, -0.7176F, 1.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(74, 66)
                        .addBox(0.6046F, -0.7176F, 0.0F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(42, 15)
                        .addBox(0.6046F, 0.2824F, 0.0F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(80, 178)
                        .addBox(4.6046F, -0.7176F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(75, 178)
                        .addBox(5.6046F, -0.7176F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.3954F, 0.3F, 2.0F));

        PartDefinition rightArm = body.addOrReplaceChild("rightArm", CubeListBuilder.create(),
                PartPose.offset(-11.0F, -8.0F, 1.0F));

        PartDefinition rShoulder = rightArm.addOrReplaceChild("rShoulder",
                CubeListBuilder.create().texOffs(43, 93)
                        .addBox(-3.0F, -2.0F, -1.7F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(66, 121)
                        .addBox(-2.0F, -3.0F, -2.7F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 64)
                        .addBox(1.0F, -4.5F, -4.2F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(20, 172)
                        .addBox(-1.0F, -4.0F, -3.7F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.0F, -0.5F, 3.2F, 2.0071F, 0.0F, 0.0F));

        PartDefinition rArmGills = rShoulder.addOrReplaceChild("rArmGills", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-7.0F, -2.5F, -3.2F, 3.0843F, 0.1423F, -0.1537F));

        PartDefinition cube_r21 = rArmGills.addOrReplaceChild("cube_r21",
                CubeListBuilder.create().texOffs(86, 197).mirror()
                        .addBox(4.8958F, -6.5919F, -3.1219F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-6.8933F, -0.6956F, 4.5451F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r22 = rArmGills.addOrReplaceChild("cube_r22",
                CubeListBuilder.create().texOffs(114, 199).mirror()
                        .addBox(5.3958F, -6.5919F, -3.1219F, 2.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-6.3933F, -1.1956F, 3.7451F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r23 = rArmGills.addOrReplaceChild("cube_r23",
                CubeListBuilder.create().texOffs(197, 197).mirror()
                        .addBox(4.8958F, -6.5919F, -3.1219F, 3.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-6.8933F, -0.1956F, 3.7451F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r24 = rArmGills.addOrReplaceChild("cube_r24",
                CubeListBuilder.create().texOffs(119, 199).mirror()
                        .addBox(5.3958F, -6.5919F, -3.1219F, 2.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-6.3933F, -0.1956F, 2.7451F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r25 = rArmGills.addOrReplaceChild("cube_r25",
                CubeListBuilder.create().texOffs(91, 161).mirror()
                        .addBox(5.8958F, -6.5919F, -3.1219F, 1.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-5.8933F, 0.0044F, 1.7451F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r26 = rArmGills.addOrReplaceChild("cube_r26",
                CubeListBuilder.create().texOffs(102, 147).mirror()
                        .addBox(5.8958F, -6.0919F, -3.1219F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(-5.8933F, -0.7173F, 5.0765F, 0.5672F, 0.0F, 0.0F));

        PartDefinition rBicep = rShoulder.addOrReplaceChild("rBicep", CubeListBuilder.create().texOffs(158, 132)
                        .addBox(-9.3954F, -2.4176F, -2.5F, 8.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(197, 59)
                        .addBox(-1.3954F, -1.4176F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(160, 89)
                        .addBox(-8.3954F, -2.9176F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(157, 125)
                        .addBox(-8.3954F, 2.0824F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(106, 168)
                        .addBox(-8.8954F, -1.9176F, -3.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(155, 0)
                        .addBox(-8.8954F, -1.9176F, 2.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5F, 0.5F, -0.2F, 0.0F, -0.6109F, 1.2654F));

        PartDefinition rElbow = rBicep.addOrReplaceChild("rElbow",
                CubeListBuilder.create().texOffs(43, 31)
                        .addBox(-5.9954F, -1.4176F, -1.5F, 6.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(130, 28)
                        .addBox(-4.9954F, 1.0824F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(74, 118)
                        .addBox(-4.9954F, -1.9176F, -1.0F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(114, 88)
                        .addBox(-5.4954F, -0.9176F, -2.0F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(110, 52)
                        .addBox(-5.4954F, -0.9176F, 1.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-9.4F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

        PartDefinition rFore = rElbow.addOrReplaceChild("rFore",
                CubeListBuilder.create().texOffs(119, 157)
                        .addBox(-7.9954F, -2.4176F, -2.5F, 8.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(154, 71)
                        .addBox(-6.9954F, -2.9176F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(34, 88)
                        .addBox(-6.9954F, 2.0824F, -1.5F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(94, 72)
                        .addBox(-7.4954F, -1.9176F, -3.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 33)
                        .addBox(-7.4954F, -1.9176F, 2.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(129, 168)
                        .addBox(-8.9954F, -1.4176F, -1.5F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.2182F, 1.0036F));

        PartDefinition rHand = rFore.addOrReplaceChild("rHand", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-8.4954F, 0.0F, -0.5F, 1.4835F, 2.8798F, 0.7418F));

        PartDefinition rHTent1 = rHand.addOrReplaceChild("rHTent1",
                CubeListBuilder.create().texOffs(190, 22).addBox(-0.9268F, -1.231F, -2.8888F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.3959F, 0.1431F, -0.0452F, -0.8047F, -0.4448F, -1.4696F));

        PartDefinition tent43 = rHTent1.addOrReplaceChild("tent43",
                CubeListBuilder.create().texOffs(0, 190).addBox(-0.8614F, -1.1759F, -3.6689F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0555F, -0.0351F, -2.6395F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent44 = tent43.addOrReplaceChild("tent44",
                CubeListBuilder.create().texOffs(189, 43).addBox(-1.215F, -0.5255F, -3.361F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3437F, -0.4013F, -3.9596F, 0.9163F, 0.0F, 0.0F));

        PartDefinition tent45 = tent44.addOrReplaceChild("tent45",
                CubeListBuilder.create().texOffs(188, 134).addBox(-1.0759F, -1.0716F, -3.4369F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1291F, 0.6675F, -3.1555F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent46 = tent45.addOrReplaceChild("tent46",
                CubeListBuilder.create().texOffs(91, 172).addBox(31.9827F, 16.0457F, -13.8181F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-32.6686F, -18.6186F, 4.3332F, 0.2182F, 0.0F, 0.0F));

        PartDefinition rHTent2 = rHand.addOrReplaceChild("rHTent2",
                CubeListBuilder.create().texOffs(182, 74).addBox(-0.9268F, -1.231F, -2.8888F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.3959F, 0.1431F, -0.0452F, 0.8727F, -2.4871F, -2.4435F));

        PartDefinition tent47 = rHTent2.addOrReplaceChild("tent47",
                CubeListBuilder.create().texOffs(51, 172).addBox(-0.8614F, -1.1759F, -3.6689F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0555F, -0.0351F, -2.6395F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent48 = tent47.addOrReplaceChild("tent48",
                CubeListBuilder.create().texOffs(32, 172).addBox(-1.2254F, -0.9537F, -3.6968F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.354F, -0.1984F, -3.4547F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent49 = tent48.addOrReplaceChild("tent49",
                CubeListBuilder.create().texOffs(145, 43).addBox(-1.0759F, -1.0716F, -3.4369F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1395F, 0.2393F, -3.4912F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent50 = tent49.addOrReplaceChild("tent50",
                CubeListBuilder.create().texOffs(118, 138).addBox(31.9827F, 16.0457F, -13.8181F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-32.6686F, -18.6186F, 4.3332F, 0.2182F, 0.0F, 0.0F));

        PartDefinition rHTent3 = rHand.addOrReplaceChild("rHTent3",
                CubeListBuilder.create().texOffs(188, 62).addBox(-0.9268F, -1.231F, -2.8888F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.3959F, 0.1431F, -0.0452F, -1.0906F, 0.8223F, -2.8626F));

        PartDefinition tent51 = rHTent3.addOrReplaceChild("tent51",
                CubeListBuilder.create().texOffs(187, 177).addBox(-0.8614F, -1.1759F, -3.6689F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0555F, -0.0351F, -2.6395F, 0.48F, 0.0F, 0.0F));

        PartDefinition tent52 = tent51.addOrReplaceChild("tent52",
                CubeListBuilder.create().texOffs(187, 170).addBox(-1.5731F, -1.2109F, -4.0175F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.7018F, -0.1359F, -3.0483F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent53 = tent52.addOrReplaceChild("tent53",
                CubeListBuilder.create().texOffs(187, 116).addBox(-1.0759F, -1.0716F, -3.4369F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.4872F, -0.018F, -3.812F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent54 = tent53.addOrReplaceChild("tent54",
                CubeListBuilder.create().texOffs(153, 132).addBox(31.9827F, 16.0457F, -13.8181F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-32.6686F, -18.6186F, 4.3332F, 0.2182F, 0.0F, 0.0F));

        PartDefinition rHTent4 = rHand.addOrReplaceChild("rHTent4",
                CubeListBuilder.create().texOffs(182, 187).addBox(-0.9268F, -1.231F, -2.8888F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.3959F, 0.1431F, -0.0452F, -2.4871F, -0.0873F, 1.3526F));

        PartDefinition tent55 = rHTent4.addOrReplaceChild("tent55",
                CubeListBuilder.create().texOffs(30, 187).addBox(-0.8614F, -1.1759F, -3.6689F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.0555F, -0.0351F, -2.6395F, 0.4835F, -0.116F, -0.0607F));

        PartDefinition tent56 = tent55.addOrReplaceChild("tent56",
                CubeListBuilder.create().texOffs(17, 187).addBox(-0.7267F, -1.12F, -3.6458F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.1447F, -0.0289F, -3.4157F, 0.5236F, 0.0F, 0.0F));

        PartDefinition tent57 = tent56.addOrReplaceChild("tent57",
                CubeListBuilder.create().texOffs(185, 0).addBox(-1.0759F, -1.0716F, -3.4369F, 2.0F, 2.0F, 4.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.3592F, 0.073F, -3.4403F, 0.6545F, 0.0F, 0.0F));

        PartDefinition tent58 = tent57.addOrReplaceChild("tent58",
                CubeListBuilder.create().texOffs(148, 201).addBox(31.9827F, 16.0457F, -13.8181F, 1.0F, 1.0F, 3.0F,
                        new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-32.6686F, -18.6186F, 4.3332F, 0.2182F, 0.0F, 0.0F));

        PartDefinition tail = wholeBody.addOrReplaceChild("tail", CubeListBuilder.create(),
                PartPose.offset(-1.0F, -6.0F, 15.6F));

        PartDefinition foot = tail.addOrReplaceChild("foot", CubeListBuilder.create(),
                PartPose.offset(1.0F, -2.0F, -0.6F));

        PartDefinition bone5 = foot.addOrReplaceChild("bone5",
                CubeListBuilder.create().texOffs(46, 46)
                        .addBox(4.0F, 8.0F, -10.0F, 4.0F, 0.0F, 19.0F, new CubeDeformation(0.0F)).texOffs(0, 64)
                        .addBox(-8.0F, 8.0F, -10.0F, 4.0F, 0.0F, 19.0F, new CubeDeformation(0.0F)).texOffs(42, 10)
                        .addBox(-8.0F, 8.0F, -14.0F, 16.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(110, 47)
                        .addBox(-6.0F, 8.0F, -18.0F, 12.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(130, 83)
                        .addBox(-4.0F, 4.0F, -14.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(24, 132)
                        .addBox(-3.0F, 3.0F, -12.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(155, 103)
                        .addBox(-5.0F, 2.0F, -10.0F, 10.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(36, 152)
                        .addBox(-5.0F, -1.0F, -6.0F, 10.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(74, 61)
                        .addBox(-5.0F, 1.0F, -8.0F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(144, 12)
                        .addBox(-6.0F, 3.0F, -6.0F, 12.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(74, 46)
                        .addBox(-6.0F, 5.0F, -2.0F, 12.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(0, 103)
                        .addBox(-5.0F, 2.0F, -2.0F, 10.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(78, 111)
                        .addBox(-4.0F, -1.0F, -2.0F, 8.0F, 3.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(78, 111)
                        .addBox(-2.9F, -3.9F, -0.1F, 6.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -1.4F));

        PartDefinition bone = bone5.addOrReplaceChild("bone",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-6.0F, -2.0F, 0.0F, 12.0F, 3.0F, 17.0F, new CubeDeformation(0.0F)).texOffs(94, 61)
                        .addBox(-9.0F, 1.0F, 0.0F, 3.0F, 0.0F, 17.0F, new CubeDeformation(0.0F)).texOffs(89, 14)
                        .addBox(6.0F, 1.0F, 0.0F, 3.0F, 0.0F, 17.0F, new CubeDeformation(0.0F)).texOffs(42, 80)
                        .addBox(-3.0F, -4.0F, 12.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(43, 89)
                        .addBox(-5.0F, -5.0F, 0.0F, 10.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(128, 0)
                        .addBox(-4.0F, -6.0F, 0.0F, 8.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 7.0F, 9.0F));

        PartDefinition seg10 = bone.addOrReplaceChild("seg10",
                CubeListBuilder.create().texOffs(0, 118)
                        .addBox(-4.0F, -1.9F, -2.0F, 8.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(0, 162)
                        .addBox(-7.0F, 0.99F, -2.0F, 3.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(74, 161)
                        .addBox(4.0F, 0.99F, -2.0F, 3.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(75, 10)
                        .addBox(-3.0F, -3.0F, -1.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 17.0F));

        PartDefinition endSeg = seg10.addOrReplaceChild("endSeg",
                CubeListBuilder.create().texOffs(0, 21)
                        .addBox(-6.0F, 1.0F, -2.0F, 12.0F, 0.0F, 18.0F, new CubeDeformation(0.0F)).texOffs(138, 117)
                        .addBox(-8.0F, 1.0F, -1.0F, 2.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)).texOffs(45, 137)
                        .addBox(6.0F, 1.0F, -1.0F, 2.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)).texOffs(97, 0)
                        .addBox(-4.5F, 0.1F, -0.8958F, 9.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(0, 147)
                        .addBox(-3.0F, -1.0F, -1.0F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(158, 143)
                        .addBox(-2.0F, -1.8F, -1.0F, 4.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(158, 143)
                        .addBox(-2.0F, 0.2F, 6.0F, 4.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 8.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    private final ModelPart root;

    public MycophantModel(ModelPart root) {
        this.root = root.getChild("root");
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               int packedColor) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(MycophantEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        double frame = ageInTicks;

        ModelPart wholeBody = this.root.getChild("wholeBody");
        ModelPart lowerColumn = wholeBody.getChild("lowerColumn");
        ModelPart upperColumn = lowerColumn.getChild("upperColumn");
        ModelPart upperTorso = upperColumn.getChild("upperTorso");
        ModelPart neck = upperTorso.getChild("neck");
        ModelPart neck2 = neck.getChild("neck2");
        ModelPart neck1 = neck2.getChild("neck1");
        ModelPart lGills = neck1.getChild("lGills");
        ModelPart rGills = neck1.getChild("rGills");

        ModelPart face = neck1.getChild("face");
        ModelPart eyes = face.getChild("eyes");
        ModelPart eye = eyes.getChild("eye");
        ModelPart eye2 = eyes.getChild("eye2");
        ModelPart eye3 = eyes.getChild("eye3");
        ModelPart eye4 = eyes.getChild("eye4");
        ModelPart eye5 = eyes.getChild("eye5");
        ModelPart eye6 = eyes.getChild("eye6");
        ModelPart eye7 = eyes.getChild("eye7");
        ModelPart eye8 = eyes.getChild("eye8");
        ModelPart eye9 = eyes.getChild("eye9");
        ModelPart eye10 = eyes.getChild("eye10");
        ModelPart eye11 = eyes.getChild("eye11");
        ModelPart eye12 = eyes.getChild("eye12");

        ModelPart tentacles = face.getChild("tentacles");
        ModelPart tent6 = tentacles.getChild("tent6");
        ModelPart tent7 = tent6.getChild("tent7");
        ModelPart tent8 = tent7.getChild("tent8");
        ModelPart tent9 = tent8.getChild("tent9");
        ModelPart tent10 = tent9.getChild("tent10");
        ModelPart tent22 = tentacles.getChild("tent22");
        ModelPart tent23 = tent22.getChild("tent23");
        ModelPart tent24 = tent23.getChild("tent24");
        ModelPart tent25 = tent24.getChild("tent25");
        ModelPart tent26 = tent25.getChild("tent26");
        ModelPart tent2 = tentacles.getChild("tent2");
        ModelPart tent3 = tent2.getChild("tent3");
        ModelPart tent4 = tent3.getChild("tent4");
        ModelPart tent5 = tent4.getChild("tent5");
        ModelPart tent11 = tent5.getChild("tent11");
        ModelPart tent12 = tentacles.getChild("tent12");
        ModelPart tent13 = tent12.getChild("tent13");
        ModelPart tent14 = tent13.getChild("tent14");
        ModelPart tent15 = tent14.getChild("tent15");
        ModelPart tent16 = tent15.getChild("tent16");
        ModelPart tent17 = tentacles.getChild("tent17");
        ModelPart tent18 = tent17.getChild("tent18");
        ModelPart tent19 = tent18.getChild("tent19");
        ModelPart tent20 = tent19.getChild("tent20");
        ModelPart tent21 = tent20.getChild("tent21");

        ModelPart body = upperTorso.getChild("body");
        ModelPart backEye = body.getChild("backEye");
        ModelPart backEye2 = body.getChild("backEye2");
        ModelPart backEye3 = body.getChild("backEye3");
        ModelPart backEye4 = body.getChild("backEye4");
        ModelPart backEye5 = body.getChild("backEye5");
        ModelPart backEye6 = body.getChild("backEye6");

        ModelPart leftArm = body.getChild("leftArm");
        ModelPart lShoulder = leftArm.getChild("lShoulder");
        ModelPart lBicep = lShoulder.getChild("lBicep");
        ModelPart lElbow = lBicep.getChild("lElbow");
        ModelPart lFore = lElbow.getChild("lFore");
        ModelPart lHand = lFore.getChild("lHand");
        ModelPart lLowerClaw = lHand.getChild("lLowerClaw");

        ModelPart rightArm = body.getChild("rightArm");
        ModelPart rShoulder = rightArm.getChild("rShoulder");
        ModelPart rBicep = rShoulder.getChild("rBicep");
        ModelPart rElbow = rBicep.getChild("rElbow");
        ModelPart rFore = rElbow.getChild("rFore");
        ModelPart rHand = rFore.getChild("rHand");
        ModelPart rHTent1 = rHand.getChild("rHTent1");
        ModelPart rHTent2 = rHand.getChild("rHTent2");
        ModelPart rHTent3 = rHand.getChild("rHTent3");
        ModelPart rHTent4 = rHand.getChild("rHTent4");

        ModelPart tail = wholeBody.getChild("tail");
        ModelPart foot = tail.getChild("foot");
        ModelPart bone5 = foot.getChild("bone5");
        ModelPart bone = bone5.getChild("bone");
        ModelPart seg10 = bone.getChild("seg10");
        ModelPart endSeg = seg10.getChild("endSeg");

        ModelPart armTent = upperTorso.getChild("armTent");
        ModelPart shoulder = armTent.getChild("shoulder");
        ModelPart seg3 = shoulder.getChild("seg3");
        ModelPart bone3 = seg3.getChild("bone3");
        ModelPart seg2 = bone3.getChild("seg2");
        ModelPart seg1 = seg2.getChild("seg1");
        ModelPart hand = seg1.getChild("hand");

        ModelPart armTent2 = upperTorso.getChild("armTent2");
        ModelPart shoulder2 = armTent2.getChild("shoulder2");
        ModelPart seg4 = shoulder2.getChild("seg4");
        ModelPart bone2 = seg4.getChild("bone2");
        ModelPart seg5 = bone2.getChild("seg5");
        ModelPart seg6 = seg5.getChild("seg6");
        ModelPart hand2 = seg6.getChild("hand2");

        ModelPart armTent3 = upperTorso.getChild("armTent3");
        ModelPart shoulder3 = armTent3.getChild("shoulder3");
        ModelPart seg7 = shoulder3.getChild("seg7");
        ModelPart bone4 = seg7.getChild("bone4");
        ModelPart seg8 = bone4.getChild("seg8");
        ModelPart seg9 = seg8.getChild("seg9");
        ModelPart hand3 = seg9.getChild("hand3");

        ModelPart armTent4 = upperTorso.getChild("armTent4");
        ModelPart shoulder4 = armTent4.getChild("shoulder4");
        ModelPart seg11 = shoulder4.getChild("seg11");
        ModelPart bone6 = seg11.getChild("bone6");
        ModelPart seg12 = bone6.getChild("seg12");
        ModelPart seg13 = seg12.getChild("seg13");
        ModelPart hand4 = seg13.getChild("hand4");

        lGills.yRot = (float) Math.abs((Math.cos((frame) * 0.5f) * 0.1325));
        rGills.yRot = (float) Math.abs((Math.cos((frame) * 0.5f) * 0.1325));
        lGills.xRot = (float) Math.abs((Math.cos((frame) * 0.5f) * 0.1325));
        rGills.xRot = (float) Math.abs((Math.cos((frame) * 0.5f) * 0.1325));
        // Upper Body
        // this.upperTorso.rotateAngleX = (float) (Math.sin((frame) * 0.13f) * 0.0325);

        // Eyes
        eye.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye2.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye2.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye3.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye3.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye4.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye4.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye5.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye5.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye6.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye6.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye7.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye7.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye8.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye8.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye9.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye9.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye10.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye10.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye11.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye11.yRot = (float) (Math.cos((frame)) * 0.0325);
        eye12.zRot = (float) (Math.sin((frame)) * 0.0325);
        eye12.yRot = (float) (Math.cos((frame)) * 0.0325);
        // cheese monke
        // Body Eyes
        backEye.zRot = (float) (Math.sin((frame)) * 0.0325);
        backEye.yRot = (float) (Math.cos((frame)) * 0.0325);
        backEye2.zRot = (float) (Math.sin((frame)) * 0.0325);
        backEye2.yRot = (float) (Math.cos((frame)) * 0.0325);
        backEye3.zRot = (float) (Math.sin((frame)) * 0.0325);
        backEye3.yRot = (float) (Math.cos((frame)) * 0.0325);
        backEye4.zRot = (float) (Math.sin((frame)) * 0.0325);
        backEye4.yRot = (float) (Math.cos((frame)) * 0.0325);
        backEye5.zRot = (float) (Math.sin((frame)) * 0.0325);
        backEye5.yRot = (float) (Math.cos((frame)) * 0.0325);
        backEye6.zRot = (float) (Math.sin((frame)) * 0.0325);
        backEye6.yRot = (float) (Math.cos((frame)) * 0.0325);

        /*
         * * // Left Arm this.leftArm.rotateAngleX = MathHelper.sin(limbSwing * 0.1662F
         * + (float) Math.PI) * 1.2F * limbSwingAmount; this.lFore.rotateAngleX =
         * MathHelper.cos(limbSwing * 0.1662F) * 1.2F * limbSwingAmount;
         * this.lHand.rotateAngleX = -Math .abs(MathHelper.sin(limbSwing * 0.1662F +
         * (float) Math.PI) * 1.2F * limbSwingAmount); // Claw
         * this.lLowerClaw.rotateAngleY = (float) -Math.abs((Math.cos((frame) * 0.2f) *
         * 0.2325));
         *
         * // Right Arm this.rightArm.rotateAngleX = -MathHelper.cos(limbSwing *
         * 0.1662F) * 1.2F * limbSwingAmount; this.rFore.rotateAngleX =
         * MathHelper.cos(limbSwing * 0.1662F) * 1.2F * limbSwingAmount;
         * this.rHand.rotateAngleX = -Math.abs(MathHelper.cos(limbSwing * 0.1662F) *
         * 1.2F * limbSwingAmount);
         */
        lLowerClaw.yRot = (float) -Math.abs((Math.cos((frame) * 0.2f) * 0.2325));
        // Right Hand Tentacles
        rHTent1.xRot = (float) (Math.sin((frame) * 0.53f) * 0.0325) - 0.75f;
        rHTent2.xRot = (float) (Math.sin((frame) * 0.53f) * 0.0325) - 5.5f;
        rHTent3.xRot = (float) (Math.sin((frame) * 0.53f) * 0.0325) - 1.5f;
        rHTent4.xRot = (float) (Math.sin((frame) * 0.53f) * 0.0325) - 2.5f;

        // Foot
        bone5.yRot = (float) Math.sin((frame) * 0.3f) * 0.05f;
        bone.yRot = (float) Math.cos((frame) * 0.5f) * 0.1f - 44.0f;
        seg10.yRot = (float) Math.sin((frame) * 0.6f) * 0.15f;
        endSeg.yRot = (float) Math.cos((frame) * 0.7f) * 0.25f;

        // Tentacles
        // Face
        tent6.xRot = (float) Math.sin((frame) * 0.3f) * 0.05f + 25.5f;
        tent7.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f + 25.7f;
        tent8.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f + 25.5f;
        tent9.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f + 25.5f;
        tent10.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f + 25.5f;

        tent22.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f + 25.5f;
        tent23.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f + 25.7f;
        tent24.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f + 25.5f;
        tent25.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f + 25.5f;
        tent26.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f + 25.5f;

        tent2.xRot = (float) Math.sin((frame) * 0.3f) * 0.05f + 25.5f;
        tent3.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f + 25.7f;
        tent4.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f + 25.5f;
        tent5.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f + 25.5f;
        tent11.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f + 25.5f;

        tent12.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f + 25.5f;
        tent13.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f + 25.7f;
        tent14.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f + 25.5f;
        tent15.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f + 25.5f;
        tent16.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f + 25.5f;

        tent17.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f + 25.5f;
        tent18.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f + 25.7f;
        tent19.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f + 25.5f;
        tent20.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f + 25.5f;
        tent21.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f + 25.5f;
        // Body
        armTent.xRot = (float) Math.sin((frame) * 0.3f) * 0.05f;
        seg3.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
        seg2.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f;
        seg1.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f;
        hand.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f;

        armTent2.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f;
        seg4.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f;
        seg5.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
        seg6.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f;
        hand2.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;

        armTent3.xRot = (float) Math.sin((frame) * 0.3f) * 0.05f;
        seg7.xRot = (float) Math.cos((frame) * 0.5f) * 0.1f;
        bone4.xRot = (float) Math.sin((frame) * 0.6f) * 0.15f;
        seg9.xRot = (float) Math.cos((frame) * 0.7f) * 0.25f;
        hand3.xRot = (float) Math.sin((frame) * 0.8f) * 0.35f;

        armTent4.xRot = (float) Math.cos((frame) * 0.3f) * 0.05f;
        seg11.xRot = (float) Math.sin((frame) * 0.5f) * 0.1f;
        seg12.xRot = (float) Math.cos((frame) * 0.6f) * 0.15f;
        seg13.xRot = (float) Math.sin((frame) * 0.7f) * 0.25f;
        hand4.xRot = (float) Math.cos((frame) * 0.8f) * 0.35f;
    }
}
