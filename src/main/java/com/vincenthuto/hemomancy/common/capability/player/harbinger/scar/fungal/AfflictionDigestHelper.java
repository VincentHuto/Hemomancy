package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.CirculationIncomeHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class AfflictionDigestHelper {
	private static final int DIGEST_EXTRA_TICKS_PER_SECOND = 40;
	private static final double BLOOD_PER_EFFECT_SECOND = 2.0D;
	private static final float HEAL_PER_SECOND_CAP = 0.5F;

	private AfflictionDigestHelper() {
	}

	public static int stepDurationTicks(int durationTicks) {
		return Math.max(0, durationTicks - DIGEST_EXTRA_TICKS_PER_SECOND);
	}

	public static double bloodFeedForEffectSeconds(int effectCount) {
		return Math.max(0, effectCount) * BLOOD_PER_EFFECT_SECOND;
	}

	public static float healForEffectSeconds(int effectCount) {
		return effectCount > 0 ? HEAL_PER_SECOND_CAP : 0.0F;
	}

	public static boolean isDigestible(Holder<MobEffect> effect) {
		return effect == MobEffects.POISON
				|| effect == MobEffects.WITHER
				|| effect == MobEffects.HUNGER
				|| effect == EffectInit.blood_loss;
	}

	public static void tick(Player player, ItemStack fungalSlot) {
		if (!(player instanceof ServerPlayer serverPlayer)
				|| player.tickCount % 20 != 0
				|| !fungalSlot.is(ItemInit.putrivora_resolvens.get())) {
			return;
		}

		List<MobEffectInstance> targets = new ArrayList<>();
		for (MobEffectInstance effect : serverPlayer.getActiveEffects()) {
			if (isDigestible(effect.getEffect())) {
				targets.add(effect);
			}
		}
		if (targets.isEmpty()) {
			return;
		}

		for (MobEffectInstance effect : targets) {
			int remaining = stepDurationTicks(effect.getDuration());
			if (remaining <= 0) {
				serverPlayer.removeEffect(effect.getEffect());
			} else {
				serverPlayer.addEffect(new MobEffectInstance(effect.getEffect(), remaining,
						effect.getAmplifier(), effect.isAmbient(), true, true));
			}
		}

		serverPlayer.heal(healForEffectSeconds(targets.size()));
		HemoCapabilityAccess.getBloodVolume(serverPlayer).ifPresent(volume -> {
			double granted = CirculationIncomeHelper.grant(serverPlayer, volume,
					bloodFeedForEffectSeconds(targets.size()), CirculationIncomeHelper.IncomeChannel.SCAR);
			syncBlood(serverPlayer, volume, granted);
		});
	}

	private static void syncBlood(ServerPlayer player, IBloodVolume volume, double granted) {
		if (granted > 0.0D) {
			PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
		}
	}
}
