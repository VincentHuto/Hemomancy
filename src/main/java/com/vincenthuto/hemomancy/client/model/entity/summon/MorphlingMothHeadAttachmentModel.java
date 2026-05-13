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

public class MorphlingMothHeadAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_moth_head_attachment"), "main");

    private final ModelPart root;
    private final ModelPart antennae;
    private final ModelPart dustFins;

    public MorphlingMothHeadAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.antennae = this.root.getChild("antennae");
        this.dustFins = this.root.getChild("dust_fins");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("mask_dust",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.8f, -8.8f, -4.6f, 9.6f, 3.0f, 1.8f, new CubeDeformation(0.08f))
                        .texOffs(0, 8)
                        .addBox(-3.8f, -6.2f, -5.0f, 7.6f, 2.4f, 1.2f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("antennae",
                CubeListBuilder.create()
                        .texOffs(28, 0)
                        .addBox(-3.8f, -13.4f, -4.1f, 0.8f, 5.4f, 0.8f, CubeDeformation.NONE)
                        .texOffs(32, 0)
                        .addBox(3.0f, -13.4f, -4.1f, 0.8f, 5.4f, 0.8f, CubeDeformation.NONE)
                        .texOffs(36, 0)
                        .addBox(-5.4f, -14.0f, -4.3f, 2.4f, 1.0f, 1.0f, CubeDeformation.NONE)
                        .texOffs(46, 0)
                        .addBox(3.0f, -14.0f, -4.3f, 2.4f, 1.0f, 1.0f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("dust_fins",
                CubeListBuilder.create()
                        .texOffs(0, 18)
                        .addBox(-7.8f, -6.8f, -3.2f, 3.4f, 5.8f, 0.9f, new CubeDeformation(0.03f))
                        .texOffs(12, 18)
                        .addBox(4.4f, -6.8f, -3.2f, 3.4f, 5.8f, 0.9f, new CubeDeformation(0.03f))
                        .texOffs(26, 18)
                        .addBox(-7.0f, -1.6f, -2.8f, 2.6f, 3.4f, 0.8f, CubeDeformation.NONE)
                        .texOffs(36, 18)
                        .addBox(4.4f, -1.6f, -2.8f, 2.6f, 3.4f, 0.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        antennae.zRot = Mth.sin(ageInTicks * 0.11f) * 0.025f;
        dustFins.xScale = 1.0f + Mth.sin(ageInTicks * 0.07f) * 0.025f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
