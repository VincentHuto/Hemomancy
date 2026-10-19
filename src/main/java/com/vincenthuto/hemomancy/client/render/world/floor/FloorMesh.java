package com.vincenthuto.hemomancy.client.render.world.floor;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierMount;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything about a consecrated floor's mesh that only changes when blocks around the Focus do: the membrane's
 * cells and drapes, the brazier trench's centre line with its grounding, reaches, ground colours and taper, and each
 * seated brazier's mound profile. Built once per Focus and kept until its floor, orientation or footprint changes,
 * so a frame only emits vertices and works out the values that move.
 */
public final class FloorMesh {
	/** Above the floor's top face, below any slab-height block placed on it. */
	public static final float SURFACE_Y = 1.004F;
	/** The Focus's basin floor, inside its one-pixel rim; the membrane pools there instead of covering the medium. */
	private static final float BASIN_Y = 0.754F;
	private static final float BASIN_INSET = 1.0F / 16.0F;
	/** How far the membrane can hang down an exposed side face; the shader's ragged hem ends it sooner. */
	private static final float DROOP = 0.9F;
	/** Drapes stand just off the side face; the surface above reaches out to meet them so no sliver shows. */
	private static final float DRAPE_OFFSET = 0.002F;
	/** A drape end with nothing to carry on into curls its hem up to nothing over this width. */
	private static final float DRAPE_END_TAPER = 0.35F;
	/** Green on a drape vertex runs from this value at a curled-up end to 255 where the hem hangs freely. */
	private static final float DRAPE_END_GREEN = 0.78F;
	/**
	 * Blue between these marks a flat membrane vertex and carries its signed distance inside the trench's centre line,
	 * so the shader cuts the membrane exactly there.
	 */
	private static final float LOOP_BLUE_CENTRE = 0.5F;
	private static final float LOOP_BLUE_SPAN = 0.1F;
	/** Blue on the flat membrane where no trench line trims it. */
	private static final int FLAT_BLUE = 191;
	/** Half the width of the Focus pad's quad; the shader grows the ragged pad and its rootlets inside it. */
	private static final float PAD_HALF = 1.45F;
	private static final float PAD_Y = 1.008F;
	/** Green 64 marks the Focus pad for the shader; red and blue carry the position inside the pad. */
	private static final int PAD_LAYER = 64;
	/** Light for a seat mound's slope shading, from high above; a flat patch comes out at full brightness. */
	private static final Vector3f SLOPE_LIGHT = new Vector3f(0.3F, 1.0F, 0.5F).normalize();
	private static final float FLAT_SHADE = 0.55F + 0.45F * SLOPE_LIGHT.y();
	/** The brazier gash lies just above the ground the braziers stand on. */
	private static final float GASH_Y = 0.012F;
	/** Half the width of the gash's strip; the shader tears the ragged gash and its capillaries inside it. */
	public static final float GASH_HALF = 0.8F;
	private static final int DIG_ALPHA_SPAN = 190;
	/** Earth-brown, for a trench wall whose ground colour cannot be read. */
	private static final int DEFAULT_GROUND = 0x7F5B3E;
	/** How far into each wall the ground it cuts through is sampled. */
	private static final float WALL_SAMPLE = 0.3F;
	/** Strip reaches tried at each side of the trench, widest first, until one lies wholly over ground. */
	private static final float[] GASH_REACHES = {GASH_HALF, 0.62F, 0.48F, 0.36F};
	/** Where the gash is broken by a gap in the ground, it pinches closed over this length approaching the break. */
	private static final float GASH_TAPER = 0.8F;
	/** The shortest sideways vector a gash vertex carries; its length beyond this encodes the taper. */
	private static final float GASH_MIN_LEAN = 0.05F;
	/** How far from a socket the gash still widens toward the clot pool a brazier stands in. */
	private static final float GASH_SWELL_RADIUS = 0.65F;
	/**
	 * Green 8 marks the gash for the shader; red runs across it, blue swells toward each socket, and the texture
	 * coordinates carry its sideways direction, shared at each join so the trench has no seams between sections.
	 * Their length carries the taper toward a break.
	 */
	private static final int GASH_LAYER = 8;
	/**
	 * The flesh that swallows the foot of a brazier seated on the ring: it rises from the plug to cover the dais and
	 * the bottom of the stem. Radii and heights are in blocks from the socket centre and the ground.
	 */
	private static final float SEAT_INNER = 0.1F;
	private static final float SEAT_STEM = 0.2F;
	private static final float SEAT_OUTER = 0.7F;
	private static final float SEAT_PEAK = 0.24F;
	private static final float SEAT_SHOULDER = 0.14F;
	/** The seat mound fades in from the floor over its outer part, so there is no edge where it meets it. */
	private static final float SEAT_FADE = 0.3F;
	private static final int SEAT_RINGS = 12;
	private static final int SEAT_SECTORS = 56;
	private static final float SEAT_STEP = 0.02F;
	/**
	 * A brazier beside the Focus would push its mound through the Focus throat, so a mound melts into the floor
	 * between these distances from the Focus centre: gone over the throat's lumpy rim, whole by the near dais's edge.
	 */
	private static final float SEAT_THROAT_GONE = 0.55F;
	private static final float SEAT_THROAT_WHOLE = 0.8F;
	/**
	 * Green 166 marks a brazier's flesh mound for the shader, which shades it as the floor membrane itself; red carries
	 * its slope shading.
	 */
	private static final int SEAT_LAYER = 166;
	/**
	 * The throat the Focus mouth draws down into once a staff is planted: flesh sloping from under the pad's lip into
	 * the Focus basin, closing into a cuff round the staff's foot. The inverse of a brazier's seat mound, and drawn
	 * with it (depth-writing), so the staff below the cuff is hidden in the flesh. Radii and heights are in blocks
	 * from the Focus centre and its block's base; the basin's floor is at 0.75.
	 */
	/** The rim's smallest radius; lumps only push it outward, so it always reaches under the pad's ragged opening. */
	private static final float THROAT_OUTER = 0.5F;
	private static final float THROAT_TOP = 1.006F;
	private static final float THROAT_DEPTH = 0.22F;
	/** How far the cuff rides back up the staff. */
	private static final float THROAT_CUFF = 0.08F;
	/** The cuff's radius gripping the staff, and how open it gapes before the staff is driven home. */
	private static final float THROAT_GRIP = 0.12F;
	private static final float THROAT_GAPE = 0.3F;
	/** Half the Focus basin's inner width (its walls are a pixel thick), with a hair of margin. */
	private static final float THROAT_BASIN_HALF = 0.43F;
	private static final int THROAT_RINGS = 14;
	private static final int THROAT_SECTORS = 40;
	/** Blue 0 on a seat-layer vertex marks the throat, so the shader keeps the Focus basin's dark clot off it. */
	private static final int THROAT_FLAG = 0;

	/**
	 * Green 128 marks the ground beyond the trench, drawn only while a grounding rite's pulse runs out through it;
	 * blue carries the trench distance, so the shader keeps it outside the line.
	 */
	private static final int RING_LAYER = 128;
	/** The ground ring is drawn only on cells this close to its travelling band. */
	private static final float RING_BAND_AHEAD = 2.0F;
	private static final float RING_BAND_BEHIND = 3.5F;
	/** The creep's furthest reach past the floor's edge (RiteWorldMath.creepReach's ceiling) plus a margin. */
	public static final int CREEP_MARGIN = 13;
	/** How far above or below the Focus's level a creep cell may find its ground. */
	private static final int CREEP_DROP = 3;
	/** Red on a ground-layer vertex: 0 marks the world stage's creep, 255 the grounding ring's passing pulse. */
	private static final int CREEP_RED = 0;

	/** Each block's average texture colour, for the trench walls cut through it; cleared on resource reload. */
	private static final Map<BlockState, Integer> GROUND_COLOURS = new HashMap<>();
	private static final int GROUND_COLOUR_LIMIT = 512;
	private static final int CACHE_LIMIT = 128;
	/** How often the blocks under a cached mesh are re-checked for changes. */
	private static final int FOOTPRINT_CHECK_TICKS = 5;
	/**
	 * While a brazier's mound and moat ease in or out, the mesh is rebuilt every tick, and each frame blends from the
	 * build before it to this one, so the trench and mound move continuously between ticks.
	 */
	private static final int TRANSITION_STEP_TICKS = 1;
	/** How far past the floor's half-width the mesh can read blocks: the trench, its reaches and the drapes. */
	private static final int FOOTPRINT_MARGIN = 5;
	private static final Map<Long, Cached> CACHE = new HashMap<>();
	/** Bumped on every resource reload, so meshes built with old ground colours are rebuilt. */
	private static int generation;

	private final int half;
	private final GashLoop loop;
	private final float[] surface;
	private final int surfaceCount;
	// Brazier trench, per centre-line sample; arrays sized samples + 1 repeat the first sample at the end.
	private final int samples;
	private final float gashY;
	private final boolean[] grounded;
	private final float[] taper;
	private final float[] fromStart;
	private final float[][] normals;
	private final float[][] reaches;
	private final int[][] groundColours;
	private final int[] swell;
	private final float[] sampleRadius;
	private final List<Seat> seats;
	/** A root from the floor's rim to each seated brazier beyond it. */
	private final List<Root> roots;
	private final float seatGround;
	private List<float[]> ringCells;
	private CreepCells creepCells;
	/** The build this one replaced while a mound and moat were easing, blended from over a tick; null when settled. */
	private FloorMesh previous;
	/** The game tick this mesh was built on. */
	private long builtAt;

