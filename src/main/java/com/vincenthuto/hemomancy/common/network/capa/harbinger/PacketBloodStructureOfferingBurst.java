package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveBloodStructureOfferingBurstClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketBloodStructureOfferingBurst(List<OfferingBurst> bursts, BlockPos center,
		int durationTicks) implements CustomPacketPayload {
	public static final Type<PacketBloodStructureOfferingBurst> TYPE =
			new Type<>(Hemomancy.rloc("packet_blood_structure_offering_burst"));
	public static final StreamCodec<FriendlyByteBuf, PacketBloodStructureOfferingBurst> STREAM_CODEC =
			StreamCodec.of(PacketBloodStructureOfferingBurst::encode, PacketBloodStructureOfferingBurst::decode);

	public PacketBloodStructureOfferingBurst {
		bursts = List.copyOf(bursts);
	}

	public static void encode(FriendlyByteBuf buf, PacketBloodStructureOfferingBurst msg) {
		buf.writeBlockPos(msg.center);
		buf.writeVarInt(msg.durationTicks);
		buf.writeVarInt(msg.bursts.size());
		for (OfferingBurst burst : msg.bursts) {
			buf.writeBlockPos(burst.source);
			ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, burst.stack);
		}
	}

	public static PacketBloodStructureOfferingBurst decode(FriendlyByteBuf buf) {
		BlockPos center = buf.readBlockPos();
		int durationTicks = buf.readVarInt();
		int size = buf.readVarInt();
		List<OfferingBurst> bursts = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			BlockPos source = buf.readBlockPos();
			ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
			bursts.add(new OfferingBurst(source, stack));
		}
		return new PacketBloodStructureOfferingBurst(bursts, center, durationTicks);
	}

	public static void handle(final PacketBloodStructureOfferingBurst msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> ActiveBloodStructureOfferingBurstClientData.add(msg.bursts, msg.center, msg.durationTicks));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public record OfferingBurst(BlockPos source, ItemStack stack) {
		public OfferingBurst {
			stack = stack.copy();
		}
	}
}
