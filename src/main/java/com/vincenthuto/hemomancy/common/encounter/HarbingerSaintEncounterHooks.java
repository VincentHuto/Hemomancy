package com.vincenthuto.hemomancy.common.encounter;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;

public final class HarbingerSaintEncounterHooks {

	private HarbingerSaintEncounterHooks() {
	}

	public static boolean spawnSaintBoss(ServerLevel level, BlockPos pos, Player sourcePlayer) {
		// Reset blood debt at encounter start so pre-fight damage doesn't carry over.
		sourcePlayer.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.ifPresent(v -> v.resetBloodDebt());

		// Intentional reuse: there is no dedicated Harbinger Saint entity type yet, so
		// the encounter hook upgrades a Harbinger Vicar into a named boss-style spawn.
		var boss = EntityInit.harbinger_vicar.get().create(level);
		if (boss == null) return false;

		boss.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
		boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null, null);
		boss.setPersistenceRequired();
		boss.setCustomName(Component.translatable("entity.hemomancy.harbinger_saint"));
		boss.setCustomNameVisible(true);
		boss.setInvulnerable(false);
		if (sourcePlayer instanceof ServerPlayer serverPlayer) {
			boss.setTarget(serverPlayer);
		}

		return level.addFreshEntity(boss);
	}
}
