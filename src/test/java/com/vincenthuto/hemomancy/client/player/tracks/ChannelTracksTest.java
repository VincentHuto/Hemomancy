package com.vincenthuto.hemomancy.client.player.tracks;

import com.vincenthuto.hemomancy.client.player.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastStyle;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static com.vincenthuto.hemomancy.client.player.CastingTrackRules.*;
import static com.vincenthuto.hemomancy.client.player.tracks.GameplayTracksTest.*;
import static org.junit.jupiter.api.Assertions.*;

class ChannelTracksTest {
    private static final Map<CastStyle,EnumBloodTendency> MAGISTER_CHANNELS=Map.of(CastStyle.FURNACE_VEINS,EnumBloodTendency.FLAMMEUS,
            CastStyle.LIVING_CIRCUIT,EnumBloodTendency.DUCTILIS,CastStyle.IRON_CHOIR,EnumBloodTendency.FERRIC,
            CastStyle.CARRION_COMMUNION,EnumBloodTendency.MORTEM,CastStyle.PENUMBRAL_DRIFT,EnumBloodTendency.TENEBRIS,
            CastStyle.RADIANT_WAKE,EnumBloodTendency.LUX);
    private static final List<CastStyle> CONTINUOUS=List.of(CastStyle.GROUND_TOUCH,CastStyle.DOWSING,CastStyle.FURNACE_VEINS,
            CastStyle.LIVING_CIRCUIT,CastStyle.IRON_CHOIR,CastStyle.CARRION_COMMUNION,CastStyle.PENUMBRAL_DRIFT,CastStyle.RADIANT_WAKE);
    private static final List<CastStyle> PASSIVE=List.of(CastStyle.BLACKHEARTED,CastStyle.SOVEREIGN_INSTINCT);

    @Test void onlyTheChannelStylesResolveAndAreCached() {
        for(var style:CastStyle.values()) {
            if(CONTINUOUS.contains(style)||PASSIVE.contains(style)) { assertNotNull(ChannelTracks.get(style),style.name()); assertSame(ChannelTracks.get(style),ChannelTracks.get(style)); }
            else assertNull(ChannelTracks.get(style),style.name());
        }
        assertNull(ChannelTracks.get(null));
    }

    @Test void everyTrackHonoursTheSharedContract() {
        for(var style:CONTINUOUS) { var t=ChannelTracks.get(style); assertContract(t,style.name(),false); assertTiming(t,style.name(),6,10,12,20); }
        for(var style:PASSIVE) { var t=ChannelTracks.get(style); assertContract(t,style.name(),true); assertTiming(t,style.name(),6,10,12,20); }
    }

    @Test void everyChannelLoopsWithinItsAmplitudeAndPeriod() {
        for(var style:CONTINUOUS) {
            var t=ChannelTracks.get(style);
            assertNotNull(t.loop(),style.name());
            int period=t.loop().period();
            // Living Circuit keeps the Ductilis two-step tic at period 12; the rest are slow channels.
            if(style==CastStyle.LIVING_CIRCUIT)assertEquals(12,period);
            else assertTrue(period>=40 && period<=60,style+" period "+period);
            assertLoopMoves(t,style.name());
        }
    }

    @Test void magisterChannelsDifferFromTheirSchoolBase() {
        for(var entry:MAGISTER_CHANNELS.entrySet()) {
            var t=ChannelTracks.get(entry.getKey()); var base=CastingTracks.base(entry.getValue(),EnumManipulationRank.MAGISTER);
            assertNotEquals(sample(base,CastPhase.HOLD,0,0,false),sample(t,CastPhase.HOLD,0,0,false),entry.getKey()+" hold");
            if(base.loop()!=null)assertNotEquals(base.loop(),t.loop(),entry.getKey()+" loop");
        }
    }

    @Test void channelLoopsExpressTheirSchoolVerb() {
        var furnace=ChannelTracks.get(CastStyle.FURNACE_VEINS);
        assertTrue(Math.abs(deg(furnace.loop().amplitude().pitch()))>=2,"furnace rocks from the hips");
        var fHold=sample(furnace,CastPhase.HOLD,0,0,false);
        assertTrue(deg(fHold.rightArm().x())>-60 && deg(fHold.leftArm().x())>-60,"furnace arms low");
        assertTrue(deg(fHold.rightArm().z())<-15 && deg(fHold.leftArm().z())>15,"furnace arms cocked out");

        var circuit=ChannelTracks.get(CastStyle.LIVING_CIRCUIT).loop().amplitude();
        assertEquals(circuit.rightArm().z(),circuit.leftArm().z(),1e-6,"current runs through both arms together");

        var choir=ChannelTracks.get(CastStyle.IRON_CHOIR).loop().amplitude();
        assertEquals(2,deg(choir.pitch()),1e-3,"iron choir settles 2 degrees");

        var carrion=ChannelTracks.get(CastStyle.CARRION_COMMUNION);
        assertTrue(deg(sample(carrion,CastPhase.HOLD,0,0,false).pitch())>=6,"carrion bowed");
        assertEquals(3,deg(carrion.loop().amplitude().rightArm().x()),1e-3,"carrion hauls 3 degrees");
        assertEquals(carrion.loop().amplitude().rightArm().x(),carrion.loop().amplitude().leftArm().x(),1e-6);

        var drift=ChannelTracks.get(CastStyle.PENUMBRAL_DRIFT);
        assertTrue(Math.abs(deg(sample(drift,CastPhase.HOLD,0,0,false).body().y()))>=8,"drift turned away");
        assertEquals(3,Math.abs(deg(drift.loop().amplitude().body().y())),1e-3,"drift sways 3 degrees");

        var wake=ChannelTracks.get(CastStyle.RADIANT_WAKE); var wHold=sample(wake,CastPhase.HOLD,0,0,false); var wAmp=wake.loop().amplitude();
        assertEquals(wHold.rightArm().x(),wHold.leftArm().x(),1e-6); assertEquals(wHold.rightArm().z(),-wHold.leftArm().z(),1e-6);
        assertEquals(wAmp.rightArm().x(),wAmp.leftArm().x(),1e-6); assertEquals(wAmp.rightArm().z(),-wAmp.leftArm().z(),1e-6);
        assertEquals(-2,deg(wAmp.rightArm().x()),1e-3,"wake rises 2 degrees");
    }

