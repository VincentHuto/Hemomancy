package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class SpawnFlaskParticlesPacket implements CustomPacketPayload {

	public static final Type<SpawnFlaskParticlesPacket> TYPE = new Type<>(Hemomancy.rloc("spawn_flask_particles_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnFlaskParticlesPacket> STREAM_CODEC = StreamCodec.of(SpawnFlaskParticlesPacket::encode, SpawnFlaskParticlesPacket::decode);
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
	public static void encode(FriendlyByteBuf buf, SpawnFlaskParticlesPacket msg) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(final SpawnFlaskParticlesPacket msg, final IPayloadContext ctxSupplier) {
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null) return;
		world.addParticle(GlowParticleFactory.createData(msg.getColor()), msg.getPos().x, msg.getPos().y + 1,
				msg.getPos().z, HLParticleUtils.inRange(-3, 3) * 0.015f, HLParticleUtils.inRange(-3, 3) * 0.015f,
				HLParticleUtils.inRange(-3, 3) * 0.015f);
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

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
