package com.vincenthuto.hemomancy.common.rite.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Still Waters zones from the Rite of Still Waters. Players inside one take
 * 30% less magic damage, partially countering Sanguine Dominion's bleed and
 * other blood-magic threats. Works in whichever dimension the zone was cast.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class StillWatersEvents {

	/** Fraction of magic damage reduced for players inside a Still Waters zone. */
	private static final float MAGIC_DAMAGE_REDUCTION = 0.30f;

	@SubscribeEvent
	public static void onLivingHurt(LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (!(player.level() instanceof ServerLevel sLevel)) return;
		if (!event.getSource().is(DamageTypes.MAGIC)) return;

		StillWatersSavedData data = StillWatersSavedData.get(sLevel.getServer().overworld());
		if (data.contains(player.blockPosition(), sLevel.dimension(), sLevel.getGameTime())) {
			event.setNewDamage(event.getNewDamage() * (1.0f - MAGIC_DAMAGE_REDUCTION));
		}
	}
}
