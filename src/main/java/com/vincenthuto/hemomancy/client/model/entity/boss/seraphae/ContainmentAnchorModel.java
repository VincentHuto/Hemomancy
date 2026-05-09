package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ContainmentAnchorModel extends EntityModel<ContainmentAnchorEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("containment_anchor"), "main");

    private final ModelPart base;
    private final ModelPart pillar;
    private final ModelPart crown;
    private final ModelPart chains;

    public ContainmentAnchorModel(ModelPart root) {
        this.base = root.getChild("base");
        this.pillar = root.getChild("pillar");
        this.crown = root.getChild("crown");
        this.chains = root.getChild("chains");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 2.0F, 10.0F)
                        .texOffs(40, 0).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 2.0F, 6.0F),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("pillar",
                CubeListBuilder.create()
                        .texOffs(0, 12).addBox(-2.0F, -18.0F, -2.0F, 4.0F, 18.0F, 4.0F)
                        .texOffs(16, 12).addBox(-1.0F, -20.0F, -1.0F, 2.0F, 20.0F, 2.0F),
                PartPose.offset(0.0F, 22.0F, 0.0F));
        root.addOrReplaceChild("crown",
                CubeListBuilder.create()
                        .texOffs(24, 12).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 4.0F, 8.0F)
                        .texOffs(0, 34).addBox(-6.0F, -2.0F, -1.0F, 12.0F, 2.0F, 2.0F)
                        .texOffs(28, 34).addBox(-1.0F, -2.0F, -6.0F, 2.0F, 2.0F, 12.0F),
                PartPose.offset(0.0F, 5.0F, 0.0F));
        root.addOrReplaceChild("chains",
                CubeListBuilder.create()
                        .texOffs(0, 38).addBox(-7.0F, -12.0F, -0.5F, 14.0F, 1.0F, 1.0F)
                        .texOffs(0, 42).addBox(-0.5F, -12.0F, -7.0F, 1.0F, 1.0F, 14.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ContainmentAnchorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.crown.yRot = entity.isActive() ? ageInTicks * 0.18F : 0.0F;
        this.chains.yRot = -this.crown.yRot * 0.5F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        base.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        pillar.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        crown.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        chains.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
