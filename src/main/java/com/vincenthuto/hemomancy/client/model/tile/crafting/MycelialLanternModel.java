package com.vincenthuto.hemomancy.client.model.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class MycelialLanternModel extends Model {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("mycelial_lantern_model"), "main");

    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart glassChamber;
    private final ModelPart cap;
    private final ModelPart supports;
    private final ModelPart lanternCore;

    public MycelialLanternModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root.getChild("mycelial_lantern");
        this.base = this.root.getChild("base");
        this.glassChamber = this.root.getChild("glass_chamber");
        this.cap = this.root.getChild("cap");
        this.supports = this.root.getChild("supports");
        this.lanternCore = this.root.getChild("lantern_core");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition mycelial_lantern = partdefinition.addOrReplaceChild("mycelial_lantern", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition base = mycelial_lantern.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -3.0F, -7.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-5.0F, -6.0F, -5.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition glass_chamber = mycelial_lantern.addOrReplaceChild("glass_chamber", CubeListBuilder.create().texOffs(54, 46).addBox(-5.0F, -22.0F, -5.0F, 10.0F, 14.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cap = mycelial_lantern.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 39).addBox(-6.0F, -25.0F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(48, 30).addBox(-3.0F, -29.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 55).addBox(-2.0F, -33.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition supports = mycelial_lantern.addOrReplaceChild("supports", CubeListBuilder.create().texOffs(80, 0).addBox(4.0F, -22.0F, -6.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(88, 0).addBox(-6.0F, -22.0F, -6.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(96, 0).addBox(4.0F, -22.0F, 4.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(104, 0).addBox(-6.0F, -22.0F, 4.0F, 2.0F, 16.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lantern_core = mycelial_lantern.addOrReplaceChild("lantern_core", CubeListBuilder.create().texOffs(72, 30).addBox(-3.0F, -16.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(96, 30).addBox(-2.0F, -18.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
            int packedColor) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    public void renderOpaqueParts(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
            int packedColor) {
        renderFrameParts(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        lanternCore.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    public void renderFrameParts(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
            int packedColor) {
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        cap.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        supports.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    public void renderGlass(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
            int packedColor) {
        glassChamber.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }
}
