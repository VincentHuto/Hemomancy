package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class SpawnLivingToolParticlesPacket implements CustomPacketPayload {

	public static final Type<SpawnLivingToolParticlesPacket> TYPE = new Type<>(Hemomancy.rloc("spawn_living_tool_particles_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnLivingToolParticlesPacket> STREAM_CODEC = StreamCodec.of(SpawnLivingToolParticlesPacket::encode, SpawnLivingToolParticlesPacket::decode);
	public static SpawnLivingToolParticlesPacket decode(FriendlyByteBuf buf) {
		SpawnLivingToolParticlesPacket msg = new SpawnLivingToolParticlesPacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.color = new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat());

		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}
	public static void encode(SpawnLivingToolParticlesPacket msg, FriendlyByteBuf buf) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeFloat(msg.getColor().getRed());
		buf.writeFloat(msg.getColor().getGreen());
		buf.writeFloat(msg.getColor().getBlue());

	}

	public static void handle(final SpawnLivingToolParticlesPacket msg, final IPayloadContext ctxSupplier) {
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null) return;
		for (int i = 0; i < 20; i++) {
			world.addParticle(BloodCellParticleFactory.createData(msg.getColor()), msg.getPos().x, msg.getPos().y + 1,
					msg.getPos().z, HLParticleUtils.inRange(-3, 3) * 0.015f, HLParticleUtils.inRange(-3, 3) * 0.015f,
					HLParticleUtils.inRange(-3, 3) * 0.015f);
		}
	}

	Vec3 pos;

	ParticleColor color;

	public SpawnLivingToolParticlesPacket() {
	}

	public SpawnLivingToolParticlesPacket(Vec3 pos, ParticleColor color) {
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
