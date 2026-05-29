package com.vincenthuto.hemomancy.client.sound;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class EndgameBossMusicHandler {
	private static final double MUSIC_RANGE_SQR = 96.0D * 96.0D;
	private static EndgameBossMusic currentMusic;

	private EndgameBossMusicHandler() {
	}

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Player player = mc.player;
		if (level == null || player == null) {
			stopCurrent();
			return;
		}

		BossMusicTarget target = findNearestMusicTarget(level, player);
		if (target == null) {
			stopCurrent();
			return;
		}
		if (currentMusic != null && currentMusic.matches(target.boss(), target.sound())) {
			return;
		}
		stopCurrent();
		currentMusic = new EndgameBossMusic(target.boss(), target.sound(), target.volume());
		mc.getSoundManager().play(currentMusic);
	}

	private static BossMusicTarget findNearestMusicTarget(ClientLevel level, Player player) {
		BossMusicTarget nearest = null;
		double nearestDistance = MUSIC_RANGE_SQR;
		for (Entity entity : level.entitiesForRendering()) {
			BossMusicTarget target = musicTarget(entity);
			if (target == null || !target.boss().isAlive()) {
				continue;
			}
			double distance = player.distanceToSqr(target.boss());
			if (distance <= nearestDistance) {
				nearest = target;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	private static BossMusicTarget musicTarget(Entity entity) {
		if (entity instanceof VesperTheCrownedRefusalEntity crownedRefusal) {
			return new BossMusicTarget(crownedRefusal, SoundInit.ENTITY_VESPER_MUSIC.get(), 0.5F);
		}
		if (entity instanceof VesperTheEveningStarEntity eveningStar) {
			return new BossMusicTarget(eveningStar, SoundInit.ENTITY_VESPER_MUSIC.get(), 0.5F);
		}
		if (entity instanceof MycophantEntity mycophant) {
			return new BossMusicTarget(mycophant, SoundInit.ENTITY_MYCOPHANT_MUSIC.get(), 1.0F);
		}
		return null;
	}

	private static void stopCurrent() {
		if (currentMusic != null) {
			currentMusic.stopSound();
			currentMusic = null;
		}
	}

	private record BossMusicTarget(LivingEntity boss, SoundEvent sound, float volume) {
	}

	private static final class EndgameBossMusic extends AbstractTickableSoundInstance {
		private final LivingEntity boss;
		private final SoundEvent soundEvent;
		private final float baseVolume;
		private boolean done;

		private EndgameBossMusic(LivingEntity boss, SoundEvent soundEvent, float baseVolume) {
			super(soundEvent, SoundSource.RECORDS, RandomSource.create());
			this.boss = boss;
			this.soundEvent = soundEvent;
			this.baseVolume = baseVolume;
			this.looping = true;
			this.delay = 0;
			this.updatePosition();
		}

		private boolean matches(LivingEntity boss, SoundEvent soundEvent) {
			return !this.done && this.boss.getId() == boss.getId() && this.soundEvent == soundEvent;
		}

		private void stopSound() {
			this.done = true;
			this.stop();
		}

		@Override
		public float getVolume() {
			return this.baseVolume;
		}

		@Override
		public void tick() {
			Minecraft mc = Minecraft.getInstance();
			if (this.done || !this.boss.isAlive() || this.boss.isRemoved()
					|| mc.player == null || mc.player.distanceToSqr(this.boss) > MUSIC_RANGE_SQR) {
				this.stopSound();
				return;
			}
			this.updatePosition();
		}

		private void updatePosition() {
			this.x = this.boss.getX();
			this.y = this.boss.getY();
			this.z = this.boss.getZ();
		}
	}
}
