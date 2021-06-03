package com.huto.hemomancy.network.jar;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketToggleJarMessage {
	private boolean enabled;

	public PacketToggleJarMessage(boolean enabled) {
		this.enabled = enabled;
	}

	public static PacketToggleJarMessage decode(final PacketBuffer buffer) {
		boolean en = buffer.readBoolean();
		return new PacketToggleJarMessage(en);
	}

	public static void encode(final PacketToggleJarMessage message, final PacketBuffer buffer) {
		buffer.writeBoolean(message.enabled);
	}

	public static void handle(final PacketToggleJarMessage message, final Supplier<NetworkEvent.Context> ctx) {
		if (ctx.get().getDirection().getReceptionSide().isClient())
			ctx.get().enqueueWork(() -> {
				boolean Pickup = message.enabled;
				Minecraft.getInstance().player.sendStatusMessage(
						new StringTextComponent(
								I18n.format(Pickup ? "Hemomancy.autopickupenabled" : "Hemomancy.autopickupdisabled")),
						true);
			});
		ctx.get().setPacketHandled(true);
	}
}