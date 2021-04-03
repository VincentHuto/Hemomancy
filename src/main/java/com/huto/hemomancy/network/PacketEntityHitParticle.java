package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.particle.factory.HitGlowParticleFactory;
import com.huto.hemomancy.particle.util.ParticleUtil;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketEntityHitParticle {

	double x, y, z;

	public PacketEntityHitParticle() {
	}

	public PacketEntityHitParticle(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static PacketEntityHitParticle decode(final PacketBuffer buffer) {
		return new PacketEntityHitParticle(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
	}

	public static void encode(final PacketEntityHitParticle message, final PacketBuffer buffer) {
		buffer.writeDouble(message.x);
		buffer.writeDouble(message.y);
		buffer.writeDouble(message.z);

	}

	public static void handle(final PacketEntityHitParticle message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();
			if (player == null)
				return;
			if (!player.world.isRemote) {
				ServerWorld sWorld = (ServerWorld) player.world;
				sWorld.spawnParticle(HitGlowParticleFactory.createData(ParticleUtil.WHITE), message.x, message.y,
						message.z, 4, 0, 0, 0, 0.001f);
			}

		});
		ctx.get().setPacketHandled(true);
	}

}