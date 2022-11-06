package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.rune.RunesCapabilities;
import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.item.tool.BloodGourdItem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class PacketGourdRuneSync {

	public int playerId;
	public byte slot;
	ItemStack mindrune;
	private double amount;

	public PacketGourdRuneSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.mindrune = buf.readItem();
		this.amount = buf.readDouble();
	}

	public PacketGourdRuneSync(int playerId, byte slot, ItemStack mindrune, double amount) {
		this.playerId = playerId;
		this.slot = slot;
		this.mindrune = mindrune;
		this.amount = amount;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (mindrune.getItem() instanceof BloodGourdItem gourd) {
				IBloodVolume bloodVolume = mindrune.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				Entity p = Minecraft.getInstance().level.getEntity(playerId);
				if (p instanceof Player) {
					p.getCapability(RunesCapabilities.RUNES).ifPresent(b -> {
						bloodVolume.setBloodVolume(amount);
						b.setStackInSlot(slot, mindrune);

					});
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		buf.writeItem(this.mindrune);
		buf.writeDouble(this.amount);
	}
}
