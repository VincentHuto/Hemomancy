package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CastingPoseIntegrationTest {
    @Test void locomotionOwnsLegsWhileMovingOrUsingSpecialPostures() {
        assertEquals(1,CastingPlayerPose.stanceWeight(true,false,0,0));
        assertEquals(0,CastingPlayerPose.stanceWeight(true,false,.1F,0));
        assertEquals(0,CastingPlayerPose.stanceWeight(true,false,0,.5F));
        assertEquals(0,CastingPlayerPose.stanceWeight(false,false,0,0));
        assertEquals(0,CastingPlayerPose.stanceWeight(true,true,0,0));
    }
    @Test void bespokeRecoveryAlwaysReturnsToOrdinaryPose() {
        for(var style:CastStyle.values()) {
            var clip=CastingClips.get(EnumBloodTendency.FERRIC,EnumManipulationRank.MAGISTER);
            assertEquals(CastingPose.ZERO,CastingPoseModifiers.apply(clip.sample(CastPhase.RECOVERY,20),
                    CastPurpose.SELF,style,CastPhase.RECOVERY,20,false));
        }
    }
    @Test void bespokeRecoveryBeginsAtItsOwnReleasePose() {
        var clip=CastingClips.get(EnumBloodTendency.FERRIC,EnumManipulationRank.MAGISTER);
        var released=CastingPoseModifiers.apply(clip.sample(CastPhase.RELEASE,6),CastPurpose.SELF,
                CastStyle.IRONHEART,CastPhase.RELEASE,6,false);
        var recovering=CastingPoseModifiers.apply(clip.sample(CastPhase.RECOVERY,0),CastPurpose.SELF,
                CastStyle.IRONHEART,CastPhase.RECOVERY,0,false);
        assertEquals(released,recovering);
    }
    @Test void differentPurposesDoNotEraseSchoolIdentity() {
        var distinct=new java.util.HashSet<CastingPose>();
        for(var school:EnumBloodTendency.values()) {
            var pose=CastingClips.get(school,EnumManipulationRank.MEDIOCRITAS).sample(CastPhase.RELEASE,2);
            distinct.add(CastingPoseModifiers.apply(pose,CastPurpose.PROJECTILE,CastStyle.SCHOOL,CastPhase.RELEASE,2,false));
        }
        assertEquals(8,distinct.size());
    }
    @Test void everyPurposeRecoversFromItsReleaseInBothViews() {
        for(var school:EnumBloodTendency.values())for(var rank:EnumManipulationRank.values())
            for(var purpose:CastPurpose.values())for(boolean first:new boolean[]{false,true}) {
                var clip=CastingClips.get(school,rank);
                var end=first?clip.firstPerson(CastPhase.RELEASE,6):clip.sample(CastPhase.RELEASE,6);
                var start=first?clip.firstPerson(CastPhase.RECOVERY,0):clip.sample(CastPhase.RECOVERY,0);
                assertEquals(CastingPoseModifiers.apply(end,purpose,CastStyle.SCHOOL,CastPhase.RELEASE,6,first),
                        CastingPoseModifiers.apply(start,purpose,CastStyle.SCHOOL,CastPhase.RECOVERY,0,first));
            }
    }
    @Test void sustainedStylesHoldWithoutPhaseBoundarySnaps() {
        for(var style:CastStyle.values())for(var purpose:CastPurpose.values())for(boolean first:new boolean[]{false,true}) {
            var clip=CastingClips.get(EnumBloodTendency.ANIMUS,EnumManipulationRank.SUMMA);
            var opening=first?clip.firstPerson(CastPhase.OPENING,6):clip.sample(CastPhase.OPENING,6);
            var holding=first?clip.firstPerson(CastPhase.HOLD,0):clip.sample(CastPhase.HOLD,0);
            var releasing=first?clip.firstPerson(CastPhase.RELEASE,0):clip.sample(CastPhase.RELEASE,0);
            var settled=CastingPoseModifiers.apply(holding,purpose,style,CastPhase.HOLD,0,first);
            assertEquals(settled,CastingPoseModifiers.apply(opening,purpose,style,CastPhase.OPENING,6,first));
            assertEquals(settled,CastingPoseModifiers.apply(releasing,purpose,style,CastPhase.RELEASE,0,first));
            for(int tick=1;tick<=240;tick++) {
                var held=CastingPoseModifiers.apply(holding,purpose,style,CastPhase.HOLD,tick,first);
                assertEquals(settled,held);
                assertEquals(0,held.body().x());
                if(first)assertTrue(Math.abs(held.rightArm().x())<Math.toRadians(12));
            }
        }
    }
}
