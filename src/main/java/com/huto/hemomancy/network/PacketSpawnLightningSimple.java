
package com.huto.hemomancy.network;

import java.util.Optional;
import java.util.function.Supplier;

import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.data.ParticleLightningData;
import com.huto.hemomancy.util.Vector3;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSpawnLightningSimple {
	Vector3 position;
	Vector3 speed;
	ParticleColor color;

	public PacketSpawnLightningSimple(Vector3d vec, Vector3d speedVec, ParticleColor color) {
		this.position = new Vector3(vec.x, vec.y, vec.z);
		this.speed = new Vector3(speedVec.x, speedVec.y, speedVec.z);
		this.color = color;
	}

	public PacketSpawnLightningSimple() {
	}

	public Vector3 getPosition() {
		return this.position;
	}

	public Vector3 getSpeed() {
		return this.speed;
	}

	public ParticleColor getColor() {
		return color;
	}

	public static PacketSpawnLightningSimple decode(PacketBuffer buf) {
		PacketSpawnLightningSimple msg = new PacketSpawnLightningSimple();
		try {
			msg.position = new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.speed = new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(PacketSpawnLightningSimple message, PacketBuffer buf) {
		buf.writeDouble((double) message.getPosition().x);
		buf.writeDouble((double) message.getPosition().y);
		buf.writeDouble((double) message.getPosition().z);
		buf.writeDouble((double) message.getSpeed().x);
		buf.writeDouble((double) message.getSpeed().y);
		buf.writeDouble((double) message.getSpeed().z);
		buf.writeFloat( message.getColor().getRed());
		buf.writeFloat( message.getColor().getGreen());
		buf.writeFloat( message.getColor().getBlue());


	}

	public static void handle(PacketSpawnLightningSimple message, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientWorld = (Optional<?>) LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientWorld.isPresent()) {
			return;
		}

		((ClientWorld) clientWorld.get()).addParticle(ParticleLightningData.createData(message.color),
				(double) message.getPosition().x, (double) message.getPosition().y, (double) message.getPosition().z,
				(double) message.getSpeed().x, (double) message.getSpeed().y, (double) message.getSpeed().z);
		ctxSupplier.get().setPacketHandled(true);
	}
}
