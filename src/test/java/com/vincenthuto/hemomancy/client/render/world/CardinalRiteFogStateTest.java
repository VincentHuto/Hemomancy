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

	private static ActiveRiteClientData.RiteEntry rite(boolean unstained) {
		return new ActiveRiteClientData.RiteEntry(
				BlockPos.ZERO, 3, 0.0D,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "fog_state_test"),
				unstained);
	}
}
