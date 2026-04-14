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

public class SomaticLoomModel extends Model {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("somatic_loom_model"), "main");
    private final ModelPart SomaticLoom;
    private final ModelPart BaseFrame;
    private final ModelPart Columns;
    private final ModelPart TopBeam;
    private final ModelPart CrankLeft;
    private final ModelPart CrankRight;
    private final ModelPart WarpPanel;
    private final ModelPart WeaveBed;
    private final ModelPart HangingThreads;

    public SomaticLoomModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.SomaticLoom = root.getChild("SomaticLoom");
        this.BaseFrame = this.SomaticLoom.getChild("BaseFrame");
        this.Columns = this.SomaticLoom.getChild("Columns");
        this.TopBeam = this.SomaticLoom.getChild("TopBeam");
        this.CrankLeft = this.SomaticLoom.getChild("CrankLeft");
        this.CrankRight = this.SomaticLoom.getChild("CrankRight");
        this.WarpPanel = this.SomaticLoom.getChild("WarpPanel");
        this.WeaveBed = this.SomaticLoom.getChild("WeaveBed");
        this.HangingThreads = this.SomaticLoom.getChild("HangingThreads");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition SomaticLoom = partdefinition.addOrReplaceChild("SomaticLoom", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

        PartDefinition BaseFrame = SomaticLoom.addOrReplaceChild("BaseFrame", CubeListBuilder.create().texOffs(55, 29).addBox(6.0F, 0.0F, -5.0F, 5.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(63, 0).addBox(-11.0F, 0.0F, -5.0F, 5.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(55, 19).addBox(-11.0F, -2.0F, 4.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(55, 24).addBox(-11.0F, -2.0F, -6.0F, 22.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Columns = SomaticLoom.addOrReplaceChild("Columns", CubeListBuilder.create().texOffs(0, 53).addBox(8.0F, -38.0F, -4.0F, 3.0F, 38.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(23, 53).addBox(-11.0F, -38.0F, -4.0F, 3.0F, 38.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(55, 42).addBox(5.0F, -34.0F, -3.0F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(46, 63).addBox(-12.0F, -34.0F, -3.0F, 7.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition TopBeam = SomaticLoom.addOrReplaceChild("TopBeam", CubeListBuilder.create().texOffs(47, 53).addBox(-10.0F, -1.0F, -3.0F, 20.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(86, 29).addBox(7.0F, -5.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(86, 63).addBox(-11.0F, -5.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -36.0F, 0.0F));

        PartDefinition CrankLeft = SomaticLoom.addOrReplaceChild("CrankLeft", CubeListBuilder.create().texOffs(82, 42).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(86, 71).addBox(2.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(11.0F, -19.0F, 0.0F));

        PartDefinition CrankRight = SomaticLoom.addOrReplaceChild("CrankRight", CubeListBuilder.create().texOffs(72, 82).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-11.0F, -19.0F, 0.0F));

        PartDefinition WarpPanel = SomaticLoom.addOrReplaceChild("WarpPanel", CubeListBuilder.create().texOffs(73, 63).addBox(3.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(46, 74).addBox(-1.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(59, 74).addBox(-5.0F, -14.0F, -2.0F, 2.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -21.0F, 0.0F));

        PartDefinition WeaveBed = SomaticLoom.addOrReplaceChild("WeaveBed", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -6.0F, -7.0F, 16.0F, 3.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-6.0F, -6.3F, -10.0F, 11.0F, 0.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(63, 13).addBox(-7.0F, 0.0F, 5.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

        PartDefinition weave_bed_r1 = WeaveBed.addOrReplaceChild("weave_bed_r1", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, 1.0F, -8.0F, 11.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.9829F, -6.5442F, 2.138F, 0.0F, 0.0F));

        PartDefinition HangingThreads = SomaticLoom.addOrReplaceChild("HangingThreads", CubeListBuilder.create().texOffs(46, 93).addBox(5.0F, -29.0F, 2.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(92, 78).addBox(1.0F, -29.0F, 2.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(87, 78).addBox(3.0F, -29.0F, 2.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(87, 87).addBox(-4.0F, -29.0F, 2.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, -6.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        SomaticLoom.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

}