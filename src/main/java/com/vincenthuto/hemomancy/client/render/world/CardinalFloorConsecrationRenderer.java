package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Draws a consecrated Cardinal Rite floor: a continuous membrane over every floor block, vessel tendrils that race
 * from the Focus to each brazier socket as the floor forms, and a glowing ring on every socket still awaiting a
 * brazier. The server only reports which floor is formed; nothing here affects rite matching.
 */
public final class CardinalFloorConsecrationRenderer {
	/** Above the floor's top face, below any slab-height block placed on it. */
	private static final float SURFACE_Y = 1.004F;
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
	/** The membrane never goes fully dark, and the braid is kept at least this bright to match it. */
	private static final int BRAID_MIN_BLOCK_LIGHT = 7;
	/** Tendrils root on a ring inside the mound around the Focus mouth instead of one crowded point. */
	private static final double TENDRIL_ROOT_RADIUS = 0.5D;
	private static final double TENDRIL_ROOT_Y = 0.99D;
	/**
	 * The writhing braid around the Focus, and the mound it sits in. Off for now: the Focus mouth wears a fleshy pad
	 * instead. Flip back on to restore both.
	 */
	private static final boolean SHOW_FOCUS_BRAID = false;
	/**
	 * Experimental: the membrane covers all grounded ground inside the brazier trench, out to its centre line, instead
	 * of stopping near the laid floor's edge.
	 */
	private static final boolean EXTEND_TO_GASH = true;
	/**
	 * Under {@link #EXTEND_TO_GASH}, blue between these marks a flat membrane vertex and carries its signed distance
	 * inside the trench's centre line, so the shader cuts the membrane exactly there.
	 */
	private static final float LOOP_BLUE_CENTRE = 0.5F;
	private static final float LOOP_BLUE_SPAN = 0.1F;
	/** Half the width of the Focus pad's quad; the shader grows the ragged pad and its rootlets inside it. */
	private static final float PAD_HALF = 1.45F;
	private static final float PAD_Y = 1.008F;
	/** Green 64 marks the Focus pad for the shader; red and blue carry the position inside the pad. */
	private static final int PAD_LAYER = 64;
	/** Blue on the flat membrane tells the shader whether the mound is drawn, so it cuts the floor away under it. */
	private static final int FLAT_WITH_MOUND = 255;
	private static final int FLAT_WITHOUT_MOUND = 191;
	private static final int MOUND_RINGS = 18;
	private static final int MOUND_SECTORS = 72;
	private static final float MOUND_SKIRT = 0.04F;
	/** Light for the mound's slope shading, from high above; a flat patch comes out at full brightness. */
	private static final Vector3f MOUND_LIGHT = new Vector3f(0.3F, 1.0F, 0.5F).normalize();
	private static final float FLAT_SHADE = 0.55F + 0.45F * MOUND_LIGHT.y();
	/** The brazier gash lies just above the ground the braziers stand on. */
	private static final float GASH_Y = 0.012F;
	/** Half the width of the gash's strip; the shader tears the ragged gash and its capillaries inside it. */
	private static final float GASH_HALF = 0.8F;
	private static final int DIG_ALPHA_SPAN = 190;
	/** Experimental: the ground's own break particles kicked up where the dig front is carving. */
	private static final boolean DIG_BLOCK_DUST = true;
	private static final int DIG_DUST_PER_TICK = 3;
	private static final Map<Long, Long> DIG_DUST_TICK = new HashMap<>();
	/** Each block's average texture colour, for the trench walls cut through it. */
	private static final Map<BlockState, Integer> GROUND_COLOURS = new HashMap<>();
	private static final int GROUND_COLOUR_LIMIT = 512;
	/** Earth-brown, for a trench wall whose ground colour cannot be read. */
	private static final int DEFAULT_GROUND = 0x7F5B3E;
	/** How far into each wall the ground it cuts through is sampled. */
	private static final float WALL_SAMPLE = 0.3F;
	/** Strip reaches tried at each side of the trench, widest first, until one lies wholly over ground. */
	private static final float[] GASH_REACHES = {GASH_HALF, 0.62F, 0.48F, 0.36F};
	/**
	 * Curve samples between neighbouring sockets along the gash. Its direction is blended linearly across each section,
	 * so the sections stay short enough that the trench walls do not crease at tight bends.
	 */
	private static final int GASH_STEPS = 28;
	/** Where the gash is broken by a gap in the ground, it pinches closed over this length approaching the break. */
	private static final float GASH_TAPER = 0.8F;
	/** The shortest sideways vector a gash vertex carries; its length beyond this encodes the taper. */
	private static final float GASH_MIN_LEAN = 0.05F;
	/**
	 * Around a seated brazier the trench swings outward like a moat, so it runs on around the outside of the brazier's
	 * mound: by this much at the socket, falling away over the spread along the loop. Together they keep the trench's
	 * inner lip (about 0.37 inside its line) clear of the mound's faded rim at {@code SEAT_OUTER} all the way round.
	 */
	private static final float SEAT_DETOUR = 1.1F;
	private static final float SEAT_DETOUR_SPREAD = 1.3F;
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
	/**
	 * Green 166 marks a brazier's flesh mound for the shader, which shades it as the floor membrane itself; red carries
	 * its slope shading.
	 */
	private static final int SEAT_LAYER = 166;
	/** How far from a socket the gash still widens toward the clot pool a brazier stands in. */
	private static final float GASH_SWELL_RADIUS = 0.65F;
	/**
	 * Green 8 marks the gash for the shader; red runs across it, blue swells toward each socket, and the texture
	 * coordinates carry its sideways direction, shared at each join so the trench has no seams between sections.
	 * Their length carries the taper toward a break.
	 */
	private static final int GASH_LAYER = 8;
	private static final int TENDRIL_CACHE_LIMIT = 256;
	private static final Map<Long, Long> TENDRILS_SPAWNED = new HashMap<>();
	private static final int LOOP_CACHE_LIMIT = 128;
	/** Each Focus's trench loop, kept until its floor or orientation changes. */
	private static final Map<Long, CachedLoop> LOOP_CACHE = new HashMap<>();

