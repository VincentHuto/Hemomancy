package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.UnlockPredicate;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery.LiberEntryDefinitions;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Cardinal Rite recipe {@link ResourceLocation}s to their
 * {@link UnlockPredicate}, controlling visibility in the Rites tab of the
 * progress screens.
 * <p>
 * Entries absent from this registry default to {@link UnlockPredicate#always()}
 * so all existing rites remain visible until explicitly gated here.
 *
 * <h3>Adding a new gate</h3>
 * <pre>{@code
 * register("cardinal_rite/my_secret_rite",
 *     UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.ENTITY));
 * }</pre>
 */
public final class RiteUnlockRegistry {

	private static final Map<ResourceLocation, UnlockPredicate> REGISTRY = new HashMap<>();

	static {
		// ── Qliphoth-related rites: hidden until the player has encountered the
		//    Qliphoth (picking up qliphoth_seed or qliphoth_pome unlocks the entry).
		register("cardinal_rite/bloom_of_qliphoth",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.QLIPHOTH));
		register("cardinal_rite/pruning_of_qliphoth",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.QLIPHOTH));

		// ── Entity/ascension rites: hidden until the player learns about the
		//    Fungal Entity (degree 7 or related memos/items unlock the entry).
		register("cardinal_rite/ancestral_communion",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.ENTITY));

		// ── Ultimate Harbinger rite: hidden until the player has uncovered
		//    the truth behind the Order (degree 8 or deep-lore sources).
		register("cardinal_rite/apotheos_rite",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.TRUTH));
		register("cardinal_rite/eternal_covenant",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.TRUTH));

		// ── Blood-Moon rite: hidden until the player knows about blood moons
		//    (surviving a blood moon grants the entry via the advancement).
		register("cardinal_rite/sanguine_eclipse",
				UnlockPredicate.hasLiberEntry(LiberEntryDefinitions.BLOOD_MOONS));
	}

	private RiteUnlockRegistry() {}

	/**
	 * Returns the {@link UnlockPredicate} for the given rite recipe ID, or
	 * {@link UnlockPredicate#always()} if no gate has been registered.
	 */
	public static UnlockPredicate get(ResourceLocation recipeId) {
		return REGISTRY.getOrDefault(recipeId, UnlockPredicate.always());
	}

	private static void register(String path, UnlockPredicate predicate) {
		REGISTRY.put(Hemomancy.rloc(path), predicate);
	}
}
