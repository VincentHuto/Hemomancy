package com.vincenthuto.hemomancy.network.morphling;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.network.NetworkEvent;

public class ToggleMorphlingJarMessagePacket {
	private boolean enabled;

	public ToggleMorphlingJarMessagePacket(boolean enabled) {
		this.enabled = enabled;
	}

	public static ToggleMorphlingJarMessagePacket decode(final FriendlyByteBuf buffer) {
		boolean en = buffer.readBoolean();
		return new ToggleMorphlingJarMessagePacket(en);
	}

	public static void encode(final ToggleMorphlingJarMessagePacket message, final FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.enabled);
	}

	public static void handle(final ToggleMorphlingJarMessagePacket message, final Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide().isClient())
			ctx.get().enqueueWork(() -> {
				boolean Pickup = message.enabled;
				Minecraft.getInstance().player.displayClientMessage(
						new TextComponent(
								I18n.get(Pickup ? "Hemomancy.autopickupenabled" : "Hemomancy.autopickupdisabled")),
						true);
			});
		ctx.get().setPacketHandled(true);
	}
}