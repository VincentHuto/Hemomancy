package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.EnumOrgan;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MaxBloodLedger {
	public static final double BASE_MAX_BLOOD = 5000.0D;
	public static final double ETERNAL_COVENANT_BONUS = 500.0D;
	public static final String ETERNAL_COVENANT_TAG = "hemomancy:eternal_covenant_used";

	private MaxBloodLedger() {
	}

	public static MaxBloodSnapshot apply(ServerPlayer player, IBloodVolume volume) {
		MaxBloodSnapshot snapshot = collect(player, volume);
		if (player == null || volume == null || !volume.isActive()) {
			return snapshot;
		}
		double beforeMax = volume.getMaxBloodVolume();
		double beforeBlood = volume.getBloodVolume();
		double targetMax = snapshot.totalMax();
		boolean changed = Math.abs(beforeMax - targetMax) > 0.000001D;
		if (changed) {
			volume.setMaxBloodVolume(targetMax);
		}
		if (volume.getBloodVolume() > targetMax) {
			volume.setBloodVolume(targetMax);
			changed = true;
		}
		if (changed || Math.abs(beforeBlood - volume.getBloodVolume()) > 0.000001D) {
			BloodVolumeEvents.syncVolume(player, volume);
		}
		return snapshot;
	}

	public static MaxBloodSnapshot collect(ServerPlayer player, IBloodVolume volume) {
		if (player == null || volume == null || !volume.isActive()) {
			return MaxBloodSnapshot.EMPTY;
		}
		List<MaxBloodContribution> contributions = new ArrayList<>();

		double capacityBonus = SkillPointHelper.getCapacityBonus(player);
		if (capacityBonus > 0.0D) {
			contributions.add(MaxBloodContribution.bonus("capacity", "message.hemomancy.max_blood_ledger.capacity", Category.SKILL, capacityBonus));
		}

		var organs = HemoCapabilityAccess.getVisceralOrgans(player);
		if (organs.isExtracted(EnumOrgan.SPLEEN)) {
			double spleenBonus = Math.max(0, organs.getOrganLevel(EnumOrgan.SPLEEN)) * 1000.0D;
			if (spleenBonus > 0.0D) {
				contributions.add(MaxBloodContribution.bonus("spleen", "message.hemomancy.max_blood_ledger.spleen", Category.ORGAN, spleenBonus));
			}
		}

		if (player.getPersistentData().getBoolean(ETERNAL_COVENANT_TAG)) {
			contributions.add(MaxBloodContribution.bonus("eternal_covenant", "message.hemomancy.max_blood_ledger.eternal_covenant",
					Category.RITE, ETERNAL_COVENANT_BONUS));
		}

		var scars = HemoCapabilityAccess.getScarState(player);
		for (ResourceLocation scarId : scars.getActiveCerebralScars()) {
			ScarDefinition definition = ScarInit.getByName(scarId.toString());
			if (definition != null) {
				addScarContribution(contributions, scarId.toString(), Util.makeDescriptionId("item", scarId),
						definition);
			}
		}
		ItemStack fungalScar = scars.getFungalScar();
		if (fungalScar.getItem() instanceof ItemScar scar) {
			ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(fungalScar.getItem());
			addScarContribution(contributions, itemId.toString(), fungalScar.getItem().getDescriptionId(),
					scar.getScarDefinition());
		}

		contributions.sort(Comparator
				.comparing((MaxBloodContribution contribution) -> contribution.category().ordinal())
				.thenComparing((MaxBloodContribution contribution) ->
						Language.getInstance().getOrDefault(contribution.label()))
				.thenComparing(MaxBloodContribution::sourceId));
		return MaxBloodRules.snapshot(BASE_MAX_BLOOD, contributions);
	}

	private static void addScarContribution(List<MaxBloodContribution> contributions, String sourceId, String label,
			ScarDefinition definition) {
		if (definition == null) {
			return;
		}
		double amount = definition.getMaxBloodModifier();
		if (amount > 0.0D) {
			contributions.add(MaxBloodContribution.bonus(sourceId, label, Category.SCAR, amount));
		} else if (amount < 0.0D) {
			contributions.add(MaxBloodContribution.penalty(sourceId, label, Category.SCAR, -amount));
		}
	}
}
