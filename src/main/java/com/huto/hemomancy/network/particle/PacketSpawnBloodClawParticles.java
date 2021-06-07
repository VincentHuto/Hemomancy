package com.huto.hemomancy.network.particle;

import java.util.Optional;
import java.util.function.Supplier;

import com.huto.hemomancy.particle.factory.BloodClawParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSpawnBloodClawParticles {
	Vector3d pos;
	ParticleColor color;

	public PacketSpawnBloodClawParticles() {
	}

	public PacketSpawnBloodClawParticles(Vector3d pos, ParticleColor color) {
		this.pos = pos;
		this.color = color;
	}

	public Vector3d getPos() {
		return pos;
	}

	public ParticleColor getColor() {
		return color;
	}

	public static PacketSpawnBloodClawParticles decode(PacketBuffer buf) {
		PacketSpawnBloodClawParticles msg = new PacketSpawnBloodClawParticles();
		try {
			msg.pos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(PacketSpawnBloodClawParticles msg, PacketBuffer buf) {
		buf.writeDouble((double) msg.getPos().x);
		buf.writeDouble((double) msg.getPos().y);
		buf.writeDouble((double) msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(PacketSpawnBloodClawParticles msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientWorld = (Optional<?>) LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientWorld.isPresent()) {
			return;
		}
		ClientWorld world = ((ClientWorld) clientWorld.get());
		world.addParticle(BloodClawParticleFactory.createData(msg.getColor()), (double) msg.getPos().x,
				(double) msg.getPos().y, (double) msg.getPos().z, 0f, 0, 0);

		ctxSupplier.get().setPacketHandled(true);
	}
}
