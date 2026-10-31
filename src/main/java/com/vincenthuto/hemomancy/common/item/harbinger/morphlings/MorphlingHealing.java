package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Heals a Morphling's host through the shared circulation window (spec balance rule 5): every point of health a
 * strain regrows or feeds claims {@link #BANDWIDTH_PER_HP} of the same bandwidth the passive blood income uses, so
 * stacking a strain's healing with armour and siphon income gives diminishing returns. Healing allies is not capped
 * here; it is the giver's gift, bounded by each power's own numbers.
 */
public final class MorphlingHealing {
	public static final double BANDWIDTH_PER_HP = 1.0D;

	private MorphlingHealing() {
	}

	/** Returns the health actually restored. */
	public static float healHost(Player player, float amount) {
		if (amount <= 0.0F || player.getHealth() >= player.getMaxHealth() || !player.isAlive()) return 0.0F;
		float wanted = Math.min(amount, player.getMaxHealth() - player.getHealth());
		float granted = wanted;
		if (player instanceof ServerPlayer server) {
			granted = (float) (BloodFlowLedger.claimCirculationBandwidth(server, wanted * BANDWIDTH_PER_HP)
					/ BANDWIDTH_PER_HP);
		}
		if (granted <= 0.0F) return 0.0F;
		float before = player.getHealth();
		player.heal(granted);
		return player.getHealth() - before;
	}
}
