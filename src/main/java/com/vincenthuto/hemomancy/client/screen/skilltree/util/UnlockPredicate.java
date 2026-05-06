package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.LiberKnowledgeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * Client-side predicate that controls whether a recipe, node, or entry is
 * visible in the progress-screen tabs.
 * <p>
 * Evaluated each time the screen is opened (in {@code onInit}). Because the
 * LiberKnowledge capability is synced to the client on login/respawn/dimension
 * change, no additional packet machinery is needed — all checks here read
 * local state.
 *
 * <h3>Composing predicates</h3>
 * <pre>{@code
 * UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.ENTITY)
 *     .and(UnlockPredicate.minDegree(5))
 * }</pre>
 */
@FunctionalInterface
public interface UnlockPredicate {

	/** Returns {@code true} if the entry should be shown for the given player. */
	boolean isUnlocked(Player player);

	// ── Combinators ───────────────────────────────────────────────────────────

	/** Returns a predicate that is true when both {@code this} and {@code other} are true. */
	default UnlockPredicate and(UnlockPredicate other) {
		return player -> this.isUnlocked(player) && other.isUnlocked(player);
	}

	/** Returns a predicate that is true when either {@code this} or {@code other} is true. */
	default UnlockPredicate or(UnlockPredicate other) {
		return player -> this.isUnlocked(player) || other.isUnlocked(player);
	}

	// ── Factory methods ───────────────────────────────────────────────────────

	/** Always visible — the default for all unregistered entries. */
	static UnlockPredicate always() {
		return player -> true;
	}

	/**
	 * Visible once the player has unlocked the given LiberKnowledge entry.
	 * <p>
	 * This is the primary unlock mechanism (Option A). LiberKnowledge is synced
	 * to the client via {@link com.vincenthuto.hemomancy.common.network.capa.PacketSyncLiberKnowledge}
	 * on login, respawn, and dimension change, so the check is reliable client-side.
	 *
	 * @param entryId a constant from
	 *                {@link com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.LiberEntryDefinitions}
	 */
	static UnlockPredicate hasLiberEntry(ResourceLocation entryId) {
		return player -> LiberKnowledgeHelper.hasEntry(player, entryId);
	}

	/**
	 * Visible when the player's inventory contains at least one stack matching
	 * {@code test}. This checks only the main inventory (not armor/offhand).
	 */
	static UnlockPredicate hasItemInInventory(Predicate<ItemStack> test) {
		return player -> player.getInventory().items.stream().anyMatch(test);
	}

	/**
	 * Visible when the player's initiatory degree is at least {@code degree}.
	 * Returns {@code false} when the capability is absent (e.g. a non-Harbinger).
	 */
	static UnlockPredicate minDegree(int degree) {
		return player -> HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(d -> d.getDegreeNumber() >= degree)
				.orElse(false);
	}

	/**
	 * Visible when the player's Unstained purity value is at least {@code purity}.
	 * Returns {@code false} when the capability is absent (e.g. a non-Unstained
	 * player or a player who has not begun the Unstained path).
	 */
	static UnlockPredicate minPurity(float purity) {
		return player -> HemoCapabilityAccess.getUnstainedProgress(player)
				.map(up -> up.getPurity() >= purity)
				.orElse(false);
	}

	/**
	 * Visible when the player's Unstained clarity value is at least {@code clarity}.
	 * Returns {@code false} when the capability is absent.
	 */
	static UnlockPredicate minClarity(float clarity) {
		return player -> HemoCapabilityAccess.getUnstainedProgress(player)
				.map(up -> up.getClarity() >= clarity)
				.orElse(false);
	}
}
