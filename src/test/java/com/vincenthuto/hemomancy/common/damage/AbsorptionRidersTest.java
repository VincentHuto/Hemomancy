package com.vincenthuto.hemomancy.common.damage;

import com.vincenthuto.hemomancy.common.damage.AbsorptionRiders.Effect;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Track 12 (CL-26): Absorption's riders come from one table keyed by the school state on the creature. */
class AbsorptionRidersTest {
	@Test
	void everySchoolStateButVeiledHasARider() {
		for (SchoolState state : SchoolState.values()) {
			AbsorptionRiders.Rider rider = AbsorptionRiders.of(state);
			if (state == SchoolState.VEILED) assertEquals(AbsorptionRiders.NONE, rider, "Veiled is the drawer's own state");
			else assertTrue(rider != AbsorptionRiders.NONE, state + " has no rider");
		}
	}

	@Test
	void theTableReadsAsDesigned() {
		AbsorptionRiders.Rider pressure = AbsorptionRiders.of(SchoolState.PRESSURE);
		assertTrue(pressure.has(Effect.PULL) && pressure.blood() > 1.0D, "Pressure pulls and gives more back");
		AbsorptionRiders.Rider searing = AbsorptionRiders.of(SchoolState.SEARING);
		assertTrue(searing.damage() > 1.0D && searing.blood() < 1.0D, "Searing draws harder and boils some off");
		assertTrue(AbsorptionRiders.of(SchoolState.RIME).has(Effect.HOLD_STILL));
		assertTrue(AbsorptionRiders.of(SchoolState.LODESTONE).has(Effect.STRIP_ARMOUR));
		assertTrue(AbsorptionRiders.of(SchoolState.NECROSIS).has(Effect.SPREAD_ON_KILL));
		assertTrue(AbsorptionRiders.of(SchoolState.DISRUPTED).has(Effect.ARC));
		assertEquals(1.5D, AbsorptionRiders.of(SchoolState.ILLUMINATED).reach(), 1.0E-9D);
		assertTrue(AbsorptionRiders.of(SchoolState.OBSCURED).has(Effect.UNTARGET_AND_VEIL));
	}

	@Test
	void noStateIsAPlainDraw() {
		AbsorptionRiders.Rider none = AbsorptionRiders.combine(Set.of());
		assertEquals(1.0D, none.damage(), 1.0E-9D);
		assertEquals(1.0D, none.blood(), 1.0E-9D);
		assertEquals(1.0D, none.reach(), 1.0E-9D);
		assertTrue(none.effects().isEmpty());
	}

	@Test
	void severalStatesGiveEveryRiderAtOnce() {
		AbsorptionRiders.Rider all = AbsorptionRiders.combine(List.of(SchoolState.PRESSURE, SchoolState.SEARING,
				SchoolState.ILLUMINATED));
		assertEquals(1.5D, all.damage(), 1.0E-9D);
		assertEquals(1.25D * 0.6D, all.blood(), 1.0E-9D);
		assertEquals(1.5D, all.reach(), 1.0E-9D);
		assertEquals(EnumSet.of(Effect.PULL), EnumSet.copyOf(all.effects()));
	}

	@Test
	void aMarkIsAnyRiderState() {
		assertTrue(AbsorptionRiders.marks(List.of(SchoolState.NECROSIS)));
		assertFalse(AbsorptionRiders.marks(List.of(SchoolState.VEILED)));
		assertFalse(AbsorptionRiders.marks(List.of()));
	}
}
