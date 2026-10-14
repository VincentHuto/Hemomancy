package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.RingOpeningMath;
import net.minecraft.core.BlockPos;

/** How far the sores of a waking ring have torn open, read from its {@code RING_OPENED} beat. */
public final class RingTear {
	private RingTear() {
	}

	/** The beat that woke this ring, while its tear is still running; null otherwise. */
	public static CardinalRiteEventClientState.RiteEvent waking(BlockPos center, int rank, float partialTick) {
		for (CardinalRiteEventClientState.RiteEvent event : CardinalRiteEventClientState.recent(center)) {
			if (event.kind() != CardinalRiteEventKind.RING_OPENED || event.index() != rank) continue;
			float depth = RingOpeningMath.depth(rank, ringsOf(event.style()));
			if (event.age(partialTick) <= RingOpeningMath.tearTicks(depth)) return event;
		}
		return null;
	}

	/** The ring count a beat carries in its style. */
	public static int ringsOf(String style) {
		try {
			return Integer.parseInt(style);
		} catch (NumberFormatException ignored) {
			return 2;
		}
	}

	/** A sore's size as a share of full: the scar while asleep, growing through the tear, then full. */
	public static float size(boolean dormant, BlockPos center, int rank, float partialTick) {
		if (dormant) return RingOpeningMath.SCAR_SCALE;
		CardinalRiteEventClientState.RiteEvent event = waking(center, rank, partialTick);
		if (event == null) return 1.0F;
		float tear = RingOpeningMath.tear(event.age(partialTick), RingOpeningMath.depth(rank, ringsOf(event.style())));
		return RingOpeningMath.SCAR_SCALE + (1.0F - RingOpeningMath.SCAR_SCALE) * tear;
	}
}
