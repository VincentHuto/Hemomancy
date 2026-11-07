package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

/**
 * A challenger striking a preserved challenger strikes the ice, not the player (design section 4): two blows from
 * another break it early. The blow is spent on the ice, so it never becomes player-versus-player damage.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class VelorumIceEvents {
	/** How far round the struck player a live Velorum is looked for. */
	private static final double SEARCH = 32.0D;

	private VelorumIceEvents() {
	}

	@SubscribeEvent
	public static void onAttack(AttackEntityEvent event) {
		Player attacker = event.getEntity();
		if (attacker.level().isClientSide || !(event.getTarget() instanceof Player target) || target == attacker) return;
		for (VelorumEntity velorum : target.level().getEntitiesOfClass(VelorumEntity.class,
				target.getBoundingBox().inflate(SEARCH), VelorumEntity::isAlive)) {
			if (!velorum.stillness().preserved(target)) continue;
			velorum.stillness().blow(target);
			event.setCanceled(true);
			return;
		}
	}
}
