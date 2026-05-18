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
import net.minecraft.util.Mth;

public class PuppeteersSpindleModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("puppeteers_spindle_model"), "main");

    private static final int STONE_COLOR = -1;
    private static final int DARK_WOOD_COLOR = 0xFF6B2A20;
    private static final int THREAD_COLOR = 0xFFC72435;

    private final ModelPart root;
    private final ModelPart stone;
    private final ModelPart wood;
    private final ModelPart spoolWood;
    private final ModelPart thread;

    public PuppeteersSpindleModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root.getChild("PuppeteersSpindle");
        this.stone = this.root.getChild("Stone");
        this.wood = this.root.getChild("Wood");
        this.spoolWood = this.root.getChild("SpoolWood");
        this.thread = this.root.getChild("Thread");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition PuppeteersSpindle = partdefinition.addOrReplaceChild("PuppeteersSpindle", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Stone = PuppeteersSpindle.addOrReplaceChild("Stone", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -2.0F, -6.0F, 14.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-5.0F, -4.0F, -4.0F, 10.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Wood = PuppeteersSpindle.addOrReplaceChild("Wood", CubeListBuilder.create().texOffs(0, 26).addBox(5.0F, -13.0F, -3.0F, 2.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(17, 26).addBox(-7.0F, -13.0F, -3.0F, 2.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 24).addBox(-7.0F, -17.0F, -5.0F, 14.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-6.0F, -5.0F, -5.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(29, 43).addBox(-6.0F, -5.0F, 3.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition SpoolWood = PuppeteersSpindle.addOrReplaceChild("SpoolWood", CubeListBuilder.create().texOffs(-1, 55).addBox(-8.0F, -1.0F, -1.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(33, 47).addBox(3.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(50, 47).addBox(-5.0F, -4.0F, -4.0F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

        PartDefinition Thread = PuppeteersSpindle.addOrReplaceChild("Thread", CubeListBuilder.create().texOffs(0, 60).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(25, 60).addBox(-3.5F, -1.0F, -3.5F, 7.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(25, 70).addBox(-3.5F, -3.5F, -1.0F, 7.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void setupAnim(float ageInTicks) {
        float rotation = ageInTicks * 0.045F;
        this.spoolWood.xRot = rotation;
        this.thread.xRot = rotation + Mth.sin(ageInTicks * 0.08F) * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               int packedColor) {
        this.stone.render(poseStack, vertexConsumer, packedLight, packedOverlay, STONE_COLOR);
        this.wood.render(poseStack, vertexConsumer, packedLight, packedOverlay, DARK_WOOD_COLOR);
        this.spoolWood.render(poseStack, vertexConsumer, packedLight, packedOverlay, DARK_WOOD_COLOR);
        this.thread.render(poseStack, vertexConsumer, packedLight, packedOverlay, THREAD_COLOR);
    }
}
