package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.SanguineMonolithShatterRenderer;
import com.vincenthuto.hemomancy.client.screen.overlay.SanguineOmenOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SpawnSanguineOmenEffectPacket implements CustomPacketPayload {

	public static final Type<SpawnSanguineOmenEffectPacket> TYPE = new Type<>(
			Hemomancy.rloc("spawn_sanguine_omen_effect_packet"));
	public static final StreamCodec<FriendlyByteBuf, SpawnSanguineOmenEffectPacket> STREAM_CODEC = StreamCodec.of(
			SpawnSanguineOmenEffectPacket::encode, SpawnSanguineOmenEffectPacket::decode);

	public static SpawnSanguineOmenEffectPacket decode(FriendlyByteBuf buf) {
		SpawnSanguineOmenEffectPacket msg = new SpawnSanguineOmenEffectPacket();
		try {
			msg.pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
			msg.durationTicks = buf.readInt();
			msg.peakAlpha = buf.readFloat();
			msg.seed = buf.readInt();
			msg.screenOverlay = buf.readBoolean();
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			return msg;
		}
		return msg;
	}

	public static void encode(FriendlyByteBuf buf, SpawnSanguineOmenEffectPacket msg) {
		buf.writeDouble(msg.getPos().x);
		buf.writeDouble(msg.getPos().y);
		buf.writeDouble(msg.getPos().z);
		buf.writeInt(msg.getDurationTicks());
		buf.writeFloat(msg.getPeakAlpha());
		buf.writeInt(msg.getSeed());
		buf.writeBoolean(msg.isScreenOverlay());
	}

	public static void handle(final SpawnSanguineOmenEffectPacket msg, final IPayloadContext ctxSupplier) {
		if (ctxSupplier.player() == null) return;
		ctxSupplier.enqueueWork(() -> {
			SanguineMonolithShatterRenderer.spawnOmenBurst(msg.getPos());
			if (SanguineOmenOverlay.instance != null) {
				SanguineOmenOverlay.Mode mode = msg.isScreenOverlay()
						? SanguineOmenOverlay.Mode.SCREEN_OVERLAY
						: SanguineOmenOverlay.Mode.WORLD_GRADE;
				SanguineOmenOverlay.instance.start(msg.getDurationTicks(), msg.getPeakAlpha(), msg.getSeed(), mode);
			}
		});
	}

	private Vec3 pos;
	private int durationTicks;
	private float peakAlpha;
	private int seed;
	private boolean screenOverlay;

	public SpawnSanguineOmenEffectPacket() {
		this(Vec3.ZERO, 120, 0.88F, 0, false);
	}

	public SpawnSanguineOmenEffectPacket(Vec3 pos, int durationTicks, float peakAlpha, int seed) {
		this(pos, durationTicks, peakAlpha, seed, false);
	}

	public SpawnSanguineOmenEffectPacket(Vec3 pos, int durationTicks, float peakAlpha, int seed,
			boolean screenOverlay) {
		this.pos = pos;
		this.durationTicks = Math.max(1, durationTicks);
		this.peakAlpha = Math.max(0.0F, Math.min(1.0F, peakAlpha));
		this.seed = seed;
		this.screenOverlay = screenOverlay;
	}

	public Vec3 getPos() {
		return pos;
	}

	public int getDurationTicks() {
		return durationTicks;
	}

	public float getPeakAlpha() {
		return peakAlpha;
	}

	public int getSeed() {
		return seed;
	}

	public boolean isScreenOverlay() {
		return screenOverlay;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
