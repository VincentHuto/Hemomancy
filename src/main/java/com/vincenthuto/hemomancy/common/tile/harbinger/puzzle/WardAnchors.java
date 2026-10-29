package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The ward anchors loaded in each level, so the threshold tick and the ward renderer never search the world. The
 * server's and the client's levels are kept apart: each side's map is only touched from its own thread.
 */
public final class WardAnchors {
	private static final Map<Level, Set<WardAnchorBlockEntity>> SERVER = new WeakHashMap<>();
	private static final Map<Level, Set<WardAnchorBlockEntity>> CLIENT = new WeakHashMap<>();

	private WardAnchors() {
	}

	public static Collection<WardAnchorBlockEntity> loaded(Level level) {
		if (level.isClientSide) CLIENT.keySet().removeIf(held -> held != level);
		Set<WardAnchorBlockEntity> anchors = side(level).get(level);
		return anchors == null ? Collections.emptySet() : Collections.unmodifiableSet(anchors);
	}

	static void add(Level level, WardAnchorBlockEntity anchor) {
		// A client keeps one level: the one it left on a dimension change never unloads its chunks, so drop it here.
		if (level.isClientSide) CLIENT.keySet().removeIf(held -> held != level);
		side(level).computeIfAbsent(level, key -> Collections.newSetFromMap(new java.util.IdentityHashMap<>()))
				.add(anchor);
	}

	static void remove(Level level, WardAnchorBlockEntity anchor) {
		Set<WardAnchorBlockEntity> anchors = side(level).get(level);
		if (anchors == null) return;
		anchors.remove(anchor);
		if (anchors.isEmpty()) side(level).remove(level);
	}

	private static Map<Level, Set<WardAnchorBlockEntity>> side(Level level) {
		return level.isClientSide ? CLIENT : SERVER;
	}

	/** On logout: the client's levels are gone. */
	public static void clearClient() {
		CLIENT.clear();
	}

	/** When a server starts or stops. */
	public static void clearServer() {
		SERVER.clear();
	}
}
