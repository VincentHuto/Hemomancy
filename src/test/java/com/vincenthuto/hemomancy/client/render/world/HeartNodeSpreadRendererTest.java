package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.common.world.HeartNodeFootprint;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/** The spread's early exit: with nothing to draw the pass gets the shared empty list and allocates nothing. */
class HeartNodeSpreadRendererTest {
	private static final BlockPos HEART = new BlockPos(0, 64, 0);

	private static HeartNodeClientState.Envelope envelope(HeartNodeSettings settings, HeartNodeRelation relation) {
		return new HeartNodeClientState.Envelope(new HeartNodeFootprint(UUID.randomUUID(), HEART, List.of()), settings,
				relation, 0L, 3);
	}

	@Test
	void nothingInReachIsTheSharedEmptyList() {
		assertSame(List.of(), HeartNodeSpreadRenderer.visible(List.of(), true));
	}

	@Test
	void theClientConfigTurnsEverySanctumOff() {
		var own = envelope(HeartNodeSettings.DEFAULT, HeartNodeRelation.OWNER);
		assertSame(List.of(), HeartNodeSpreadRenderer.visible(List.of(own), false));
	}

	@Test
	void hiddenSanctumsAllocateNothing() {
		var hiddenFromStrangers = envelope(HeartNodeSettings.DEFAULT, HeartNodeRelation.STRANGER);
		var spreadOff = envelope(HeartNodeSettings.DEFAULT.with(HeartNodeSettings.SPREAD, false), HeartNodeRelation.OWNER);
		var withered = new HeartNodeClientState.Envelope(new HeartNodeFootprint(UUID.randomUUID(), null, List.of()),
				HeartNodeSettings.DEFAULT, HeartNodeRelation.OWNER, 0L, 0);
		assertSame(List.of(), HeartNodeSpreadRenderer.visible(List.of(hiddenFromStrangers, spreadOff, withered), true));
	}

	@Test
	void theOwnersAndShownStrangersSanctumsAreDrawn() {
		var own = envelope(HeartNodeSettings.DEFAULT, HeartNodeRelation.OWNER);
		var shown = envelope(HeartNodeSettings.DEFAULT.with(HeartNodeSettings.SHOW_STRANGERS, true),
				HeartNodeRelation.STRANGER);
		var hidden = envelope(HeartNodeSettings.DEFAULT, HeartNodeRelation.STRANGER);
		assertEquals(List.of(own, shown), HeartNodeSpreadRenderer.visible(List.of(own, hidden, shown), true));
	}
}
