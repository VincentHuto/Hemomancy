package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import java.util.EnumMap;
import static com.vincenthuto.hemomancy.client.player.CastingPose.*;

/** Chest-height gestures for rigid limbs. Sustained casts settle once and hold until the server ends them. */
public final class CastingClips {
    private static final EnumMap<EnumBloodTendency, EnumMap<EnumManipulationRank, Clip>> CLIPS =
            new EnumMap<>(EnumBloodTendency.class);
    static {
        // ANIMUS: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.ANIMUS, EnumManipulationRank.HUMILIS,
                p(3,0,0,-84.4,-24,0,0,0), p(1,0,0,-89,-12,0,0,0), p(-1.2,0,0,-92,-15,0,0,0), f(3,-4.8,0,0), f(-4.5,-3.6,0,0));
        add(EnumBloodTendency.ANIMUS, EnumManipulationRank.MEDIOCRITAS,
                p(2.5,0,-1.6,-86,-28,-81,20,0), p(1.2,0,1.6,-81,-20,-89,26,0), p(-1.8,0,0,-91,-35,-91,35,0), f(3.5,-7.2,2,7.2), f(-5,-10.8,-5,10.8));
        add(EnumBloodTendency.ANIMUS, EnumManipulationRank.SUMMA,
                p(4.5,0,0,-86.4,-40,-86.4,40,6), p(3,0,0,-91,-32,-91,32,7), p(-3,0,0,-95,-65,-95,65,8), f(5,-10.8,5,10.8), f(-6,-16.8,-6,16.8));
        add(EnumBloodTendency.ANIMUS, EnumManipulationRank.MAGISTER,
                p(3.5,0,-1,-88.8,-28,-84,35,7), p(5.5,0,1,-92.4,-18,-90,24,8.5), p(-4,0,0,-97,-72,-94,65,6), f(6.2,-13.2,4,12), f(-7,-19.2,-6,16.8));
        add(EnumBloodTendency.ANIMUS, EnumManipulationRank.PERFECTUS,
                p(-2,0,0,-90,-48,-90,48,5), p(-3.5,0,0,-94.4,-70,-94.4,70,6), p(-3.5,0,0,-94.4,-70,-94.4,70,6), f(3,-15,3,15), f(-5,-21,-5,21));
        // FLAMMEUS: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.FLAMMEUS, EnumManipulationRank.HUMILIS,
                p(2.5,0,-3.2,-76,-15,0,0,0), p(3.5,0,-4.4,-76,-12,0,0,0), p(-3,0,2.4,-93,-10,0,0,0), f(-3,-4.8,0,0), f(-7,4.8,0,0));
        add(EnumBloodTendency.FLAMMEUS, EnumManipulationRank.MEDIOCRITAS,
                p(3,0,-4.8,-76,-28,-76,18,0), p(4.5,0,-6.4,-76,-22,-76,22,0), p(-4,0,2.8,-93,-36,-88,32,0), f(-4.5,-9,-2,7.2), f(-7.5,-10.8,-5.5,12));
        add(EnumBloodTendency.FLAMMEUS, EnumManipulationRank.SUMMA,
                p(4,0,-2.4,-76,-40,-76,40,8), p(6,0,-3.6,-76,-30,-76,36,9), p(-5.5,0,2,-96,-55,-93,48,10), f(-5.5,-10.8,-5,10.8), f(-9,-15,-7.5,14.4));
        add(EnumBloodTendency.FLAMMEUS, EnumManipulationRank.MAGISTER,
                p(5,0,-4,-78.4,-36,-77,28,7), p(6.5,0,-5.6,-84,-18,-81.6,18,8), p(-6,0,1.6,-97,-68,-94,62,9), f(-4.5,-12,-4,10.8), f(-9.5,-16.8,-7.5,16.8));
        add(EnumBloodTendency.FLAMMEUS, EnumManipulationRank.PERFECTUS,
                p(6,0,0,-76,-35,-76,35,7), p(7,0,0,-77,-46,-77,46,8), p(8,0,0,-78,-55,-78,55,9), f(-3,-16.8,-3,16.8), f(-4.5,-21.6,-4.5,21.6));
        // DUCTILIS: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.DUCTILIS, EnumManipulationRank.HUMILIS,
                p(2,0,-1.6,-81,-35,0,0,0), p(-3,0,2.8,-93,12,0,0,0), p(2.5,0,-2.4,-89.6,-22,0,0,0), f(4.5,7.2,0,0), f(-6,-8.4,0,0));
        add(EnumBloodTendency.DUCTILIS, EnumManipulationRank.MEDIOCRITAS,
                p(3.8,0,-4.4,-94,15,-79,42,0), p(-4.5,0,4.4,-80,-40,-94,-15,0), p(1.2,0,-1.2,-91,-10,-86,35,0), f(5.5,10.8,-3.5,13.2), f(-6.2,-7.2,4.5,-10.8));
        add(EnumBloodTendency.DUCTILIS, EnumManipulationRank.SUMMA,
                p(4.5,0,-5.6,-99,12,-81,62,7), p(-5,0,6,-81,-62,-99,-12,3), p(2,0,0,-93,-25,-93,25,9), f(7,13.2,-4.5,18), f(-7,-9.6,6,-14.4));
        add(EnumBloodTendency.DUCTILIS, EnumManipulationRank.MAGISTER,
                p(5,0,5,-84,-58,-101,10,5), p(-6.2,0,-6.4,-101,-8,-81,56,9), p(3,0,4,-96,-42,-84,35,6), f(7.5,-14.4,-5,14.4), f(-7.5,12,6,-18));
        add(EnumBloodTendency.DUCTILIS, EnumManipulationRank.PERFECTUS,
                p(5.5,0,-7,-102,5,-81,75,9), p(-5.5,0,7,-81,-75,-102,-5,9), p(0,0,0,-96,-65,-96,65,6), f(7,16.8,-6.2,19.2), f(-8,-16.8,7,-19.2));
        // LUX: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.LUX, EnumManipulationRank.HUMILIS,
                p(-1.5,0,0,-87,-12,0,0,0), p(-3,0,0,-97,-18,0,0,0), p(-2.5,0,0,-93,-28,0,0,0), f(4.5,-7.2,0,0), f(-2.5,-13.2,0,0));
        add(EnumBloodTendency.LUX, EnumManipulationRank.MEDIOCRITAS,
                p(-1.5,0,0,-85,-15,-85,15,0), p(-3,0,0,-93,-40,-93,40,0), p(-4,0,0,-96,-60,-96,60,0), f(3,-9.6,3,9.6), f(-3.8,-15.6,-3.8,15.6));
        add(EnumBloodTendency.LUX, EnumManipulationRank.SUMMA,
                p(-2.5,0,0,-89,-28,-89,28,4), p(-4.5,0,0,-99,-55,-99,55,5), p(-5,0,0,-94,-82,-94,82,6), f(4,-14.4,4,14.4), f(-4.5,-19.2,-4.5,19.2));
        add(EnumBloodTendency.LUX, EnumManipulationRank.MAGISTER,
                p(-3.5,0,0,-94,-40,-94,40,4), p(-5.5,0,0,-103,-34,-103,34,5), p(-6,0,0,-100,-65,-100,65,5), f(5.5,-15.6,5.5,15.6), f(-4,-21.6,-4,21.6));
        add(EnumBloodTendency.LUX, EnumManipulationRank.PERFECTUS,
                p(-4,0,0,-95,-65,-95,65,5), p(-6.2,0,0,-92,-90,-92,90,6), p(-6.2,0,0,-92,-90,-92,90,6), f(4.5,-19.2,4.5,19.2), f(-3,-25.2,-3,25.2));
        // MORTEM: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.MORTEM, EnumManipulationRank.HUMILIS,
                p(3.8,0,-2,-93,-15,0,0,0), p(5,0,1.6,-85,-30,0,0,0), p(6.2,0,2.8,-78.4,-12,0,0,0), f(-4.5,-4.8,0,0), f(5,-7.2,0,0));
        add(EnumBloodTendency.MORTEM, EnumManipulationRank.MEDIOCRITAS,
                p(4.5,0,-3,-95,-22,-81,28,0), p(6.5,0,1.6,-87,-38,-85,34,0), p(7.5,0,2.4,-79.6,-15,-77,18,0), f(-5.5,-7.2,3,9), f(6,-9.6,4.5,10.8));
        add(EnumBloodTendency.MORTEM, EnumManipulationRank.SUMMA,
                p(5.5,0,-2,-97,-32,-89,38,5), p(7.5,0,2.4,-85,-45,-86,45,6), p(8.5,0,0,-77,-18,-77,18,7), f(-6.2,-12,4,13.2), f(7.5,-12,5.5,14.4));
        add(EnumBloodTendency.MORTEM, EnumManipulationRank.MAGISTER,
                p(6.2,0,-2.8,-96,-30,-86,40,7), p(9,0,2.4,-88,-42,-91,30,8), p(10,0,0,-76,-12,-76,15,6), f(-7,-14.4,5.5,12), f(8.5,-13.2,6.5,13.2));
        add(EnumBloodTendency.MORTEM, EnumManipulationRank.PERFECTUS,
                p(7.5,0,0,-79,-15,-79,15,5), p(10.5,0,0,-76,-10,-76,10,5), p(10.5,0,0,-76,-10,-76,10,5), f(3,-7.2,3,7.2), f(5.5,-10.8,5.5,10.8));
        // CONGEATIO: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.CONGEATIO, EnumManipulationRank.HUMILIS,
                p(2,0,0,-79,-30,0,0,0), p(2.5,0,0,-88.4,-20,0,0,0), p(2.5,0,0,-88.4,-20,0,0,0), f(3,-9,0,0), f(4.5,-6,0,0));
        add(EnumBloodTendency.CONGEATIO, EnumManipulationRank.MEDIOCRITAS,
                p(2.5,0,0,-83,-40,-83,40,0), p(3,0,0,-89,-32,-89,32,0), p(3,0,0,-89,-32,-89,32,0), f(3.5,-13.2,3.5,13.2), f(5.5,-9,5.5,9));
        add(EnumBloodTendency.CONGEATIO, EnumManipulationRank.SUMMA,
                p(3,0,0,-84,-52,-84,52,6), p(4,0,0,-91,-40,-91,40,8), p(4,0,0,-91,-40,-91,40,8), f(4.5,-16.8,4.5,16.8), f(6.5,-10.8,6.5,10.8));
        add(EnumBloodTendency.CONGEATIO, EnumManipulationRank.MAGISTER,
                p(3.8,0,0,-89,-42,-89,42,7), p(5.5,0,0,-94,-28,-94,28,9), p(5.5,0,0,-94,-28,-94,28,9), f(6,-13.2,6,13.2), f(7.5,-8.4,7.5,8.4));
        add(EnumBloodTendency.CONGEATIO, EnumManipulationRank.PERFECTUS,
                p(4.5,0,0,-93,-32,-93,32,6), p(6.5,0,0,-96,-22,-96,22,8), p(6.5,0,0,-96,-22,-96,22,8), f(7,-10.8,7,10.8), f(8.5,-6,8.5,6));
        // FERRIC: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.FERRIC, EnumManipulationRank.HUMILIS,
                p(2,0,-1.6,-99,-12,0,0,0), p(2.5,0,-2.4,-103,-8,0,0,0), p(3.5,0,1.6,-81.6,-18,0,0,0), f(7,-4.8,0,0), f(-6,-7.2,0,0));
        add(EnumBloodTendency.FERRIC, EnumManipulationRank.MEDIOCRITAS,
                p(2.5,0,-3.2,-99,-12,-87,30,0), p(3,0,-4.4,-104,-8,-89,35,0), p(4.5,0,2.4,-83,-20,-89,35,0), f(7.5,-7.2,3.5,12), f(-7,-10.8,3.5,12));
        add(EnumBloodTendency.FERRIC, EnumManipulationRank.SUMMA,
                p(3,0,-3.6,-97,-18,-90,38,9), p(4,0,-5.2,-102,-12,-91,40,10), p(5,0,2.8,-80.4,-25,-91,40,11), f(8,-10.8,4.5,14.4), f(-7.5,-13.2,4.5,14.4));
        add(EnumBloodTendency.FERRIC, EnumManipulationRank.MAGISTER,
                p(3.8,0,0,-86.4,-38,-86.4,38,10), p(5.5,0,-2.4,-97,-22,-90,35,11), p(6.5,0,2,-81,-32,-88,40,12), f(6,-14.4,6,14.4), f(-8,-16.8,5,16.8));
        add(EnumBloodTendency.FERRIC, EnumManipulationRank.PERFECTUS,
                p(4,0,0,-77,-32,-77,32,10), p(5,0,0,-81,-46,-81,46,12), p(5,0,0,-81,-46,-81,46,12), f(4.5,-18,4.5,18), f(2,-22.8,2,22.8));
        // TENEBRIS: Humilis, Mediocritas, Summa, Magister, Perfectus.
        add(EnumBloodTendency.TENEBRIS, EnumManipulationRank.HUMILIS,
                p(1.2,0,-4.4,-90,35,0,0,0), p(0,0,-6,-95,42,0,0,0), p(-1.2,0,4.4,-86,-25,0,0,0), f(4.5,13.2,0,0), f(-4,-10.8,0,0));
        add(EnumBloodTendency.TENEBRIS, EnumManipulationRank.MEDIOCRITAS,
                p(2,0,-6,-95,48,-81,32,0), p(-1.5,0,-8.4,-99,38,-84,45,0), p(-2.5,0,6.8,-83,-40,-91,-20,0), f(5.5,16.8,-2,10.8), f(-5,-15,3.5,-12));
        add(EnumBloodTendency.TENEBRIS, EnumManipulationRank.SUMMA,
                p(2.5,0,-7.6,-96,55,-80,45,7.5), p(-2.5,0,-9.6,-101,42,-85,55,4), p(-3,0,8.4,-85,-48,-95,-25,9), f(6.5,18,-3,13.2), f(-6,-19.2,5,-14.4));
        add(EnumBloodTendency.TENEBRIS, EnumManipulationRank.MAGISTER,
                p(3,0,-6,-97,50,-85,35,6), p(-3.5,0,-10,-102,45,-81,60,9), p(-4,0,9,-87,-55,-97,-18,5), f(7.5,15.6,-4.5,15.6), f(-7,-20.4,6,-15.6));
        add(EnumBloodTendency.TENEBRIS, EnumManipulationRank.PERFECTUS,
                p(2,0,-4.8,-95,42,-79,20,3), p(0,0,-7.2,-100,34,-83,28,4), p(0,0,-7.2,-100,34,-83,28,4), f(6.5,14.4,-4,10.8), f(-5,-18,4.5,-13.2));
    }
    private CastingClips() {}
    private static CastingPose p(double h,double b,double y,double rx,double rz,double lx,double lz,double legs) {
        return pose(h,b,y,rx,rz,lx,lz,legs);
    }
    private static CastingPose f(double rx,double rz,double lx,double lz) { return hands(rx,rz,lx,lz); }
    private static void add(EnumBloodTendency school, EnumManipulationRank rank,
                            CastingPose opening, CastingPose hold, CastingPose release,
                            CastingPose firstHold, CastingPose firstRelease) {
        CLIPS.computeIfAbsent(school, key -> new EnumMap<>(EnumManipulationRank.class))
                .put(rank,new Clip(school,rank,opening,hold,release,firstHold,firstRelease));
    }
    public static Clip get(EnumBloodTendency school, EnumManipulationRank rank) { return CLIPS.get(school).get(rank); }

    public record Clip(EnumBloodTendency school, EnumManipulationRank rank,
                       CastingPose opening, CastingPose hold, CastingPose release,
                       CastingPose firstHold, CastingPose firstRelease) {
        public CastingPose sample(CastPhase phase, float time) {
            return track(phase,time,opening,hold,release,cancelPose());
        }
        public CastingPose firstPerson(CastPhase phase, float time) {
            // Small camera-space gestures at the sides; no camera or aim rotation.
            if (phase == CastPhase.OPENING) return CastingPose.ZERO.blend(firstHold, ease(time/6));
            return track(phase,time,firstHold.blend(CastingPose.ZERO,.25F),firstHold,firstRelease,
                    firstHold.blend(firstRelease,.35F));
        }
        private CastingPose track(CastPhase phase,float time,CastingPose a,CastingPose b,CastingPose c,CastingPose cancel) {
            return switch(phase) {
                case OPENING -> a.blend(b,ease(time/6));
                case HOLD -> b;
                case RELEASE -> b.blend(c,ease(time/6));
                case RECOVERY -> c.blend(CastingPose.ZERO,ease(time/14));
                case CANCEL -> time<3?b.blend(cancel,ease(time/3)):cancel.blend(CastingPose.ZERO,ease((time-3)/7));
            };
        }
        private CastingPose cancelPose() {
            return switch(school) {
                case ANIMUS -> p(3,0,0,-81,-18,-81,18,0);
                case FLAMMEUS -> p(-4.5,0,1.6,-76,-48,-76,48,0);
                case DUCTILIS -> p(3.8,0,-3.6,-77.6,16,-76,28,0);
                case LUX -> p(1,0,0,-78.4,-12,-78.4,12,0);
                case MORTEM -> p(8,0,0,0,0,0,0,0);
                case CONGEATIO -> p(2,0,0,-79,-48,-79,48,0);
                case FERRIC -> p(3,0,0,-76,-28,-76,28,0);
                case TENEBRIS -> p(0,0,3.6,-85,25,-79,-15,0);
            };
        }
    }
    private static float ease(float value) { float t=Math.clamp(value,0,1); return t*t*(3-2*t); }
}
