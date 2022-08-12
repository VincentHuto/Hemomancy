package com.vincenthuto.hemomancy.network.charm;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.container.CharmSlotMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;

public class OpenCharmPacket {

	public OpenCharmPacket() {
	}

	public OpenCharmPacket(FriendlyByteBuf buf) {
	}

	public void encode(FriendlyByteBuf buf) {
	}

	public boolean handle(Supplier<NetworkEvent.Context> context) {
		context.get().getSender()
				.openMenu(new SimpleMenuProvider(
						(i, playerInventory, playerEntity) -> new CharmSlotMenu(i, playerInventory),
						new TranslatableComponent("container.crafting")));
		return true;
	}
}
