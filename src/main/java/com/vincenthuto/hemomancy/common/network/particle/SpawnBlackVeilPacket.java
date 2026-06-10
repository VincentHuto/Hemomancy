package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.BlackVeilRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpawnBlackVeilPacket implements CustomPacketPayload {
	public static final Type<SpawnBlackVeilPacket> TYPE = new Type<>(Hemomancy.rloc("spawn_black_veil_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnBlackVeilPacket> STREAM_CODEC =
			StreamCodec.of(SpawnBlackVeilPacket::encode, SpawnBlackVeilPacket::decode);

	private Vec3 pos;
	private float radius;
	private int durationTicks;
	private int seed;

	public SpawnBlackVeilPacket() {
		this(Vec3.ZERO, 8.0F, 1, 0);
	}

	public SpawnBlackVeilPacket(Vec3 pos, float radius, int durationTicks, int seed) {
		this.pos = pos;
		this.radius = Math.max(1.0F, radius);
		this.durationTicks = Math.max(1, durationTicks);
		this.seed = seed;
	}

	public static SpawnBlackVeilPacket decode(FriendlyByteBuf buf) {
		return new SpawnBlackVeilPacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
				buf.readFloat(), buf.readInt(), buf.readInt());
	}

	public static void encode(FriendlyByteBuf buf, SpawnBlackVeilPacket msg) {
		buf.writeDouble(msg.pos.x);
		buf.writeDouble(msg.pos.y);
		buf.writeDouble(msg.pos.z);
		buf.writeFloat(msg.radius);
		buf.writeInt(msg.durationTicks);
		buf.writeInt(msg.seed);
	}

	public static void handle(SpawnBlackVeilPacket msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> BlackVeilRenderer.addVeil(msg.pos, msg.radius, msg.durationTicks, msg.seed));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
