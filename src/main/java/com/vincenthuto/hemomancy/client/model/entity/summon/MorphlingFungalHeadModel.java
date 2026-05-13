package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
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
import net.minecraft.world.entity.LivingEntity;

/**
 * Head-replacement morphling attachment for the fungal morphling.
 */
public class MorphlingFungalHeadModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_fungal_head"), "main");

    private final ModelPart root;
    private final ModelPart leftTendril;
    private final ModelPart rightTendril;
    private final ModelPart centerTendril;
    private final ModelPart jawTendril;
    private final ModelPart rearTendril;

    public MorphlingFungalHeadModel(ModelPart root) {
        this.root = root.getChild("root");
        this.leftTendril = this.root.getChild("left_tendril");
        this.rightTendril = this.root.getChild("right_tendril");
        this.centerTendril = this.root.getChild("center_tendril");
        this.jawTendril = this.root.getChild("jaw_tendril");
        this.rearTendril = this.root.getChild("rear_tendril");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();

        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("stem",
                CubeListBuilder.create()
                        .texOffs(32, 40)
                        .addBox(-3.5f, -10.0f, -3.25f, 7.0f, 10.0f, 6.5f, new CubeDeformation(0.15f)),
                PartPose.ZERO);

        root.addOrReplaceChild("face_plate",
                CubeListBuilder.create()
                        .texOffs(0, 40)
                        .addBox(-3.0f, -9.5f, -5.2f, 6.0f, 8.0f, 2.0f, new CubeDeformation(0.06f))
                        .texOffs(18, 44)
                        .addBox(-1.0f, -6.2f, -6.0f, 2.0f, 4.0f, 1.0f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("cap",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-7.0f, -15.4f, -6.5f, 14.0f, 3.2f, 11.0f, new CubeDeformation(0.45f))
                        .texOffs(0, 17)
                        .addBox(-5.6f, -14.4f, -10.7f, 11.2f, 3.0f, 5.2f, new CubeDeformation(0.25f))
                        .texOffs(52, 0)
                        .addBox(-9.4f, -13.7f, -5.6f, 4.6f, 2.9f, 8.8f, new CubeDeformation(0.15f))
                        .texOffs(52, 16)
                        .addBox(4.8f, -13.7f, -5.6f, 4.6f, 2.9f, 8.8f, new CubeDeformation(0.15f))
                        .texOffs(0, 29)
                        .addBox(-5.2f, -13.4f, 3.8f, 10.4f, 2.8f, 4.6f, new CubeDeformation(0.12f)),
                PartPose.ZERO);

        root.addOrReplaceChild("gills",
                CubeListBuilder.create()
                        .texOffs(0, 64)
                        .addBox(-4.6f, -11.6f, -7.7f, 1.0f, 6.0f, 1.0f, CubeDeformation.NONE)
                        .texOffs(6, 64)
                        .addBox(-2.2f, -11.8f, -7.9f, 1.0f, 6.6f, 1.0f, CubeDeformation.NONE)
                        .texOffs(12, 64)
                        .addBox(1.2f, -11.8f, -7.9f, 1.0f, 6.6f, 1.0f, CubeDeformation.NONE)
                        .texOffs(18, 64)
                        .addBox(3.6f, -11.6f, -7.7f, 1.0f, 6.0f, 1.0f, CubeDeformation.NONE)
                        .texOffs(24, 64)
                        .addBox(-5.5f, -11.3f, -2.6f, 1.0f, 5.0f, 1.0f, CubeDeformation.NONE)
                        .texOffs(30, 64)
                        .addBox(4.5f, -11.3f, -2.6f, 1.0f, 5.0f, 1.0f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("left_tendril",
                CubeListBuilder.create()
                        .texOffs(72, 48)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 8.0f, 1.0f, new CubeDeformation(0.08f))
                        .texOffs(78, 52)
                        .addBox(-1.0f, 6.4f, -0.8f, 1.6f, 3.0f, 1.6f, CubeDeformation.NONE),
                PartPose.offset(-3.8f, -7.0f, -5.1f));

        root.addOrReplaceChild("right_tendril",
                CubeListBuilder.create()
                        .texOffs(86, 48)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 8.2f, 1.0f, new CubeDeformation(0.08f))
                        .texOffs(92, 52)
                        .addBox(-0.6f, 6.6f, -0.8f, 1.6f, 3.0f, 1.6f, CubeDeformation.NONE),
                PartPose.offset(3.7f, -7.1f, -5.0f));

        root.addOrReplaceChild("center_tendril",
                CubeListBuilder.create()
                        .texOffs(100, 48)
                        .addBox(-0.6f, 0.0f, -0.6f, 1.2f, 9.2f, 1.2f, new CubeDeformation(0.08f)),
                PartPose.offset(0.0f, -7.5f, -5.6f));

        root.addOrReplaceChild("jaw_tendril",
                CubeListBuilder.create()
                        .texOffs(108, 48)
                        .addBox(-0.5f, 0.0f, -0.5f, 1.0f, 7.0f, 1.0f, new CubeDeformation(0.06f)),
                PartPose.offset(1.7f, -4.0f, -6.0f));

        root.addOrReplaceChild("rear_tendril",
                CubeListBuilder.create()
                        .texOffs(116, 48)
                        .addBox(-0.6f, 0.0f, -0.6f, 1.2f, 7.4f, 1.2f, new CubeDeformation(0.08f)),
                PartPose.offset(-1.8f, -6.8f, 3.5f));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        float slow = ageInTicks * 0.075f;
        float walk = limbSwing * 0.6f;
        float swing = 0.12f + limbSwingAmount * 0.25f;

        leftTendril.xRot = Mth.sin(slow + walk) * swing;
        leftTendril.zRot = 0.14f + Mth.cos(slow * 0.8f) * 0.08f;
        rightTendril.xRot = Mth.cos(slow + walk) * swing;
        rightTendril.zRot = -0.14f + Mth.sin(slow * 0.8f) * 0.08f;
        centerTendril.xRot = Mth.sin(slow * 1.2f + 1.4f) * (swing * 0.8f);
        jawTendril.xRot = -0.08f + Mth.cos(slow * 1.4f) * 0.1f;
        jawTendril.zRot = Mth.sin(slow) * 0.06f;
        rearTendril.xRot = Mth.cos(slow * 0.9f + 2.0f) * (swing * 0.7f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
