package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.engram;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramAim;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

/**
 * The ground an engram lies on around one loom: for every block column under it, the height of the surface it drapes
 * over (searched from a block above the loom's floor to three below), with the loom's own frame seen through. The skin is a quad
 * on each surface and a ragged drape hanging over each step down, so it reads as grown over the world rather than
 * floating on it. Rebuilt when the loom is turned or upgraded and re-checked every couple of seconds.
 */
final class EngramSigilMesh {
	/** The skin sits a hair above the surface; lanes and caps a little higher, in that order. */
	static final float SKIN_LIFT = 0.012F;
	private static final float DRAPE_DEPTH = 0.9F;
	private static final int SEARCH_UP = 1, SEARCH_DOWN = 3;
	private static final long RECHECK_TICKS = 40L;
	private static final Map<BlockPos, EngramSigilMesh> CACHE = new HashMap<>();

	private final BlockPos loom;
	private final int turns;
	private final double radius;
	private final Vec3 centre;
	private final int minX, minZ, size;
	/** Surface heights by column, NaN where the engram finds no ground. */
	private final float[] heights;
	private final long builtAt;
	private final int fingerprint;

	private EngramSigilMesh(BlockPos loom, int turns, double radius, Vec3 centre, int minX, int minZ, int size,
			float[] heights, long builtAt, int fingerprint) {
		this.loom = loom;
		this.turns = turns;
		this.radius = radius;
		this.centre = centre;
		this.minX = minX;
		this.minZ = minZ;
		this.size = size;
		this.heights = heights;
		this.builtAt = builtAt;
		this.fingerprint = fingerprint;
	}

	static EngramSigilMesh of(SomaticLoomBlockEntity te) {
		Level level = te.getLevel();
		long now = level.getGameTime();
		EngramSigilMesh cached = CACHE.get(te.getBlockPos());
		boolean same = cached != null && cached.turns == te.sigilTurns() && cached.radius == te.sigilRadius();
		if (same && now - cached.builtAt < RECHECK_TICKS && now >= cached.builtAt) return cached;
		EngramSigilMesh built = build(te, now);
		if (same && built.fingerprint == cached.fingerprint) {
			// Nothing moved: keep the old mesh, but note that it was checked.
			built = new EngramSigilMesh(cached.loom, cached.turns, cached.radius, cached.centre, cached.minX, cached.minZ,
					cached.size, cached.heights, now, cached.fingerprint);
		}
		if (CACHE.size() > 32) CACHE.clear();
		CACHE.put(te.getBlockPos().immutable(), built);
		return built;
	}

	static void clear() {
		CACHE.clear();
	}