	private FloorMesh(int half, GashLoop loop, VertexList surface, Gash gash, List<Seat> seats, List<Root> roots,
			float seatGround) {
		this.half = half;
		this.loop = loop;
		this.surface = surface.data;
		this.surfaceCount = surface.count;
		this.samples = gash == null ? 0 : gash.samples;
		this.gashY = gash == null ? 0.0F : gash.y;
		this.grounded = gash == null ? new boolean[0] : gash.grounded;
		this.taper = gash == null ? new float[0] : gash.taper;
		this.fromStart = gash == null ? new float[0] : gash.fromStart;
		this.normals = gash == null ? new float[0][] : gash.normals;
		this.reaches = gash == null ? new float[0][] : gash.reaches;
		this.groundColours = gash == null ? new int[0][] : gash.groundColours;
		this.swell = gash == null ? new int[0] : gash.swell;
		this.sampleRadius = gash == null ? new float[0] : gash.radius;
		this.seats = seats;
		this.roots = roots;
		this.seatGround = seatGround;
	}

	/**
	 * The mesh for a Focus's formed floor, built on first use and kept until the floor, its orientation or the blocks
	 * in its footprint change, or resources reload. A brazier set on or taken off the floor eases its mound and moat in
	 * or out ({@link SeatTransitions}), the mesh rebuilt every few ticks until it settles.
	 */
	public static FloorMesh of(Level level, CardinalRiteFloorDefinition floor, BlockPos origin, Direction forwards) {
		long tick = level.getGameTime();
		int half = CardinalRiteFloorGeometry.halfWidth(floor);
		List<BlockPos> sockets = CardinalRiteFloorGeometry.sockets(floor, origin, forwards);
		List<BlockPos> standing = CardinalRiteFloorGeometry.standingCells(floor, origin, forwards);
		Cached cached = CACHE.get(origin.asLong());
		if (cached != null && cached.floor.equals(floor.id()) && cached.forwards == forwards
				&& cached.generation == generation && cached.level == System.identityHashCode(level)) {
			boolean easing = !cached.transitions.settled(cached.builtAt);
			long since = tick - (easing ? cached.builtAt : cached.checkedAt);
			if (tick >= cached.checkedAt && since >= 0 && since < (easing ? TRANSITION_STEP_TICKS : FOOTPRINT_CHECK_TICKS)) {
				return cached.mesh;
			}
			long footprint = footprint(level, origin, half, sockets);
			cached.checkedAt = tick;
			if (footprint != cached.footprint) {
				cached.transitions.target(seatedSockets(level, standing), tick);
			} else if (!easing) {
				return cached.mesh;
			}
			FloorMesh before = cached.mesh;
			before.previous = null;
			cached.mesh = build(level, floor, origin, forwards, half, sockets, cached.transitions, tick);
			cached.mesh.previous = before;
			cached.mesh.builtAt = tick;
			cached.footprint = footprint;
			cached.builtAt = tick;
			cached.transitions.prune(tick);
			return cached.mesh;
		}
		if (CACHE.size() > CACHE_LIMIT) CACHE.clear();
		SeatTransitions transitions = SeatTransitions.settledWith(seatedSockets(level, standing));
		FloorMesh mesh = build(level, floor, origin, forwards, half, sockets, transitions, tick);
		mesh.builtAt = tick;
		CACHE.put(origin.asLong(), new Cached(System.identityHashCode(level), floor.id(), forwards, generation,
				footprint(level, origin, half, sockets), tick, mesh, transitions));
		return mesh;
	}

	public GashLoop loop() {
		return loop;
	}

	public int samples() {
		return samples;
	}

	public boolean grounded(int sample) {
		return grounded[sample];
	}

	/** Arc length along the trench's centre line from where its dig front starts. */
	public float arc(int sample) {
		return fromStart[sample];
	}

	/** The trench's whole length around the loop. */
	public float loopLength() {
		return samples == 0 ? 0.0F : fromStart[samples];
	}

	/** Distance from the Focus centre to a centre-line sample. */
	public float sampleRadius(int sample) {
		return sampleRadius[sample];
	}

	/** The membrane: every covered cell's surface and drapes, and the pool in the Focus basin. */
	public void emitMembrane(VertexConsumer consumer, Matrix4f matrix, int alpha) {
		for (int i = 0; i < surfaceCount; i++) {
			int at = i * VertexList.STRIDE;
			consumer.addVertex(matrix, surface[at], surface[at + 1], surface[at + 2])
					.setUv(surface[at + 3], surface[at + 4])
					.setColor((int) surface[at + 5], (int) surface[at + 6], (int) surface[at + 7], alpha);
		}
	}

	/**
	 * The ground just beyond the trench, where a grounding rite's pulse runs out into the surrounding terrain. Only
	 * cells near the travelling band are emitted; the shader draws the band itself.
	 */
	public void emitGroundRing(VertexConsumer consumer, Matrix4f matrix, Level level, BlockPos origin, float radius,
			int alpha) {
		if (ringCells == null) ringCells = buildRingCells(level, origin);
		for (float[] cell : ringCells) {
			if (cell[2] < radius - RING_BAND_BEHIND || cell[2] > radius + RING_BAND_AHEAD) continue;
			float x0 = cell[0];
			float z0 = cell[1];
			ringVertex(consumer, matrix, x0, z0, cell[3], alpha);
			ringVertex(consumer, matrix, x0, z0 + 1.0F, cell[4], alpha);
			ringVertex(consumer, matrix, x0 + 1.0F, z0 + 1.0F, cell[5], alpha);
			ringVertex(consumer, matrix, x0 + 1.0F, z0, cell[6], alpha);
		}
	}

	/**
	 * The world stage's creep: grounded cells beyond the trench out to {@code radius} from the Focus centre, each at
	 * its own ground height so the membrane follows the terrain. {@link CreepCells} re-reads the ground every few
	 * seconds, out to the furthest the creep can reach; the shader draws its ragged front.
	 */
	public void emitCreep(VertexConsumer consumer, Matrix4f matrix, Level level, BlockPos origin, float radius,
			int alpha) {
		if (radius <= 0.0F) return;
		if (creepCells == null) creepCells = new CreepCells(half, CREEP_MARGIN, CREEP_DROP);
		List<float[]> cells = creepCells.cells(level.getGameTime(),
				(x, dy, z) -> spillable(level, origin.offset(x, dy, z)),
				(x, z) -> loop != null ? loop.reachesOutside(x, z) : Math.abs(x) > half || Math.abs(z) > half,
				this::ringBlue);
		for (float[] cell : cells) {
			if (cell[2] > radius + 1.5F) continue;
			float x0 = cell[0];
			float z0 = cell[1];
			float y = cell[7];
			creepVertex(consumer, matrix, x0, y, z0, cell[3], alpha);
			creepVertex(consumer, matrix, x0, y, z0 + 1.0F, cell[4], alpha);
			creepVertex(consumer, matrix, x0 + 1.0F, y, z0 + 1.0F, cell[5], alpha);
			creepVertex(consumer, matrix, x0 + 1.0F, y, z0, cell[6], alpha);
		}
	}

