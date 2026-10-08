package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class AxonalTravelPathTest {
    private static final Vec3 EAST = new Vec3(1, 0, 0);
    private static class Network implements AxonalTravelPath.Network {
        final Set<BlockPos> cells = new HashSet<>();
        final Set<BlockPos> nodes = new HashSet<>();
        Network(BlockPos... positions) { cells.addAll(Set.of(positions)); nodes.add(positions[0]); }
        public boolean contains(BlockPos p) { return cells.contains(p); }
        public boolean node(BlockPos p) { return nodes.contains(p); }
    }
    private static BlockPos p(int x, int y, int z) { return new BlockPos(x, y, z); }

    @Test void fullSpeedChecksTheFirstNodeInsteadOfSkippingIt() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0), p(3,0,0)); n.nodes.add(p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0));
        assertEquals(AxonalTravelPath.Result.NODE, path.advance(n, 4, 1, EAST));
        assertEquals(Vec3.atCenterOf(p(2,0,0)), path.position());
    }
    @Test void bendsDoNotTakeDiagonalShortcutsOrRequireFramePerfectSteering() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(1,0,1), p(1,1,1));
        var path = new AxonalTravelPath(p(0,0,0));
        path.advance(n, 3, 1, EAST);
        assertEquals(Vec3.atCenterOf(p(1,1,1)), path.position());
    }
    @Test void isolatedDiagonalFibersRemainConnected() {
        Network n = new Network(p(0,0,0), p(1,1,1), p(2,2,2));
        var path = new AxonalTravelPath(p(0,0,0));
        path.advance(n, Math.sqrt(3), 1, EAST);
        assertEquals(Vec3.atCenterOf(p(1,1,1)), path.position());
    }
    @Test void releaseStopsAndReversalWithinAnEdgeReturnsToTheSource() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0)); path.advance(n, 0.4, 1, EAST);
        Vec3 parked = path.position(); path.advance(n, 10, 0, EAST); assertEquals(parked, path.position());
        assertEquals(AxonalTravelPath.Result.NODE, path.advance(n, 0.4, -1, EAST));
        assertEquals(Vec3.atCenterOf(p(0,0,0)), path.position());
    }
    @Test void turningTheCameraReversesForwardWithinAnEdge() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0));
        path.advance(n, 0.4, 1, EAST);
        assertEquals(AxonalTravelPath.Result.NODE, path.advance(n, 0.4, 1, EAST.reverse()));
        assertEquals(Vec3.atCenterOf(p(0,0,0)), path.position());
    }
    @Test void turningTheCameraAtACellCenterSelectsThePreviousFiber() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0));
        path.advance(n, 1, 1, EAST);
        assertEquals(AxonalTravelPath.Result.NODE, path.advance(n, 1, 1, EAST.reverse()));
        assertEquals(Vec3.atCenterOf(p(0,0,0)), path.position());
    }
    @Test void backwardTravelsAwayFromTheCameraAfterTurningAround() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0));
        path.advance(n, 0.4, 1, EAST);
        path.advance(n, 0.4, -1, EAST.reverse());
        assertEquals(1.3, path.position().x, 1.0E-7);
    }
    @Test void heldForwardKeepsReversingOnVerticalAndDiagonalRuns() {
        for (BlockPos offset : java.util.List.of(p(0,1,0), p(1,1,1))) {
            Network n = new Network(p(0,0,0), offset, offset.multiply(2));
            Vec3 aim = Vec3.atLowerCornerOf(offset).normalize();
            var path = new AxonalTravelPath(p(0,0,0));
            path.advance(n, 0.6, 1, aim);
            path.advance(n, 0.2, 1, aim.reverse());
            path.advance(n, 0.2, 1, aim.reverse());
            assertEquals(0.2, path.position().distanceTo(new Vec3(0.5,0.5,0.5)), 1.0E-7);
        }
    }
    @Test void entryDoesNotDepartAlongAConnectionOppositeTheRequestedView() {
        Network n = new Network(p(0,0,0), p(1,0,0));
        var path = new AxonalTravelPath(p(0,0,0));
        assertEquals(AxonalTravelPath.Result.STOPPED, path.advance(n, 1, -1, EAST));
        assertEquals(new Vec3(0.5,0.5,0.5), path.position());
    }
    @Test void aBlockedEndpointCanBeReversedByLookingBackAndHoldingForward() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0), p(3,0,0)); n.nodes.add(p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0));
        path.advance(n, 2, 1, EAST);
        path.advance(n, 1, 1, EAST.reverse());
        assertEquals(Vec3.atCenterOf(p(1,0,0)), path.position());
    }
    @Test void aForkPausesUntilThePlayerAimsAtABranch() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0), p(1,0,1));
        var path = new AxonalTravelPath(p(0,0,0)); path.advance(n, 1, 1, EAST);
        assertEquals(AxonalTravelPath.Result.STOPPED, path.advance(n, 1, 1, new Vec3(1,0,1)));
        path.advance(n, 1, 1, new Vec3(0,0,1));
        assertEquals(Vec3.atCenterOf(p(1,0,1)), path.position());
    }
    @Test void removingTheNextCellStopsAndAllowsBacktracking() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0)); path.advance(n, 1.4, 1, EAST);
        n.cells.remove(p(2,0,0)); path.advance(n, 1, 1, EAST);
        assertEquals(Vec3.atCenterOf(p(1,0,0)), path.position());
        assertEquals(AxonalTravelPath.Result.NODE, path.advance(n, 1, -1, EAST));
    }
    @Test void gapsAndUnloadedCellsCannotBeCrossed() {
        Network n = new Network(p(0,0,0), p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0)); path.advance(n, 10, 1, EAST);
        assertEquals(Vec3.atCenterOf(p(0,0,0)), path.position());
        n.cells.remove(p(0,0,0)); assertEquals(AxonalTravelPath.Result.LOST, path.advance(n, 1, 1, EAST));
    }
    @Test void aNewIntermediateNodeInvalidatesAnAlreadySelectedDiagonal() {
        Network n = new Network(p(0,0,0),p(1,1,0));
        var path = new AxonalTravelPath(p(0,0,0)); path.advance(n,.3,1,EAST);
        n.cells.add(p(1,0,0)); n.nodes.add(p(1,0,0));
        assertEquals(AxonalTravelPath.Result.STOPPED,path.advance(n,2,1,EAST));
        assertEquals(Vec3.atCenterOf(p(0,0,0)),path.position());
        assertEquals(AxonalTravelPath.Result.NODE,path.advance(n,2,1,EAST));
        assertEquals(Vec3.atCenterOf(p(1,0,0)),path.position());
    }
    @Test void aBlockedEndpointCanBeReversedWithoutAnAutomaticForwardDeparture() {
        Network n = new Network(p(0,0,0), p(1,0,0), p(2,0,0), p(3,0,0)); n.nodes.add(p(2,0,0));
        var path = new AxonalTravelPath(p(0,0,0)); path.advance(n, 2, 1, EAST);
        assertEquals(AxonalTravelPath.Result.NODE, path.advance(n, 2, 1, EAST));
        path.advance(n, 1, -1, EAST); assertEquals(Vec3.atCenterOf(p(1,0,0)), path.position());
    }
}
