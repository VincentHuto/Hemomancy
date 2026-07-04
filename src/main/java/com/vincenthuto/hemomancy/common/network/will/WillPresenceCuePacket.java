package com.vincenthuto.hemomancy.common.network.will;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.WillPresenceOverlay;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class WillPresenceCuePacket implements CustomPacketPayload {
	public static final Type<WillPresenceCuePacket> TYPE = new Type<>(Hemomancy.rloc("will_presence_cue"));
	public static final StreamCodec<FriendlyByteBuf, WillPresenceCuePacket> STREAM_CODEC =
			StreamCodec.of(WillPresenceCuePacket::encode, WillPresenceCuePacket::decode);

	private final Vec3 position;

	public WillPresenceCuePacket(Vec3 position) {
		this.position = position == null ? Vec3.ZERO : position;
	}

	public static WillPresenceCuePacket decode(FriendlyByteBuf buf) {
		try {
			return new WillPresenceCuePacket(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		} catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
			return new WillPresenceCuePacket(Vec3.ZERO);
		}
	}

	public static void encode(FriendlyByteBuf buf, WillPresenceCuePacket msg) {
		buf.writeDouble(msg.position.x);
		buf.writeDouble(msg.position.y);
		buf.writeDouble(msg.position.z);
	}

	public static void handle(final WillPresenceCuePacket msg, final IPayloadContext context) {
		if (context.player() == null) return;
		context.enqueueWork(() -> {
			context.player().level().playLocalSound(msg.position.x, msg.position.y, msg.position.z,
					SoundInit.ENTITY_WILL_PRESENCE_STING.get(), SoundSource.HOSTILE, 0.55F, 1.35F, false);
			if (WillPresenceOverlay.instance != null) {
				WillPresenceOverlay.instance.trigger();
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