	private CardinalFloorConsecrationRenderer() {
	}

	public static boolean hasFloor(CardinalFocusBlockEntity focus) {
		return focus.getFormedFloor() != null || focus.getFadingFloor() != null;
	}

	/** How far from the Focus the membrane and sockets can reach, for render bounds. */
	public static int renderRadius(CardinalFocusBlockEntity focus) {
		var id = focus.getFormedFloor() != null ? focus.getFormedFloor() : focus.getFadingFloor();
		return id == null ? 1 : CardinalRiteFloorRegistry.get(id)
				.map(floor -> CardinalRiteFloorGeometry.halfWidth(floor) + 5).orElse(1);
	}

	public static void render(CardinalFocusBlockEntity focus, float partialTick, PoseStack pose,
			MultiBufferSource buffers) {
		Level level = focus.getLevel();
		if (level == null) return;
		float now = level.getGameTime() + partialTick;
		float fade = 1.0F;
		var id = focus.getFormedFloor();
		Direction forwards = focus.getFormedForwards();
		float age = now - focus.getFormationStart();
		boolean collapsing = focus.isCollapsing(now);
		if (collapsing) {
			// A broken or replaced floor plays its formation backwards; any replacement waits until it has finished.
			id = focus.getFadingFloor();
			forwards = focus.getFadingForwards();
			age = CardinalRiteFloorVisualRules.collapseAge(now - focus.getFadingSince());
		}
		if (id == null || age <= 0.0F) return;
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(id).orElse(null);
		if (floor == null) return;

		BlockPos origin = focus.getBlockPos();
		int half = CardinalRiteFloorGeometry.halfWidth(floor);
		int style = CardinalRiteFloorVisualRules.styleIndex(floor.style());
		var palette = CardinalRiteFloorVisualRules.palette(style);
		float spread = CardinalRiteFloorVisualRules.spreadRadius(Math.min(age, 1.0e6F), half);
		float front = age >= CardinalRiteFloorVisualRules.FORM_TICKS ? 0.0F
				: CardinalRiteFloorVisualRules.frontIntensity(age);
		float beat = CardinalRiteFloorVisualRules.heartbeat(now);
		float seed = (Math.floorMod(origin.asLong() * 0x9E3779B97F4A7C15L, 1000L)) * 0.1F;

		// One light sample at the Focus drives both the membrane and the braid, so they darken together.
		int packedLight = LevelRenderer.getLightColor(level, origin.above());
		float floorLight = worldLight(level, packedLight, partialTick);
		// The Focus centre relative to the camera, so the shader can place the trench from camera-relative positions.
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		float focusX = (float) (origin.getX() + 0.5D - camera.x);
		float focusZ = (float) (origin.getZ() + 0.5D - camera.z);
		List<BlockPos> sockets = CardinalRiteFloorGeometry.sockets(floor, origin, forwards);
		GashLoop loop = loopFor(origin, floor.id(), forwards, sockets, seatedSockets(level, sockets));
		drawMembrane(pose, buffers, level, floor, origin, forwards, now, seed, style, half, spread, front, beat,
				floorLight, palette, fade, focusX, focusZ, loop);
		if (loop != null) {
			List<float[]> seated = drawGash(pose, buffers.getBuffer(HemoRenderTypes.cardinalFloorMembrane(now, seed,
					style, half, spread, front, beat, floorLight, palette.base(), palette.vein(), palette.glow(), focusX,
					focusZ)), level, origin, loop, age, fade);
			if (!seated.isEmpty()) {
				// Depth-writing, so the brazier's foot is hidden inside the flesh.
				drawSeats(pose, buffers.getBuffer(HemoRenderTypes.cardinalFloorMound(now, seed, style, half, spread,
						front, beat, floorLight, palette.base(), palette.vein(), palette.glow(), focusX, focusZ)),
						loop.sockets().getFirst().getY() - origin.getY(), seated, beat, spread, Math.round(255 * fade));
			}
		}
		float grow = CardinalRiteFloorVisualRules.braidGrowth(age);
		if (SHOW_FOCUS_BRAID) {
			int braidLight = LightTexture.pack(Math.max(LightTexture.block(packedLight), BRAID_MIN_BLOCK_LIGHT),
					LightTexture.sky(packedLight));
			CardinalFocusBraidRenderer.draw(pose, buffers, now, grow, beat, braidLight);
		} else {
			drawFocusPad(pose, buffers.getBuffer(HemoRenderTypes.cardinalFloorMembrane(now, seed, style, half, spread,
					front, beat, floorLight, palette.base(), palette.vein(), palette.glow(), focusX, focusZ)), Math.round(255 * grow));
		}
		if (!collapsing && age < CardinalRiteFloorVisualRules.FORM_TICKS) {
			spawnFormationTendrils(origin, focus.getFormedSince(), sockets, palette, partialTick);
		}
	}

