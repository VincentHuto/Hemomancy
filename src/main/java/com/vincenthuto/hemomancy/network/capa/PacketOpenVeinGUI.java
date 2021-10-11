package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.gui.manips.GuiChooseVein;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketOpenVeinGUI {

	public PacketOpenVeinGUI(FriendlyByteBuf buf) {
	}

	public PacketOpenVeinGUI() {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			Minecraft.getInstance().setScreen(new GuiChooseVein(mc.player));
			mc.player.playSound(SoundEvents.BOOK_PAGE_TURN, 0.40f, 1F);
		});
		ctx.get().setPacketHandled(true);
	}
}
