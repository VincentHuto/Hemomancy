package com.vincenthuto.hemomancy.common.network.particle;

import java.util.Optional;
import java.util.function.Supplier;

import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class SpawnFlaskParticlesPacket {
	public static SpawnFlaskParticlesPacket decode(FriendlyByteBuf buf) {
		SpawnFlaskParticlesPacket msg = new SpawnFlaskParticlesPacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}
	public static void encode(SpawnFlaskParticlesPacket msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(SpawnFlaskParticlesPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
		NetworkEvent.Context ctx = ctxSupplier.get();
		LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
		Optional<?> clientLevel = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
		if (!clientLevel.isPresent()) {
			return;
		}
		ClientLevel world = ((ClientLevel) clientLevel.get());
		world.addParticle(GlowParticleFactory.createData(msg.getColor()), msg.getPos().x, msg.getPos().y + 1,
				msg.getPos().z, HLParticleUtils.inRange(-3, 3) * 0.015f, HLParticleUtils.inRange(-3, 3) * 0.015f,
				HLParticleUtils.inRange(-3, 3) * 0.015f);

		ctxSupplier.get().setPacketHandled(true);
	}

	Vec3 pos;

	ParticleColor color;

	public SpawnFlaskParticlesPacket() {
	}

	public SpawnFlaskParticlesPacket(Vec3 pos, ParticleColor color) {
		this.pos = pos;
		this.color = color;
	}

	public ParticleColor getColor() {
		return color;
	}

	public Vec3 getPos() {
		return pos;
	}
}
