package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncBodyIdiomState(float ironHeartHealth, long ironHeartExpiryTick,
		float necroticSaturation, long blackheartedCooldownUntil) implements CustomPacketPayload {
	public static final Type<PacketSyncBodyIdiomState> TYPE =
			new Type<>(Hemomancy.rloc("sync_body_idiom_state"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncBodyIdiomState> STREAM_CODEC =
			StreamCodec.of(PacketSyncBodyIdiomState::encode, PacketSyncBodyIdiomState::decode);

	public PacketSyncBodyIdiomState(PowerGuardrailState state) {
		this(state.getIronHeartHealth(), state.getIronHeartExpiryTick(), state.getNecroticSaturation(),
				state.getBlackheartedCooldownUntil());
	}

	private static void encode(FriendlyByteBuf buffer, PacketSyncBodyIdiomState packet) {
		buffer.writeFloat(packet.ironHeartHealth);
		buffer.writeLong(packet.ironHeartExpiryTick);
		buffer.writeFloat(packet.necroticSaturation);
		buffer.writeLong(packet.blackheartedCooldownUntil);
	}

	private static PacketSyncBodyIdiomState decode(FriendlyByteBuf buffer) {
		return new PacketSyncBodyIdiomState(buffer.readFloat(), buffer.readLong(), buffer.readFloat(), buffer.readLong());
	}

	public static void handle(PacketSyncBodyIdiomState packet, IPayloadContext context) {
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
