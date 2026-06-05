package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.event.worldevent.SanctumBoundaryRelation;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SanctumBoundaryClientData {
	private static final List<Entry> ENTRIES = new ArrayList<>();

	private SanctumBoundaryClientData() {
	}

	public static void setEntries(List<com.vincenthuto.hemomancy.common.network.capa.PacketSyncSanctumBoundaries.Entry> entries) {
		ENTRIES.clear();
		for (com.vincenthuto.hemomancy.common.network.capa.PacketSyncSanctumBoundaries.Entry entry : entries) {
			ENTRIES.add(new Entry(entry.center(), entry.radius(), entry.ownerUuid(), entry.relation()));
		}
	}

	public static List<Entry> entries() {
		return List.copyOf(ENTRIES);
	}

	public static void clear() {
		ENTRIES.clear();
	}

	public record Entry(BlockPos center, float radius, UUID ownerUuid, SanctumBoundaryRelation relation) {
	}
}
