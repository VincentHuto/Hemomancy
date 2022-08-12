package com.vincenthuto.hemomancy.radial;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

public class PacketOpenCharm {

	public PacketOpenCharm() {
	}

	public PacketOpenCharm(FriendlyByteBuf buf) {
	}

	public void encode(FriendlyByteBuf buf) {
	}

	public boolean handle(Supplier<NetworkEvent.Context> context) {
		context.get().getSender()
				.openMenu(new SimpleMenuProvider(
						(i, playerInventory, playerEntity) -> new CharmSlotContainer(i, playerInventory),
						new TranslatableComponent("container.crafting")));
		return true;
	}
}
