package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.rune.RunesCapabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import ItemStack;

public class PacketRuneSync {

	public int playerId;
	public byte slot;
	ItemStack mindrune;

	public PacketRuneSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.mindrune = buf.readItemStack();
	}

	public PacketRuneSync(int playerId, byte slot, ItemStack mindrune) {
		this.playerId = playerId;
		this.slot = slot;
		this.mindrune = mindrune;
	}

	public void toBytes(PacketBuffer buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		buf.writeItemStack(this.mindrune);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity p = Minecraft.getInstance().world.getEntityByID(playerId);
			if (p instanceof PlayerEntity) {
				p.getCapability(RunesCapabilities.RUNES).ifPresent(b -> {
					b.setStackInSlot(slot, mindrune);
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
