package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.util.HashMap;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.UnlockPredicate;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.LiberEntryDefinitions;

import net.minecraft.resources.ResourceLocation;

/**
 * Maps Cerebral Scar recipe {@link ResourceLocation}s to their
 * {@link UnlockPredicate}, controlling visibility in the Scars tab of the
 * Harbinger progress screen.
 * <p>
 * Entries absent from this registry default to {@link UnlockPredicate#always()}
 * so all existing scar recipes remain visible until explicitly gated here.
 *
 * <h3>Adding a new gate</h3>
 * <pre>{@code
 * register("scar/scar_heart",
 *     UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.HEMOMANCY));
 * }</pre>
 */
public final class ScarUnlockRegistry {

	private static final Map<ResourceLocation, UnlockPredicate> REGISTRY = new HashMap<>();

	static {
		// ── Fungal scars are cultivated rather than carved and require contact
		//    with the Fungal Entity's influence.  They become visible once the
		//    player has unlocked the ENTITY LiberKnowledge entry (degree 7,
		//    abyssal_ichor/void_ichor pickup, or Entity-related memos).
		register("scar/respergillus",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.ENTITY));
		register("scar/talaromyces_minus",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.ENTITY));
		register("scar/lumina_devorans",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.ENTITY));
		register("scar/noctifly_agaric",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.ENTITY));
	}

	private ScarUnlockRegistry() {}

	/**
	 * Returns the {@link UnlockPredicate} for the given scar recipe ID, or
	 * {@link UnlockPredicate#always()} if no gate has been registered.
	 */
	public static UnlockPredicate get(ResourceLocation recipeId) {
		return REGISTRY.getOrDefault(recipeId, UnlockPredicate.always());
	}

	private static void register(String path, UnlockPredicate predicate) {
		REGISTRY.put(Hemomancy.rloc(path), predicate);
	}
}
