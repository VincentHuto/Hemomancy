package com.vincenthuto.hemomancy.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class VisceralMirrorModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("modelvisceralmirrormodel"), "main");
            private final ModelPart base;
            private final ModelPart frame;
            private final ModelPart support;

    public VisceralMirrorModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.base = root.getChild("base");
        this.frame = root.getChild("frame");
        this.support = root.getChild("support");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 40).addBox(-7.0F, -2.0F, 6.0F, 14.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(31, 55).addBox(-5.0F, -3.0F, 6.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(25, 0).addBox(-1.0F, -4.0F, -6.0F, 2.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(31, 40).addBox(-7.0F, -2.0F, -7.0F, 14.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 55).addBox(-5.0F, -3.0F, -7.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition frame = partdefinition.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -27.5F, -5.5F, 1.0F, 23.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(25, 14).addBox(-1.5F, -27.5F, -5.5F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(25, 27).addBox(-1.5F, -5.5F, -5.5F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 44).addBox(-1.5F, -26.5F, 4.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(5, 44).addBox(-1.5F, -26.5F, -5.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition support = partdefinition.addOrReplaceChild("support", CubeListBuilder.create().texOffs(10, 44).addBox(-1.0F, -17.0F, 6.0F, 2.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(35, 44).addBox(-1.5F, -22.0F, -7.5F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 44).addBox(-1.5F, -22.0F, 5.5F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(17, 44).addBox(-1.0F, -17.0F, -7.0F, 2.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        frame.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        support.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}