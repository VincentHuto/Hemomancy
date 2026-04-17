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

public class HarbingerSaintSarcophagusModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("modelharbingersaintsarcophagus"), "main");
    private final ModelPart sarcophagus_base;
    private final ModelPart corpse;
    private final ModelPart offeringbowl;
    private final ModelPart sarcophagus_lid;
    private final ModelPart effigy;
    private final ModelPart halo;
    private final ModelPart braziers;
    private final ModelPart tendrils;

    public HarbingerSaintSarcophagusModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.sarcophagus_base = root.getChild("sarcophagus_base");
        this.corpse = this.sarcophagus_base.getChild("corpse");
        this.offeringbowl = this.sarcophagus_base.getChild("offeringbowl");
        this.sarcophagus_lid = this.sarcophagus_base.getChild("sarcophagus_lid");
        this.effigy = this.sarcophagus_lid.getChild("effigy");
        this.halo = root.getChild("halo");
        this.braziers = root.getChild("braziers");
        this.tendrils = root.getChild("tendrils");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition sarcophagus_base = partdefinition.addOrReplaceChild("sarcophagus_base", CubeListBuilder.create().texOffs(0, 0).addBox(-11.9211F, 3.0317F, -8.0781F, 24.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 20).addBox(-9.9211F, -1.9683F, -6.0781F, 20.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52).addBox(-8.9211F, -2.9683F, -5.0781F, 18.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(57, 60).addBox(-8.9211F, -4.9683F, 3.9219F, 18.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-8.9211F, -4.9683F, -5.0781F, 18.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(65, 35).addBox(8.0789F, -4.9683F, -4.0781F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(66, 64).addBox(-8.9211F, -4.9683F, -4.0781F, 1.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(68, 84).addBox(10.6765F, -5.9683F, -8.5894F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(86, 84).addBox(-12.4211F, -2.9683F, -8.5781F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(59, 84).addBox(10.6765F, -5.9683F, 6.4106F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(77, 84).addBox(-12.4211F, -2.9683F, 6.4219F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(57, 52).addBox(-9.8235F, -0.9683F, -7.0894F, 20.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(57, 56).addBox(-9.8235F, -0.9683F, 5.9106F, 20.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(39, 64).addBox(10.1765F, -0.9683F, -6.0894F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(65, 20).addBox(-10.9211F, -0.9683F, -6.0781F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0789F, 17.9683F, -0.1719F, 0.0F, -1.5708F, 0.0F));

        PartDefinition corpse = sarcophagus_base.addOrReplaceChild("corpse", CubeListBuilder.create().texOffs(40, 79).addBox(-2.0976F, -1.05F, -2.7F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(81, 0).addBox(-2.0976F, -1.55F, -6.6887F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(66, 75).addBox(-2.0976F, -0.65F, 2.3113F, 4.0F, 1.5F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(84, 35).addBox(1.9024F, -0.95F, -2.6887F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(84, 43).addBox(-4.0976F, -0.95F, -2.6887F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4265F, -2.9183F, -0.1394F, 0.0F, -1.5708F, 0.0F));

        PartDefinition offeringbowl = sarcophagus_base.addOrReplaceChild("offeringbowl", CubeListBuilder.create().texOffs(87, 71).addBox(-1.0976F, -4.0F, -0.9887F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 77).addBox(-3.0976F, -5.0F, -2.4887F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.8235F, 6.0317F, -0.0894F));

        PartDefinition sarcophagus_lid = sarcophagus_base.addOrReplaceChild("sarcophagus_lid", CubeListBuilder.create().texOffs(0, 38).addBox(-20.0976F, -9.9615F, -7.5079F, 20.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(10.1765F, 3.9932F, 1.4299F));

        PartDefinition effigy = sarcophagus_lid.addOrReplaceChild("effigy", CubeListBuilder.create().texOffs(21, 79).addBox(-2.0976F, -1.05F, -2.7F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(21, 68).addBox(-2.0976F, -2.05F, -6.6887F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 68).addBox(-2.0976F, -0.55F, 2.3113F, 4.0F, 1.5F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(81, 9).addBox(1.9024F, -0.05F, -2.6887F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 84).addBox(-4.0976F, -0.05F, -2.6887F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.75F, -10.9115F, -1.5692F, 0.0F, -1.5708F, 0.0F));

        PartDefinition halo = partdefinition.addOrReplaceChild("halo", CubeListBuilder.create().texOffs(0, 90).addBox(-6.0F, -0.2778F, -0.5F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(14, 90).addBox(-7.0F, 0.7222F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 90).addBox(5.0F, 0.7222F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(-0.5F, -5.2778F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, 2.2778F, -5.8F, 0.0F, -1.5708F, 0.0F));

        PartDefinition halo_spike_4l_r1 = halo.addOrReplaceChild("halo_spike_4l_r1", CubeListBuilder.create().texOffs(32, 96).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 3.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, 3.2222F, 0.0F, 0.0F, 0.0F, 0.8727F));

        PartDefinition halo_spike_4r_r1 = halo.addOrReplaceChild("halo_spike_4r_r1", CubeListBuilder.create().texOffs(28, 96).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 3.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.5F, 3.2222F, 0.0F, 0.0F, 0.0F, -0.8727F));

        PartDefinition halo_spike_3l_r1 = halo.addOrReplaceChild("halo_spike_3l_r1", CubeListBuilder.create().texOffs(24, 96).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, 1.7222F, 0.0F, 0.0F, 0.0F, 0.6109F));

        PartDefinition halo_spike_3r_r1 = halo.addOrReplaceChild("halo_spike_3r_r1", CubeListBuilder.create().texOffs(20, 96).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, 1.7222F, 0.0F, 0.0F, 0.0F, -0.6109F));

        PartDefinition halo_spike_2l_r1 = halo.addOrReplaceChild("halo_spike_2l_r1", CubeListBuilder.create().texOffs(16, 96).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.7222F, 0.0F, 0.0F, 0.0F, 0.384F));

        PartDefinition halo_spike_2r_r1 = halo.addOrReplaceChild("halo_spike_2r_r1", CubeListBuilder.create().texOffs(12, 96).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.7222F, 0.0F, 0.0F, 0.0F, -0.384F));

        PartDefinition halo_spike_1l_r1 = halo.addOrReplaceChild("halo_spike_1l_r1", CubeListBuilder.create().texOffs(8, 96).addBox(-0.5F, -4.5F, -0.5F, 1.0F, 4.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2222F, 0.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition halo_spike_1r_r1 = halo.addOrReplaceChild("halo_spike_1r_r1", CubeListBuilder.create().texOffs(4, 96).addBox(-0.5F, -4.5F, -0.5F, 1.0F, 4.5F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2222F, 0.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition braziers = partdefinition.addOrReplaceChild("braziers", CubeListBuilder.create().texOffs(30, 87).addBox(10.4183F, -3.0F, -9.1516F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(43, 87).addBox(-12.5817F, 0.0F, -9.1516F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(85, 64).addBox(10.4183F, -3.0F, 5.8484F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(17, 87).addBox(-12.5817F, 0.0F, 5.8484F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1683F, 12.0F, -0.3484F, 0.0F, -1.5708F, 0.0F));

        PartDefinition tendrils = partdefinition.addOrReplaceChild("tendrils", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, 18.0F, -0.1333F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        sarcophagus_base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        halo.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        braziers.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        tendrils.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}