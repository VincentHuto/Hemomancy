package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QliphothBloomGeometryTest {
    @Test
    void growthMilestonesExposeTheAuthoredStructure() {
        assertEquals(0, QliphothBloomGeometry.mainBranches(5, 0).size());
        assertEquals(7, QliphothBloomGeometry.mainBranches(6, 0).size());
        assertEquals(0, QliphothBloomGeometry.crystals(6, 0).size());
        assertEquals(3, QliphothBloomGeometry.crystals(7, 0).size());
        assertEquals(6, QliphothBloomGeometry.crystals(8, 0).size());
        assertEquals(9, QliphothBloomGeometry.crystals(9, 0).size());
        for (int stage = 0; stage <= 9; stage++) {
            assertEquals(stage, QliphothBloomGeometry.pomes(stage, 0).size());
        }
        assertFalse(QliphothBloomGeometry.hasApex(8));
        assertTrue(QliphothBloomGeometry.hasApex(9));
    }

    @Test
    void rootsAndBranchesAreContinuousPointedTubes() {
        List<QliphothBloomGeometry.Limb> roots = QliphothBloomGeometry.roots(9, 30);
        assertEquals(8, roots.size());
        for (QliphothBloomGeometry.Limb root : roots) {
            assertTrue(root.points().get(0).radialDistance() < .48);
            assertEquals(0, root.radii().get(root.radii().size() - 1), 1.0e-9);
            assertMonotonic(root.radii());
        }
        for (QliphothBloomGeometry.Limb branch : QliphothBloomGeometry.mainBranches(9, 30)) {
            assertTrue(branch.points().get(0).radialDistance() < .43,
                    "branch shoulder must begin inside the trunk");
            assertEquals(0, branch.radii().get(branch.radii().size() - 1), 1.0e-9);
            assertMonotonic(branch.radii());
        }
    }

    @Test
    void motionIsDeterministicBoundedAndKeepsShouldersFixed() {
        QliphothBloomGeometry.Limb a = QliphothBloomGeometry.mainBranches(9, 417.25).get(3);
        QliphothBloomGeometry.Limb b = QliphothBloomGeometry.mainBranches(9, 417.25).get(3);
        QliphothBloomGeometry.Limb rest = QliphothBloomGeometry.mainBranches(9, 0).get(3);
        assertEquals(a, b);
        assertEquals(rest.points().get(0), a.points().get(0));
        for (int i = 0; i < a.points().size(); i++) {
            assertTrue(a.points().get(i).distanceTo(rest.points().get(i)) <= .24,
                    "writhe plus twitch must remain restrained");
        }
    }

    @Test
    void veinsRemainOutsideTheirWoodSurface() {
        QliphothBloomGeometry.Limb branch = QliphothBloomGeometry.mainBranches(9, 80).get(2);
        List<QliphothBloomGeometry.Point> vein = QliphothBloomGeometry.surfaceVein(branch, 2);
        assertEquals(branch.points().size(), vein.size());
        for (int i = 0; i < vein.size() - 1; i++) {
            assertTrue(vein.get(i).distanceTo(branch.points().get(i)) >= branch.radii().get(i) + .012);
        }
    }

    @Test
    void apexLoopsAreClosedStableAndDifferentlyTilted() {
        List<QliphothBloomGeometry.Limb> loops = QliphothBloomGeometry.apexLoops(135.5);
        assertEquals(4, loops.size());
        assertNotEquals(loops.get(0).points(), loops.get(1).points());
        for (QliphothBloomGeometry.Limb loop : loops) {
            assertEquals(loop.points().get(0), loop.points().get(loop.points().size() - 1));
            assertTrue(loop.points().stream().allMatch(Point -> Double.isFinite(Point.x())
                    && Double.isFinite(Point.y()) && Double.isFinite(Point.z())));
        }
    }

    @Test
    void matureTrunkTapersIntoTheBottomOfTheEventHorizon() {
        QliphothBloomGeometry.Limb trunk = QliphothBloomGeometry.trunk(9, 0);
        int tip = trunk.points().size() - 1;
        double gapBelowCenter = QliphothBloomGeometry.apexCenter(0).y() - trunk.points().get(tip).y();

        assertEquals(0, trunk.radii().get(tip), 1.0e-9);
        assertTrue(gapBelowCenter >= .50 && gapBelowCenter <= .65,
                "trunk tip should meet the lower event horizon instead of crossing its face");
    }

    @Test
    void matureCrownProngsStopBelowTheEventHorizon() {
        double centerY = QliphothBloomGeometry.apexCenter(0).y();
        for (QliphothBloomGeometry.Limb prong : QliphothBloomGeometry.crownProngs(0)) {
            int tip = prong.points().size() - 1;
            assertTrue(centerY - prong.points().get(tip).y() >= .50,
                    "crown prongs must not pierce the event horizon");
            assertEquals(0, prong.radii().get(tip), 1.0e-9);
        }
    }

    @Test
    void angularLimbsUseShortBevelsInsteadOfIntersectingElbowRings() {
        QliphothBloomGeometry.Limb branch = QliphothBloomGeometry.mainBranches(9, 0).get(0);
        assertTrue(branch.points().size() >= 8, "authored corners need approach and exit rings");
        assertTrue(maxTurn(branch.points()) < Math.toRadians(72),
                "a single severe ring turn creates the visible fins and disconnected flares");
        assertEquals(0, branch.radii().get(branch.radii().size() - 1), 1.0e-9);
    }

    @Test
    void memorialPomesHaveSeededAsymmetricEtherealProfiles() {
        List<QliphothBloomGeometry.Pome> pomes = QliphothBloomGeometry.pomes(9, 0);
        assertEquals(9, pomes.size());
        assertTrue(pomes.stream().map(QliphothBloomGeometry.Pome::aspectY).distinct().count() >= 5);
        assertTrue(pomes.stream().allMatch(pome -> pome.aspectX() >= .72 && pome.aspectX() <= 1.28));
        assertTrue(pomes.stream().allMatch(pome -> pome.wispPhase() >= 0 && pome.wispPhase() < Math.PI * 2));
    }

    @Test
    void bloodFruitsVaryInShapeInsteadOfRepeatingOneDiamond() {
        List<QliphothBloomGeometry.Crystal> fruits = QliphothBloomGeometry.crystals(9, 0);
        assertEquals(9, fruits.size());
        assertTrue(fruits.stream().map(QliphothBloomGeometry.Crystal::lobes).distinct().count() >= 3);
        assertTrue(fruits.stream().map(QliphothBloomGeometry.Crystal::skew).distinct().count() >= 6);
    }

    @Test
    void apexVeinsAreThinAndActuallyChangeShapeWhileSwimming() {
        List<QliphothBloomGeometry.Limb> first = QliphothBloomGeometry.apexLoops(20);
        List<QliphothBloomGeometry.Limb> later = QliphothBloomGeometry.apexLoops(55);
        assertTrue(first.stream().flatMap(loop -> loop.radii().stream()).allMatch(radius -> radius <= .026));
        double firstChord = first.get(0).points().get(3).distanceTo(first.get(0).points().get(12));
        double laterChord = later.get(0).points().get(3).distanceTo(later.get(0).points().get(12));
        assertNotEquals(firstChord, laterChord, 1.0e-3,
                "writhing must deform the loop, not just rigidly rotate it");
    }

    @Test
    void rootsHaveMultipleViolentAlternatingKinks() {
        List<QliphothBloomGeometry.Limb> roots = QliphothBloomGeometry.roots(9, 0);
        assertTrue(roots.stream().map(root -> root.points().size()).distinct().count() >= 3,
                "seeded roots must not all share one joint template");
        assertTrue(roots.stream().map(root -> Math.round(root.points().get(root.points().size() - 1).radialDistance() * 10))
                .distinct().count() >= 5, "root reach needs visible seeded variation");
        for (QliphothBloomGeometry.Limb root : roots) {
            assertTrue(root.points().size() >= 10, "roots need enough authored joints to look violently jagged");
            int directionChanges = 0;
            double previous = 0;
            for (int i = 1; i < root.points().size() - 1; i++) {
                QliphothBloomGeometry.Point a = root.points().get(i).subtract(root.points().get(i - 1));
                QliphothBloomGeometry.Point b = root.points().get(i + 1).subtract(root.points().get(i));
                double crossY = a.z() * b.x() - a.x() * b.z();
                if (i > 1 && Math.signum(crossY) != Math.signum(previous) && Math.abs(crossY) > .002) directionChanges++;
                if (Math.abs(crossY) > .002) previous = crossY;
            }
            assertTrue(directionChanges >= 1, "each root should slash around instead of radiating straight out");
        }
    }

    @Test
    void fruitStemsHaveAConnectedCollarAndSeveralBentSegments() {
        for (QliphothBloomGeometry.Crystal fruit : QliphothBloomGeometry.crystals(9, 40)) {
            QliphothBloomGeometry.Limb stem = QliphothBloomGeometry.fruitStem(fruit, 40);
            assertEquals(fruit.anchor(), stem.points().get(0));
            assertEquals(fruit.center(), stem.points().get(stem.points().size() - 1));
            assertTrue(stem.points().size() >= 5);
            assertTrue(stem.radii().get(0) >= .055, "branch attachment needs a visible collar");
            assertTrue(stem.radii().get(stem.radii().size() - 1) >= .025, "stem must visibly enter the fruit");
        }
    }

    @Test
    void barkTextureCoordinatesWrapAndAdvanceAlongTheTrunk() {
        QliphothBloomGeometry.Limb trunk = QliphothBloomGeometry.trunk(9, 12);
        List<Double> v = QliphothBloomGeometry.textureV(trunk, 2.4);
        assertEquals(trunk.points().size(), v.size());
        assertEquals(0, v.get(0), 1.0e-9);
        assertTrue(v.get(v.size() - 1) > 12, "bark grain should repeat along the full height");
        for (int i = 1; i < v.size(); i++) assertTrue(v.get(i) > v.get(i - 1));
    }

    private static double maxTurn(List<QliphothBloomGeometry.Point> points) {
        double maximum = 0;
        for (int i = 1; i < points.size() - 1; i++) {
            QliphothBloomGeometry.Point incoming = points.get(i).subtract(points.get(i - 1)).normalized();
            QliphothBloomGeometry.Point outgoing = points.get(i + 1).subtract(points.get(i)).normalized();
            double dot = Math.max(-1, Math.min(1, incoming.x() * outgoing.x()
                    + incoming.y() * outgoing.y() + incoming.z() * outgoing.z()));
            maximum = Math.max(maximum, Math.acos(dot));
        }
        return maximum;
    }

    private static void assertMonotonic(List<Double> radii) {
        for (int i = 1; i < radii.size(); i++) {
            assertTrue(radii.get(i) <= radii.get(i - 1) + 1.0e-9);
        }
    }
}
