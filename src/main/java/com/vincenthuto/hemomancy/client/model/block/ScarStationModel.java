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

public class ScarStationModel extends Model {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("chisel_station_model"), "main");
    private final ModelPart ChiselStation;
    private final ModelPart Desk;
    private final ModelPart Backboard;
    private final ModelPart ToolRack;
    private final ModelPart Shelf;
    private final ModelPart DeskSurface;
    private final ModelPart ChiselTool;


    public ScarStationModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.ChiselStation = root.getChild("ChiselStation");
        this.Desk = this.ChiselStation.getChild("Desk");
        this.Backboard = this.ChiselStation.getChild("Backboard");
        this.ToolRack = this.ChiselStation.getChild("ToolRack");
        this.Shelf = this.ChiselStation.getChild("Shelf");
        this.DeskSurface = this.ChiselStation.getChild("DeskSurface");
        this.ChiselTool = this.DeskSurface.getChild("ChiselTool");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ChiselStation = partdefinition.addOrReplaceChild("ChiselStation", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition Desk = ChiselStation.addOrReplaceChild("Desk", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -3.0F, -8.0F, 22.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(6.0F, 0.0F, -8.0F, 5.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(43, 20).addBox(-11.0F, 0.0F, -8.0F, 5.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 69).addBox(-6.0F, 3.0F, -1.0F, 12.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Backboard = ChiselStation.addOrReplaceChild("Backboard", CubeListBuilder.create().texOffs(0, 49).addBox(-11.0F, -20.0F, -8.0F, 22.0F, 17.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ToolRack = ChiselStation.addOrReplaceChild("ToolRack", CubeListBuilder.create().texOffs(49, 67).addBox(-8.0F, -5.0F, -9.0F, 16.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(59, 83).addBox(6.0F, -11.0F, -9.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 83).addBox(2.0F, -12.0F, -9.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 85).addBox(4.0F, -10.0F, -9.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(64, 83).addBox(-3.0F, -11.0F, -9.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(82, 72).addBox(-5.0F, -13.0F, -9.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(5, 85).addBox(-7.0F, -10.0F, -9.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 3.0F));

        PartDefinition Shelf = ChiselStation.addOrReplaceChild("Shelf", CubeListBuilder.create().texOffs(49, 49).addBox(-11.0F, -22.0F, -10.0F, 22.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(69, 72).addBox(7.0F, -28.0F, -9.9F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 75).addBox(-10.0F, -28.0F, -9.9F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(50, 72).addBox(-3.0F, -29.0F, -9.9F, 6.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(13, 83).addBox(-6.0F, -25.0F, -9.9F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition DeskSurface = ChiselStation.addOrReplaceChild("DeskSurface", CubeListBuilder.create().texOffs(49, 56).addBox(-5.0F, -4.0F, -4.0F, 11.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(13, 75).addBox(8.0F, -7.0F, -2.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(77, 13).addBox(7.0F, -6.0F, 2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(69, 82).addBox(-8.0F, -8.0F, -3.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(78, 87).addBox(-7.5F, -9.0F, -2.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(78, 87).addBox(-8.5F, -7.0F, 1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(78, 87).addBox(8.0F, -7.0F, 3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 83).addBox(-9.0F, -6.0F, 1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(77, 0).addBox(-11.0F, -7.0F, -2.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ChiselTool = DeskSurface.addOrReplaceChild("ChiselTool", CubeListBuilder.create().texOffs(77, 8).addBox(-2.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -4.0F, -1.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        ChiselStation.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}