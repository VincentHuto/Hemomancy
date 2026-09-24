package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Cutting the nerve network rouses the Borers that maintain it. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class MyelinBorerAgitationEvents {
	private static final double PROVOKE_RADIUS = 16.0D;

	private MyelinBorerAgitationEvents() {
	}

	@SubscribeEvent
	public static void onBreak(BlockEvent.BreakEvent event) {
		if (!(event.getLevel() instanceof ServerLevel level)
				|| !event.getState().is(MyelinBorerEntity.CRAWLABLE)) {
			return;
		}
		provokeNearby(level, new AABB(event.getPos()), event.getPlayer());
	}

	@SubscribeEvent
	public static void onDeath(LivingDeathEvent event) {
		if (event.getEntity() instanceof MyelinBorerEntity borer && borer.level() instanceof ServerLevel level) {
			provokeNearby(level, borer.getBoundingBox(), attacker(event.getSource()));
		}
	}

	private static Player attacker(DamageSource source) {
		return source.getEntity() instanceof Player player ? player : null;
	}

	public static void provokeNearby(ServerLevel level, AABB center, Player attacker) {
		AABB area = center.inflate(PROVOKE_RADIUS);
		for (MyelinBorerEntity borer : level.getEntitiesOfClass(MyelinBorerEntity.class, area)) {
			if (borer.position().distanceToSqr(center.getCenter()) > PROVOKE_RADIUS * PROVOKE_RADIUS) {
				continue;
			}
			borer.provoke();
			if (attacker != null && !attacker.isCreative() && !attacker.isSpectator()) {
				borer.setTarget(attacker);
			}
		}
	}
}