	private static void drawMembrane(PoseStack pose, MultiBufferSource buffers, Level level,
			CardinalRiteFloorDefinition floor,
			BlockPos origin, Direction forwards, float now, float seed, int style, int half, float spread,
			float front, float beat, float floorLight, CardinalRiteFloorVisualRules.Palette palette, float fade,
			float focusX, float focusZ, GashLoop loop) {
		VertexConsumer consumer = buffers.getBuffer(HemoRenderTypes.cardinalFloorMembrane(now, seed, style, half,
				spread, front, beat, floorLight, palette.base(), palette.vein(), palette.glow(), focusX, focusZ));
		Matrix4f matrix = pose.last().pose();
		int alpha = Math.round(255 * fade);
		Set<BlockPos> covered = new HashSet<>(CardinalRiteFloorGeometry.cells(floor, origin, forwards));
		boolean extended = EXTEND_TO_GASH && loop != null;
		if (extended) {
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
			// The membrane may spill one block past the laid floor, but only onto open ground level with it.
			for (BlockPos cell : CardinalRiteFloorGeometry.margin(floor, origin)) {
				if (spillable(level, cell)) covered.add(cell);
			}
		}
		Set<BlockPos> solid = new HashSet<>(covered);
		solid.add(origin);
		// Over a drop, the membrane droops down the exposed side instead of stopping at the edge. Under the trench
		// extension a drape hangs along whatever part of its top edge lies inside the trench line.
		Map<BlockPos, Set<Direction>> drapes = new HashMap<>();
		for (BlockPos cell : covered) {
			Set<Direction> draped = EnumSet.noneOf(Direction.class);
			for (Direction side : Direction.Plane.HORIZONTAL) {
				BlockPos beside = cell.relative(side);
				if (solid.contains(beside) || level.getBlockState(beside).isSolidRender(level, beside)) continue;
				if (extended && insideSpan(loop, cell, origin, side) == null) continue;
				draped.add(side);
			}
			drapes.put(cell, draped);
		}
		for (BlockPos cell : covered) {
			Set<Direction> draped = drapes.get(cell);
			for (Direction side : draped) {
				Direction towardA = Direction.fromDelta(side.getStepZ(), 0, -side.getStepX());
				drape(consumer, matrix, cell, origin, side, alpha, extended ? loop : null,
						drapeCarriesOn(drapes, cell, side, towardA),
						drapeCarriesOn(drapes, cell, side, towardA.getOpposite()));
			}
			// UVs are block offsets from the Focus centre, so the shader sees one unbroken surface. Over a drape
			// the surface reaches out to meet the drape's top edge.
			float x0 = cell.getX() - origin.getX() - (draped.contains(Direction.WEST) ? DRAPE_OFFSET : 0.0F);
			float x1 = cell.getX() - origin.getX() + 1.0F + (draped.contains(Direction.EAST) ? DRAPE_OFFSET : 0.0F);
			float z0 = cell.getZ() - origin.getZ() - (draped.contains(Direction.NORTH) ? DRAPE_OFFSET : 0.0F);
			float z1 = cell.getZ() - origin.getZ() + 1.0F + (draped.contains(Direction.SOUTH) ? DRAPE_OFFSET : 0.0F);
			if (extended) {
				loopQuad(consumer, matrix, loop, x0, z0, x1, z1, alpha);
			} else {
				quad(consumer, matrix, x0, z0, x1, z1, SURFACE_Y, alpha);
			}
		}
		quad(consumer, matrix, BASIN_INSET, BASIN_INSET, 1.0F - BASIN_INSET, 1.0F - BASIN_INSET, BASIN_Y, alpha);
		// The mound writes depth, so the braid and tendril roots sink into it rather than drawing over it.
		if (SHOW_FOCUS_BRAID) {
			drawMound(buffers.getBuffer(HemoRenderTypes.cardinalFloorMound(now, seed, style, half, spread, front,
					beat, floorLight, palette.base(), palette.vein(), palette.glow(), focusX, focusZ)), matrix, alpha);
		}
	}

	/**
	 * The Focus mouth as a fleshy pad: a ragged rim with rootlets creeping out over the floor. Its open centre shows
	 * the basin beneath, where the shader darkens the pool into a clot.
	 */
	private static void drawFocusPad(PoseStack pose, VertexConsumer consumer, int alpha) {
		if (alpha <= 2) return;
		Matrix4f matrix = pose.last().pose();
		padVertex(consumer, matrix, 0.5F - PAD_HALF, 0.5F - PAD_HALF, 0, 0, alpha);
		padVertex(consumer, matrix, 0.5F - PAD_HALF, 0.5F + PAD_HALF, 0, 255, alpha);
		padVertex(consumer, matrix, 0.5F + PAD_HALF, 0.5F + PAD_HALF, 255, 255, alpha);
		padVertex(consumer, matrix, 0.5F + PAD_HALF, 0.5F - PAD_HALF, 255, 0, alpha);
	}

	private static void padVertex(VertexConsumer consumer, Matrix4f matrix, float x, float z, int padX, int padZ,
			int alpha) {
		consumer.addVertex(matrix, x, PAD_Y, z).setUv(x - 0.5F, z - 0.5F).setColor(padX, PAD_LAYER, padZ, alpha);
	}

	/**
	 * The swelling the Focus braid sits in: a polar mesh from the iron lip out to where it settles into the floor.
	 * Green 128 marks it for the shader, and the red channel carries its slope shading.
	 */
	private static void drawMound(VertexConsumer consumer, Matrix4f matrix, int alpha) {
		for (int ring = 0; ring < MOUND_RINGS; ring++) {
			float r0 = moundRadius(ring);
			float r1 = moundRadius(ring + 1);
			for (int sector = 0; sector < MOUND_SECTORS; sector++) {
				float t0 = (float) (Math.PI * 2.0D * sector / MOUND_SECTORS);
				float t1 = (float) (Math.PI * 2.0D * (sector + 1) / MOUND_SECTORS);
				moundVertex(consumer, matrix, r0, t0, alpha);
				moundVertex(consumer, matrix, r1, t0, alpha);
				moundVertex(consumer, matrix, r1, t1, alpha);
				moundVertex(consumer, matrix, r0, t1, alpha);
			}
		}
	}

