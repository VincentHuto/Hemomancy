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

public class CleansingAltarModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("modelaltarcleasning"), "main");
    private final ModelPart steps;
    private final ModelPart basin;
    private final ModelPart pale_lady;
    private final ModelPart gothic_arch;
    private final ModelPart curtains;
    private final ModelPart urn_left;
    private final ModelPart urn_right;
    private final ModelPart cloth_drapes;


    public CleansingAltarModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.steps = root.getChild("steps");
        this.basin = root.getChild("basin");
        this.pale_lady = root.getChild("pale_lady");
        this.gothic_arch = root.getChild("gothic_arch");
        this.curtains = root.getChild("curtains");
        this.urn_left = root.getChild("urn_left");
        this.urn_right = root.getChild("urn_right");
        this.cloth_drapes = root.getChild("cloth_drapes");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition steps = partdefinition.addOrReplaceChild("steps", CubeListBuilder.create().texOffs(0, 0).addBox(-15.0F, -5.5F, -10.0F, 30.0F, 3.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-12.5F, -8.0F, -7.5F, 25.0F, 3.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 46).addBox(-10.0F, -8.5F, -5.0F, 20.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 26.0F, 0.0F));

        PartDefinition basin = partdefinition.addOrReplaceChild("basin", CubeListBuilder.create().texOffs(87, 34).addBox(-4.25F, -2.0F, -3.75F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(67, 104).addBox(-2.5F, -3.875F, -2.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(43, 58).addBox(-6.75F, -6.75F, -5.0F, 13.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(101, 10).addBox(-6.75F, -7.25F, -5.0F, 13.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(100, 103).addBox(5.25F, -7.25F, -4.75F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(8, 111).addBox(-6.75F, -7.25F, -4.75F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.5F, 0.0F));

        PartDefinition pale_lady = partdefinition.addOrReplaceChild("pale_lady", CubeListBuilder.create().texOffs(87, 24).addBox(-6.75F, -4.0F, -1.875F, 13.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(67, 112).addBox(5.75F, -3.0F, -4.375F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(113, 71).addBox(-9.25F, -3.0F, -4.375F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(88, 63).addBox(-5.0F, -3.0F, -6.875F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(67, 91).addBox(-4.25F, -11.75F, -1.875F, 8.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(88, 53).addBox(-4.625F, -12.75F, -2.5F, 9.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(120, 91).addBox(-1.75F, -14.5F, -1.25F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(101, 13).addBox(-2.5F, -19.375F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(101, 0).addBox(-5.375F, -22.125F, 0.0F, 11.0F, 9.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(101, 91).addBox(-2.875F, -19.0F, 1.25F, 6.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 112).addBox(3.25F, -11.5F, -1.875F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(120, 97).addBox(3.0F, -7.75F, -4.375F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(87, 118).addBox(-5.75F, -11.5F, -1.875F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(120, 105).addBox(-5.125F, -7.75F, -4.375F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 120).addBox(-2.125F, -5.75F, -5.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.25F, 5.625F));

        PartDefinition gothic_arch = partdefinition.addOrReplaceChild("gothic_arch", CubeListBuilder.create().texOffs(0, 58).addBox(-10.375F, -28.0F, -0.75F, 21.0F, 28.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(41, 91).addBox(8.25F, -25.0F, -1.875F, 3.0F, 25.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(54, 91).addBox(-11.75F, -25.0F, -1.875F, 3.0F, 25.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(92, 91).addBox(6.5F, -23.0F, -2.125F, 1.0F, 23.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 97).addBox(-7.25F, -23.0F, -2.125F, 1.0F, 23.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(9, 97).addBox(-5.0F, -30.5F, -1.25F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(108, 45).addBox(3.75F, -28.0F, -1.25F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(117, 52).addBox(-8.75F, -28.0F, -1.25F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(118, 121).addBox(-2.5F, -29.0F, -1.875F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(122, 13).addBox(3.25F, -25.5F, -1.875F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(122, 18).addBox(-6.75F, -25.5F, -1.875F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(67, 122).addBox(-1.75F, -25.5F, -1.875F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(9, 104).addBox(-4.25F, -34.0F, -1.25F, 8.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(36, 97).addBox(4.0F, -34.0F, -0.625F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(86, 112).addBox(-4.75F, -34.0F, -0.625F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(9, 122).addBox(-2.5F, -34.75F, -0.625F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(117, 66).addBox(5.75F, -22.25F, -2.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(120, 40).addBox(-9.25F, -22.25F, -2.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 21.0F, 9.375F));

        PartDefinition curtains = partdefinition.addOrReplaceChild("curtains", CubeListBuilder.create(), PartPose.offset(0.0F, -11.5F, 10.0F));

        PartDefinition urn_left = partdefinition.addOrReplaceChild("urn_left", CubeListBuilder.create().texOffs(61, 53).addBox(-1.75F, -1.0F, -1.25F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(113, 81).addBox(-2.125F, -6.25F, -1.875F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 104).addBox(-0.375F, -8.25F, -1.25F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(120, 34).addBox(-1.75F, -9.125F, -1.875F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(22, 122).addBox(1.5F, -7.75F, -0.625F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(76, 122).addBox(-2.875F, -7.75F, -0.625F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(8.75F, 17.25F, 1.25F));

        PartDefinition urn_right = partdefinition.addOrReplaceChild("urn_right", CubeListBuilder.create().texOffs(74, 53).addBox(-1.75F, -1.0F, -1.25F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(101, 114).addBox(-2.125F, -6.25F, -1.875F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 120).addBox(-0.375F, -8.25F, -1.25F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(43, 120).addBox(-1.75F, -9.125F, -1.875F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(81, 122).addBox(1.5F, -7.75F, -0.625F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 124).addBox(-2.875F, -7.75F, -0.625F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.75F, 17.25F, 1.25F));

        PartDefinition cloth_drapes = partdefinition.addOrReplaceChild("cloth_drapes", CubeListBuilder.create().texOffs(117, 59).addBox(7.625F, -0.5F, -8.75F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(118, 114).addBox(-12.625F, -0.5F, -8.75F, 5.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(43, 71).addBox(12.5F, -0.5F, -6.25F, 1.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(78, 71).addBox(-13.25F, -0.5F, -6.25F, 1.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 87).addBox(-7.875F, -0.5F, -11.25F, 16.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(61, 46).addBox(-10.0F, 2.0F, -12.5F, 20.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        steps.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        basin.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        pale_lady.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        gothic_arch.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        curtains.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        urn_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        urn_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        cloth_drapes.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }
}