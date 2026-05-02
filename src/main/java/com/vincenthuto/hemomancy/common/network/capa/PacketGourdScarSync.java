package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PacketGourdScarSync implements CustomPacketPayload {

	public static void encode(FriendlyByteBuf buf, PacketGourdScarSync msg) {
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
		this.mindscar = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
		this.amount = buf.readDouble();
	}

	public PacketGourdScarSync(int playerId, byte slot, ItemStack mindscar, double amount) {
		this.playerId = playerId;
		this.slot = slot;
		this.mindscar = mindscar;
		this.amount = amount;
	}


	public static void handle(final PacketGourdScarSync msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (msg.mindscar.getItem() instanceof BloodGourdItem gourd) {
				IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(msg.mindscar)
						.orElseThrow(NullPointerException::new);
				Minecraft mc = Minecraft.getInstance();
				if (mc.level == null) {
					return;
				}
				Entity p = mc.level.getEntity(msg.playerId);
				if (p instanceof Player player) {
					HemoCapabilityAccess.getScars(player).ifPresent(b -> {
						bloodVolume.setBloodVolume(msg.amount);
						b.setStackInSlot(msg.slot, msg.mindscar);

					});
				}
			}
		});
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, this.mindscar);
		buf.writeDouble(this.amount);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