	private static void creepVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float blue,
			int alpha) {
		consumer.addVertex(matrix, x, y, z).setUv(x - 0.5F, z - 0.5F)
				.setColor(CREEP_RED, RING_LAYER, (int) blue, alpha);
	}

	private static void ringVertex(VertexConsumer consumer, Matrix4f matrix, float x, float z, float blue, int alpha) {
		consumer.addVertex(matrix, x, SURFACE_Y, z).setUv(x - 0.5F, z - 0.5F)
				.setColor(255, RING_LAYER, (int) blue, alpha);
	}

	/**
	 * The trench, carved around the loop by the dig front as the floor forms. {@code riteGlow}, if given, adds the
	 * rite's own glow per centre-line sample on top of the dig front's.
	 */
	public void emitGash(VertexConsumer consumer, Matrix4f matrix, float age, float[] riteGlow, double time) {
		if (loop == null) return;
		float[][] centre = loop.centre();
		// While a moat is easing, the trench slides from the build before this one to this one across the tick.
		float blend = blend(time);
		FloorMesh from = blend < 1.0F && previous != null && previous.loop != null && previous.samples == samples
				? previous : null;
		float[][] fromCentre = from == null ? null : from.loop.centre();
		// While the dig front is still going round, the trench pinches shut behind it at the point where it started,
		// instead of ending in a flat cut; once the front meets it again the loop joins.
		float joined = CardinalRiteFloorVisualRules.digProgress(age, samples - 1, samples);
		for (int i = 0; i < samples; i++) {
			if (!grounded[i] || !grounded[i + 1]) continue;
			// The dig front carves the trench open around the loop: it widens and deepens from a glowing seam.
			float digA = CardinalRiteFloorVisualRules.digProgress(age, i, samples);
			float digB = CardinalRiteFloorVisualRules.digProgress(age, i + 1, samples);
			if (digA <= 0.001F && digB <= 0.001F) continue;
			int next = (i + 1) % samples;
			int glowA = digAlpha(digA, riteGlow == null ? 0.0F : riteGlow[i]);
			int glowB = digAlpha(digB, riteGlow == null ? 0.0F : riteGlow[i + 1]);
			float carveA = Math.min(taper[i], digA * Math.max(digTail(fromStart[i]), joined));
			float carveB = Math.min(taper[i + 1], digB * Math.max(digTail(fromStart[i + 1]), joined));
			float[] centreA = centre[i];
			float[] centreB = centre[i + 1];
			float[] normalA = normals[i];
			float[] normalB = normals[next];
			float[] reachA = reaches[i];
			float[] reachB = reaches[next];
			if (from != null) {
				centreA = mix(fromCentre[i], centreA, blend);
				centreB = mix(fromCentre[i + 1], centreB, blend);
				normalA = normalised(mix(from.normals[i], normalA, blend));
				normalB = normalised(mix(from.normals[next], normalB, blend));
				reachA = mix(from.reaches[i], reachA, blend);
				reachB = mix(from.reaches[next], reachB, blend);
			}
			gashVertex(consumer, matrix, centreA, normalA, carveA, 1.0F, reachA[0], swell[i], glowA,
					groundColours[i][0]);
			gashVertex(consumer, matrix, centreA, normalA, carveA, -1.0F, reachA[1], swell[i], glowA,
					groundColours[i][1]);
			gashVertex(consumer, matrix, centreB, normalB, carveB, -1.0F, reachB[1], swell[i + 1],
					glowB, groundColours[next][1]);
			gashVertex(consumer, matrix, centreB, normalB, carveB, 1.0F, reachB[0], swell[i + 1],
					glowB, groundColours[next][0]);
		}
	}

	/** How far a frame at {@code time} has blended from the previous build to this one, 0 to 1. */
	private float blend(double time) {
		if (previous == null) return 1.0F;
		return (float) Math.max(0.0D, Math.min(1.0D, (time - builtAt) / TRANSITION_STEP_TICKS));
	}

	static float[] mix(float[] a, float[] b, float t) {
		// The previous build only fills samples whose ground held; an ungrounded one has nothing to blend from.
		if (a == null || a.length != b.length) return b;
		float[] out = new float[b.length];
		for (int i = 0; i < b.length; i++) out[i] = a[i] + (b[i] - a[i]) * t;
		return out;
	}

	private static float[] normalised(float[] v) {
		float length = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]);
		return length < 1.0e-6F ? v : new float[] {v[0] / length, v[1] / length};
	}

	public boolean hasSeats() {
		return !seats.isEmpty();
	}

	/** How many roots wire seated braziers into the floor. */
	public int rootCount() {
		return roots.size();
	}

	/** A lumpy mound of flesh around each seated brazier, swallowing its dais and the foot of its stem. */
	public void emitSeats(VertexConsumer consumer, Matrix4f matrix, float beat, float spread, int alpha, double time) {
		float blend = blend(time);
		if (alpha <= 2) return;
		float swelling = 1.0F + 0.05F * beat;
		int points = (SEAT_RINGS + 1) * SEAT_SECTORS;
		float[] heights = new float[points];
		float[] shades = new float[points];
		for (Seat seat : seats) {
			// The mound rises as the membrane's edge reaches it, and sinks back as a collapsing floor draws away.
			float grown = seat.previousWeight + (seat.weight - seat.previousWeight) * blend;
			float rise = CardinalRiteFloorVisualRules.seatRise(spread, seat.distance) * grown;
			if (rise <= 0.01F) continue;
			int seatAlpha = Math.round(alpha * Math.min(1.0F, grown));
			for (int k = 0; k < points; k++) {
				heights[k] = (seat.h0[k] * swelling + seat.wrinkle[k]) * rise * seat.clear[k];
				float dr = (seat.h0Radial[k] * swelling + seat.wrinkleRadial[k]) * rise * seat.clear[k] / (2.0F * SEAT_STEP);
				float dt = (seat.h0Around[k] * swelling + seat.wrinkleAround[k]) * rise * seat.clear[k]
						/ (2.0F * SEAT_STEP * Math.max(seat.radius[k], 0.05F));
				float cos = seat.cos[k];
				float sin = seat.sin[k];
				float nx = -(dr * cos - dt * sin);
				float nz = -(dr * sin + dt * cos);
				float length = (float) Math.sqrt(nx * nx + 1.0F + nz * nz);
				float lit = (nx * SLOPE_LIGHT.x() + SLOPE_LIGHT.y() + nz * SLOPE_LIGHT.z()) / length;
				shades[k] = Math.min(1.0F, (0.55F + 0.45F * Math.max(0.0F, lit)) / FLAT_SHADE);
			}
			for (int ring = 0; ring < SEAT_RINGS; ring++) {
				for (int sector = 0; sector < SEAT_SECTORS; sector++) {
					int after = (sector + 1) % SEAT_SECTORS;
					seatVertex(consumer, matrix, seat, ring * SEAT_SECTORS + sector, heights, shades, seatAlpha);
					seatVertex(consumer, matrix, seat, (ring + 1) * SEAT_SECTORS + sector, heights, shades, seatAlpha);
					seatVertex(consumer, matrix, seat, (ring + 1) * SEAT_SECTORS + after, heights, shades, seatAlpha);
					seatVertex(consumer, matrix, seat, ring * SEAT_SECTORS + after, heights, shades, seatAlpha);
				}
			}
		}
		// Each root grows out from the rim as the membrane's edge reaches its brazier, and swells on the beat.
		for (Root root : roots) {
			float rise = CardinalRiteFloorVisualRules.seatRise(spread, root.distance);
			if (rise <= 0.01F) continue;
			root.emit(consumer, matrix, rise, swelling, alpha);
		}
	}

	private void seatVertex(VertexConsumer consumer, Matrix4f matrix, Seat seat, int k, float[] heights,
			float[] shades, int alpha) {
		// It rises from the floor surface itself, sharing its texture coordinates, and fades in from its rim.
		float x = seat.x[k];
		float z = seat.z[k];
		float y = seatGround - 1.0F + SURFACE_Y + heights[k];
		consumer.addVertex(matrix, x, y, z).setUv(x - 0.5F, z - 0.5F)
				.setColor(Math.round(255 * shades[k]), SEAT_LAYER, 255, Math.round(alpha * seat.rim[k]));
	}

	/**
	 * The Focus throat gripping a planted staff, {@code grip} of the way from gaping to closed. It clenches a little on
	 * each heartbeat and gathers into folds as it pulls toward the staff.
	 */
	public static void emitThroat(VertexConsumer consumer, Matrix4f matrix, float grip, float beat, float time,
			float seed, int alpha) {
		if (grip <= 0.001F || alpha <= 2) return;
		float depth = THROAT_DEPTH * grip;
		float cuff = THROAT_CUFF * grip;
		float inner = (THROAT_GAPE + (THROAT_GRIP - THROAT_GAPE) * grip) * (1.0F - 0.05F * beat);
		int points = (THROAT_RINGS + 1) * THROAT_SECTORS;
		float[] xs = new float[points];
		float[] ys = new float[points];
		float[] zs = new float[points];
		int[] shades = new int[points];
		for (int ring = 0; ring <= THROAT_RINGS; ring++) {
			float t = ring / (float) THROAT_RINGS;
			float radius = THROAT_OUTER + (inner - THROAT_OUTER) * (float) Math.pow(t, 0.9D);
			float height = throatHeight(t, depth, cuff);
			// The slope's lighting: a surface y = f(r) faces inward and up, steeper further down.
			float step = 0.5F / THROAT_RINGS;
			float slope = (throatHeight(Math.max(0.0F, t - step), depth, cuff)
					- throatHeight(Math.min(1.0F, t + step), depth, cuff))
					/ Math.max(0.001F, Math.abs((THROAT_OUTER - inner)) * 2.0F * step);
			float sunk = (THROAT_TOP - height) / Math.max(0.001F, THROAT_DEPTH);
			for (int sector = 0; sector < THROAT_SECTORS; sector++) {
				int k = ring * THROAT_SECTORS + sector;
				float theta = (float) (Math.PI * 2.0D * sector / THROAT_SECTORS);
				// A lumpy rim and an uneven fall, so the throat is never a turned bowl.
				float lumps = 0.5F + 0.5F * (float) Math.sin(theta * 3.0F + seed)
						+ 0.3F * (float) Math.sin(theta * 5.0F + seed * 1.7F)
						+ 0.15F * (float) Math.sin(theta * 11.0F + seed * 2.3F);
				float rim = 1.0F + 0.09F * Math.max(0.0F, lumps) * (1.0F - t);
				float sag = 1.0F + 0.18F * (float) Math.sin(theta * 2.0F + seed * 0.7F) * t;
				// Creases gathered toward the staff, twisting as they run down like a closing sphincter.
				double twist = theta + t * 0.9D + seed;
				float crease = (float) Math.pow(1.0D - Math.abs(Math.sin(twist * 3.5D)), 3.0D);
				float creaseWeight = 4.0F * t * (1.0F - t) * grip;
				// A slow swallowing ripple runs down the slope, and a lighter writhe across it.
				float swallow = 0.012F * (float) Math.sin(t * 7.0F - time * 0.12F + seed) * t * grip;
				float writhe = 0.006F * (float) Math.sin(theta * 4.0F - time * 0.013F + seed) * creaseWeight;
				float r = radius * rim * (1.0F - 0.06F * crease * creaseWeight);
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);
				xs[k] = 0.5F + cos * r;
				zs[k] = 0.5F + sin * r;
				ys[k] = THROAT_TOP - (THROAT_TOP - height) * sag + 0.022F * crease * creaseWeight + swallow + writhe;
				// Over the Focus block's rim the flesh stays on top of it, folding over the edge before it drops into the
				// basin; dipping below the rim would let the iron show through between the pad and the slope.
				float overRim = Math.max(Math.abs(xs[k] - 0.5F), Math.abs(zs[k] - 0.5F));
				if (overRim > THROAT_BASIN_HALF) ys[k] = Math.max(ys[k], THROAT_TOP - 0.002F);
				float nx = -slope * cos;
				float nz = -slope * sin;
				float length = (float) Math.sqrt(nx * nx + 1.0F + nz * nz);
				float lit = (nx * SLOPE_LIGHT.x() + SLOPE_LIGHT.y() + nz * SLOPE_LIGHT.z()) / length;
				float shade = Math.min(1.0F, (0.55F + 0.45F * Math.max(0.0F, lit)) / FLAT_SHADE);
				// Deeper in the throat it falls into shadow, crease valleys deepest; the cuff catches a little light again.
				shade *= 1.0F - 0.5F * Math.min(1.0F, Math.max(0.0F, sunk * sag));
				shade *= 0.8F + 0.2F * crease;
				shades[k] = Math.round(255 * Math.max(0.0F, Math.min(1.0F, shade)));
			}
		}
		for (int ring = 0; ring < THROAT_RINGS; ring++) {
			for (int sector = 0; sector < THROAT_SECTORS; sector++) {
				int after = (sector + 1) % THROAT_SECTORS;
				throatVertex(consumer, matrix, ring * THROAT_SECTORS + sector, xs, ys, zs, shades, alpha);
				throatVertex(consumer, matrix, (ring + 1) * THROAT_SECTORS + sector, xs, ys, zs, shades, alpha);
				throatVertex(consumer, matrix, (ring + 1) * THROAT_SECTORS + after, xs, ys, zs, shades, alpha);
				throatVertex(consumer, matrix, ring * THROAT_SECTORS + after, xs, ys, zs, shades, alpha);
			}
		}
		// The cuff closes over the staff's foot: a cap from its rim to the centre seals the throat, so nothing below
		// shows past a staff thinner than the grip. The staff still rises through it, hidden only beneath it.
		int rim = THROAT_RINGS * THROAT_SECTORS;
		float capY = 0.0F;
		for (int sector = 0; sector < THROAT_SECTORS; sector++) capY += ys[rim + sector];
		capY = capY / THROAT_SECTORS - 0.01F;
		int capShade = Math.round(shades[rim] * 0.6F);
		for (int sector = 0; sector < THROAT_SECTORS; sector++) {
			int after = (sector + 1) % THROAT_SECTORS;
			throatVertex(consumer, matrix, rim + sector, xs, ys, zs, shades, alpha);
			throatVertex(consumer, matrix, rim + after, xs, ys, zs, shades, alpha);
			consumer.addVertex(matrix, 0.5F, capY, 0.5F).setUv(0.0F, 0.0F)
					.setColor(capShade, SEAT_LAYER, THROAT_FLAG, alpha);
			consumer.addVertex(matrix, 0.5F, capY, 0.5F).setUv(0.0F, 0.0F)
					.setColor(capShade, SEAT_LAYER, THROAT_FLAG, alpha);
		}
	}

	/** The throat's height {@code t} of the way in from its rim: a slope falling away, then a cuff riding back up. */
	private static float throatHeight(float t, float depth, float cuff) {
		float fall = smooth(Math.min(1.0F, t / 0.82F));
		float rise = smooth(Math.max(0.0F, (t - 0.78F) / 0.22F));
		return THROAT_TOP - depth * fall + cuff * rise;
	}

	private static float smooth(float t) {
		return t * t * (3.0F - 2.0F * t);
	}

	private static void throatVertex(VertexConsumer consumer, Matrix4f matrix, int k, float[] xs, float[] ys,
			float[] zs, int[] shades, int alpha) {
		consumer.addVertex(matrix, xs[k], ys[k], zs[k]).setUv(xs[k] - 0.5F, zs[k] - 0.5F)
				.setColor(shades[k], SEAT_LAYER, THROAT_FLAG, alpha);
	}

	/**
	 * The Focus mouth as a fleshy pad: a ragged rim with rootlets creeping out over the floor. Its open centre shows
	 * the basin beneath, where the shader darkens the pool into a clot.
	 */
	public static void emitFocusPad(VertexConsumer consumer, Matrix4f matrix, int alpha) {
		if (alpha <= 2) return;
		padVertex(consumer, matrix, 0.5F - PAD_HALF, 0.5F - PAD_HALF, 0, 0, alpha);
		padVertex(consumer, matrix, 0.5F - PAD_HALF, 0.5F + PAD_HALF, 0, 255, alpha);
		padVertex(consumer, matrix, 0.5F + PAD_HALF, 0.5F + PAD_HALF, 255, 255, alpha);
		padVertex(consumer, matrix, 0.5F + PAD_HALF, 0.5F - PAD_HALF, 255, 0, alpha);
	}

	private static void padVertex(VertexConsumer consumer, Matrix4f matrix, float x, float z, int padX, int padZ,
			int alpha) {
		consumer.addVertex(matrix, x, PAD_Y, z).setUv(x - 0.5F, z - 0.5F).setColor(padX, PAD_LAYER, padZ, alpha);
	}

	// ---- Building ----

	private static FloorMesh build(Level level, CardinalRiteFloorDefinition floor, BlockPos origin,
			Direction forwards, int half, List<BlockPos> sockets, SeatTransitions transitions, long tick) {
		float seatGround = sockets.isEmpty() ? 1.0F : sockets.getFirst().getY() - origin.getY();
		// The trench runs round the floor's gash, a circle just inside its edge. A brazier standing anywhere on the floor
		// sinks into a mound of membrane; the trench swings out round the back of each one on the gash, like a moat, so
		// it stands on the membrane. Sconces and hanging braziers leave the floor alone. (Roots wiring off-floor
		// braziers in are kept for later use: Root, GashLoop.rim.)
		Set<BlockPos> gashCells = new HashSet<>(sockets);
		List<float[]> moatCentres = new ArrayList<>();
		List<Float> moatWeights = new ArrayList<>();
		List<Seat> seats = new ArrayList<>();
		for (BlockPos cell : transitions.cells()) {
			float weight = transitions.weight(cell, tick);
			float before = transitions.weight(cell, tick - TRANSITION_STEP_TICKS);
			if (weight <= 0.0F && before <= 0.0F) continue;
			float[] centre = {cell.getX() - origin.getX() + 0.5F, cell.getZ() - origin.getZ() + 0.5F};
			seats.add(new Seat(centre, before, weight));
			if (gashCells.contains(cell)) {
				moatCentres.add(centre);
				moatWeights.add(weight);
			}
		}
		float[] weights = new float[moatWeights.size()];
		for (int i = 0; i < weights.length; i++) weights[i] = moatWeights.get(i);
		GashLoop loop = GashLoop.circle(half, moatCentres, weights);
		List<Root> roots = List.of();
		VertexList surface = buildMembrane(level, floor, origin, forwards, half, loop);
		Gash gash = buildGash(level, origin, loop, seatGround + GASH_Y);
		return new FloorMesh(half, loop, surface, gash, seats, roots, seatGround);
	}

	private static VertexList buildMembrane(Level level, CardinalRiteFloorDefinition floor, BlockPos origin,
			Direction forwards, int half, GashLoop loop) {
		VertexList out = new VertexList();
		Set<BlockPos> covered = new HashSet<>(CardinalRiteFloorGeometry.cells(floor, origin, forwards));
		if (loop != null) {
			// Every grounded block that reaches inside the trench's centre line; the shader trims to the line itself.
			int reach = half + 4;
			for (int x = -reach; x <= reach; x++) {
				for (int z = -reach; z <= reach; z++) {
					BlockPos cell = origin.offset(x, 0, z);
					if (covered.contains(cell) || cell.equals(origin) || !spillable(level, cell)) continue;
					if (loop.reachesInside(x, z)) covered.add(cell);
				}
			}
		} else {
			// Without a trench to run out to, the membrane spills one block past the laid floor onto level ground.
			for (BlockPos cell : CardinalRiteFloorGeometry.margin(floor, origin)) {
				if (spillable(level, cell)) covered.add(cell);
			}
		}
		Set<BlockPos> solid = new HashSet<>(covered);
		solid.add(origin);
		// Over a drop, the membrane droops down the exposed side instead of stopping at the edge, along whatever part
		// of its top edge lies inside the trench line.
		Map<BlockPos, Set<Direction>> drapes = new HashMap<>();
		for (BlockPos cell : covered) {
			Set<Direction> draped = EnumSet.noneOf(Direction.class);
			for (Direction side : Direction.Plane.HORIZONTAL) {
				BlockPos beside = cell.relative(side);
				if (solid.contains(beside) || level.getBlockState(beside).isSolidRender(level, beside)) continue;
				if (loop != null && insideSpan(loop, cell, origin, side) == null) continue;
				draped.add(side);
			}
			drapes.put(cell, draped);
		}
		for (BlockPos cell : covered) {
			Set<Direction> draped = drapes.get(cell);
			for (Direction side : draped) {
				Direction towardA = Direction.fromDelta(side.getStepZ(), 0, -side.getStepX());
				drape(out, cell, origin, side, loop, drapeCarriesOn(drapes, cell, side, towardA),
						drapeCarriesOn(drapes, cell, side, towardA.getOpposite()));
			}
			// UVs are block offsets from the Focus centre, so the shader sees one unbroken surface. Over a drape
			// the surface reaches out to meet the drape's top edge.
			float x0 = cell.getX() - origin.getX() - (draped.contains(Direction.WEST) ? DRAPE_OFFSET : 0.0F);
			float x1 = cell.getX() - origin.getX() + 1.0F + (draped.contains(Direction.EAST) ? DRAPE_OFFSET : 0.0F);
			float z0 = cell.getZ() - origin.getZ() - (draped.contains(Direction.NORTH) ? DRAPE_OFFSET : 0.0F);
			float z1 = cell.getZ() - origin.getZ() + 1.0F + (draped.contains(Direction.SOUTH) ? DRAPE_OFFSET : 0.0F);
			flatQuad(out, loop, x0, z0, x1, z1, SURFACE_Y);
		}
		flatQuad(out, null, BASIN_INSET, BASIN_INSET, 1.0F - BASIN_INSET, 1.0F - BASIN_INSET, BASIN_Y);
		return out;
	}

	/** A flat membrane quad; under a trench line its corners carry their signed distance inside it. */
	private static void flatQuad(VertexList out, GashLoop loop, float x0, float z0, float x1, float z1, float y) {
		flatVertex(out, loop, x0, y, z0);
		flatVertex(out, loop, x0, y, z1);
		flatVertex(out, loop, x1, y, z1);
		flatVertex(out, loop, x1, y, z0);
	}

	private static void flatVertex(VertexList out, GashLoop loop, float x, float y, float z) {
		out.add(x, y, z, x - 0.5F, z - 0.5F, 255, 255, loop == null ? FLAT_BLUE : loopBlue(loop, x, z));
	}

	/** Blue encoding of how far inside the trench's centre line a surface point lies. */
	private static int loopBlue(GashLoop loop, float x, float z) {
		float inside = Math.max(-1.0F, Math.min(1.0F, loop.distance(x, z)));
		return Math.round(255 * (LOOP_BLUE_CENTRE + LOOP_BLUE_SPAN * inside));
	}

	/**
	 * The part of a side face's top edge that lies inside the trench line, as fractions along it from its first
	 * corner, or null if none does. Where the line crosses the edge the crossing is found by bisection.
	 */
	private static float[] insideSpan(GashLoop loop, BlockPos cell, BlockPos origin, Direction side) {
		float cx = cell.getX() - origin.getX() + 0.5F + side.getStepX() * 0.5F;
		float cz = cell.getZ() - origin.getZ() + 0.5F + side.getStepZ() * 0.5F;
		float tx = -side.getStepZ() * 0.5F;
		float tz = side.getStepX() * 0.5F;
		boolean startIn = loop.distance(cx - tx, cz - tz) > 0.0F;
		boolean endIn = loop.distance(cx + tx, cz + tz) > 0.0F;
		if (startIn && endIn) return new float[] {0.0F, 1.0F};
		if (!startIn && !endIn) return null;
		float lo = 0.0F;
		float hi = 1.0F;
		for (int step = 0; step < 12; step++) {
			float mid = (lo + hi) * 0.5F;
			boolean in = loop.distance(cx - tx + 2.0F * tx * mid, cz - tz + 2.0F * tz * mid) > 0.0F;
			if (in == startIn) lo = mid;
			else hi = mid;
		}
		float crossing = (lo + hi) * 0.5F;
		if (startIn ? crossing < 0.08F : crossing > 0.92F) return null;
		return startIn ? new float[] {0.0F, crossing} : new float[] {crossing, 1.0F};
	}

	/**
	 * Whether a drape's end toward {@code along} carries on: around the corner onto the same block's next face, or
	 * straight on along the ledge onto the neighbouring block's drape. Otherwise that end curls up.
	 */
	private static boolean drapeCarriesOn(Map<BlockPos, Set<Direction>> drapes, BlockPos cell, Direction side,
			Direction along) {
		if (drapes.get(cell).contains(along)) return true;
		Set<Direction> next = drapes.get(cell.relative(along));
		return next != null && next.contains(side);
	}

	/**
	 * A sheet hanging from the top edge of an exposed side face, in up to three strips so an end with nothing to carry
	 * on into can curl its hem up to nothing.
	 */
	private static void drape(VertexList out, BlockPos cell, BlockPos origin, Direction side, GashLoop loop,
			boolean carriesOnA, boolean carriesOnB) {
		float[] span = loop == null ? new float[] {0.0F, 1.0F} : insideSpan(loop, cell, origin, side);
		if (span == null) return;
		// An end cut short by the trench line always curls up there.
		carriesOnA &= span[0] <= 0.0F;
		carriesOnB &= span[1] >= 1.0F;
		float width = span[1] - span[0];
		float startTaper = carriesOnA ? 0.0F : Math.min(DRAPE_END_TAPER, width * 0.45F);
		float endTaper = carriesOnB ? 0.0F : Math.min(DRAPE_END_TAPER, width * 0.45F);
		float[] stops = {span[0], span[0] + startTaper, span[1] - endTaper, span[1]};
		float[] hem = {carriesOnA ? 1.0F : 0.0F, 1.0F, 1.0F, carriesOnB ? 1.0F : 0.0F};
		for (int strip = 0; strip < 3; strip++) {
			if (stops[strip + 1] - stops[strip] < 1.0e-4F) continue;
			drapeStrip(out, cell, origin, side, loop != null, stops[strip], stops[strip + 1], hem[strip],
					hem[strip + 1]);
		}
	}

	/**
	 * One strip of a drape between fractions {@code u0} and {@code u1} along its top edge. Its UVs keep running outward
	 * as it hangs, as if the membrane rolled over the lip. Red carries how far down a vertex hangs and green how freely
	 * its hem may hang, for the shader's ragged hem; under a trench line blue marks it as inside.
	 */
	private static void drapeStrip(VertexList out, BlockPos cell, BlockPos origin, Direction side, boolean trimmed,
			float u0, float u1, float hem0, float hem1) {
		float nx = side.getStepX();
		float nz = side.getStepZ();
		float cx = cell.getX() - origin.getX() + 0.5F + nx * 0.5F;
		float cz = cell.getZ() - origin.getZ() + 0.5F + nz * 0.5F;
		float tx = -nz * 0.5F;
		float tz = nx * 0.5F;
		float ax = cx - tx + 2.0F * tx * u0;
		float az = cz - tz + 2.0F * tz * u0;
		float bx = cx - tx + 2.0F * tx * u1;
		float bz = cz - tz + 2.0F * tz * u1;
		float px = nx * DRAPE_OFFSET;
		float pz = nz * DRAPE_OFFSET;
		float bottom = 1.0F - DROOP;
		int blue = trimmed ? Math.round(255 * (LOOP_BLUE_CENTRE + LOOP_BLUE_SPAN)) : 255;
		int greenA = Math.round(255 * (DRAPE_END_GREEN + (1.0F - DRAPE_END_GREEN) * hem0));
		int greenB = Math.round(255 * (DRAPE_END_GREEN + (1.0F - DRAPE_END_GREEN) * hem1));
		hangingVertex(out, ax + px, SURFACE_Y, az + pz, ax, az, 0.0F, greenA, blue);
		hangingVertex(out, ax + px, bottom, az + pz, ax + nx * DROOP, az + nz * DROOP, 1.0F, greenA, blue);
		hangingVertex(out, bx + px, bottom, bz + pz, bx + nx * DROOP, bz + nz * DROOP, 1.0F, greenB, blue);
		hangingVertex(out, bx + px, SURFACE_Y, bz + pz, bx, bz, 0.0F, greenB, blue);
	}

	private static void hangingVertex(VertexList out, float x, float y, float z, float surfaceX, float surfaceZ,
			float hang, int green, int blue) {
		out.add(x, y, z, surfaceX - 0.5F, surfaceZ - 0.5F, Math.round(255 * (1.0F - hang)), green, blue);
	}

	private static Gash buildGash(Level level, BlockPos origin, GashLoop gash, float y) {
		float[][] points = gash.points();
		float[][] centre = gash.centre();
		int samples = centre.length - 1;
		Gash out = new Gash(samples, y);
		for (int i = 0; i < samples; i++) out.grounded[i] = gashGround(level, origin, centre[i]);
		out.grounded[samples] = out.grounded[0];
		gashTaper(centre, out.grounded, samples, out.taper);
		for (int i = 1; i <= samples; i++) {
			float dx = centre[i][0] - centre[i - 1][0];
			float dz = centre[i][1] - centre[i - 1][1];
			out.fromStart[i] = out.fromStart[i - 1] + (float) Math.sqrt(dx * dx + dz * dz);
		}
		// Where a brazier already stands, the floor heaps up around its foot and the trench has swung out around it.
		// Only the empty sockets still open the trench wider.
		List<float[]> open = new ArrayList<>();
		for (int i = 0; i < points.length; i++) {
			if (!gash.seated()[i]) open.add(points[i]);
		}
		float[][] widening = open.toArray(float[][]::new);
		for (int i = 0; i <= samples; i++) {
			out.swell[i] = Math.round(255 * swell(centre[i], widening));
			out.radius[i] = (float) Math.hypot(centre[i][0] - 0.5F, centre[i][1] - 0.5F);
		}
		// On the inside of a tight bend the strip may reach no further than the bend's radius, or it folds back
		// over itself and the trench walls draw twice.
		float[][] bendLimit = gashBendLimits(centre, samples);
		// Each sample's sideways direction and how far the strip reaches either side, shared by the sections meeting
		// there. The loop is closed, so neighbours wrap around its start and the two sides of the seam agree.
		for (int k = 0; k < samples; k++) {
			if (!out.grounded[k]) continue;
			float[] normal = gashNormal(centre[Math.floorMod(k - 1, samples)], centre[(k + 1) % samples]);
			out.normals[k] = normal;
			int plus = groundColour(level, origin, centre[k][0] + normal[0] * WALL_SAMPLE,
					centre[k][1] + normal[1] * WALL_SAMPLE);
			int minus = groundColour(level, origin, centre[k][0] - normal[0] * WALL_SAMPLE,
					centre[k][1] - normal[1] * WALL_SAMPLE);
			// Beside a ledge a wall's sample can land in open air: borrow the ground under the trench, then the other
			// wall's, so the band never blends toward black.
			int under = groundColour(level, origin, centre[k][0], centre[k][1]);
			int fallback = under != 0 ? under : plus != 0 ? plus : minus != 0 ? minus : DEFAULT_GROUND;
			out.groundColours[k] = new int[] {plus != 0 ? plus : fallback, minus != 0 ? minus : fallback};
			out.reaches[k] = new float[] {gashReach(level, origin, centre[k], normal, 1.0F, bendLimit[k][0]),
					gashReach(level, origin, centre[k], normal, -1.0F, bendLimit[k][1])};
		}
		return out;
	}

	/** Sockets holding an Iron Brazier on sturdy ground: the braziers seated on the ring. */
	private static Set<BlockPos> seatedSockets(Level level, List<BlockPos> sockets) {
		Set<BlockPos> seated = new HashSet<>();
		for (BlockPos socket : sockets) {
			BlockPos ground = socket.below();
			BlockState state = level.getBlockState(socket);
			if (state.is(BlockInit.iron_brazier.get()) && BrazierMount.face(state) == AttachFace.FLOOR
					&& level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
				seated.add(socket);
			}
		}
		return seated;
	}

	private static boolean spillable(Level level, BlockPos cell) {
		BlockPos above = cell.above();
		return level.getBlockState(cell).isFaceSturdy(level, cell, Direction.UP)
				&& !level.getBlockState(above).isSolidRender(level, above);
	}

	/** Grounded cells around the floor with some part beyond the trench line, out as far as a grounding ring runs. */
	private List<float[]> buildRingCells(Level level, BlockPos origin) {
		List<float[]> cells = new ArrayList<>();
		float limit = CardinalRiteFloorVisualRules.reach(half) + CardinalRiteFloorVisualRules.GROUND_RING_BEYOND
				+ RING_BAND_AHEAD;
		int reach = (int) Math.ceil(limit);
		for (int x = -reach; x <= reach; x++) {
			for (int z = -reach; z <= reach; z++) {
				float radius = (float) Math.hypot(x, z);
				if (radius > limit || x == 0 && z == 0) continue;
				if (loop != null && !loop.reachesOutside(x, z)) continue;
				if (!spillable(level, origin.offset(x, 0, z))) continue;
				cells.add(new float[] {x, z, radius, ringBlue(x, z), ringBlue(x, z + 1), ringBlue(x + 1, z + 1),
						ringBlue(x + 1, z)});
			}
		}
		return cells;
	}

	private float ringBlue(float x, float z) {
		return loop == null ? 0.0F : loopBlue(loop, x, z);
	}

	/**
	 * 1 along unbroken gash, falling to 0 at the last grounded sample before each break, so the cut pinches shut
	 * there instead of ending open. Distances run along the loop in both directions.
	 */
	private static void gashTaper(float[][] centre, boolean[] grounded, int samples, float[] taper) {
		float[] along = new float[samples + 1];
		for (int i = 1; i <= samples; i++) {
			float dx = centre[i][0] - centre[i - 1][0];
			float dz = centre[i][1] - centre[i - 1][1];
			along[i] = along[i - 1] + (float) Math.sqrt(dx * dx + dz * dz);
		}
		float loop = along[samples];
		List<Integer> ends = new ArrayList<>();
		for (int i = 0; i < samples; i++) {
			int before = Math.floorMod(i - 1, samples);
			int after = (i + 1) % samples;
			if (grounded[i] && (!grounded[before] || !grounded[after])) ends.add(i);
		}
		for (int i = 0; i <= samples; i++) {
			float nearest = Float.MAX_VALUE;
			for (int end : ends) {
				float gap = Math.abs(along[i] - along[end]);
				nearest = Math.min(nearest, Math.min(gap, loop - gap));
			}
			float t = Math.min(1.0F, nearest / GASH_TAPER);
			taper[i] = t * t * (3.0F - 2.0F * t);
		}
	}

	/** 0 at the point where the dig front started, opening to 1 a short way along the trench from it. */
	private static float digTail(float distanceFromStart) {
		float t = Math.min(1.0F, distanceFromStart / GASH_TAPER);
		return t * t * (3.0F - 2.0F * t);
	}

	/**
	 * The trench is opaque, so its vertex alpha carries the dig front's glow instead: 255 where nothing is digging,
	 * falling to {@code 255 - DIG_ALPHA_SPAN} at the brightest point of the front or of the rite's own glow.
	 */
	private static int digAlpha(float progress, float riteGlow) {
		float glow = Math.min(1.0F, Math.max(CardinalRiteFloorVisualRules.digGlow(progress), riteGlow));
		return 255 - Math.round(DIG_ALPHA_SPAN * glow);
	}

	private static float[] gashNormal(float[] behind, float[] ahead) {
		float dx = ahead[0] - behind[0];
		float dz = ahead[1] - behind[1];
		float length = (float) Math.sqrt(dx * dx + dz * dz);
		return length < 1.0e-5F ? new float[] {1.0F, 0.0F} : new float[] {-dz / length, dx / length};
	}

	/** 1 at a socket, falling to 0 a little way along the gash from it. */
	private static float swell(float[] at, float[][] sockets) {
		float nearest = Float.MAX_VALUE;
		for (float[] socket : sockets) {
			float dx = at[0] - socket[0];
			float dz = at[1] - socket[1];
			nearest = Math.min(nearest, dx * dx + dz * dz);
		}
		return (float) Math.exp(-nearest / (GASH_SWELL_RADIUS * GASH_SWELL_RADIUS));
	}

	/** The gash only lies on sturdy ground with open space above, like the braziers it holds. */
	private static boolean gashGround(Level level, BlockPos origin, float[] at) {
		BlockPos ground = origin.offset((int) Math.floor(at[0]), 0, (int) Math.floor(at[1]));
		BlockPos above = ground.above();
		return level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)
				&& !level.getBlockState(above).isSolidRender(level, above);
	}

	/**
	 * How far the strip may reach to one side of the trench: its full width, or less where the ground runs out, so the
	 * lip shadow and capillaries never hang out over a drop.
	 */
	private static float gashReach(Level level, BlockPos origin, float[] centre, float[] normal, float side,
			float limit) {
		for (float candidate : GASH_REACHES) {
			float reach = Math.min(candidate, limit);
			float x = centre[0] + normal[0] * reach * side;
			float z = centre[1] + normal[1] * reach * side;
			float mx = centre[0] + normal[0] * reach * 0.5F * side;
			float mz = centre[1] + normal[1] * reach * 0.5F * side;
			if (gashGround(level, origin, new float[] {x, z}) && gashGround(level, origin, new float[] {mx, mz})) {
				return reach;
			}
		}
		return Math.min(GASH_REACHES[GASH_REACHES.length - 1], limit);
	}

	/**
	 * For each sample, how far the strip may reach to its +1 and -1 sides: unlimited on the outside of a bend, and
	 * within its radius of curvature on the inside, so the strip never folds over itself.
	 */
	private static float[][] gashBendLimits(float[][] centre, int samples) {
		float[][] limits = new float[samples][2];
		for (int i = 0; i < samples; i++) {
			float[] prev = centre[Math.floorMod(i - 1, samples)];
			float[] here = centre[i];
			float[] next = centre[(i + 1) % samples];
			float ax = here[0] - prev[0];
			float az = here[1] - prev[1];
			float bx = next[0] - here[0];
			float bz = next[1] - here[1];
			float cross = ax * bz - az * bx;
			limits[i][0] = Float.MAX_VALUE;
			limits[i][1] = Float.MAX_VALUE;
			if (Math.abs(cross) < 1.0e-6F) continue;
			float a = (float) Math.hypot(ax, az);
			float b = (float) Math.hypot(bx, bz);
			float c = (float) Math.hypot(next[0] - prev[0], next[1] - prev[1]);
			float radius = a * b * c / (2.0F * Math.abs(cross));
			// The strip's +1 side is its normal (-dz, dx), the left of travel; turning left puts it on the inside.
			limits[i][cross > 0.0F ? 0 : 1] = Math.max(0.2F, radius * 0.85F);
		}
		// Smooth the limits over about half a block of loop so the strip's edge does not kink from sample to sample.
		float[][] smoothed = new float[samples][2];
		for (int i = 0; i < samples; i++) {
			for (int side = 0; side < 2; side++) {
				float least = limits[i][side];
				for (int k = -5; k <= 5; k++) least = Math.min(least, limits[Math.floorMod(i + k, samples)][side]);
				smoothed[i][side] = least;
			}
		}
		return smoothed;
	}

	/**
	 * A trench vertex. Its UVs pack, for the vertex shader to unpack: x, the taper as a step from 0 to 1000 times 8 plus
	 * the sideways direction's angle; y, the colour of the ground the wall cuts through as a 24-bit integer, which a
	 * float holds exactly.
	 */
	private void gashVertex(VertexConsumer consumer, Matrix4f matrix, float[] centre, float[] normal, float taper,
			float side, float reach, int swell, int alpha, int groundColour) {
		float x = centre[0] + normal[0] * reach * side;
		float z = centre[1] + normal[1] * reach * side;
		float lean = GASH_MIN_LEAN + (1.0F - GASH_MIN_LEAN) * taper;
		float angle = (float) Math.atan2(normal[1], normal[0]);
		if (angle < 0.0F) angle += (float) (Math.PI * 2.0D);
		float packedSide = Math.round(lean * 1000.0F) * 8.0F + Math.min(angle, 6.28F);
		// Red runs across the strip at the same scale whatever its reach, so the shader's distances stay true.
		consumer.addVertex(matrix, x, gashY, z).setUv(packedSide, groundColour & 0xFFFFFF)
				.setColor(Math.round(255 * (0.5F + 0.5F * side * reach / GASH_HALF)), GASH_LAYER, swell, alpha);
	}

	/** The average texture colour of the ground block under a point, as 0xRRGGBB; 0 when it cannot be read. */
	private static int groundColour(Level level, BlockPos origin, float x, float z) {
		BlockPos at = origin.offset((int) Math.floor(x), 0, (int) Math.floor(z));
		BlockState state = level.getBlockState(at);
		if (state.isAir() || !state.isFaceSturdy(level, at, Direction.UP)) return 0;
		Integer known = GROUND_COLOURS.get(state);
		if (known != null) return known;
		if (GROUND_COLOURS.size() > GROUND_COLOUR_LIMIT) GROUND_COLOURS.clear();
		int colour = averageColour(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
				.getParticleIcon(state));
		GROUND_COLOURS.put(state, colour);
		return colour;
	}

	private static int averageColour(TextureAtlasSprite sprite) {
		var image = sprite.contents().getOriginalImage();
		long red = 0;
		long green = 0;
		long blue = 0;
		long count = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int abgr = image.getPixelRGBA(x, y);
				if ((abgr >>> 24) < 128) continue;
				red += abgr & 0xFF;
				green += (abgr >> 8) & 0xFF;
				blue += (abgr >> 16) & 0xFF;
				count++;
			}
		}
		if (count == 0) return 0;
		// Keep a faint value so pure black still reads as a colour, not as "unknown".
		return Math.max(1, (int) (red / count) << 16 | (int) (green / count) << 8 | (int) (blue / count));
	}

	/**
	 * A cheap fingerprint of every block the mesh reads: the floor level and the level above it out past the trench,
	 * and each socket with the ground under it. Any change rebuilds the mesh.
	 */
	private static long footprint(Level level, BlockPos origin, int half, List<BlockPos> sockets) {
		int reach = half + FOOTPRINT_MARGIN;
		long hash = 1125899906842597L;
		BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
		for (int dy = 0; dy <= 1; dy++) {
			for (int x = -reach; x <= reach; x++) {
				for (int z = -reach; z <= reach; z++) {
					at.set(origin.getX() + x, origin.getY() + dy, origin.getZ() + z);
					hash = hash * 31L + System.identityHashCode(level.getBlockState(at));
				}
			}
		}
		for (BlockPos socket : sockets) {
			hash = hash * 31L + System.identityHashCode(level.getBlockState(socket));
			hash = hash * 31L + System.identityHashCode(level.getBlockState(socket.below()));
		}
		return hash;
	}

	/** Forgets every cached mesh and ground colour; textures may have changed. */
	public static void clearCaches() {
		CACHE.clear();
		GROUND_COLOURS.clear();
		generation++;
	}

	@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
	public static final class ReloadHook {
		private ReloadHook() {
		}

		@SubscribeEvent
		public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
			event.registerReloadListener((ResourceManagerReloadListener) resources -> {
				clearCaches();
				com.vincenthuto.hemomancy.client.render.world.rite.CardinalRiteOfferingMemory.clear();
			});
		}
	}

	private static final class Cached {
		/** The level the mesh was built in, by identity, so another dimension's Focus at the same spot never shares it. */
		private final int level;
		private final ResourceLocation floor;
		private final Direction forwards;
		private final int generation;
		private long footprint;
		private FloorMesh mesh;
		private long checkedAt;
		/** When the mesh was last built, so easing mounds and moats rebuild it every few ticks. */
		private long builtAt;
		private final SeatTransitions transitions;

		private Cached(int level, ResourceLocation floor, Direction forwards, int generation, long footprint,
				long checkedAt, FloorMesh mesh, SeatTransitions transitions) {
			this.builtAt = checkedAt;
			this.transitions = transitions;
			this.level = level;
			this.floor = floor;
			this.forwards = forwards;
			this.generation = generation;
			this.footprint = footprint;
			this.checkedAt = checkedAt;
			this.mesh = mesh;
		}
	}

	private static final class Gash {
		private final int samples;
		private final float y;
		private final boolean[] grounded;
		private final float[] taper;
		private final float[] fromStart;
		private final float[][] normals;
		private final float[][] reaches;
		private final int[][] groundColours;
		private final int[] swell;
		private final float[] radius;

		private Gash(int samples, float y) {
			this.samples = samples;
			this.y = y;
			grounded = new boolean[samples + 1];
			taper = new float[samples + 1];
			fromStart = new float[samples + 1];
			normals = new float[samples][];
			reaches = new float[samples][];
			groundColours = new int[samples][];
			swell = new int[samples + 1];
			radius = new float[samples + 1];
		}
	}

	/**
	 * A seated brazier's mound, sampled on its polar grid. Its height is {@code h0 * (1 + 0.05 * beat) + wrinkle}
	 * scaled by how risen it is, so the profile and its finite-difference slopes are kept split that way and a
	 * frame only combines them.
	 */
	/**
	 * Flesh wiring a seated brazier into the floor: a main root and two thinner rootlets twisting about it, leaving the
	 * membrane at the rim facing the brazier, arching across the gap and diving into its mound. Shaded like the seat
	 * mounds, so it reads as the same flesh as the floor.
	 */
	private static final class Root {
		private static final int SAMPLES = 20;
		private static final int SECTORS = 10;
		private static final float MAIN_RADIUS = 0.12F;
		private static final float RIDGE_RADIUS = 0.045F;
		/** How far the rootlets swing out from the main root at its middle. */
		private static final float RIDGE_SPREAD = 0.15F;
		/** The root fades in from the membrane over its first part, so there is no seam where it leaves the floor. */
		private static final float EMERGE = 0.15F;
		private final float distance;
		private final float[][] path;
		private final float[][] side;
		private final float[][] up;
		private final float phase;

		private Root(float distance, float[][] path, float phase) {
			this.distance = distance;
			this.path = path;
			this.phase = phase;
			side = new float[path.length][];
			up = new float[path.length][];
			for (int i = 0; i < path.length; i++) {
				float[] a = path[Math.max(0, i - 1)];
				float[] b = path[Math.min(path.length - 1, i + 1)];
				float tx = b[0] - a[0];
				float ty = b[1] - a[1];
				float tz = b[2] - a[2];
				float tl = (float) Math.sqrt(tx * tx + ty * ty + tz * tz);
				tx /= tl;
				ty /= tl;
				tz /= tl;
				// Side lies level across the root; up completes the frame round it.
				float sx = -tz;
				float sz = tx;
				float sl = (float) Math.sqrt(sx * sx + sz * sz);
				if (sl < 1.0e-5F) {
					sx = 1.0F;
					sz = 0.0F;
					sl = 1.0F;
				}
				sx /= sl;
				sz /= sl;
				side[i] = new float[] {sx, 0.0F, sz};
				up[i] = new float[] {ty * sz, tz * sx - tx * sz, -ty * sx};
			}
		}

		/** The root to a brazier centred at {@code socket} (x, z) whose mound stands on {@code ground}. */
		private static Root toward(int half, float[] socket, float ground) {
			float[] rim = FloorRimRules.rimPoint(half, socket[0], socket[1]);
			float dx = socket[0] - rim[0];
			float dz = socket[1] - rim[1];
			float length = Math.max(1.0e-4F, (float) Math.sqrt(dx * dx + dz * dz));
			// It dives into the mound's flank, short of the brazier's stem.
			float into = Math.min(0.32F, length * 0.4F);
			float[] start = {rim[0], SURFACE_Y - 0.02F, rim[1]};
			float[] end = {socket[0] - dx / length * into, ground + 0.1F, socket[1] - dz / length * into};
			float phase = (float) (Math.floorMod((long) Math.floor(socket[0] * 31.0F + socket[1] * 17.0F), 628L) / 100.0D);
			return new Root((float) Math.hypot(socket[0] - 0.5F, socket[1] - 0.5F),
					FloorRimRules.rootPath(start, end, SAMPLES), phase);
		}

		private void emit(VertexConsumer consumer, Matrix4f matrix, float rise, float swelling, int alpha) {
			// The root grows out from the rim to the brazier as the floor forms.
			int reach = Math.max(1, Math.round(SAMPLES * rise));
			tube(consumer, matrix, reach, 0.0F, MAIN_RADIUS, swelling, alpha);
			tube(consumer, matrix, reach, RIDGE_SPREAD, RIDGE_RADIUS, swelling, alpha);
			tube(consumer, matrix, reach, -RIDGE_SPREAD, RIDGE_RADIUS, swelling, alpha);
		}

		/** One strand: {@code spread} swings it out from the spine at the middle, twisting as it goes. */
		private void tube(VertexConsumer consumer, Matrix4f matrix, int reach, float spread, float radius,
				float swelling, int alpha) {
			float[][] ring = new float[SECTORS][];
			float[][] previous = null;
			int[] previousShade = new int[SECTORS];
			int[] shade = new int[SECTORS];
			float previousAlpha = 0.0F;
			for (int i = 0; i <= reach; i++) {
				float t = i / (float) SAMPLES;
				float swing = spread * (float) Math.sin(Math.PI * t);
				float twist = t * 5.0F + phase;
				float ox = (float) (side[i][0] * Math.cos(twist) + up[i][0] * Math.sin(twist)) * swing;
				float oy = (float) (up[i][1] * Math.sin(twist)) * swing;
				float oz = (float) (side[i][2] * Math.cos(twist) + up[i][2] * Math.sin(twist)) * swing;
				float r = radius * (1.0F - 0.35F * t) * swelling * (1.0F + 0.15F * (float) Math.sin(t * 9.0F + phase));
				for (int k = 0; k < SECTORS; k++) {
					double theta = Math.PI * 2.0D * k / SECTORS;
					float nx = (float) (up[i][0] * Math.cos(theta) + side[i][0] * Math.sin(theta));
					float ny = (float) (up[i][1] * Math.cos(theta));
					float nz = (float) (up[i][2] * Math.cos(theta) + side[i][2] * Math.sin(theta));
					ring[k] = new float[] {path[i][0] + ox + nx * r, path[i][1] + oy + ny * r, path[i][2] + oz + nz * r};
					float lit = nx * SLOPE_LIGHT.x() + ny * SLOPE_LIGHT.y() + nz * SLOPE_LIGHT.z();
					shade[k] = Math.round(255 * Math.min(1.0F, (0.55F + 0.45F * Math.max(0.0F, lit)) / FLAT_SHADE));
				}
				float fade = smooth(Math.min(1.0F, t / EMERGE));
				if (previous != null) {
					int a0 = Math.round(alpha * previousAlpha);
					int a1 = Math.round(alpha * fade);
					for (int k = 0; k < SECTORS; k++) {
						int after = (k + 1) % SECTORS;
						rootVertex(consumer, matrix, previous[k], previousShade[k], a0);
						rootVertex(consumer, matrix, ring[k], shade[k], a1);
						rootVertex(consumer, matrix, ring[after], shade[after], a1);
						rootVertex(consumer, matrix, previous[after], previousShade[after], a0);
					}
				}
				previous = ring.clone();
				ring = new float[SECTORS][];
				System.arraycopy(shade, 0, previousShade, 0, SECTORS);
				previousAlpha = fade;
			}
		}

		private static void rootVertex(VertexConsumer consumer, Matrix4f matrix, float[] at, int shade, int alpha) {
			consumer.addVertex(matrix, at[0], at[1], at[2]).setUv(at[0] - 0.5F, at[2] - 0.5F)
					.setColor(shade, SEAT_LAYER, 255, alpha);
		}
	}

	/**
	 * How much of a seat mound may stand at this point (local to the Focus block, its centre at 0.5, 0.5): none over the
	 * Focus throat, all of it clear of it, eased between so the mound melts into the floor instead of cutting the throat.
	 */
	static float throatClearance(float x, float z) {
		float distance = (float) Math.hypot(x - 0.5F, z - 0.5F);
		float t = Math.max(0.0F, Math.min(1.0F, (distance - SEAT_THROAT_GONE) / (SEAT_THROAT_WHOLE - SEAT_THROAT_GONE)));
		return t * t * (3.0F - 2.0F * t);
	}

	private static final class Seat {
		private final float distance;
		private final float[] x;
		private final float[] z;
		private final float[] radius;
		private final float[] cos;
		private final float[] sin;
		private final float[] rim;
		/** How much of the mound stands here, clear of the Focus throat (see {@link #throatClearance}). */
		private final float[] clear;
		private final float[] h0;
		private final float[] wrinkle;
		private final float[] h0Radial;
		private final float[] wrinkleRadial;
		private final float[] h0Around;
		private final float[] wrinkleAround;

		/** How far the mound had grown in (or sunk away) one build before, and at this build, 0 to 1. */
		private final float previousWeight;
		private final float weight;

		private Seat(float[] centre, float previousWeight, float weight) {
			this.previousWeight = previousWeight;
			this.weight = weight;
			distance = (float) Math.hypot(centre[0] - 0.5F, centre[1] - 0.5F);
			float phase = (float) (Math.floorMod((long) Math.floor(centre[0] * 31.0F + centre[1] * 17.0F), 628L)
					/ 100.0D);
			int points = (SEAT_RINGS + 1) * SEAT_SECTORS;
			x = new float[points];
			z = new float[points];
			radius = new float[points];
			cos = new float[points];
			sin = new float[points];
			rim = new float[points];
			clear = new float[points];
			h0 = new float[points];
			wrinkle = new float[points];
			h0Radial = new float[points];
			wrinkleRadial = new float[points];
			h0Around = new float[points];
			wrinkleAround = new float[points];
			for (int ring = 0; ring <= SEAT_RINGS; ring++) {
				float r = SEAT_INNER + (SEAT_OUTER - SEAT_INNER) * ring / SEAT_RINGS;
				float edge = Math.min(1.0F, Math.max(0.0F, (SEAT_OUTER - r) / (SEAT_OUTER * SEAT_FADE)));
				for (int sector = 0; sector < SEAT_SECTORS; sector++) {
					int k = ring * SEAT_SECTORS + sector;
					float theta = (float) (Math.PI * 2.0D * sector / SEAT_SECTORS);
					radius[k] = r;
					cos[k] = (float) Math.cos(theta);
					sin[k] = (float) Math.sin(theta);
					x[k] = centre[0] + cos[k] * r;
					z[k] = centre[1] + sin[k] * r;
					clear[k] = throatClearance(x[k], z[k]);
					rim[k] = edge * edge * (3.0F - 2.0F * edge) * clear[k];
					h0[k] = seatProfile(r, theta, phase);
					wrinkle[k] = seatWrinkle(r, theta, phase);
					h0Radial[k] = seatProfile(r + SEAT_STEP, theta, phase) - seatProfile(r - SEAT_STEP, theta, phase);
					wrinkleRadial[k] = seatWrinkle(r + SEAT_STEP, theta, phase)
							- seatWrinkle(r - SEAT_STEP, theta, phase);
					h0Around[k] = seatProfile(r, theta + SEAT_STEP, phase) - seatProfile(r, theta - SEAT_STEP, phase);
					wrinkleAround[k] = seatWrinkle(r, theta + SEAT_STEP, phase)
							- seatWrinkle(r, theta - SEAT_STEP, phase);
				}
			}
		}

		/**
		 * Height of a seat mound above the ground at rest: hugging the stem, a shoulder over the dais, then easing down
		 * to the plug, lumpy around its circumference. The heartbeat swells it slightly.
		 */
		private static float seatProfile(float radius, float theta, float phase) {
			float profile;
			if (radius <= SEAT_STEM) {
				profile = SEAT_PEAK;
			} else if (radius <= 0.38F) {
				float t = (radius - SEAT_STEM) / (0.38F - SEAT_STEM);
				profile = SEAT_PEAK + (SEAT_SHOULDER - SEAT_PEAK) * t * t;
			} else {
				float t = Math.min(1.0F, (radius - 0.38F) / (SEAT_OUTER - 0.38F));
				profile = SEAT_SHOULDER * 0.5F * (1.0F + (float) Math.cos(Math.PI * t));
			}
			float lumps = 1.0F + 0.16F * (float) Math.sin(theta * 3.0F + phase)
					+ 0.1F * (float) Math.sin(theta * 7.0F + phase * 2.3F)
					+ 0.06F * (float) Math.sin(theta * 13.0F + phase * 3.1F);
			return profile * lumps;
		}

		/** Wrinkles where the flesh has gathered up around the foot. */
		private static float seatWrinkle(float radius, float theta, float phase) {
			return 0.01F * (float) Math.sin(theta * 17.0F + radius * 22.0F + phase)
					* (float) Math.sin(Math.PI * Math.min(1.0F, radius / SEAT_OUTER));
		}
	}

	/** Position, texture and colour for a run of static vertices; alpha is applied as they are emitted. */
	private static final class VertexList {
		private static final int STRIDE = 8;
		private float[] data = new float[STRIDE * 256];
		private int count;

		private void add(float x, float y, float z, float u, float v, int red, int green, int blue) {
			if ((count + 1) * STRIDE > data.length) data = java.util.Arrays.copyOf(data, data.length * 2);
			int at = count * STRIDE;
			data[at] = x;
			data[at + 1] = y;
			data[at + 2] = z;
			data[at + 3] = u;
			data[at + 4] = v;
			data[at + 5] = red;
			data[at + 6] = green;
			data[at + 7] = blue;
			count++;
		}
	}
}
