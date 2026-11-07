package com.vincenthuto.hemomancy.common.entity.boss;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a footprint stands this tick: the body ({@code origin}), the locked or tracked aim point, the target's feet,
 * the floor the height bands are measured from, the move's own points (discs, path), excluded cells (regions), anchors
 * (tethers) and the arena boxes. The client rebuilds the same frame from synced data.
 */
public record FootprintFrame(Vec3 origin, Vec3 aim, Vec3 target, double floorY, List<Vec3> points,
		LongSet cells, List<Vec3> anchors, AABB bossFloor, AABB chamber) {
	public FootprintFrame {
		points = List.copyOf(points);
		anchors = List.copyOf(anchors);
		cells = LongSets.unmodifiable(new LongOpenHashSet(cells));
	}

	/** A frame with no points, cells or anchors and arena boxes round the origin. */
	public static FootprintFrame simple(Vec3 origin, Vec3 aim, Vec3 target, double floorY) {
		AABB around = new AABB(origin, origin).inflate(16.0D);
		return new FootprintFrame(origin, aim, target, floorY, List.of(), LongSets.EMPTY_SET, List.of(), around, around);
	}

	public Vec3 anchor(BossFootprint.Anchor anchor) {
		return switch (anchor) {
			case ORIGIN -> origin;
			case AIM -> aim;
			case TARGET -> target;
		};
	}

	public FootprintFrame withOrigin(Vec3 value) {
		return new FootprintFrame(value, aim, target, floorY, points, cells, anchors, bossFloor, chamber);
	}

	public FootprintFrame withAim(Vec3 value) {
		return new FootprintFrame(origin, value, target, floorY, points, cells, anchors, bossFloor, chamber);
	}

	public FootprintFrame withTarget(Vec3 value) {
		return new FootprintFrame(origin, aim, value, floorY, points, cells, anchors, bossFloor, chamber);
	}

	public FootprintFrame withPoints(List<Vec3> value) {
		return new FootprintFrame(origin, aim, target, floorY, value, cells, anchors, bossFloor, chamber);
	}

	public FootprintFrame withCells(LongSet value) {
		return new FootprintFrame(origin, aim, target, floorY, points, value, anchors, bossFloor, chamber);
	}

	public FootprintFrame withAnchors(List<Vec3> value) {
		return new FootprintFrame(origin, aim, target, floorY, points, cells, value, bossFloor, chamber);
	}

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.put("origin", vec(origin));
		tag.put("aim", vec(aim));
		tag.put("target", vec(target));
		tag.putDouble("floor", floorY);
		tag.put("points", vecs(points));
		tag.putLongArray("cells", cells.toLongArray());
		tag.put("anchors", vecs(anchors));
		tag.put("boss_floor", box(bossFloor));
		tag.put("chamber", box(chamber));
		return tag;
	}

	public static FootprintFrame load(CompoundTag tag) {
		LongSet cells = new LongOpenHashSet(tag.getLongArray("cells"));
		return new FootprintFrame(vec(tag.getList("origin", Tag.TAG_DOUBLE)), vec(tag.getList("aim", Tag.TAG_DOUBLE)),
				vec(tag.getList("target", Tag.TAG_DOUBLE)), tag.getDouble("floor"), vecs(tag.getList("points", Tag.TAG_LIST)),
				cells, vecs(tag.getList("anchors", Tag.TAG_LIST)), box(tag.getList("boss_floor", Tag.TAG_DOUBLE)),
				box(tag.getList("chamber", Tag.TAG_DOUBLE)));
	}

	static ListTag vec(Vec3 value) {
		ListTag list = new ListTag();
		list.add(DoubleTag.valueOf(value.x));
		list.add(DoubleTag.valueOf(value.y));
		list.add(DoubleTag.valueOf(value.z));
		return list;
	}

	static Vec3 vec(ListTag list) {
		if (list.size() < 3) return Vec3.ZERO;
		return new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
	}

	private static ListTag vecs(List<Vec3> values) {
		ListTag list = new ListTag();
		for (Vec3 value : values) list.add(vec(value));
		return list;
	}

	private static List<Vec3> vecs(ListTag list) {
		List<Vec3> values = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) values.add(vec(list.getList(i)));
		return values;
	}

	private static ListTag box(AABB box) {
		ListTag list = new ListTag();
		for (double value : new double[]{box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ}) {
			list.add(DoubleTag.valueOf(value));
		}
		return list;
	}

	private static AABB box(ListTag list) {
		if (list.size() < 6) return new AABB(0, 0, 0, 0, 0, 0);
		return new AABB(list.getDouble(0), list.getDouble(1), list.getDouble(2), list.getDouble(3), list.getDouble(4),
				list.getDouble(5));
	}
}
