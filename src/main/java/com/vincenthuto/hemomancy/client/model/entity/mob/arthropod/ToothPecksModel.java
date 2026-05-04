package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ToothPecksEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

// Flat molar-shaped body + 8 spider legs.
// As feedCount rises (0-5), blood lumps appear on the body and the
// renderer scales the whole entity up to show it growing engorged.
public class ToothPecksModel<T extends ToothPecksEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("tooth_pecks"), "main");

    private static final float LEG_ROOT_ROT = 0.2182F;
    private static final float LEG_TIP_ROT = 0.1745F;

    private final ModelPart whole;
    private final ModelPart body;
    private final ModelPart lumps;
    private final ModelPart lump1;
    private final ModelPart lump2;
    private final ModelPart lump3;
    private final ModelPart lump4;
    private final ModelPart lump5;
    private final ModelPart llegs;
    private final ModelPart lLegSeg1;
    private final ModelPart lLegSeg2;
    private final ModelPart lLegSeg3;
    private final ModelPart lLegSeg4;
    private final ModelPart lLegSeg5;
    private final ModelPart lLegSeg6;
    private final ModelPart lLegSeg7;
    private final ModelPart lLegSeg8;
    private final ModelPart rllegs;
    private final ModelPart rLegSeg1;
    private final ModelPart rLegSeg2;
    private final ModelPart rLegSeg3;
    private final ModelPart rLegSeg4;
    private final ModelPart rLegSeg5;
    private final ModelPart rLegSeg6;
    private final ModelPart rLegSeg7;
    private final ModelPart rLegSeg8;
    private final ModelPart head;
    private final ModelPart fangs;
    private final ModelPart rFang;
    private final ModelPart lFang;

    public ToothPecksModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.whole = root.getChild("whole");
        this.body = this.whole.getChild("body");
        this.lumps = this.body.getChild("lumps");
        this.lump1 = this.lumps.getChild("lump1");
        this.lump2 = this.lumps.getChild("lump2");
        this.lump3 = this.lumps.getChild("lump3");
        this.lump4 = this.lumps.getChild("lump4");
        this.lump5 = this.lumps.getChild("lump5");
        this.llegs = this.body.getChild("llegs");
        this.lLegSeg1 = this.llegs.getChild("lLegSeg1");
        this.lLegSeg2 = this.lLegSeg1.getChild("lLegSeg2");
        this.lLegSeg3 = this.llegs.getChild("lLegSeg3");
        this.lLegSeg4 = this.lLegSeg3.getChild("lLegSeg4");
        this.lLegSeg5 = this.llegs.getChild("lLegSeg5");
        this.lLegSeg6 = this.lLegSeg5.getChild("lLegSeg6");
        this.lLegSeg7 = this.llegs.getChild("lLegSeg7");
        this.lLegSeg8 = this.lLegSeg7.getChild("lLegSeg8");
        this.rllegs = this.body.getChild("rllegs");
        this.rLegSeg1 = this.rllegs.getChild("rLegSeg1");
        this.rLegSeg2 = this.rLegSeg1.getChild("rLegSeg2");
        this.rLegSeg3 = this.rllegs.getChild("rLegSeg3");
        this.rLegSeg4 = this.rLegSeg3.getChild("rLegSeg4");
        this.rLegSeg5 = this.rllegs.getChild("rLegSeg5");
        this.rLegSeg6 = this.rLegSeg5.getChild("rLegSeg6");
        this.rLegSeg7 = this.rllegs.getChild("rLegSeg7");
        this.rLegSeg8 = this.rLegSeg7.getChild("rLegSeg8");
        this.head = this.body.getChild("head");
        this.fangs = this.head.getChild("fangs");
        this.rFang = this.fangs.getChild("rFang");
        this.lFang = this.fangs.getChild("lFang");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 22.7F, -0.9F));

        PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -1.7F, -3.1F, 6.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-2.0F, -2.7F, -2.35F, 4.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lumps = body.addOrReplaceChild("lumps", CubeListBuilder.create(), PartPose.offset(0.0F, 1.3F, 0.9F));

        PartDefinition lump1 = lumps.addOrReplaceChild("lump1", CubeListBuilder.create().texOffs(0, 37).addBox(0.75F, -3.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.75F, 0.0F));

        PartDefinition lump2 = lumps.addOrReplaceChild("lump2", CubeListBuilder.create().texOffs(21, 30).addBox(0.75F, -3.0F, -3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -1.75F, 0.5F));

        PartDefinition lump3 = lumps.addOrReplaceChild("lump3", CubeListBuilder.create().texOffs(25, 17).addBox(0.75F, -4.0F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 3.0F));

        PartDefinition lump4 = lumps.addOrReplaceChild("lump4", CubeListBuilder.create().texOffs(21, 23).addBox(-0.25F, -4.0F, -4.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.25F, -1.5F, 4.5F));

        PartDefinition lump5 = lumps.addOrReplaceChild("lump5", CubeListBuilder.create().texOffs(0, 23).addBox(-1.25F, -5.0F, -4.0F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.25F, -1.25F, 8.25F));

        PartDefinition llegs = body.addOrReplaceChild("llegs", CubeListBuilder.create(), PartPose.offset(2.5F, -0.2F, 1.4F));

        PartDefinition lLegSeg1 = llegs.addOrReplaceChild("lLegSeg1", CubeListBuilder.create().texOffs(34, 15).addBox(-0.4763F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.0F, 0.0F, 0.2182F));

        PartDefinition lLegSeg2 = lLegSeg1.addOrReplaceChild("lLegSeg2", CubeListBuilder.create().texOffs(30, 30).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition lLegSeg3 = llegs.addOrReplaceChild("lLegSeg3", CubeListBuilder.create().texOffs(34, 18).addBox(-0.4763F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.0F, 0.0F, 0.2182F));

        PartDefinition lLegSeg4 = lLegSeg3.addOrReplaceChild("lLegSeg4", CubeListBuilder.create().texOffs(31, 0).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition lLegSeg5 = llegs.addOrReplaceChild("lLegSeg5", CubeListBuilder.create().texOffs(21, 34).addBox(-0.4763F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.2182F));

        PartDefinition lLegSeg6 = lLegSeg5.addOrReplaceChild("lLegSeg6", CubeListBuilder.create().texOffs(31, 3).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition lLegSeg7 = llegs.addOrReplaceChild("lLegSeg7", CubeListBuilder.create().texOffs(34, 21).addBox(-0.4763F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, 0.2182F));

        PartDefinition lLegSeg8 = lLegSeg7.addOrReplaceChild("lLegSeg8", CubeListBuilder.create().texOffs(31, 6).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition rllegs = body.addOrReplaceChild("rllegs", CubeListBuilder.create(), PartPose.offset(-2.5F, -0.2F, 1.4F));

        PartDefinition rLegSeg1 = rllegs.addOrReplaceChild("rLegSeg1", CubeListBuilder.create().texOffs(34, 24).addBox(-1.5237F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.0F, 0.0F, -0.2182F));

        PartDefinition rLegSeg2 = rLegSeg1.addOrReplaceChild("rLegSeg2", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition rLegSeg3 = rllegs.addOrReplaceChild("rLegSeg3", CubeListBuilder.create().texOffs(34, 27).addBox(-1.5237F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -3.0F, 0.0F, 0.0F, -0.2182F));

        PartDefinition rLegSeg4 = rLegSeg3.addOrReplaceChild("rLegSeg4", CubeListBuilder.create().texOffs(31, 9).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition rLegSeg5 = rllegs.addOrReplaceChild("rLegSeg5", CubeListBuilder.create().texOffs(28, 36).addBox(-1.5237F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.0F, 0.0F, -0.2182F));

        PartDefinition rLegSeg6 = rLegSeg5.addOrReplaceChild("rLegSeg6", CubeListBuilder.create().texOffs(30, 33).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition rLegSeg7 = rllegs.addOrReplaceChild("rLegSeg7", CubeListBuilder.create().texOffs(35, 36).addBox(-1.5237F, -0.2836F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.0F, 0.0F, -0.2182F));

        PartDefinition rLegSeg8 = rLegSeg7.addOrReplaceChild("rLegSeg8", CubeListBuilder.create().texOffs(7, 34).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5237F, 0.2164F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(25, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.2F, -3.1F));

        PartDefinition fangs = head.addOrReplaceChild("fangs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.5F, -1.0F));

        PartDefinition rFang = fangs.addOrReplaceChild("rFang", CubeListBuilder.create().texOffs(14, 34).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 0.0F, 0.0F));

        PartDefinition lFang = fangs.addOrReplaceChild("lFang", CubeListBuilder.create().texOffs(34, 12).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {

        int feeds = entity.getFeedCount();

        // Reveal blood lumps progressively
        lump1.visible = feeds >= 1;
        lump2.visible = feeds >= 2;
        lump3.visible = feeds >= 3;
        lump4.visible = feeds >= 4;
        lump5.visible = feeds >= 5;

        // Head tracking plus Chthonian-style idle fang chewing.
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;

        float fangChew = Mth.sin(ageInTicks * 0.3F) * 0.15F;
        fangs.xRot = Mth.sin(ageInTicks * 0.45F) * 0.05F;
        lFang.yRot = fangChew;
        rFang.yRot = -fangChew;

        // Chthonian-inspired spider gait: diagonal leg pairs alternate, with
        // smaller secondary movement on the outer segments so the legs bend.
        float legSpeed = 0.6662F;
        float legAmplitude = 0.5F;
        float swing1 = Mth.cos(limbSwing * legSpeed) * legAmplitude * limbSwingAmount;
        float swing2 = Mth.cos(limbSwing * legSpeed + Mth.PI) * legAmplitude * limbSwingAmount;
        float reach = Mth.cos(limbSwing * legSpeed) * 0.25F * limbSwingAmount;

        lLegSeg1.zRot = LEG_ROOT_ROT + swing1;
        lLegSeg3.zRot = LEG_ROOT_ROT + swing2;
        lLegSeg5.zRot = LEG_ROOT_ROT + swing1;
        lLegSeg7.zRot = LEG_ROOT_ROT + swing2;
        rLegSeg3.zRot = -LEG_ROOT_ROT - swing2;
        rLegSeg5.zRot = -LEG_ROOT_ROT - swing1;
        rLegSeg1.zRot = -LEG_ROOT_ROT - swing2;
        rLegSeg7.zRot = -LEG_ROOT_ROT - swing1;

        lLegSeg2.zRot = LEG_TIP_ROT + swing1 * 0.35F;
        lLegSeg4.zRot = LEG_TIP_ROT + swing2 * 0.35F;
        lLegSeg6.zRot = LEG_TIP_ROT + swing1 * 0.35F;
        lLegSeg8.zRot = LEG_TIP_ROT + swing2 * 0.35F;
        rLegSeg4.zRot = -LEG_TIP_ROT - swing2 * 0.35F;
        rLegSeg6.zRot = -LEG_TIP_ROT - swing1 * 0.35F;
        rLegSeg2.zRot = -LEG_TIP_ROT - swing2 * 0.35F;
        rLegSeg8.zRot = -LEG_TIP_ROT - swing1 * 0.35F;

        lLegSeg1.yRot = -reach;
        lLegSeg7.yRot = reach;
        rLegSeg3.yRot = reach;
        rLegSeg7.yRot = -reach;
        lLegSeg3.yRot = 0.0F;
        lLegSeg5.yRot = 0.0F;
        rLegSeg1.yRot = 0.0F;
        rLegSeg5.yRot = 0.0F;

        // Bite bob while latched
        body.y = entity.isLatched() ? (-3.0F + Mth.sin(ageInTicks * 0.4F) * 0.15F) : -3.0F;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        whole.render(pose, buffer, packedLight, packedOverlay, packedColor);
    }
}
