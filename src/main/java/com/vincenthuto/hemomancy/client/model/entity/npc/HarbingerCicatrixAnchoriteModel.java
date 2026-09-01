package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Humanoid model for the Harbinger Vicar — a dignified, ceremonially robed
 * figure whose sweeping vestments reflect ecclesiastical authority. Upright
 * posture and layered robes convey doctrinal gravitas.
 */
public class HarbingerCicatrixAnchoriteModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("harbinger_cicatrix_anchorite"), "main");

    private final ModelPart whole;
    private final ModelPart body;
    private final ModelPart ClothBack;
    private final ModelPart ClothBack1;
    private final ModelPart ClothBackR1;
    private final ModelPart ClothBackL1;
    private final ModelPart ClothBack2;
    private final ModelPart ClothBackR2;
    private final ModelPart ClothBackR3;
    private final ModelPart ClothBackL2;
    private final ModelPart ClothBackL3;
    private final ModelPart SideclothR;
    private final ModelPart SideclothR4;
    private final ModelPart SideclothR5;
    private final ModelPart SideclothR6;
    private final ModelPart SideclothL;
    private final ModelPart SideclothR1;
    private final ModelPart SideclothR2;
    private final ModelPart SideclothR3;
    private final ModelPart ClothFront;
    private final ModelPart ClothFrontR1;
    private final ModelPart ClothFrontR2;
    private final ModelPart ClothFrontR3;
    private final ModelPart ClothFrontL1;
    private final ModelPart ClothFrontL2;
    private final ModelPart ClothFrontL3;
    private final ModelPart tassleR;
    private final ModelPart tassleL;
    private final ModelPart whiteCloak;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart head;
    private final ModelPart Hood1;
    private final ModelPart Hood2;
    private final ModelPart Hood3;
    private final ModelPart Hood4;
    private final ModelPart bone;
    private final ModelPart rightArm;
    private final ModelPart leftArm;

    public HarbingerCicatrixAnchoriteModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.body = this.whole.getChild("body");
        this.ClothBack = this.body.getChild("ClothBack");
        this.ClothBack1 = this.ClothBack.getChild("ClothBack1");
        this.ClothBackR1 = this.ClothBack1.getChild("ClothBackR1");
        this.ClothBackL1 = this.ClothBack1.getChild("ClothBackL1");
        this.ClothBack2 = this.ClothBack1.getChild("ClothBack2");
        this.ClothBackR2 = this.ClothBack2.getChild("ClothBackR2");
        this.ClothBackR3 = this.ClothBack2.getChild("ClothBackR3");
        this.ClothBackL2 = this.ClothBack2.getChild("ClothBackL2");
        this.ClothBackL3 = this.ClothBack2.getChild("ClothBackL3");
        this.SideclothR = this.body.getChild("SideclothR");
        this.SideclothR4 = this.SideclothR.getChild("SideclothR4");
        this.SideclothR5 = this.SideclothR4.getChild("SideclothR5");
        this.SideclothR6 = this.SideclothR5.getChild("SideclothR6");
        this.SideclothL = this.body.getChild("SideclothL");
        this.SideclothR1 = this.SideclothL.getChild("SideclothR1");
        this.SideclothR2 = this.SideclothR1.getChild("SideclothR2");
        this.SideclothR3 = this.SideclothR2.getChild("SideclothR3");
        this.ClothFront = this.body.getChild("ClothFront");
        this.ClothFrontR1 = this.ClothFront.getChild("ClothFrontR1");
        this.ClothFrontR2 = this.ClothFront.getChild("ClothFrontR2");
        this.ClothFrontR3 = this.ClothFront.getChild("ClothFrontR3");
        this.ClothFrontL1 = this.ClothFront.getChild("ClothFrontL1");
        this.ClothFrontL2 = this.ClothFront.getChild("ClothFrontL2");
        this.ClothFrontL3 = this.ClothFront.getChild("ClothFrontL3");
        this.tassleR = this.body.getChild("tassleR");
        this.tassleL = this.body.getChild("tassleL");
        this.whiteCloak = this.body.getChild("whiteCloak");
        this.leftLeg = this.whole.getChild("leftLeg");
        this.rightLeg = this.whole.getChild("rightLeg");
        this.head = this.whole.getChild("head");
        this.Hood1 = this.head.getChild("Hood1");
        this.Hood2 = this.head.getChild("Hood2");
        this.Hood3 = this.head.getChild("Hood3");
        this.Hood4 = this.head.getChild("Hood4");
        this.bone = this.head.getChild("bone");
        this.rightArm = this.whole.getChild("rightArm");
        this.leftArm = this.whole.getChild("leftArm");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 0).addBox(-4.0F, -1.0F, -2.0F, 8.0F, 13.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(50, 33).addBox(3.3497F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(36, 50).addBox(-4.2998F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(70, 59).addBox(-3.9F, -0.5F, 1.75F, 8.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 50).addBox(-4.0F, -0.5F, -3.0F, 8.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(70, 67).addBox(-4.0F, 4.4045F, 1.9055F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(33, 18).addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(0.0F, 12.3F, 4.15F));

        PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, -0.75F));

        PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(67, 77).addBox(-4.0F, -0.0523F, -1.4973F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(0, 78).addBox(-4.0F, -0.0523F, -1.4973F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 8.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition ClothBackR2 = ClothBack2.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(24, 78).addBox(-1.0F, 7.5132F, -2.3976F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackR3 = ClothBack2.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(58, 12).addBox(-4.0F, 7.5132F, -2.3976F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL2 = ClothBack2.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(87, 46).addBox(-4.0F, 7.5132F, -2.3976F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL3 = ClothBack2.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(87, 36).addBox(-3.0F, 7.5132F, -2.3976F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition SideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(), PartPose.offset(-3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR4 = SideclothR.addOrReplaceChild("SideclothR4", CubeListBuilder.create().texOffs(54, 72).addBox(-1.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition SideclothR5 = SideclothR4.addOrReplaceChild("SideclothR5", CubeListBuilder.create().texOffs(17, 41).addBox(-0.291F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, 0.2967F));

        PartDefinition SideclothR6 = SideclothR5.addOrReplaceChild("SideclothR6", CubeListBuilder.create().texOffs(74, 30).addBox(-0.866F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR1 = SideclothL.addOrReplaceChild("SideclothR1", CubeListBuilder.create().texOffs(11, 74).addBox(0.0417F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

        PartDefinition SideclothR2 = SideclothR1.addOrReplaceChild("SideclothR2", CubeListBuilder.create().texOffs(74, 39).addBox(-0.709F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, -0.2967F));

        PartDefinition SideclothR3 = SideclothR2.addOrReplaceChild("SideclothR3", CubeListBuilder.create().texOffs(77, 0).addBox(-0.134F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition ClothFront = body.addOrReplaceChild("ClothFront", CubeListBuilder.create(), PartPose.offset(-0.2F, 12.2F, -5.05F));

        PartDefinition ClothFrontR1 = ClothFront.addOrReplaceChild("ClothFrontR1", CubeListBuilder.create().texOffs(78, 77).addBox(-3.8F, 0.0841F, 1.0701F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 1.0F, -0.1047F, 0.0F, 0.0F));

        PartDefinition ClothFrontR2 = ClothFront.addOrReplaceChild("ClothFrontR2", CubeListBuilder.create().texOffs(11, 58).addBox(-0.8F, 7.7715F, 2.0399F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition ClothFrontR3 = ClothFront.addOrReplaceChild("ClothFrontR3", CubeListBuilder.create().texOffs(39, 87).addBox(-3.8F, 7.7715F, 2.0399F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition ClothFrontL1 = ClothFront.addOrReplaceChild("ClothFrontL1", CubeListBuilder.create().texOffs(54, 83).addBox(-3.8F, 0.0841F, 1.0701F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.1047F, 0.0F, 0.0F));

        PartDefinition ClothFrontL2 = ClothFront.addOrReplaceChild("ClothFrontL2", CubeListBuilder.create().texOffs(24, 74).addBox(-3.8F, 7.7715F, 2.0399F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition ClothFrontL3 = ClothFront.addOrReplaceChild("ClothFrontL3", CubeListBuilder.create().texOffs(87, 41).addBox(-2.8F, 7.7715F, 2.0399F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition tassleR = body.addOrReplaceChild("tassleR", CubeListBuilder.create().texOffs(16, 85).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.6F, 2.0F, -2.75F));

        PartDefinition tassleL = body.addOrReplaceChild("tassleL", CubeListBuilder.create().texOffs(11, 85).addBox(6.7F, -2.5F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.6F, 2.0F, -2.75F));

        PartDefinition whiteCloak = body.addOrReplaceChild("whiteCloak", CubeListBuilder.create().texOffs(100, 69).addBox(3.5F, -1.5F, -2.4875F, 0.0F, 14.0F, 5.0F, new CubeDeformation(0.125F))
                .texOffs(0, 29).addBox(-9.7659F, -6.2021F, -3.475F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.5F))
                .texOffs(19, 107).addBox(-14.7818F, -1.5F, 2.5625F, 5.0F, 14.0F, 0.0F, new CubeDeformation(0.125F))
                .texOffs(56, 97).addBox(-15.0318F, -1.5F, -2.4875F, 0.0F, 14.0F, 5.0F, new CubeDeformation(0.125F))
                .texOffs(108, 15).addBox(-1.75F, -1.5F, 2.5625F, 5.0F, 14.0F, 0.0F, new CubeDeformation(0.125F)), PartPose.offset(5.7659F, 4.6021F, 0.175F));

        PartDefinition ShoulderR_16_45_0eedefc6_r1 = whiteCloak.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r1", CubeListBuilder.create().texOffs(71, 12).mirror().addBox(-2.0774F, -1.5937F, -2.3875F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.375F)).mirror(false), PartPose.offsetAndRotation(-12.1813F, -2.203F, -0.1875F, 0.0F, 0.0F, 0.4363F));

        PartDefinition ShoulderR_16_45_0eedefc6_r2 = whiteCloak.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r2", CubeListBuilder.create().texOffs(11, 65).mirror().addBox(-2.5937F, -2.0774F, -2.6125F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)).mirror(false), PartPose.offsetAndRotation(-11.1081F, -2.8694F, 0.0375F, 0.0F, 0.0F, 1.1345F));

        PartDefinition ShoulderR_16_45_0eedefc6_r3 = whiteCloak.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r3", CubeListBuilder.create().texOffs(11, 65).addBox(-2.4063F, -2.0774F, -2.6125F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(-0.4237F, -2.8694F, 0.0375F, 0.0F, 0.0F, -1.1345F));

        PartDefinition ShoulderR_16_45_0eedefc6_r4 = whiteCloak.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r4", CubeListBuilder.create().texOffs(71, 12).addBox(-1.9226F, -1.5937F, -2.3875F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.375F)), PartPose.offsetAndRotation(0.6496F, -2.203F, -0.1875F, 0.0F, 0.0F, -0.4363F));

        PartDefinition leftLeg = whole.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(33, 33).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition rightLeg = whole.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 41).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.1F, -3.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hood1 = head.addOrReplaceChild("Hood1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hood2 = head.addOrReplaceChild("Hood2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition Hood3 = head.addOrReplaceChild("Hood3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Hood4 = head.addOrReplaceChild("Hood4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

        PartDefinition bone = head.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -1.3F, -3.9F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, -1.35F, 0.5F, 0.1745F, 0.0F, 0.0F));

        PartDefinition rightArm = whole.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(52, 18).mirror().addBox(-3.5F, 3.5F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 84).mirror().addBox(-3.0F, 9.5F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(49, 52).mirror().addBox(-4.0159F, 1.1021F, -2.5125F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.125F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));


        PartDefinition leftArm = whole.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(52, 18).addBox(-0.5F, 3.5F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(24, 84).addBox(0.0F, 9.5F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(49, 52).addBox(-0.9841F, 1.1021F, -2.5125F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.125F)), PartPose.offset(5.0F, 2.0F, 0.0F));


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
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
