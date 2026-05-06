package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.particle.factory.HitGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EntityHitParticlePacket implements CustomPacketPayload {

	public static final Type<EntityHitParticlePacket> TYPE = new Type<>(Hemomancy.rloc("entity_hit_particle_packet"));
	public static final StreamCodec<FriendlyByteBuf, EntityHitParticlePacket> STREAM_CODEC = StreamCodec.of(EntityHitParticlePacket::encode, EntityHitParticlePacket::decode);

	public static EntityHitParticlePacket decode(final FriendlyByteBuf buffer) {
		return new EntityHitParticlePacket(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	public static void encode(final FriendlyByteBuf buffer, final EntityHitParticlePacket message) {
		buffer.writeDouble(message.x);
		buffer.writeDouble(message.y);
		buffer.writeDouble(message.z);

	}

	public static void handle(final EntityHitParticlePacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			if (!player.level().isClientSide) {
				ServerLevel sLevel = (ServerLevel) player.level();
				sLevel.sendParticles(HitGlowParticleFactory.createData(ParticleColor.WHITE), message.x, message.y,
						message.z, 4, 0, 0, 0, 0.001f);
			}

		});
	}

	double x, y, z;

	public EntityHitParticlePacket() {
	}

	public EntityHitParticlePacket(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
