package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.routing.BloodRoutingMode;
import net.minecraft.core.BlockPos;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public final class SutureLinkClientData {
	private static final long TIMEOUT_TICKS = 20L * 60L * 5L;
	private static List<Entry> entries = List.of();
	private static long lastSyncTick;

	private SutureLinkClientData() {
	}

	public static void setEntries(List<Entry> nextEntries) {
		entries = List.copyOf(nextEntries);
		lastSyncTick = currentTick();
	}

	public static List<Entry> entries() {
		if (currentTick() - lastSyncTick > TIMEOUT_TICKS) {
			entries = List.of();
		}
		return new ArrayList<>(entries);
	}

	private static long currentTick() {
		return Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
	}

	public record Entry(BlockPos pos, BloodRoutingMode mode, boolean bloodlineEnabled) {
	}
}
