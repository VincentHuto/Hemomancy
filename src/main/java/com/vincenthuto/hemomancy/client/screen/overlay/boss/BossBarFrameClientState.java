package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.resources.ResourceLocation;

/**
 * Which frame draws each boss bar this client sees, and each bar's animation: the damage chip (the lost stretch stays
 * lit a moment, then drains) and the reveal (the frame unfurls when a bar appears, and again when a hidden bar
 * returns). Bindings outlive a hidden bar, because a bar shown again by {@code setVisible} gets no new binding.
 */
public final class BossBarFrameClientState {
	/** How long the lost stretch holds before it drains, in ticks. */
	public static final float CHIP_HOLD = 16.0F;
	/** How fast the chip drains, in bar fractions per tick. */
	public static final float CHIP_DRAIN = 0.02F;
	public static final float REVEAL_TICKS = 20.0F;
	/** A bar this long unseen has gone for good. */
	public static final int FORGET_TICKS = 6000;
	/** A bar painted this long ago (in ticks) still counts as painted this frame. */
	public static final float PAINT_FRESH = 1.5F;

	private static final Map<UUID, Entry> ENTRIES = new HashMap<>();
	private static int clock;

	private BossBarFrameClientState() {
	}

	public static void bind(UUID bar, ResourceLocation frame) {
		Entry entry = ENTRIES.get(bar);
		if (entry == null || !entry.frame.equals(frame)) ENTRIES.put(bar, new Entry(frame, clock));
	}

	@Nullable
	public static Entry entry(UUID bar) {
		return ENTRIES.get(bar);
	}

	/**
	 * The bar drawn by {@code frame} that the frame renderer painted this frame (within {@link #PAINT_FRESH} of
	 * {@code now}), or null: the Saint meter layer reads its origin to draw under it.
	 */
	@Nullable
	public static Entry painted(ResourceLocation frame, float now) {
		Entry found = null;
		for (Entry entry : ENTRIES.values()) {
			if (!entry.frame.equals(frame) || entry.paintedAt < now - PAINT_FRESH) continue;
			if (found == null || entry.paintedY < found.paintedY) found = entry;
		}
		return found;
	}

	public static int clock() {
		return clock;
	}

	public static void tick() {
		clock++;
		if (clock % 200 == 0) prune(clock);
	}

	static void prune(float now) {
		ENTRIES.values().removeIf(entry -> entry.lastSeen < now - FORGET_TICKS);
	}

	public static void clear() {
		ENTRIES.clear();
		clock = 0;
	}

	public static final class Entry {
		private final ResourceLocation frame;
		private float revealStart;
		private float lastSeen;
		private float chip = -1.0F;
		private float last;
		private float holdUntil;
		private float lastStep;
		private int paintedX;
		private int paintedY;
		private float paintedAt = -1.0E9F;

		Entry(ResourceLocation frame, float boundAt) {
			this.frame = frame;
			this.revealStart = boundAt;
			this.lastSeen = boundAt;
		}

		public ResourceLocation frame() {
			return frame;
		}

		/** Marks the bar drawn at {@code now}; after a gap (it was hidden) the reveal plays again. */
		public void seen(float now) {
			if (now - lastSeen > 2.0F) revealStart = now;
			lastSeen = now;
		}

		/** Records where the bar was painted at {@code now}: the Saint meter draws under it. */
		public void painted(int x, int y, float now) {
			paintedX = x;
			paintedY = y;
			paintedAt = now;
		}

		public int paintedX() {
			return paintedX;
		}

		public int paintedY() {
			return paintedY;
		}

		/** 0 as the bar appears, 1 once it has fully unfurled. */
		public float reveal(float now) {
			return HemoEasing.clamp01((now - revealStart) / REVEAL_TICKS);
		}

		/** Where the lit chip ends: at or above {@code progress}, holding after each loss, then draining down to it. */
		public float chip(float progress, float now) {
			if (chip < 0.0F) {
				chip = last = progress;
				lastStep = now;
				return chip;
			}
			if (progress < last - 1.0E-5F) holdUntil = now + CHIP_HOLD;
			last = progress;
			if (progress >= chip) chip = progress;
			else if (now > holdUntil) chip = Math.max(progress, chip - CHIP_DRAIN * (now - Math.max(lastStep, holdUntil)));
			lastStep = now;
			return chip;
		}
	}
}
