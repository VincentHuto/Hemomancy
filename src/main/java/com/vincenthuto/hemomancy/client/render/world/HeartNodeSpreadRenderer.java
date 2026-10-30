package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.HeartNodeClientState;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes.HeartNodeSpreadUniforms;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.world.HeartNodeFootprint;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import com.vincenthuto.hemomancy.config.HemoClientConfig;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A heart-node sanctum shown as a living mycelial layer creeping over the ground inside its envelope: thick and veined
 * near the heart, lace toward the lobes' edges, frayed into fronds at the boundary, with threads from the heart to
 * each synapse and a heartbeat rolling outward. There is no dome; the sanctum ends where the growth stops.
 *
 * <p>The structure follows {@link MausoleumWardRenderer}: the exposed faces inside an envelope are found once per chunk
 * section and kept per sanctum, rebuilt on a staggered timer, when the envelope changes (a synapse raised or
 * withered) and when the client hears of a block change in the section ({@link #invalidate}), at most
 * {@link #BUILDS_PER_FRAME} a frame. Each sanctum is one {@link HemoRenderTypes#heartNodeSpread} type and one draw;
 * with no sanctum to draw the pass returns before it allocates anything. The per-corner values come from
 * {@link HeartNodeSpreadGeometry}; the look is {@code shaders/core/world/heart_node_spread.fsh}.
 */
public final class HeartNodeSpreadRenderer {
	/** Faces are drawn within this many blocks of the camera; the envelope itself may reach further. */
	private static final double REACH = 48.0D;
	private static final double REACH_SQR = REACH * REACH;
	private static final int REBUILD_TICKS = 100;
	private static final int REBUILD_STAGGER = 40;
	/** Sections found per frame at most, so walking into a sanctum never stalls a frame. */
	private static final int BUILDS_PER_FRAME = 6;
	/** Sections kept per sanctum before the far ones are dropped. */
	private static final int SECTIONS_KEPT = 64;
	/** Just off the surface; the render type's view offset does the rest. */
	private static final float FACE_OFFSET = 0.003F;
	/** Spore motes: the chance a tick that a sanctum sends one up, three a second near the camera. */
	private static final float MOTE_CHANCE = 0.15F;
	private static final ParticleColor[] MOTE_COLORS = {new ParticleColor(226, 220, 206), new ParticleColor(214, 196, 190),
			new ParticleColor(236, 230, 222)};
	private static final Direction[] DIRECTIONS = Direction.values();
	/** Each face's corners in the unit cube, in {x, y, z} steps. */
	private static final int[][][] CORNERS = {
			{{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}},
			{{0, 1, 0}, {0, 1, 1}, {1, 1, 1}, {1, 1, 0}},
			{{0, 0, 0}, {0, 1, 0}, {1, 1, 0}, {1, 0, 0}},
			{{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}},
			{{0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}},
			{{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}}};

	private static final Map<UUID, Sanctum> SANCTUMS = new HashMap<>();
	/** Sanctums whose node withered, by heart position, drawn receding and greying until they are gone. */
	private static final Map<BlockPos, Sanctum> WITHERING = new HashMap<>();
	private static int buildsThisFrame;

	private HeartNodeSpreadRenderer() {
	}

	/** One sanctum as drawn: its lobes' growth, its threads and its kept faces. */
	private static final class Sanctum {
		private HeartNodeFootprint footprint;
		/** The footprint with the receding lobes of withered synapses still in it, which is what faces are found in. */
		private HeartNodeFootprint drawn;
		private List<HeartNodeSpreadGeometry.Thread> threads = List.of();
		private Map<BlockPos, Double> arrivals = Map.of();
		private final Map<BlockPos, Long> appeared = new HashMap<>();
		private final Map<BlockPos, Long> withering = new LinkedHashMap<>();
		private final Long2ObjectOpenHashMap<Section> sections = new Long2ObjectOpenHashMap<>();

		/** Notes synapses raised and withered since the last packet, and finds every section again when it changes. */
		void update(HeartNodeFootprint next, long now) {
			boolean changed = !next.equals(footprint);
			if (changed) {
				if (footprint == null || !next.heart().equals(footprint.heart())) {
					// A sanctum coming into sight, or replanted: what stands is already grown.
					appeared.clear();
					withering.clear();
					for (BlockPos synapse : next.synapses()) appeared.put(synapse, HeartNodeSpreadGeometry.LONG_AGO);
				} else {
					for (BlockPos synapse : next.synapses()) {
						if (!footprint.synapses().contains(synapse)) {
							appeared.put(synapse, now);
							withering.remove(synapse);
						}
					}
					for (BlockPos synapse : footprint.synapses()) {
						if (!next.synapses().contains(synapse)) {
							appeared.remove(synapse);
							withering.put(synapse, now);
						}
					}
				}
				footprint = next;
				threads = HeartNodeSpreadGeometry.threads(next);
				arrivals = HeartNodeSpreadGeometry.arrivals(next);
			}
			if (withering.values().removeIf(at -> HeartNodeSpreadGeometry.withered(at, now))) changed = true;
			if (!changed) return;
			HeartNodeFootprint withReceding = footprint;
			for (BlockPos synapse : withering.keySet()) withReceding = withReceding.withSynapse(synapse);
			drawn = withReceding;
			for (Section section : sections.values()) section.rebuildAt = 0L;
		}
	}

	/** The spread's faces in one chunk section: per face its block, side, light and lift, per corner colour and run. */
	private static final class Section {
		private final long[] blocks;
		private final byte[] faces;
		private final int[] lights;
		private final float[] lifts;
		private final int[] colors;
		private final float[] paths;
		private long rebuildAt;

		Section(long[] blocks, byte[] faces, int[] lights, float[] lifts, int[] colors, float[] paths, long rebuildAt) {
			this.blocks = blocks;
			this.faces = faces;
			this.lights = lights;
			this.lifts = lifts;
			this.colors = colors;
			this.paths = paths;
			this.rebuildAt = rebuildAt;
		}
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		List<HeartNodeClientState.Envelope> visible = visible(HeartNodeClientState.envelopes(), enabled());
		List<HeartNodeClientState.Withering> withering = enabled()
				? HeartNodeClientState.withering(level.getGameTime(), HeartNodeSpreadGeometry.WITHER_TICKS) : List.of();
		if (withering.isEmpty() && !WITHERING.isEmpty()) WITHERING.clear();
		if (visible.isEmpty() && withering.isEmpty()) {
			if (!SANCTUMS.isEmpty()) SANCTUMS.clear();
			return;
		}
		if (SANCTUMS.size() > visible.size()) {
			Set<UUID> owners = new HashSet<>();
			for (HeartNodeClientState.Envelope envelope : visible) owners.add(envelope.footprint().owner());
			SANCTUMS.keySet().retainAll(owners);
		}
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		long now = level.getGameTime();
		float time = AnimationTime.gameTime(level, partialTick);
		buildsThisFrame = 0;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		PoseStack.Pose pose = poseStack.last();
		for (HeartNodeClientState.Envelope envelope : visible) {
			HeartNodeFootprint footprint = envelope.footprint();
			if (!near(footprint, camera)) continue;
			Sanctum sanctum = SANCTUMS.computeIfAbsent(footprint.owner(), owner -> new Sanctum());
			sanctum.update(footprint, now);
			RenderType type = HemoRenderTypes.heartNodeSpread(footprint.owner(),
					uniforms(mc, envelope, sanctum, camera, time, partialTick, 1.0D));
			VertexConsumer consumer = buffers.getBuffer(type);
			drawSections(level, sanctum, consumer, pose, camera, now);
			buffers.endBatch(type);
		}
		if (withering.isEmpty()) return;
		Set<BlockPos> hearts = new HashSet<>();
		for (HeartNodeClientState.Withering gone : withering) {
			HeartNodeClientState.Envelope envelope = gone.envelope();
			HeartNodeFootprint footprint = envelope.footprint();
			hearts.add(gone.heart());
			if (!HeartNodeSpreadGeometry.drawsFor(envelope.settings(), envelope.relation()) || !near(footprint, camera)) {
				continue;
			}
			Sanctum sanctum = WITHERING.computeIfAbsent(gone.heart(), heart -> new Sanctum());
			sanctum.update(footprint, now);
			double life = HeartNodeSpreadGeometry.witherLife(gone.since(), time);
			RenderType type = HemoRenderTypes.heartNodeSpread(footprint.owner(),
					uniforms(mc, envelope, sanctum, camera, time, partialTick, life));
			drawSections(level, sanctum, buffers.getBuffer(type), pose, camera, now);
			buffers.endBatch(type);
		}
		WITHERING.keySet().retainAll(hearts);
	}

	/**
	 * The sanctums this viewer sees: each one's settings allow it (Track 3's spread and strangers bits) and it still has
	 * a heart. With none, or with the client config off, it is the shared empty list, so the pass allocates nothing.
	 */
	static List<HeartNodeClientState.Envelope> visible(List<HeartNodeClientState.Envelope> envelopes, boolean enabled) {
		if (!enabled || envelopes.isEmpty()) return List.of();
		List<HeartNodeClientState.Envelope> out = null;
		for (HeartNodeClientState.Envelope envelope : envelopes) {
			if (envelope.footprint().heart() == null
					|| !HeartNodeSpreadGeometry.drawsFor(envelope.settings(), envelope.relation())) continue;
			if (out == null) out = new ArrayList<>(2);
			out.add(envelope);
		}
		return out == null ? List.of() : out;
	}

	/** A block changed where the client can see it: find the faces of its section, and of its neighbours', again. */
	public static void invalidate(BlockPos pos) {
		if (SANCTUMS.isEmpty()) return;
		for (Sanctum sanctum : SANCTUMS.values()) {
			markDirty(sanctum, SectionPos.asLong(pos));
			for (Direction direction : DIRECTIONS) markDirty(sanctum, SectionPos.asLong(pos.relative(direction)));
		}
	}

	private static void markDirty(Sanctum sanctum, long key) {
		Section section = sanctum.sections.get(key);
		if (section != null) section.rebuildAt = 0L;
	}

	public static void clear() {
		SANCTUMS.clear();
		WITHERING.clear();
	}

	/** Faces kept for {@code owner}'s sanctum, or -1 when it is not being drawn; for the review client. */
	public static int keptFaces(UUID owner) {
		Sanctum sanctum = SANCTUMS.get(owner);
		if (sanctum == null) return -1;
		int faces = 0;
		for (Section section : sanctum.sections.values()) faces += section.blocks.length;
		return faces;
	}

	/** Withered synapse lobes still receding in {@code owner}'s sanctum; for the review client. */
	public static int receding(UUID owner) {
		Sanctum sanctum = SANCTUMS.get(owner);
		return sanctum == null ? 0 : sanctum.withering.size();
	}

	private static boolean enabled() {
		return HemoClientConfig.RENDER_HEART_NODE_SPREAD == null || HemoClientConfig.RENDER_HEART_NODE_SPREAD.get();
	}

	private static boolean near(HeartNodeFootprint footprint, Vec3 camera) {
		BlockPos heart = footprint.heart();
		double dx = heart.getX() + 0.5D - camera.x;
		double dz = heart.getZ() + 0.5D - camera.z;
		double reach = REACH + footprint.maxRadius();
		return dx * dx + dz * dz <= reach * reach;
	}

	// ------------------------------------------------------------------ uniforms

	/**
	 * The shader's uniforms for one sanctum. {@code sanctumLife} is 1 for a living sanctum; for one whose node withered
	 * it falls to 0, drawing every lobe in toward its centre and greying it.
	 */
	private static HeartNodeSpreadUniforms uniforms(Minecraft mc, HeartNodeClientState.Envelope envelope, Sanctum sanctum,
			Vec3 camera, float time, float partialTick, double sanctumLife) {
		float[] lobes = new float[HeartNodeSpreadGeometry.LOBE_SLOTS * 4];
		float[] lives = new float[HeartNodeSpreadGeometry.LOBE_SLOTS];
		float[] arrivals = new float[HeartNodeSpreadGeometry.LOBE_SLOTS];
		for (int slot = 0; slot < HeartNodeSpreadGeometry.LOBE_SLOTS; slot++) {
			lobes[slot * 4 + 3] = -1.0F;
			arrivals[slot] = -1.0F;
		}
		HeartNodeFootprint footprint = sanctum.footprint;
		lobe(lobes, 0, footprint.heart(), camera, receding(HeartNodeSpreadGeometry.heartFront(envelope.plantedAt(), time),
				HeartNodeRules.HEART_RADIUS, sanctumLife));
		lives[0] = (float) sanctumLife;
		arrivals[0] = 0.0F;
		int slot = 1;
		for (BlockPos synapse : footprint.synapses()) {
			if (slot >= HeartNodeSpreadGeometry.LOBE_SLOTS) break;
			long appeared = sanctum.appeared.getOrDefault(synapse, HeartNodeSpreadGeometry.LONG_AGO);
			lobe(lobes, slot, synapse, camera, receding(HeartNodeSpreadGeometry.synapseFront(appeared, time),
					HeartNodeRules.SYNAPSE_RADIUS, sanctumLife));
			lives[slot] = (float) sanctumLife;
			Double arrival = sanctum.arrivals.get(synapse);
			arrivals[slot] = arrival == null ? -1.0F : arrival.floatValue();
			slot++;
		}
		for (Map.Entry<BlockPos, Long> receding : sanctum.withering.entrySet()) {
			if (slot >= HeartNodeSpreadGeometry.LOBE_SLOTS) break;
			double life = HeartNodeSpreadGeometry.witherLife(receding.getValue(), time) * sanctumLife;
			lobe(lobes, slot, receding.getKey(), camera, (HeartNodeRules.SYNAPSE_RADIUS + 1.0D) * life);
			lives[slot] = (float) life;
			slot++;
		}
		Vec3 feet = mc.player == null ? camera.add(0.0D, -1000.0D, 0.0D) : mc.player.getPosition(partialTick);
		HeartNodeSettings settings = envelope.settings();
		boolean stranger = envelope.relation() == HeartNodeRelation.STRANGER;
		return new HeartNodeSpreadUniforms(time, lobes, lives, arrivals,
				(float) (feet.x - camera.x), (float) (feet.y - camera.y), (float) (feet.z - camera.z),
				settings.has(HeartNodeSettings.EDGE), settings.has(HeartNodeSettings.THREADS) && !stranger,
				settings.has(HeartNodeSettings.PULSE), stranger,
				settings.intensity() / (float) HeartNodeSettings.MAX_INTENSITY);
	}

	/** A lobe's front as its sanctum withers: drawn in from wherever it had grown to, gone at {@code life} 0. */
	private static double receding(double front, double radius, double life) {
		return life >= 1.0D ? front : Math.min(front, radius + 1.0D) * life;
	}

	private static void lobe(float[] lobes, int slot, BlockPos centre, Vec3 camera, double front) {
		lobes[slot * 4] = (float) (centre.getX() + 0.5D - camera.x);
		lobes[slot * 4 + 1] = (float) (centre.getY() + 0.5D - camera.y);
		lobes[slot * 4 + 2] = (float) (centre.getZ() + 0.5D - camera.z);
		lobes[slot * 4 + 3] = (float) front;
	}

	// ------------------------------------------------------------------ faces

	private static void drawSections(ClientLevel level, Sanctum sanctum, VertexConsumer consumer, PoseStack.Pose pose,
			Vec3 camera, long now) {
		int[] box = bounds(sanctum.drawn);
		int minX = SectionPos.blockToSectionCoord(Math.max(box[0], Mth.floor(camera.x - REACH)));
		int maxX = SectionPos.blockToSectionCoord(Math.min(box[3], Mth.floor(camera.x + REACH)));
		int minY = SectionPos.blockToSectionCoord(Math.max(box[1], Mth.floor(camera.y - REACH)));
		int maxY = SectionPos.blockToSectionCoord(Math.min(box[4], Mth.floor(camera.y + REACH)));
		int minZ = SectionPos.blockToSectionCoord(Math.max(box[2], Mth.floor(camera.z - REACH)));
		int maxZ = SectionPos.blockToSectionCoord(Math.min(box[5], Mth.floor(camera.z + REACH)));
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int sx = minX; sx <= maxX; sx++) {
			for (int sz = minZ; sz <= maxZ; sz++) {
				for (int sy = minY; sy <= maxY; sy++) {
					long key = SectionPos.asLong(sx, sy, sz);
					Section section = sanctum.sections.get(key);
					if ((section == null || now >= section.rebuildAt) && buildsThisFrame < BUILDS_PER_FRAME
							&& level.getChunkSource().hasChunk(sx, sz)) {
						buildsThisFrame++;
						if (section == null && sanctum.sections.size() >= SECTIONS_KEPT) dropFar(sanctum.sections, camera);
						section = build(level, sanctum, box, sx, sy, sz, now + REBUILD_TICKS
								+ Math.floorMod(Long.hashCode(key) * 31, REBUILD_STAGGER));
						sanctum.sections.put(key, section);
					}
					if (section != null) emit(section, consumer, pose, camera, pos);
				}
			}
		}
	}

	/** The block box the lobes of {@code footprint} can reach: min x, y, z then max x, y, z. */
	private static int[] bounds(HeartNodeFootprint footprint) {
		int[] box = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE,
				Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
		widen(box, footprint.heart(), HeartNodeRules.HEART_RADIUS);
		for (BlockPos synapse : footprint.synapses()) widen(box, synapse, HeartNodeRules.SYNAPSE_RADIUS);
		return box;
	}

	private static void widen(int[] box, BlockPos centre, double radius) {
		int reach = Mth.ceil(radius);
		// One cell more in each direction: a floor is the solid block under a cell inside the envelope.
		int band = HeartNodeRules.VERTICAL_BAND + 1;
		box[0] = Math.min(box[0], centre.getX() - reach - 1);
		box[1] = Math.min(box[1], centre.getY() - band);
		box[2] = Math.min(box[2], centre.getZ() - reach - 1);
		box[3] = Math.max(box[3], centre.getX() + reach + 1);
		box[4] = Math.max(box[4], centre.getY() + band);
		box[5] = Math.max(box[5], centre.getZ() + reach + 1);
	}

	/**
	 * Every face the spread covers in one section: a solid block whose neighbour on that side is open, holds no fluid
	 * and lies inside the envelope, weighted by orientation (floors full, walls near the floor, ceilings only tips).
	 */
	private static Section build(ClientLevel level, Sanctum sanctum, int[] box, int sx, int sy, int sz, long rebuildAt) {
		LongArrayList blocks = new LongArrayList();
		ByteArrayList faces = new ByteArrayList();
		IntArrayList lights = new IntArrayList();
		FloatArrayList lifts = new FloatArrayList();
		IntArrayList colors = new IntArrayList();
		FloatArrayList paths = new FloatArrayList();
		HeartNodeFootprint drawn = sanctum.drawn;
		int x0 = SectionPos.sectionToBlockCoord(sx), y0 = SectionPos.sectionToBlockCoord(sy);
		int z0 = SectionPos.sectionToBlockCoord(sz);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos beyond = new BlockPos.MutableBlockPos();
		double[] weights = new double[4];
		for (int x = Math.max(x0, box[0]); x <= Math.min(x0 + 15, box[3]); x++) {
			for (int y = Math.max(y0, box[1]); y <= Math.min(y0 + 15, box[4]); y++) {
				for (int z = Math.max(z0, box[2]); z <= Math.min(z0 + 15, box[5]); z++) {
					pos.set(x, y, z);
					if (!carries(level, pos)) continue;
					for (Direction direction : DIRECTIONS) {
						beyond.setWithOffset(pos, direction);
						BlockState open = level.getBlockState(beyond);
						if (open.isSolidRender(level, beyond) || !open.getFluidState().isEmpty()
								|| !drawn.contains(beyond)) continue;
						if (!weigh(level, direction, beyond, weights)) continue;
						float lift = direction == Direction.UP ? lift(level, beyond, open) : 0.0F;
						blocks.add(pos.asLong());
						faces.add((byte) direction.ordinal());
						lights.add(LevelRenderer.getLightColor(level, beyond));
						lifts.add(lift);
						corners(sanctum, pos, direction, beyond.getY(), weights, colors, paths);
					}
				}
			}
		}
		return new Section(blocks.toLongArray(), faces.toByteArray(), lights.toIntArray(), lifts.toFloatArray(),
				colors.toIntArray(), paths.toFloatArray(), rebuildAt);
	}

	/** A block the layer grows on: solid, and not glass, leaves or the node and synapse themselves (Track 5 draws those). */
	private static boolean carries(ClientLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (!state.isSolidRender(level, pos)) return false;
		return !state.is(BlockTags.LEAVES) && !state.is(Tags.Blocks.GLASS_BLOCKS)
				&& !state.is(BlockInit.heart_node.get()) && !state.is(BlockInit.mycelial_synapse.get());
	}

	/** Fills each corner's orientation weight; false when the face carries none. */
	private static boolean weigh(ClientLevel level, Direction direction, BlockPos open, double[] weights) {
		int[][] corners = CORNERS[direction.ordinal()];
		if (direction == Direction.UP) {
			for (int i = 0; i < 4; i++) weights[i] = 1.0D;
			return true;
		}
		int gap = floorGap(level, open, HeartNodeSpreadGeometry.CEILING_REACH + 1);
		boolean any = false;
		for (int i = 0; i < 4; i++) {
			int[] corner = corners[i];
			if (direction == Direction.DOWN) {
				boolean besideWall = solid(level, open.relative(corner[0] == 0 ? Direction.WEST : Direction.EAST))
						|| solid(level, open.relative(corner[2] == 0 ? Direction.NORTH : Direction.SOUTH));
				weights[i] = HeartNodeSpreadGeometry.ceilingWeight(besideWall, gap);
			} else {
				weights[i] = HeartNodeSpreadGeometry.wallWeight(gap + corner[1]);
			}
			any |= weights[i] > 0.0D;
		}
		return any;
	}

	/** Open cells under {@code open} before a solid one, counting {@code open} itself out; capped at {@code cap}. */
	private static int floorGap(ClientLevel level, BlockPos open, int cap) {
		BlockPos.MutableBlockPos below = open.mutable();
		for (int gap = 0; gap < cap; gap++) {
			below.move(Direction.DOWN);
			if (solid(level, below)) return gap;
		}
		return cap;
	}

	private static boolean solid(ClientLevel level, BlockPos pos) {
		return level.getBlockState(pos).isSolidRender(level, pos);
	}

	/** A carpet or snow layer over a floor: the layer is drawn on, at its top. */
	private static float lift(ClientLevel level, BlockPos open, BlockState state) {
		if (!(state.getBlock() instanceof CarpetBlock) && !(state.getBlock() instanceof SnowLayerBlock)) return 0.0F;
		return (float) state.getShape(level, open).max(Direction.Axis.Y);
	}

	private static void corners(Sanctum sanctum, BlockPos pos, Direction direction, int cellY, double[] weights,
			IntArrayList colors, FloatArrayList paths) {
		HeartNodeFootprint drawn = sanctum.drawn;
		int[][] corners = CORNERS[direction.ordinal()];
		HeartNodeSpreadGeometry.Thread thread = direction == Direction.UP ? HeartNodeSpreadGeometry.nearestThread(
				sanctum.threads, pos.getX() + 0.5D, pos.getZ() + 0.5D) : null;
		for (int i = 0; i < 4; i++) {
			double x = pos.getX() + corners[i][0];
			double z = pos.getZ() + corners[i][2];
			double strength = HeartNodeSpreadGeometry.bodyStrength(drawn, x, z);
			double depth = HeartNodeSpreadGeometry.edgeDepth(drawn, x, z, cellY);
			double toThread = thread == null ? HeartNodeSpreadGeometry.NO_THREAD : thread.signedDistance(x, z);
			colors.add(HeartNodeSpreadGeometry.pack(strength, depth, toThread, weights[i]));
			paths.add(thread == null ? 0.0F : (float) thread.pathLength(x, z));
		}
	}

	private static void dropFar(Long2ObjectOpenHashMap<Section> sections, Vec3 camera) {
		int cx = SectionPos.blockToSectionCoord(Mth.floor(camera.x));
		int cy = SectionPos.blockToSectionCoord(Mth.floor(camera.y));
		int cz = SectionPos.blockToSectionCoord(Mth.floor(camera.z));
		sections.long2ObjectEntrySet().removeIf(entry -> {
			long key = entry.getLongKey();
			return Math.abs(SectionPos.x(key) - cx) > 3 || Math.abs(SectionPos.y(key) - cy) > 3
					|| Math.abs(SectionPos.z(key) - cz) > 3;
		});
	}

	private static void emit(Section section, VertexConsumer consumer, PoseStack.Pose pose, Vec3 camera,
			BlockPos.MutableBlockPos pos) {
		for (int i = 0; i < section.blocks.length; i++) {
			pos.set(section.blocks[i]);
			Direction direction = DIRECTIONS[section.faces[i]];
			double dx = pos.getX() + 0.5D + direction.getStepX() * 0.5D - camera.x;
			double dy = pos.getY() + 0.5D + direction.getStepY() * 0.5D - camera.y;
			double dz = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D - camera.z;
			if (dx * dx + dy * dy + dz * dz > REACH_SQR) continue;
			int[][] corners = CORNERS[direction.ordinal()];
			float offset = FACE_OFFSET + section.lifts[i];
			float ox = direction.getStepX() * offset, oy = direction.getStepY() * offset, oz = direction.getStepZ() * offset;
			for (int c = 0; c < 4; c++) {
				int color = section.colors[i * 4 + c];
				consumer.addVertex(pose.pose(), (float) (pos.getX() + corners[c][0] - camera.x) + ox,
								(float) (pos.getY() + corners[c][1] - camera.y) + oy,
								(float) (pos.getZ() + corners[c][2] - camera.z) + oz)
						.setColor(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24)
						.setUv(section.paths[i * 4 + c], 0.0F)
						.setLight(section.lights[i])
						.setNormal(pose, direction.getStepX(), direction.getStepY(), direction.getStepZ());
			}
		}
	}

	// ------------------------------------------------------------------ motes

	/**
	 * A few pale spore motes rising slowly from floor faces in the heart lobe of each sanctum drawn with its motes on,
	 * about three a second near the camera and fewer farther off. Called once per client tick.
	 */
	public static void tickMotes() {
		if (SANCTUMS.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || mc.isPaused()) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		RandomSource random = level.random;
		for (HeartNodeClientState.Envelope envelope : HeartNodeClientState.envelopes()) {
			if (!envelope.settings().has(HeartNodeSettings.MOTES) || random.nextFloat() >= MOTE_CHANCE) continue;
			Sanctum sanctum = SANCTUMS.get(envelope.footprint().owner());
			if (sanctum == null || sanctum.sections.isEmpty()) continue;
			BlockPos heart = envelope.footprint().heart();
			double front = Math.min(HeartNodeRules.HEART_RADIUS,
					HeartNodeSpreadGeometry.heartFront(envelope.plantedAt(), level.getGameTime()));
			Section section = randomSection(sanctum, random);
			if (section == null || section.blocks.length == 0) continue;
			int face = random.nextInt(section.blocks.length);
			if (section.faces[face] != Direction.UP.ordinal()) continue;
			BlockPos floor = BlockPos.of(section.blocks[face]);
			double x = floor.getX() + random.nextDouble();
			double y = floor.getY() + 1.0D + section.lifts[face] + 0.05D;
			double z = floor.getZ() + random.nextDouble();
			double fromHeart = Math.hypot(x - heart.getX() - 0.5D, z - heart.getZ() - 0.5D);
			double distance = camera.distanceTo(new Vec3(x, y, z));
			if (fromHeart > front || random.nextDouble() > 1.0D - distance / REACH) continue;
			level.addParticle(HemoParticleData.sporiticSpore(MOTE_COLORS[random.nextInt(MOTE_COLORS.length)]), x, y, z,
					(random.nextDouble() - 0.5D) * 0.006D, 0.01D + random.nextDouble() * 0.012D,
					(random.nextDouble() - 0.5D) * 0.006D);
		}
	}

	private static Section randomSection(Sanctum sanctum, RandomSource random) {
		int skip = random.nextInt(sanctum.sections.size());
		for (Section section : sanctum.sections.values()) {
			if (skip-- == 0) return section;
		}
		return null;
	}
}
