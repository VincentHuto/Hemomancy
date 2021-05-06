package com.huto.hemomancy.network.particle;

import java.util.Optional;
import java.util.function.Supplier;

import com.hutoslib.client.particle.ParticleColor;
import com.hutoslib.client.particle.ParticleUtil;
import com.hutoslib.client.particles.factory.GlowParticleFactory;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSpawnFlaskParticles {
	Vector3d pos;
	ParticleColor color;

	public PacketSpawnFlaskParticles() {
	}

	public PacketSpawnFlaskParticles(Vector3d pos, ParticleColor color) {
		this.pos = pos;
		this.color = color;
	}

	public Vector3d getPos() {
		return pos;
	}

	public ParticleColor getColor() {
		return color;
	}

	public static PacketSpawnFlaskParticles decode(PacketBuffer buf) {
		PacketSpawnFlaskParticles msg = new PacketSpawnFlaskParticles();
		try {
			msg.pos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(PacketSpawnFlaskParticles msg, PacketBuffer buf) {
		buf.writeDouble((double) msg.getPos().x);
		buf.writeDouble((double) msg.getPos().y);
		buf.writeDouble((double) msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(PacketSpawnFlaskParticles msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientWorld = (Optional<?>) LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientWorld.isPresent()) {
			return;
		}
		ClientWorld world = ((ClientWorld) clientWorld.get());
		world.addParticle(GlowParticleFactory.createData(msg.getColor()), (double) msg.getPos().x,
				(double) msg.getPos().y + 1, (double) msg.getPos().z, ParticleUtil.inRange(-3, 3) * 0.015f,
				ParticleUtil.inRange(-3, 3) * 0.015f, ParticleUtil.inRange(-3, 3) * 0.015f);

		ctxSupplier.get().setPacketHandled(true);
	}
}
