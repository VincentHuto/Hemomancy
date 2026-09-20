package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public final class PhlegethonticBombardierAnimations {
    private PhlegethonticBombardierAnimations() {}
    private static AnimationChannel rotate(float end, float x, float y, float z) {
        return new AnimationChannel(AnimationChannel.Targets.ROTATION,
                new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(end, KeyframeAnimations.degreeVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM));
    }
    private static AnimationChannel move(float end, float x, float y, float z) {
        return new AnimationChannel(AnimationChannel.Targets.POSITION,
                new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                new Keyframe(end, KeyframeAnimations.posVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM));
    }
    private static AnimationChannel loop(float length, float x, float y, float z) {
        return new AnimationChannel(AnimationChannel.Targets.ROTATION,
                rotation(0, 0, 0, 0), rotation(length * .25F, x, y, z),
                rotation(length * .75F, -x, -y, -z), rotation(length, 0, 0, 0));
    }
    private static AnimationChannel loop(float length, float ax, float ay, float az,
                                         float bx, float by, float bz) {
        return new AnimationChannel(AnimationChannel.Targets.ROTATION,
                rotation(0, 0, 0, 0), rotation(length * .25F, ax, ay, az),
                rotation(length * .75F, bx, by, bz), rotation(length, 0, 0, 0));
    }
    private static Keyframe rotation(float time, float x, float y, float z) {
        return new Keyframe(time, KeyframeAnimations.degreeVec(x, y, z), AnimationChannel.Interpolations.CATMULLROM);
    }
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2).looping()
            .addAnimation("tail", loop(2, -3, 0, 0, 0, 0, 0)).build();
    public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(1).looping()
            .addAnimation("lLegF", loop(1, 18, -20, 0)).addAnimation("rLegF", loop(1, -18, 20, 0))
            .addAnimation("lLegM", loop(1, -15, -12, 0)).addAnimation("rLegM", loop(1, 15, 12, 0))
            .addAnimation("lLegB", loop(1, 18, 15, 0)).addAnimation("rLegB", loop(1, -18, -15, 0)).build();
    public static final AnimationDefinition FLATTEN_REST = AnimationDefinition.Builder.withLength(.8F)
            .addAnimation("whole", move(.8F, 0, 2.2F, 0)).addAnimation("tail", rotate(.8F, 18, 0, 0)).build();
    public static final AnimationDefinition WAKE_WARNING = AnimationDefinition.Builder.withLength(.6F)
            .addAnimation("whole", move(.6F, 0, -1.2F, 0)).addAnimation("wingL", rotate(.6F, 0, 0, -12))
            .addAnimation("wingR", rotate(.6F, 0, 0, 12)).build();
    public static final AnimationDefinition GRAZE = AnimationDefinition.Builder.withLength(1).looping()
            .addAnimation("head", loop(1, 15, -8, 0, 15, 8, 0)).build();
    public static final AnimationDefinition ABDOMEN_WINDUP = AnimationDefinition.Builder.withLength(1.5F)
            .addAnimation("tail", rotate(1.5F, 45, 0, 0)).addAnimation("tail2", rotate(1.5F, 40, 0, 0))
            .addAnimation("tail3", rotate(1.5F, 32, 0, 0)).addAnimation("tail4", rotate(1.5F, -27, 0, 0))
            .addAnimation("wingL", rotate(1.5F, 34, 0, 20)).addAnimation("wingR", rotate(1.5F, 34, 0, -20))
            .build();
    public static final AnimationDefinition ABDOMEN_FIRE = AnimationDefinition.Builder.withLength(.8F)
            .addAnimation("tail", rotate(.8F, 50, 0, 0)).addAnimation("tail2", rotate(.8F, 45, 0, 0))
            .addAnimation("tail3", rotate(.8F, 38, 0, 0)).addAnimation("tail4", rotate(.8F, -43, 0, 0))
            .addAnimation("wingL", rotate(.8F, 42, 0, 24)).addAnimation("wingR", rotate(.8F, 42, 0, -24))
            .build();
    public static final AnimationDefinition VENT_COOLDOWN = AnimationDefinition.Builder.withLength(1).looping()
            .addAnimation("tail4", loop(1, -8, 5, 0, 3, -5, 0)).build();
    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(.25F)
            .addAnimation("whole", rotate(.12F, 0, 0, 8)).addAnimation("whole", rotate(.25F, 0, 0, -4)).build();
    public static final AnimationDefinition DEATH = AnimationDefinition.Builder.withLength(1)
            .addAnimation("whole", rotate(1, 0, 0, 90)).addAnimation("whole", move(1, 0, 3, 0)).build();
}
