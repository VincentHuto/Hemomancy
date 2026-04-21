package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.NetworkEvent;

public class PacketGourdScarSync {

	public int playerId;
	public byte slot;
	ItemStack mindscar;
	private double amount;

	public PacketGourdScarSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.mindscar = buf.readItem();
		this.amount = buf.readDouble();
	}

	public PacketGourdScarSync(int playerId, byte slot, ItemStack mindscar, double amount) {
		this.playerId = playerId;
		this.slot = slot;
		this.mindscar = mindscar;
		this.amount = amount;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (mindscar.getItem() instanceof BloodGourdItem gourd) {
				IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(mindscar)
						.orElseThrow(NullPointerException::new);
				Entity p = Minecraft.getInstance().level.getEntity(playerId);
				if (p instanceof Player) {
					p.getCapability(ScarsCapabilities.SCARS).ifPresent(b -> {
						bloodVolume.setBloodVolume(amount);
						b.setStackInSlot(slot, mindscar);

					});
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		buf.writeItem(this.mindscar);
		buf.writeDouble(this.amount);
	}
}
