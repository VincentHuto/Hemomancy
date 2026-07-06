package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.effect.MnemonicPotionRules;
import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.item.harbinger.CheapBloodInfusionHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.SporiticThuribleResonanceState;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.config.HemoMnAConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public final class ManipulationCostLedger {
	private ManipulationCostLedger() {
	}

	public static ManipulationCostSnapshot collect(Player player, BloodManipulation manipulation) {
		return collect(player, manipulation, purityCostMultiplier(player));
	}

	public static ManipulationCostSnapshot collect(Player player, BloodManipulation manipulation,
			double purityCostMultiplier) {
		if (player == null || manipulation == null || manipulation == BloodManipulation.BLANK) {
			return ManipulationCostSnapshot.EMPTY;
		}

		List<ManipulationCostContribution> contributions = new ArrayList<>();
		addIfNotNeutral(contributions, "efficiency", "Efficiency", Category.SKILL,
				SkillPointHelper.getEfficiencyMultiplier(player));
		addIfNotNeutral(contributions, "purity", "Purity", Category.TENDENCY, purityCostMultiplier,
				purityCostMultiplier <= 0.0D ? "Manipulation blocked" : "");

		double levelCostMultiplier = HemoCapabilityAccess.getKnownManipulations(player)
				.map(k -> k.getManipLevel(manipulation))
				.map(ManipLevel::getCostMultiplier)
				.orElse(1.0D);
		addIfNotNeutral(contributions, "manipulation_mastery", "Manipulation Mastery", Category.MASTERY,
				levelCostMultiplier);

		if (matchesStrongestTendency(player, manipulation.getTend())) {
			double dynamicUse = SkillPointHelper.getDynamicUseMultiplier(player);
			if (dynamicUse > 0.0D) {
				addIfNotNeutral(contributions, "dynamic_use", "Dynamic Use", Category.SKILL,
						1.0D / dynamicUse);
			}
		}

		if (ModList.get().isLoaded("mna") && player.hasEffect(EffectInit.arcane_resonance)) {
			try {
				double reduction = HemoMnAConfig.ARCANE_RESONANCE_BLOOD_REDUCTION.get();
				addIfNotNeutral(contributions, "arcane_resonance", "Arcane Resonance", Category.EFFECT,
						1.0D - reduction);
			} catch (Exception ignored) {
			}
		}

		if (player instanceof ServerPlayer serverPlayer && QliphothBloomEvents.isInQliphothBloom(serverPlayer)) {
			addIfNotNeutral(contributions, "qliphoth_bloom", "Qliphoth Bloom", Category.RITE, 0.75D);
		}

		long pomeExpiry = HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(d -> d.getPomeEmpowermentExpiry()).orElse(0L);
		if (player.level().getGameTime() < pomeExpiry) {
			addIfNotNeutral(contributions, "qliphoth_pome_empowerment", "Qliphoth Pome Empowerment",
					Category.RITE,
					QliphothPomeRules.empowermentCostMultiplier(
							SkillPointHelper.getQliphothGestationLevel(player)));
		}

		if (BloodMoonEvents.isBloodMoonActive(player.level())) {
			addIfNotNeutral(contributions, "blood_moon", "Blood Moon", Category.WORLD, 0.75D);
		}

		var degreeData = HemoCapabilityAccess.getInitiatoryDegree(player);
		int pomesConsumed = degreeData
				.map(d -> d.getTotalPomesConsumed())
				.orElse(0);
		boolean blockedByPomes = pomesConsumed >= 9 && degreeData
				.map(d -> d.getDegreeNumber() < 8)
				.orElse(true);
		if (blockedByPomes) {
			addIfNotNeutral(contributions, "pome_corruption_block", "Pome Corruption", Category.RITE,
					1.0D, "Manipulation blocked until Apotheos");
		} else if (pomesConsumed > 0) {
			addIfNotNeutral(contributions, "pome_corruption", "Pome Corruption", Category.RITE,
					1.0D + pomesConsumed * 0.12D);
		}

		addIfNotNeutral(contributions, "blood_drunkenness", "Blood Drunkenness", Category.EFFECT,
				CheapBloodInfusionHelper.getManipulationCostMultiplier(player));
		addIfNotNeutral(contributions, "sporitic_thurible", "Sporitic Thurible", Category.TOOL,
				SporiticThuribleResonanceState.getCostMultiplier(player, manipulation.getTend()));
		addIfNotNeutral(contributions, "mnemonic_screams", "Mnemonic Screams", Category.EFFECT,
				MnemonicPotionRules.manipulationCostMultiplier(player.hasEffect(EffectInit.mnemonic_screams)));

		return ManipulationCostRules.snapshot(manipulation.getCost(), contributions);
	}

	public static double purityCostMultiplier(Player player) {
		if (player == null) {
			return 1.0D;
		}
		var optUnstained = HemoCapabilityAccess.getUnstainedProgress(player);
		if (optUnstained.isEmpty()) {
			return 1.0D;
		}
		var unstained = optUnstained.orElseThrow(IllegalStateException::new);
		if (!unstained.hasBegunPurification()) {
			return 1.0D;
		}
		EnumPurityStage stage = EnumPurityStage.byPurity(unstained.getPurity());
		if (stage == EnumPurityStage.PURIFIED) {
			return 0.0D;
		}
		if (stage != EnumPurityStage.CORRUPTED) {
			return 1.0D + stage.getBloodMagicPenalty();
		}
		return 1.0D;
	}

	private static boolean matchesStrongestTendency(Player player, EnumBloodTendency manipulationTendency) {
		IBloodTendency tendency = HemoCapabilityAccess.getBloodTendency(player).orElse(null);
		if (tendency == null || manipulationTendency == null) {
			return false;
		}
		EnumBloodTendency strongest = null;
		float highestVal = 0.0F;
		for (EnumBloodTendency candidate : EnumBloodTendency.values()) {
			float val = tendency.getAlignmentByTendency(candidate);
			if (val > highestVal) {
				highestVal = val;
				strongest = candidate;
			}
		}
		return strongest == manipulationTendency;
	}

	private static void addIfNotNeutral(List<ManipulationCostContribution> contributions, String sourceId,
			String label, Category category, double multiplier) {
		addIfNotNeutral(contributions, sourceId, label, category, multiplier, "");
	}

	private static void addIfNotNeutral(List<ManipulationCostContribution> contributions, String sourceId,
			String label, Category category, double multiplier, String condition) {
		double safeMultiplier = Double.isFinite(multiplier) ? Math.max(0.0D, multiplier) : 1.0D;
		if (Math.abs(safeMultiplier - 1.0D) > 0.000001D || (condition != null && !condition.isEmpty())) {
			contributions.add(ManipulationCostContribution.multiplier(sourceId, label, category,
					safeMultiplier, condition));
		}
	}
}
