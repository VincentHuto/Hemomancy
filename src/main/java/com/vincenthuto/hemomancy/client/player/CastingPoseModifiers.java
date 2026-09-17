package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import static com.vincenthuto.hemomancy.client.player.CastingPose.*;

public final class CastingPoseModifiers {
    private CastingPoseModifiers() {}
    public static CastingPose apply(CastingPose base,CastPurpose purpose,CastStyle style,CastPhase phase,float time,boolean first) {
        if(phase==CastPhase.CANCEL && style==CastStyle.SCHOOL)return base;
        if(style!=CastStyle.SCHOOL) {
            CastingPose a,b;
            if(first) {
                a=switch(style) {
                    case IRONHEART -> hands(8,-12,4.5,13.2);
                    case UMBRAL_STEP,UMBRAL_REVERSAL -> hands(6.2,19.2,-3,14.4);
                    case AVATAR -> hands(5,-18,5,18);
                    case STAFF -> hands(6.2,-9,0,0);
                    case CRIMSON_TITHE -> hands(5.5,-9.6,1.5,12);
                    case UNCLOSING_EYE -> hands(4.5,-20.4,4.5,20.4);
                    case BLOOM_OF_ROT -> hands(7,-15.6,7,15.6);
                    case ENDLESS_HOUR -> hands(7.5,-7.2,7.5,7.2);
                    default -> base;
                };
                b=switch(style) {
                    case IRONHEART -> hands(-7,-10.8,4.5,13.2);
                    case UMBRAL_STEP -> hands(-5,-18,5,-12);
                    case UMBRAL_REVERSAL -> hands(5,12,-5,18);
                    case AVATAR -> hands(-5,-22.8,-5,22.8);
                    case STAFF -> hands(-3.5,-7.2,0,0);
                    case CRIMSON_TITHE -> hands(8,-7.2,6.2,10.8);
                    case UNCLOSING_EYE -> hands(-3,-25.2,-3,25.2);
                    case BLOOM_OF_ROT -> hands(-2,-20.4,-2,20.4);
                    case ENDLESS_HOUR -> hands(8.5,-4.8,8.5,4.8);
                    default -> base;
                };
            } else {
                a=switch(style) {
                    case IRONHEART -> pose(3,0,-3.2,-104,-8,-89.6,38,9);
                    case UMBRAL_STEP,UMBRAL_REVERSAL -> pose(0,0,-8.4,-100,42,-82,40,5);
                    case AVATAR -> pose(2.5,0,0,-88,-35,-88,35,8);
                    case STAFF -> pose(2,0,-2,-91,-15,-77,10,0);
                    case CRIMSON_TITHE -> pose(5.5,0,-2,-90,-28,-81,30,6);
                    case UNCLOSING_EYE -> pose(-4,0,0,-101,-40,-101,40,6);
                    case BLOOM_OF_ROT -> pose(7.5,0,0,-91,-35,-91,35,9);
                    case ENDLESS_HOUR -> pose(4.5,0,0,-92,-28,-92,28,7);
                    default -> base;
                };
                b=switch(style) {
                    case IRONHEART -> pose(5,0,2,-83.6,-18,-89.6,38,10);
                    case UMBRAL_STEP -> pose(-2.5,0,7.2,-85,-42,-94,-22,8);
                    case UMBRAL_REVERSAL -> pose(-2.5,0,-7.2,-94,22,-85,42,8);
                    case AVATAR -> pose(-3,0,0,-97,-75,-97,75,10);
                    case STAFF -> pose(0,0,1.6,-83,-12,0,0,0);
                    case CRIMSON_TITHE -> pose(8,0,0,-83,-18,-88,25,6);
                    case UNCLOSING_EYE -> pose(-6.5,0,0,-92,-90,-92,90,7);
                    case BLOOM_OF_ROT -> pose(5,0,0,-82,-70,-82,70,10);
                    case ENDLESS_HOUR -> pose(6,0,0,-97,-18,-97,18,8);
                    default -> base;
                };
            }
            if (phase==CastPhase.RECOVERY) {
                float t=Math.clamp(time/14,0,1);
                return b.blend(CastingPose.ZERO,t*t*(3-2*t));
            }
            if(phase==CastPhase.CANCEL)return a.blend(CastingPose.ZERO,ease(time/10));
            if(phase==CastPhase.OPENING)return CastingPose.ZERO.blend(a,ease(time/6));
            return phase==CastPhase.RELEASE?a.blend(b,ease(time/6)):a;
        }
        var directional=first?switch(purpose) {
            case PROJECTILE,TARGETED -> hands(-5,-4.8,2,7.2);
            case SELF -> hands(6.2,-9,6.2,9);
            case AREA -> hands(-3,-18,-3,18);
            case CONSTRUCTION -> hands(4,-13.2,4,13.2);
            case CONJURATION -> hands(-3,-6,0,0);
            case TRAVEL -> hands(4,10.8,-3,12);
        }:switch(purpose) {
            case PROJECTILE,TARGETED -> pose(0,0,0,-91,0,-77,15,0);
            case SELF -> pose(2,0,0,-86,-28,-86,28,0);
            case AREA -> pose(0,0,0,-88,-70,-88,70,5);
            case CONSTRUCTION -> pose(1.2,0,0,-92,-40,-92,40,4);
            case CONJURATION -> pose(0,0,0,-86,-12,0,0,0);
            case TRAVEL -> pose(0,0,4,-85,15,-76,20,0);
        };
        if(phase==CastPhase.RECOVERY) {
            float t=Math.clamp(time/14,0,1);
            directional=directional.blend(CastingPose.ZERO,t*t*(3-2*t));
        }
        // An unused support arm should stay relaxed, rather than making a shallow waist-height gesture.
        directional=new CastingPose(directional.head(),directional.body(),
                base.rightArm().equals(CastingPose.Rotation.ZERO)?CastingPose.Rotation.ZERO:directional.rightArm(),
                base.leftArm().equals(CastingPose.Rotation.ZERO)?CastingPose.Rotation.ZERO:directional.leftArm(),
                directional.rightLeg(),directional.leftLeg());
        float strength=phase==CastPhase.RECOVERY?.18F:phase==CastPhase.RELEASE?.08F+.10F*ease(time/6):.08F;
        return base.blend(directional,strength);
    }
    private static float ease(float value) { float t=Math.clamp(value,0,1); return t*t*(3-2*t); }
}
