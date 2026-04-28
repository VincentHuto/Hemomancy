package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.SanguineMonolithShatterRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpawnPomePulsePacket implements CustomPacketPayload {

	public static final Type<SpawnPomePulsePacket> TYPE = new Type<>(Hemomancy.rloc("spawn_pome_pulse_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnPomePulsePacket> STREAM_CODEC = StreamCodec.of(SpawnPomePulsePacket::encode, SpawnPomePulsePacket::decode);

	public static SpawnPomePulsePacket decode(FriendlyByteBuf buf) {
		SpawnPomePulsePacket msg = new SpawnPomePulsePacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(FriendlyByteBuf buf, SpawnPomePulsePacket msg) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
	}

	public static void handle(final SpawnPomePulsePacket msg, final IPayloadContext ctxSupplier) {
		ClientLevel world = Minecraft.getInstance().level;
		if (world == null) return;
		ctxSupplier.enqueueWork(() -> SanguineMonolithShatterRenderer.spawnPomePulse(msg.getPos()));
	}

	private Vec3 pos;

	public SpawnPomePulsePacket() {
		this.pos = Vec3.ZERO;
	}

	public SpawnPomePulsePacket(Vec3 pos) {
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
