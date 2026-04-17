package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;

/**
 * Spawns the Seraphae (The Bound Radiance) boss encounter.
 * Similar pattern to {@link HarbingerSaintEncounterHooks} but for the
 * containment-based fight.
 */
public final class SeraphaeEncounterHooks {

	private SeraphaeEncounterHooks() {
	}

	/**
	 * Spawns Seraphae at the given position and sets her target to the source player.
	 *
	 * @return true if the entity was successfully added to the world
	 */
	public static boolean spawnSeraphae(ServerLevel level, BlockPos pos, Player sourcePlayer) {
		SeraphaeEntity boss = EntityInit.seraphae.get().create(level);
		if (boss == null) return false;

		boss.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
				level.random.nextFloat() * 360.0F, 0.0F);
		boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null, null);
		boss.setPersistenceRequired();
		boss.setCustomName(Component.translatable("entity.hemomancy.seraphae"));
		boss.setCustomNameVisible(true);
		if (sourcePlayer instanceof ServerPlayer serverPlayer) {
			boss.setTarget(serverPlayer);
		}

		return level.addFreshEntity(boss);
	}
}
