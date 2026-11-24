package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData.SigilSegment;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilGougeStages;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * When this client first saw each sigil link and each awakening, in game ticks, so the
 * gouge renderer can stage a cut-in or a completion from the moment it arrived. Anything
 * that came with the rite's first sight (a join or relog mid-rite) is stamped as settled,
 * so a late joiner sees the finished look with no replay. A rite that leaves the sync keeps its
 * stamps a while as retired, so its ending ghost can drain the cuts it had rather than snap them
 * to a fresh look. Fed by {@link ActiveRiteClientData} on every applied rite sync.
 */
public final class SigilGougeClientState {
	/** A stamp meaning "here before this client looked". */
	private static final long SETTLED_STAMP = Long.MIN_VALUE;
	/** Stamps keyed by rite centre, then by sigil key. */
	private static final Map<BlockPos, Map<String, SigilStamps>> STAMPS = new HashMap<>();
	/** Longer than any rite ending, so a ghost never outlives the stamps it reads. */
	static final long RETIRED_KEEP_TICKS = 200L;
	/** Stamps of rites that left the sync, keyed by rite centre, kept for their ending ghosts. */
	private static final Map<BlockPos, Retired> RETIRED = new HashMap<>();

	private SigilGougeClientState() {
	}

	/** Called once per applied rite sync. firstSightOfRite is true when this client had no entry for the rite before. */
	public static void observe(BlockPos riteCenter, List<SigilSegment> segments,
			long gameTime, boolean firstSightOfRite) {
		pruneRetired(gameTime);
		if (segments.isEmpty()) {
			STAMPS.remove(riteCenter);
			return;
		}
		long stamp = firstSightOfRite ? SETTLED_STAMP : gameTime;
		Map<String, SigilStamps> previous = STAMPS.getOrDefault(riteCenter, Map.of());
		Map<String, SigilStamps> next = new LinkedHashMap<>();
		for (SigilSegment segment : segments) {
			SigilStamps sigil = next.computeIfAbsent(segment.sigilKey(), key -> new SigilStamps());
			SigilStamps old = previous.get(segment.sigilKey());
			Long oldLink = old == null ? null : old.links.get(segment.linkIndex());
			sigil.links.putIfAbsent(segment.linkIndex(), oldLink == null ? stamp : oldLink);
			if (segment.awakened() && sigil.awakened == null) {
				sigil.awakened = old != null && old.awakened != null ? old.awakened : stamp;
			}
		}
		STAMPS.put(riteCenter, next);
	}

	/** Ticks since the link was first seen, or a settled age if it arrived with the rite's first sight (or was never seen). */
	public static float linkAge(BlockPos riteCenter, String sigilKey, int linkIndex, float gameTime) {
		SigilStamps sigil = sigil(riteCenter, sigilKey);
		Long stamp = sigil == null ? null : sigil.links.get(linkIndex);
		return stamp == null ? IchorianSigilGougeStages.settledAge(gameTime) : age(stamp, gameTime);
	}

	/** Ticks since the sigil was first seen awake; negative if it is not awake; a settled age if first seen awake with the rite. */
	public static float awakeAge(BlockPos riteCenter, String sigilKey, float gameTime) {
		SigilStamps sigil = sigil(riteCenter, sigilKey);
		if (sigil == null || sigil.awakened == null) return -1.0F;
		return age(sigil.awakened, gameTime);
	}

	/** Drops a rite's stamps outright: the centre now holds a different rite. */
	public static void forget(BlockPos riteCenter) {
		STAMPS.remove(riteCenter);
		RETIRED.remove(riteCenter);
	}

	/**
	 * The rite left the sync: its stamps stay readable for {@link #RETIRED_KEEP_TICKS} so its ending ghost drains
	 * what it had. A rite seen again at the centre stamps afresh over them.
	 */
	public static void retire(BlockPos riteCenter, long gameTime) {
		Map<String, SigilStamps> sigils = STAMPS.remove(riteCenter);
		if (sigils != null) RETIRED.put(riteCenter, new Retired(sigils, gameTime));
	}

	public static void clear() {
		STAMPS.clear();
		RETIRED.clear();
	}

	private static void pruneRetired(long gameTime) {
		RETIRED.values().removeIf(retired -> gameTime < retired.at() || gameTime - retired.at() > RETIRED_KEEP_TICKS);
	}

	private static SigilStamps sigil(BlockPos riteCenter, String sigilKey) {
		Map<String, SigilStamps> sigils = STAMPS.get(riteCenter);
		if (sigils == null) {
			Retired retired = RETIRED.get(riteCenter);
			sigils = retired == null ? null : retired.sigils();
		}
		return sigils == null ? null : sigils.get(sigilKey);
	}

	private static float age(long stamp, float gameTime) {
		if (stamp == SETTLED_STAMP) return IchorianSigilGougeStages.settledAge(gameTime);
		return (float) Math.max(0.0D, (double) gameTime - stamp);
	}

	private record Retired(Map<String, SigilStamps> sigils, long at) {
	}

	private static final class SigilStamps {
		private final Map<Integer, Long> links = new HashMap<>();
		/** When the sigil was first seen awake, or null while it is not. */
		private Long awakened;
	}
}
