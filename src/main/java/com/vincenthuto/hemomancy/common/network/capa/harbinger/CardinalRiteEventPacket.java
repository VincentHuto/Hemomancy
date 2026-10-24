package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → client: one discrete beat of an active Cardinal Rite. The periodic
 * {@link PacketSyncActiveRites} snapshot carries state; this carries the moments
 * between snapshots (impact, seals, wave arrival, offerings, culmination, endings)
 * so the floor, boundary and daemon can react on the exact tick.
 */
public record CardinalRiteEventPacket(BlockPos center, CardinalRiteEventKind kind, int index,
		String style, List<Vec3> points, ItemStack item) implements CustomPacketPayload {
	public static final Type<CardinalRiteEventPacket> TYPE = new Type<>(
			Hemomancy.rloc("cardinal_rite_event"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CardinalRiteEventPacket> STREAM_CODEC =
			StreamCodec.of(CardinalRiteEventPacket::encode, CardinalRiteEventPacket::decode);
	/** Players this far from the rite centre receive its beats. */
	public static final double BROADCAST_RADIUS = 96.0D;
	private static final int MAX_POINTS = 64;
	private static final int MAX_STYLE = 256;

	public CardinalRiteEventPacket {
		style = style == null ? "" : style.length() > MAX_STYLE ? style.substring(0, MAX_STYLE) : style;
		points = points == null ? List.of() : List.copyOf(points.subList(0, Math.min(MAX_POINTS, points.size())));
		item = item == null ? ItemStack.EMPTY : item;
	}

	public static CardinalRiteEventPacket of(BlockPos center, CardinalRiteEventKind kind) {
		return new CardinalRiteEventPacket(center, kind, 0, "", List.of(), ItemStack.EMPTY);
	}

	public static CardinalRiteEventPacket of(BlockPos center, CardinalRiteEventKind kind, int index) {
		return new CardinalRiteEventPacket(center, kind, index, "", List.of(), ItemStack.EMPTY);
	}

	/** Sends the beat to every player near the rite in its level. */
	public void broadcast(ServerLevel level) {
		Vec3 origin = Vec3.atCenterOf(center);
		double radiusSqr = BROADCAST_RADIUS * BROADCAST_RADIUS;
		for (var player : level.players()) {
			if (player.distanceToSqr(origin) <= radiusSqr) PacketHandler.sendToPlayer(player, this);
		}
	}

	private static void encode(RegistryFriendlyByteBuf buffer, CardinalRiteEventPacket packet) {
		buffer.writeBlockPos(packet.center);
		buffer.writeVarInt(packet.kind.ordinal());
		buffer.writeVarInt(packet.index);
		buffer.writeUtf(packet.style, MAX_STYLE);
		buffer.writeVarInt(packet.points.size());
		for (Vec3 point : packet.points) {
			buffer.writeDouble(point.x);
			buffer.writeDouble(point.y);
			buffer.writeDouble(point.z);
		}
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, packet.item);
	}

	private static CardinalRiteEventPacket decode(RegistryFriendlyByteBuf buffer) {
		BlockPos center = buffer.readBlockPos();
		CardinalRiteEventKind kind = CardinalRiteEventKind.byOrdinal(buffer.readVarInt());
		int index = buffer.readVarInt();
		String style = buffer.readUtf(MAX_STYLE);
		int count = Math.min(MAX_POINTS, buffer.readVarInt());
		List<Vec3> points = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			points.add(new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
		}
		ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
		return new CardinalRiteEventPacket(center, kind, index, style, points, item);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
