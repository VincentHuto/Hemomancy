package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.network.chat.Component;
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
			HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(unstained -> {
				if (unstained.hasBegunPurification() && !unstained.isPurified()) {
					// Passive purity gain for those on the Unstained path
					float purityGain = 0.01f * (amplifier + 1);
					unstained.addPurity(purityGain);
				} else if (!unstained.hasBegunPurification()) {
					// Hemomancer (blood active, not on Unstained path) — apply blood drain damage
					// ── Skill: Coagulation — chance to block blood-drain tick ──
					double coagChance = com.vincenthuto.hemomancy.common.capability.player.skill.SkillPointHelper.getCoagulationChance(player);
					if (coagChance > 0 && player.level().random.nextDouble() < coagChance) {
						return; // Blocked by Coagulation skill
					}
					HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
						if (volume.isActive()) {
							float drainAmount = 5.0f * (amplifier + 1);
							volume.drain(drainAmount);
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

