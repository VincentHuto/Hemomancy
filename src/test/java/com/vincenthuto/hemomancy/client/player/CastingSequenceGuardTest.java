package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class CastingSequenceGuardTest {
    @Test void expiredVisualDoesNotForgetTerminalOrdering() {
        var order=new CastingSequenceGuard();
        var id=UUID.randomUUID();
        assertTrue(order.accept(id,5,CastPhase.CANCEL,110,110,5,40,110));
        assertFalse(order.accept(id,5,CastPhase.HOLD,100,115,10,40,145));
        assertFalse(order.accept(id,5,CastPhase.CANCEL,110,110,5,40,145));
        assertTrue(order.accept(id,6,CastPhase.OPENING,146,146,1,40,146));
    }
    @Test void freshTrackingCanRestoreAnOngoingSnapshot() {
        var order=new CastingSequenceGuard();
        var id=UUID.randomUUID();
        assertTrue(order.accept(id,5,CastPhase.HOLD,100,120,0,0,120));
        assertTrue(order.accept(id,5,CastPhase.HOLD,100,120,0,0,125));
        order.clear();
        assertTrue(order.accept(id,1,CastPhase.OPENING,0,0,1,40,0));
    }
}
