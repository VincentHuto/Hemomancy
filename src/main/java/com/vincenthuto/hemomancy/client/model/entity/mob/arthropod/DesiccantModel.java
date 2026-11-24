package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.DesiccantEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

public class DesiccantModel extends HierarchicalModel<DesiccantEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("desiccant"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart[] leftLegs = new ModelPart[4];
    private final ModelPart[] rightLegs = new ModelPart[4];
    private final ModelPart leftPalp;
    private final ModelPart rightPalp;
    private final ModelPart leftFinger;
    private final ModelPart rightFinger;
    private final ModelPart[] tail = new ModelPart[5];
    private final ModelPart telson;
    private final ModelPart venomBulb;

    public DesiccantModel(ModelPart root) {
        this.root = root;
        body = root.getChild("desiccant");
        head = body.getChild("carapace");
        for (int i = 0; i < leftLegs.length; i++) {
            leftLegs[i] = body.getChild("left_leg_" + (i + 1));
            rightLegs[i] = body.getChild("right_leg_" + (i + 1));
        }
        leftPalp = body.getChild("left_pedipalp");
        rightPalp = body.getChild("right_pedipalp");
        leftFinger = leftPalp.getChild("chela").getChild("movable_finger");
        rightFinger = rightPalp.getChild("chela").getChild("movable_finger");
        ModelPart segment = body.getChild("metasoma");
        for (int i = 0; i < tail.length; i++) {
            segment = segment.getChild("tail_articulation_" + (i + 1));
            tail[i] = segment;
        }
        telson = segment.getChild("telson");
        venomBulb = telson.getChild("venom_bulb");
    }

    public static LayerDefinition createBodyLayer() {
        return DesiccantMesh.createBodyLayer();
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(DesiccantEntity entity, float swing, float swingAmount, float age, float yaw, float pitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0.0F, 1.0F);
        float stingProgress = entity.isStinging()
                ? entity.getStingProgress() + partialTick / DesiccantEntity.STING_ANIMATION_TICKS : 0.0F;
        pose(swing, swingAmount, age, yaw, pitch, stingProgress);
    }

    void pose(float swing, float swingAmount, float age, float yaw, float pitch, float stingProgress) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float gait = Mth.clamp(swingAmount * 1.6F, 0.0F, 1.0F);
        float step = swing * 1.25F;
        float idle = age * 0.12F;
        float windup = Mth.sin(Mth.clamp(stingProgress / 0.18F, 0.0F, 1.0F) * Mth.PI);
        float strike = Mth.sin(Mth.clamp((stingProgress - 0.11F) / 0.49F, 0.0F, 1.0F) * Mth.PI);
        float swell = Mth.sin(Mth.clamp(stingProgress, 0.0F, 1.0F) * Mth.PI);

        body.y += (0.22F + 0.18F * Mth.cos(step * 2.0F)) * gait;
        head.yRot = Mth.clamp(yaw * Mth.DEG_TO_RAD, -0.11F, 0.11F);
        head.xRot = Mth.clamp(pitch * Mth.DEG_TO_RAD, -0.08F, 0.08F);
        for (int i = 0; i < leftLegs.length; i++) {
            float phase = step + i * Mth.PI;
            stepLeg(leftLegs[i], phase, gait, -1);
            stepLeg(rightLegs[i], phase + Mth.PI, gait, 1);
        }

        poseClaw(leftPalp, leftFinger, idle, step, gait, strike, -1);
        poseClaw(rightPalp, rightFinger, idle + 1.3F, step + Mth.PI, gait, strike, 1);

        for (int i = 0; i < tail.length; i++) {
            float wave = idle - i * 0.52F;
            tail[i].yRot = Mth.sin(wave) * (0.038F + gait * 0.025F) * (1.0F - strike);
            tail[i].xRot = Mth.cos(wave) * 0.025F + strike * (0.29F - i * 0.035F) - windup * 0.09F;
        }
        telson.xRot = strike * 0.21F;
        venomBulb.xScale = 1.0F + swell * 0.33F;
        venomBulb.yScale = 1.0F + swell * 0.24F;
        venomBulb.zScale = 1.0F + swell * 0.17F;
    }

    private static void stepLeg(ModelPart leg, float phase, float gait, int side) {
        leg.yRot = Mth.cos(phase) * gait * side * 0.18F;
        leg.zRot = -Math.max(0.0F, Mth.cos(phase)) * gait * side * 0.16F;
    }

    private static void poseClaw(ModelPart palp, ModelPart finger, float idle, float step,
                                 float gait, float strike, int side) {
        palp.xRot = Mth.sin(idle) * 0.045F + Mth.cos(step) * gait * 0.07F + strike * 0.12F;
        palp.yRot = side * (Mth.sin(idle + 0.8F) * 0.045F + strike * 0.10F);
        finger.yRot = -side * (0.035F * Mth.sin(idle + 0.6F) + 0.23F * strike);
    }
}
