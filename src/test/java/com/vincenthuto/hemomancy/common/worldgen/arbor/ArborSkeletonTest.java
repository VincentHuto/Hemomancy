package com.vincenthuto.hemomancy.common.worldgen.arbor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborSkeletonTest {
    @Test
    void boughGrowsOutOfTheHeartwoodThroughItsCrestAndEveryVisibleStation() {
        for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
            List<ArborCanopyGeometry.Point> bough = ArborSkeleton.bough(family, 7, 7);
            assertTrue(bough.get(0).radialDistance() < ArborSkeleton.TRUNK_SURFACE_RADIUS,
                    "family " + family + " bough must start inside the trunk");
            ArborCanopyGeometry.Point crest = ArborSkeleton.crest(family, 7);
            assertTrue(bough.stream().anyMatch(p -> p.distanceTo(crest) < 1.0e-9), "family " + family + " skips its crest");
            for (int whorl = 1; whorl <= 7; whorl++) {
                ArborCanopyGeometry.Point station = ArborSkeleton.station(family, whorl, 7);
                assertTrue(bough.stream().anyMatch(p -> p.distanceTo(station) < 1.0e-9),
                        "family " + family + " bough skips whorl " + whorl);
            }
            ArborCanopyGeometry.Point tip = bough.get(bough.size() - 1);
            assertTrue(tip.radialDistance() > ArborSkeleton.station(family, 7, 7).radialDistance());
        }
    }

    @Test
    void boughsSpringFromTheTopOfTheTrunkWithNoSpireAboveThem() {
        for (int degree = 1; degree <= 8; degree++) {
            double highestCrest = 0;
            for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
                double peel = ArborSkeleton.peelHeight(family, degree);
                assertTrue(peel < ArborSkeleton.trunkTop(degree), "D" + degree + " family " + family + " peels above its trunk");
                assertTrue(peel > ArborSkeleton.trunkTop(degree) * .8,
                        "D" + degree + " family " + family + " must leave from the crown, not the lower trunk");
                highestCrest = Math.max(highestCrest, ArborSkeleton.crest(family, degree).y());
            }
            assertTrue(ArborSkeleton.trunkTop(degree) < highestCrest, "D" + degree + " trunk rises above its crown");
        }
    }

    @Test
    void boughsArchOutFromTheCrownThenWeepDown() {
        for (int degree = 1; degree <= 7; degree++) {
            for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
                ArborCanopyGeometry.Point crest = ArborSkeleton.crest(family, degree);
                assertTrue(crest.y() > ArborSkeleton.peelHeight(family, degree), "bough must rise before it weeps");
                ArborCanopyGeometry.Point previous = crest;
                for (int whorl = 1; whorl <= Math.min(7, degree); whorl++) {
                    ArborCanopyGeometry.Point station = ArborSkeleton.station(family, whorl, degree);
                    assertTrue(station.radialDistance() > previous.radialDistance());
                    assertTrue(station.y() < previous.y(), "D" + degree + " family " + family + " whorl " + whorl
                            + " must hang lower than the point before it");
                    previous = station;
                }
                List<ArborCanopyGeometry.Point> bough = ArborSkeleton.bough(family, degree, degree);
                assertTrue(bough.get(bough.size() - 1).y() < previous.y(), "bough tips droop below their last station");
            }
        }
    }

    @Test
    void canopyRisesWithTheTreeAndStopsAtTheGrownCrown() {
        for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
            for (int degree = 2; degree <= 7; degree++) {
                assertTrue(ArborSkeleton.peelHeight(family, degree) > ArborSkeleton.peelHeight(family, degree - 1));
            }
            assertEquals(ArborSkeleton.peelHeight(family, 7), ArborSkeleton.peelHeight(family, 8), 1.0e-9);
        }
        assertTrue(ArborOfWillVisualRules.treeHeight(7) >= 10.0, "the grown Arbor must read as a tree, not a shrub");
    }

    @Test
    void boughsLeaveTheTrunkAtStaggeredHeights() {
        for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
            int next = (family + 1) % ArborSkeleton.FAMILY_COUNT;
            assertTrue(Math.abs(ArborSkeleton.peelHeight(family, 7) - ArborSkeleton.peelHeight(next, 7)) >= 0.15,
                    "neighbouring boughs " + family + " and " + next + " peel at the same height");
        }
    }

    @Test
    void spursGrowOutwardFromTheirStationAndFruitHangsBelowTheSpur() {
        for (int family = 0; family < ArborSkeleton.FAMILY_COUNT; family++) {
            for (int whorl = 1; whorl <= 7; whorl++) {
                ArborCanopyGeometry.Point station = ArborSkeleton.station(family, whorl, 7);
                ArborCanopyGeometry.Point tip = ArborSkeleton.spurTip(family, whorl, 7);
                assertTrue(tip.radialDistance() > station.radialDistance());
                for (int slot = 0; slot < 8; slot++) {
                    ArborCanopyGeometry.Point fruit = ArborSkeleton.fruitAnchor(family, whorl, slot, 8, 7);
                    assertTrue(fruit.y() < tip.y() - 0.3, "fruit must hang below its spur");
                }
            }
        }
    }
}
