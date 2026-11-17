package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Track 0 groundwork: the profile map, the share of an empty profile, opposing pairs and the strongest-temper tie. */
class BloodTemperamentProfileTest {

	@Test
	void anImmutableOrPartialMapCanBeWrittenAfterItIsInstalled() {
		BloodTemperament temper = new BloodTemperament();
		temper.setTemper(Map.of(EnumBloodTemper.LUX, 20F));

		assertDoesNotThrow(() -> temper.addTemperAlignment(EnumBloodTemper.LUX, 1F));
		assertDoesNotThrow(() -> temper.setTemperAlignment(EnumBloodTemper.MORTEM, 3F));
		assertEquals(21F, temper.getAlignmentByTemper(EnumBloodTemper.LUX));
		assertEquals(3F, temper.getAlignmentByTemper(EnumBloodTemper.MORTEM));
		for (EnumBloodTemper value : EnumBloodTemper.values()) {
			assertEquals(true, temper.serializeNBT(null).contains(value.toString()),
					value + " is missing from the saved profile");
		}
	}

	@Test
	void anEmptyProfileHasNoShareInsteadOfDividingByZero() {
		BloodTemperament temper = new BloodTemperament();
		for (EnumBloodTemper value : EnumBloodTemper.values()) {
			assertEquals(0F, temper.getPercentByTemper(value));
		}
		temper.setTemperAlignment(EnumBloodTemper.ANIMUS, 3F);
		temper.setTemperAlignment(EnumBloodTemper.FERRIC, 1F);
		assertEquals(75F, temper.getPercentByTemper(EnumBloodTemper.ANIMUS), 0.0001F);
	}

	@Test
	void everyTemperOpposesExactlyOneOtherAndThePairIsMutual() {
		for (EnumBloodTemper value : EnumBloodTemper.values()) {
			EnumBloodTemper opposite = value.opposite();
			assertEquals(value, opposite.opposite(), value + " and " + opposite + " are not a pair");
			assertEquals(false, value == opposite, value + " opposes itself");
		}
		assertEquals(EnumBloodTemper.MORTEM, EnumBloodTemper.ANIMUS.opposite());
		assertEquals(EnumBloodTemper.FERRIC, EnumBloodTemper.DUCTILIS.opposite());
		assertEquals(EnumBloodTemper.TENEBRIS, EnumBloodTemper.LUX.opposite());
		assertEquals(EnumBloodTemper.CONGEATIO, EnumBloodTemper.FLAMMEUS.opposite());
	}

	@Test
	void theStrongestTemperIsTheHighestAndATieGoesToTheFirstDeclared() {
		BloodTemperament temper = new BloodTemperament();
		assertNull(temper.strongestTemper(), "an empty profile has no strongest temper");

		temper.setTemperAlignment(EnumBloodTemper.TENEBRIS, 5F);
		temper.setTemperAlignment(EnumBloodTemper.MORTEM, 4F);
		assertEquals(EnumBloodTemper.TENEBRIS, temper.strongestTemper());

		temper.setTemperAlignment(EnumBloodTemper.MORTEM, 5F);
		assertEquals(EnumBloodTemper.MORTEM, temper.strongestTemper(), "Mortem is declared before Tenebris");

		temper.addTransientAlignment(EnumBloodTemper.TENEBRIS, 0.5F);
		assertEquals(EnumBloodTemper.TENEBRIS, temper.strongestTemper(), "a scar's lent alignment counts");
	}
}
