package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ConductorGraphTest {
    @Test void graphConnectsFacesOnlyAndDoesNotCrossUnloadedOrBrokenNodes() {
        Set<BlockPos> blocks = new HashSet<>(Set.of(BlockPos.ZERO, new BlockPos(1,0,0),
                new BlockPos(2,0,0), new BlockPos(3,0,0), new BlockPos(1,1,1)));
        assertEquals(3, ConductorGraph.collect(BlockPos.ZERO,
                p -> p.getX() < 3, blocks::contains).size());
        blocks.remove(new BlockPos(1,0,0));
        assertEquals(Set.of(BlockPos.ZERO), new HashSet<>(ConductorGraph.collect(BlockPos.ZERO,
                p -> true, blocks::contains)));
    }

    @Test void graphCapsNodesAndRadiusBeforeReadingWorldState() {
        var nodes = ConductorGraph.collect(BlockPos.ZERO, p -> true, p -> {
            assertTrue(p.distSqr(BlockPos.ZERO) <= 144);
            return true;
        });
        assertEquals(256, nodes.size());
        assertEquals(256, new HashSet<>(nodes).size());
        assertEquals(13,ConductorGraph.collect(BlockPos.ZERO,p->true,
                p->p.getX()>=0&&p.getY()==0&&p.getZ()==0).size());
        assertTrue(ConductorGraph.collect(BlockPos.ZERO, p -> false,
                p -> { fail("Unloaded node queried"); return true; }).isEmpty());
    }
}
