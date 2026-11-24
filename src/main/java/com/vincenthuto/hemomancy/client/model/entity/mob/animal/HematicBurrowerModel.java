package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.HematicBurrowerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class HematicBurrowerModel extends HierarchicalModel<HematicBurrowerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("hematic_burrower"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart leftForepaw;
    private final ModelPart rightForepaw;
    private final ModelPart leftHindpaw;
    private final ModelPart rightHindpaw;

    public HematicBurrowerModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.tail = this.body.getChild("tail");
        this.leftForepaw = this.root.getChild("left_forepaw");
        this.rightForepaw = this.root.getChild("right_forepaw");
        this.leftHindpaw = this.root.getChild("left_hindpaw");
        this.rightHindpaw = this.root.getChild("right_hindpaw");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();

        PartDefinition root = mesh.getRoot().addOrReplaceChild("root",
                CubeListBuilder.create(), PartPose.offset(0.00F, 24.00F, 0.00F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.00F, -5.00F, -5.00F, 10.00F, 6.00F, 8.00F)
                        .texOffs(36, 0).addBox(-4.00F, 1.00F, -4.00F, 8.00F, 1.00F, 9.00F)
                        .texOffs(70, 0).addBox(-4.00F, -4.00F, 3.00F, 8.00F, 5.00F, 4.00F), PartPose.offset(0.00F, -3.00F, 0.00F));

        body.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(94, 0).addBox(-4.00F, -3.00F, -4.00F, 8.00F, 5.00F, 3.00F)
                        .texOffs(108, 14).addBox(-3.00F, -1.00F, -6.00F, 6.00F, 3.00F, 2.00F)
                        .texOffs(28, 21).addBox(-2.00F, 0.00F, -8.00F, 4.00F, 2.00F, 2.00F)
                        .texOffs(48, 21).addBox(-1.00F, 0.00F, -9.00F, 2.00F, 2.00F, 1.00F), PartPose.offset(0.00F, -1.00F, -4.00F));

        PartDefinition tail = body.addOrReplaceChild("tail",
                CubeListBuilder.create(), PartPose.offset(0.00F, 1.00F, 6.00F));

        tail.addOrReplaceChild("tail_curve",
                CubeListBuilder.create()
                        .texOffs(40, 21).addBox(-1.00F, -1.00F, 0.00F, 2.00F, 2.00F, 2.00F)
                        .texOffs(54, 21).addBox(0.00F, 0.00F, 2.00F, 1.00F, 1.00F, 2.00F),
                PartPose.offsetAndRotation(0.00F, 0.00F, 1.00F, 0.0F, -0.209439510F, 0.0F));

        PartDefinition left_forepaw = root.addOrReplaceChild("left_forepaw",
                CubeListBuilder.create()
                        .texOffs(28, 14).addBox(0.00F, -1.00F, -2.00F, 2.00F, 3.00F, 4.00F)
                        .texOffs(52, 14).addBox(0.00F, 1.00F, -5.00F, 5.00F, 2.00F, 4.00F)
                        .texOffs(74, 21).addBox(2.00F, 1.00F, -6.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(78, 21).addBox(2.00F, 2.00F, -8.00F, 1.00F, 1.00F, 2.00F), PartPose.offset(4.00F, -3.00F, -4.00F));

        left_forepaw.addOrReplaceChild("claw_1",
                CubeListBuilder.create()
                        .texOffs(60, 21).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(10, 26).addBox(-0.50F, 0.00F, -2.00F, 1.00F, 1.00F, 1.00F),
                PartPose.offsetAndRotation(0.50F, 2.00F, -4.00F, 0.0F, 0.139626340F, 0.0F));

        left_forepaw.addOrReplaceChild("claw_2",
                CubeListBuilder.create()
                        .texOffs(64, 21).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(68, 21).addBox(-0.50F, 0.00F, -3.00F, 1.00F, 1.00F, 2.00F),
                PartPose.offsetAndRotation(1.50F, 2.00F, -5.00F, 0.0F, 0.069813170F, 0.0F));

        left_forepaw.addOrReplaceChild("claw_4",
                CubeListBuilder.create()
                        .texOffs(84, 21).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(88, 21).addBox(-0.50F, 0.00F, -3.00F, 1.00F, 1.00F, 2.00F),
                PartPose.offsetAndRotation(3.50F, 2.00F, -5.00F, 0.0F, -0.069813170F, 0.0F));

        left_forepaw.addOrReplaceChild("claw_5",
                CubeListBuilder.create()
                        .texOffs(94, 21).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(14, 26).addBox(-0.50F, 0.00F, -2.00F, 1.00F, 1.00F, 1.00F),
                PartPose.offsetAndRotation(4.50F, 2.00F, -4.00F, 0.0F, -0.139626340F, 0.0F));

        root.addOrReplaceChild("left_hindpaw",
                CubeListBuilder.create()
                        .texOffs(70, 14).addBox(0.00F, -2.00F, -2.00F, 2.00F, 3.00F, 3.00F)
                        .texOffs(0, 21).addBox(0.00F, 1.00F, -2.00F, 3.00F, 1.00F, 4.00F)
                        .texOffs(18, 26).addBox(0.00F, 1.00F, -3.00F, 1.00F, 1.00F, 1.00F)
                        .texOffs(22, 26).addBox(2.00F, 1.00F, -3.00F, 1.00F, 1.00F, 1.00F), PartPose.offset(3.00F, -2.00F, 5.00F));

        PartDefinition right_forepaw = root.addOrReplaceChild("right_forepaw",
                CubeListBuilder.create()
                        .texOffs(40, 14).addBox(-2.00F, -1.00F, -2.00F, 2.00F, 3.00F, 4.00F)
                        .texOffs(80, 14).addBox(-5.00F, 1.00F, -5.00F, 5.00F, 2.00F, 4.00F)
                        .texOffs(112, 21).addBox(-3.00F, 1.00F, -6.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(116, 21).addBox(-3.00F, 2.00F, -8.00F, 1.00F, 1.00F, 2.00F), PartPose.offset(-4.00F, -3.00F, -4.00F));

        right_forepaw.addOrReplaceChild("claw_1",
                CubeListBuilder.create()
                        .texOffs(98, 21).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(26, 26).addBox(-0.50F, 0.00F, -2.00F, 1.00F, 1.00F, 1.00F),
                PartPose.offsetAndRotation(-0.50F, 2.00F, -4.00F, 0.0F, -0.139626340F, 0.0F));

        right_forepaw.addOrReplaceChild("claw_2",
                CubeListBuilder.create()
                        .texOffs(102, 21).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(106, 21).addBox(-0.50F, 0.00F, -3.00F, 1.00F, 1.00F, 2.00F),
                PartPose.offsetAndRotation(-1.50F, 2.00F, -5.00F, 0.0F, -0.069813170F, 0.0F));

        right_forepaw.addOrReplaceChild("claw_4",
                CubeListBuilder.create()
                        .texOffs(122, 21).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(0, 26).addBox(-0.50F, 0.00F, -3.00F, 1.00F, 1.00F, 2.00F),
                PartPose.offsetAndRotation(-3.50F, 2.00F, -5.00F, 0.0F, 0.069813170F, 0.0F));

        right_forepaw.addOrReplaceChild("claw_5",
                CubeListBuilder.create()
                        .texOffs(6, 26).addBox(-0.50F, -1.00F, -1.00F, 1.00F, 2.00F, 1.00F)
                        .texOffs(30, 26).addBox(-0.50F, 0.00F, -2.00F, 1.00F, 1.00F, 1.00F),
                PartPose.offsetAndRotation(-4.50F, 2.00F, -4.00F, 0.0F, 0.139626340F, 0.0F));

        root.addOrReplaceChild("right_hindpaw",
                CubeListBuilder.create()
                        .texOffs(98, 14).addBox(-2.00F, -2.00F, -2.00F, 2.00F, 3.00F, 3.00F)
                        .texOffs(14, 21).addBox(-3.00F, 1.00F, -2.00F, 3.00F, 1.00F, 4.00F)
                        .texOffs(34, 26).addBox(-1.00F, 1.00F, -3.00F, 1.00F, 1.00F, 1.00F)
                        .texOffs(38, 26).addBox(-3.00F, 1.00F, -3.00F, 1.00F, 1.00F, 1.00F), PartPose.offset(-3.00F, -2.00F, 5.00F));

        return LayerDefinition.create(mesh, 128, 32);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(HematicBurrowerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        this.pose(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, entity.isDigging());
    }

    void pose(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
            boolean digging) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        if (digging) {
            this.poseDigging(ageInTicks);
            return;
        }

        float movement = Mth.clamp(limbSwingAmount * 1.8F, 0.0F, 1.0F);
        float cycle = limbSwing * 1.7F;
        float stride = Mth.sin(cycle) * movement;
        float lift = Mth.cos(cycle) * movement;
        poseScurryPaw(this.leftForepaw, stride, lift);
        poseScurryPaw(this.rightForepaw, -stride, -lift);
        poseScurryPaw(this.leftHindpaw, -stride, -lift);
        poseScurryPaw(this.rightHindpaw, stride, lift);

        float sniff = Mth.sin(ageInTicks * 0.62F) * (1.0F - movement);
        this.body.y -= 0.12F * Mth.sin(ageInTicks * 0.24F);
        this.body.zRot = 0.035F * stride;
        this.head.yRot = Mth.clamp(netHeadYaw, -35.0F, 35.0F) * Mth.DEG_TO_RAD * 0.65F;
        this.head.xRot = Mth.clamp(headPitch, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.5F + 0.035F * sniff;
        this.head.z += 3.0F * Math.abs(this.head.yRot) + 2.0F * Math.abs(this.head.xRot) - 0.05F * sniff;
        this.tail.yRot = 0.10F * Mth.sin(ageInTicks * 0.28F) + 0.15F * stride;
    }

    private static void poseScurryPaw(ModelPart paw, float stride, float lift) {
        float raised = Math.max(0.0F, lift);
        paw.z -= 0.8F * stride;
        paw.y -= 0.65F * raised;
        paw.xRot = 0.20F * raised;
    }

    private void poseDigging(float ageInTicks) {
        float cycle = ageInTicks * 1.05F;
        float sweep = Mth.cos(cycle);
        float lift = Mth.sin(cycle);
        this.body.xRot = 0.12F;
        this.body.zRot = 0.07F * sweep;
        this.body.y += 0.2F * Math.abs(sweep);
        this.head.xRot = 0.12F + 0.04F * Math.abs(lift);
        this.head.z += 2.0F * this.head.xRot;
        this.tail.xRot = -0.18F;
        this.tail.yRot = -0.18F * sweep;
        this.leftForepaw.z -= 1.25F * sweep;
        this.rightForepaw.z += 1.25F * sweep;
        this.leftForepaw.y -= 0.9F * Math.max(0.0F, lift);
        this.rightForepaw.y -= 0.9F * Math.max(0.0F, -lift);
        this.leftForepaw.xRot = 0.20F * Math.max(0.0F, lift);
        this.rightForepaw.xRot = 0.20F * Math.max(0.0F, -lift);
    }
}
