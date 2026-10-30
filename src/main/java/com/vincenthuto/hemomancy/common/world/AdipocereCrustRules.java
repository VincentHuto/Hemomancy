package com.vincenthuto.hemomancy.common.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

/**
 * Where corpse wax crusts on the walls of old dark places: vanilla monster rooms, strongholds and ancient cities.
 * The decoration pass asks these rules which faces may take a crust, how many a site gets, and which candidates win;
 * every answer is a pure function of its arguments, so a chunk decorates the same way in every copy of a world.
 */
public final class AdipocereCrustRules {
	/** A chunk never takes more crusts than this, however many sites touch it. */
	public static final int PER_CHUNK_CAP = 4;
	/** A face counts as interior only under a roof this many blocks above it at most. */
	public static final int ROOF_REACH = 8;
	/** Monster rooms are found by their spawner; their interior reaches this far from it. */
	public static final int MONSTER_ROOM_REACH = 3;

	/** The structures that grow crusts, with the expected crusts per site and the most one site takes. */
	public enum Host {
		/** One room is one site: two crusts, at most three when the density is raised. */
		MONSTER_ROOM(2.0, 3),
		/** One site per chunk a stronghold's pieces touch; a stronghold covers fifty or so, so twenty or so in all. */
		STRONGHOLD(0.35, 2),
		/** One site per chunk an ancient city touches; a city covers a hundred or more, so thirty or so in all. */
		ANCIENT_CITY(0.3, 2);

		private final double expected;
		private final int cap;

		Host(double expected, int cap) {
			this.expected = expected;
			this.cap = cap;
		}

		public double expected() {
			return expected;
		}

		public int cap() {
			return cap;
		}
	}

	private AdipocereCrustRules() {
	}

	/** The host for a structure id, or null when that structure grows no crusts (the mausoleums among them). */
	@Nullable
	public static Host hostFor(String structureId) {
		return switch (structureId) {
			case "minecraft:stronghold" -> Host.STRONGHOLD;
			case "minecraft:ancient_city" -> Host.ANCIENT_CITY;
			default -> null;
		};
	}

	/**
	 * Whether an air cell may take a crust against one horizontal neighbour. The neighbour is the wall: it must be of
	 * the structure's masonry, and the masonry must carry on above or below it, so a lone block or a step never counts.
	 * The cell must lie under a roof, which keeps crusts off the outer faces of a city standing in an open cave.
	 * Floors and ceilings never count: the crust only ever hangs on a horizontal neighbour.
	 */
	public static boolean isCandidate(boolean cellEmpty, boolean supportIsWall, boolean wallContinues, boolean roofed) {
		return cellEmpty && supportIsWall && wallContinues && roofed;
	}

	/** How many crusts a site gets, before the chunk cap: the host's expectation scaled by the configured density. */
	public static int crustsFor(Host host, double density, long seed) {
		double expected = host.expected() * Math.max(0.0, density);
		int whole = (int) Math.floor(expected);
		double fraction = expected - whole;
		// Mixed first: java.util.Random's first draw barely moves between neighbouring seeds.
		int count = whole + (new Random(mix(seed)).nextDouble() < fraction ? 1 : 0);
		return Math.min(host.cap(), count);
	}

	/** Which of {@code candidates} take a crust: up to {@code crusts} distinct indices, the same for the same seed. */
	public static List<Integer> pick(int candidates, int crusts, long seed) {
		List<Integer> picked = new ArrayList<>();
		if (candidates <= 0 || crusts <= 0) return picked;
		Random random = new Random(mix(seed + 1));
		List<Integer> pool = new ArrayList<>(candidates);
		for (int i = 0; i < candidates; i++) pool.add(i);
		for (int i = 0; i < Math.min(crusts, candidates); i++) {
			picked.add(pool.remove(random.nextInt(pool.size())));
		}
		return picked;
	}

	/** How many crusts a chunk may still take once {@code placed} are already in it. */
	public static int remaining(int placed) {
		return Math.max(0, PER_CHUNK_CAP - placed);
	}

	/** The seed for one site in one chunk: the world seed, the chunk, the host and the site's own origin. */
	public static long seed(long worldSeed, long chunkKey, Host host, long siteKey) {
		long h = worldSeed;
		h = h * 31 + chunkKey;
		h = h * 31 + host.ordinal();
		h = h * 31 + siteKey;
		return mix(h);
	}

	private static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}
}
