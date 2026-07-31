package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Starts the synchronized first/third-person Living Staff planting animation. */
public record PacketCardinalRiteStaffPlanting(int casterEntityId, BlockPos focus, ItemStack staff)
		implements CustomPacketPayload {
	public static final Type<PacketCardinalRiteStaffPlanting> TYPE =
			new Type<>(Hemomancy.rloc("cardinal_rite_staff_planting"));
	public static final StreamCodec<FriendlyByteBuf, PacketCardinalRiteStaffPlanting> STREAM_CODEC =
			StreamCodec.of(PacketCardinalRiteStaffPlanting::encode, PacketCardinalRiteStaffPlanting::decode);

	public PacketCardinalRiteStaffPlanting {
		staff = staff == null ? ItemStack.EMPTY : staff.copyWithCount(1);
	}

	private static void encode(FriendlyByteBuf buffer, PacketCardinalRiteStaffPlanting packet) {
		buffer.writeVarInt(packet.casterEntityId);
		buffer.writeBlockPos(packet.focus);
		ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buffer, packet.staff);
	}

	private static PacketCardinalRiteStaffPlanting decode(FriendlyByteBuf buffer) {
		return new PacketCardinalRiteStaffPlanting(
				buffer.readVarInt(),
				buffer.readBlockPos(),
				ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer));
	}

	public static void handle(PacketCardinalRiteStaffPlanting packet, IPayloadContext context) {
		context.enqueueWork(() -> CardinalRiteStaffPlantingClientState.start(
				packet.casterEntityId, packet.focus, packet.staff));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
