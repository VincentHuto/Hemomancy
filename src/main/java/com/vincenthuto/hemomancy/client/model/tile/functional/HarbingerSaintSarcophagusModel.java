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
    private final ModelPart braziers;
    private final ModelPart tendrils;

    public HarbingerSaintSarcophagusModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.sarcophagus_base = root.getChild("sarcophagus_base");
        this.corpse = this.sarcophagus_base.getChild("corpse");
        this.offeringbowl = this.sarcophagus_base.getChild("offeringbowl");
        this.sarcophagus_lid = this.sarcophagus_base.getChild("sarcophagus_lid");
        this.effigy = this.sarcophagus_lid.getChild("effigy");
        this.braziers = root.getChild("braziers");
        this.tendrils = root.getChild("tendrils");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition sarcophagus_base = partdefinition.addOrReplaceChild("sarcophagus_base", CubeListBuilder.create().texOffs(0, 0).addBox(-17.8817F, 4.0475F, -12.1171F, 36.0F, 5.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(-14.8817F, -3.4525F, -9.1171F, 30.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(0, 78).addBox(-13.3817F, -4.9525F, -7.6171F, 27.0F, 2.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(85, 90).addBox(-13.3817F, -7.4525F, 5.8829F, 27.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(82, 96).addBox(-13.3817F, -7.4525F, -7.6171F, 27.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(97, 43).addBox(11.6183F, -7.4525F, -6.1171F, 2.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(97, 59).addBox(-13.8816F, -7.4525F, -6.1171F, 2.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(75, 124).addBox(16.0147F, -8.9525F, -12.884F, 3.0F, 18.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(130, 24).addBox(-18.6316F, -4.9525F, -12.8671F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(88, 124).addBox(16.0147F, -8.9525F, 9.616F, 3.0F, 18.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(50, 130).addBox(-18.6316F, -4.9525F, 9.6329F, 3.0F, 14.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(85, 78).addBox(-14.7353F, -1.4525F, -10.634F, 30.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(85, 84).addBox(-14.7353F, -1.4525F, 8.866F, 30.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 96).addBox(14.7647F, -1.4525F, -9.134F, 2.0F, 3.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(41, 96).addBox(-16.8816F, -1.4525F, -9.1171F, 2.0F, 3.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0183F, 14.8136F, 2.6421F, 0.0F, -1.5708F, 0.0F));

        PartDefinition corpse = sarcophagus_base.addOrReplaceChild("corpse", CubeListBuilder.create().texOffs(115, 101).addBox(-3.1464F, -1.575F, -4.05F, 6.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(25, 118).addBox(-3.1464F, -2.325F, -10.0331F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(82, 112).addBox(-3.1464F, -0.725F, 3.4669F, 6.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(121, 12).addBox(2.8536F, -1.925F, -4.0331F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(113, 123).addBox(-6.1464F, -1.925F, -4.0331F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6397F, -4.3775F, -0.209F, 0.0F, -1.5708F, 0.0F));

        PartDefinition offeringbowl = sarcophagus_base.addOrReplaceChild("offeringbowl", CubeListBuilder.create().texOffs(19, 131).addBox(-1.6464F, -6.0F, -1.4831F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(82, 101).addBox(-5.1464F, -8.0F, -3.7331F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-19.2353F, 9.0475F, -0.134F));

        PartDefinition sarcophagus_lid = sarcophagus_base.addOrReplaceChild("sarcophagus_lid", CubeListBuilder.create().texOffs(0, 57).addBox(-30.1464F, -15.4423F, -11.2619F, 30.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(15.2647F, 5.9898F, 2.1448F));

        PartDefinition effigy = sarcophagus_lid.addOrReplaceChild("effigy", CubeListBuilder.create().texOffs(113, 112).addBox(-3.1464F, -1.575F, -4.05F, 6.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 118).addBox(-3.1464F, -3.075F, -10.0331F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(97, 30).addBox(-3.1464F, -0.575F, 2.4669F, 6.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(50, 118).addBox(2.8536F, -0.575F, -4.0331F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(121, 0).addBox(-6.1464F, -0.575F, -4.0331F, 3.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.625F, -16.3673F, -2.3538F, 0.0F, -1.5708F, 0.0F));

        PartDefinition braziers = partdefinition.addOrReplaceChild("braziers", CubeListBuilder.create().texOffs(126, 65).addBox(16.1274F, -5.0F, -13.7274F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 43).addBox(-19.3726F, -0.5F, -13.7274F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 131).addBox(16.1274F, -5.0F, 8.7726F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(126, 54).addBox(-19.3726F, -0.5F, 8.7726F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1524F, 5.8611F, 2.3774F, 0.0F, -1.5708F, 0.0F));

        PartDefinition tendrils = partdefinition.addOrReplaceChild("tendrils", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.2F, 14.8611F, 2.7F, 0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        sarcophagus_base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        braziers.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        tendrils.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}