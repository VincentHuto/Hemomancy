package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Map;

/** Shared client/server discovery gates for Harbinger cardinal rites. */
public final class RiteDiscoveryRules {
	private static final Map<ResourceLocation, ResourceLocation> REQUIRED_ENTRIES = Map.of(
			Hemomancy.rloc("cardinal_rite/bloom_of_qliphoth"), LiberEntryDefinitions.QLIPHOTH,
			Hemomancy.rloc("cardinal_rite/ancestral_communion"), LiberEntryDefinitions.ENTITY,
			Hemomancy.rloc("cardinal_rite/apotheos_rite"), LiberEntryDefinitions.TRUTH,
			Hemomancy.rloc("cardinal_rite/eternal_covenant"), LiberEntryDefinitions.TRUTH,
			Hemomancy.rloc("cardinal_rite/sanguine_eclipse"), LiberEntryDefinitions.BLOOD_MOONS);

	private RiteDiscoveryRules() {
	}

	@Nullable
	public static ResourceLocation requiredEntry(ResourceLocation riteId) {
		return REQUIRED_ENTRIES.get(riteId);
	}

	public static boolean isDiscovered(Player player, ResourceLocation riteId) {
		ResourceLocation required = requiredEntry(riteId);
		return required == null || LiberKnowledgeHelper.hasEntry(player, required);
	}
}
