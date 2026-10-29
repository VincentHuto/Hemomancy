package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossScenes;
import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class HarbingerSaintEncounterHooks {

	private HarbingerSaintEncounterHooks() {
	}

	public static boolean spawnSaintBoss(ServerLevel level, BlockPos pos, Player sourcePlayer) {
		return spawnSaintBoss(level, pos, sourcePlayer, EnumSaintType.HEMORATH);
	}

	public static boolean spawnSaintBoss(ServerLevel level, BlockPos pos, Player sourcePlayer, EnumSaintType saintType) {
		SaintTrialEntity boss = createBoss(level, saintType);
		if (boss == null) return false;
		// remember the tomb before the encounter clears it, so every ending can close the Saint back into it
		BlockState tomb = level.getBlockState(pos);
		if (tomb.getBlock() instanceof SaintSarcophagusBlock) boss.bindTomb(pos, tomb);

		// at the middle of the floor the sarcophagus stood on (it is cleared right after): the Saint rises out of it,
		// facing out of the foot end toward whoever came to the bowl
		double[] centre = tomb.getBlock() instanceof SaintSarcophagusBlock
				? SaintSarcophagusBlock.tombCentre(tomb.getValue(SaintSarcophagusBlock.FACING), 0) : new double[]{0.5, 0, 0.5};
		float facing = tomb.getBlock() instanceof SaintSarcophagusBlock
				? tomb.getValue(SaintSarcophagusBlock.FACING).toYRot() : level.random.nextFloat() * 360.0F;
		boss.moveTo(pos.getX() + centre[0], pos.getY(), pos.getZ() + centre[2], facing, 0.0F);
		boss.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.TRIGGERED, null);
		if (level.getBlockEntity(pos) instanceof com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity record) {
			boss.setAwakening(SaintEnrageRules.effectiveMode(record.doorMode().name(),
					com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents.isApotheos(sourcePlayer)), record.skippedChapels(),
					record.doorMode().name());
		}
		boss.setPersistenceRequired();
		boss.setCustomName(Component.translatable("message.hemomancy.saint.saint_s", saintType.getDisplayName()));
		boss.setCustomNameVisible(true);
		if (sourcePlayer instanceof ServerPlayer serverPlayer) {
			boss.setTarget(serverPlayer);
		}

		boss.beginRise();
		if (!level.addFreshEntity(boss)) return false;
		BossScenes.play(boss, BossPresentationIds.SCENE_SAINT_RISE);
		// Reset blood debt only after the encounter exists so a failed spawn cannot erase player state.
		HemoCapabilityAccess.getBloodVolume(sourcePlayer).resetBloodDebt();
		return true;
	}

	private static SaintTrialEntity createBoss(ServerLevel level, EnumSaintType saintType) {
		return switch (saintType) {
			case HEMORATH -> EntityInit.hemorath.get().create(level);
			case SERAPHAE -> EntityInit.seraphae.get().create(level);
			case PUTRICIEL -> EntityInit.putriciel.get().create(level);
			case VELORUM -> EntityInit.velorum.get().create(level);
		};
	}
}
