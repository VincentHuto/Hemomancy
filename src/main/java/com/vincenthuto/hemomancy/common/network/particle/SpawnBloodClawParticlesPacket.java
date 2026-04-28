package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.particle.factory.BloodClawParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class SpawnBloodClawParticlesPacket implements CustomPacketPayload {

	public static final Type<SpawnBloodClawParticlesPacket> TYPE = new Type<>(Hemomancy.rloc("spawn_blood_claw_particles_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnBloodClawParticlesPacket> STREAM_CODEC = StreamCodec.of(SpawnBloodClawParticlesPacket::encode, SpawnBloodClawParticlesPacket::decode);
	public static SpawnBloodClawParticlesPacket decode(FriendlyByteBuf buf) {
		SpawnBloodClawParticlesPacket msg = new SpawnBloodClawParticlesPacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}
	public static void encode(FriendlyByteBuf buf, SpawnBloodClawParticlesPacket msg) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(final SpawnBloodClawParticlesPacket msg, final IPayloadContext ctxSupplier) {
		if (ctxSupplier.player() == null) return;
		ctxSupplier.player().level().addParticle(
				BloodClawParticleFactory.createData(msg.getColor()),
				msg.getPos().x, msg.getPos().y, msg.getPos().z, 0f, 0, 0);
	}

	Vec3 pos;

	ParticleColor color;

	public SpawnBloodClawParticlesPacket() {
	}

	public SpawnBloodClawParticlesPacket(Vec3 pos, ParticleColor color) {
		this.pos = pos;
		this.color = color;
	}

	public ParticleColor getColor() {
		return color;
	}

	public Vec3 getPos() {
		return pos;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
