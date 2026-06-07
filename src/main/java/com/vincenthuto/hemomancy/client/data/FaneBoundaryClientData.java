package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.event.worldevent.FaneBoundaryRelation;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFaneBoundaries;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class FaneBoundaryClientData {
	private static final List<Entry> ENTRIES = new ArrayList<>();
	private static ViewMode viewMode = ViewMode.VEILED;

	private FaneBoundaryClientData() {
	}

	public static void setEntries(List<PacketSyncFaneBoundaries.Entry> entries) {
		ENTRIES.clear();
		for (PacketSyncFaneBoundaries.Entry entry : entries) {
			ENTRIES.add(new Entry(entry.heart(), entry.stakes(), entry.radius(), entry.ownerUuid(), entry.relation()));
		}
	}

	public static List<Entry> entries() {
		return List.copyOf(ENTRIES);
	}

	public static void clear() {
		ENTRIES.clear();
	}

	public static ViewMode viewMode() {
		return viewMode;
	}

	public static ViewMode cycleViewMode() {
		viewMode = viewMode.next();
		return viewMode;
	}

	public enum ViewMode {
		VEILED("Fane Sight: Veiled"),
		MUNDANE("Fane Sight: Mundane"),
		HIDDEN("Fane Sight: Hidden"),
		REVEALED("Fane Sight: Revealed");

		private final String label;

		ViewMode(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}

		private ViewMode next() {
			ViewMode[] modes = values();
			return modes[(ordinal() + 1) % modes.length];
		}
	}

	public record Entry(BlockPos heart, List<BlockPos> stakes, float radius, UUID ownerUuid, FaneBoundaryRelation relation) {
	}
}
