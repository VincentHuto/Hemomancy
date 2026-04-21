package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class ToggleMorphlingJarMessagePacket implements CustomPacketPayload {

	public static final Type<ToggleMorphlingJarMessagePacket> TYPE = new Type<>(Hemomancy.rloc("toggle_morphling_jar_message_packet"));
	public static final StreamCodec<FriendlyByteBuf, ToggleMorphlingJarMessagePacket> STREAM_CODEC = StreamCodec.of(ToggleMorphlingJarMessagePacket::encode, ToggleMorphlingJarMessagePacket::decode);
	public static ToggleMorphlingJarMessagePacket decode(final FriendlyByteBuf buffer) {
		boolean en = buffer.readBoolean();
		return new ToggleMorphlingJarMessagePacket(en);
	}

	public static void encode(final ToggleMorphlingJarMessagePacket message, final FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.enabled);
	}

	public static void handle(final ToggleMorphlingJarMessagePacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (Minecraft.getInstance().player == null) return;
			boolean pickup = message.enabled;
			Minecraft.getInstance().player.displayClientMessage(
					Component.literal(
							I18n.get(pickup ? "Hemomancy.autopickupenabled" : "Hemomancy.autopickupdisabled")),
					true);
		});
	}

	private boolean enabled;

	public ToggleMorphlingJarMessagePacket(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
