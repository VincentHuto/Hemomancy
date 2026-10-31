package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.FaneBoundaryClientData;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes.FaneGroundUniforms;
import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Founding Fane drawn on the ground instead of as a dome: Engram glyphs scattered over the floor inside its bounds, a
 * heartbeat rolling out from the heart, and a close ring of glyphs along a bright cut where the bounds meet the ground.
 * Drawn in the SURFACE {@link com.vincenthuto.hemomancy.config.HemoClientConfig.FaneBoundaryStyle} as the viewer nears
 * a fane, while {@link FaneBoundaryRenderer} fades that fane's dome out; from afar the dome is all there is.
 *
 * <p>The shape is the server's, circles across the ground unbounded up and down, with each stake grown into the heart's
 * bounds (or another stake's) through a rounded neck ({@link FaneGroundGeometry}).
 *
 * <p>The structure follows {@link MausoleumWardRenderer}: the floors inside a fane's bounds are found once per chunk
 * section and kept per fane, rebuilt on a staggered timer, when its stakes change and when the client hears of a block
 * change in the section ({@link #invalidate}), at most {@link #BUILDS_PER_FRAME} a frame. Each fane is one
 * {@link HemoRenderTypes#faneGround} type and one draw. The look is {@code shaders/core/world/fane_ground.fsh}.
 */
public final class FaneGroundRenderer {
	/** Floors are drawn within this many blocks of the camera. */
	private static final double REACH = 48.0D;
	private static final double REACH_SQR = REACH * REACH;
	/** Floors are looked for this far above and below the camera; past it they are hidden or faded anyway. */
	private static final double VERTICAL_REACH = 32.0D;
	/** The ground is whole this close outside a fane's bounds (across the ground) and gone this far out. */
	private static final double FADE_NEAR = 24.0D;
	private static final double FADE_FAR = 40.0D;
	private static final int REBUILD_TICKS = 100;
	private static final int REBUILD_STAGGER = 40;
	/** Sections found per frame at most, so walking into a fane never stalls a frame. */
	private static final int BUILDS_PER_FRAME = 8;
	/** Sections kept per fane before the far ones are dropped. */
	private static final int SECTIONS_KEPT = 96;
	/** Just off the surface; the render type's view offset does the rest. */
	private static final float FACE_OFFSET = 0.003F;
	/** World x/z wrap for the glyph grid, so UVs stay precise far from the origin. */
	private static final int UV_WRAP = 4096;
	/** A corner's depth inside the bounds goes to vertex red as (depth - DEPTH_FLOOR) / DEPTH_SPAN. */
	private static final double DEPTH_FLOOR = -2.0D;
	private static final double DEPTH_SPAN = 10.0D;
	/** The floor face's corners in the unit cube, in {x, z} steps, wound as Direction.UP's. */
	private static final int[][] CORNERS = {{0, 0}, {0, 1}, {1, 1}, {1, 0}};

	private static final Map<BlockPos, Fane> FANES = new HashMap<>();
	private static int buildsThisFrame;

	private FaneGroundRenderer() {
	}

	/** One fane as drawn: the bounds its faces were found in, and its kept faces. */
	private static final class Fane {
		private List<BlockPos> stakes = List.of();
		private float radius = -1.0F;
		private final Long2ObjectOpenHashMap<Section> sections = new Long2ObjectOpenHashMap<>();

		/** Finds every section again when a stake is raised or lost. */
		void update(FaneBoundaryClientData.Entry entry) {
			if (stakes.equals(entry.stakes()) && radius == entry.radius()) return;
			stakes = List.copyOf(entry.stakes());
			radius = entry.radius();
			sections.clear();
		}
	}

	/**
	 * The floor faces in one chunk section: per face its block, lift and the quarter turn its glyph faces
	 * ({@link FaneGroundGeometry#facing}), per corner its depth inside the bounds.
	 */
	private static final class Section {
		private final long[] blocks;
		private final float[] lifts;
		private final byte[] facings;
		private final byte[] depths;
		private long rebuildAt;

		Section(long[] blocks, float[] lifts, byte[] facings, byte[] depths, long rebuildAt) {
			this.blocks = blocks;
			this.lifts = lifts;
			this.facings = facings;
			this.depths = depths;
			this.rebuildAt = rebuildAt;
		}
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || mc.player == null) return;
		if (!FaneBoundaryRenderer.surfaceStyle() || !FaneBoundaryRenderer.faneBoundaryRendererEnabled()) {
			if (!FANES.isEmpty()) FANES.clear();
			return;
		}
		List<FaneBoundaryClientData.Entry> entries = FaneBoundaryClientData.entries();
		if (entries.isEmpty()) {
			if (!FANES.isEmpty()) FANES.clear();
			return;
		}
		if (!FANES.isEmpty()) {
			Set<BlockPos> hearts = new HashSet<>();
			for (FaneBoundaryClientData.Entry entry : entries) hearts.add(entry.heart());
			FANES.keySet().retainAll(hearts);
		}
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		long now = level.getGameTime();
		float time = AnimationTime.gameTime(level, partialTick);
		Vec3 feet = mc.player.getPosition(partialTick);
		buildsThisFrame = 0;
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		PoseStack.Pose pose = poseStack.last();
		for (FaneBoundaryClientData.Entry entry : entries) {
			float fade = 1.0F - (float) HemoEasing.smoothstep(FADE_NEAR, FADE_FAR,
					Math.max(0.0D, -depth(entry, camera.x, camera.z)));
			if (fade <= 0.01F) continue;
			Fane fane = FANES.computeIfAbsent(entry.heart(), heart -> new Fane());
			fane.update(entry);
			RenderType type = HemoRenderTypes.faneGround(entry.heart(), uniforms(entry, camera, feet, time, fade));
			VertexConsumer consumer = buffers.getBuffer(type);
			drawSections(level, entry, fane, consumer, pose, camera, now);
			buffers.endBatch(type);
		}
	}

	/** A block changed where the client can see it: find the floors of its section, and the one under it, again. */
	public static void invalidate(BlockPos pos) {
		if (FANES.isEmpty()) return;
		long key = SectionPos.asLong(pos);
		long below = SectionPos.asLong(pos.below());
		for (Fane fane : FANES.values()) {
			markDirty(fane, key);
			if (below != key) markDirty(fane, below);
		}
	}

	private static void markDirty(Fane fane, long key) {
		Section section = fane.sections.get(key);
		if (section != null) section.rebuildAt = 0L;
	}

	public static void clear() {
		FANES.clear();
	}

	/** Floor faces kept for the fane whose heart is {@code heart}, or -1 when it is not being drawn; for the review client. */
	public static int keptFaces(BlockPos heart) {
		Fane fane = FANES.get(heart);
		if (fane == null) return -1;
		int faces = 0;
		for (Section section : fane.sections.values()) faces += section.blocks.length;
		return faces;
	}

	// ------------------------------------------------------------------ uniforms

	private static FaneGroundUniforms uniforms(FaneBoundaryClientData.Entry entry, Vec3 camera, Vec3 feet, float time,
			float fade) {
		FaneBoundaryRelation relation = FaneBoundaryRenderer.effectiveRelation(entry);
		BlockPos heart = entry.heart();
		return new FaneGroundUniforms(time, color(relation),
				(float) (heart.getX() + 0.5D - camera.x), (float) (heart.getY() + 0.15D - camera.y),
				(float) (heart.getZ() + 0.5D - camera.z), FaneBoundaryRenderer.HEART_RADIUS,
				(float) (feet.x - camera.x), (float) (feet.y - camera.y), (float) (feet.z - camera.z),
				relationCode(relation), (float) REACH, fade);
	}

	/** The ground's colour to this viewer: warm for its own members, deep and dark for those it keeps out. */
	private static int color(FaneBoundaryRelation relation) {
		return switch (relation) {
			case MEMBER -> 0xF2381F;
			case RIVAL_ELDER -> 0xD91A0F;
			case OUTSIDER -> 0xC00A08;
			case MUNDANE_OUTSIDER -> 0x731008;
		};
	}

	/** The shader's relation code: 0 member, 1 rival elder, 2 outsider, 3 mundane outsider. */
	private static float relationCode(FaneBoundaryRelation relation) {
		return switch (relation) {
			case MEMBER -> 0.0F;
			case RIVAL_ELDER -> 1.0F;
			case OUTSIDER -> 2.0F;
			case MUNDANE_OUTSIDER -> 3.0F;
		};
	}

	// ------------------------------------------------------------------ faces

	/**
	 * Draws every kept section in reach, then finds the missing or stale ones nearest the camera first, so arriving at
	 * a fane fills the ground out from the viewer's feet rather than in grid order.
	 */
	private static void drawSections(ClientLevel level, FaneBoundaryClientData.Entry entry, Fane fane,
			VertexConsumer consumer, PoseStack.Pose pose, Vec3 camera, long now) {
		int[] box = bounds(level, entry);
		int minX = SectionPos.blockToSectionCoord(Math.max(box[0], Mth.floor(camera.x - REACH)));
		int maxX = SectionPos.blockToSectionCoord(Math.min(box[3], Mth.floor(camera.x + REACH)));
		int minY = SectionPos.blockToSectionCoord(Math.max(box[1], Mth.floor(camera.y - VERTICAL_REACH)));
		int maxY = SectionPos.blockToSectionCoord(Math.min(box[4], Mth.floor(camera.y + VERTICAL_REACH)));
		int minZ = SectionPos.blockToSectionCoord(Math.max(box[2], Mth.floor(camera.z - REACH)));
		int maxZ = SectionPos.blockToSectionCoord(Math.min(box[5], Mth.floor(camera.z + REACH)));
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		LongArrayList wanted = null;
		for (int sx = minX; sx <= maxX; sx++) {
			for (int sz = minZ; sz <= maxZ; sz++) {
				for (int sy = minY; sy <= maxY; sy++) {
					long key = SectionPos.asLong(sx, sy, sz);
					Section section = fane.sections.get(key);
					if (section != null) emit(section, consumer, pose, camera, pos);
					if ((section == null || now >= section.rebuildAt) && level.getChunkSource().hasChunk(sx, sz)) {
						if (wanted == null) wanted = new LongArrayList();
						wanted.add(key);
					}
				}
			}
		}
		if (wanted == null || buildsThisFrame >= BUILDS_PER_FRAME) return;
		int cx = SectionPos.blockToSectionCoord(Mth.floor(camera.x));
		int cy = SectionPos.blockToSectionCoord(Mth.floor(camera.y));
		int cz = SectionPos.blockToSectionCoord(Mth.floor(camera.z));
		wanted.sort((long a, long b) -> Integer.compare(sectionDistance(a, cx, cy, cz), sectionDistance(b, cx, cy, cz)));
		for (int i = 0; i < wanted.size() && buildsThisFrame < BUILDS_PER_FRAME; i++) {
			long key = wanted.getLong(i);
			boolean fresh = !fane.sections.containsKey(key);
			if (fresh && fane.sections.size() >= SECTIONS_KEPT) dropFar(fane.sections, camera);
			long rebuildAt = now + REBUILD_TICKS + Math.floorMod(Long.hashCode(key) * 31, REBUILD_STAGGER);
			if (onlyAir(level, key)) {
				// No block in it can carry a floor, and finding that out costs nothing, so it spends none of the frame's builds.
				fane.sections.put(key, new Section(new long[0], new float[0], new byte[0], new byte[0], rebuildAt));
				continue;
			}
			buildsThisFrame++;
			Section section = build(level, entry, box, SectionPos.x(key), SectionPos.y(key), SectionPos.z(key),
					rebuildAt);
			fane.sections.put(key, section);
			// A stale section was drawn above already; only a fresh one still needs drawing this frame.
			if (fresh) emit(section, consumer, pose, camera, pos);
		}
	}

	private static boolean onlyAir(ClientLevel level, long key) {
		int index = level.getSectionIndexFromSectionY(SectionPos.y(key));
		if (index < 0 || index >= level.getSectionsCount()) return true;
		return level.getChunk(SectionPos.x(key), SectionPos.z(key)).getSection(index).hasOnlyAir();
	}

	private static int sectionDistance(long key, int cx, int cy, int cz) {
		int dx = SectionPos.x(key) - cx, dy = SectionPos.y(key) - cy, dz = SectionPos.z(key) - cz;
		return dx * dx + dy * dy * 4 + dz * dz;
	}

	/**
	 * The block box the fane's bounds can reach: min x, y, z then max x, y, z. The lobes run the world's full height;
	 * across the ground they reach a little past each circle, for the necks.
	 */
	private static int[] bounds(ClientLevel level, FaneBoundaryClientData.Entry entry) {
		int[] box = {Integer.MAX_VALUE, level.getMinBuildHeight(), Integer.MAX_VALUE,
				Integer.MIN_VALUE, level.getMaxBuildHeight() - 1, Integer.MIN_VALUE};
		widen(box, entry.heart(), FaneBoundaryRenderer.HEART_RADIUS);
		for (BlockPos stake : entry.stakes()) widen(box, stake, entry.radius());
		return box;
	}

	private static void widen(int[] box, BlockPos centre, float radius) {
		int reach = Mth.ceil(radius + FaneGroundGeometry.BLEND * 0.25D) + 1;
		box[0] = Math.min(box[0], centre.getX() - reach);
		box[2] = Math.min(box[2], centre.getZ() - reach);
		box[3] = Math.max(box[3], centre.getX() + reach);
		box[5] = Math.max(box[5], centre.getZ() + reach);
	}

	/**
	 * Every floor in one section with a corner inside the bounds: the top of a solid block under an open cell that holds
	 * no fluid. Each corner keeps its depth inside the bounds, so the shader cuts the edge as a smooth curve.
	 */
	private static Section build(ClientLevel level, FaneBoundaryClientData.Entry entry, int[] box, int sx, int sy, int sz,
			long rebuildAt) {
		LongArrayList blocks = new LongArrayList();
		FloatArrayList lifts = new FloatArrayList();
		ByteArrayList facings = new ByteArrayList();
		ByteArrayList depths = new ByteArrayList();
		int x0 = SectionPos.sectionToBlockCoord(sx), y0 = SectionPos.sectionToBlockCoord(sy);
		int z0 = SectionPos.sectionToBlockCoord(sz);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
		double[] corners = new double[4];
		for (int x = Math.max(x0, box[0]); x <= Math.min(x0 + 15, box[3]); x++) {
			for (int z = Math.max(z0, box[2]); z <= Math.min(z0 + 15, box[5]); z++) {
				for (int y = Math.max(y0, box[1]); y <= Math.min(y0 + 15, box[4]); y++) {
					pos.set(x, y, z);
					if (!carries(level, pos)) continue;
					above.setWithOffset(pos, Direction.UP);
					BlockState open = level.getBlockState(above);
					if (open.isSolidRender(level, above) || !open.getFluidState().isEmpty()) continue;
					boolean inside = false;
					for (int c = 0; c < 4; c++) {
						corners[c] = depth(entry, x + CORNERS[c][0], z + CORNERS[c][1]);
						inside |= corners[c] >= 0.0D;
					}
					if (!inside) continue;
					blocks.add(pos.asLong());
					lifts.add(lift(level, above, open));
					facings.add((byte) FaneGroundGeometry.facing(entry.heart(), FaneBoundaryRenderer.HEART_RADIUS,
							entry.stakes(), entry.radius(), x + 0.5D, z + 0.5D));
					for (double depth : corners) depths.add(encodeDepth(depth));
				}
			}
		}
		return new Section(blocks.toLongArray(), lifts.toFloatArray(), facings.toByteArray(), depths.toByteArray(),
				rebuildAt);
	}

	/** A block the glyphs are written on: solid, and not glass or leaves. */
	private static boolean carries(ClientLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.isSolidRender(level, pos) && !state.is(BlockTags.LEAVES) && !state.is(Tags.Blocks.GLASS_BLOCKS);
	}

	/** How deep a point lies inside the fane's bounds, in blocks: negative outside them. */
	private static double depth(FaneBoundaryClientData.Entry entry, double x, double z) {
		return FaneGroundGeometry.depth(entry.heart(), FaneBoundaryRenderer.HEART_RADIUS, entry.stakes(), entry.radius(),
				x, z);
	}

	private static byte encodeDepth(double depth) {
		return (byte) Math.round(HemoEasing.clamp01((depth - DEPTH_FLOOR) / DEPTH_SPAN) * 255.0D);
	}

	/** A carpet or snow layer over a floor: the glyphs are drawn on its top. */
	private static float lift(ClientLevel level, BlockPos open, BlockState state) {
		if (!(state.getBlock() instanceof CarpetBlock) && !(state.getBlock() instanceof SnowLayerBlock)) return 0.0F;
		return (float) state.getShape(level, open).max(Direction.Axis.Y);
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
			double dx = pos.getX() + 0.5D - camera.x;
			double dy = pos.getY() + 1.0D - camera.y;
			double dz = pos.getZ() + 0.5D - camera.z;
			if (dx * dx + dy * dy + dz * dz > REACH_SQR) continue;
			float top = (float) (pos.getY() + 1.0D - camera.y) + FACE_OFFSET + section.lifts[i];
			int u = Math.floorMod(pos.getX(), UV_WRAP);
			int v = Math.floorMod(pos.getZ(), UV_WRAP);
			int facing = section.facings[i] * 64;
			for (int c = 0; c < 4; c++) {
				int depth = section.depths[i * 4 + c] & 0xFF;
				consumer.addVertex(pose.pose(), (float) (pos.getX() + CORNERS[c][0] - camera.x), top,
								(float) (pos.getZ() + CORNERS[c][1] - camera.z))
						.setColor(depth, facing, 0, 255)
						.setUv(u + CORNERS[c][0], v + CORNERS[c][1])
						.setLight(LightTexture.FULL_BRIGHT)
						.setNormal(pose, 0.0F, 1.0F, 0.0F);
			}
		}
	}
}
