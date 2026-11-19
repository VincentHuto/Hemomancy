package com.vincenthuto.hemomancy.client.render.world.circus;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.client.data.RingmasterFinaleClientData;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberDomePortal;
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.ChamberSkyThemeRegistry;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleStage;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

/**
 * The Ringmaster finale's merge (owner, 2026-11-18 fourth pass): his real Chamber render projected onto the copied
 * tent's own block faces, so the tent is made of his Chamber. Every exposed face of the pavilion's blocks (canvas,
 * walls, galleries, seating, poles, rigging and floor) outside the ring is a mask: {@link ChamberDomePortal#project}
 * renders his Chamber sky ({@link ChamberSkyThemeRegistry#RINGMASTER_VEIL}: the shared Chamber layers through their
 * own {@code ChamberThemeEffects}, camera-centred as in the Chamber, under his skybox and nebula) into its own target, marks the stencil where these faces are the nearest thing seen, and lays the sky
 * over the screen there at {@link FinaleRules#veilStrength}: nearly solid at the opening, thinning as he is worn down
 * until the tent's stripes show through. The ring (a column round the carousel) stays plainly real, and blocks that
 * give light keep their own look as his stage lights. Open air and the sky past canvas openings are never marked.
 *
 * <p>The faces follow {@code HeartNodeSpreadRenderer} and {@code FaneGroundRenderer}: found once per chunk section in
 * the stage's bounds and kept, found again when the client hears of a block change in the section ({@link #invalidate}),
 * at most {@link #BUILDS_PER_FRAME} a frame, from the crossing on so they are ready when the veil rises.
 */
public final class FinaleVeilRenderer {
	private static final int BUILDS_PER_FRAME = 8;
	/** Just off the surface; the render type's view offset does the rest. */
	private static final float FACE_OFFSET = 0.004F;
	private static final Direction[] DIRECTIONS = Direction.values();

	private static final Long2ObjectOpenHashMap<Section> SECTIONS = new Long2ObjectOpenHashMap<>();
	@Nullable
	private static BlockPos stage;
	private static int buildsThisFrame;

	private FinaleVeilRenderer() {
	}

	/** The faces in one chunk section: four corners each (x, y, z from the stage's centre). */
	private static final class Section {
		private final float[] corners;
		private boolean stale;

		Section(float[] corners) {
			this.corners = corners;
		}

		int faces() {
			return corners.length / 12;
		}
	}