    @Test void groundTouchLowersTheBodyAndPressesTowardTheGround() {
        var t=ChannelTracks.get(CastStyle.GROUND_TOUCH); var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertTrue(deg(hold.pitch())>=10 && deg(hold.pitch())<=14,"body lowers");
        assertTrue(deg(hold.rightArm().x())>=-40,"lead arm low");
        assertTrue(deg(hold.head().x())>=8,"head down");
        var a=t.loop().amplitude(); float max=0;
        for(var r:new CastingPose.Rotation[]{a.head(),a.body(),a.rightArm(),a.leftArm(),a.rightLeg(),a.leftLeg()})
            max=Math.max(max,Math.max(Math.abs(deg(r.x())),Math.max(Math.abs(deg(r.y())),Math.abs(deg(r.z())))));
        max=Math.max(max,Math.abs(deg(a.pitch())));
        assertEquals(2,max,1e-3,"slow 2 degree press");
    }

    @Test void dowsingHoldsALevelRodAndScansWithTheHeadOnly() {
        var t=ChannelTracks.get(CastStyle.DOWSING); var hold=sample(t,CastPhase.HOLD,0,0,false);
        assertEquals(-90,deg(hold.rightArm().x()),1e-3); assertEquals(0,deg(hold.rightArm().z()),1e-3);
        assertTrue(Math.abs(deg(t.loop().amplitude().head().y()))>=2,"head scans");
        for(var phase:CastPhase.values())for(float time=0;time<=Math.max(48,phaseDuration(t,phase,false));time+=.5F) {
            var p=sample(t,phase,time,0,false); String id=phase+" "+time;
            assertEquals(CastingPose.Rotation.ZERO,p.leftArm(),id); assertEquals(0,p.pitch(),1e-6,id);
            assertEquals(CastingPose.Rotation.ZERO,p.rightLeg(),id); assertEquals(CastingPose.Rotation.ZERO,p.leftLeg(),id);
            assertEquals(CastingPose.Rotation.ZERO,p.body(),id);
        }
    }

    @Test void passivesDrawInwardOnAndThrowOutwardOff() {
        for(var style:PASSIVE) {
            var t=ChannelTracks.get(style); String id=style.name();
            assertNull(t.loop(),id);
            assertEquals(16,t.on().duration(),id); assertEquals(12,t.off().duration(),id);
            assertEquals(16,t.firstOn().duration(),id); assertEquals(12,t.firstOff().duration(),id);
            assertEquals(16,phaseDuration(t,CastPhase.RELEASE,true)); assertEquals(12,phaseDuration(t,CastPhase.CANCEL,true));
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RELEASE,0,0,true),id+" on starts at rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.RELEASE,16,0,true),id+" on ends at rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,0,0,true),id+" off starts at rest");
            assertEquals(CastingPose.ZERO,sample(t,CastPhase.CANCEL,12,0,true),id+" off ends at rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.RELEASE,16,0,true),id+" first on ends at rest");
            assertEquals(HandPose.ZERO,sampleFirst(t,CastPhase.CANCEL,12,0,true),id+" first off ends at rest");
            boolean drawn=false,thrown=false,handsIn=false,handsOut=false,differ=false;
            for(int tick=0;tick<=16;tick++) {
                var on=sample(t,CastPhase.RELEASE,tick,0,true);
                drawn|=deg(on.rightArm().z())>20 && deg(on.leftArm().z())<-20 && deg(on.rightArm().x())<-45;
                var fOn=sampleFirst(t,CastPhase.RELEASE,tick,0,true);
                handsIn|=fOn.lead().tx()<-.1F && fOn.support().tx()>.1F;
                if(tick<=12) {
                    var off=sample(t,CastPhase.CANCEL,tick,0,true);
                    thrown|=deg(off.rightArm().z())<=-50 && deg(off.leftArm().z())>=50;
                    var fOff=sampleFirst(t,CastPhase.CANCEL,tick,0,true);
                    handsOut|=fOff.lead().tx()>.1F && fOff.support().tx()<-.1F;
                    differ|=!on.equals(off);
                }
            }
            assertTrue(drawn,id+" on draws both arms to the chest");
            assertTrue(thrown,id+" off throws both arms outward");
            assertTrue(handsIn,id+" first on draws inward");
            assertTrue(handsOut,id+" first off pushes outward");
            assertTrue(differ,id+" on and off differ");
        }
    }
}
