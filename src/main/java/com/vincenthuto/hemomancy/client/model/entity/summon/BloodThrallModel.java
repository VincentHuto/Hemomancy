package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.BloodThrallEntity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Blood Thrall model — an animated blob of coagulated blood.
 * Small body with stubby legs and a rounded top that pulses
 * based on how much blood it carries.
 */
public class BloodThrallModel extends EntityModel<BloodThrallEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("blood_thrall"), "main");

    private final ModelPart body;
    private final ModelPart top;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public BloodThrallModel(ModelPart root) {
        this.body = root.getChild("body");
        this.top = root.getChild("top");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDef = mesh.getRoot();

        // Main body — slightly flattened sphere (8×7×8)
        partDef.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 20.0F, 0.0F));

        // Top dome — smaller rounded piece (6×4×6)
        partDef.addOrReplaceChild("top",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .addBox(-3.0F, -4.0F, -3.0F, 6.0F, 4.0F, 6.0F,
                                new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, 13.0F, 0.0F));

        // Left leg — stubby
        partDef.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(24, 15)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(2.0F, 20.0F, 0.0F));

        // Right leg — stubby
        partDef.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(24, 22)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, 20.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(BloodThrallEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Waddle legs
        float legSwing = Mth.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
        this.leftLeg.xRot = legSwing;
        this.rightLeg.xRot = -legSwing;

        // Pulse the top dome based on carried blood ratio
        float carryRatio = entity.getCarryRatio();
        float pulse = 1.0f + carryRatio * 0.15f * Mth.sin(ageInTicks * 0.15f);
        this.top.xScale = pulse;
        this.top.yScale = pulse;
        this.top.zScale = pulse;

        // Subtle body bob
        this.body.y = 20.0f + Mth.sin(ageInTicks * 0.1f) * 0.3f;
        this.top.y = 13.0f + Mth.sin(ageInTicks * 0.1f) * 0.3f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        top.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
