package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteFogStateTest {
	@Test
	void activeHarbingerRiteFadesInOverTenTicks() {
		CardinalRiteFogState state = new CardinalRiteFogState();

		assertEquals(0.0F, state.update(List.of(rite(false)), 0.0F).getFirst().opacity());
		assertEquals(0.5F, state.update(List.of(rite(false)), 5.0F).getFirst().opacity());
		assertEquals(1.0F, state.update(List.of(rite(false)), 10.0F).getFirst().opacity());
	}

	@Test
	void unstainedRitesNeverEnterTheFogState() {
		CardinalRiteFogState state = new CardinalRiteFogState();

		assertTrue(state.update(List.of(rite(true)), 0.0F).isEmpty());
		assertTrue(state.update(List.of(rite(true)), 10.0F).isEmpty());
	}

	@Test
	void unstainedRiteAtTheSameCenterImmediatelySuppressesOldRedFog() {
		CardinalRiteFogState state = new CardinalRiteFogState();
		state.update(List.of(rite(false)), 0.0F);
		state.update(List.of(rite(false)), 10.0F);

		assertTrue(state.update(List.of(rite(true)), 11.0F).isEmpty());
	}

	@Test
	void vanishedRiteFadesOutAndIsThenDiscarded() {
		CardinalRiteFogState state = new CardinalRiteFogState();
		state.update(List.of(rite(false)), 0.0F);
		state.update(List.of(rite(false)), 10.0F);

		assertEquals(0.5F, state.update(List.of(), 18.0F).getFirst().opacity());
		assertTrue(state.update(List.of(), 26.0F).isEmpty());
	}

	@Test
	void authoredNoneAtmosphereNeverEntersFogState() {
		CardinalRiteFogState state = new CardinalRiteFogState();

		assertTrue(state.update(List.of(rite(false, "none", false, false)), 10.0F).isEmpty());
	}

	@Test
	void pallidShadowStormReachesFullFogDensity() {
		CardinalRiteFogState state = new CardinalRiteFogState();
		var pallidShadow = rite(false, "storm", true, true);

		state.update(List.of(pallidShadow), 0.0F);
		assertEquals(1.0F, state.update(List.of(pallidShadow), 10.0F).getFirst().opacity());
	}

	private static ActiveRiteClientData.RiteEntry rite(boolean unstained) {
		return rite(unstained, unstained ? "none" : "storm", !unstained, !unstained);
	}

	private static ActiveRiteClientData.RiteEntry rite(boolean unstained,
			String fogProfile, boolean lightning, boolean dome) {
		return new ActiveRiteClientData.RiteEntry(
				BlockPos.ZERO, 9, 0.0D,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "fog_state_test"),
				unstained, "CONSECRATION", 0, 0, 3, 0, 3,
				0, 600, 0, 0, -1, "", 9.0F,
				List.of(), List.of(), List.of(), List.of(), false, null, 0, 0,
				fogProfile, lightning, dome);
	}
}
