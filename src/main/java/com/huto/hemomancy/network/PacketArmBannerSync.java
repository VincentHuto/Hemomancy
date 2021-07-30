package com.huto.hemomancy.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import ItemStack;

public class PacketArmBannerSync {

	public int playerId;
	public int slot;
	ItemStack armBanner;

	public PacketArmBannerSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.armBanner = buf.readItemStack();
	}

	public PacketArmBannerSync(int playerId, int slot, ItemStack armBanner) {
		this.playerId = playerId;
		this.slot = slot;
		this.armBanner = armBanner;
	}

	public void decode(PacketBuffer buf) {
		buf.writeInt(this.playerId);
		buf.writeInt(this.slot);
		buf.writeItemStack(this.armBanner);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity p = Minecraft.getInstance().world.getEntityByID(playerId);

			if (p instanceof PlayerEntity) {
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
