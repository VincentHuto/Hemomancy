package com.vincenthuto.hemomancy.common.rite.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * Server-side event handler for active Still Waters zones established by the
 * Rite of Still Waters.
 * <p>
 * Players inside a Still Waters zone take 30% less magic damage, partially
 * countering Sanguine Dominion's bleed and other blood-magic threats.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class StillWatersEvents {

	/** Fraction of magic damage reduced for players inside a Still Waters zone. */
	private static final float MAGIC_DAMAGE_REDUCTION = 0.30f;


	// â”€â”€ Hurt event: reduce magic damage inside Still Waters zones â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

	@SubscribeEvent
	public static void onLivingHurt(LivingDamageEvent.Pre event) {
		// Only affect players
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;

		// Only reduce magic-type damage
		if (!event.getSource().is(DamageTypes.MAGIC)) return;

		ServerLevel sLevel = (ServerLevel) player.level();
		ServerLevel overworld = sLevel.getServer().overworld();
		StillWatersSavedData data = StillWatersSavedData.get(overworld);

		if (data.getEntries().isEmpty()) return;

		BlockPos pos = player.blockPosition();
		String dimension = sLevel.dimension().location().toString();
		long tick = sLevel.getGameTime();

		if (data.isInZone(pos, dimension, tick)) {
			event.setNewDamage(event.getNewDamage() * (1.0f - MAGIC_DAMAGE_REDUCTION));
		}
	}
}
