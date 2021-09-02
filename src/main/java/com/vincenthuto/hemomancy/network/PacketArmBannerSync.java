package com.vincenthuto.hemomancy.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketArmBannerSync {

	public int playerId;
	public int slot;
	ItemStack armBanner;

	public PacketArmBannerSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.armBanner = buf.readItem();
	}

	public PacketArmBannerSync(int playerId, int slot, ItemStack armBanner) {
		this.playerId = playerId;
		this.slot = slot;
		this.armBanner = armBanner;
	}

	public void decode(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeInt(this.slot);
		buf.writeItem(this.armBanner);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity p = Minecraft.getInstance().level.getEntity(playerId);

			if (p instanceof Player) {
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
