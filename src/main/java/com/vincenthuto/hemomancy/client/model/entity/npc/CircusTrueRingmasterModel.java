package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.Move;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Shared joint animation for the Ringmaster and the form he reveals in the Chamber. */
public class CircusTrueRingmasterModel extends EntityModel<CircusRingmasterEntity> {
    /** The staff's own turn on the fist through a blow: the topper leads, behind the head raised, ahead of him landed. */
    private static final float STAFF_BLOW = 1.1F;
    /** How far the kneel lowers his hips, in pixels: the near thigh upright, its shin along the floor. */
    private static final float KNEEL_DROP = 8.5F;
    private final ModelPart root, pelvis, chest, head, staff, topper, core;
    private final ModelPart[] threads;
    private final boolean trueSelf;
    private final ModelPart[] uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2],
            shins = new ModelPart[2], feet = new ModelPart[2], tails = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];

    public CircusTrueRingmasterModel() {
        this(RingmasterMesh.trueSelf(), true);
    }

    protected CircusTrueRingmasterModel(ModelPart mesh, boolean trueSelf) {
        this.trueSelf = trueSelf;
        root = mesh;
        pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        head = chest.getChild("head");
        if (trueSelf) {
            ModelPart strings = chest.getChild("strings");
            threads = new ModelPart[] {strings.getChild("thread_1"), strings.getChild("thread_2"),
                    strings.getChild("thread_3")};
        } else {
            threads = new ModelPart[0];
        }
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild("thumb");
            thumbTips[i] = thumbs[i].getChild("tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild("finger_" + f);
                tips[i][f] = fingers[i][f].getChild("tip");
            }
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
            tails[i] = pelvis.getChild(side + "_tail");
        }
        staff = hands[0].getChild("staff");
        topper = staff.getChild("topper");
        core = topper.getChild("core");
    }

    @Override
    public void setupAnim(CircusRingmasterEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = AnimationTime.partialTick();
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.move(), entity.moveTime(partialTick),
                entity.phase(), entity.finalePhase(), entity.finalePhaseTime(partialTick), entity.hasTopper());
    }

    /**
     * The whole pose: {@code moveTime} is the ticks into the running move, {@code phaseTime} the ticks into the
     * finale's phase.
     */
    private void pose(float limbSwing, float movement, float age, float yaw, float pitch, Move move, float moveTime,
            CircusPavilionStateRules.Phase pavilionPhase, FinaleRules.FinalePhase phase, float phaseTime,
            boolean hasTopper) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        // Keep the base form at the original performer's scale. The reveal renderer supplies its own enlargement.
        if (!trueSelf) root.xScale = root.yScale = root.zScale = 0.66F;
        topper.visible = hasTopper;
        float breath = Mth.sin(age * .05F);
        // A looming stoop, the chest rising and falling, the head cocked a little too far and drifting back.
        chest.xRot += .07F + breath * .015F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        head.zRot += .08F + Mth.sin(age * .031F) * .05F;
        // The staff arm: forearm out before him, the fist closed on the shaft. The free hand hangs, its fingers working.
        uppers[0].xRot = -.25F + breath * .02F;
        uppers[0].zRot += .10F;
        fores[0].xRot = -.45F;
        curl(0, .25F);
        uppers[1].xRot = .05F;
        uppers[1].zRot -= .14F;
        fores[1].xRot = -.35F;
        curl(1, .45F + .25F * Mth.sin(age * .09F) * Mth.sin(age * .023F));
        for (int i = 0; i < 2; i++) tails[i].xRot += Mth.sin(age * .05F + i) * .04F;
        // Walking: a long stride bending at the knee and ankle, the hips dipping, the tails swinging behind.
        boolean perched = !trueSelf && (phase == FinaleRules.FinalePhase.RAFTERS
                || pavilionPhase == CircusPavilionStateRules.Phase.RAFTERS);
        float step = perched ? 0.0F : Mth.clamp(movement * 4.0F, 0.0F, 1.0F);
        float stride = limbSwing * .6662F;
        pelvis.y += Math.abs(Mth.sin(stride)) * .7F * step;
        for (int i = 0; i < 2; i++) {
            float swing = Mth.cos(stride + i * Mth.PI);
            thighs[i].xRot += swing * .6F * step;
            shins[i].xRot += Math.max(0.0F, Mth.sin(stride + i * Mth.PI)) * .9F * step;
            feet[i].xRot = -(thighs[i].xRot + shins[i].xRot) * .6F;
            tails[i].xRot += Math.max(0.0F, -swing) * .45F * step + .15F * step;
        }
        uppers[1].xRot += Mth.cos(stride) * .4F * step;
        float cane = -Mth.cos(stride) * .35F * step;
        pelvis.yRot += Mth.cos(stride) * .06F * step;
        if (perched) perch(breath);
        if (!trueSelf && pavilionPhase == CircusPavilionStateRules.Phase.CAROUSEL) {
            present(FinaleRules.REVEAL_TICKS / 2.0F, age);
        }
        float blow = 0.0F;
        BossMoveTimeline timeline = move.timeline();
        if (timeline != null) {
            float commit = timeline.commitTick(), total = timeline.total();
            switch (move) {
                case KNIFE_THROW -> throwKnife(moveTime, commit, total);
                case STAFF_STRIKE -> blow = staffStrike(moveTime, commit, total);
                case SPOTLIGHT -> spotlight(moveTime, commit, total, age);
                case CUE -> blow = cue(moveTime, commit, total);
                case STRINGS_CUT -> pullStrings(moveTime, commit, total);
                default -> {
                }
            }
        }
        if (trueSelf && phase == FinaleRules.FinalePhase.REVEAL) present(phaseTime, age);
        if (phase == FinaleRules.FinalePhase.KNEEL || phase == FinaleRules.FinalePhase.ENDED) {
            blow = 0.0F;
            kneel(HemoEasing.smoothstep(0.0F, FinaleRules.KNEEL_SETTLE_TICKS, phaseTime), breath);
        } else if (!trueSelf && pavilionPhase == CircusPavilionStateRules.Phase.CURTAIN) {
            blow = 0.0F;
            kneel(HemoEasing.smoothstep(0.0F, 14.0F, moveTime), breath);
            chest.xRot += .4F * HemoEasing.smoothstep(20.0F, 40.0F, moveTime);
        }

        // The upright shaft follows the hand's position. It follows the arm's rotation during a strike.
        Quaternionf planted = rotation(pelvis, chest, uppers[0], fores[0], hands[0]).conjugate()
                .rotateZYX(-.16F, 0.0F, cane);
        Quaternionf striking = new Quaternionf().rotationZYX(staff.getInitialPose().zRot, 0.0F, STAFF_BLOW);
        orient(staff, planted.slerp(striking, blow));
        float pulse = 1.0F + Mth.sin(age * .11F) * .06F;
        core.xScale = core.yScale = core.zScale = pulse;
        for (int i = 0; i < threads.length; i++) {
            Quaternionf strand = rotation(pelvis, chest).conjugate()
                    .rotateZYX(Mth.sin(age * .04F + i) * .035F, 0.0F, Mth.cos(age * .035F + i) * .025F);
            orient(threads[i], strand);
        }
    }

    private void perch(float breath) {
        pelvis.y += 8.0F;
        chest.xRot = .18F + breath * .025F;
        head.xRot -= .1F;
        uppers[0].xRot = -.55F;
        uppers[1].xRot = -.2F;
        for (int i = 0; i < 2; i++) {
            thighs[i].xRot = -1.18F;
            thighs[i].yRot = i == 0 ? .12F : -.12F;
            shins[i].xRot = 1.38F;
            feet[i].xRot = -.2F;
            tails[i].xRot = .32F;
        }
    }

    private static Quaternionf rotation(ModelPart... chain) {
        Quaternionf turn = new Quaternionf();
        for (ModelPart part : chain) turn.rotateZYX(part.zRot, part.yRot, part.xRot);
        return turn;
    }

    private static void orient(ModelPart part, Quaternionf rotation) {
        Vector3f angles = rotation.getEulerAnglesZYX(new Vector3f());
        part.xRot = angles.x;
        part.yRot = angles.y;
        part.zRot = angles.z;
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .06F);
            tips[i][f].zRot = sign * amount * 1.1F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    /** 0 until {@code lead} ticks before {@code at}, then 1 at {@code at}, accelerating like a falling weight. */
    private static float drop(float since, float at, float lead) {
        float t = Mth.clamp((since - (at - lead)) / lead, 0.0F, 1.0F);
        return t * t;
    }

    /**
     * The staff strike: the topper hauled back over his shoulder through the wind-up, the body rearing, a tremble at the
     * top, then cracked down in front of him on the commit tick, the body pitched into it, a hold, and the recovery.
     * Returns how fully the staff rides the arm.
     */
    private float staffStrike(float ticks, float commit, float total) {
        float raise = HemoEasing.smoothstep(0.0F, commit - 1.0F, ticks);
        float down = drop(ticks, commit, 2.0F);
        float tension = HemoEasing.smoothstep(commit - 4.0F, commit - 2.0F, ticks) * (1.0F - down);
        float tremble = Mth.sin(ticks * 3.1F) * tension;
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, Mth.lerp(raise, -1.0F, -3.0F) + .04F * tremble, -.7F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, -.1F);
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, -.4F, -.15F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(down, -.9F * raise, .4F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(down, -.5F, -.25F));
        chest.xRot += weight * Mth.lerp(down, -.22F * raise, .32F);
        head.xRot += weight * Mth.lerp(down, -.2F * raise, .15F);
        // He steps into the blow: the right foot forward, the left knee bending.
        thighs[0].xRot += weight * Mth.lerp(down, .1F, -.5F);
        shins[0].xRot += weight * down * .3F;
        thighs[1].xRot += weight * down * .25F;
        shins[1].xRot += weight * down * .35F;
        for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
        return weight;
    }

    /** The free arm drawn back behind the shoulder through the wind-up, then whipped out and down as the knife leaves. */
    private void throwKnife(float ticks, float commit, float total) {
        float wind = HemoEasing.smoothstep(0.0F, commit * .6F, ticks);
        float whip = drop(ticks, commit, 2.0F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(whip, Mth.lerp(wind, -.4F, -2.8F), -.8F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(whip, -.35F, .2F));
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, Mth.lerp(whip, -1.3F * wind, -.1F));
        curl(1, Mth.lerp(weight, .45F, Mth.lerp(whip, 1.2F, .05F)));
        chest.yRot += weight * Mth.lerp(whip, .35F * wind, -.3F);
        head.yRot -= weight * Mth.lerp(whip, .2F * wind, -.15F);
    }

    /** The staff lifted high and held for the whole of the light, the free hand pointing down at the lit pool. */
    private void spotlight(float ticks, float commit, float total, float age) {
        float hold = HemoEasing.smoothstep(0.0F, commit * .6F, ticks) * (1.0F - HemoEasing.smoothstep(total - 6.0F, total, ticks));
        float sway = Mth.sin(age * .15F) * .05F;
        uppers[0].xRot = Mth.lerp(hold, uppers[0].xRot, -2.8F + sway);
        uppers[0].zRot = Mth.lerp(hold, uppers[0].zRot, .2F);
        fores[0].xRot = Mth.lerp(hold, fores[0].xRot, -.2F);
        uppers[1].xRot = Mth.lerp(hold, uppers[1].xRot, -1.2F);
        uppers[1].yRot = Mth.lerp(hold, uppers[1].yRot, -.3F);
        fores[1].xRot = Mth.lerp(hold, fores[1].xRot, -.1F);
        curl(1, Mth.lerp(hold, .45F, .1F));
        head.xRot += .25F * hold;
    }

    /** The Cue: the staff raised like a baton through the count, the free arm high, then brought down hard on the beat. */
    private float cue(float ticks, float commit, float total) {
        float count = HemoEasing.smoothstep(0.0F, commit - 1.0F, ticks);
        float down = drop(ticks, commit, 1.5F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, Mth.lerp(count, -1.0F, -3.0F), -.5F));
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, -.3F, -.2F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(down, -2.4F * count, -.6F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(down, -.9F, -.3F));
        curl(1, Mth.lerp(weight, .45F, .05F));
        head.xRot += weight * Mth.lerp(down, -.3F * count, .1F);
        chest.xRot += weight * down * .28F;
        return weight;
    }

    /** Strings Cut: both fists close and haul back on a thread through the wind-up, then the borrowed act lands. */
    private void pullStrings(float ticks, float commit, float total) {
        float haul = HemoEasing.smoothstep(0.0F, commit, ticks);
        float release = drop(ticks, commit + 1.5F, 1.5F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 3.0F, total, ticks));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(release, -1.5F + .4F * haul, -1.9F));
        uppers[1].yRot = Mth.lerp(weight, uppers[1].yRot, -.5F * (1.0F - release));
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, Mth.lerp(release, -1.2F * haul, -.1F));
        curl(1, Mth.lerp(weight, .45F, Mth.lerp(release, 1.5F, .1F)));
        chest.xRot += weight * Mth.lerp(release, -.25F * haul, .3F);
    }

    /** The reveal: the staff raised high, the free arm thrown wide to the stands, the head back. */
    private void present(float ticks, float age) {
        float on = HemoEasing.smoothstep(0.0F, 14.0F, ticks)
                * (1.0F - HemoEasing.smoothstep(FinaleRules.REVEAL_TICKS - 12.0F, FinaleRules.REVEAL_TICKS, ticks));
        float sway = Mth.sin(age * .07F);
        uppers[0].xRot = Mth.lerp(on, uppers[0].xRot, -2.6F + .05F * sway);
        uppers[0].zRot = Mth.lerp(on, uppers[0].zRot, .25F);
        fores[0].xRot = Mth.lerp(on, fores[0].xRot, -.3F);
        uppers[1].xRot = Mth.lerp(on, uppers[1].xRot, -.4F);
        uppers[1].zRot = Mth.lerp(on, uppers[1].zRot, -1.25F + .06F * sway);
        fores[1].xRot = Mth.lerp(on, fores[1].xRot, -.15F);
        curl(1, Mth.lerp(on, .45F, .1F));
        chest.xRot -= .15F * on;
        head.xRot -= .3F * on;
    }

    /** The kneel: onto the right knee, the left foot planted before him, the staff planted, the head bowed over it. */
    private void kneel(float k, float breath) {
        pelvis.y += KNEEL_DROP * k;
        pelvis.yRot *= 1.0F - k;
        thighs[0].xRot = Mth.lerp(k, thighs[0].xRot, .12F);
        shins[0].xRot = Mth.lerp(k, shins[0].xRot, 1.45F);
        feet[0].xRot = Mth.lerp(k, feet[0].xRot, -.25F);
        thighs[1].xRot = Mth.lerp(k, thighs[1].xRot, -1.45F);
        thighs[1].zRot = Mth.lerp(k, thighs[1].zRot, -.08F);
        shins[1].xRot = Mth.lerp(k, shins[1].xRot, 1.42F);
        feet[1].xRot = -thighs[1].xRot - shins[1].xRot;
        for (int i = 0; i < 2; i++) tails[i].xRot = Mth.lerp(k, tails[i].xRot, .55F);
        chest.xRot = Mth.lerp(k, chest.xRot, .32F + breath * .03F);
        chest.yRot *= 1.0F - k;
        head.xRot = Mth.lerp(k, head.xRot, .65F);
        head.yRot *= 1.0F - k;
        uppers[0].xRot = Mth.lerp(k, uppers[0].xRot, -.55F);
        uppers[0].zRot = Mth.lerp(k, uppers[0].zRot, .05F);
        fores[0].xRot = Mth.lerp(k, fores[0].xRot, -1.55F);
        // The free hand on the raised knee.
        uppers[1].xRot = Mth.lerp(k, uppers[1].xRot, -.75F);
        uppers[1].zRot = Mth.lerp(k, uppers[1].zRot, -.05F);
        uppers[1].yRot = Mth.lerp(k, uppers[1].yRot, 0.0F);
        fores[1].xRot = Mth.lerp(k, fores[1].xRot, -.2F);
        curl(1, Mth.lerp(k, .45F, .9F));
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
