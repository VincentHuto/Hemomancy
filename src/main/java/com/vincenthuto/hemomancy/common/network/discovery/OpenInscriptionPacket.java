package com.vincenthuto.hemomancy.common.network.discovery;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.discovery.DiscoveryInscriptionScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record OpenInscriptionPacket(
		String title,
		List<String> lines,
		boolean revealed,
		@Nullable ResourceLocation riteId,
		boolean requiresAbocipherLiteracy) implements CustomPacketPayload {
	public static final Type<OpenInscriptionPacket> TYPE = new Type<>(Hemomancy.rloc("open_inscription_packet"));
	public static final StreamCodec<FriendlyByteBuf, OpenInscriptionPacket> STREAM_CODEC =
			StreamCodec.of(OpenInscriptionPacket::encode, OpenInscriptionPacket::decode);

	public OpenInscriptionPacket(String title, List<String> lines, boolean revealed,
			@Nullable ResourceLocation riteId) {
		this(title, lines, revealed, riteId, false);
	}

	public OpenInscriptionPacket {
		lines = List.copyOf(lines);
	}

	public static void encode(FriendlyByteBuf buf, OpenInscriptionPacket msg) {
		buf.writeUtf(msg.title);
		buf.writeVarInt(msg.lines.size());
		for (String line : msg.lines) {
			buf.writeUtf(line);
		}
		buf.writeBoolean(msg.revealed);
		buf.writeBoolean(msg.riteId != null);
		if (msg.riteId != null) {
			buf.writeResourceLocation(msg.riteId);
		}
		buf.writeBoolean(msg.requiresAbocipherLiteracy);
	}

	public static OpenInscriptionPacket decode(FriendlyByteBuf buf) {
		String title = buf.readUtf();
		int count = buf.readVarInt();
		List<String> lines = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			lines.add(buf.readUtf());
		}
		boolean revealed = buf.readBoolean();
		ResourceLocation riteId = buf.readBoolean() ? buf.readResourceLocation() : null;
		boolean requiresAbocipherLiteracy = buf.readBoolean();
		return new OpenInscriptionPacket(title, lines, revealed, riteId, requiresAbocipherLiteracy);
	}

	public static void handle(final OpenInscriptionPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> DiscoveryInscriptionScreen.open(msg.title, msg.lines, msg.revealed, msg.riteId,
				msg.requiresAbocipherLiteracy));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
