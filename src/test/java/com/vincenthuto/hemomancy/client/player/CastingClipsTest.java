package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CastingClipsTest {
    @Test void everyRankHasDistinctChoreographyAndReturnsToNeutral() {
        for (var school : EnumBloodTendency.values()) {
            var signatures = new java.util.HashSet<CastingPose>();
            for (var rank : EnumManipulationRank.values()) {
                var clip = CastingClips.get(school, rank);
                signatures.add(clip.sample(CastPhase.HOLD, 8));
                assertEquals(CastingPose.ZERO, clip.sample(CastPhase.RECOVERY, 100));
                assertEquals(CastingPose.ZERO, clip.sample(CastPhase.CANCEL, 100));
                assertNotEquals(CastingPose.ZERO, clip.firstPerson(CastPhase.HOLD, 8));
            }
            assertEquals(5, signatures.size(), school.name());
        }
    }

    @Test void mirroringExchangesLimbsAndReversesTwist() {
        var pose = CastingClips.get(EnumBloodTendency.TENEBRIS, EnumManipulationRank.SUMMA)
                .sample(CastPhase.RELEASE, 2);
        assertEquals(pose, pose.mirror().mirror());
        assertEquals(-pose.body().y(), pose.mirror().body().y());
        assertEquals(pose.rightArm().x(), pose.mirror().leftArm().x());
    }

    @Test void everySchoolSettlesAndHoldsWithoutRepeatingInBothViews() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values()) {
            var clip=CastingClips.get(school,rank);
            for(int tick=0;tick<=240;tick++) {
                assertEquals(clip.sample(CastPhase.OPENING,6),clip.sample(CastPhase.HOLD,tick),school+" "+rank);
                assertEquals(clip.firstPerson(CastPhase.OPENING,6),clip.firstPerson(CastPhase.HOLD,tick),school+" first person");
            }
        }
    }
    @Test void castingKeepsTorsoUprightAndGesturesAboveTheWaist() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values()) {
            var clip=CastingClips.get(school,rank);
            for(var phase:CastPhase.values())for(int tick=0;tick<24;tick++) {
                var pose=clip.sample(phase,tick);
                assertEquals(0,pose.body().x(),school+" torso pitch");
                assertTrue(Math.abs(clip.firstPerson(phase,tick).rightArm().x())<=Math.toRadians(12));
                if(phase==CastPhase.HOLD)for(var arm:new CastingPose.Rotation[]{pose.rightArm(),pose.leftArm()}) {
                    if(arm.equals(CastingPose.Rotation.ZERO))continue;
                    double handY=2+10*Math.cos(arm.x())*Math.cos(arm.z());
                    assertTrue(handY<6,school+" "+rank+" hand should stay at chest height");
                }
            }
        }
    }
}
