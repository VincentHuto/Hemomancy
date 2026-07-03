package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * A beneficial effect that passively replenishes the player's blood volume.
 * Applied by the leeches morphling while it is attached to the player. Each
 * tick, the player's blood volume is filled by a small amount that scales
 * with the amplifier.
 */
public class SanguineSiphonEffect extends MobEffect {

	public SanguineSiphonEffect(MobEffectCategory typeIn, int liquidColorIn) {
		super(typeIn, liquidColorIn);
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return true;
		if (!(entity instanceof Player player)) return true;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (!volume.isActive()) return;
			if (!volume.isFull()) {
				double fillAmount = 1.0 + amplifier * 0.5;
				CirculationIncomeHelper.grant(player, volume, fillAmount,
						CirculationIncomeHelper.IncomeChannel.MORPHLING);
				BloodVolumeEvents.syncVolume((ServerPlayer) player, volume);
			}
		});
		return true;
	}

	@Override
	public void applyInstantenousEffect(Entity source, Entity indirectSource, LivingEntity entityLivingBaseIn,
			int amplifier, double health) {
		super.applyInstantenousEffect(source, indirectSource, entityLivingBaseIn, amplifier, health);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("effect.hemomancy.sanguine_siphon");
	}

	@Override
	public boolean isBeneficial() {
		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return duration % 40 == 0;
	}

}

