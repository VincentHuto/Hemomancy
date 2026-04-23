package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.render.world.SanguineMonolithShatterRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class SpawnMonolithShatterBurstPacket implements CustomPacketPayload {

	public static final Type<SpawnMonolithShatterBurstPacket> TYPE = new Type<>(Hemomancy.rloc("spawn_monolith_shatter_burst_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnMonolithShatterBurstPacket> STREAM_CODEC = StreamCodec.of(SpawnMonolithShatterBurstPacket::encode, SpawnMonolithShatterBurstPacket::decode);

	public static SpawnMonolithShatterBurstPacket decode(FriendlyByteBuf buf) {
		SpawnMonolithShatterBurstPacket msg = new SpawnMonolithShatterBurstPacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(FriendlyByteBuf buf, SpawnMonolithShatterBurstPacket msg) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
	}

	public static void handle(final SpawnMonolithShatterBurstPacket msg, final IPayloadContext ctxSupplier) {
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null) return;
		ctxSupplier.enqueueWork(() -> SanguineMonolithShatterRenderer.spawnBurst(msg.getPos(), world.random));
	}

	private Vec3 pos;

	public SpawnMonolithShatterBurstPacket() {
		this.pos = Vec3.ZERO;
	}

	public SpawnMonolithShatterBurstPacket(Vec3 pos) {
		this.pos = pos;
	}

	public Vec3 getPos() {
		return pos;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
