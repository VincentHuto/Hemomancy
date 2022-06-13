package com.vincenthuto.hemomancy.network.morphling;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

public class PacketToggleJarMessage {
	private boolean enabled;

	public PacketToggleJarMessage(boolean enabled) {
		this.enabled = enabled;
	}

	public static PacketToggleJarMessage decode(final FriendlyByteBuf buffer) {
		boolean en = buffer.readBoolean();
		return new PacketToggleJarMessage(en);
	}

	public static void encode(final PacketToggleJarMessage message, final FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.enabled);
	}

	public static void handle(final PacketToggleJarMessage message, final Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide().isClient())
			ctx.get().enqueueWork(() -> {
				boolean Pickup = message.enabled;
				Minecraft.getInstance().player.displayClientMessage(
						 Component.translatable(
								I18n.get(Pickup ? "Hemomancy.autopickupenabled" : "Hemomancy.autopickupdisabled")),
						true);
			});
		ctx.get().setPacketHandled(true);
	}
}
