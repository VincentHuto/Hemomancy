package com.vincenthuto.hemomancy.common.effect;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.util.FlightArbiter;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/** G-7b: the Elytra effect's flight grant ends with the effect, whether it runs out or is milked in the air. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class ElytraEffectEvents {
	private ElytraEffectEvents() {
	}

	@SubscribeEvent
	public static void onRemoved(MobEffectEvent.Remove event) {
		release(event.getEntity(), event.getEffect());
	}

	@SubscribeEvent
	public static void onExpired(MobEffectEvent.Expired event) {
		if (event.getEffectInstance() != null) release(event.getEntity(), event.getEffectInstance().getEffect());
	}

	private static void release(LivingEntity entity, Holder<MobEffect> effect) {
		if (effect.is(EffectInit.noctifly_agaric) && entity instanceof ServerPlayer player
				&& FlightArbiter.grants(player, FlightArbiter.Source.ELYTRA_EFFECT)) {
			FlightArbiter.revoke(player, FlightArbiter.Source.ELYTRA_EFFECT);
		}
	}
}
