package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QliphothBloomSnapshotTest {
    @Test
    void everyAuthoredStageHasOneImmutableSnapshotIdentity() {
        for (int stage = 0; stage <= 9; stage++) {
            QliphothBloomGeometry.StageSnapshot snapshot = QliphothBloomGeometry.snapshot(stage);
            assertEquals(stage, snapshot.stage());
            assertSame(snapshot, QliphothBloomGeometry.snapshot(stage));
        }
    }

    @Test
    void stageEightSnapshotContainsOneStableWoodLayout() {
        QliphothBloomGeometry.StageSnapshot snapshot = QliphothBloomGeometry.snapshot(8);

        assertEquals(8, snapshot.stage());
        assertEquals(8, snapshot.roots().size());
        assertEquals(7, snapshot.mainBranches().size());
        assertEquals(7, snapshot.secondaryBranches().size());
        assertEquals(0, snapshot.crownProngs().size());
        assertSame(snapshot, QliphothBloomGeometry.snapshot(8));
    }

    @Test
    void stageNineSnapshotIncludesApexCrownProngs() {
        assertEquals(5, QliphothBloomGeometry.snapshot(9).crownProngs().size());
    }

    @Test
    void snapshotAndLimbCollectionsAreImmutable() {
        QliphothBloomGeometry.StageSnapshot snapshot = QliphothBloomGeometry.snapshot(9);

        assertThrows(UnsupportedOperationException.class, () -> snapshot.mainBranches().clear());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.trunk().points().clear());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.trunk().radii().clear());
    }
}
