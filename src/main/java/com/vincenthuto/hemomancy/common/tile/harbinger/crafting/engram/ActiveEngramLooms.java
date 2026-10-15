package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Looms whose engram is unfurled, per side and dimension, so a Living Staff finds the engram it points at without
 * scanning the world. Each weaving loom re-registers itself every tick; entries that stop answering are dropped by
 * whoever looks them up.
 */
public final class ActiveEngramLooms {
	private static final Map<ResourceKey<Level>, Set<BlockPos>> SERVER = new ConcurrentHashMap<>();
	private static final Map<ResourceKey<Level>, Set<BlockPos>> CLIENT = new ConcurrentHashMap<>();

	private ActiveEngramLooms() {
	}

	private static Map<ResourceKey<Level>, Set<BlockPos>> side(Level level) {
		return level.isClientSide ? CLIENT : SERVER;
	}

	public static void track(Level level, BlockPos pos) {
		side(level).computeIfAbsent(level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
	}

	public static void untrack(Level level, BlockPos pos) {
		Set<BlockPos> looms = side(level).get(level.dimension());
		if (looms != null) looms.remove(pos);
	}

	/** Registered looms within `radius` blocks of `around` in the same dimension, nearest first. */
	public static List<BlockPos> near(Level level, BlockPos around, double radius) {
		Set<BlockPos> looms = side(level).get(level.dimension());
		List<BlockPos> out = new ArrayList<>();
		if (looms == null) return out;
		for (BlockPos pos : looms) if (pos.distSqr(around) <= radius * radius) out.add(pos);
		out.sort((a, b) -> Double.compare(a.distSqr(around), b.distSqr(around)));
		return out;
	}

	public static void clear() {
		SERVER.clear();
		CLIENT.clear();
	}

	public static void clearClient() {
		CLIENT.clear();
	}
}
