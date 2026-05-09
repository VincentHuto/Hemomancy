package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class SeraphaeFragmentModel<T extends SeraphaeFragmentEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("seraphae_fragment"), "main");

    private final ModelPart core;
    private final ModelPart ring;

    public SeraphaeFragmentModel(ModelPart root) {
        this.core = root.getChild("core");
        this.ring = root.getChild("ring");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 14.0F, 7.0F)
                        .texOffs(0, 22).addBox(-1.0F, -11.0F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(8, 22).addBox(-1.0F, 7.0F, -1.0F, 2.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 15.0F, 0.0F));
        root.addOrReplaceChild("ring",
                CubeListBuilder.create()
                        .texOffs(16, 22).addBox(-6.0F, -1.0F, -1.0F, 12.0F, 2.0F, 2.0F)
                        .texOffs(16, 26).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(-0.1F)),
                PartPose.offset(0.0F, 15.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.core.yRot = ageInTicks * 0.12F;
        this.core.y = 15.0F + (float) Math.sin(ageInTicks * 0.08F) * 1.5F;
        this.ring.yRot = -ageInTicks * 0.16F;
        this.ring.zRot = ageInTicks * 0.08F;
        this.ring.y = this.core.y;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        core.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        ring.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
