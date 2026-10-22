package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/** Starts the synchronized first/third-person ritual-implement planting animation. */
public record PacketCardinalRiteStaffPlanting(int casterEntityId, BlockPos focus, ItemStack staff, boolean active)
		implements CustomPacketPayload {
	public static final Type<PacketCardinalRiteStaffPlanting> TYPE =
			new Type<>(Hemomancy.rloc("cardinal_rite_staff_planting"));
	public static final StreamCodec<FriendlyByteBuf, PacketCardinalRiteStaffPlanting> STREAM_CODEC =
			StreamCodec.of(PacketCardinalRiteStaffPlanting::encode, PacketCardinalRiteStaffPlanting::decode);

	public PacketCardinalRiteStaffPlanting {
		staff = staff == null ? ItemStack.EMPTY : staff.copyWithCount(1);
	}

	public PacketCardinalRiteStaffPlanting(int casterEntityId, BlockPos focus, ItemStack staff) {
		this(casterEntityId, focus, staff, true);
	}

	public static PacketCardinalRiteStaffPlanting stop(int casterEntityId) {
		return new PacketCardinalRiteStaffPlanting(casterEntityId, BlockPos.ZERO, ItemStack.EMPTY, false);
	}

	private static void encode(FriendlyByteBuf buffer, PacketCardinalRiteStaffPlanting packet) {
		buffer.writeVarInt(packet.casterEntityId);
		buffer.writeBlockPos(packet.focus);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, packet.staff);
		buffer.writeBoolean(packet.active);
	}

	private static PacketCardinalRiteStaffPlanting decode(FriendlyByteBuf buffer) {
		return new PacketCardinalRiteStaffPlanting(
				buffer.readVarInt(),
				buffer.readBlockPos(),
				ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer),
				buffer.readBoolean());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
