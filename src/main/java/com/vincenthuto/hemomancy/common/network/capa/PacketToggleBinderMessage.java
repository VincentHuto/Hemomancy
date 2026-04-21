package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class PacketToggleBinderMessage implements CustomPacketPayload {

	public static final Type<PacketToggleBinderMessage> TYPE = new Type<>(Hemomancy.rloc("packet_toggle_binder_message"));
	public static final StreamCodec<FriendlyByteBuf, PacketToggleBinderMessage> STREAM_CODEC = StreamCodec.of(PacketToggleBinderMessage::encode, PacketToggleBinderMessage::decode);
	public PacketToggleBinderMessage(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean enabled;

	public static PacketToggleBinderMessage decode(final FriendlyByteBuf buffer) {
		boolean en = buffer.readBoolean();
		return new PacketToggleBinderMessage(en);
	}

	public static void encode(final PacketToggleBinderMessage message, final FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.enabled);
	}

	public static void handle(final PacketToggleBinderMessage message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (Minecraft.getInstance().player == null) return;
			boolean pickup = message.enabled;
			Minecraft.getInstance().player.displayClientMessage(
					Component.translatable(
							I18n.get(pickup ? "Hemomancy.autopickupenabled" : "Hemomancy.autopickupdisabled")),
					true);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
