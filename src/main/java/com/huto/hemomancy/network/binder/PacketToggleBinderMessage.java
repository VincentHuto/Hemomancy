package com.huto.hemomancy.network.binder;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketToggleBinderMessage {
	public PacketToggleBinderMessage(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean enabled;

	public static PacketToggleBinderMessage decode(final PacketBuffer buffer) {
		boolean en = buffer.readBoolean();
		return new PacketToggleBinderMessage(en);
	}

	public static void encode(final PacketToggleBinderMessage message, final PacketBuffer buffer) {
		buffer.writeBoolean(message.enabled);
	}

	public static void handle(final PacketToggleBinderMessage message, final Supplier<NetworkEvent.Context> ctx) {
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