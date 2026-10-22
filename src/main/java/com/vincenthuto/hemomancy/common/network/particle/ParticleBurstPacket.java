package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/**
 * A one-shot particle burst at a point. Each {@link Kind} names one effect the client spawns; kinds that are not
 * tinted ignore {@code color}.
 */
public record ParticleBurstPacket(Kind kind, Vec3 pos, ParticleColor color) implements CustomPacketPayload {
	public enum Kind {
		/** A Blood Avatar landing a hit. */
		AVATAR_HIT,
		/** A Bloody Flask being drunk. */
		BLOOD_FLASK,
		/** Vesper's claw strike. */
		BLOOD_CLAW,
		/** A living tool breaking apart into blood cells. */
		LIVING_TOOL_BREAK,
		/** A Sanguine Monolith shattering. */
		MONOLITH_SHATTER,
		/** The smaller Monolith shard pulse of Silent Archon movement. */
		MONOLITH_SHARD_PULSE,
		/** A Qliphoth pome bursting. */
		POME_PULSE
	}

	private static final ParticleColor UNTINTED = new ParticleColor(0.0F, 0.0F, 0.0F);
	// MOD_ID is a compile-time constant, so building the id this way keeps the codec usable without loading
	// Hemomancy (VisualPayloadCodecTest).
	public static final Type<ParticleBurstPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "particle_burst"));
	public static final StreamCodec<FriendlyByteBuf, ParticleBurstPacket> STREAM_CODEC =
			StreamCodec.of(ParticleBurstPacket::encode, ParticleBurstPacket::decode);

	public ParticleBurstPacket {
		color = color == null ? UNTINTED : color;
	}

	/** An untinted burst. */
	public ParticleBurstPacket(Kind kind, Vec3 pos) {
		this(kind, pos, null);
	}

	private static void encode(FriendlyByteBuf buf, ParticleBurstPacket packet) {
		buf.writeEnum(packet.kind);
		buf.writeDouble(packet.pos.x);
		buf.writeDouble(packet.pos.y);
		buf.writeDouble(packet.pos.z);
		buf.writeFloat(packet.color.getRed());
		buf.writeFloat(packet.color.getGreen());
		buf.writeFloat(packet.color.getBlue());
	}

	private static ParticleBurstPacket decode(FriendlyByteBuf buf) {
		Kind kind = buf.readEnum(Kind.class);
		Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		return new ParticleBurstPacket(kind, pos, new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
