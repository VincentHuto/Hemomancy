package com.vincenthuto.hemomancy.common.util;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class CrimsonFireHelper {
	private CrimsonFireHelper() {
	}

	public static void igniteCrimson(Entity target, float seconds) {
		if (target == null) {
			return;
		}
		target.igniteForSeconds(seconds);
		markCrimson(target, Math.max(1, Math.round(seconds * 20.0F)));
	}

	public static void markCrimsonForRemainingFire(Entity target, int minimumTicks) {
		if (target == null) {
			return;
		}
		markCrimson(target, Math.max(minimumTicks, target.getRemainingFireTicks()));
	}

	public static void markCrimson(Entity target, int durationTicks) {
		if (target instanceof LivingEntity living && !living.level().isClientSide()) {
			PacketHandler.sendCrimsonFireVisual(living, durationTicks);
		}
	}
}
