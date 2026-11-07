package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolDamageSource;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * The kit's one incoming-damage subscriber, after {@code SchoolCombatEvents.prepare}: stages the hit's
 * {@link SchoolHitContext} on a kit Saint so its {@code actuallyHurt} can classify the hit as a school cast. A
 * legacy Saint is left alone.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class SaintHitEvents {
	private SaintHitEvents() {
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void stage(LivingIncomingDamageEvent event) {
		if (event.getEntity().level().isClientSide || !(event.getEntity() instanceof SaintTrialEntity saint)
				|| saint.kit() == null) return;
		SchoolHitContext context = event.getSource() instanceof SchoolDamageSource source ? source.context()
				: SchoolDamage.current();
		saint.stageHit(context);
	}
}
