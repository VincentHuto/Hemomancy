package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.player.LivingStaffMorphClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketLivingStaffMorph(int casterEntityId, ItemStack beforeMain, ItemStack afterMain,
		ItemStack beforeOff, ItemStack afterOff) implements CustomPacketPayload {
	public static final Type<PacketLivingStaffMorph> TYPE =
			new Type<>(Hemomancy.rloc("living_staff_morph"));
	public static final StreamCodec<FriendlyByteBuf, PacketLivingStaffMorph> STREAM_CODEC =
			StreamCodec.of(PacketLivingStaffMorph::encode, PacketLivingStaffMorph::decode);

	public PacketLivingStaffMorph {
		beforeMain = copy(beforeMain);
		afterMain = copy(afterMain);
		beforeOff = copy(beforeOff);
		afterOff = copy(afterOff);
	}

	private static ItemStack copy(ItemStack stack) {
		return stack == null ? ItemStack.EMPTY : stack.copy();
	}

	private static void encode(FriendlyByteBuf buffer, PacketLivingStaffMorph packet) {
		RegistryFriendlyByteBuf registryBuffer = (RegistryFriendlyByteBuf) buffer;
		buffer.writeVarInt(packet.casterEntityId);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuffer, packet.beforeMain);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuffer, packet.afterMain);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuffer, packet.beforeOff);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(registryBuffer, packet.afterOff);
	}

	private static PacketLivingStaffMorph decode(FriendlyByteBuf buffer) {
		RegistryFriendlyByteBuf registryBuffer = (RegistryFriendlyByteBuf) buffer;
		return new PacketLivingStaffMorph(buffer.readVarInt(),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuffer),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuffer),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuffer),
				ItemStack.OPTIONAL_STREAM_CODEC.decode(registryBuffer));
	}

	public static void handle(PacketLivingStaffMorph packet, IPayloadContext context) {
		context.enqueueWork(() -> LivingStaffMorphClientState.start(packet));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
