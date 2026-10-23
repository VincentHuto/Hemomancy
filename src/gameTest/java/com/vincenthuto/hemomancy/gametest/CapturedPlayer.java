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

	private CapturedPlayer(GameTestHelper helper) {
		var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),
				"cap_" + UUID.randomUUID().toString().substring(0, 8)), false);
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

	@Override
	public void close() {
		if (online) offline();
		player.discard();
		channel.finishAndReleaseAll();
	}
}
