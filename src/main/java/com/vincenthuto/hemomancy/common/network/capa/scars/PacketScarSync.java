package com.vincenthuto.hemomancy.common.network.capa.scars;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketScarSync {

	public int playerId;
	public byte slot;
	ItemStack mindscar;

	public PacketScarSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.mindscar = buf.readItem();
	}

	public PacketScarSync(int playerId, byte slot, ItemStack mindscar) {
		this.playerId = playerId;
		this.slot = slot;
		this.mindscar = mindscar;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity p = Minecraft.getInstance().level.getEntity(playerId);
			if (p instanceof Player) {
				p.getCapability(ScarsCapabilities.SCARS).ifPresent(b -> {
					b.setStackInSlot(slot, mindscar);
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		buf.writeItem(this.mindscar);
	}
}
