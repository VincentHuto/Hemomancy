package com.vincenthuto.hemomancy.common.entity.boss;

import java.util.Collection;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

/**
 * O-1: a boss's bar, held by composition. The boss forwards {@code startSeenByPlayer} / {@code stopSeenByPlayer}
 * to {@link #show} / {@link #hide} and reports its progress each tick; phase changes recolour, rename or hide the
 * bar. The title is a translation key, so every boss bar is translatable.
 */
public final class BossBarBinding {
	private final ServerBossEvent event;

	public BossBarBinding(String titleKey, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
		this.event = new ServerBossEvent(Component.translatable(titleKey), color, overlay);
	}

	public void show(ServerPlayer player) {
		event.addPlayer(player);
	}

	public void hide(ServerPlayer player) {
		event.removePlayer(player);
	}

	public void hideAll() {
		event.removeAllPlayers();
	}

	/** The usual progress: the boss's health fraction. */
	public void trackHealth(LivingEntity boss) {
		event.setProgress(boss.getHealth() / boss.getMaxHealth());
	}

	public void setProgress(float progress) {
		event.setProgress(progress);
	}

	public void setColor(BossEvent.BossBarColor color) {
		event.setColor(color);
	}

	public void setName(Component name) {
		event.setName(name);
	}

	public void setVisible(boolean visible) {
		event.setVisible(visible);
	}

	/** The players who currently see the bar, which is everyone in the encounter. */
	public Collection<ServerPlayer> viewers() {
		return event.getPlayers();
	}

	/** Sends a chat line to everyone who sees the bar. */
	public void broadcast(Component message) {
		for (ServerPlayer player : event.getPlayers()) {
			player.sendSystemMessage(message);
		}
	}
}
