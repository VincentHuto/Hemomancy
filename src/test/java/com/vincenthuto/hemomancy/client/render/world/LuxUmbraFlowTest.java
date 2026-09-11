package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.network.particle.ManipulationFlowPacket;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LuxUmbraFlowTest {
    @Test void payloadRetainsMovingAndSurfaceAnchorsAndLifecycle() {
        var config = TendrilEffectConfig.defaults().withLifecycle(6, 12, 10)
                .withFixedSeed(true, 71).withMode(TendrilEffectConfig.Mode.SURFACE);
        var original = new ManipulationFlowPacket(true, entity(4), new TendrilAnchor.Point(new Vec3(3, 2, 1)), config);
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ManipulationFlowPacket.STREAM_CODEC.encode(buf, original);
            var decoded = ManipulationFlowPacket.STREAM_CODEC.decode(buf);
            assertEquals(original, decoded);
            assertEquals(0, buf.readableBytes());
        } finally { buf.release(); }
    }

    @Test void connectionFollowsBothEntitiesThenDissolvesAtItsLastPosition() {
        Map<Integer, Vec3> positions = new HashMap<>(Map.of(1, Vec3.ZERO, 2, new Vec3(0, 2, 3)));
        TendrilAnchor.EntityResolver resolver = (id, anchor, offset) -> Optional.ofNullable(positions.get(id));
        var state = new LuxUmbraFlow(new ManipulationFlowPacket(false, entity(1), entity(2),
                TendrilEffectConfig.defaults().withLifecycle(2, 30, 10)), 0);
        assertTrue(state.resolve(resolver, 0));
        positions.put(1, new Vec3(4, 0, 0));
        positions.put(2, new Vec3(4, 2, 3));
        assertTrue(state.resolve(resolver, 10));
        assertEquals(positions.get(1), state.start);
        assertEquals(positions.get(2), state.end);
        positions.remove(2);
        assertFalse(state.resolve(resolver, 11));
        assertEquals(new Vec3(4, 2, 3), state.end);
        assertTrue(state.opacity(15) > 0 && state.opacity(15) < state.opacity(11));
        assertTrue(state.finished(19));
    }

    @Test void selfTargetAndUnresolvedEntitiesDoNotCreateInvalidConnections() {
        var config = TendrilEffectConfig.defaults();
        var state = new LuxUmbraFlow(new ManipulationFlowPacket(false, entity(1), entity(1), config), 0);
        assertTrue(state.resolve((id, anchor, offset) -> Optional.of(Vec3.ZERO), 0));
        assertEquals(state.start, state.end);
        var missing = new LuxUmbraFlow(new ManipulationFlowPacket(false, entity(1), entity(2), config), 0);
        assertFalse(missing.resolve((id, anchor, offset) -> Optional.empty(), 0));
        assertNull(missing.start);
        assertTrue(missing.finished(8));
    }

    private static TendrilAnchor entity(int id) {
        return new TendrilAnchor.Entity(id, TendrilAnchor.AnchorPoint.CENTER, Vec3.ZERO);
    }
}
