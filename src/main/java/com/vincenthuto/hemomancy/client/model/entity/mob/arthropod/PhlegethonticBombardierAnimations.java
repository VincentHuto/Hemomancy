package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.BombardierFlameGeometry;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

import static net.minecraft.client.animation.AnimationChannel.Interpolations.CATMULLROM;

public final class PhlegethonticBombardierAnimations {
    private static final String[] LEG_ROWS = {"fore", "middle", "hind"};

    private PhlegethonticBombardierAnimations() {}

    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2).looping()
            .addAnimation("abdomen", loop(2, 2, 0, 0))
            .addAnimation("antenna_r", loop(2, 3, -7, 2))
            .addAnimation("antenna_l", loop(2, -3, -7, 2)).build();

    public static final AnimationDefinition WALK = walk();

    public static final AnimationDefinition FLATTEN_REST = flattenRest();

    public static final AnimationDefinition WAKE_WARNING = AnimationDefinition.Builder.withLength(.6F)
            .addAnimation("head", rotate(rotation(0, 0, 0, 0), rotation(.6F, -9, 0, 0)))
            .addAnimation("antenna_r", rotate(rotation(0, 0, 0, 0), rotation(.6F, -16, -12, 0)))
            .addAnimation("antenna_l", rotate(rotation(0, 0, 0, 0), rotation(.6F, -16, 12, 0)))
            .addAnimation("elytron_r", rotate(rotation(0, 0, 0, 0), rotation(.6F, -5, 7, -7)))
            .addAnimation("elytron_l", rotate(rotation(0, 0, 0, 0), rotation(.6F, -5, -7, 7))).build();

    public static final AnimationDefinition GRAZE = AnimationDefinition.Builder.withLength(1).looping()
            .addAnimation("head", rotate(rotation(0, 8, 0, 0), rotation(.24F, 24, -5, 0),
                    rotation(.5F, 13, 0, 0), rotation(.76F, 24, 5, 0), rotation(1, 8, 0, 0)))
            .addAnimation("mandible_r", loop(1, 3, -10, 0))
            .addAnimation("mandible_l", loop(1, 3, 10, 0))
            .addAnimation("antenna_r", loop(1, -8, 4, 0))
            .addAnimation("antenna_l", loop(1, -8, -4, 0)).build();

    private static final float RAISED = BombardierFlameGeometry.RAISED_ABDOMEN_DEGREES;

    public static final AnimationDefinition ABDOMEN_WINDUP = AnimationDefinition.Builder.withLength(1.5F)
            .addAnimation("abdomen", rotate(rotation(0, 0, 0, 0), rotation(.35F, 12, 0, 0),
                    rotation(1, RAISED, 0, 0), rotation(1.5F, RAISED, 0, 0)))
            .addAnimation("spray_valve", rotate(rotation(0, 0, 0, 0), rotation(.42F, 25, 0, 0),
                    rotation(1, 180 - RAISED, 0, 0), rotation(1.5F, 180 - RAISED, 0, 0)))
            .addAnimation("elytron_r", flare(1))
            .addAnimation("elytron_l", flare(-1))
            .addAnimation("head", rotate(rotation(0, 0, 0, 0), rotation(1, 12, 0, 0))).build();

    public static final AnimationDefinition ABDOMEN_FIRE = AnimationDefinition.Builder.withLength(.8F)
            .addAnimation("abdomen", rotate(rotation(0, RAISED, 0, 0)))
            .addAnimation("spray_valve", rotate(rotation(0, 180 - RAISED, 0, 0)))
            .addAnimation("elytron_r", fireWing(1))
            .addAnimation("elytron_l", fireWing(-1))
            .addAnimation("head", rotate(rotation(0, 12, 0, 0),
                    rotation(.16F, 16, 0, 0), rotation(.8F, 12, 0, 0))).build();

    public static final AnimationDefinition VENT_COOLDOWN = AnimationDefinition.Builder.withLength(1).looping()
            .addAnimation("spray_valve", loop(1, 12, 3, 0))
            .addAnimation("elytron_r", loop(1, -3, 0, -3))
            .addAnimation("elytron_l", loop(1, -3, 0, 3)).build();

    public static final AnimationDefinition HURT = AnimationDefinition.Builder.withLength(.25F)
            .addAnimation("bombardier", rotate(rotation(0, 0, 0, 0), rotation(.1F, 0, 0, 7),
                    rotation(.25F, 0, 0, 0))).build();

    public static final AnimationDefinition DEATH = death();

    private static AnimationDefinition walk() {
        var walk = AnimationDefinition.Builder.withLength(1).looping();
        for (int row = 0; row < LEG_ROWS.length; row++) {
            for (int side : new int[]{1, -1}) {
                String suffix = LEG_ROWS[row] + (side > 0 ? "_r" : "_l");
                int phase = (row == 1 ? -1 : 1) * side;
                walk.addAnimation("leg_" + suffix, loop(1, 0, phase * 16, -side * phase * 10));
                walk.addAnimation("shin_" + suffix, loop(1, phase * 7, 0, -side * phase * 4));
            }
        }
        return walk.build();
    }

    private static AnimationDefinition flattenRest() {
        var rest = AnimationDefinition.Builder.withLength(.8F)
                .addAnimation("bombardier", new AnimationChannel(AnimationChannel.Targets.POSITION,
                        new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), CATMULLROM),
                        new Keyframe(.8F, KeyframeAnimations.posVec(0, -.9F, 0), CATMULLROM)))
                .addAnimation("head", rotate(rotation(0, 0, 0, 0), rotation(.8F, 8, 0, 0)));
        for (String row : LEG_ROWS) {
            rest.addAnimation("leg_" + row + "_r", rotate(rotation(0, 0, 0, 0), rotation(.8F, 0, 0, -7)));
            rest.addAnimation("leg_" + row + "_l", rotate(rotation(0, 0, 0, 0), rotation(.8F, 0, 0, 7)));
        }
        return rest.build();
    }

    private static AnimationDefinition death() {
        var death = AnimationDefinition.Builder.withLength(.8F)
                .addAnimation("head", rotate(rotation(0, 0, 0, 0), rotation(.8F, 25, 0, 0)));
        for (String row : LEG_ROWS) {
            death.addAnimation("shin_" + row + "_r", rotate(rotation(0, 0, 0, 0), rotation(.8F, 0, 0, 42)));
            death.addAnimation("shin_" + row + "_l", rotate(rotation(0, 0, 0, 0), rotation(.8F, 0, 0, -42)));
        }
        return death.build();
    }

    private static AnimationChannel flare(int side) {
        return rotate(rotation(0, 0, 0, 0), rotation(.45F, -10, side * 9, side * -8),
                rotation(1, -27, side * 20, side * -17));
    }

    private static AnimationChannel fireWing(int side) {
        return rotate(rotation(0, -27, side * 20, side * -17), rotation(.18F, -31, side * 23, side * -21),
                rotation(.43F, -26, side * 19, side * -16), rotation(.64F, -29, side * 22, side * -19),
                rotation(.8F, -27, side * 20, side * -17));
    }

    private static AnimationChannel loop(float seconds, float x, float y, float z) {
        return rotate(rotation(0, 0, 0, 0), rotation(seconds * .25F, x, y, z),
                rotation(seconds * .75F, -x, -y, -z), rotation(seconds, 0, 0, 0));
    }

    private static AnimationChannel rotate(Keyframe... frames) {
        return new AnimationChannel(AnimationChannel.Targets.ROTATION, frames);
    }

    private static Keyframe rotation(float time, float x, float y, float z) {
        return new Keyframe(time, KeyframeAnimations.degreeVec(x, y, z), CATMULLROM);
    }
}
