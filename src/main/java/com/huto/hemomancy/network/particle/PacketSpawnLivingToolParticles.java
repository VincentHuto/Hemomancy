package com.huto.hemomancy.network.particle;

import java.util.Optional;
import java.util.function.Supplier;

import com.huto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.hutoslib.client.particle.util.ParticleColor;
import com.hutoslib.client.particle.util.ParticleUtils;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import ParticleColor;

import Vec3;

public class PacketSpawnLivingToolParticles {
	Vec3 pos;
	ParticleColor color;

	public PacketSpawnLivingToolParticles() {
	}

	public PacketSpawnLivingToolParticles(Vec3 pos, ParticleColor color) {
		this.pos = pos;
		this.color = color;
	}

	public Vector3d getPos() {
		return pos;
	}

	public ParticleColor getColor() {
		return color;
	}

	public static PacketSpawnLivingToolParticles decode(PacketBuffer buf) {
		PacketSpawnLivingToolParticles msg = new PacketSpawnLivingToolParticles();
		try {
			msg.pos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(PacketSpawnLivingToolParticles msg, PacketBuffer buf) {
		buf.writeDouble((double) msg.getPos().x);
		buf.writeDouble((double) msg.getPos().y);
		buf.writeDouble((double) msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(PacketSpawnLivingToolParticles msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientWorld = (Optional<?>) LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientWorld.isPresent()) {
			return;
		}
		ClientWorld world = ((ClientWorld) clientWorld.get());
		for (int i = 0; i < 20; i++) {
			world.addParticle(BloodCellParticleFactory.createData(msg.getColor()), (double) msg.getPos().x,
					(double) msg.getPos().y + 1, (double) msg.getPos().z, ParticleUtils.inRange(-3, 3) * 0.015f,
					ParticleUtils.inRange(-3, 3) * 0.015f, ParticleUtils.inRange(-3, 3) * 0.015f);
		}
		ctxSupplier.get().setPacketHandled(true);
	}
}
