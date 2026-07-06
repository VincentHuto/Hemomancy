package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.ClawSlashRenderer;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpawnClawSlashPacket implements CustomPacketPayload {
	public static final Type<SpawnClawSlashPacket> TYPE = new Type<>(Hemomancy.rloc("spawn_claw_slash_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnClawSlashPacket> STREAM_CODEC =
			StreamCodec.of(SpawnClawSlashPacket::encode, SpawnClawSlashPacket::decode);

	private Vec3 pos;
	private Vec3 forward;
	private ParticleColor color;
	private boolean ambush;
	private float scale;
	private int seed;

	public SpawnClawSlashPacket() {
		this(Vec3.ZERO, new Vec3(0.0D, 0.0D, 1.0D), ParticleColor.BLOOD, false, 1.0F, 0);
	}

	public SpawnClawSlashPacket(Vec3 pos, Vec3 forward, ParticleColor color, boolean ambush, float scale, int seed) {
		this.pos = pos;
		this.forward = forward;
		this.color = color;
		this.ambush = ambush;
		this.scale = Math.max(0.25F, scale);
		this.seed = seed;
	}

	public static SpawnClawSlashPacket decode(FriendlyByteBuf buf) {
		return new SpawnClawSlashPacket(
				new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
				new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
				new ParticleColor(buf.readFloat(), buf.readFloat(), buf.readFloat()),
				buf.readBoolean(),
				buf.readFloat(),
				buf.readInt());
	}

	public static void encode(FriendlyByteBuf buf, SpawnClawSlashPacket msg) {
		buf.writeDouble(msg.pos.x);
		buf.writeDouble(msg.pos.y);
		buf.writeDouble(msg.pos.z);
		buf.writeDouble(msg.forward.x);
		buf.writeDouble(msg.forward.y);
		buf.writeDouble(msg.forward.z);
		buf.writeFloat(msg.color.getRed());
		buf.writeFloat(msg.color.getGreen());
		buf.writeFloat(msg.color.getBlue());
		buf.writeBoolean(msg.ambush);
		buf.writeFloat(msg.scale);
		buf.writeInt(msg.seed);
	}

	public static void handle(SpawnClawSlashPacket msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ClawSlashRenderer.spawn(msg.pos, msg.forward, msg.color, msg.ambush, msg.scale, msg.seed));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
