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
import net.minecraft.util.Mth;

public class MasonsEffigyModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("masons_effigy_model"), "main");

    private final ModelPart stand;
    private final ModelPart veins;
    private final ModelPart tablet4;
    private final ModelPart tablet1;
    private final ModelPart tablet2;
    private final ModelPart tablet3;
    private final ModelPart torso;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart halo;

    public MasonsEffigyModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.stand = root.getChild("stand");
        this.veins = this.stand.getChild("veins");
        this.tablet4 = this.stand.getChild("tablet4");
        this.tablet1 = this.stand.getChild("tablet1");
        this.tablet2 = this.stand.getChild("tablet2");
        this.tablet3 = this.stand.getChild("tablet3");
        this.torso = root.getChild("torso");
        this.neck = root.getChild("neck");
        this.head = root.getChild("head");
        this.halo = root.getChild("halo");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition stand = partdefinition.addOrReplaceChild("stand", CubeListBuilder.create().texOffs(0, 43).addBox(-5.5F, -5.0F, -5.5F, 11.0F, 3.5F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(0, 30).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 15).addBox(-6.5F, -6.5F, -6.5F, 13.0F, 1.5F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(25, 108).addBox(-4.5F, -7.0F, -4.5F, 9.0F, 0.5F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.5F, -1.5F, -6.5F, 13.0F, 1.5F, 13.0F, new CubeDeformation(0.0F))
                .texOffs(85, 16).addBox(-0.6F, -7.4F, -6.3F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition finial_7_r1 = stand.addOrReplaceChild("finial_7_r1", CubeListBuilder.create().texOffs(50, 70).addBox(-0.6F, -0.9F, -0.6F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.384F, -6.5F, -4.384F, 0.0F, -5.4978F, 0.0F));

        PartDefinition finial_6_r1 = stand.addOrReplaceChild("finial_6_r1", CubeListBuilder.create().texOffs(46, 70).addBox(-0.6F, -0.9F, -0.6F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.7F, -6.5F, 0.0F, 0.0F, -4.7124F, 0.0F));

        PartDefinition finial_5_r1 = stand.addOrReplaceChild("finial_5_r1", CubeListBuilder.create().texOffs(34, 88).addBox(-0.6F, -0.9F, -0.6F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.384F, -6.5F, 4.384F, 0.0F, -3.927F, 0.0F));

        PartDefinition finial_4_r1 = stand.addOrReplaceChild("finial_4_r1", CubeListBuilder.create().texOffs(38, 70).addBox(-0.6F, -0.9F, -0.6F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.5F, 5.7F, 0.0F, -3.1416F, 0.0F));

        PartDefinition finial_3_r1 = stand.addOrReplaceChild("finial_3_r1", CubeListBuilder.create().texOffs(34, 70).addBox(-0.6F, -0.9F, -0.6F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.384F, -6.5F, 4.384F, 0.0F, -2.3562F, 0.0F));

        PartDefinition finial_2_r1 = stand.addOrReplaceChild("finial_2_r1", CubeListBuilder.create().texOffs(42, 70).addBox(-0.6F, -0.9F, -0.6F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.7F, -6.5F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition finial_1_r1 = stand.addOrReplaceChild("finial_1_r1", CubeListBuilder.create().texOffs(87, 89).addBox(-0.6F, -0.9F, -0.6F, 1.2F, 0.9F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.384F, -6.5F, -4.384F, 0.0F, -0.7854F, 0.0F));

        PartDefinition veins = stand.addOrReplaceChild("veins", CubeListBuilder.create().texOffs(22, 69).addBox(-0.5F, -4.5F, 3.1F, 1.0F, 4.5F, 0.5F, new CubeDeformation(0.0F))
                .texOffs(96, 108).addBox(-0.2F, -4.7F, 3.1F, 3.6F, 0.4F, 0.4F, new CubeDeformation(0.0F))
                .texOffs(0, 68).addBox(-0.4F, -5.8F, 3.1F, 0.8F, 1.5F, 2.5F, new CubeDeformation(0.0F))
                .texOffs(62, 68).addBox(-0.4F, -8.1F, 4.3F, 0.8F, 2.4F, 0.3F, new CubeDeformation(0.0F))
                .texOffs(64, 67).addBox(-0.4F, -14.65F, 2.4F, 0.8F, 7.2F, 1.3F, new CubeDeformation(0.0F))
                .texOffs(102, 43).addBox(-1.7F, -12.4F, 2.4F, 3.4F, 0.4F, 0.3F, new CubeDeformation(0.0F))
                .texOffs(100, 107).addBox(-3.4F, -4.7F, 3.1F, 3.6F, 0.4F, 0.4F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.5F, -6.2F));

        PartDefinition v_brL_up_r1 = veins.addOrReplaceChild("v_brL_up_r1", CubeListBuilder.create().texOffs(42, 58).addBox(-0.2F, -1.8F, -0.2F, 0.4F, 2.4F, 1.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.7073F, -4.8528F, 3.3F, 0.0F, 0.0F, -0.829F));

        PartDefinition v_neck_r1 = veins.addOrReplaceChild("v_neck_r1", CubeListBuilder.create().texOffs(8, 70).addBox(-0.4F, -1.2F, -0.15F, 0.8F, 1.9F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.6854F, 4.0617F, 2.3562F, 0.0F, 0.0F));

        PartDefinition v_tmpR_r1 = veins.addOrReplaceChild("v_tmpR_r1", CubeListBuilder.create().texOffs(111, 28).addBox(-0.2F, -2.25F, -0.15F, 0.3F, 4.25F, 0.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -12.0F, 2.75F, 0.0F, 0.0F, 0.733F));

        PartDefinition v_tmpL_r1 = veins.addOrReplaceChild("v_tmpL_r1", CubeListBuilder.create().texOffs(115, 69).addBox(-0.1F, -2.25F, -0.15F, 0.3F, 4.25F, 0.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -12.0F, 2.75F, 0.0F, 0.0F, -0.733F));

        PartDefinition v_neck_r2 = veins.addOrReplaceChild("v_neck_r2", CubeListBuilder.create().texOffs(26, 69).addBox(-0.4F, -1.2F, -0.15F, 0.8F, 2.4F, 1.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.1805F, 3.5789F, 1.2217F, 0.0F, 0.0F));

        PartDefinition v_brR_up_r1 = veins.addOrReplaceChild("v_brR_up_r1", CubeListBuilder.create().texOffs(32, 65).addBox(-0.2F, -1.8F, -0.2F, 0.4F, 2.4F, 1.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.7073F, -4.8528F, 3.3F, 0.0F, 0.0F, 0.829F));

        PartDefinition v_brR_lo_r1 = veins.addOrReplaceChild("v_brR_lo_r1", CubeListBuilder.create().texOffs(74, 109).addBox(-0.2F, -1.3F, -0.2F, 0.4F, 3.3F, 0.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8F, -3.6F, 3.3F, 0.0F, 0.0F, -0.8029F));

        PartDefinition v_brL_lo_r1 = veins.addOrReplaceChild("v_brL_lo_r1", CubeListBuilder.create().texOffs(118, 112).addBox(-0.2F, -1.3F, -0.2F, 0.4F, 3.3F, 0.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8F, -3.6F, 3.3F, 0.0F, 0.0F, 0.8029F));

        PartDefinition v_brL_lo_r2 = veins.addOrReplaceChild("v_brL_lo_r2", CubeListBuilder.create().texOffs(99, 28).addBox(-0.2F, -2.8F, -0.2F, 0.4F, 4.8F, 0.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -2.6F, 3.3F, 0.0F, 0.0F, 0.8029F));

        PartDefinition v_brR_lo_r2 = veins.addOrReplaceChild("v_brR_lo_r2", CubeListBuilder.create().texOffs(99, 28).addBox(-0.2F, -2.8F, -0.2F, 0.4F, 4.8F, 0.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -2.6F, 3.3F, 0.0F, 0.0F, -0.8029F));

        PartDefinition tablet4 = stand.addOrReplaceChild("tablet4", CubeListBuilder.create(), PartPose.offset(-6.35F, -5.0F, 0.0F));

        PartDefinition tablet_0_r1 = tablet4.addOrReplaceChild("tablet_0_r1", CubeListBuilder.create().texOffs(90, 85).addBox(-1.1F, -1.4F, -0.25F, 2.2F, 2.8F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, 2.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition chain_0_r1 = tablet4.addOrReplaceChild("chain_0_r1", CubeListBuilder.create().texOffs(12, 92).addBox(-0.25F, -1.2F, -0.25F, 0.5F, 2.0F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition tablet1 = stand.addOrReplaceChild("tablet1", CubeListBuilder.create(), PartPose.offset(-4.175F, -6.1F, -6.591F));

        PartDefinition tablet_1_r1 = tablet1.addOrReplaceChild("tablet_1_r1", CubeListBuilder.create().texOffs(66, 81).addBox(-1.1F, -1.4F, -0.25F, 2.2F, 2.8F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.125F, 3.1F, -0.125F, 0.0F, 0.5236F, 0.0F));

        PartDefinition chain_1_r1 = tablet1.addOrReplaceChild("chain_1_r1", CubeListBuilder.create().texOffs(48, 90).addBox(-0.25F, -1.2F, -0.25F, 0.5F, 2.0F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.125F, 1.1F, 0.125F, 0.0F, 0.5236F, 0.0F));

        PartDefinition tablet2 = stand.addOrReplaceChild("tablet2", CubeListBuilder.create(), PartPose.offset(4.05F, -6.0F, -6.466F));

        PartDefinition tablet_2_r1 = tablet2.addOrReplaceChild("tablet_2_r1", CubeListBuilder.create().texOffs(96, 61).addBox(-1.1F, -1.4F, -0.25F, 2.2F, 2.8F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 3.0F, -0.25F, 0.0F, -0.5236F, 0.0F));

        PartDefinition chain_2_r1 = tablet2.addOrReplaceChild("chain_2_r1", CubeListBuilder.create().texOffs(42, 98).addBox(-0.25F, -1.2F, -0.25F, 0.5F, 2.0F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, -0.5236F, 0.0F));

        PartDefinition tablet3 = stand.addOrReplaceChild("tablet3", CubeListBuilder.create(), PartPose.offset(6.475F, -6.1F, 0.0F));

        PartDefinition chain_3_r1 = tablet3.addOrReplaceChild("chain_3_r1", CubeListBuilder.create().texOffs(69, 101).addBox(-0.25F, -1.2F, -0.25F, 0.5F, 2.0F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.125F, 1.1F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition tablet_3_r1 = tablet3.addOrReplaceChild("tablet_3_r1", CubeListBuilder.create().texOffs(95, 74).addBox(-1.1F, -1.4F, -0.25F, 2.2F, 2.8F, 0.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.125F, 3.1F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition torso = partdefinition.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(52, 23).addBox(-3.75F, 1.0F, -2.3F, 7.5F, 2.0F, 4.3F, new CubeDeformation(0.0F))
                .texOffs(52, 7).addBox(-4.5F, -1.0F, -2.7F, 9.0F, 2.0F, 5.2F, new CubeDeformation(0.0F))
                .texOffs(52, 0).addBox(-5.8F, -3.2F, -3.0F, 11.6F, 2.2F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 15.0F, 0.0F));

        PartDefinition delt_L_r1 = torso.addOrReplaceChild("delt_L_r1", CubeListBuilder.create().texOffs(108, 91).addBox(-3.2F, 0.0F, -2.4F, 3.2F, 1.6F, 4.8F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -3.2F, 0.0F, 0.0F, 0.0F, 0.4189F));

        PartDefinition delt_R_r1 = torso.addOrReplaceChild("delt_R_r1", CubeListBuilder.create().texOffs(18, 58).addBox(0.0F, 0.0F, -2.4F, 3.2F, 1.6F, 4.8F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -3.2F, 0.0F, 0.0F, 0.0F, -0.4189F));

        PartDefinition neck = partdefinition.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(50, 61).addBox(-1.9F, -2.3F, -1.8F, 3.7F, 3.6F, 3.2F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.5F, 0.0F));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(102, 50).addBox(-2.9F, 3.5F, -3.4F, 5.6F, 1.6F, 5.6F, new CubeDeformation(0.0F))
                .texOffs(48, 30).addBox(-4.2F, -2.2F, -3.4F, 8.2F, 5.8F, 7.3F, new CubeDeformation(0.0F))
                .texOffs(0, 58).addBox(-4.2F, -2.2F, 3.6F, 8.2F, 5.2F, 0.7F, new CubeDeformation(0.0F))
                .texOffs(52, 15).addBox(-3.0F, -3.6F, -2.6F, 6.0F, 1.4F, 5.7F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, 0.0F));

        PartDefinition halo = partdefinition.addOrReplaceChild("halo", CubeListBuilder.create(), PartPose.offset(0.0F, 3.7438F, 6.0F));

        PartDefinition spike_7_r1 = halo.addOrReplaceChild("spike_7_r1", CubeListBuilder.create().texOffs(68, 55).addBox(8.565F, -0.4146F, -0.45F, 3.0F, 0.9F, 0.9F, new CubeDeformation(0.0F))
                .texOffs(104, 8).addBox(-3.7304F, 7.7509F, -0.6F, 7.389F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -7.0686F));

        PartDefinition spike_6_r1 = halo.addOrReplaceChild("spike_6_r1", CubeListBuilder.create().texOffs(53, 105).addBox(8.6F, -0.4F, -0.45F, 3.0F, 0.9F, 0.9F, new CubeDeformation(0.0F))
                .texOffs(38, 85).addBox(-3.694F, 7.765F, -0.6F, 7.388F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -6.2832F));

        PartDefinition spike_5_r1 = halo.addOrReplaceChild("spike_5_r1", CubeListBuilder.create().texOffs(8, 68).addBox(8.6357F, -0.4146F, -0.45F, 3.0F, 0.9F, 0.9F, new CubeDeformation(0.0F))
                .texOffs(64, 61).addBox(-3.6586F, 7.7509F, -0.6F, 7.389F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -5.4978F));

        PartDefinition spike_4_r1 = halo.addOrReplaceChild("spike_4_r1", CubeListBuilder.create().texOffs(48, 68).addBox(8.65F, -0.45F, -0.45F, 3.0F, 0.9F, 0.9F, new CubeDeformation(0.0F))
                .texOffs(78, 96).addBox(-3.644F, 7.715F, -0.6F, 7.388F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -4.7124F));

        PartDefinition spike_3_r1 = halo.addOrReplaceChild("spike_3_r1", CubeListBuilder.create().texOffs(40, 68).addBox(8.6357F, -0.4854F, -0.45F, 3.0F, 0.9F, 0.9F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-3.6586F, 7.6802F, -0.6F, 7.389F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -3.927F));

        PartDefinition spike_2_r1 = halo.addOrReplaceChild("spike_2_r1", CubeListBuilder.create().texOffs(17, 120).addBox(8.6F, -0.5F, -0.45F, 3.0F, 0.9F, 0.9F, new CubeDeformation(0.0F))
                .texOffs(88, 115).addBox(-3.694F, 7.665F, -0.6F, 7.388F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -3.1416F));

        PartDefinition spike_1_r1 = halo.addOrReplaceChild("spike_1_r1", CubeListBuilder.create().texOffs(66, 115).addBox(8.565F, -0.4854F, -0.45F, 3.0F, 0.9F, 0.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -2.3562F));

        PartDefinition spike_0_r1 = halo.addOrReplaceChild("spike_0_r1", CubeListBuilder.create().texOffs(9, 82).addBox(8.55F, -0.45F, -0.45F, 3.2F, 0.9F, 0.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -1.5708F));

        PartDefinition ring_7_r1 = halo.addOrReplaceChild("ring_7_r1", CubeListBuilder.create().texOffs(64, 63).addBox(-3.7304F, 7.6802F, -0.6F, 7.389F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -8.6394F));

        PartDefinition ring_6_r1 = halo.addOrReplaceChild("ring_6_r1", CubeListBuilder.create().texOffs(5, 105).addBox(-3.744F, 7.715F, -0.6F, 7.388F, 1.2F, 1.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.0438F, 0.0F, 0.0F, 0.0F, -7.854F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


    public void setupAnim(float ageInTicks) {
        // slow halo rotation
        float rotation = ageInTicks * 0.045F;
        this.halo.xRot = 0;
        this.halo.yRot = 0;
        this.halo.zRot = rotation;

        // reset tablet base rotations then apply gentle sway
        // sway parameters
        float swaySpeed = 0.12F; // how fast the tablets sway
        float swayAmp = 0.08F;   // angular amplitude in radians (~4.6 degrees)
        float tiltAmp = 0.02F;   // small tilt in x axis for a more natural motion

        // phase-offset each tablet so they don't all move identically
        this.tablet1.zRot = Mth.sin(ageInTicks * swaySpeed + 0.0F) * swayAmp;
        this.tablet1.xRot = Mth.sin(ageInTicks * swaySpeed + 0.5F) * tiltAmp;

        this.tablet2.zRot = Mth.sin(ageInTicks * swaySpeed + 1.2F) * swayAmp;
        this.tablet2.xRot = Mth.sin(ageInTicks * swaySpeed + 1.7F) * tiltAmp;

        this.tablet3.zRot = Mth.sin(ageInTicks * swaySpeed + 2.4F) * swayAmp;
        this.tablet3.xRot = Mth.sin(ageInTicks * swaySpeed + 2.9F) * tiltAmp;

        this.tablet4.zRot = Mth.sin(ageInTicks * swaySpeed + 3.6F) * swayAmp;
        this.tablet4.xRot = Mth.sin(ageInTicks * swaySpeed + 4.1F) * tiltAmp;

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               int packedColor) {
        stand.render(poseStack, vertexConsumer,   packedLight, packedOverlay,packedColor);
        torso.render(poseStack, vertexConsumer,  packedLight, packedOverlay,packedColor);
        neck.render(poseStack, vertexConsumer,  packedLight, packedOverlay,packedColor);
        head.render(poseStack, vertexConsumer,  packedLight, packedOverlay,packedColor);
        halo.render(poseStack, vertexConsumer,  packedLight, packedOverlay,packedColor);

    }
}
