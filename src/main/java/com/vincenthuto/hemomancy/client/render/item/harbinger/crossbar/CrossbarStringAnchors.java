package com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The string knots of each held Crossbar as drawn this frame, as world-axis offsets from the holder. Threads only
 * use knots stamped with the current frame, so first person, a sheathed bar or an off-screen holder falls back to
 * the hand estimate instead of a stale position.
 */
public final class CrossbarStringAnchors {
	private record Key(UUID holder, UUID crossbar) {
	}

	private record Entry(Entity holder, Vec3[] offsets, long frame) {
	}

	private static final Map<Key, Entry> ANCHORS = new HashMap<>();

	private CrossbarStringAnchors() {
	}

	static void record(Entity holder, UUID crossbar, Vec3[] offsets) {
		long frame = CrossbarHolderTracker.frame();
		if (ANCHORS.size() > 32) ANCHORS.values().removeIf(entry -> entry.frame() < frame - 20);
		ANCHORS.put(new Key(holder.getUUID(), crossbar), new Entry(holder, offsets, frame));
	}

	/** Knot {@code index} of the Crossbar {@code crossbar} held by {@code holder}, or null if not drawn this frame. */
	public static Vec3 knot(Entity holder, UUID crossbar, int index, float partialTick) {
		Entry entry = ANCHORS.get(new Key(holder.getUUID(), crossbar));
		if (entry == null || entry.frame() != CrossbarHolderTracker.frame() || index < 0
				|| index >= entry.offsets().length) {
			return null;
		}
		Vec3 offset = entry.offsets()[index];
		return new Vec3(Mth.lerp(partialTick, holder.xOld, holder.getX()) + offset.x,
				Mth.lerp(partialTick, holder.yOld, holder.getY()) + offset.y,
				Mth.lerp(partialTick, holder.zOld, holder.getZ()) + offset.z);
	}
}
