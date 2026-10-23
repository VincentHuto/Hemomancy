package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class HemolysisEffect extends MobEffect {
	private static final BloodAccounting.Source SOURCE = BloodAccounting.Source.of("hemolysis", Category.EFFECT, false, false);

	public HemolysisEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity instanceof Player player) {
			if (player.level().isClientSide) {
				return true;
			}
			var unstained = HemoCapabilityAccess.getUnstainedProgress(player);
			if (unstained.hasBegunPurification() && !unstained.isPurified()) {
				// Passive purity gain for those on the Unstained path
				float purityGain = 0.01f * (amplifier + 1);
				unstained.addPurity(purityGain);
			} else if (!unstained.hasBegunPurification()) {
				// Hemomancer (blood active, not on Unstained path) — apply blood drain damage
				// ── Skill: Coagulation — chance to block blood-drain tick ──
				double coagChance = SkillPointHelper.getCoagulationChance(player);
				float drainAmount = 5.0f * (amplifier + 1);
				if (coagChance > 0 && player.level().random.nextDouble() < coagChance) {
					BloodFlowLedger.recordApplied((ServerPlayer) player, "hemolysis",
							"Hemolysis", Category.EFFECT, -drainAmount, 0.0D, 1, false,
							"Blocked by Coagulation");
					return true; // Blocked by Coagulation skill
				}
				var volume = HemoCapabilityAccess.getBloodVolume(player);
				if (volume.isActive()) {
					BloodAccounting.payUpkeep((ServerPlayer) player, drainAmount, SOURCE, 1, false);
					player.hurt(player.damageSources().magic(), 0.5f * (amplifier + 1));
				}
			}
		}
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

}

