package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeTier;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * What a trade may never pay (docs/npc_trade/00 "Trades never grant progression"): a manipulation memory
 * ({@code memory_*}, {@code crude_memory_*}) or {@code hematic_memory}, a station upgrade item from
 * {@link StationUpgradeCatalog}, or anything tagged {@link #NEVER_SOLD}. The loader fails a file that gives one; the
 * tag is checked once tags are bound.
 */
public final class NpcTradeGiveRules {
	public static final ResourceLocation NEVER_SOLD = Hemomancy.rloc("trade/never_sold");
	/** Items whose names start like a memory but teach nothing (Memory Thread is a weaving material). */
	static final Set<String> MEMORY_NAMED_MATERIALS = Set.of("memory_thread");

	private NpcTradeGiveRules() {}

	public static boolean isManipulationMemory(ResourceLocation item) {
		if (!item.getNamespace().equals(Hemomancy.MOD_ID)) return false;
		String path = item.getPath();
		if (path.equals("hematic_memory")) return true;
		if (MEMORY_NAMED_MATERIALS.contains(path)) return false;
		return path.startsWith("memory_") || path.startsWith("crude_memory_");
	}

	public static boolean isStationUpgrade(ResourceLocation item) {
		return stationUpgradeItems().contains(item);
	}

	public static boolean isBannedById(ResourceLocation item) {
		return isManipulationMemory(item) || isStationUpgrade(item);
	}

	static Set<ResourceLocation> stationUpgradeItems() {
		return StationUpgradeCatalog.all().stream().map(StationUpgradeTier::upgradeItem).collect(Collectors.toSet());
	}
}