	private static EngramSigilMesh build(SomaticLoomBlockEntity te, long now) {
		Level level = te.getLevel();
		Vec3 centre = te.sigilCentre();
		double radius = te.sigilRadius();
		int half = (int) Math.ceil(radius * 1.2D + 1.5D);
		int minX = (int) Math.floor(centre.x) - half, minZ = (int) Math.floor(centre.z) - half;
		int size = half * 2 + 1;
		float[] heights = new float[size * size];
		int floor = te.getBlockPos().getY();
		int fingerprint = 17;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				int x = minX + i, z = minZ + j;
				double dx = x + 0.5D - centre.x, dz = z + 0.5D - centre.z;
				float height = Float.NaN;
				if (dx * dx + dz * dz <= sq(radius * 1.2D + 1.2D)) height = surface(level, te.getBlockPos(), x, z, floor);
				heights[i * size + j] = height;
				fingerprint = fingerprint * 31 + Float.floatToIntBits(height);
			}
		}
		return new EngramSigilMesh(te.getBlockPos().immutable(), te.sigilTurns(), radius, centre, minX, minZ, size, heights,
				now, fingerprint);
	}

	/** The top of the highest walkable surface in a column near the loom's floor, ignoring the loom's own frame. */
	private static float surface(Level level, BlockPos loom, int x, int z, int floor) {
		BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
		for (int y = floor + SEARCH_UP; y >= floor - SEARCH_DOWN; y--) {
			at.set(x, y - 1, z);
			if (isLoom(level, loom, at)) continue;
			BlockState below = level.getBlockState(at);
			VoxelShape shape = below.getCollisionShape(level, at);
			if (shape.isEmpty()) continue;
			at.set(x, y, z);
			BlockState above = level.getBlockState(at);
			if (!isLoom(level, loom, at) && !above.getCollisionShape(level, at).isEmpty() && above.isCollisionShapeFullBlock(level, at))
				continue;
			return (float) (y - 1 + shape.max(Direction.Axis.Y));
		}
		return Float.NaN;
	}

	private static boolean isLoom(Level level, BlockPos loom, BlockPos at) {
		if (at.equals(loom)) return true;
		return level.getBlockEntity(at) instanceof FillerBlockEntity filler && loom.equals(filler.getMainBlockPos());
	}

	private static double sq(double v) {
		return v * v;
	}

	/** The surface under a world point, or the loom's floor where there is none. */
	double heightAt(double x, double z) {
		int i = (int) Math.floor(x) - minX, j = (int) Math.floor(z) - minZ;
		if (i < 0 || j < 0 || i >= size || j >= size) return centre.y;
		float height = heights[i * size + j];
		return Float.isNaN(height) ? centre.y : height;
	}

	/** A point of the engram's frame (blocks from the loom) laid on the ground, in world space. */
	Vec3 ground(double sx, double sz, double lift) {
		double[] world = EngramAim.toWorld(sx, sz, turns);
		double x = centre.x + world[0], z = centre.z + world[1];
		return new Vec3(x, heightAt(x, z) + lift, z);
	}

	/** A point of the engram's frame settled on the centre of the block-face pixel it lies on. */
	Vec3 pixel(double sx, double sz) {
		Vec3 at = ground(sx, sz, 0);
		double[] p = EngramAim.onPixel(at.x, at.y, at.z, 0, 1, 0);
		return new Vec3(p[0], p[1], p[2]);
	}

	/** The skin: a quad on each column's surface and a drape over every step down, relative to the loom's block. */
	void emitSkin(VertexConsumer consumer, Matrix4f matrix) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				float y = heights[i * size + j];
				if (Float.isNaN(y)) continue;
				int x = minX + i, z = minZ + j;
				float top = y + SKIN_LIFT;
				skinVertex(consumer, matrix, x, top, z, 1F);
				skinVertex(consumer, matrix, x, top, z + 1, 1F);
				skinVertex(consumer, matrix, x + 1, top, z + 1, 1F);
				skinVertex(consumer, matrix, x + 1, top, z, 1F);
				drape(consumer, matrix, i, j, -1, 0, x, z, x, z + 1, y);
				drape(consumer, matrix, i, j, 1, 0, x + 1, z + 1, x + 1, z, y);
				drape(consumer, matrix, i, j, 0, -1, x + 1, z, x, z, y);
				drape(consumer, matrix, i, j, 0, 1, x, z + 1, x + 1, z + 1, y);
			}
		}
	}

	/** Where the neighbouring column lies lower (or has no ground), the skin hangs down its edge. */
	private void drape(VertexConsumer consumer, Matrix4f matrix, int i, int j, int di, int dj, float x0, float z0,
			float x1, float z1, float y) {
		int ni = i + di, nj = j + dj;
		float neighbour = ni < 0 || nj < 0 || ni >= size || nj >= size ? Float.NaN : heights[ni * size + nj];
		float drop = Float.isNaN(neighbour) ? DRAPE_DEPTH : Math.min(DRAPE_DEPTH, y - neighbour);
		if (drop < 0.05F) return;
		float outX = di * 0.003F, outZ = dj * 0.003F;
		float top = y + SKIN_LIFT, bottom = y - drop;
		// Red carries how far down the drape a vertex hangs; the shader tears the hem from it.
		skinVertex(consumer, matrix, x0 + outX, top, z0 + outZ, 1F);
		skinVertex(consumer, matrix, x0 + outX, bottom, z0 + outZ, 1F - drop);
		skinVertex(consumer, matrix, x1 + outX, bottom, z1 + outZ, 1F - drop);
		skinVertex(consumer, matrix, x1 + outX, top, z1 + outZ, 1F);
	}

	private void skinVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, float red) {
		double[] sigil = EngramAim.toSigil(x - centre.x, z - centre.z, turns);
		consumer.addVertex(matrix, x - loom.getX(), y - loom.getY(), z - loom.getZ())
				.setUv((float) sigil[0], (float) sigil[1])
				.setColor(red, 1F, 1F, 1F);
	}

	BlockPos loom() {
		return loom;
	}
}
