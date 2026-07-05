package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class HemolysisEffect extends MobEffect {

	public HemolysisEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);

	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity instanceof Player player) {
			if (player.level().isClientSide) {
				return true;
			}
			HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(unstained -> {
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
						return; // Blocked by Coagulation skill
					}
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
						if (volume.isActive()) {
							BloodFlowLedger.applyDrain((ServerPlayer) player, volume, "hemolysis",
									"Hemolysis", Category.EFFECT, drainAmount, 1, false);
							player.hurt(player.damageSources().magic(), 0.5f * (amplifier + 1));
						}
					});
				}
			});
		}
		return true;
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Hemolysis");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return true;
	}

}

