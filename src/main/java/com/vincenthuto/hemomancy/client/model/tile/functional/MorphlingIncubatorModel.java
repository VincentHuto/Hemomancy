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

public class MorphlingIncubatorModel extends Model {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("morphling_incubator_model"), "main");
    private final ModelPart morphling_incubator;
    private final ModelPart Base;
    private final ModelPart Tank;
    private final ModelPart SideVials;
    private final ModelPart FloorVials;
    private final ModelPart Creature;

    public MorphlingIncubatorModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.morphling_incubator = root.getChild("morphling_incubator");
        this.Base = this.morphling_incubator.getChild("Base");
        this.Tank = this.morphling_incubator.getChild("Tank");
        this.SideVials = this.morphling_incubator.getChild("SideVials");
        this.FloorVials = this.morphling_incubator.getChild("FloorVials");
        this.Creature = this.morphling_incubator.getChild("Creature");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition morphling_incubator = partdefinition.addOrReplaceChild("morphling_incubator", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Base = morphling_incubator.addOrReplaceChild("Base", CubeListBuilder.create().texOffs(41, 0).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(41, 15).addBox(5.0F, -3.0F, -4.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(25, 43).addBox(-9.0F, -3.0F, -4.0F, 4.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(13, 56).addBox(5.2F, -5.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(13, 61).addBox(-8.2F, -5.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Tank = morphling_incubator.addOrReplaceChild("Tank", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -16.0F, -5.0F, 10.0F, 16.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(50, 38).addBox(5.1F, -16.0F, -2.0F, 2.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(25, 55).addBox(-7.1F, -16.0F, -2.0F, 2.0F, 16.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(-6.0F, -18.9F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(49, 27).addBox(-3.0F, -22.9F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(13, 66).addBox(5.0F, -17.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 58).addBox(-7.0F, -17.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

        PartDefinition SideVials = morphling_incubator.addOrReplaceChild("SideVials", CubeListBuilder.create().texOffs(0, 56).addBox(7.1F, -15.0F, -2.0F, 2.0F, 9.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(38, 59).addBox(-9.1F, -15.0F, -2.0F, 2.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition FloorVials = morphling_incubator.addOrReplaceChild("FloorVials", CubeListBuilder.create().texOffs(51, 59).addBox(3.0F, -5.0F, 5.1F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(63, 38).addBox(-7.0F, -5.0F, 5.1F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(63, 48).addBox(3.0F, -5.0F, -9.1F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(66, 15).addBox(-7.0F, -5.0F, -9.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Creature = morphling_incubator.addOrReplaceChild("Creature", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(68, 63).addBox(2.0F, -4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(68, 68).addBox(-4.0F, -4.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(51, 69).addBox(2.0F, 2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 70).addBox(-4.0F, 2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    /**
     * Animates the Creature part with a gentle bobbing motion.
     * @param ageInTicks game time + partial ticks
     */
    public void setupAnim(float ageInTicks) {
        this.Creature.y = -11.0F + (float) Math.sin(ageInTicks * 0.1) * 1.5F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        morphling_incubator.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    public void renderCreature(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        Creature.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }
}
