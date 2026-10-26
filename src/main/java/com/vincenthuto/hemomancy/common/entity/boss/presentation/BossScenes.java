package com.vincenthuto.hemomancy.common.entity.boss.presentation;

import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.boss.BossScenePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Plays a boss scene for the players around a boss. The scene is presentation only: the boss's own server timing
 * (its intro window, its emergence) decides when it can act and be hurt, whatever the client shows or skips.
 */
public final class BossScenes {
	/** Everyone within this range of the boss sees its scene, which covers every arena's bar viewers. */
	public static final double RANGE = 64.0D;

	private BossScenes() {
	}

	public static void play(Entity boss, ResourceLocation scene) {
		play(boss, scene, 0);
	}

	public static void play(Entity boss, ResourceLocation scene, int offset) {
		if (!(boss.level() instanceof ServerLevel level)) return;
		PacketHandler.sendNear(level, boss.getX(), boss.getY(), boss.getZ(), RANGE,
				new BossScenePacket(scene, boss.getId(), offset));
	}

	public static void playTo(ServerPlayer player, Entity boss, ResourceLocation scene, int offset) {
		PacketHandler.sendToPlayer(player, new BossScenePacket(scene, boss.getId(), offset));
	}
}
