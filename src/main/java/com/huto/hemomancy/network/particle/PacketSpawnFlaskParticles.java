package com.huto.hemomancy.network.particle;

import java.util.Optional;
import java.util.function.Supplier;

import com.hutoslib.client.particle.factory.GlowParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ParticleUtils;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketSpawnFlaskParticles {
	Vec3 pos;
	ParticleColor color;

	public PacketSpawnFlaskParticles() {
	}

	public PacketSpawnFlaskParticles(Vec3 pos, ParticleColor color) {
		this.pos = pos;
		this.color = color;
	}

	public Vec3 getPos() {
		return pos;
	}

	public ParticleColor getColor() {
		return color;
	}

	public static PacketSpawnFlaskParticles decode(FriendlyByteBuf buf) {
		PacketSpawnFlaskParticles msg = new PacketSpawnFlaskParticles();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(PacketSpawnFlaskParticles msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(PacketSpawnFlaskParticles msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientLevel = (Optional<?>) LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientLevel.isPresent()) {
			return;
		}
		ClientLevel world = ((ClientLevel) clientLevel.get());
		world.addParticle(GlowParticleFactory.createData(msg.getColor()), (double) msg.getPos().x, msg.getPos().y + 1,
				(double) msg.getPos().z, ParticleUtils.inRange(-3, 3) * 0.015f, ParticleUtils.inRange(-3, 3) * 0.015f,
				ParticleUtils.inRange(-3, 3) * 0.015f);

		ctxSupplier.get().setPacketHandled(true);
	}
}
