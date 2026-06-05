package com.vincenthuto.hemomancy.common.network.capa.harbinger.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.IKnownSummons;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class KnownSummonsServerPacket implements CustomPacketPayload {
	public static final Type<KnownSummonsServerPacket> TYPE = new Type<>(Hemomancy.rloc("known_summons_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, KnownSummonsServerPacket> STREAM_CODEC =
			StreamCodec.of(KnownSummonsServerPacket::encode, KnownSummonsServerPacket::decode);

	private final List<String> knownSummonNames;

	public KnownSummonsServerPacket(IKnownSummons known) {
		this.knownSummonNames = new ArrayList<>(known.getKnownSummonNames());
	}

	private KnownSummonsServerPacket(List<String> knownSummonNames) {
		this.knownSummonNames = knownSummonNames;
	}

	public static void encode(FriendlyByteBuf buf, KnownSummonsServerPacket msg) {
		buf.writeInt(msg.knownSummonNames.size());
		for (String name : msg.knownSummonNames) {
			buf.writeUtf(name);
		}
	}

	public static KnownSummonsServerPacket decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			names.add(buf.readUtf());
		}
		return new KnownSummonsServerPacket(names);
	}

	public static void handle(final KnownSummonsServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player != null) {
				HemoCapabilityAccess.getKnownSummons(player)
						.ifPresent(known -> known.setKnownSummonNames(msg.knownSummonNames));
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
