package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LivingVeinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LuxTraceBlock;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the ground network's light (core-loop Track 6): lit gouge runs in their function's colour, living veins as a
 * faint pulse of tissue, and Lux traces, each as a thin strip in the block's own 16 px grid joining a cell's centre to
 * the neighbours its run continues into, so a run reads as one narrow line however it bends. A dormant gouge is only
 * its baked groove and a dark run is not drawn at all. Partly clear, with a slow shimmer travelling along the run.
 *
 * <p>Marks are found per chunk section near the camera, only in sections whose palette holds one, rebuilt when the
 * client hears of a block change there ({@link #invalidate}) or every two seconds, a few sections a frame.
 */
public final class GroundRunRenderer {
	private static final int RADIUS_SECTIONS = 2;
	private static final int REBUILD_TICKS = 40;
	private static final int BUILDS_PER_FRAME = 4;
	/** Strips sit this far off the face, inside the cell: just above a gouge's baked groove (0.1 px off the face). */
	private static final float LIFT = 0.012F;
	private static final float HALF_CORE = 1.0F / 16.0F;
	private static final float HALF_HALO = 2.0F / 16.0F;
	private static final int TISSUE = 0xA82434;
	private static final int LUX = 0xFFE6A8;
	private static final Direction[] DIRECTIONS = Direction.values();

	private static final Long2ObjectOpenHashMap<Section> SECTIONS = new Long2ObjectOpenHashMap<>();
	private static ClientLevel lastLevel;

	private GroundRunRenderer() {
	}

	private enum Kind { GOUGE, VEIN, LUX }

	/** One face of one mark, with the in-plane directions its run continues toward. */
	private record Face(BlockPos cell, Direction face, Kind kind, GroundFunction function, boolean lit, int fade,
			List<Direction> joins) {
	}

	private static final class Section {
		final List<Face> faces;
		long builtAt;
		boolean dirty;

		Section(List<Face> faces, long builtAt) {
			this.faces = faces;
			this.builtAt = builtAt;
		}
	}

	public static void render(PoseStack poseStack, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level != lastLevel) {
			SECTIONS.clear();
			lastLevel = level;
		}
		if (level == null) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		SectionPos centre = SectionPos.of(BlockPos.containing(camera));
		long now = level.getGameTime();
		int builds = 0;
		SECTIONS.long2ObjectEntrySet().removeIf(entry -> {
			SectionPos at = SectionPos.of(entry.getLongKey());
			return Math.abs(at.x() - centre.x()) > RADIUS_SECTIONS || Math.abs(at.y() - centre.y()) > RADIUS_SECTIONS
					|| Math.abs(at.z() - centre.z()) > RADIUS_SECTIONS;
		});
		List<Section> drawn = new ArrayList<>();
		for (int dx = -RADIUS_SECTIONS; dx <= RADIUS_SECTIONS; dx++) {
			for (int dy = -RADIUS_SECTIONS; dy <= RADIUS_SECTIONS; dy++) {
				for (int dz = -RADIUS_SECTIONS; dz <= RADIUS_SECTIONS; dz++) {
					long key = SectionPos.asLong(centre.x() + dx, centre.y() + dy, centre.z() + dz);
					Section section = SECTIONS.get(key);
					boolean stale = section == null || section.dirty || now - section.builtAt > REBUILD_TICKS;
					if (stale && builds < BUILDS_PER_FRAME) {
						builds++;
						section = build(level, SectionPos.of(key), now);
						SECTIONS.put(key, section);
					}
					if (section != null && !section.faces.isEmpty()) drawn.add(section);
				}
			}
		}
		if (drawn.isEmpty()) return;
		float time = AnimationTime.gameTime(level, partialTick);
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
		VertexConsumer consumer = buffers.getBuffer(HemoRenderTypes.GROUND_RUN_GLOW);
		Matrix4f pose = poseStack.last().pose();
		for (Section section : drawn) {
			for (Face face : section.faces) draw(consumer, pose, camera, face, time);
		}
		buffers.endBatch(HemoRenderTypes.GROUND_RUN_GLOW);
	}

	/** A block changed near {@code pos}: find the marks in its section and its neighbours' again. */
	public static void invalidate(BlockPos pos) {
		if (SECTIONS.isEmpty()) return;
		markDirty(SectionPos.asLong(pos));
		for (Direction direction : DIRECTIONS) markDirty(SectionPos.asLong(pos.relative(direction)));
	}

	private static void markDirty(long key) {
		Section section = SECTIONS.get(key);
		if (section != null) section.dirty = true;
	}

	public static void clear() {
		SECTIONS.clear();
	}

	private static Section build(ClientLevel level, SectionPos at, long now) {
		List<Face> faces = new ArrayList<>();
		if (!level.hasChunk(at.x(), at.z()) || at.y() < level.getMinSection() || at.y() >= level.getMaxSection()) {
			return new Section(faces, now);
		}
		LevelChunk chunk = level.getChunk(at.x(), at.z());
		LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(at.y()));
		if (section.hasOnlyAir() || !section.maybeHas(GroundRunRenderer::isDrawnMark)) return new Section(faces, now);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				for (int z = 0; z < 16; z++) {
					BlockState state = section.getBlockState(x, y, z);
					if (!isDrawnMark(state)) continue;
					cursor.set(at.minBlockX() + x, at.minBlockY() + y, at.minBlockZ() + z);
					collect(level, cursor.immutable(), state, faces);
				}
			}
		}
		return new Section(faces, now);
	}

	private static boolean isDrawnMark(BlockState state) {
		return state.getBlock() instanceof BloodGougeBlock || state.getBlock() instanceof LuxTraceBlock;
	}

	private static void collect(ClientLevel level, BlockPos cell, BlockState state, List<Face> faces) {
		Kind kind = state.getBlock() instanceof LuxTraceBlock ? Kind.LUX
				: state.getBlock() instanceof LivingVeinBlock ? Kind.VEIN : Kind.GOUGE;
		boolean lit = kind == Kind.LUX || state.getValue(BloodGougeBlock.LIT);
		GroundFunction function = kind == Kind.LUX ? GroundFunction.LIGHT : state.getValue(BloodGougeBlock.FUNCTION);
		// A dormant gouge is its baked groove; a dark run hides itself.
		if (kind == Kind.GOUGE && !lit || lit && !function.drawn()) return;
		int fade = kind == Kind.LUX ? state.getValue(LuxTraceBlock.FADE) : 0;
		for (Direction face : DIRECTIONS) {
			if (!MultifaceBlock.hasFace(state, face)) continue;
			List<Direction> joins = new ArrayList<>(4);
			for (Direction toward : DIRECTIONS) {
				if (toward.getAxis() == face.getAxis()) continue;
				if (joins(level, cell, face, toward, kind)) joins.add(toward);
			}
			faces.add(new Face(cell, face, kind, function, lit, fade, List.copyOf(joins)));
		}
	}

	/** Whether the run on {@code face} of {@code cell} carries on toward {@code toward}: flat, round a corner or a step. */
	private static boolean joins(ClientLevel level, BlockPos cell, Direction face, Direction toward, Kind kind) {
		BlockState here = level.getBlockState(cell);
		if (MultifaceBlock.hasFace(here, toward)) return true;
		BlockPos flat = cell.relative(toward);
		if (sameRun(level.getBlockState(flat), face, kind)) return true;
		BlockPos over = flat.relative(face);
		BlockState overState = level.getBlockState(over);
		return sameRun(overState, face, kind) || sameRun(overState, toward.getOpposite(), kind);
	}

	private static boolean sameRun(BlockState state, Direction face, Kind kind) {
		if (!MultifaceBlock.hasFace(state, face)) return false;
		return kind == Kind.LUX ? state.getBlock() instanceof LuxTraceBlock : state.getBlock() instanceof BloodGougeBlock;
	}

	private static void draw(VertexConsumer consumer, Matrix4f pose, Vec3 camera, Face face, float time) {
		int color;
		float alpha;
		BlockPos cell = face.cell();
		float wave = 0.72F + 0.28F * Mth.sin(time * 0.18F - (cell.getX() + cell.getY() + cell.getZ()) * 0.7F);
		switch (face.kind()) {
			case LUX -> {
				color = LUX;
				alpha = (0.9F - 0.16F * face.fade()) * (0.85F + 0.15F * wave);
			}
			case VEIN -> {
				color = face.lit() ? face.function().color() : TISSUE;
				alpha = (face.lit() ? 0.78F : 0.5F) * wave;
			}
			default -> {
				color = face.function().color();
				alpha = (face.function() == GroundFunction.NONE ? 0.5F : 0.8F) * wave;
			}
		}
		strips(consumer, pose, camera, face, HALF_HALO, color, alpha * 0.32F);
		strips(consumer, pose, camera, face, HALF_CORE, color, alpha);
	}

	/**
	 * The cell's centre square and an arm to each joined edge, {@code half} wide either side of the middle. The centre is
	 * a pixel wider, so the gouged pit under it reads as glowing, not as a gap in the line.
	 */
	private static void strips(VertexConsumer consumer, Matrix4f pose, Vec3 camera, Face face, float half, int color, float alpha) {
		float lo = 0.5F - half;
		float hi = 0.5F + half;
		float pit = 1.0F / 16.0F;
		quad(consumer, pose, camera, face, lo - pit, lo - pit, hi + pit, hi + pit, color, alpha);
		for (Direction toward : face.joins()) {
			float[] arm = arm(face.face(), toward, lo, hi);
			quad(consumer, pose, camera, face, arm[0], arm[1], arm[2], arm[3], color, alpha);
		}
	}

	/** The arm toward {@code toward} in the face's (u, v) square: from the centre square's edge to the cell's edge. */
	private static float[] arm(Direction face, Direction toward, float lo, float hi) {
		Direction.Axis[] axes = planeAxes(face);
		boolean alongU = toward.getAxis() == axes[0];
		boolean positive = toward.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		float from = positive ? hi : 0.0F;
		float to = positive ? 1.0F : lo;
		return alongU ? new float[] {from, lo, to, hi} : new float[] {lo, from, hi, to};
	}

	private static Direction.Axis[] planeAxes(Direction face) {
		return switch (face.getAxis()) {
			case X -> new Direction.Axis[] {Direction.Axis.Y, Direction.Axis.Z};
			case Y -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z};
			case Z -> new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Y};
		};
	}

	/** A rectangle (u0, v0)-(u1, v1) in the face's plane, just off the face it lies on. */
	private static void quad(VertexConsumer consumer, Matrix4f pose, Vec3 camera, Face face, float u0, float v0, float u1,
			float v1, int color, float alpha) {
		Direction side = face.face();
		Direction.Axis[] axes = planeAxes(side);
		float depth = side.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0F - LIFT : LIFT;
		float[][] corners = {{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		int a = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
		BlockPos cell = face.cell();
		for (float[] corner : corners) {
			float[] xyz = new float[3];
			xyz[side.getAxis().ordinal()] = depth;
			xyz[axes[0].ordinal()] = corner[0];
			xyz[axes[1].ordinal()] = corner[1];
			consumer.addVertex(pose, (float) (cell.getX() + xyz[0] - camera.x), (float) (cell.getY() + xyz[1] - camera.y),
					(float) (cell.getZ() + xyz[2] - camera.z)).setColor(r, g, b, a);
		}
	}
}
