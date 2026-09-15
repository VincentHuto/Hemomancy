package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.BombardierState;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.PhlegethonticBombardier;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public final class PhlegethonticBombardierModel extends HierarchicalModel<PhlegethonticBombardier> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("hemomancy", "phlegethontic_bombardier"), "main");
    private final ModelPart whole;
    private final ModelPart nozzle;

    public PhlegethonticBombardierModel(ModelPart root) {
        whole = root.getChild("whole");
        nozzle = whole.getChild("body").getChild("tail").getChild("tail2")
                .getChild("tail3").getChild("tail4");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition whole = root.addOrReplaceChild("whole", CubeListBuilder.create(),
                PartPose.offset(0.0F, 21.1238F, -4.9992F));

        PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 55).addBox(-1.0F, -0.7476F, -4.5016F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(54, 53).addBox(-2.0F, -0.7476F, -3.5016F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 45).addBox(-3.0F, -1.7476F, -1.5016F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 58).addBox(-1.0F, -1.9976F, -1.5016F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.8762F, -2.9992F));
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(56, 36)
                .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 0.2182F, -3.9787F, 0.3491F, 0.0F, 0.0F));
        head.addOrReplaceChild("antennaL", CubeListBuilder.create().texOffs(42, 51)
                .addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, 1.0024F, -2.5016F, 0.0F, -0.6109F, 0.0F));
        head.addOrReplaceChild("antennaR", CubeListBuilder.create().texOffs(54, 47)
                .addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, 1.0024F, -2.5016F, 0.0F, 0.6109F, 0.0F));
        PartDefinition eyeL = head.addOrReplaceChild("eyeL", CubeListBuilder.create(),
                PartPose.offset(3.5F, 1.2524F, -1.2516F));
        eyeL.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(44, 66)
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -0.75F, -1.0F, 0.0F, 0.4363F, 0.0F));
        PartDefinition eyeL2 = head.addOrReplaceChild("eyeL2", CubeListBuilder.create(),
                PartPose.offset(-3.5F, 1.2524F, -1.2516F));
        eyeL2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(44, 66).mirror()
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(1.5F, -0.75F, -1.0F, 0.0F, -0.4363F, 0.0F));

        PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(34, 47).addBox(-4.0F, -5.0F, -6.0F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 1.8762F, 2.9992F));
        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(36, 32).addBox(-3.5F, -2.5F, 1.0F, 7.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-4.0F, -0.5F, 1.0F, 8.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -3.5F, 2.0F));
        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(42, 40).addBox(-2.5F, -1.75F, 0.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(22, 40).addBox(-3.5F, -0.75F, 0.0F, 7.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 4.0F));
        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-3.0F, -1.0F, 0.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 3.0F));
        tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(30, 51).addBox(-1.0F, -3.25F, -0.75F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(14, 51).addBox(-2.0F, -2.25F, -0.75F, 5.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(42, 57).addBox(-0.5F, -2.25F, 1.25F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(32, 58).addBox(0.75F, -2.75F, 1.75F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(58, 40).addBox(-1.25F, -2.75F, 1.75F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(10, 56).addBox(-1.25F, -0.75F, 1.75F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 32).addBox(-1.25F, -2.75F, 1.75F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-0.5F, 1.0F, 3.75F));

        body.addOrReplaceChild("wingL", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.0F, -0.5F, 0.5F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(22, 38).addBox(-2.0F, -0.25F, 12.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 8).addBox(3.0F, -0.25F, 1.5F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(10, 64).addBox(-2.0F, -0.25F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, -6.0F, -5.25F, 0.0F, 0.0F, 0.2618F));
        body.addOrReplaceChild("wingR", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-3.0F, -0.5F, 0.5F, 6.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(60, 26).addBox(-3.0F, -0.25F, 12.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 20).addBox(-4.0F, -0.25F, 1.5F, 1.0F, 1.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(8, 66).addBox(-2.0F, -0.25F, -0.5F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, -6.0F, -5.25F, 0.0F, 0.0F, -0.2618F));

        PartDefinition lLegF = body.addOrReplaceChild("lLegF", CubeListBuilder.create().texOffs(20, 64)
                .addBox(-1.5F, -1.5F, -0.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -3.0F, -6.0F, 0.0F, 0.6109F, 0.0F));
        PartDefinition flLeg2 = lLegF.addOrReplaceChild("flLeg2", CubeListBuilder.create()
                .texOffs(52, 58).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 45).addBox(-1.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -0.5F, 0.75F, 0.0F, 0.0F, 0.5672F));
        flLeg2.addOrReplaceChild("flLeg3", CubeListBuilder.create()
                .texOffs(64, 4).addBox(1.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 8).addBox(0.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
        PartDefinition rLegF = body.addOrReplaceChild("rLegF", CubeListBuilder.create().texOffs(36, 66)
                .addBox(-0.5F, -1.5F, -0.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, -3.0F, -6.0F, 0.0F, -0.6109F, 0.0F));
        PartDefinition flLeg8 = rLegF.addOrReplaceChild("flLeg8", CubeListBuilder.create()
                .texOffs(60, 28).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 46).addBox(-2.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, -0.5F, 0.75F, 0.0F, 0.0F, -0.5672F));
        flLeg8.addOrReplaceChild("flLeg9", CubeListBuilder.create()
                .texOffs(0, 61).addBox(-4.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 62).addBox(-5.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        PartDefinition lLegM = body.addOrReplaceChild("lLegM", CubeListBuilder.create().texOffs(28, 64)
                .addBox(-1.5F, -1.5F, -0.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, -3.0F, -1.0F));
        PartDefinition flLeg4 = lLegM.addOrReplaceChild("flLeg4", CubeListBuilder.create()
                .texOffs(10, 60).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 56).addBox(-1.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -0.5F, 0.75F, 0.0F, 0.0F, 0.3054F));
        flLeg4.addOrReplaceChild("flLeg5", CubeListBuilder.create()
                .texOffs(60, 10).addBox(1.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 14).addBox(0.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5672F));
        PartDefinition rLegM = body.addOrReplaceChild("rLegM", CubeListBuilder.create().texOffs(66, 36)
                .addBox(-0.5F, -1.5F, -0.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, -3.0F, -1.0F));
        PartDefinition flLeg10 = rLegM.addOrReplaceChild("flLeg10", CubeListBuilder.create()
                .texOffs(40, 62).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 48).addBox(-2.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, -0.5F, 0.75F, 0.0F, 0.0F, -0.3054F));
        flLeg10.addOrReplaceChild("flLeg11", CubeListBuilder.create()
                .texOffs(50, 62).addBox(-4.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 58).addBox(-5.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5672F));

        PartDefinition lLegB = body.addOrReplaceChild("lLegB", CubeListBuilder.create().texOffs(0, 65)
                .addBox(-1.5F, -1.5F, -0.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(5.0F, -3.0F, 3.0F, 0.0F, -0.6981F, 0.0F));
        PartDefinition flLeg6 = lLegB.addOrReplaceChild("flLeg6", CubeListBuilder.create()
                .texOffs(60, 16).addBox(0.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(66, 44).addBox(-1.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.5F, -0.5F, 0.75F, 0.0F, 0.0F, 0.3054F));
        flLeg6.addOrReplaceChild("flLeg7", CubeListBuilder.create()
                .texOffs(60, 20).addBox(1.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(60, 24).addBox(0.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5672F));
        PartDefinition rLegB = body.addOrReplaceChild("rLegB", CubeListBuilder.create().texOffs(66, 40)
                .addBox(-0.5F, -1.5F, -0.25F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.0F, -3.0F, 3.0F, 0.0F, 0.6981F, 0.0F));
        PartDefinition flLeg12 = rLegB.addOrReplaceChild("flLeg12", CubeListBuilder.create()
                .texOffs(60, 62).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(50, 66).addBox(-2.0F, -0.5F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-0.5F, -0.5F, 0.75F, 0.0F, 0.0F, -0.3054F));
        flLeg12.addOrReplaceChild("flLeg13", CubeListBuilder.create()
                .texOffs(64, 0).addBox(-4.0F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(62, 60).addBox(-5.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-2.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5672F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return whole;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        whole.render(pose, consumer, light, overlay, color);
    }

    @Override
    public void setupAnim(PhlegethonticBombardier entity, float swing, float swingAmount,
                          float age, float yaw, float pitch) {
        whole.getAllParts().forEach(ModelPart::resetPose);
        AnimationDefinition animation = entity.deathTime > 0 ? PhlegethonticBombardierAnimations.DEATH
                : entity.hurtTime > 0 ? PhlegethonticBombardierAnimations.HURT : switch (entity.getBombardierState()) {
                    case IDLE -> PhlegethonticBombardierAnimations.IDLE;
                    case CAMOUFLAGED -> PhlegethonticBombardierAnimations.FLATTEN_REST;
                    case WARNING -> PhlegethonticBombardierAnimations.WAKE_WARNING;
                    case GRAZING -> PhlegethonticBombardierAnimations.GRAZE;
                    case WINDUP -> PhlegethonticBombardierAnimations.ABDOMEN_WINDUP;
                    case FIRING -> PhlegethonticBombardierAnimations.ABDOMEN_FIRE;
                    case COOLING -> PhlegethonticBombardierAnimations.VENT_COOLDOWN;
                };
        animate(entity.presentationAnimationState, animation, age);
        if (swingAmount > .02F)
            animateWalk(PhlegethonticBombardierAnimations.WALK, swing, swingAmount, 2, 2.5F);
        if (entity.getBombardierState() == BombardierState.FIRING
                || entity.getBombardierState() == BombardierState.WINDUP
                && entity.getStateTick() >= PhlegethonticBombardier.AIM_TRACKING_TICKS) {
            nozzle.yRot += (float) Math.toRadians(entity.getLockedYaw() - entity.yBodyRot);
            nozzle.xRot -= (float) Math.toRadians(entity.getLockedPitch());
        }
    }
}
