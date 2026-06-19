package com.vincenthuto.hemomancy.common.network.capa.harbinger.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketEquipmentSync implements CustomPacketPayload {

	public static void encode(FriendlyByteBuf buf, PacketEquipmentSync msg) {
		msg.toBytes(buf);
	}

	public static PacketEquipmentSync decode(FriendlyByteBuf buf) {
		return new PacketEquipmentSync(buf);
	}

	public static final Type<PacketEquipmentSync> TYPE = new Type<>(Hemomancy.rloc("packet_scar_sync"));
	public static final StreamCodec<FriendlyByteBuf, PacketEquipmentSync> STREAM_CODEC = StreamCodec.of(PacketEquipmentSync::encode, PacketEquipmentSync::decode);

	public int playerId;
	public byte slot;
	ItemStack mindscar;

	public PacketEquipmentSync(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.slot = buf.readByte();
		this.mindscar = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
	}

	public PacketEquipmentSync(int playerId, byte slot, ItemStack mindscar) {
		this.playerId = playerId;
		this.slot = slot;
		this.mindscar = mindscar;
	}


	public static void handle(final PacketEquipmentSync msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) {
				return;
			}
			Entity p = mc.level.getEntity(msg.playerId);
			if (p instanceof Player player) {
				HemoCapabilityAccess.getEquipment(player).ifPresent(b -> {
					b.setStackInSlot(msg.slot, msg.mindscar);
				});
			}
		});
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeByte(this.slot);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, this.mindscar);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
