package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Echo casts by manipulation name, one class per school. Only retired, uncastable memories fall back to their
 * school's gesture; src/test/resources/echo_cast_pending.txt lists exactly those.
 */
public final class EchoCasts {
	private static final Map<String, EchoCast> CASTS = new HashMap<>();
	private static final EchoCast FALLBACK = new SchoolGestureEchoCast();

	static {
		// Each school registers its real casts, one call per manipulation name.
		AnimusEchoCasts.registerAll();
		MortemEchoCasts.registerAll();
		DuctilisEchoCasts.registerAll();
		FerricEchoCasts.registerAll();
		LuxUmbraEchoCasts.registerAll();
		ThermalEchoCasts.registerAll();
	}

	private EchoCasts() {
	}

	static void register(String name, EchoCast cast) {
		CASTS.put(name, cast);
	}

	/**
	 * Plays `name`'s cast on the next client tick, off the block entity render pass (casts add entities and cues),
	 * and swallows any failure so a broken cast can never take the frame down with it.
	 */
	public static void releaseSafely(String name, EchoCast.EchoContext context) {
		EchoScheduler.later(1, () -> {
			try {
				forManipulation(name).release(context);
			} catch (RuntimeException failure) {
				com.vincenthuto.hemomancy.Hemomancy.LOGGER.debug("Echo cast {} failed", name, failure);
			}
		});
	}

	public static EchoCast forManipulation(String name) {
		return CASTS.getOrDefault(name, FALLBACK);
	}

	public static Set<String> registered() {
		return Set.copyOf(CASTS.keySet());
	}
}
