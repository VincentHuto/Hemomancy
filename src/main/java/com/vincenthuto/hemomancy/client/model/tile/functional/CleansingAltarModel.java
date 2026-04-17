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

        PartDefinition steps = partdefinition.addOrReplaceChild("steps", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -4.0F, -8.0F, 24.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-10.0F, -6.0F, -6.0F, 20.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 36).addBox(-8.0F, -7.0F, -4.0F, 16.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 26.0F, 0.0F));

        PartDefinition basin = partdefinition.addOrReplaceChild("basin", CubeListBuilder.create().texOffs(64, 67).addBox(-3.0F, -1.5F, -3.0F, 6.0F, 1.5F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(81, 5).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 1.5F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(49, 36).addBox(-5.0F, -5.0F, -4.0F, 10.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(69, 33).addBox(-5.0F, -6.0F, -4.0F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(83, 76).addBox(4.0F, -6.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(84, 36).addBox(-5.0F, -6.0F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition pale_lady = partdefinition.addOrReplaceChild("pale_lady", CubeListBuilder.create().texOffs(64, 59).addBox(-5.0F, -3.0F, -1.5F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(83, 84).addBox(5.0F, -2.0F, -3.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(64, 86).addBox(-7.0F, -2.0F, -3.5F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(69, 27).addBox(-4.0F, -2.0F, -5.5F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(64, 76).addBox(-3.0F, -9.0F, -1.5F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(69, 19).addBox(-3.5F, -10.0F, -2.0F, 7.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(98, 5).addBox(-1.0F, -11.5F, -1.0F, 2.0F, 1.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 80).addBox(-2.0F, -15.5F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 90).addBox(-3.0F, -15.0F, -1.5F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(45, 90).addBox(2.0F, -15.0F, -1.5F, 1.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(49, 80).addBox(-2.5F, -15.0F, 1.0F, 5.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(54, 90).addBox(3.0F, -9.5F, -1.5F, 2.0F, 3.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(54, 97).addBox(2.5F, -6.0F, -3.5F, 1.5F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(90, 92).addBox(-5.0F, -9.5F, -1.5F, 2.0F, 3.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 98).addBox(-4.0F, -6.0F, -3.5F, 1.5F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(93, 59).addBox(-1.5F, -4.5F, -4.0F, 3.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 19.0F, 4.5F));

        PartDefinition gothic_arch = partdefinition.addOrReplaceChild("gothic_arch", CubeListBuilder.create().texOffs(0, 46).addBox(-8.5F, -22.0F, -0.6F, 17.0F, 22.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 69).addBox(7.0F, -20.0F, -1.5F, 2.0F, 20.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(9, 69).addBox(-9.0F, -20.0F, -1.5F, 2.0F, 20.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 69).addBox(5.0F, -18.0F, -1.7F, 1.0F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(25, 69).addBox(-6.0F, -18.0F, -1.7F, 1.0F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(81, 0).addBox(-4.0F, -24.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(92, 24).addBox(3.0F, -22.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(92, 29).addBox(-7.0F, -22.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(79, 92).addBox(-2.0F, -23.0F, -1.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(98, 84).addBox(3.0F, -20.0F, -1.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(98, 88).addBox(-5.0F, -20.0F, -1.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(99, 34).addBox(-1.0F, -20.0F, -1.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(81, 12).addBox(-3.0F, -27.0F, -1.0F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(27, 90).addBox(3.0F, -27.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(13, 92).addBox(-4.0F, -27.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(93, 64).addBox(-2.0F, -28.0F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(98, 14).addBox(5.0F, -18.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(98, 75).addBox(-7.0F, -18.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.0F, 7.5F));

        PartDefinition curtains = partdefinition.addOrReplaceChild("curtains", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 8.0F));

        PartDefinition urn_left = partdefinition.addOrReplaceChild("urn_left", CubeListBuilder.create().texOffs(9, 98).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(32, 89).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 97).addBox(-0.5F, -6.5F, -1.0F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(63, 94).addBox(-1.0F, -7.0F, -1.5F, 2.0F, 0.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(85, 97).addBox(1.5F, -6.0F, -0.5F, 0.5F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(99, 38).addBox(-2.0F, -6.0F, -0.5F, 0.5F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, 19.0F, 1.0F));

        PartDefinition urn_right = partdefinition.addOrReplaceChild("urn_right", CubeListBuilder.create().texOffs(98, 10).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(89, 67).addBox(-1.5F, -5.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(98, 79).addBox(-0.5F, -6.5F, -1.0F, 1.0F, 1.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(27, 97).addBox(-1.0F, -7.0F, -1.5F, 2.0F, 0.5F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(63, 99).addBox(1.5F, -6.0F, -0.5F, 0.5F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(68, 99).addBox(-2.0F, -6.0F, -0.5F, 0.5F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, 19.0F, 1.0F));

        PartDefinition cloth_drapes = partdefinition.addOrReplaceChild("cloth_drapes", CubeListBuilder.create().texOffs(0, 92).addBox(6.1F, -0.5F, -7.0F, 4.0F, 2.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(92, 18).addBox(-10.1F, -0.5F, -7.0F, 4.0F, 2.5F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(35, 46).addBox(10.0F, -0.5F, -5.0F, 1.0F, 2.5F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(35, 63).addBox(-11.0F, -0.5F, -5.0F, 1.0F, 2.5F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(64, 51).addBox(-6.5F, -0.4F, -9.0F, 13.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(64, 46).addBox(-8.0F, 2.0F, -10.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        steps.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        basin.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        pale_lady.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        gothic_arch.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        curtains.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        urn_left.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        urn_right.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        cloth_drapes.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}