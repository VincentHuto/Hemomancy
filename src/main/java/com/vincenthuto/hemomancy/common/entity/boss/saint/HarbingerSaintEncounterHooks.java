package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;

public final class HarbingerSaintEncounterHooks {

	private HarbingerSaintEncounterHooks() {
	}

	public static boolean spawnSaintBoss(ServerLevel level, BlockPos pos, Player sourcePlayer) {
		return spawnSaintBoss(level, pos, sourcePlayer, EnumSaintType.HEMORATH);
	}

	public static boolean spawnSaintBoss(ServerLevel level, BlockPos pos, Player sourcePlayer, EnumSaintType saintType) {
		// Reset blood debt at encounter start so pre-fight damage doesn't carry over.
		HemoCapabilityAccess.getBloodVolume(sourcePlayer)
				.ifPresent(v -> v.resetBloodDebt());

		Mob boss = createBoss(level, saintType);
		if (boss == null) return false;

		boss.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
				level.random.nextFloat() * 360.0F, 0.0F);
		boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null);
		boss.setPersistenceRequired();
		boss.setCustomName(Component.literal("Saint " + saintType.getDisplayName()));
		boss.setCustomNameVisible(true);
		if (sourcePlayer instanceof ServerPlayer serverPlayer) {
			boss.setTarget(serverPlayer);
		}

		return level.addFreshEntity(boss);
	}

	private static Mob createBoss(ServerLevel level, EnumSaintType saintType) {
		return switch (saintType) {
			case HEMORATH -> EntityInit.hemorath.get().create(level);
			case SERAPHAE -> EntityInit.seraphae.get().create(level);
			case PUTRICIEL -> EntityInit.putriciel.get().create(level);
			case VELORUM -> EntityInit.velorum.get().create(level);
		};
	}
}
