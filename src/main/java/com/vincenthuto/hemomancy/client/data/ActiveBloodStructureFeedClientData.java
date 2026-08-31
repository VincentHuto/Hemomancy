package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.event.BloodStructureFeedRules;
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
		upsert(0L, positions, progress, visibleTicks);
	}

	public static void upsert(long channelId, List<BlockPos> positions, float progress, int visibleTicks) {
		if (positions.isEmpty()) {
			return;
		}
		String key = key(channelId, positions);
		FeedEntry previous = ACTIVE_FEEDS.get(key);
		ACTIVE_FEEDS.put(key, new FeedEntry(channelId, List.copyOf(sorted(positions)),
				Math.max(0.0f, Math.min(1.0f, progress)), visibleTicks,
				previous == null ? 0 : previous.engulfmentTicks));
	}

	public static void clear(List<BlockPos> positions) {
		clear(0L, positions);
	}

	public static void clear(long channelId, List<BlockPos> positions) {
		if (channelId != 0L) {
			ACTIVE_FEEDS.remove(channelKey(channelId));
			return;
		}
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

	private static String key(long channelId, List<BlockPos> positions) {
		return channelId == 0L ? key(positions) : channelKey(channelId);
	}

	private static String channelKey(long channelId) {
		return "channel:" + channelId;
	}

	public static final class FeedEntry {
		private static final int COMPLETION_LINGER_VISIBLE_TICKS = 20;
		private static final int ENGULFMENT_TICKS = 20;
		private final long channelId;
		private final List<BlockPos> positions;
		private final float progress;
		private final int initialVisibleTicks;
		private int remainingTicks;
		private int engulfmentTicks;

		private FeedEntry(long channelId, List<BlockPos> positions, float progress, int visibleTicks,
				int engulfmentTicks) {
			this.channelId = channelId;
			this.positions = positions;
			this.progress = progress;
			this.initialVisibleTicks = Math.max(1, visibleTicks);
			this.remainingTicks = this.initialVisibleTicks;
			this.engulfmentTicks = engulfmentTicks;
		}

		public List<BlockPos> getPositions() {
			return positions;
		}

		public long getChannelId() {
			return channelId;
		}

		public float getProgress() {
			return progress;
		}

		public float getEngulfmentProgress(float partialTick) {
			return Math.min(1.0F, (engulfmentTicks + partialTick) / ENGULFMENT_TICKS);
		}

		public float getFinalizeProgress() {
			if (!isCompletionLinger()) {
				return 0.0f;
			}
			return 1.0f - Math.max(0.0f, remainingTicks) / (float) initialVisibleTicks;
		}

		public float getFadeAlpha(float partialTick) {
			if (isCompletionLinger()) {
				return 1.0f;
			}
			return BloodStructureFeedRules.resumeFadeAlpha(remainingTicks, initialVisibleTicks, partialTick);
		}

		private boolean isCompletionLinger() {
			return progress >= 1.0f && initialVisibleTicks > COMPLETION_LINGER_VISIBLE_TICKS;
		}

		private boolean tick() {
			engulfmentTicks++;
			return --remainingTicks <= 0;
		}
	}
}
