package com.vincenthuto.hemomancy.client.model.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class GhastlyAlembicModel extends Model {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("ghastly_alembic_model"), "main");
    private final ModelPart GhastlyAlembic;
    private final ModelPart Base;
    private final ModelPart Flask;
    private final ModelPart ThornCollar;
    private final ModelPart Cap;
    private final ModelPart PipeRod;

    public GhastlyAlembicModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.GhastlyAlembic = root.getChild("GhastlyAlembic");
        this.Base = this.GhastlyAlembic.getChild("Base");
        this.Flask = this.GhastlyAlembic.getChild("Flask");
        this.ThornCollar = this.GhastlyAlembic.getChild("ThornCollar");
        this.Cap = this.GhastlyAlembic.getChild("Cap");
        this.PipeRod = this.GhastlyAlembic.getChild("PipeRod");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition GhastlyAlembic = partdefinition.addOrReplaceChild("GhastlyAlembic", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Base = GhastlyAlembic.addOrReplaceChild("Base", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(41, 19).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(52, 67).addBox(-1.0F, -4.0F, -8.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 72).addBox(-1.0F, -4.0F, 6.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(61, 67).addBox(6.0F, -4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(70, 67).addBox(-8.0F, -4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Flask = GhastlyAlembic.addOrReplaceChild("Flask", CubeListBuilder.create().texOffs(0, 19).addBox(-5.0F, -8.0F, -5.0F, 10.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition ThornCollar = GhastlyAlembic.addOrReplaceChild("ThornCollar", CubeListBuilder.create().texOffs(34, 62).addBox(-6.0F, 1.0F, 5.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(61, 62).addBox(-6.0F, 1.0F, -6.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(65, 5).addBox(-6.0F, -6.0F, 5.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(57, 48).addBox(-6.0F, -6.0F, -5.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(34, 48).addBox(5.0F, -6.0F, -5.0F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(65, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(41, 34).addBox(4.0F, 1.0F, -5.0F, 2.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(-6.0F, 1.0F, -5.0F, 2.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition Cap = GhastlyAlembic.addOrReplaceChild("Cap", CubeListBuilder.create().texOffs(65, 10).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 60).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(43, 67).addBox(1.0F, -5.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 67).addBox(-3.0F, -5.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 39).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -25.0F, 0.0F));

        PartDefinition PipeRod = GhastlyAlembic.addOrReplaceChild("PipeRod", CubeListBuilder.create().texOffs(25, 46).addBox(-1.0F, -28.0F, -1.0F, 2.0F, 26.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 71).addBox(1.0F, -18.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(9, 71).addBox(1.0F, -11.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 34).addBox(1.0F, -30.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        GhastlyAlembic.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}