package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.scar.ScarsCapabilities;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PacketGourdScarSync implements CustomPacketPayload {

	public static void encode(PacketGourdScarSync msg, FriendlyByteBuf buf) {
		msg.toBytes(buf);
	}

	public static PacketGourdScarSync decode(FriendlyByteBuf buf) {
		return new PacketGourdScarSync(buf);
	}

	public static final Type<PacketGourdScarSync> TYPE = new Type<>(Hemomancy.rloc("packet_gourd_scar_sync"));
	public static final StreamCodec<FriendlyByteBuf, PacketGourdScarSync> STREAM_CODEC = StreamCodec.of(PacketGourdScarSync::encode, PacketGourdScarSync::decode);

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

	public static void handle(PacketGourdScarSync msg, IPayloadContext ctx) {
		msg.handle(ctx);
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
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
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		buf.writeItem(this.mindscar);
		buf.writeDouble(this.amount);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
