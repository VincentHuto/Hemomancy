package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;

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
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity instanceof Player player) {
			player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(unstained -> {
				if (unstained.hasBegunPurification() && !unstained.isPurified()) {
					// Passive purity gain for those on the Unstained path
					float purityGain = 0.01f * (amplifier + 1);
					unstained.addPurity(purityGain);
				} else {
					// Hemomancer (blood active, not on Unstained path) — apply blood drain damage
					player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
						if (volume.isActive()) {
							float drainAmount = 5.0f * (amplifier + 1);
							volume.drain(drainAmount);
							player.hurt(player.damageSources().magic(), 0.5f * (amplifier + 1));
						}
					});
				}
			});
		}
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
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return true;
	}

}
