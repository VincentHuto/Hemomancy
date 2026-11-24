package com.vincenthuto.hemomancy.client.model.entity.boss.phlegethosoma;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaBreachRules;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaEntity.Action;
import com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma.PhlegethosomaSwimmingRules;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

import java.util.List;

/** Animates the articulated sucker, muscular body, mantle and ribbon tail. */
public final class PhlegethosomaModel extends EntityModel<PhlegethosomaEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("phlegethosoma"), "main");

    private final ModelPart root;
    private final List<ModelPart> parts;
    private final ModelPart head;
    private final ModelPart sucker;
    private final ModelPart upperLip;
    private final ModelPart lowerLip;
    private final ModelPart bloodSac;
    private final ModelPart[] joints = new ModelPart[PhlegethosomaSwimmingRules.SEGMENTS];
    private final ModelPart[] leftFrills = new ModelPart[9];
    private final ModelPart[] rightFrills = new ModelPart[9];

    public PhlegethosomaModel(ModelPart baked) {
        root = baked.getChild("arterial_fluke");
        parts = root.getAllParts().toList();
        head = root.getChild("head");
        sucker = head.getChild("oral_sucker");
        upperLip = sucker.getChild("upper_lip");
        lowerLip = sucker.getChild("lower_lip");
        joints[0] = root.getChild("forebody");
        joints[1] = joints[0].getChild("crop");
        bloodSac = joints[1].getChild("blood_sac");
        for (int i = 1; i <= 8; i++) {
            joints[i + 1] = joints[i].getChild("swim_segment_0" + i);
        }
        joints[10] = joints[9].getChild("ribbon_tail");
        joints[11] = joints[10].getChild("ribbon_tip");
        for (int i = 0; i < leftFrills.length; i++) {
            leftFrills[i] = joints[i + 1].getChild("left_frill");
            rightFrills[i] = joints[i + 1].getChild("right_frill");
        }
    }

    public static LayerDefinition createBodyLayer() {
        return PhlegethosomaMesh.createLayer();
    }

    @Override
    public void setupAnim(PhlegethosomaEntity entity, float limbSwing, float limbAmount, float age,
                          float yaw, float pitch) {
        parts.forEach(ModelPart::resetPose);
        float partial = age - entity.tickCount;
        float time = entity.animationTime(partial);
        Action action = entity.action();
        boolean swimming = entity.isIchorSwimming();
        boolean swimmingPose = swimming || entity.isBreachAttack();
        boolean ruptured = entity.phase() == 3;
        float movement = swimmingPose ? Mth.clamp(entity.swimAmount(), 0, 1) : Mth.clamp(limbAmount * 2, 0, .6F);
        float followThrough = 1 - ease(entity.deathTime / 20F);
        float thrust = switch (action) {
            case LUNGE -> pulse(time, 14, 20, 28, 39);
            case BREACH -> 1 - ease(time / PhlegethosomaBreachRules.TIMELINE.active());
            default -> 0;
        };

        root.xRot = swimmingPose ? entity.swimPitch() : 0;
        if (swimming) {
            root.y += Mth.sin(age * .17F) * .4F * followThrough;
        }
        boolean tracking = action == Action.DORMANT || action == Action.SEEKING || action == Action.RECOVERY;
        float headAim = Mth.clamp(yaw * Mth.DEG_TO_RAD, -.28F, .28F);
        head.yRot = tracking ? headAim + head.yRot * .65F * (1 - movement) : 0;
        head.xRot *= tracking ? 1 - movement : 0;
        head.xRot += Mth.sin(entity.hurtTime * .6F) * .1F;
        if (entity.introTime() >= 0) {
            float rise = Mth.clamp((entity.introTime() + partial) / PhlegethosomaCombatRules.INTRO_TICKS, 0, 1);
            head.xRot -= Mth.sin(rise * Mth.PI) * .32F;
            joints[0].xRot -= Mth.sin(rise * Mth.PI) * .1F;
        }

        swim(age, movement, thrust, followThrough, ruptured);
        feed(action, time, age, entity.phase(), entity.swollen(), followThrough);
    }

    private void swim(float age, float movement, float thrust, float followThrough, boolean ruptured) {
        float restCurve = (1 - movement * .83F) * (1 - thrust * .96F);
        for (int i = 0; i < joints.length; i++) {
            ModelPart joint = joints[i];
            // Blend two fixed-frequency waves so changing swim speed cannot jump the phase.
            float idleWave = PhlegethosomaSwimmingRules.segmentYaw(i, age, 0);
            float swimWave = PhlegethosomaSwimmingRules.segmentYaw(i, age, 1);
            joint.yRot = joint.yRot * restCurve
                    + Mth.lerp(movement, idleWave, swimWave) * (1 - thrust * .8F) * followThrough;
            joint.xRot = joint.xRot * (1 - thrust)
                    + Mth.sin(age * .25F - i * .6F) * .035F * movement * followThrough;
        }
        for (int i = 0; i < leftFrills.length; i++) {
            float frequency = ruptured ? .52F : .3F;
            float flutter = (ruptured ? .17F : .05F) + movement * .11F;
            leftFrills[i].zRot = Mth.sin(age * frequency - i * .68F) * flutter * followThrough;
            rightFrills[i].zRot = -Mth.sin(age * frequency - i * .68F + .45F) * flutter * followThrough;
        }
    }

    private void feed(Action action, float time, float age, int phase, boolean swollen, float followThrough) {
        float swell = phase == 3 ? -.08F : swollen ? .17F : .02F;
        float pressure = Mth.sin(age * .18F) * (swollen ? .042F : .017F) * followThrough;
        float gape = 0;
        float reach = 0;

        switch (action) {
            case SUCTION_WINDUP -> {
                gape = .9F * ease(time / PhlegethosomaCombatRules.suction(phase).windup());
                pressure += gape * .07F;
            }
            case SUCTION_ACTIVE -> {
                int duration = PhlegethosomaCombatRules.suction(phase).active();
                gape = (.9F + Mth.sin(time * .5F) * .08F) * (1 - ease((time - duration + 8) / 8));
                reach = .8F * gape;
                pressure += gape * .08F;
            }
            case LATCH_ATTACK -> {
                float lunge = pulse(time, 3, 15, 21, 36);
                gape = .65F * pulse(time, 2, 11, 12, 15) - .6F * pulse(time, 12, 15, 20, 29);
                reach = lunge * 2.1F;
                head.xRot -= lunge * .08F;
            }
            case CLOT_SPIT -> {
                gape = .9F * pulse(time, 8, 20, 22, 33);
                pressure += .12F * pulse(time, 0, 16, 19, 22);
                reach = gape;
            }
            case ICHOR_SPRAY -> {
                gape = .75F * pulse(time, 12, 24, 36, 48);
                pressure += .13F * pulse(time, 0, 20, 23, 36) - .07F * pulse(time, 24, 36, 38, 50);
                reach = gape * .9F;
            }
            case DIVE -> gape = -.25F * ease(time / 12);
            case BREACH -> {
                gape = .85F * pulse(time, 0, 5, 13, PhlegethosomaBreachRules.TIMELINE.active());
                reach = gape * 1.2F;
            }
            case LUNGE -> gape = .7F * pulse(time, 12, 20, 28, 36);
            case STAGGERED -> {
                head.zRot += Mth.sin(time * .8F) * .18F * (1 - ease(time / PhlegethosomaCombatRules.STAGGER_TICKS));
                gape = -.25F * pulse(time, 0, 6, 12, 24);
            }
            default -> { }
        }

        bloodSac.xScale = 1 + (swell + pressure) * .65F;
        bloodSac.yScale = 1 + swell + pressure;
        sucker.z -= reach;
        sucker.xScale = 1 + gape * .13F;
        sucker.yScale = 1 + gape * .14F;
        upperLip.y -= gape * .42F;
        lowerLip.y += gape * .55F;
    }

    private static float pulse(float time, float start, float peak, float hold, float end) {
        return ease((time - start) / (peak - start)) * (1 - ease((time - hold) / (end - hold)));
    }

    private static float ease(float progress) {
        float t = Mth.clamp(progress, 0, 1);
        return t * t * (3 - 2 * t);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) {
        root.render(pose, vertices, light, overlay, color);
    }
}
