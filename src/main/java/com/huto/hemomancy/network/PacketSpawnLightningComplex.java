
package com.huto.hemomancy.network;

import java.util.Optional;
import java.util.function.Supplier;

import com.huto.hemomancy.particle.ParticleColor;
import com.huto.hemomancy.particle.data.ParticleComplexLightningData;
import com.huto.hemomancy.util.Vector3;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketSpawnLightningComplex {
	Vector3 position;
	Vector3 speed;
	ParticleColor color;
	int bPerTick, maxAge;

	public PacketSpawnLightningComplex(Vector3d vec, Vector3d speedVec, ParticleColor color, int bPerTick, int maxAge) {
		this.position = new Vector3(vec.x, vec.y, vec.z);
		this.speed = new Vector3(speedVec.x, speedVec.y, speedVec.z);
		this.color = color;
		this.bPerTick = bPerTick;
		this.maxAge = maxAge;

	}

	public PacketSpawnLightningComplex() {
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

	public int getbPerTick() {
		return bPerTick;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public static PacketSpawnLightningComplex decode(PacketBuffer buf) {
		PacketSpawnLightningComplex msg = new PacketSpawnLightningComplex();
		try {
			msg.position = new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.speed = new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());
			msg.bPerTick = buf.readInt();
			msg.maxAge = buf.readInt();

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(PacketSpawnLightningComplex message, PacketBuffer buf) {
		buf.writeDouble((double) message.getPosition().x);
		buf.writeDouble((double) message.getPosition().y);
		buf.writeDouble((double) message.getPosition().z);
		buf.writeDouble((double) message.getSpeed().x);
		buf.writeDouble((double) message.getSpeed().y);
		buf.writeDouble((double) message.getSpeed().z);
		buf.writeFloat(message.getColor().getRed());
		buf.writeFloat(message.getColor().getGreen());
		buf.writeFloat(message.getColor().getBlue());
		buf.writeInt(message.getbPerTick());
		buf.writeInt(message.getMaxAge());

	}

	public static void handle(PacketSpawnLightningComplex message, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientWorld = (Optional<?>) LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientWorld.isPresent()) {
			return;
		}

		((ClientWorld) clientWorld.get()).addParticle(ParticleComplexLightningData.createData(message.color),
				(double) message.getPosition().x, (double) message.getPosition().y, (double) message.getPosition().z,
				(double) message.getSpeed().x, (double) message.getSpeed().y, (double) message.getSpeed().z);
		ctxSupplier.get().setPacketHandled(true);
	}
}
