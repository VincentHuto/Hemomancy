package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomData.BloomEntry;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * Client-side cache for persistent Qliphoth Bloom locations.
 * Updated by PacketSyncQliphothBlooms from the server.
 * Used by QliphothBloomRenderer to draw the tree and pulsing rings.
 */
public class QliphothBloomClientData {

	private static List<BloomEntry> activeBlooms = Collections.emptyList();
	private static Set<BlockPos> activeCenters = Collections.emptySet();

	public static void set(List<BloomEntry> blooms) {
		List<BloomEntry> bloomCopy = Collections.unmodifiableList(new ArrayList<>(blooms));
		Set<BlockPos> centers = new HashSet<>(bloomCopy.size());
		for (BloomEntry bloom : bloomCopy) {
			centers.add(bloom.getCenter());
		}
		activeBlooms = bloomCopy;
		activeCenters = Collections.unmodifiableSet(centers);
	}

	public static List<BloomEntry> getActiveBlooms() {
		return activeBlooms;
	}

	public static void clear() {
		activeBlooms = Collections.emptyList();
		activeCenters = Collections.emptySet();
	}

	public static boolean containsCenter(BlockPos pos) {
		return activeCenters.contains(pos);
	}
}
