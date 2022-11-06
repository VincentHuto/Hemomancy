package com.vincenthuto.hemomancy.network.particle;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.particle.factory.HitGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class EntityHitParticlePacket {

	public static EntityHitParticlePacket decode(final FriendlyByteBuf buffer) {
		return new EntityHitParticlePacket(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	public static void encode(final EntityHitParticlePacket message, final FriendlyByteBuf buffer) {
		buffer.writeDouble(message.x);
		buffer.writeDouble(message.y);
		buffer.writeDouble(message.z);

	}

	public static void handle(final EntityHitParticlePacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.level.isClientSide) {
				ServerLevel sLevel = (ServerLevel) player.level;
				sLevel.sendParticles(HitGlowParticleFactory.createData(ParticleColor.WHITE), message.x, message.y,
						message.z, 4, 0, 0, 0, 0.001f);
			}

		});
		ctx.get().setPacketHandled(true);
	}

	double x, y, z;

	public EntityHitParticlePacket() {
	}

	public EntityHitParticlePacket(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

}