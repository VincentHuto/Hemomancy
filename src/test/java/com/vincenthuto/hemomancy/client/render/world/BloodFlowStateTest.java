package com.vincenthuto.hemomancy.client.render.world;
import com.vincenthuto.hemomancy.common.network.particle.BloodFlowPacket;
import com.vincenthuto.hutoslib.common.tendril.*;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
class BloodFlowStateTest {
    private BloodFlowPacket packet() {
        return new BloodFlowPacket(42,1,BloodFlowPacket.Style.COMMUNION,
                new TendrilAnchor.Point(new Vec3(0,1,4)),new TendrilAnchor.Entity(1,TendrilAnchor.AnchorPoint.EYES,Vec3.ZERO),
                TendrilEffectConfig.defaults().withLifecycle(2,24,8),false);
    }
    @Test void channelRefreshMaintainsCirculationAndStopFadesAtTheLastAnchors() {
        var state=new BloodFlowState(packet(),10);
        assertTrue(state.resolve((id,point,offset)->Optional.of(new Vec3(1,2,0)),12));
        assertEquals(1,state.opacity(15),.001);
        state.refresh(packet(),30);
        assertEquals(10,state.born);assertEquals(1,state.opacity(31),.001);
        state.retire(32);assertFalse(state.resolve((id,point,offset)->Optional.of(new Vec3(9,9,9)),33));
        assertEquals(new Vec3(1,2,0),state.end);
        assertTrue(state.opacity(36)>0 && state.opacity(36)<1);assertTrue(state.finished(40));
    }
    @Test void missingMovingTargetRetiresButPointSourceSurvivesCorpseRemoval() {
        var state=new BloodFlowState(packet(),0);
        assertTrue(state.resolve((id,point,offset)->Optional.of(Vec3.ZERO),3));
        assertEquals(new Vec3(0,1,4),state.start);
        assertFalse(state.resolve((id,point,offset)->Optional.empty(),5));
        assertFalse(state.finished(12));assertTrue(state.finished(13));
    }
    @Test void nonFiniteAnchorNeverReachesGeometry() {
        var state=new BloodFlowState(packet(),0);
        assertFalse(state.resolve((id,point,offset)->Optional.of(new Vec3(Double.NaN,0,0)),1));
        assertNull(state.start);assertTrue(state.finished(9));
    }
    @Test void resumedChannelReattachesDuringThePreviousStopFade() {
        var state=new BloodFlowState(packet(),0);
        state.resolve((id,point,offset)->Optional.of(Vec3.ZERO),2);
        state.retire(10);
        state.refresh(packet(),12);
        assertTrue(state.resolve((id,point,offset)->Optional.of(new Vec3(3,2,1)),13));
        assertEquals(new Vec3(3,2,1),state.end);
        assertFalse(state.finished(18));
    }
}
