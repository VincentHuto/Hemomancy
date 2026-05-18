package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge;

import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;

/**
 * Hemomancy-specific discovery sources for the Liber Sanguinum / Liber
 * Immaculatus book knowledge system.
 *
 * <p>Generic sources that are shared across all HutosLib consumer mods
 * (item pickup, advancement, entity kill, biome entry, structure discovery)
 * are provided by {@code CommonDiscoverySource} in HutosLib and are NOT
 * repeated here.
 */
public enum HemomancyDiscoverySource implements IDiscoverySource {
	/** Entry was unlocked by collecting a Hematic Memory memo item. */
	MEMO,
	/** Entry was unlocked by performing a Cardinal Rite. */
	RITE,
	/** Entry was unlocked by reaching an initiatory degree in the Hematic Order. */
	DEGREE,
	/** Entry was unlocked by a dialogue event (NPC conversation choice). */
	DIALOGUE,
	/** Entry was unlocked by reading a blood-memory inscription. */
	BLOOD_ECHO,
	/** Entry was unlocked by reading a rite fragment inscription. */
	RITE_FRAGMENT,
}
