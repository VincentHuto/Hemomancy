package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodlinePetitions.Petition;

import java.util.List;

/** Client-side copy of the petitions waiting on the leader's line, set by {@code PacketSyncBloodlinePetitions}. */
public final class BloodlinePetitionClientData {
	private static List<Petition> petitions = List.of();

	private BloodlinePetitionClientData() {
	}

	public static List<Petition> petitions() {
		return petitions;
	}

	public static void set(List<Petition> waiting) {
		petitions = List.copyOf(waiting);
	}

	public static void clear() {
		petitions = List.of();
	}
}
