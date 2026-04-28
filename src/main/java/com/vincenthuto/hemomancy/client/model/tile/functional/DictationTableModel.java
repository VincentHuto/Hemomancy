package com.vincenthuto.hemomancy.client.model.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class DictationTableModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("dictationtablemodel"), "main");

    private final ModelPart base;
    private final ModelPart tableTop;
    private final ModelPart bone;

    public DictationTableModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.base = root.getChild("base");
        this.tableTop = this.base.getChild("tableTop");
        this.bone = this.tableTop.getChild("bone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(25, 54).addBox(-7.0F, -14.0F, 3.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(42, 54).addBox(3.0F, -14.0F, 3.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 57).addBox(-7.0F, -14.0F, -7.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(56, 31).addBox(3.0F, -14.0F, -7.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 47).addBox(-8.0F, -3.0F, -8.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(31, 44).addBox(-8.0F, -3.0F, 2.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(41, 21).addBox(2.0F, -3.0F, 2.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(31, 34).addBox(2.0F, -3.0F, -8.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition tableTop = base.addOrReplaceChild("tableTop", CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, -16.0F, -2.0F, 18.0F, 2.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 21).addBox(-13.0F, -8.0F, 2.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 0.0F, -7.0F));

        PartDefinition bone = tableTop.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 34).addBox(-7.0F, -3.0F, -3.0F, 13.0F, 10.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.5F, -18.75F, 6.5F, 0.0F, 1.5708F, -1.7453F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }
}
