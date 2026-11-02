package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A server player with a live connection whose outgoing custom payloads are captured instead of sent. It never joins
 * the level, so tests running alongside cannot see it. Close it to discard the player and release the channel.
 */
public final class CapturedPlayer implements AutoCloseable {
	public final ServerPlayer player;
	public final List<CustomPacketPayload> payloads = new ArrayList<>();
	/** Chat and action-bar text sent to the player, in send order. */
	public final List<String> chat = new ArrayList<>();
	private final EmbeddedChannel channel;
	private boolean online;
	/** The level list {@link #joinLevel()} added the player to; null when not joined. */
	private List<ServerPlayer> levelPlayers;

	private CapturedPlayer(GameTestHelper helper) {
		this(helper, new GameProfile(UUID.randomUUID(), "cap_" + UUID.randomUUID().toString().substring(0, 8)));
	}

	private CapturedPlayer(GameTestHelper helper, GameProfile profile) {
		var cookie = CommonListenerCookie.createInitial(profile, false);
		this.player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
				cookie.clientInformation());
		var connection = new Connection(PacketFlow.SERVERBOUND);
		this.channel = new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override public void send(Packet<?> packet) { capture(packet); }
			@Override public void send(Packet<?> packet, @Nullable PacketSendListener listener) { capture(packet); }
		};
	}

	public static CapturedPlayer create(GameTestHelper helper) {
		return new CapturedPlayer(helper);
	}

	/** A player with a given profile, so a respawned or relogged player keeps the UUID of the one it replaces. */
	public static CapturedPlayer create(GameTestHelper helper, GameProfile profile) {
		return new CapturedPlayer(helper, profile);
	}

	private void capture(Packet<?> packet) {
		if (packet instanceof ClientboundCustomPayloadPacket custom) payloads.add(custom.payload());
		if (packet instanceof ClientboundSystemChatPacket text) chat.add(text.content().getString());
	}

	/** Everything said to the player, joined for assertion messages. */
	public String said() {
		return String.join(" | ", chat);
	}

	/**
	 * Lists the player in the server's UUID lookup, so code that finds players by UUID (a rite finding its caster)
	 * sees them, without joining the level or running a login. {@link #close()} takes them out again.
	 */
	public CapturedPlayer online() {
		playersByUuid().put(player.getUUID(), player);
		online = true;
		return this;
	}

	/** Takes the player out of the server's UUID lookup, as if they had logged out. */
	public CapturedPlayer offline() {
		if (levelPlayers != null) leaveLevel();
		playersByUuid().remove(player.getUUID(), player);
		online = false;
		return this;
	}

	@SuppressWarnings("unchecked")
	private Map<UUID, ServerPlayer> playersByUuid() {
		try {
			var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
			field.setAccessible(true);
			return (Map<UUID, ServerPlayer>) field.get(player.server.getPlayerList());
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not reach the player list's UUID lookup", exception);
		}
	}

	/** The captured payloads of one type, in send order. */
	public <T extends CustomPacketPayload> List<T> sent(Class<T> type) {
		return payloads.stream().filter(type::isInstance).map(type::cast).toList();
	}

	/**
	 * Opt-in: lists the player in {@code PlayerList.getPlayers()} and in its level's player list, so code that finds
	 * players through those lists ({@code level.getPlayerByUUID}, {@code getNearestPlayer}, {@code level.getPlayers},
	 * {@code server.getPlayerList().getPlayers()}) sees them. The player is still not in the level's entity storage:
	 * the server never ticks them and {@code getEntitiesOfClass(Player.class, ...)} does not find them.
	 * {@link #leaveLevel()} (and {@link #offline()} and {@link #close()}) take them out again. Also puts them
	 * {@link #online()}.
	 */
	public CapturedPlayer joinLevel() {
		online();
		List<ServerPlayer> all = serverPlayers();
		if (!all.contains(player)) all.add(player);
		levelPlayers = levelPlayers(player.serverLevel());
		if (!levelPlayers.contains(player)) levelPlayers.add(player);
		return this;
	}

	/** Takes the player out of the lists {@link #joinLevel()} put them in; nothing when they were not joined. */
	public CapturedPlayer leaveLevel() {
		serverPlayers().remove(player);
		if (levelPlayers != null) levelPlayers.remove(player);
		levelPlayers(player.serverLevel()).remove(player);
		levelPlayers = null;
		return this;
	}

	public boolean joinedLevel() {
		return levelPlayers != null;
	}

	@SuppressWarnings("unchecked")
	private List<ServerPlayer> serverPlayers() {
		try {
			var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("players");
			field.setAccessible(true);
			return (List<ServerPlayer>) field.get(player.server.getPlayerList());
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not reach the player list", exception);
		}
	}

	@SuppressWarnings("unchecked")
	private static List<ServerPlayer> levelPlayers(net.minecraft.server.level.ServerLevel level) {
		try {
			var field = net.minecraft.server.level.ServerLevel.class.getDeclaredField("players");
			field.setAccessible(true);
			return (List<ServerPlayer>) field.get(level);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not reach the level's player list", exception);
		}
	}

	@Override
	public void close() {
		if (levelPlayers != null) leaveLevel();
		if (online) offline();
		player.discard();
		channel.finishAndReleaseAll();
	}
}
