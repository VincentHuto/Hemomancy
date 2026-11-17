package com.vincenthuto.hemomancy.common.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What a periodic broadcast last sent each player, so it goes out again only when it changes (FX-14). A new player
 * object (a login or a respawn) and a move to another level always count as a change, so they get a full send.
 * Each holder registers {@link #clear} and {@link #forget} with {@code TransientServerStateEvents}.
 */
public final class LastSent<T> {
	private record Mark(ServerPlayer player, ResourceKey<Level> level, Object value) {
	}

	private final Map<UUID, Mark> marks = new ConcurrentHashMap<>();

	/** Records {@code value} as sent to {@code player} and returns whether it differs from what they last had. */
	public boolean changed(ServerPlayer player, T value) {
		Mark previous = marks.put(player.getUUID(), new Mark(player, player.level().dimension(), value));
		return previous == null || previous.player() != player || !previous.level().equals(player.level().dimension())
				|| !Objects.equals(previous.value(), value);
	}

	public void forget(Player player) {
		marks.remove(player.getUUID());
	}

	public void clear() {
		marks.clear();
	}
}
