package com.vincenthuto.hemomancy.network.particle;

import java.util.Optional;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.LogicalSidedProvider;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketSpawnLivingToolParticles {
	Vec3 pos;
	ParticleColor color;

	public PacketSpawnLivingToolParticles() {
	}

	public PacketSpawnLivingToolParticles(Vec3 pos, ParticleColor color) {
		this.pos = pos;
		this.color = color;
	}

	public Vec3 getPos() {
		return pos;
	}

	public ParticleColor getColor() {
		return color;
	}

	public static PacketSpawnLivingToolParticles decode(FriendlyByteBuf buf) {
		PacketSpawnLivingToolParticles msg = new PacketSpawnLivingToolParticles();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(PacketSpawnLivingToolParticles msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(PacketSpawnLivingToolParticles msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientLevel = (Optional<?>) LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientLevel.isPresent()) {
			return;
		}
		ClientLevel world = ((ClientLevel) clientLevel.get());
		for (int i = 0; i < 20; i++) {
			world.addParticle(BloodCellParticleFactory.createData(msg.getColor()), msg.getPos().x, msg.getPos().y + 1,
					msg.getPos().z, HLParticleUtils.inRange(-3, 3) * 0.015f, HLParticleUtils.inRange(-3, 3) * 0.015f,
					HLParticleUtils.inRange(-3, 3) * 0.015f);
		}
		ctxSupplier.get().setPacketHandled(true);
	}
}
