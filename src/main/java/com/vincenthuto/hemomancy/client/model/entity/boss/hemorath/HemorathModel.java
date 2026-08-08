package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class HemorathModel<T extends HemorathEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("hemorath"), "main");
    private final ModelPart BackCloak;
    private final ModelPart Cloak1;
    private final ModelPart Cloak3;
    private final ModelPart Cloak2;
    private final ModelPart CloakCL;
    private final ModelPart CloakCR;
    private final ModelPart Head;
    private final ModelPart Head2;
    private final ModelPart CollarBlack;
    private final ModelPart FrontCloth;
    private final ModelPart Frontcloth2;
    private final ModelPart Frontcloth0;
    private final ModelPart Frontcloth1;
    private final ModelPart Torso;
    private final ModelPart CollarL;
    private final ModelPart CollarR;
    private final ModelPart CollarB;
    private final ModelPart CollarF;
    private final ModelPart ArmL;
    private final ModelPart ShoulderR1;
    private final ModelPart ShoulderR;
    private final ModelPart ShoulderR2;
    private final ModelPart ShoulderR0;
    private final ModelPart ArmR;
    private final ModelPart ShoulderL1;
    private final ModelPart ShoulderL0;
    private final ModelPart ShoulderL;
    private final ModelPart ShoulderL2;
    private final ModelPart BackpanelR1;
    private final ModelPart WaistR1;
    private final ModelPart WaistR2;
    private final ModelPart WaistR3;
    private final ModelPart LegL;
    private final ModelPart WaistL1;
    private final ModelPart WaistL2;
    private final ModelPart WaistL3;
    private final ModelPart BackpanelL1;
    private final ModelPart LegR;


    public HemorathModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.BackCloak = root.getChild("BackCloak");
        this.Cloak1 = this.BackCloak.getChild("Cloak1");
        this.Cloak3 = this.BackCloak.getChild("Cloak3");
        this.Cloak2 = this.BackCloak.getChild("Cloak2");
        this.CloakCL = this.BackCloak.getChild("CloakCL");
        this.CloakCR = this.BackCloak.getChild("CloakCR");
        this.Head = root.getChild("Head");
        this.Head2 = root.getChild("Head2");
        this.CollarBlack = root.getChild("CollarBlack");
        this.FrontCloth = root.getChild("FrontCloth");
        this.Frontcloth2 = this.FrontCloth.getChild("Frontcloth2");
        this.Frontcloth0 = this.FrontCloth.getChild("Frontcloth0");
        this.Frontcloth1 = this.FrontCloth.getChild("Frontcloth1");
        this.Torso = root.getChild("Torso");
        this.CollarL = this.Torso.getChild("CollarL");
        this.CollarR = this.Torso.getChild("CollarR");
        this.CollarB = this.Torso.getChild("CollarB");
        this.CollarF = this.Torso.getChild("CollarF");
        this.ArmL = root.getChild("ArmL");
        this.ShoulderR1 = this.ArmL.getChild("ShoulderR1");
        this.ShoulderR = this.ArmL.getChild("ShoulderR");
        this.ShoulderR2 = this.ArmL.getChild("ShoulderR2");
        this.ShoulderR0 = this.ArmL.getChild("ShoulderR0");
        this.ArmR = root.getChild("ArmR");
        this.ShoulderL1 = this.ArmR.getChild("ShoulderL1");
        this.ShoulderL0 = this.ArmR.getChild("ShoulderL0");
        this.ShoulderL = this.ArmR.getChild("ShoulderL");
        this.ShoulderL2 = this.ArmR.getChild("ShoulderL2");
        this.BackpanelR1 = root.getChild("BackpanelR1");
        this.WaistR1 = root.getChild("WaistR1");
        this.WaistR2 = root.getChild("WaistR2");
        this.WaistR3 = root.getChild("WaistR3");
        this.LegL = root.getChild("LegL");
        this.WaistL1 = root.getChild("WaistL1");
        this.WaistL2 = root.getChild("WaistL2");
        this.WaistL3 = root.getChild("WaistL3");
        this.BackpanelL1 = root.getChild("BackpanelL1");
        this.LegR = root.getChild("LegR");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition BackCloak = partdefinition.addOrReplaceChild("BackCloak", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 1.5F));

        PartDefinition Cloak1 = BackCloak.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-5.0F, 1.5F, 4.0F, 10.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition Cloak3 = BackCloak.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(0, 37).addBox(-5.0F, 17.5F, -0.8F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.4466F, 0.0F, 0.0F));

        PartDefinition Cloak2 = BackCloak.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-5.0F, 13.5F, 1.7F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.3069F, 0.0F, 0.0F));

        PartDefinition CloakCL = BackCloak.addOrReplaceChild("CloakCL", CubeListBuilder.create().texOffs(0, 43).addBox(-5.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition CloakCR = BackCloak.addOrReplaceChild("CloakCR", CubeListBuilder.create().texOffs(0, 43).addBox(3.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, -4.0F, 0.1396F, 0.0F, 0.0F));

        PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(47, 12).addBox(-3.5F, -6.0F, -2.5F, 7.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.5F, -3.8F, -0.1047F, 0.0F, 0.0F));

        PartDefinition Head2 = partdefinition.addOrReplaceChild("Head2", CubeListBuilder.create().texOffs(26, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, -0.1047F, 0.0F, 0.0F));

        PartDefinition CollarBlack = partdefinition.addOrReplaceChild("CollarBlack", CubeListBuilder.create().texOffs(22, 0).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.8378F, 0.0F, 0.0F));

        PartDefinition FrontCloth = partdefinition.addOrReplaceChild("FrontCloth", CubeListBuilder.create(), PartPose.offset(1.3333F, 7.6667F, -0.8333F));

        PartDefinition Frontcloth2 = FrontCloth.addOrReplaceChild("Frontcloth2", CubeListBuilder.create().texOffs(114, 47).addBox(-5.0F, 8.5F, -1.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6667F, 3.3333F, 0.8333F, -0.3316F, 0.0F, 0.0F));

        PartDefinition Frontcloth0 = FrontCloth.addOrReplaceChild("Frontcloth0", CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 3.2F, -3.5F, 6.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3333F, -7.6667F, -1.6667F, 0.1745F, 0.0F, 0.0F));

        PartDefinition Frontcloth1 = FrontCloth.addOrReplaceChild("Frontcloth1", CubeListBuilder.create().texOffs(114, 39).addBox(-5.0F, 1.5F, -3.5F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6667F, 4.3333F, 0.8333F, -0.1047F, 0.0F, 0.0F));

        PartDefinition Torso = partdefinition.addOrReplaceChild("Torso", CubeListBuilder.create().texOffs(34, 45).mirror().addBox(-5.0F, 2.5F, -3.0F, 10.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745F, 0.0F, 0.0F));

        PartDefinition CollarL = Torso.addOrReplaceChild("CollarL", CubeListBuilder.create().texOffs(75, 50).addBox(-4.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8378F, 0.0F, 0.0F));

        PartDefinition CollarR = Torso.addOrReplaceChild("CollarR", CubeListBuilder.create().texOffs(67, 50).addBox(3.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8378F, 0.0F, 0.0F));

        PartDefinition CollarB = Torso.addOrReplaceChild("CollarB", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, 2.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8378F, 0.0F, 0.0F));

        PartDefinition CollarF = Torso.addOrReplaceChild("CollarF", CubeListBuilder.create().texOffs(77, 59).addBox(-3.5F, -0.5F, -7.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8378F, 0.0F, 0.0F));

        PartDefinition ArmL = partdefinition.addOrReplaceChild("ArmL", CubeListBuilder.create().texOffs(78, 32).addBox(-0.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 3.0F, -2.0F, 0.0F, 0.0F, -0.1047F));

        PartDefinition ShoulderR1 = ArmL.addOrReplaceChild("ShoulderR1", CubeListBuilder.create().texOffs(0, 23).addBox(2.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1868F));

        PartDefinition ShoulderR = ArmL.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(0, 0).addBox(0.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1868F));

        PartDefinition ShoulderR2 = ArmL.addOrReplaceChild("ShoulderR2", CubeListBuilder.create().texOffs(0, 12).addBox(0.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1868F));

        PartDefinition ShoulderR0 = ArmL.addOrReplaceChild("ShoulderR0", CubeListBuilder.create().texOffs(56, 31).addBox(-0.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ArmR = partdefinition.addOrReplaceChild("ArmR", CubeListBuilder.create().texOffs(78, 32).mirror().addBox(-3.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, 3.0F, -2.0F, 0.0F, 0.0F, 0.1047F));

        PartDefinition ShoulderL1 = ArmR.addOrReplaceChild("ShoulderL1", CubeListBuilder.create().texOffs(0, 23).mirror().addBox(-3.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1868F));

        PartDefinition ShoulderL0 = ArmR.addOrReplaceChild("ShoulderL0", CubeListBuilder.create().texOffs(56, 31).mirror().addBox(-4.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderL = ArmR.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1868F));

        PartDefinition ShoulderL2 = ArmR.addOrReplaceChild("ShoulderL2", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-2.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1868F));

        PartDefinition BackpanelR1 = partdefinition.addOrReplaceChild("BackpanelR1", CubeListBuilder.create().texOffs(96, 7).addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

        PartDefinition WaistR1 = partdefinition.addOrReplaceChild("WaistR1", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

        PartDefinition WaistR2 = partdefinition.addOrReplaceChild("WaistR2", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

        PartDefinition WaistR3 = partdefinition.addOrReplaceChild("WaistR3", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

        PartDefinition LegL = partdefinition.addOrReplaceChild("LegL", CubeListBuilder.create().texOffs(79, 19).addBox(-1.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 12.5F, 0.0F));

        PartDefinition WaistL1 = partdefinition.addOrReplaceChild("WaistL1", CubeListBuilder.create().texOffs(96, 14).mirror().addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

        PartDefinition WaistL2 = partdefinition.addOrReplaceChild("WaistL2", CubeListBuilder.create().texOffs(116, 13).mirror().addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

        PartDefinition WaistL3 = partdefinition.addOrReplaceChild("WaistL3", CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

        PartDefinition BackpanelL1 = partdefinition.addOrReplaceChild("BackpanelL1", CubeListBuilder.create().texOffs(96, 7).mirror().addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

        PartDefinition LegR = partdefinition.addOrReplaceChild("LegR", CubeListBuilder.create().texOffs(79, 19).mirror().addBox(-2.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.0F, 12.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        int visualState = entity.getVisualState();
        if (visualState == HemorathEntity.VISUAL_OVERLOAD) {
            float rupture = (float) Math.sin(ageInTicks * 0.7F) * 0.18F;
            this.Head.zRot = -rupture;
            this.ArmR.xRot = -1.35F;
            this.ArmL.xRot = -1.35F;
            this.ArmR.zRot = 0.72F;
            this.ArmL.zRot = -0.72F;
        } else if (visualState == HemorathEntity.VISUAL_COLLAPSE_IMPACT) {
            this.ArmR.xRot = -1.45F;
            this.ArmL.xRot = -1.45F;
        } else if (entity.isCollapseCharging()) {
            this.ArmR.xRot = -1.1F;
            this.ArmL.xRot = -1.1F;
            this.ArmR.zRot = 0.42F;
            this.ArmL.zRot = -0.42F;
            this.Head.xRot = headPitch * ((float) Math.PI / 180F) - 0.18F;
        } else if (visualState == HemorathEntity.VISUAL_EMPTY_PULSE) {
            this.Torso.xRot = 0.18F;
            this.ArmR.xRot = -0.35F;
            this.ArmL.xRot = -0.35F;
            this.ArmR.zRot = 0.9F;
            this.ArmL.zRot = -0.9F;
        } else if (visualState == HemorathEntity.VISUAL_EMPTY_PULSE_IMPACT) {
            this.ArmR.zRot = 1.2F;
            this.ArmL.zRot = -1.2F;
        } else if (entity.isInPhase2()) {
            this.ArmR.xRot -= 0.25F;
            this.ArmL.xRot -= 0.25F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,int packedLight, int packedOverlay, int packedColor) {
        BackCloak.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        Head2.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        CollarBlack.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        FrontCloth.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        Torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        ArmL.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        ArmR.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        BackpanelR1.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        WaistR1.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        WaistR2.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        WaistR3.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        LegL.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        WaistL1.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        WaistL2.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        WaistL3.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        BackpanelL1.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        LegR.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }
}
