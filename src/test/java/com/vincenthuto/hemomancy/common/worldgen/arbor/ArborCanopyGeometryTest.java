package com.vincenthuto.hemomancy.common.worldgen.arbor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArborCanopyGeometryTest {
    @Test
    void familyLimbBraidsAroundTrunkBeforeOpeningIntoCanopy() {
        List<ArborCanopyGeometry.Point> limb = ArborCanopyGeometry.familyLimb(2, 7.4, 4.8);
        assertTrue(limb.size() >= 13);
        assertTrue(limb.get(0).radialDistance() < 0.75);
        assertTrue(limb.get(5).angle() - limb.get(0).angle() > 1.25,
                "the lower limb must visibly coil around the trunk");
        assertTrue(limb.get(limb.size() - 1).radialDistance() > 3.5);
        assertTrue(limb.get(limb.size() - 1).y() > 4.5);
    }

    @Test
    void fruitStemIsShortCurvedAndHanging() {
        ArborCanopyGeometry.Point fruit = new ArborCanopyGeometry.Point(3.2, 4.8, -1.4);
        List<ArborCanopyGeometry.Point> stem = ArborCanopyGeometry.hangingStem(fruit, 1.1, .37);
        assertEquals(6, stem.size());
        assertTrue(stem.get(0).y() > fruit.y());
        assertTrue(stem.get(0).distanceTo(fruit) < 1.6);
        assertEquals(fruit.x(), stem.get(5).x(), 1.0e-6);
        assertEquals(fruit.y(), stem.get(5).y(), 1.0e-6);
    }

    @Test
    void smoothingPreservesEndpointsAndAddsAContinuousCurve() {
        List<ArborCanopyGeometry.Point> path = List.of(
                new ArborCanopyGeometry.Point(0, 0, 0),
                new ArborCanopyGeometry.Point(0, 1, 0),
                new ArborCanopyGeometry.Point(1, 2, 0),
                new ArborCanopyGeometry.Point(2, 2, 1));

        List<ArborCanopyGeometry.Point> smooth = ArborCanopyGeometry.smooth(path, 3);

        assertEquals(path.get(0), smooth.get(0));
        assertEquals(path.get(path.size() - 1), smooth.get(smooth.size() - 1));
        assertEquals((path.size() - 1) * 3 + 1, smooth.size());
        assertTrue(smooth.stream().allMatch(point -> Double.isFinite(point.x())
                && Double.isFinite(point.y()) && Double.isFinite(point.z())));
        assertTrue(smooth.get(4).x() > 0 && smooth.get(4).x() < 1,
                "the former elbow must become an interpolated bend");
    }

    @Test
    void tubeCapRingClosesTheEndPerpendicularToItsBranch() {
        ArborCanopyGeometry.Point center = new ArborCanopyGeometry.Point(2, 3, 4);
        ArborCanopyGeometry.Point tangent = new ArborCanopyGeometry.Point(1, 2, -1);

        List<ArborCanopyGeometry.Point> ring = ArborCanopyGeometry.capRing(center, tangent, .25, 10);

        assertEquals(10, ring.size());
        for (ArborCanopyGeometry.Point point : ring) {
            ArborCanopyGeometry.Point relative = point.subtract(center);
            assertEquals(.25, relative.length(), 1.0e-6);
            assertEquals(0, relative.dot(tangent), 1.0e-6);
        }
    }

    @Test
    void coloredVeinFollowsTheOutsideSurfaceInsteadOfClippingInsideBark() {
        List<ArborCanopyGeometry.Point> branch = List.of(
                new ArborCanopyGeometry.Point(1, 0, 0),
                new ArborCanopyGeometry.Point(2, 1, 0),
                new ArborCanopyGeometry.Point(3, 2, 0));

        List<ArborCanopyGeometry.Point> vein = ArborCanopyGeometry.surfaceVein(
                branch, .19, .045, .027, .010, .004);

        assertEquals(.213, vein.get(0).distanceTo(branch.get(0)), 1.0e-6);
        assertEquals(.051, vein.get(2).distanceTo(branch.get(2)), 1.0e-6);
        assertTrue(vein.get(1).radialDistance() > branch.get(1).radialDistance());
    }

    @Test
    void terminalFoliageSitsBeyondTheCappedBranchEnd() {
        List<ArborCanopyGeometry.Point> branch = List.of(
                new ArborCanopyGeometry.Point(0, 0, 0),
                new ArborCanopyGeometry.Point(0, 1, 0),
                new ArborCanopyGeometry.Point(.4, 2, 0));

        ArborCanopyGeometry.Point foliage = ArborCanopyGeometry.terminalFoliageCenter(branch, .24);
        ArborCanopyGeometry.Point last = branch.get(branch.size() - 1);
        ArborCanopyGeometry.Point outward = last.subtract(branch.get(branch.size() - 2)).normalized();

        assertEquals(.24, foliage.distanceTo(last), 1.0e-6);
        assertTrue(foliage.subtract(last).dot(outward) > 0,
                "terminal leaves must cover the outside face rather than sink into the tube");
    }

    @Test
    void terminalWoodContinuesIntoTheFoliageAttachment() {
        List<ArborCanopyGeometry.Point> branch = List.of(
                new ArborCanopyGeometry.Point(0, 0, 0),
                new ArborCanopyGeometry.Point(.2, 1, 0),
                new ArborCanopyGeometry.Point(.6, 2, .1));

        List<ArborCanopyGeometry.Point> tapered = ArborCanopyGeometry.terminalTaper(branch, .36);
        ArborCanopyGeometry.Point oldEnd = branch.get(branch.size() - 1);
        ArborCanopyGeometry.Point outward = oldEnd.subtract(branch.get(branch.size() - 2)).normalized();

        assertEquals(branch, tapered.subList(0, branch.size()));
        assertEquals(branch.size() + 3, tapered.size());
        assertEquals(.36, tapered.get(tapered.size() - 1).distanceTo(oldEnd), 1.0e-6);
        assertTrue(tapered.get(tapered.size() - 1).subtract(oldEnd).dot(outward) > .35,
                "the wooden tip must reach the foliage bud instead of ending at a flat disk");
        assertTrue(tapered.get(branch.size()).distanceTo(oldEnd)
                        < tapered.get(branch.size() + 1).distanceTo(oldEnd));
    }

    @Test
    void pointedProfileReachesExactlyZeroRadiusAtItsTip() {
        assertEquals(.61F, ArborCanopyGeometry.pointedRadius(0F, .65F, .61F, .22F), 1.0e-6F);
        assertEquals(.22F, ArborCanopyGeometry.pointedRadius(.65F, .65F, .61F, .22F), 1.0e-6F);
        assertTrue(ArborCanopyGeometry.pointedRadius(.90F, .65F, .61F, .22F) > 0F);
        assertEquals(0F, ArborCanopyGeometry.pointedRadius(1F, .65F, .61F, .22F), 1.0e-6F);
        float beforeSlope = (ArborCanopyGeometry.pointedRadius(.65F, .65F, .61F, .22F)
                - ArborCanopyGeometry.pointedRadius(.649F, .65F, .61F, .22F)) / .001F;
        float afterSlope = (ArborCanopyGeometry.pointedRadius(.651F, .65F, .61F, .22F)
                - ArborCanopyGeometry.pointedRadius(.65F, .65F, .61F, .22F)) / .001F;
        assertEquals(beforeSlope, afterSlope, .01F,
                "the taper must inherit the incoming radius slope instead of forming a shoulder");
        float nearTip = ArborCanopyGeometry.pointedRadius(.999F, .65F, .61F, .22F);
        assertTrue(nearTip < .0001F,
                "the eased taper must also arrive smoothly at the mathematical point");
    }

    @Test
    void foliageBudScalesWithoutLeavingTheLeafAttachmentExposed() {
        assertEquals(.07F, ArborCanopyGeometry.foliageBudRadius(.60F, 0F), 1.0e-6F);
        assertEquals(.15F, ArborCanopyGeometry.foliageBudRadius(1.20F, 1F), 1.0e-6F);
    }

    @Test
    void volumetricCrownsBecomeBroaderAndDenserWithFoliageProgress() {
        ArborCanopyGeometry.Point center = new ArborCanopyGeometry.Point(2, 5, -1);

        List<ArborCanopyGeometry.Leaflet> sparse = ArborCanopyGeometry.foliageCrown(center, .60, .10, 17);
        List<ArborCanopyGeometry.Leaflet> full = ArborCanopyGeometry.foliageCrown(center, 1.20, 1.0, 17);

        assertTrue(sparse.size() >= 4, "a wounded crown should retain a few actual leaves");
        assertTrue(full.size() >= 18, "a healthy crown should read as a substantial canopy mass");
        assertTrue(full.size() > sparse.size());
        assertTrue(crownSpan(full) >= 1.0 && crownSpan(full) <= 1.3,
                "healthy crowns should be roughly 1.2 blocks wide");
        assertTrue(full.stream().allMatch(leaf -> leaf.center().distanceTo(center) <= .22),
                "leaf bases must overlap into a connected rosette instead of floating on a shell");
        assertTrue(full.stream().mapToDouble(ArborCanopyGeometry.Leaflet::length).average().orElse(0) >= .34,
                "leaf blades must be long enough to read as foliage rather than pebbles");
        assertTrue(full.stream().allMatch(leaf -> leaf.thickness() < leaf.width() * .35),
                "leaf blades need a thin profile rather than a stone-like ellipsoid");
        assertTrue(full.stream().anyMatch(leaf -> Math.abs(leaf.curl()) >= .14),
                "a crown needs visibly curved layers instead of one horizontal plane");
        assertTrue(full.stream().anyMatch(leaf -> leaf.direction().y() > .45));
        assertTrue(full.stream().anyMatch(leaf -> leaf.direction().y() < -.12));
    }

    private static double crownSpan(List<ArborCanopyGeometry.Leaflet> leaves) {
        double min = leaves.stream().mapToDouble(leaf -> leaf.center().x() - leaf.length()).min().orElse(0);
        double max = leaves.stream().mapToDouble(leaf -> leaf.center().x() + leaf.length()).max().orElse(0);
        return max - min;
    }

    @Test
    void fruitFamiliesHaveDistinctSilhouettes() {
        assertEquals(6, ArborFruitGeometry.familyProfiles().size());
        assertNotEquals(ArborFruitGeometry.profile("core"), ArborFruitGeometry.profile("living_staff"));
        assertEquals(5, ArborFruitGeometry.profile("summons").lobes());
        assertTrue(ArborFruitGeometry.profile("mycelial").capWidth() >
                ArborFruitGeometry.profile("mycelial").bodyWidth());
        assertEquals(2, ArborFruitGeometry.profile("covenant").lobes());
    }
}