	/**
	 * Finds the faces (always, while a finale is on) and projects his Chamber onto them at the veil's strength. Call
	 * where the tent's terrain and the entities in front of it are in depth.
	 */
	public static void render(RenderLevelStageEvent event, float partial) {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || !RingmasterFinaleClientData.isActive()) {
			if (stage != null) clear();
			return;
		}
		BlockPos centre = BlockPos.containing(RingmasterFinaleClientData.copyCentre());
		if (!centre.equals(stage)) {
			SECTIONS.clear();
			stage = centre;
		}
		buildsThisFrame = 0;
		int reach = FinaleStage.reach() + 1;
		int minX = SectionPos.blockToSectionCoord(centre.getX() - reach), maxX = SectionPos.blockToSectionCoord(centre.getX() + reach);
		int minY = SectionPos.blockToSectionCoord(centre.getY() - FinaleStage.depth());
		int maxY = SectionPos.blockToSectionCoord(centre.getY() + FinaleStage.height());
		int minZ = SectionPos.blockToSectionCoord(centre.getZ() - reach), maxZ = SectionPos.blockToSectionCoord(centre.getZ() + reach);
		for (int sx = minX; sx <= maxX; sx++) {
			for (int sz = minZ; sz <= maxZ; sz++) {
				if (!level.getChunkSource().hasChunk(sx, sz)) continue;
				for (int sy = minY; sy <= maxY; sy++) {
					long key = SectionPos.asLong(sx, sy, sz);
					Section section = SECTIONS.get(key);
					if ((section == null || section.stale) && buildsThisFrame < BUILDS_PER_FRAME) {
						buildsThisFrame++;
						SECTIONS.put(key, build(level, centre, reach, sx, sy, sz));
					}
				}
			}
		}
		float strength = RingmasterFinaleClientData.veilStrength(partial);
		if (strength <= 0.001F || SECTIONS.isEmpty()) return;
		Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
		ChamberDomePortal.project(event, ChamberSkyThemeRegistry.RINGMASTER_VEIL, partial,
				strength, pose -> drawMask(pose, camera, centre));
	}

	/** Every kept face, camera-relative, as plain positions: the projector's mask. */
	private static void drawMask(Matrix4f pose, Vec3 camera, BlockPos centre) {
		float cx = (float) (camera.x - centre.getX()), cy = (float) (camera.y - centre.getY());
		float cz = (float) (camera.z - centre.getZ());
		BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
		for (Section section : SECTIONS.values()) {
			float[] corners = section.corners;
			for (int at = 0; at < corners.length; at += 3)
				buffer.addVertex(pose, corners[at] - cx, corners[at + 1] - cy, corners[at + 2] - cz);
		}
		MeshData mesh = buffer.build();
		if (mesh != null) BufferUploader.drawWithShader(mesh);
	}

	/** A block changed where the client can see it: find the faces of its section and its neighbours' again. */
	public static void invalidate(BlockPos pos) {
		if (stage == null) return;
		markStale(SectionPos.asLong(pos));
		for (Direction direction : DIRECTIONS) markStale(SectionPos.asLong(pos.relative(direction)));
	}

	private static void markStale(long key) {
		Section section = SECTIONS.get(key);
		if (section != null) section.stale = true;
	}

	public static void clear() {
		SECTIONS.clear();
		stage = null;
	}

	/** Faces kept for the stage now drawn; for the review client. */
	public static int keptFaces() {
		int faces = 0;
		for (Section section : SECTIONS.values()) faces += section.faces();
		return faces;
	}

	/**
	 * Every exposed face of the stage's blocks in one section: each box of a block's shape, on each side not flush
	 * against a solid neighbour, outside the ring's column. Blocks that give light are left as they are.
	 */
	private static Section build(ClientLevel level, BlockPos centre, int reach, int sx, int sy, int sz) {
		FloatArrayList corners = new FloatArrayList();
		int x0 = SectionPos.sectionToBlockCoord(sx), y0 = SectionPos.sectionToBlockCoord(sy);
		int z0 = SectionPos.sectionToBlockCoord(sz);
		int index = level.getSectionIndexFromSectionY(sy);
		if (index < 0 || index >= level.getSectionsCount() || level.getChunk(sx, sz).getSection(index).hasOnlyAir())
			return new Section(new float[0]);
		int minY = centre.getY() - FinaleStage.depth(), maxY = centre.getY() + FinaleStage.height();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos beside = new BlockPos.MutableBlockPos();
		for (int x = Math.max(x0, centre.getX() - reach); x <= Math.min(x0 + 15, centre.getX() + reach); x++) {
			for (int z = Math.max(z0, centre.getZ() - reach); z <= Math.min(z0 + 15, centre.getZ() + reach); z++) {
				for (int y = Math.max(y0, minY); y <= Math.min(y0 + 15, maxY); y++) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.isAir() || state.getLightEmission(level, pos) > 0 || !state.getFluidState().isEmpty()) continue;
					boolean full = state.isSolidRender(level, pos);
					VoxelShape shape = full ? null : state.getShape(level, pos);
					if (!full && shape.isEmpty()) continue;
					for (AABB box : full ? java.util.List.of(new AABB(0, 0, 0, 1, 1, 1)) : shape.toAabbs()) {
						for (Direction side : DIRECTIONS) {
							if (onBoundary(box, side)) {
								beside.setWithOffset(pos, side);
								if (level.getBlockState(beside).isSolidRender(level, beside)) continue;
							}
							face(corners, pos, box, side, centre);
						}
					}
				}
			}
		}
		return new Section(corners.toFloatArray());
	}

	private static boolean onBoundary(AABB box, Direction side) {
		return switch (side) {
			case DOWN -> box.minY <= 0.0D;
			case UP -> box.maxY >= 1.0D;
			case NORTH -> box.minZ <= 0.0D;
			case SOUTH -> box.maxZ >= 1.0D;
			case WEST -> box.minX <= 0.0D;
			case EAST -> box.maxX >= 1.0D;
		};
	}

	/** Adds one face of {@code box} (block-local) at {@code pos}, unless its middle lies in the ring's column. */
	private static void face(FloatArrayList corners, BlockPos pos, AABB box, Direction side, BlockPos centre) {
		double bx = pos.getX(), by = pos.getY(), bz = pos.getZ();
		double minX = bx + box.minX, minY = by + box.minY, minZ = bz + box.minZ;
		double maxX = bx + box.maxX, maxY = by + box.maxY, maxZ = bz + box.maxZ;
		double cx = (minX + maxX) * 0.5D + side.getStepX() * (maxX - minX) * 0.5D;
		double cy = (minY + maxY) * 0.5D + side.getStepY() * (maxY - minY) * 0.5D;
		double cz = (minZ + maxZ) * 0.5D + side.getStepZ() * (maxZ - minZ) * 0.5D;
		double dx = cx - (centre.getX() + 0.5D), dz = cz - (centre.getZ() + 0.5D);
		if (dx * dx + dz * dz < FinaleRules.RING_RADIUS * FinaleRules.RING_RADIUS
				&& cy < centre.getY() + FinaleRules.RING_HEIGHT) return;
		double ox = side.getStepX() * FACE_OFFSET, oy = side.getStepY() * FACE_OFFSET, oz = side.getStepZ() * FACE_OFFSET;
		double[][] quad = switch (side) {
			case DOWN -> new double[][]{{minX, minY, minZ}, {maxX, minY, minZ}, {maxX, minY, maxZ}, {minX, minY, maxZ}};
			case UP -> new double[][]{{minX, maxY, minZ}, {minX, maxY, maxZ}, {maxX, maxY, maxZ}, {maxX, maxY, minZ}};
			case NORTH -> new double[][]{{minX, minY, minZ}, {minX, maxY, minZ}, {maxX, maxY, minZ}, {maxX, minY, minZ}};
			case SOUTH -> new double[][]{{minX, minY, maxZ}, {maxX, minY, maxZ}, {maxX, maxY, maxZ}, {minX, maxY, maxZ}};
			case WEST -> new double[][]{{minX, minY, minZ}, {minX, minY, maxZ}, {minX, maxY, maxZ}, {minX, maxY, minZ}};
			case EAST -> new double[][]{{maxX, minY, minZ}, {maxX, maxY, minZ}, {maxX, maxY, maxZ}, {maxX, minY, maxZ}};
		};
		for (double[] corner : quad) {
			// Kept relative to the stage's centre, so the floats stay precise far from the world's origin.
			corners.add((float) (corner[0] + ox - centre.getX()));
			corners.add((float) (corner[1] + oy - centre.getY()));
			corners.add((float) (corner[2] + oz - centre.getZ()));
		}
	}
}
