package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.EnumOrgan;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
			contributions.add(MaxBloodContribution.bonus("capacity", "Capacity", Category.SKILL, capacityBonus));
		}

		HemoCapabilityAccess.getVisceralOrgans(player).ifPresent(organs -> {
			if (organs.isExtracted(EnumOrgan.SPLEEN)) {
				double spleenBonus = Math.max(0, organs.getOrganLevel(EnumOrgan.SPLEEN)) * 1000.0D;
				if (spleenBonus > 0.0D) {
					contributions.add(MaxBloodContribution.bonus("spleen", "Spleen", Category.ORGAN, spleenBonus));
				}
			}
		});

		if (player.getPersistentData().getBoolean(ETERNAL_COVENANT_TAG)) {
			contributions.add(MaxBloodContribution.bonus("eternal_covenant", "Eternal Covenant",
					Category.RITE, ETERNAL_COVENANT_BONUS));
		}

		HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
			for (ResourceLocation scarId : scars.getActiveCerebralScars()) {
				ScarDefinition definition = ScarInit.getByName(scarId.toString());
				if (definition != null) {
					addScarContribution(contributions, scarId.toString(), titleCase(scarId.getPath()), definition);
				}
			}
			ItemStack fungalScar = scars.getFungalScar();
			if (fungalScar.getItem() instanceof ItemScar scar) {
				ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(fungalScar.getItem());
				addScarContribution(contributions, itemId.toString(), titleCase(itemId.getPath()),
						scar.getScarDefinition());
			}
		});

		contributions.sort(Comparator
				.comparing((MaxBloodContribution contribution) -> contribution.category().ordinal())
				.thenComparing(MaxBloodContribution::label)
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

	private static String titleCase(String value) {
		String[] parts = value.toLowerCase(Locale.ROOT).split("_");
		StringBuilder builder = new StringBuilder();
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			if (!builder.isEmpty()) {
				builder.append(' ');
			}
			builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
		}
		return builder.toString();
	}
}