	private static float moundRadius(int ring) {
		float t = ring / (float) MOUND_RINGS;
		// Rings bunch toward the lip and crest, where the profile bends fastest. The last ring runs a little past
		// the mound's edge, flat, so the shader alone decides where mound gives way to floor: a polygon edge would
		// leave hairline slivers against the floor's circular cut-out.
		float outer = CardinalRiteFloorVisualRules.MOUND_OUTER + MOUND_SKIRT;
		return CardinalRiteFloorVisualRules.MOUND_INNER + (outer - CardinalRiteFloorVisualRules.MOUND_INNER) * t * t;
	}

	private static void moundVertex(VertexConsumer consumer, Matrix4f matrix, float radius, float theta, int alpha) {
		float x = (float) Math.cos(theta) * radius;
		float z = (float) Math.sin(theta) * radius;
		float step = 0.02F;
		float slopeX = (moundAt(x + step, z) - moundAt(x - step, z)) / (2.0F * step);
		float slopeZ = (moundAt(x, z + step) - moundAt(x, z - step)) / (2.0F * step);
		Vector3f normal = new Vector3f(-slopeX, 1.0F, -slopeZ).normalize();
		float shade = Math.min(1.0F, (0.55F + 0.45F * Math.max(0.0F, normal.dot(MOUND_LIGHT))) / FLAT_SHADE);
		consumer.addVertex(matrix, 0.5F + x, SURFACE_Y + moundAt(x, z), 0.5F + z).setUv(x, z)
				.setColor(Math.round(255 * shade), 128, 255, alpha);
	}

	private static float moundAt(float x, float z) {
		return CardinalRiteFloorVisualRules.moundHeight((float) Math.sqrt(x * x + z * z), (float) Math.atan2(z, x));
	}

