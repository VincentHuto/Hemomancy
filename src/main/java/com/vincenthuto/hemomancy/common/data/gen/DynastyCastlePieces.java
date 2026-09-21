package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FargoneVariant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Geometry source for the dynasty castle jigsaw pieces. Each piece is authored in convenient
 * (possibly negative, keep-centered) local coordinates and then normalized so its minimum block
 * corner sits at the origin. Because {@link #size}, {@link #blocks}, and {@link #entities} all read
 * from the same normalized build, they can never disagree about bounds.
 */
public final class DynastyCastlePieces {
	public record BlockPlacement(int x, int y, int z, String name) {}
	public record EntityPlacement(double x, double y, double z, String entityId, int fargoneVariant) {}
	private record Position(int x, int y, int z) {}
	private record Built(int[] size, List<BlockPlacement> blocks, List<EntityPlacement> entities) {}

	private static final String CRUST = "hemomancy:hemorrhagic_crust";
	private static final String HYPHAE = "hemomancy:hyphae_block";
	private static final String MASS = "hemomancy:conscious_mass";
	private static final String GLASS = "hemomancy:sanguine_glass";
	private static final String CALCIFIED = "hemomancy:calcified_hyphae";
	private static final String DARK_STONE = "minecraft:polished_blackstone";
	private static final String DARK_BRICK = "minecraft:polished_blackstone_bricks";
	private static final String AIR = "minecraft:air";

	/** Half-extent of the courtyard clearing box (RING 13 in the layout + tower/gate overhang). */
	private static final int COURTYARD_HALF = 17;
	/** How high the courtyard clearing carves terrain out of the castle envelope. */
	private static final int COURTYARD_CLEAR = 48;

	private static final Map<String, Built> CACHE = new ConcurrentHashMap<>();

	private DynastyCastlePieces() {}

	public static List<String> pieceNames() {
		return List.of("courtyard",
				"keep_grand", "keep_ruined", "keep_squat",
				"tower_tall", "tower_short", "tower_broken",
				"wall_ns_windows", "wall_ns_plain", "wall_ns_breach",
				"wall_ew_windows", "wall_ew_plain", "wall_ew_breach",
				"gate_side", "gate_side_ruined");
	}

	public static int[] size(String piece) {
		return built(piece).size().clone();
	}

	public static List<BlockPlacement> blocks(String piece) {
		return built(piece).blocks();
	}

	public static List<EntityPlacement> entities(String piece) {
		return built(piece).entities();
	}

	private static Built built(String piece) {
		return CACHE.computeIfAbsent(piece, DynastyCastlePieces::build);
	}

	private static Built build(String piece) {
		Builder b = new Builder();
		switch (piece) {
			case "courtyard" -> courtyard(b);
			case "keep_grand" -> {
				keep(b, 7, 6, 30, false);
				b.fargone(0, 2, 0, FargoneVariant.ELDER);
				b.fargone(-3, 2, 2, FargoneVariant.GUARD);
				b.fargone(3, 2, -2, FargoneVariant.ATTENDANT);
			}
			case "keep_ruined" -> {
				keep(b, 7, 6, 22, true);
				b.fargone(0, 2, 0, FargoneVariant.RECLAIMED);
				b.fargone(2, 2, 2, FargoneVariant.RECLAIMED);
			}
			case "keep_squat" -> {
				keep(b, 5, 4, 14, false);
				b.fargone(0, 2, 0, FargoneVariant.ELDER);
			}
			case "tower_tall" -> {
				tower(b, 3, 28, false);
				b.fargone(0, 2, 0, FargoneVariant.SCOUT);
			}
			case "tower_short" -> {
				tower(b, 3, 18, false);
				b.fargone(0, 2, 0, FargoneVariant.GUARD);
			}
			case "tower_broken" -> {
				tower(b, 3, 11, true);
				b.thirster(0, 2, 0);
			}
			case "wall_ns_windows" -> wallSide(b, 0, false);
			case "wall_ns_plain" -> wallSide(b, 1, false);
			case "wall_ns_breach" -> wallSide(b, 2, false);
			case "wall_ew_windows" -> wallSide(b, 0, true);
			case "wall_ew_plain" -> wallSide(b, 1, true);
			case "wall_ew_breach" -> wallSide(b, 2, true);
			case "gate_side" -> gateSide(b, false);
			case "gate_side_ruined" -> gateSide(b, true);
			default -> throw new IllegalArgumentException("Unknown piece " + piece);
		}
		return b.normalize();
	}

	// --- courtyard clearing --------------------------------------------------

	/**
	 * A full-footprint base: a solid floor slab plus an air column that carves the whole castle
	 * envelope out of the surrounding terrain. Placed first, so the keep, towers, and walls
	 * overwrite their own columns afterwards. This is what stops terrain generating through the
	 * castle and its bailey.
	 */
	private static void courtyard(Builder b) {
		for (int x = -COURTYARD_HALF; x <= COURTYARD_HALF; x++) {
			for (int z = -COURTYARD_HALF; z <= COURTYARD_HALF; z++) {
				b.set(x, 0, z, Math.floorMod(x * 3 + z, 7) == 0 ? MASS : CRUST);
				for (int y = 1; y <= COURTYARD_CLEAR; y++) b.set(x, y, z, AIR);
			}
		}
	}

	// --- keep ----------------------------------------------------------------

	private static void keep(Builder b, int halfX, int halfZ, int height, boolean ruined) {
		// foundation slab so the piece rests on a solid base
		for (int x = -halfX; x <= halfX; x++)
			for (int z = -halfZ; z <= halfZ; z++)
				b.set(x, 0, z, Math.floorMod(x * 3 + z, 7) == 0 ? MASS : CRUST);

		for (int y = 1; y <= height; y++) {
			for (int x = -halfX; x <= halfX; x++) {
				for (int z = -halfZ; z <= halfZ; z++) {
					boolean edge = Math.abs(x) == halfX || Math.abs(z) == halfZ;
					if (!edge && y > 1 && y < height && y % 8 != 0) {
						b.set(x, y, z, AIR); // hollow interior, explicitly cleared
						continue;
					}
					if (ruined && y > height - 7 && Math.floorMod(x * 7 + z * 5 + y, 6) == 0) {
						b.set(x, y, z, AIR);
						continue;
					}
					b.set(x, y, z, y == height || y % 8 == 0 ? CALCIFIED : facade(Math.abs(z) == halfZ ? x : z, y));
				}
			}
		}
		// interior floor beams every 8 blocks
		for (int floor = 8; floor < height; floor += 8) {
			for (int x = -halfX + 1; x < halfX; x++) {
				b.set(x, floor, -halfZ + 1, DARK_BRICK);
				b.set(x, floor, halfZ - 1, DARK_BRICK);
			}
		}
		buttresses(b, halfX, halfZ, height);
		keepEntrance(b, halfX, halfZ);
		gothicWindow(b, 0, 8, -halfZ, Math.min(11, height - 6), true);
		gothicWindow(b, 0, 5, halfZ, Math.min(14, height - 3), true);
		gothicWindow(b, -halfX, 6, 0, Math.min(10, height - 4), false);
		gothicWindow(b, halfX, 6, 0, Math.min(10, height - 4), false);
		keepStairs(b, halfX, halfZ, height);
		furnishKeep(b, halfX, halfZ, ruined);
		furnishUpperFloors(b, halfX, halfZ, height, ruined);
		keepRoof(b, height, halfX, halfZ, ruined);
	}

	/**
	 * Cuts a stairwell up the +X side of the keep: one flight per solid floor, each with a hole
	 * through the slab above it, alternating direction each level like a real tower stair.
	 */
	private static void keepStairs(Builder b, int halfX, int halfZ, int height) {
		int runX = halfX - 2;
		int level = 0;
		for (int floorY = 8; floorY < height; floorY += 8, level++) {
			flight(b, runX, floorY, halfX, halfZ, level % 2 == 0 ? 1 : -1);
		}
	}

	/** One 2-wide flight rising to the solid floor at {@code topY}, climbing in +Z or -Z per {@code dir}. */
	private static void flight(Builder b, int runX, int topY, int halfX, int halfZ, int dir) {
		int steps = 7;
		int zStart = dir > 0 ? -(halfZ - 1) : (halfZ - 1);
		String facing = dir > 0 ? "north" : "south";
		for (int s = 0; s < steps; s++) {
			int y = topY - (steps - 1) + s; // topY-6 .. topY
			int z = zStart + dir * s;
			for (int w = 0; w < 2; w++) {
				int x = runX - w;
				b.set(x, y, z, stair("polished_blackstone", facing, "bottom"));
				b.set(x, y - 1, z, DARK_BRICK); // solid stringer beneath the steps
			}
		}
		// clear headroom / passage through the slab above the upper part of the run
		for (int s = 4; s <= 6; s++) {
			int z = zStart + dir * s;
			for (int w = 0; w < 2; w++) {
				int x = runX - w;
				if (Math.abs(x) < halfX && Math.abs(z) < halfZ) b.set(x, topY, z, AIR);
			}
		}
	}

	// --- furnishings ---------------------------------------------------------

	private static final String CHAIN = "hemomancy:hematic_iron_chain[axis=y,waterlogged=false]";
	private static final String LANTERN_HANGING = "hemomancy:hematic_lantern[hanging=true,waterlogged=false]";
	private static final String LANTERN_STANDING = "hemomancy:hematic_lantern[hanging=false,waterlogged=false]";
	private static final String CARPET = "minecraft:red_carpet";

	/** Populates the keep's ground-floor hall with braziers, hanging lanterns, and (when grand) a throne and tables. */
	private static void furnishKeep(Builder b, int halfX, int halfZ, boolean ruined) {
		int floor = 2;          // walkable surface (y=1 is the solid ground-floor slab)
		int ceiling = 8;        // first interior floor slab
		// braziers on the -X side (the +X side holds the stairwell)
		brazier(b, -(halfX - 2), floor, -(halfZ - 2));
		brazier(b, -(halfX - 2), floor, halfZ - 2);
		hangLantern(b, -(halfX - 4), ceiling, 0);

		if (ruined || halfX < 7) return; // only the grand keep gets full furniture

		// throne against the back wall, on a small dais with a runner carpet
		b.set(0, floor, halfZ - 1, DARK_STONE);
		b.set(0, floor + 1, halfZ - 1, stair("polished_blackstone", "north", "bottom"));
		b.set(-1, floor, halfZ - 1, DARK_BRICK);
		b.set(1, floor, halfZ - 1, DARK_BRICK);
		b.set(-1, floor + 1, halfZ - 1, LANTERN_STANDING);
		b.set(1, floor + 1, halfZ - 1, LANTERN_STANDING);
		for (int z = 0; z <= halfZ - 2; z++) b.set(0, floor, z, CARPET);

		// a table with two chairs off to one side
		table(b, -(halfX - 3), floor, -2);
		b.set(-(halfX - 4), floor, -2, stair("polished_blackstone", "west", "bottom"));
		b.set(-(halfX - 2), floor, -2, stair("polished_blackstone", "east", "bottom"));
	}

	/** Furnishes each upper floor with braziers, a hanging lantern, and (when intact) a table and chair. */
	private static void furnishUpperFloors(Builder b, int halfX, int halfZ, int height, boolean ruined) {
		for (int floorY = 8; floorY < height - 3; floorY += 8) {
			int w = floorY + 1;                        // walkable surface of the room above this slab
			int ceiling = Math.min(floorY + 8, height); // next slab or the keep top
			brazier(b, -(halfX - 2), w, -(halfZ - 2));
			brazier(b, -(halfX - 2), w, halfZ - 2);
			if (ceiling - w >= 4) hangLantern(b, -1, ceiling, 0);
			if (!ruined && halfX >= 7) {
				table(b, -(halfX - 3), w, 0);
				b.set(-(halfX - 4), w, 0, stair("polished_blackstone", "west", "bottom"));
			}
		}
	}

	/** A hanging lantern in the base of a tower. */
	private static void furnishTower(Builder b, int radius) {
		b.set(radius - 1, 2, radius - 1, DARK_STONE);
		b.set(radius - 1, 3, radius - 1, LANTERN_STANDING);
	}

	private static void brazier(Builder b, int x, int y, int z) {
		b.set(x, y, z, DARK_BRICK);
		b.set(x, y + 1, z, LANTERN_STANDING);
	}

	private static void hangLantern(Builder b, int x, int ceiling, int z) {
		b.set(x, ceiling - 1, z, CHAIN);
		b.set(x, ceiling - 2, z, LANTERN_HANGING);
	}

	private static void table(Builder b, int x, int y, int z) {
		b.set(x, y, z, "minecraft:polished_blackstone_wall");
		b.set(x, y + 1, z, "minecraft:polished_blackstone_slab[type=bottom]");
	}

	private static String stair(String kind, String facing, String half) {
		return "minecraft:" + kind + "_stairs[facing=" + facing + ",half=" + half + "]";
	}

	/** Carves an arched doorway through the keep's front (-Z) face into the hollow interior. */
	private static void keepEntrance(Builder b, int halfX, int halfZ) {
		for (int x = -1; x <= 1; x++)
			for (int y = 1; y <= 5; y++)
				b.set(x, y, -halfZ, AIR);
		b.set(-2, 5, -halfZ, CALCIFIED); // arch shoulders
		b.set(2, 5, -halfZ, CALCIFIED);
		for (int x = -2; x <= 2; x++)
			b.set(x, 6, -halfZ, CALCIFIED); // lintel
		for (int x = -2; x <= 2; x++)
			b.set(x, 0, -halfZ, DARK_BRICK); // threshold
	}

	private static void keepRoof(Builder b, int height, int halfX, int halfZ, boolean ruined) {
		int layers = ruined ? 3 : Math.max(halfX, halfZ) + 1;
		for (int layer = 0; layer < layers; layer++) {
			int rx = Math.max(1, halfX - layer);
			int rz = Math.max(1, halfZ - layer);
			for (int x = -rx; x <= rx; x++) {
				for (int z = -rz; z <= rz; z++) {
					boolean roofEdge = Math.abs(x) == rx || Math.abs(z) == rz;
					b.set(x, height + 1 + layer, z,
							roofEdge ? (layer % 2 == 0 ? DARK_STONE : CRUST) : (layer % 2 == 0 ? CRUST : HYPHAE));
				}
			}
		}
		if (!ruined) spire(b, 0, 0, height + 1 + layers, Math.max(2, halfX / 2), 8);
	}

	// --- tower ---------------------------------------------------------------

	private static void tower(Builder b, int radius, int height, boolean broken) {
		for (int x = -radius; x <= radius; x++)
			for (int z = -radius; z <= radius; z++)
				b.set(x, 0, z, CRUST);
		for (int y = 1; y <= height; y++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					boolean edge = Math.abs(x) == radius || Math.abs(z) == radius;
					if (!edge && y > 1 && y < height) { b.set(x, y, z, AIR); continue; }
					if (broken && y > height - 5 && Math.floorMod(x * 7 + z * 5 + y, 5) == 0) { b.set(x, y, z, AIR); continue; }
					b.set(x, y, z, y == height ? CALCIFIED : facade(Math.abs(z) == radius ? x : z, y));
				}
			}
		}
		gothicWindow(b, 0, 5, -radius, Math.min(10, height - 3), true);
		gothicWindow(b, 0, 5, radius, Math.min(8, height - 3), true);
		if (!broken) furnishTower(b, radius);
		if (!broken) spire(b, 0, 0, height + 1, radius + 1, 9);
	}

	// --- curtain wall (spans a full side, corner to corner) ------------------

	/**
	 * A full curtain-wall side, centered on the origin. Built along the long axis chosen by
	 * {@code alongZ} (false = along X for north/south sides, true = along Z for east/west) so that
	 * every side can be placed with Rotation.NONE -- avoiding Minecraft's rotate-about-origin math.
	 * Three blocks thick with a battlemented walk. variant: 0 = arrow-slit windows, 1 = plain,
	 * 2 = breached (gaps).
	 */
	private static void wallSide(Builder b, int variant, boolean alongZ) {
		int half = 13;
		int height = 7;
		int halfPerp = 1;
		boolean breach = variant == 2;
		for (int a = -half; a <= half; a++) {
			if (breach && Math.floorMod(a * 5 + 40, 11) < 2) continue; // breach gaps
			// Breached walls crumble to an uneven top; intact walls keep a level battlement.
			int top = breach ? height - Math.floorMod(a * a * 3 + a * 7, 4) : height;
			for (int p = -halfPerp; p <= halfPerp; p++) {
				int x = alongZ ? p : a;
				int z = alongZ ? a : p;
				b.set(x, 0, z, CRUST);
				for (int y = 1; y <= top; y++) {
					String block = y == top
							? (breach ? CRUST : CALCIFIED) // crumbled vs clean cap
							: facade(a, y);
					b.set(x, y, z, block);
				}
			}
			if (!breach && Math.floorMod(a, 2) == 0) { // neat merlons on both edges
				b.set(alongZ ? -halfPerp : a, height + 1, alongZ ? a : -halfPerp, CALCIFIED);
				b.set(alongZ ? halfPerp : a, height + 1, alongZ ? a : halfPerp, CALCIFIED);
			} else if (breach && Math.floorMod(a * 3 + 1, 7) == 0) { // an occasional surviving stub
				b.set(alongZ ? -halfPerp : a, top + 1, alongZ ? a : -halfPerp, CRUST);
			}
		}
		if (variant == 0) { // arrow slits on the outer face
			for (int a = -half + 2; a <= half - 2; a += 4) {
				int x = alongZ ? -halfPerp : a;
				int z = alongZ ? a : -halfPerp;
				b.set(x, 3, z, GLASS);
				b.set(x, 4, z, GLASS);
			}
		}
	}

	// --- gatehouse side ------------------------------------------------------

	/**
	 * A full side like {@link #wallSide} but with a central arched tunnel and a raised gatehouse.
	 * Built along X, facing -Z outward; the arch passes cleanly through all three thicknesses.
	 */
	private static void gateSide(Builder b, boolean ruined) {
		int half = 13;
		int height = 7;
		int halfZ = 1;
		for (int x = -half; x <= half; x++) {
			for (int z = -halfZ; z <= halfZ; z++) {
				b.set(x, 0, z, CRUST);
				for (int y = 1; y <= height; y++) {
					if (Math.abs(x) <= 2 && y <= 6) continue; // arch tunnel through the wall
					if (ruined && Math.floorMod(x * 3 + y, 7) == 0 && y > 4) continue;
					b.set(x, y, z, y == height ? CALCIFIED : facade(x, y));
				}
			}
			if (Math.floorMod(x, 2) == 0) {
				b.set(x, height + 1, -halfZ, CALCIFIED);
				b.set(x, height + 1, halfZ, CALCIFIED);
			}
		}
		// arch keystone trim over the opening
		for (int x = -3; x <= 3; x++)
			for (int z = -halfZ; z <= halfZ; z++)
				b.set(x, 7, z, CALCIFIED);
		if (!ruined) {
			// raised gatehouse block above the arch
			for (int y = height + 1; y <= height + 4; y++)
				for (int x = -3; x <= 3; x++)
					for (int z = -halfZ; z <= halfZ; z++) {
						boolean edge = Math.abs(x) == 3 || Math.abs(z) == halfZ;
						b.set(x, y, z, edge ? (y == height + 4 ? CALCIFIED : facade(x, y)) : (y == height + 4 ? CRUST : "minecraft:air"));
					}
			spire(b, -3, 0, height + 5, 1, 4);
			spire(b, 3, 0, height + 5, 1, 4);
		}
	}

	// --- shared helpers ------------------------------------------------------

	private static void spire(Builder b, int cx, int cz, int startY, int radius, int height) {
		for (int layer = 0; layer < height; layer++) {
			int r = Math.max(0, radius - (layer * radius / Math.max(1, height - 1)));
			for (int x = -r; x <= r; x++) {
				for (int z = -r; z <= r; z++) {
					boolean spireEdge = Math.abs(x) == r || Math.abs(z) == r;
					b.set(cx + x, startY + layer, cz + z,
							spireEdge || r == 0 ? (layer % 3 == 0 ? CALCIFIED : DARK_BRICK) : CRUST);
				}
			}
		}
	}

	private static void buttresses(Builder b, int halfX, int halfZ, int height) {
		int[] xs = {-halfX, halfX};
		int[] zs = {-halfZ, halfZ};
		for (int x : xs) {
			for (int z : zs) {
				for (int y = 1; y <= height; y++) {
					b.set(x, y, z, CALCIFIED);
				}
			}
		}
	}

	private static void gothicWindow(Builder b, int x, int y, int z, int height, boolean front) {
		for (int i = 0; i < height; i++) {
			int yy = y + i;
			if (front) {
				b.set(x, yy, z, GLASS);
				b.set(x - 1, yy, z, CALCIFIED);
				b.set(x + 1, yy, z, CALCIFIED);
			} else {
				b.set(x, yy, z, GLASS);
				b.set(x, yy, z - 1, CALCIFIED);
				b.set(x, yy, z + 1, CALCIFIED);
			}
			if (i == height - 1) b.set(x, yy + 1, z, CALCIFIED);
		}
	}

	/**
	 * Axis-aligned masonry facade for a wall block, given its horizontal position {@code u} along
	 * the face and height {@code y}. Produces a dark plinth, horizontal string courses, vertical
	 * pilasters, and crust panels between -- reads as coursed stonework rather than diagonal stripes.
	 */
	private static String facade(int u, int y) {
		if (y <= 1) return DARK_STONE;                   // plinth base course
		if (Math.floorMod(y, 4) == 0) return DARK_BRICK; // horizontal string course
		if (Math.floorMod(u, 5) == 0) return CALCIFIED;  // vertical pilaster
		return CRUST;                                    // recessed panel
	}

	// --- builder with normalization ------------------------------------------

	private static final class Builder {
		private final Map<Position, String> blocks = new LinkedHashMap<>();
		private final List<double[]> fargones = new ArrayList<>(); // x,y,z,variant
		private final List<double[]> thirsters = new ArrayList<>(); // x,y,z

		void set(int x, int y, int z, String id) {
			blocks.put(new Position(x, y, z), id);
		}

		void fargone(int x, int y, int z, FargoneVariant variant) {
			fargones.add(new double[] {x + 0.5, y, z + 0.5, variant.id()});
		}

		void thirster(int x, int y, int z) {
			thirsters.add(new double[] {x + 0.5, y, z + 0.5});
		}

		Built normalize() {
			int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
			for (Position p : blocks.keySet()) {
				minX = Math.min(minX, p.x()); minY = Math.min(minY, p.y()); minZ = Math.min(minZ, p.z());
				maxX = Math.max(maxX, p.x()); maxY = Math.max(maxY, p.y()); maxZ = Math.max(maxZ, p.z());
			}
			int[] size = {maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1};

			List<BlockPlacement> outBlocks = new ArrayList<>(blocks.size());
			for (Map.Entry<Position, String> e : blocks.entrySet()) {
				Position p = e.getKey();
				outBlocks.add(new BlockPlacement(p.x() - minX, p.y() - minY, p.z() - minZ, e.getValue()));
			}

			List<EntityPlacement> outEntities = new ArrayList<>(fargones.size() + thirsters.size());
			for (double[] f : fargones)
				outEntities.add(new EntityPlacement(f[0] - minX, f[1] - minY, f[2] - minZ, "hemomancy:fargone", (int) f[3]));
			for (double[] t : thirsters)
				outEntities.add(new EntityPlacement(t[0] - minX, t[1] - minY, t[2] - minZ, "hemomancy:thirster", -1));

			return new Built(size, List.copyOf(outBlocks), List.copyOf(outEntities));
		}
	}
}
