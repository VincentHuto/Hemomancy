package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.rune.RunesCapabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketRuneSync {

	public int playerId;
	public byte slot;
	ItemStack mindrune;

	public PacketRuneSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.mindrune = buf.readItem();
	}

	public PacketRuneSync(int playerId, byte slot, ItemStack mindrune) {
		this.playerId = playerId;
		this.slot = slot;
		this.mindrune = mindrune;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity p = Minecraft.getInstance().level.getEntity(playerId);
			if (p instanceof Player) {
				p.getCapability(RunesCapabilities.RUNES).ifPresent(b -> {
					b.setStackInSlot(slot, mindrune);
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		buf.writeItem(this.mindrune);
	}
}
