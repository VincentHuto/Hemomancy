package com.vincenthuto.hemomancy.client.data;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ActiveBloodStructureFeedClientData {
	private static final Map<String, FeedEntry> ACTIVE_FEEDS = new LinkedHashMap<>();

	private ActiveBloodStructureFeedClientData() {
	}

	public static void upsert(List<BlockPos> positions, float progress, int visibleTicks) {
		if (positions.isEmpty()) {
			return;
		}
		ACTIVE_FEEDS.put(key(positions), new FeedEntry(List.copyOf(sorted(positions)),
				Math.max(0.0f, Math.min(1.0f, progress)), visibleTicks));
	}

	public static void clear(List<BlockPos> positions) {
		if (positions.isEmpty()) {
			ACTIVE_FEEDS.clear();
			return;
		}
		ACTIVE_FEEDS.remove(key(positions));
	}

	public static List<FeedEntry> getActiveFeeds() {
		return new ArrayList<>(ACTIVE_FEEDS.values());
	}

	public static void tick() {
		Iterator<FeedEntry> iterator = ACTIVE_FEEDS.values().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().tick()) {
				iterator.remove();
			}
		}
	}

	private static List<BlockPos> sorted(List<BlockPos> positions) {
		List<BlockPos> sorted = new ArrayList<>(positions);
		sorted.sort(Comparator.comparingLong(BlockPos::asLong));
		return sorted;
	}

	private static String key(List<BlockPos> positions) {
		StringBuilder builder = new StringBuilder();
		for (BlockPos pos : sorted(positions)) {
			if (!builder.isEmpty()) {
				builder.append(',');
			}
			builder.append(pos.asLong());
		}
		return builder.toString();
	}

	public static final class FeedEntry {
		private static final int COMPLETION_LINGER_VISIBLE_TICKS = 20;
		private final List<BlockPos> positions;
		private final float progress;
		private final int initialVisibleTicks;
		private int remainingTicks;

		private FeedEntry(List<BlockPos> positions, float progress, int visibleTicks) {
			this.positions = positions;
			this.progress = progress;
			this.initialVisibleTicks = Math.max(1, visibleTicks);
			this.remainingTicks = this.initialVisibleTicks;
		}

		public List<BlockPos> getPositions() {
			return positions;
		}

		public float getProgress() {
			return progress;
		}

		public float getFinalizeProgress() {
			if (!isCompletionLinger()) {
				return 0.0f;
			}
			return 1.0f - Math.max(0.0f, remainingTicks) / (float) initialVisibleTicks;
		}

		private boolean isCompletionLinger() {
			return progress >= 1.0f && initialVisibleTicks > COMPLETION_LINGER_VISIBLE_TICKS;
		}

		private boolean tick() {
			return --remainingTicks <= 0;
		}
	}
}
