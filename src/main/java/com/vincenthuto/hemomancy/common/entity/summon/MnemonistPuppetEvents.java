package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MnemonistPuppetEvents {
	private static final double MEMORY_RANGE = 24.0D;
	private static final ThreadLocal<Boolean> REPLAYING_DAMAGE = ThreadLocal.withInitial(() -> false);

	private MnemonistPuppetEvents() {
	}

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent.Pre event) {
		if (isReplayingDamage() || event.getNewDamage() <= 0.0F) {
			return;
		}
		LivingEntity damaged = event.getEntity();
		if (damaged.level().isClientSide) {
			return;
		}
		// Usually zero or a few puppets; scanning them beats a world search on every damage event.
		for (MnemonistPuppetEntity puppet : MnemonistPuppetEntity.loaded()) {
			if (puppet.level() == damaged.level() && puppet.isAlive() && puppet.getTarget() == damaged
					&& puppet.distanceToSqr(damaged) <= MEMORY_RANGE * MEMORY_RANGE) {
				puppet.rememberDamage(damaged, event.getNewDamage(), event.getSource().getEntity());
			}
		}
	}

	public static boolean isReplayingDamage() {
		return REPLAYING_DAMAGE.get();
	}

	public static void withReplayDamage(Runnable action) {
		boolean previous = REPLAYING_DAMAGE.get();
		REPLAYING_DAMAGE.set(true);
		try {
			action.run();
		} finally {
			REPLAYING_DAMAGE.set(previous);
		}
	}
}