	/**
	 * A sheet hanging from the top edge of an exposed side face. Its UVs keep running outward as it hangs, as if the
	 * membrane rolled over the lip, so the pattern and the wandering edge carry on unbroken. The red channel carries
	 * how far down the sheet a vertex hangs, for the shader's ragged hem.
	 */
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
	private static void drape(VertexConsumer consumer, Matrix4f matrix, BlockPos cell, BlockPos origin,
			Direction side, int alpha, GashLoop loop, boolean carriesOnA, boolean carriesOnB) {
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
			drapeStrip(consumer, matrix, cell, origin, side, alpha, loop, stops[strip], stops[strip + 1],
					hem[strip], hem[strip + 1]);
		}
	}

	/**
	 * One strip of a drape between fractions {@code u0} and {@code u1} along its top edge. Its UVs keep running outward
	 * as it hangs, as if the membrane rolled over the lip. Red carries how far down a vertex hangs and green how freely
	 * its hem may hang, for the shader's ragged hem; under the trench extension blue carries the trench distance.
	 */
	private static void drapeStrip(VertexConsumer consumer, Matrix4f matrix, BlockPos cell, BlockPos origin,
			Direction side, int alpha, GashLoop loop, float u0, float u1, float hem0, float hem1) {
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
		int blue = loop == null ? 255 : Math.round(255 * (LOOP_BLUE_CENTRE + LOOP_BLUE_SPAN));
		int blueA = blue;
		int blueB = blue;
		int greenA = Math.round(255 * (DRAPE_END_GREEN + (1.0F - DRAPE_END_GREEN) * hem0));
		int greenB = Math.round(255 * (DRAPE_END_GREEN + (1.0F - DRAPE_END_GREEN) * hem1));
		hangingVertex(consumer, matrix, ax + px, SURFACE_Y, az + pz, ax, az, alpha, 0.0F, greenA, blueA);
		hangingVertex(consumer, matrix, ax + px, bottom, az + pz, ax + nx * DROOP, az + nz * DROOP, alpha, 1.0F,
				greenA, blueA);
		hangingVertex(consumer, matrix, bx + px, bottom, bz + pz, bx + nx * DROOP, bz + nz * DROOP, alpha, 1.0F,
				greenB, blueB);
		hangingVertex(consumer, matrix, bx + px, SURFACE_Y, bz + pz, bx, bz, alpha, 0.0F, greenB, blueB);
	}

	private static void hangingVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
			float surfaceX, float surfaceZ, int alpha, float hang, int green, int blue) {
		consumer.addVertex(matrix, x, y, z).setUv(surfaceX - 0.5F, surfaceZ - 0.5F)
				.setColor(Math.round(255 * (1.0F - hang)), green, blue, alpha);
	}

	/** 0 in darkness to 1 in full light: the brighter of block light and sky light dimmed by the time of day. */
	private static float worldLight(Level level, int packedLight, float partialTick) {
		float sky = LightTexture.sky(packedLight) / 15.0F;
		if (level instanceof ClientLevel client) sky *= client.getSkyDarken(partialTick);
		return Math.max(LightTexture.block(packedLight) / 15.0F, sky);
	}

	private static boolean spillable(Level level, BlockPos cell) {
		BlockPos above = cell.above();
		return level.getBlockState(cell).isFaceSturdy(level, cell, Direction.UP)
				&& !level.getBlockState(above).isSolidRender(level, above);
	}

	private static void quad(VertexConsumer consumer, Matrix4f matrix, float x0, float z0, float x1, float z1,
			float y, int alpha) {
		vertex(consumer, matrix, x0, y, z0, alpha);
		vertex(consumer, matrix, x0, y, z1, alpha);
		vertex(consumer, matrix, x1, y, z1, alpha);
		vertex(consumer, matrix, x1, y, z0, alpha);
	}

	/** A flat membrane quad whose corners carry their signed distance inside the trench's centre line. */
	private static void loopQuad(VertexConsumer consumer, Matrix4f matrix, GashLoop loop, float x0, float z0,
			float x1, float z1, int alpha) {
		loopVertex(consumer, matrix, loop, x0, z0, alpha);
		loopVertex(consumer, matrix, loop, x0, z1, alpha);
		loopVertex(consumer, matrix, loop, x1, z1, alpha);
		loopVertex(consumer, matrix, loop, x1, z0, alpha);
	}

	private static void loopVertex(VertexConsumer consumer, Matrix4f matrix, GashLoop loop, float x, float z,
			int alpha) {
		consumer.addVertex(matrix, x, SURFACE_Y, z).setUv(x - 0.5F, z - 0.5F)
				.setColor(255, 255, loopBlue(loop, x, z), alpha);
	}

	/** Blue encoding of how far inside the trench's centre line a surface point lies. */
	private static int loopBlue(GashLoop loop, float x, float z) {
		float inside = Math.max(-1.0F, Math.min(1.0F, loop.distance(x, z)));
		return Math.round(255 * (LOOP_BLUE_CENTRE + LOOP_BLUE_SPAN * inside));
	}

	/** Sockets holding an Iron Brazier on sturdy ground: the braziers seated on the ring. */
	private static Set<BlockPos> seatedSockets(Level level, List<BlockPos> sockets) {
		Set<BlockPos> seated = new HashSet<>();
		for (BlockPos socket : sockets) {
			BlockPos ground = socket.below();
			if (level.getBlockState(socket).is(BlockInit.iron_brazier.get())
					&& level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
				seated.add(socket);
			}
		}
		return seated;
	}

	private static GashLoop loopFor(BlockPos origin, ResourceLocation floor, Direction forwards,
			List<BlockPos> sockets, Set<BlockPos> seated) {
		CachedLoop cached = LOOP_CACHE.get(origin.asLong());
		if (cached != null && cached.floor().equals(floor) && cached.forwards() == forwards
				&& cached.seated().equals(seated)) {
			return cached.loop();
		}
		if (LOOP_CACHE.size() > LOOP_CACHE_LIMIT) LOOP_CACHE.clear();
		GashLoop loop = GashLoop.through(origin, sockets, seated);
		LOOP_CACHE.put(origin.asLong(), new CachedLoop(floor, forwards, Set.copyOf(seated), loop));
		return loop;
	}

	private record CachedLoop(ResourceLocation floor, Direction forwards, Set<BlockPos> seated, GashLoop loop) {
	}

	private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, int alpha) {
		consumer.addVertex(matrix, x, y, z).setUv(x - 0.5F, z - 0.5F)
				.setColor(255, 255, SHOW_FOCUS_BRAID ? FLAT_WITH_MOUND : FLAT_WITHOUT_MOUND, alpha);
	}

	/**
	 * One continuous gash torn in the ground around the floor, passing through every brazier socket along a smooth
	 * closed curve, and widening into a clot pool at each socket. It tears open around the loop as the floor forms,
	 * and skips any stretch with no ground to lie on.
	 */
	/** Draws the trench, and returns the socket centres of the braziers seated on it. */
	private static List<float[]> drawGash(PoseStack pose, VertexConsumer consumer, Level level, BlockPos origin,
			GashLoop gash, float age, float fade) {
		List<BlockPos> loop = gash.sockets();
		int count = loop.size();
		float y = loop.getFirst().getY() - origin.getY() + GASH_Y;
		float[][] points = gash.points();
		float[][] centre = gash.centre();
		int samples = centre.length - 1;
		boolean[] grounded = new boolean[samples + 1];
		for (int i = 0; i < samples; i++) grounded[i] = gashGround(level, origin, centre[i]);
		grounded[samples] = grounded[0];
		float[] taper = gashTaper(centre, grounded, samples);
		// While the dig front is still going round, the trench pinches shut behind it at the point where it started,
		// instead of ending in a flat cut; once the front meets it again the loop joins.
		float joined = CardinalRiteFloorVisualRules.digProgress(age, samples - 1, samples);
		float[] fromStart = new float[samples + 1];
		for (int i = 1; i <= samples; i++) {
			float dx = centre[i][0] - centre[i - 1][0];
			float dz = centre[i][1] - centre[i - 1][1];
			fromStart[i] = fromStart[i - 1] + (float) Math.sqrt(dx * dx + dz * dz);
		}
		// Where a brazier already stands, the floor heaps up around its foot and the trench has swung out around it. Only the
		// empty sockets still open the trench wider.
		List<float[]> plugs = new ArrayList<>();
		List<float[]> open = new ArrayList<>();
		for (int i = 0; i < count; i++) (gash.seated()[i] ? plugs : open).add(points[i]);
		float[][] widening = open.toArray(float[][]::new);
		if (DIG_BLOCK_DUST) spawnDigDust(level, origin, centre, grounded, age, samples);
		// On the inside of a tight bend the strip may reach no further than the bend's radius, or it folds back
		// over itself and the trench walls draw twice.
		float[][] bendLimit = gashBendLimits(centre, samples);
		// Each sample's sideways direction and how far the strip reaches either side, shared by the sections meeting
		// there. The loop is closed, so neighbours wrap around its start and the two sides of the seam agree.
		float[][] normals = new float[samples][];
		float[][] reaches = new float[samples][];
		int[][] groundColours = new int[samples][];
		for (int k = 0; k < samples; k++) {
			if (!grounded[k]) continue;
			normals[k] = gashNormal(centre[Math.floorMod(k - 1, samples)], centre[(k + 1) % samples]);
			int plus = groundColour(level, origin, centre[k][0] + normals[k][0] * WALL_SAMPLE,
					centre[k][1] + normals[k][1] * WALL_SAMPLE);
			int minus = groundColour(level, origin, centre[k][0] - normals[k][0] * WALL_SAMPLE,
					centre[k][1] - normals[k][1] * WALL_SAMPLE);
			// Beside a ledge a wall's sample can land in open air: borrow the ground under the trench, then the other
			// wall's, so the band never blends toward black.
			int under = groundColour(level, origin, centre[k][0], centre[k][1]);
			int fallback = under != 0 ? under : plus != 0 ? plus : minus != 0 ? minus : DEFAULT_GROUND;
			groundColours[k] = new int[] {plus != 0 ? plus : fallback, minus != 0 ? minus : fallback};
			reaches[k] = new float[] {gashReach(level, origin, centre[k], normals[k], 1.0F, bendLimit[k][0]),
					gashReach(level, origin, centre[k], normals[k], -1.0F, bendLimit[k][1])};
		}
		Matrix4f matrix = pose.last().pose();
		for (int i = 0; i < samples; i++) {
			float[] a = centre[i];
			float[] b = centre[i + 1];
			if (!grounded[i] || !grounded[i + 1]) continue;
			// The dig front carves the trench open around the loop: it widens and deepens from a glowing seam.
			float digA = CardinalRiteFloorVisualRules.digProgress(age, i, samples);
			float digB = CardinalRiteFloorVisualRules.digProgress(age, i + 1, samples);
			if (digA <= 0.001F && digB <= 0.001F) continue;
			int next = (i + 1) % samples;
			float[] normalA = normals[i];
			float[] normalB = normals[next];
			int swellA = Math.round(255 * swell(a, widening));
			int swellB = Math.round(255 * swell(b, widening));
			int glowA = digAlpha(digA, fade);
			int glowB = digAlpha(digB, fade);
			float carveA = Math.min(taper[i], digA * Math.max(digTail(fromStart[i]), joined));
			float carveB = Math.min(taper[i + 1], digB * Math.max(digTail(fromStart[i + 1]), joined));
			gashVertex(consumer, matrix, a, normalA, carveA, 1.0F, reaches[i][0], y, swellA, glowA,
					groundColours[i][0]);
			gashVertex(consumer, matrix, a, normalA, carveA, -1.0F, reaches[i][1], y, swellA, glowA,
					groundColours[i][1]);
			gashVertex(consumer, matrix, b, normalB, carveB, -1.0F, reaches[next][1], y, swellB, glowB,
					groundColours[next][1]);
			gashVertex(consumer, matrix, b, normalB, carveB, 1.0F, reaches[next][0], y, swellB, glowB,
					groundColours[next][0]);
		}
		return plugs;
	}

	/** A lumpy mound of flesh around each seated brazier, swallowing its dais and the foot of its stem. */
	private static void drawSeats(PoseStack pose, VertexConsumer consumer, float ground, List<float[]> seats,
			float beat, float spread, int alpha) {
		if (alpha <= 2) return;
		Matrix4f matrix = pose.last().pose();
		for (float[] seat : seats) {
			// The mound rises as the membrane's edge reaches it, and sinks back as a collapsing floor draws away.
			float rise = CardinalRiteFloorVisualRules.seatRise(spread,
					(float) Math.hypot(seat[0] - 0.5F, seat[1] - 0.5F));
			if (rise <= 0.01F) continue;
			float phase = (float) (Math.floorMod((long) Math.floor(seat[0] * 31.0F + seat[1] * 17.0F), 628L) / 100.0D);
			for (int ring = 0; ring < SEAT_RINGS; ring++) {
				float r0 = SEAT_INNER + (SEAT_OUTER - SEAT_INNER) * ring / SEAT_RINGS;
				float r1 = SEAT_INNER + (SEAT_OUTER - SEAT_INNER) * (ring + 1) / SEAT_RINGS;
				for (int sector = 0; sector < SEAT_SECTORS; sector++) {
					float t0 = (float) (Math.PI * 2.0D * sector / SEAT_SECTORS);
					float t1 = (float) (Math.PI * 2.0D * (sector + 1) / SEAT_SECTORS);
					seatVertex(consumer, matrix, seat, ground, r0, t0, phase, beat, rise, alpha);
					seatVertex(consumer, matrix, seat, ground, r1, t0, phase, beat, rise, alpha);
					seatVertex(consumer, matrix, seat, ground, r1, t1, phase, beat, rise, alpha);
					seatVertex(consumer, matrix, seat, ground, r0, t1, phase, beat, rise, alpha);
				}
			}
		}
	}

	/**
	 * Height of a seat mound above the ground: hugging the stem, a shoulder over the dais, then easing down to the
	 * plug, lumpy around its circumference and swelling slightly with each heartbeat.
	 */
	private static float seatHeight(float radius, float theta, float phase, float beat) {
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
		// Wrinkles where the flesh has gathered up around the foot.
		float wrinkle = 0.01F * (float) Math.sin(theta * 17.0F + radius * 22.0F + phase)
				* (float) Math.sin(Math.PI * Math.min(1.0F, radius / SEAT_OUTER));
		return profile * lumps * (1.0F + 0.05F * beat) + wrinkle;
	}

	private static void seatVertex(VertexConsumer consumer, Matrix4f matrix, float[] seat, float ground,
			float radius, float theta, float phase, float beat, float rise, int alpha) {
		float x = seat[0] + (float) Math.cos(theta) * radius;
		float z = seat[1] + (float) Math.sin(theta) * radius;
		float step = 0.02F;
		float dr = (seatHeight(radius + step, theta, phase, beat) - seatHeight(radius - step, theta, phase, beat)) * rise
				/ (2.0F * step);
		float dt = (seatHeight(radius, theta + step, phase, beat) - seatHeight(radius, theta - step, phase, beat)) * rise
				/ (2.0F * step * Math.max(radius, 0.05F));
		float cos = (float) Math.cos(theta);
		float sin = (float) Math.sin(theta);
		Vector3f normal = new Vector3f(-(dr * cos - dt * sin), 1.0F, -(dr * sin + dt * cos)).normalize();
		float shade = Math.min(1.0F, (0.55F + 0.45F * Math.max(0.0F, normal.dot(MOUND_LIGHT))) / FLAT_SHADE);
		// It rises from the floor surface itself, sharing its texture coordinates, and fades in from its rim.
		float rim = Math.min(1.0F, Math.max(0.0F, (SEAT_OUTER - radius) / (SEAT_OUTER * SEAT_FADE)));
		float y = ground - 1.0F + SURFACE_Y + seatHeight(radius, theta, phase, beat) * rise;
		consumer.addVertex(matrix, x, y, z).setUv(x - 0.5F, z - 0.5F)
				.setColor(Math.round(255 * shade), SEAT_LAYER, 255, Math.round(alpha * rim * rim * (3.0F - 2.0F * rim)));
	}

	/** The brazier trench's centre line: a closed curve through the sockets, in coordinates local to the Focus block. */
	private record GashLoop(List<BlockPos> sockets, boolean[] seated, float[][] points, float[][] centre,
			Map<Long, Float> distances) {
		static GashLoop through(BlockPos origin, List<BlockPos> sockets, Set<BlockPos> seatedSockets) {
			if (sockets.size() < 3) return null;
			List<BlockPos> sorted = new ArrayList<>(sockets);
			sorted.sort(Comparator.comparingDouble(socket ->
					Math.atan2(socket.getZ() - origin.getZ(), socket.getX() - origin.getX())));
			int count = sorted.size();
			float[][] points = new float[count][];
			for (int i = 0; i < count; i++) {
				points[i] = new float[] {sorted.get(i).getX() - origin.getX() + 0.5F,
						sorted.get(i).getZ() - origin.getZ() + 0.5F};
			}
			int samples = count * GASH_STEPS;
			float[][] centre = new float[samples + 1][];
			for (int i = 0; i <= samples; i++) {
				int segment = (i / GASH_STEPS) % count;
				float t = (i % GASH_STEPS) / (float) GASH_STEPS;
				centre[i] = catmullRom(points[Math.floorMod(segment - 1, count)], points[segment],
						points[(segment + 1) % count], points[(segment + 2) % count], t);
			}
			boolean[] seated = new boolean[count];
			for (int i = 0; i < count; i++) seated[i] = seatedSockets.contains(sorted.get(i));
			// Swing the line outward around each seated brazier, so the trench becomes a moat around its mound.
			float[][] detoured = new float[samples + 1][];
			for (int i = 0; i <= samples; i++) {
				float x = centre[i][0];
				float z = centre[i][1];
				for (int seat = 0; seat < count; seat++) {
					if (!seated[seat]) continue;
					float ox = points[seat][0] - 0.5F;
					float oz = points[seat][1] - 0.5F;
					float length = (float) Math.sqrt(ox * ox + oz * oz);
					if (length < 1.0e-4F) continue;
					float dx = centre[i][0] - points[seat][0];
					float dz = centre[i][1] - points[seat][1];
					float weight = (float) Math.exp(-(dx * dx + dz * dz) / (SEAT_DETOUR_SPREAD * SEAT_DETOUR_SPREAD));
					x += ox / length * SEAT_DETOUR * weight;
					z += oz / length * SEAT_DETOUR * weight;
				}
				detoured[i] = new float[] {x, z};
			}
			detoured[samples] = detoured[0];
			return new GashLoop(List.copyOf(sorted), seated, points, detoured, new HashMap<>());
		}

		/** Positive inside the centre line, negative outside, in blocks. */
		float signedDistance(float x, float z) {
			float nearest = Float.MAX_VALUE;
			boolean inside = false;
			for (int i = 0, j = centre.length - 2; i < centre.length - 1; j = i++) {
				float[] a = centre[i];
				float[] b = centre[j];
				if ((a[1] > z) != (b[1] > z) && x < (b[0] - a[0]) * (z - a[1]) / (b[1] - a[1]) + a[0]) inside = !inside;
				float dx = b[0] - a[0];
				float dz = b[1] - a[1];
				float lengthSq = dx * dx + dz * dz;
				float t = lengthSq < 1.0e-8F ? 0.0F
						: Math.max(0.0F, Math.min(1.0F, ((x - a[0]) * dx + (z - a[1]) * dz) / lengthSq));
				float ex = a[0] + dx * t - x;
				float ez = a[1] + dz * t - z;
				nearest = Math.min(nearest, ex * ex + ez * ez);
			}
			float distance = (float) Math.sqrt(nearest);
			return inside ? distance : -distance;
		}

		/** {@link #signedDistance}, remembered per point to a 1/256 block grid: the loop never moves. */
		float distance(float x, float z) {
			long key = ((long) Math.round(x * 256.0F) << 32) ^ (Math.round(z * 256.0F) & 0xFFFFFFFFL);
			Float known = distances.get(key);
			if (known != null) return known;
			float measured = signedDistance(x, z);
			distances.put(key, measured);
			return measured;
		}

		/** Whether any corner of the block at this offset from the Focus lies inside the centre line. */
		boolean reachesInside(int x, int z) {
			return distance(x, z) > -0.05F || distance(x + 1, z) > -0.05F
					|| distance(x, z + 1) > -0.05F || distance(x + 1, z + 1) > -0.05F;
		}
	}

	/**
	 * 1 along unbroken gash, falling to 0 at the last grounded sample before each break, so the cut pinches shut
	 * there instead of ending open. Distances run along the loop in both directions.
	 */
	private static float[] gashTaper(float[][] centre, boolean[] grounded, int samples) {
		float[] taper = new float[samples + 1];
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
		return taper;
	}

	/**
	 * The trench is opaque, so its vertex alpha carries the dig front's glow instead: 255 where nothing is digging,
	 * falling to {@code 255 - DIG_ALPHA_SPAN} at the brightest point of the front.
	 */
	/** Once per game tick, the ground's break particles burst up from a few points the dig front is cutting. */
	private static void spawnDigDust(Level level, BlockPos origin, float[][] centre, boolean[] grounded, float age,
			int samples) {
		long tick = level.getGameTime();
		Long last = DIG_DUST_TICK.get(origin.asLong());
		if (last != null && last == tick) return;
		if (DIG_DUST_TICK.size() > LOOP_CACHE_LIMIT) DIG_DUST_TICK.clear();
		DIG_DUST_TICK.put(origin.asLong(), tick);
		List<Integer> cutting = new ArrayList<>();
		for (int i = 0; i < samples; i++) {
			float dig = CardinalRiteFloorVisualRules.digProgress(age, i, samples);
			if (grounded[i] && dig > 0.1F && dig < 0.9F) cutting.add(i);
		}
		if (cutting.isEmpty()) return;
		var random = level.random;
		for (int n = 0; n < DIG_DUST_PER_TICK; n++) {
			float[] at = centre[cutting.get(random.nextInt(cutting.size()))];
			BlockPos ground = origin.offset((int) Math.floor(at[0]), 0, (int) Math.floor(at[1]));
			var state = level.getBlockState(ground);
			if (state.isAir()) continue;
			double x = origin.getX() + at[0] + (random.nextDouble() - 0.5D) * 0.3D;
			double z = origin.getZ() + at[1] + (random.nextDouble() - 0.5D) * 0.3D;
			level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, origin.getY() + 1.02D, z,
					(random.nextDouble() - 0.5D) * 0.15D, 0.12D + random.nextDouble() * 0.12D,
					(random.nextDouble() - 0.5D) * 0.15D);
		}
	}

	/** 0 at the point where the dig front started, opening to 1 a short way along the trench from it. */
	private static float digTail(float distanceFromStart) {
		float t = Math.min(1.0F, distanceFromStart / GASH_TAPER);
		return t * t * (3.0F - 2.0F * t);
	}

	private static int digAlpha(float progress, float fade) {
		float glow = CardinalRiteFloorVisualRules.digGlow(progress) * fade;
		return 255 - Math.round(DIG_ALPHA_SPAN * glow);
	}

	private static float[] catmullRom(float[] p0, float[] p1, float[] p2, float[] p3, float t) {
		float t2 = t * t;
		float t3 = t2 * t;
		float[] out = new float[2];
		for (int axis = 0; axis < 2; axis++) {
			out[axis] = 0.5F * (2.0F * p1[axis] + (-p0[axis] + p2[axis]) * t
					+ (2.0F * p0[axis] - 5.0F * p1[axis] + 4.0F * p2[axis] - p3[axis]) * t2
					+ (-p0[axis] + 3.0F * p1[axis] - 3.0F * p2[axis] + p3[axis]) * t3);
		}
		return out;
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
	private static void gashVertex(VertexConsumer consumer, Matrix4f matrix, float[] centre, float[] normal,
			float taper, float side, float reach, float y, int swell, int alpha, int groundColour) {
		float x = centre[0] + normal[0] * reach * side;
		float z = centre[1] + normal[1] * reach * side;
		float lean = GASH_MIN_LEAN + (1.0F - GASH_MIN_LEAN) * taper;
		float angle = (float) Math.atan2(normal[1], normal[0]);
		if (angle < 0.0F) angle += (float) (Math.PI * 2.0D);
		float packedSide = Math.round(lean * 1000.0F) * 8.0F + Math.min(angle, 6.28F);
		// Red runs across the strip at the same scale whatever its reach, so the shader's distances stay true.
		consumer.addVertex(matrix, x, y, z).setUv(packedSide, groundColour & 0xFFFFFF)
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

	/** Once per formation, a vessel races from the Focus to every socket it will feed. */
	private static void spawnFormationTendrils(BlockPos origin, long formedSince, List<BlockPos> sockets,
			CardinalRiteFloorVisualRules.Palette palette, float partialTick) {
		long key = origin.asLong();
		if (Long.valueOf(formedSince).equals(TENDRILS_SPAWNED.get(key))) return;
		if (TENDRILS_SPAWNED.size() > TENDRIL_CACHE_LIMIT) TENDRILS_SPAWNED.clear();
		TENDRILS_SPAWNED.put(key, formedSince);
		Vec3 centre = Vec3.atLowerCornerOf(origin).add(0.5D, TENDRIL_ROOT_Y, 0.5D);
		for (int index = 0; index < sockets.size(); index++) {
			Vec3 end = Vec3.atLowerCornerOf(sockets.get(index)).add(0.5D, 0.06D, 0.5D);
			// Each vessel leaves the Focus mouth on the side facing its socket, under the collar.
			Vec3 heading = new Vec3(end.x - centre.x, 0.0D, end.z - centre.z).normalize();
			Vec3 start = centre.add(heading.scale(TENDRIL_ROOT_RADIUS));
			long seed = key * 0x9E3779B97F4A7C15L ^ formedSince * 0xBF58476D1CE4E5B9L ^ index * 0x94D049BB133111EBL;
			TendrilRenderer.INSTANCE.add(new TendrilEffectData(new TendrilAnchor.Point(start),
					new TendrilAnchor.Point(end), tendrilConfig(palette, seed), seed), partialTick);
		}
	}

	private static TendrilEffectConfig tendrilConfig(CardinalRiteFloorVisualRules.Palette palette, long seed) {
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0xF0000000 | (palette.base() & 0xFFFFFF), 0xD0000000 | (palette.vein() & 0xFFFFFF))
				.withBlendColors(false)
				.withLifecycle(30, 26, 18)
				.withShape(20, 1, 0.035F, 0.07F)
				.withBranching(1, 2, 0.14F, 0.4F)
				.withWrithe(0.05F, 0.1F, 0.06F, 0.0F)
				.withFixedSeed(true, seed);
	}
}
