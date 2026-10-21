package com.vincenthuto.hemomancy.common.manipulation.ferric;

import static org.junit.jupiter.api.Assertions.*;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WeaponConjureRulesTest {
    @Test void everyStaffFormHasItsOwnWeaponAndTheStaffIsTheFallback() {
        String[] forms = { LivingStaffWeaponFormRules.CONJURE_BLADE, LivingStaffWeaponFormRules.CONJURE_AXE,
                LivingStaffWeaponFormRules.CONJURE_SPEAR, LivingStaffWeaponFormRules.CONJURE_CLAWS,
                LivingStaffWeaponFormRules.CONJURE_CROSSBOW, LivingStaffWeaponFormRules.CONJURE_TORCH,
                LivingStaffWeaponFormRules.CONJURE_FLAIL, LivingStaffWeaponFormRules.CONJURE_SICKLE };
        Set<Integer> seen = new HashSet<>();
        for (String form : forms) {
            assertTrue(LivingStaffWeaponFormRules.isStaffWeaponFormManip(form));
            int weapon = WeaponConjureRules.weapon(form);
            assertNotEquals(WeaponConjureRules.STAFF, weapon, form);
            assertTrue(seen.add(weapon), form + " shares a weapon");
        }
        assertEquals(WeaponConjureRules.STAFF, WeaponConjureRules.weapon(""));
        assertEquals(WeaponConjureRules.STAFF, WeaponConjureRules.weapon(null));
    }

    @Test void schoolsFollowEachWeaponsTendency() {
        assertEquals(WeaponConjureRules.School.ANIMUS, WeaponConjureRules.school(WeaponConjureRules.BLADE));
        assertEquals(WeaponConjureRules.School.FERRIC, WeaponConjureRules.school(WeaponConjureRules.AXE));
        assertEquals(WeaponConjureRules.School.LUX, WeaponConjureRules.school(WeaponConjureRules.SPEAR));
        assertEquals(WeaponConjureRules.School.UMBRA, WeaponConjureRules.school(WeaponConjureRules.CLAWS));
        assertEquals(WeaponConjureRules.School.DUCTILIS, WeaponConjureRules.school(WeaponConjureRules.CROSSBOW));
        assertEquals(WeaponConjureRules.School.FLAME, WeaponConjureRules.school(WeaponConjureRules.TORCH));
        assertEquals(WeaponConjureRules.School.CRUOR, WeaponConjureRules.school(WeaponConjureRules.FLAIL));
        assertEquals(WeaponConjureRules.School.MORTEM, WeaponConjureRules.school(WeaponConjureRules.SICKLE));
        assertEquals(WeaponConjureRules.School.FERRIC, WeaponConjureRules.school(WeaponConjureRules.STAFF));
    }

    @Test void encodingRoundTripsAndStaysPositive() {
        for (int weapon = 0; weapon < WeaponConjureRules.WEAPONS; weapon++)
            for (int variant = 0; variant < 8; variant++) {
                int count = WeaponConjureRules.encode(weapon, variant);
                assertTrue(count >= 1);
                assertEquals(weapon, WeaponConjureRules.decodeWeapon(count));
                assertEquals(variant, WeaponConjureRules.decodeVariant(count));
            }
    }

    @Test void strokesAreWellFormedAndHandSized() {
        for (int weapon = 0; weapon < WeaponConjureRules.WEAPONS; weapon++) {
            assertTrue(WeaponConjureRules.strokes(weapon).length > 0);
            for (var stroke : WeaponConjureRules.strokes(weapon)) {
                assertEquals(0, stroke.points().length % 2);
                assertTrue(stroke.size() >= 2);
                assertTrue(stroke.width() > 0 && stroke.width() < .1);
                for (double coordinate : stroke.points()) assertTrue(Math.abs(coordinate) <= 1.3);
            }
            assertTrue(WeaponConjureRules.length(weapon) > .3, "weapon " + weapon);
        }
    }

    @Test void theTimelineGathersSketchesHardensAndSeatsInOrder() {
        assertEquals(0, WeaponConjureRules.gather(0), 1e-9);
        assertEquals(1, WeaponConjureRules.gather(WeaponConjureRules.GATHER_END), 1e-9);
        for (int weapon = 0; weapon < WeaponConjureRules.WEAPONS; weapon++) {
            int strokes = WeaponConjureRules.strokes(weapon).length;
            for (int k = 0; k < strokes; k++) {
                assertEquals(0, WeaponConjureRules.sketch(WeaponConjureRules.GATHER_END - 1, k, strokes), 1e-9);
                assertEquals(1, WeaponConjureRules.sketch(WeaponConjureRules.SKETCH_END, k, strokes), 1e-9, "weapon " + weapon);
                double last = 0;
                for (double age = 0; age <= WeaponConjureRules.SKETCH_END; age += .25) {
                    double drawn = WeaponConjureRules.sketch(age, k, strokes);
                    assertTrue(drawn >= last - 1e-12);
                    last = drawn;
                }
            }
        }
        assertEquals(0, WeaponConjureRules.spread(WeaponConjureRules.GATHER_END - 1), 1e-9);
        assertEquals(1, WeaponConjureRules.spread(WeaponConjureRules.SKETCH_END), 1e-9);
        assertTrue(WeaponConjureRules.spread(6) < .4, "the coat creeps out over the sketch, not in a tick or two");
        assertEquals(0, WeaponConjureRules.harden(WeaponConjureRules.SKETCH_END - 1), 1e-9);
        assertEquals(1, WeaponConjureRules.harden(WeaponConjureRules.HARDEN_END), 1e-9);
        assertEquals(1, WeaponConjureRules.seat(WeaponConjureRules.HARDEN_END), 1e-9);
        assertEquals(.25, WeaponConjureRules.seat(WeaponConjureRules.SEAT_END), 1e-9);
        assertEquals(1, WeaponConjureRules.fray(WeaponConjureRules.VISUAL_TICKS), 1e-9);
    }
}
