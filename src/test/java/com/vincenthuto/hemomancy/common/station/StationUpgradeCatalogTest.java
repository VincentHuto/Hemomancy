package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StationUpgradeCatalogTest {
    @Test
    void everyStationHasExactlyTwoSequentialTiers() {
        for (UpgradeStation station : UpgradeStation.values()) {
            assertEquals(1, StationUpgradeCatalog.get(station, 1).tier());
            assertEquals(2, StationUpgradeCatalog.get(station, 2).tier());
            assertTrue(StationUpgradeCatalog.get(station, 2).requiredDegree()
                    > StationUpgradeCatalog.get(station, 1).requiredDegree(), station.name());
        }
        assertEquals(14, StationUpgradeCatalog.all().size());
    }

    @Test
    void alchemistStationsUseFourAndSixAndOthersUseFiveAndSeven() {
        assertNotNull(UpgradeStation.byName("centrifuge"));
        for (UpgradeStation station : new UpgradeStation[]{UpgradeStation.ALEMBIC, UpgradeStation.byName("centrifuge")}) {
            assertEquals(4, StationUpgradeCatalog.get(station, 1).requiredDegree());
            assertEquals(6, StationUpgradeCatalog.get(station, 2).requiredDegree());
        }
        for (UpgradeStation station : new UpgradeStation[]{UpgradeStation.RESONANT_FORGE,
                UpgradeStation.ARMATURE, UpgradeStation.SCRIPTORIUM}) {
            assertEquals(5, StationUpgradeCatalog.get(station, 1).requiredDegree(), station.name());
            assertEquals(7, StationUpgradeCatalog.get(station, 2).requiredDegree(), station.name());
        }
    }

    @Test
    void theLoomGrowsAtTheDegreesThatTeachItsRanks() {
        UpgradeStation loom = UpgradeStation.byName("loom");
        assertNotNull(loom);
        assertEquals(ResourceLocation.parse("hemomancy:somatic_loom"), loom.blockId());
        StationUpgradeTier ganglionic = StationUpgradeCatalog.get(loom, 1);
        StationUpgradeTier cortical = StationUpgradeCatalog.get(loom, 2);
        assertEquals(5, ganglionic.requiredDegree());
        assertEquals(6, cortical.requiredDegree());
        assertEquals("cardinal_rite/ganglionic_weave", ganglionic.ritePath());
        assertEquals("cardinal_rite/cortical_weave", cortical.ritePath());
        assertEquals(ResourceLocation.parse("hemomancy:ganglionic_harness"), ganglionic.upgradeItem());
        assertEquals(ResourceLocation.parse("hemomancy:cortical_crown"), cortical.upgradeItem());
        assertEquals(java.util.List.of(StationUpgradeCatalog.LOOM_WEAVE), ganglionic.requiredUsage());
        assertEquals(java.util.List.of(StationUpgradeCatalog.LOOM_WEAVE_GANGLIONIC), cortical.requiredUsage());
        assertTrue(ganglionic.dialogueKey().startsWith("hemomancy.mnemonist.loom."));
    }

    @Test
    void theCondenserGrowsAtThreeAndFive() {
        UpgradeStation condenser = UpgradeStation.byName("condenser");
        assertNotNull(condenser);
        assertEquals(ResourceLocation.parse("hemomancy:sanguine_condenser"), condenser.blockId());
        assertEquals(1, condenser.seatDistance());
        StationUpgradeTier lattice = StationUpgradeCatalog.get(condenser, 1);
        StationUpgradeTier druse = StationUpgradeCatalog.get(condenser, 2);
        assertEquals(3, lattice.requiredDegree());
        assertEquals(5, druse.requiredDegree());
        assertEquals(ResourceLocation.parse("hemomancy:clotting_lattice"), lattice.upgradeItem());
        assertEquals(ResourceLocation.parse("hemomancy:druse_heart"), druse.upgradeItem());
        assertEquals("cardinal_rite/first_clotting", lattice.ritePath());
        assertEquals("cardinal_rite/druse_crystallization", druse.ritePath());
        assertEquals(java.util.List.of(StationUpgradeCatalog.CONDENSE), lattice.requiredUsage());
        assertEquals(java.util.List.of(StationUpgradeCatalog.CONDENSE_LATTICED), druse.requiredUsage());
        assertEquals("lesser", lattice.riteType());
        assertEquals("greater", druse.riteType());
        assertTrue(lattice.dialogueKey().startsWith("hemomancy.vicar.condenser."));
    }

    @Test
    void upgradeItemsAreUniquePerTier() {
        Set<ResourceLocation> items = new HashSet<>();
        for (StationUpgradeTier tier : StationUpgradeCatalog.all())
            assertTrue(items.add(tier.upgradeItem()), "shared upgrade item " + tier.upgradeItem());
        assertEquals(ResourceLocation.parse("hemomancy:rubricators_quill"),
                StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1).upgradeItem());
        assertEquals(ResourceLocation.parse("hemomancy:palimpsest_burin"),
                StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 2).upgradeItem());
    }

    @Test
    void ritesResolveBackToTheirTier() {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            assertEquals(tier, StationUpgradeCatalog.forRite(tier.rite()).orElseThrow());
            assertTrue(StationUpgradeCatalog.isUpgradeRitePath(tier.ritePath()));
        }
        assertEquals("cardinal_rite/palimpsest", StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 2).ritePath());
        assertTrue(StationUpgradeCatalog.forRite(ResourceLocation.parse("hemomancy:cardinal_rite/archon_rite")).isEmpty());
        assertTrue(StationUpgradeCatalog.forRite(null).isEmpty());
    }

    @Test
    void ceremonyShapeIsStandardAcrossStations() {
        for (StationUpgradeTier tier : StationUpgradeCatalog.all()) {
            assertEquals(tier.tier() == 1 ? 2 : 3, tier.circuits(), tier.ritePath());
            assertEquals(tier.tier() == 1 ? 4 : 8, tier.anchors(), tier.ritePath());
            boolean lesser = tier.station().lesserFirstTier() && tier.tier() == 1;
            assertEquals(lesser ? "lesser" : "greater", tier.riteType(), tier.ritePath());
            assertEquals(lesser ? "hemomancy:working_lesser" : "hemomancy:working_greater", tier.floor());
            assertEquals(tier.station() == UpgradeStation.RESONANT_FORGE ? 250 : 50, tier.bloodPerCircuit());
            assertFalse(tier.requiredUsage().isEmpty(), tier.ritePath());
        }
        assertEquals(6, StationUpgradeCatalog.OFFERINGS_PER_RITE);
    }

    @Test
    void stationsRoundTripByName() {
        for (UpgradeStation station : UpgradeStation.values())
            assertEquals(station, UpgradeStation.byName(station.serializedName()));
        assertNull(UpgradeStation.byName("brewing_stand"));
    }
}
