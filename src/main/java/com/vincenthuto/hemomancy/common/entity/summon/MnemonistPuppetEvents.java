package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MnemonistPuppetEvents {
	private static final ThreadLocal<Boolean> REPLAYING_DAMAGE = ThreadLocal.withInitial(() -> false);

	private MnemonistPuppetEvents() {
	}

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent.Pre event) {
		if (isReplayingDamage() || event.getNewDamage() <= 0.0F) {
			return;
		}
		LivingEntity damaged = event.getEntity();
		damaged.level().getEntitiesOfClass(MnemonistPuppetEntity.class, damaged.getBoundingBox().inflate(24.0D),
						puppet -> puppet.isAlive() && puppet.getTarget() == damaged)
				.forEach(puppet -> puppet.rememberDamage(damaged, event.getNewDamage()));
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
