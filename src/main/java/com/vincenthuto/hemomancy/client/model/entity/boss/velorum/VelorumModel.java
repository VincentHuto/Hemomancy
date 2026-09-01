package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class VelorumModel<T extends VelorumEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("velorum"), "main");

    private final ModelPart Waist;
    private final ModelPart LegpanelR4;
    private final ModelPart LegpanelR5;
    private final ModelPart LegpanelR6;
    private final ModelPart BackpanelR1;
    private final ModelPart BackpanelR2;
    private final ModelPart BackpanelR3;
    private final ModelPart BackpanelL3;
    private final ModelPart LegpanelL4;
    private final ModelPart LegpanelL5;
    private final ModelPart LegpanelL6;
    private final ModelPart BackpanelL1;
    private final ModelPart BackpanelL2;
    private final ModelPart SidepanelL1;
    private final ModelPart SidepanelR1;
    private final ModelPart SidepanelR2;
    private final ModelPart SidepanelR3;
    private final ModelPart SidepanelR4;
    private final ModelPart SidepanelL2;
    private final ModelPart SidepanelL3;
    private final ModelPart SidepanelL4;
    private final ModelPart LegpanelC1;
    private final ModelPart LegpanelC2;
    private final ModelPart LegpanelC3;
    private final ModelPart ArmR;
    private final ModelPart ArmL1;
    private final ModelPart ArmL2;
    private final ModelPart ArmL3;
    private final ModelPart ShoulderL;
    private final ModelPart ShoulderplateLtop;
    private final ModelPart ShoulderplateL2;
    private final ModelPart ShoulderplateL3;
    private final ModelPart ShoulderplateR1;
    private final ModelPart ArmL;
    private final ModelPart ShoulderplateTopR;
    private final ModelPart ShoulderplateR2;
    private final ModelPart ShoulderplateR3;
    private final ModelPart ShoulderplateL1;
    private final ModelPart ArmR1;
    private final ModelPart ArmR2;
    private final ModelPart ArmR3;
    private final ModelPart ShoulderR;
    private final ModelPart Torso;
    private final ModelPart BeltR;
    private final ModelPart Mbelt;
    private final ModelPart MbeltL;
    private final ModelPart MbeltR;
    private final ModelPart BeltL;
    private final ModelPart Chestplate;
    private final ModelPart Cloak1;
    private final ModelPart Cloak2;
    private final ModelPart Cloak3;
    private final ModelPart Backplate;
    private final ModelPart Head;
    private final ModelPart HoodEye;
    private final ModelPart Hood1;
    private final ModelPart Hood2;
    private final ModelPart Hood3;
    private final ModelPart Hood4;




    public VelorumModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.Waist = root.getChild("Waist");
        this.LegpanelR4 = this.Waist.getChild("LegpanelR4");
        this.LegpanelR5 = this.Waist.getChild("LegpanelR5");
        this.LegpanelR6 = this.Waist.getChild("LegpanelR6");
        this.BackpanelR1 = this.Waist.getChild("BackpanelR1");
        this.BackpanelR2 = this.Waist.getChild("BackpanelR2");
        this.BackpanelR3 = this.Waist.getChild("BackpanelR3");
        this.BackpanelL3 = this.Waist.getChild("BackpanelL3");
        this.LegpanelL4 = this.Waist.getChild("LegpanelL4");
        this.LegpanelL5 = this.Waist.getChild("LegpanelL5");
        this.LegpanelL6 = this.Waist.getChild("LegpanelL6");
        this.BackpanelL1 = this.Waist.getChild("BackpanelL1");
        this.BackpanelL2 = this.Waist.getChild("BackpanelL2");
        this.SidepanelL1 = this.Waist.getChild("SidepanelL1");
        this.SidepanelR1 = this.Waist.getChild("SidepanelR1");
        this.SidepanelR2 = this.Waist.getChild("SidepanelR2");
        this.SidepanelR3 = this.SidepanelR2.getChild("SidepanelR3");
        this.SidepanelR4 = this.SidepanelR3.getChild("SidepanelR4");
        this.SidepanelL2 = this.Waist.getChild("SidepanelL2");
        this.SidepanelL3 = this.SidepanelL2.getChild("SidepanelL3");
        this.SidepanelL4 = this.SidepanelL3.getChild("SidepanelL4");
        this.LegpanelC1 = this.Waist.getChild("LegpanelC1");
        this.LegpanelC2 = this.LegpanelC1.getChild("LegpanelC2");
        this.LegpanelC3 = this.LegpanelC2.getChild("LegpanelC3");
        this.ArmR = root.getChild("ArmR");
        this.ArmL1 = this.ArmR.getChild("ArmL1");
        this.ArmL2 = this.ArmL1.getChild("ArmL2");
        this.ArmL3 = this.ArmL1.getChild("ArmL3");
        this.ShoulderL = this.ArmR.getChild("ShoulderL");
        this.ShoulderplateLtop = this.ArmR.getChild("ShoulderplateLtop");
        this.ShoulderplateL2 = this.ArmR.getChild("ShoulderplateL2");
        this.ShoulderplateL3 = this.ArmR.getChild("ShoulderplateL3");
        this.ShoulderplateR1 = this.ArmR.getChild("ShoulderplateR1");
        this.ArmL = root.getChild("ArmL");
        this.ShoulderplateTopR = this.ArmL.getChild("ShoulderplateTopR");
        this.ShoulderplateR2 = this.ArmL.getChild("ShoulderplateR2");
        this.ShoulderplateR3 = this.ArmL.getChild("ShoulderplateR3");
        this.ShoulderplateL1 = this.ArmL.getChild("ShoulderplateL1");
        this.ArmR1 = this.ArmL.getChild("ArmR1");
        this.ArmR2 = this.ArmR1.getChild("ArmR2");
        this.ArmR3 = this.ArmR1.getChild("ArmR3");
        this.ShoulderR = this.ArmL.getChild("ShoulderR");
        this.Torso = root.getChild("Torso");
        this.BeltR = this.Torso.getChild("BeltR");
        this.Mbelt = this.Torso.getChild("Mbelt");
        this.MbeltL = this.Torso.getChild("MbeltL");
        this.MbeltR = this.Torso.getChild("MbeltR");
        this.BeltL = this.Torso.getChild("BeltL");
        this.Chestplate = this.Torso.getChild("Chestplate");
        this.Cloak1 = this.Torso.getChild("Cloak1");
        this.Cloak2 = this.Cloak1.getChild("Cloak2");
        this.Cloak3 = this.Cloak2.getChild("Cloak3");
        this.Backplate = this.Torso.getChild("Backplate");
        this.Head = root.getChild("Head");
        this.HoodEye = this.Head.getChild("HoodEye");
        this.Hood1 = this.Head.getChild("Hood1");
        this.Hood2 = this.Hood1.getChild("Hood2");
        this.Hood3 = this.Hood1.getChild("Hood3");
        this.Hood4 = this.Hood1.getChild("Hood4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Waist = partdefinition.addOrReplaceChild("Waist", CubeListBuilder.create(), PartPose.offset(0.0F, 6.3824F, -0.4118F));

        PartDefinition LegpanelR4 = Waist.addOrReplaceChild("LegpanelR4", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.3824F, 0.4118F, -0.4363F, 0.0F, 0.0F));

        PartDefinition LegpanelR5 = Waist.addOrReplaceChild("LegpanelR5", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.3824F, 0.4118F, -0.4363F, 0.0F, 0.0F));

        PartDefinition LegpanelR6 = Waist.addOrReplaceChild("LegpanelR6", CubeListBuilder.create().texOffs(6, 43).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.3824F, 0.4118F, -0.4363F, 0.0F, 0.0F));

        PartDefinition BackpanelR1 = Waist.addOrReplaceChild("BackpanelR1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.3824F, 0.4118F, 0.4363F, 0.0F, 0.0F));

        PartDefinition BackpanelR2 = Waist.addOrReplaceChild("BackpanelR2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.3824F, 0.4118F, 0.4363F, 0.0F, 0.0F));

        PartDefinition BackpanelR3 = Waist.addOrReplaceChild("BackpanelR3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.3824F, 0.4118F, 0.4363F, 0.0F, 0.0F));

        PartDefinition BackpanelL3 = Waist.addOrReplaceChild("BackpanelL3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -0.3824F, 0.4118F, 0.4363F, 0.0F, 0.0F));

        PartDefinition LegpanelL4 = Waist.addOrReplaceChild("LegpanelL4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.3824F, 0.4118F, -0.4363F, 0.0F, 0.0F));

        PartDefinition LegpanelL5 = Waist.addOrReplaceChild("LegpanelL5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.3824F, 0.4118F, -0.4363F, 0.0F, 0.0F));

        PartDefinition LegpanelL6 = Waist.addOrReplaceChild("LegpanelL6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -0.3824F, 0.4118F, -0.4363F, 0.0F, 0.0F));

        PartDefinition BackpanelL1 = Waist.addOrReplaceChild("BackpanelL1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -0.3824F, 0.4118F, 0.4363F, 0.0F, 0.0F));

        PartDefinition BackpanelL2 = Waist.addOrReplaceChild("BackpanelL2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -0.3824F, 0.4118F, 0.4363F, 0.0F, 0.0F));

        PartDefinition SidepanelL1 = Waist.addOrReplaceChild("SidepanelL1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -0.3824F, 0.4118F, 0.0F, 0.0F, 0.4363F));

        PartDefinition SidepanelR1 = Waist.addOrReplaceChild("SidepanelR1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, -0.3824F, 0.4118F, 0.0F, 0.0F, -0.4363F));

        PartDefinition SidepanelR2 = Waist.addOrReplaceChild("SidepanelR2", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, 0.0F, -0.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.5F, 3.1176F, -1.5882F, 0.0F, 0.0F, -0.1222F));

        PartDefinition SidepanelR3 = SidepanelR2.addOrReplaceChild("SidepanelR3", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-1.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.2967F));

        PartDefinition SidepanelR4 = SidepanelR3.addOrReplaceChild("SidepanelR4", CubeListBuilder.create().texOffs(24, 35).mirror().addBox(-1.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition SidepanelL2 = Waist.addOrReplaceChild("SidepanelL2", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, 0.0F, -0.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.5F, 3.1176F, -1.5882F, 0.0F, 0.0F, 0.1222F));

        PartDefinition SidepanelL3 = SidepanelL2.addOrReplaceChild("SidepanelL3", CubeListBuilder.create().texOffs(0, 35).mirror().addBox(-1.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.2967F));

        PartDefinition SidepanelL4 = SidepanelL3.addOrReplaceChild("SidepanelL4", CubeListBuilder.create().texOffs(24, 35).mirror().addBox(-1.0F, 0.0F, -0.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition LegpanelC1 = Waist.addOrReplaceChild("LegpanelC1", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.0F, 0.0F, -0.5F, 6.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -0.8824F, -2.5882F));

        PartDefinition LegpanelC2 = LegpanelC1.addOrReplaceChild("LegpanelC2", CubeListBuilder.create().texOffs(16, 54).mirror().addBox(-3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition LegpanelC3 = LegpanelC2.addOrReplaceChild("LegpanelC3", CubeListBuilder.create().texOffs(32, 59).mirror().addBox(-3.0F, 0.0F, -0.5F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition ArmR = partdefinition.addOrReplaceChild("ArmR", CubeListBuilder.create(), PartPose.offset(-5.0F, -4.0F, 0.0F));

        PartDefinition ArmL1 = ArmR.addOrReplaceChild("ArmL1", CubeListBuilder.create().texOffs(72, 8).mirror().addBox(-3.0F, 2.5F, -1.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9599F, 0.1047F, 0.192F));

        PartDefinition ArmL2 = ArmL1.addOrReplaceChild("ArmL2", CubeListBuilder.create().texOffs(76, 28).mirror().addBox(-3.0F, 9.5F, 3.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ArmL3 = ArmL1.addOrReplaceChild("ArmL3", CubeListBuilder.create().texOffs(76, 23).mirror().addBox(-3.0F, 6.5F, 3.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderL = ArmR.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, 0.1222F, 0.0349F));

        PartDefinition ShoulderplateLtop = ArmR.addOrReplaceChild("ShoulderplateLtop", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, 0.3142F, 0.4363F));

        PartDefinition ShoulderplateL2 = ArmR.addOrReplaceChild("ShoulderplateL2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, 0.3142F, 0.4363F));

        PartDefinition ShoulderplateL3 = ArmR.addOrReplaceChild("ShoulderplateL3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, 0.3142F, 0.4363F));

        PartDefinition ShoulderplateR1 = ArmR.addOrReplaceChild("ShoulderplateR1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, 0.3142F, 0.4363F));

        PartDefinition ArmL = partdefinition.addOrReplaceChild("ArmL", CubeListBuilder.create(), PartPose.offset(5.0F, -4.0F, 0.0F));

        PartDefinition ShoulderplateTopR = ArmL.addOrReplaceChild("ShoulderplateTopR", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, -0.3142F, -0.4363F));

        PartDefinition ShoulderplateR2 = ArmL.addOrReplaceChild("ShoulderplateR2", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, -0.3142F, -0.4363F));

        PartDefinition ShoulderplateR3 = ArmL.addOrReplaceChild("ShoulderplateR3", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, -0.3142F, -0.4363F));

        PartDefinition ShoulderplateL1 = ArmL.addOrReplaceChild("ShoulderplateL1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, -0.3142F, -0.4363F));

        PartDefinition ArmR1 = ArmL.addOrReplaceChild("ArmR1", CubeListBuilder.create().texOffs(72, 8).mirror().addBox(-1.0F, 2.5F, -1.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.9599F, -0.1047F, -0.192F));

        PartDefinition ArmR2 = ArmR1.addOrReplaceChild("ArmR2", CubeListBuilder.create().texOffs(76, 28).mirror().addBox(-1.0F, 9.5F, 3.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ArmR3 = ArmR1.addOrReplaceChild("ArmR3", CubeListBuilder.create().texOffs(76, 23).mirror().addBox(-1.0F, 6.5F, 3.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR = ArmL.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3665F, -0.1222F, -0.0349F));

        PartDefinition Torso = partdefinition.addOrReplaceChild("Torso", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition BeltR = Torso.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Mbelt = Torso.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(56, 55).mirror().addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MbeltL = Torso.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition MbeltR = Torso.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition BeltL = Torso.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(76, 44).mirror().addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Chestplate = Torso.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(56, 45).mirror().addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Cloak1 = Torso.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(106, 0).mirror().addBox(-10.0F, 0.0F, -0.5F, 10.0F, 18.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 0.0F, 4.0F));

        PartDefinition Cloak2 = Cloak1.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(106, 19).mirror().addBox(-10.0F, 0.0F, -0.5F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 18.0F, 0.0F));

        PartDefinition Cloak3 = Cloak2.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(106, 24).mirror().addBox(-10.0F, 0.0F, -0.5F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition Backplate = Torso.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).mirror().addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, -6.0F, 0.0F));

        PartDefinition HoodEye = Head.addOrReplaceChild("HoodEye", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hood1 = Head.addOrReplaceChild("Hood1", CubeListBuilder.create().texOffs(40, 12).mirror().addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hood2 = Hood1.addOrReplaceChild("Hood2", CubeListBuilder.create().texOffs(36, 28).mirror().addBox(-3.5F, -8.7F, 2.0F, 7.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition Hood3 = Hood1.addOrReplaceChild("Hood3", CubeListBuilder.create().texOffs(22, 19).mirror().addBox(-3.0F, -9.0F, 2.5F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Hood4 = Hood1.addOrReplaceChild("Hood4", CubeListBuilder.create().texOffs(40, 4).mirror().addBox(-2.5F, -9.7F, 3.5F, 5.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

        @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        int visualState = entity.getVisualState();
        if (entity.isMartyrdom()) {
        }
        if (visualState == VelorumEntity.VISUAL_FROST_NOVA) {
        } else if (visualState == VelorumEntity.VISUAL_VEIL) {
        } else if (visualState == VelorumEntity.VISUAL_SILENCE_DRAIN) {
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,int packedLight, int packedOverlay, int packedColor) {
        Waist.render(poseStack, vertexConsumer, packedLight, packedOverlay,packedColor);
        ArmR.render(poseStack, vertexConsumer, packedLight, packedOverlay,packedColor);
        ArmL.render(poseStack, vertexConsumer, packedLight, packedOverlay,packedColor);
        Torso.render(poseStack, vertexConsumer, packedLight, packedOverlay,packedColor);
        Head.render(poseStack, vertexConsumer, packedLight, packedOverlay,packedColor);
    }
}
