package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * A beneficial effect that passively replenishes the player's blood volume.
 * Applied by the Deadman's Purse Morphling while it is attached to the player. Each
 * tick, the player's blood volume is filled by a small amount that scales
 * with the amplifier.
 */
public class SanguineSiphonEffect extends MobEffect {
	private final String displayKey;

	public SanguineSiphonEffect(MobEffectCategory typeIn, int liquidColorIn) {
		this(typeIn, liquidColorIn, "effect.hemomancy.sanguine_siphon");
	}

	public SanguineSiphonEffect(MobEffectCategory typeIn, int liquidColorIn, String displayKey) {
		super(typeIn, liquidColorIn);
		this.displayKey = displayKey;
	}

	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity == null || entity.level().isClientSide) return true;
		if (!(entity instanceof Player player)) return true;

		HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
			if (player instanceof ServerPlayer serverPlayer) {
				double fillAmount = 1.0 + amplifier * 0.5;
				BloodFlowLedger.applyCirculationIncome(serverPlayer, volume, "sanguine_siphon",
						"Sanguine Siphon", Category.EFFECT, fillAmount, 40,
						CirculationIncomeHelper.IncomeChannel.MORPHLING);
			}
		});
		return true;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(displayKey);
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

