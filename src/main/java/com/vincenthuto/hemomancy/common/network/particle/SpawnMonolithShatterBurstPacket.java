package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.SanguineMonolithShatterRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpawnMonolithShatterBurstPacket implements CustomPacketPayload {

	public static final Type<SpawnMonolithShatterBurstPacket> TYPE = new Type<>(Hemomancy.rloc("spawn_monolith_shatter_burst_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnMonolithShatterBurstPacket> STREAM_CODEC = StreamCodec.of(SpawnMonolithShatterBurstPacket::encode, SpawnMonolithShatterBurstPacket::decode);

	public static SpawnMonolithShatterBurstPacket decode(FriendlyByteBuf buf) {
		SpawnMonolithShatterBurstPacket msg = new SpawnMonolithShatterBurstPacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.shardOnly = buf.readableBytes() > 0 && buf.readBoolean();
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(FriendlyByteBuf buf, SpawnMonolithShatterBurstPacket msg) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeBoolean(msg.isShardOnly());
	}

	public static void handle(final SpawnMonolithShatterBurstPacket msg, final IPayloadContext ctxSupplier) {
		if (ctxSupplier.player() == null) return;
		ctxSupplier.enqueueWork(() -> {
			if (msg.isShardOnly()) {
				SanguineMonolithShatterRenderer.spawnShardPulse(msg.getPos(), ctxSupplier.player().level().random);
			} else {
				SanguineMonolithShatterRenderer.spawnBurst(msg.getPos(), ctxSupplier.player().level().random);
			}
		});
	}

	private Vec3 pos;
	private boolean shardOnly;

	public SpawnMonolithShatterBurstPacket() {
		this(Vec3.ZERO, false);
	}

	public SpawnMonolithShatterBurstPacket(Vec3 pos) {
		this(pos, false);
	}

	public SpawnMonolithShatterBurstPacket(Vec3 pos, boolean shardOnly) {
		this.pos = pos;
		this.shardOnly = shardOnly;
	}

	public Vec3 getPos() {
		return pos;
	}

	public boolean isShardOnly() {
		return shardOnly;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
