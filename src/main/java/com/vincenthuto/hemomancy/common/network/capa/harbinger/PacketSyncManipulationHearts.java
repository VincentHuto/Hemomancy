package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncManipulationHearts(float ironHeartHealth, long ironHeartExpiryTick,
		float necroticSaturation, long blackheartedCooldownUntil) implements CustomPacketPayload {
	public static final Type<PacketSyncManipulationHearts> TYPE =
			new Type<>(Hemomancy.rloc("sync_manipulation_hearts"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncManipulationHearts> STREAM_CODEC =
			StreamCodec.of(PacketSyncManipulationHearts::encode, PacketSyncManipulationHearts::decode);

	public PacketSyncManipulationHearts(PowerGuardrailState state) {
		this(state.getIronHeartHealth(), state.getIronHeartExpiryTick(), state.getNecroticSaturation(),
				state.getBlackheartedCooldownUntil());
	}

	private static void encode(FriendlyByteBuf buffer, PacketSyncManipulationHearts packet) {
		buffer.writeFloat(packet.ironHeartHealth);
		buffer.writeLong(packet.ironHeartExpiryTick);
		buffer.writeFloat(packet.necroticSaturation);
		buffer.writeLong(packet.blackheartedCooldownUntil);
	}

	private static PacketSyncManipulationHearts decode(FriendlyByteBuf buffer) {
		return new PacketSyncManipulationHearts(buffer.readFloat(), buffer.readLong(), buffer.readFloat(), buffer.readLong());
	}

	public static void handle(PacketSyncManipulationHearts packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(context.player());
			state.setIronHeartHealth(packet.ironHeartHealth);
			state.setIronHeartExpiryTick(packet.ironHeartExpiryTick);
			state.setNecroticSaturation(packet.necroticSaturation);
			state.setBlackheartedCooldownUntil(packet.blackheartedCooldownUntil);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
