package com.vincenthuto.hemomancy.common.network.capa.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.IKnownStillArts;
import com.vincenthuto.hemomancy.common.unstained.stillarts.StillArt;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class KnownStillArtsServerPacket implements CustomPacketPayload {
	public static final Type<KnownStillArtsServerPacket> TYPE = new Type<>(Hemomancy.rloc("known_still_arts_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, KnownStillArtsServerPacket> STREAM_CODEC =
			StreamCodec.of(KnownStillArtsServerPacket::encode, KnownStillArtsServerPacket::decode);

	private final List<String> knownArtNames;
	private final String selectedArtName;

	public KnownStillArtsServerPacket(IKnownStillArts known) {
		this.knownArtNames = new ArrayList<>(known.getKnownArtNames());
		this.selectedArtName = known.getSelectedArt().getName();
	}

	private KnownStillArtsServerPacket(List<String> knownArtNames, String selectedArtName) {
		this.knownArtNames = knownArtNames;
		this.selectedArtName = selectedArtName;
	}

	public static void encode(FriendlyByteBuf buf, KnownStillArtsServerPacket msg) {
		buf.writeInt(msg.knownArtNames.size());
		for (String name : msg.knownArtNames) {
			buf.writeUtf(name);
		}
		buf.writeUtf(msg.selectedArtName);
	}

	public static KnownStillArtsServerPacket decode(FriendlyByteBuf buf) {
		int count = buf.readInt();
		List<String> names = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			names.add(buf.readUtf());
		}
		return new KnownStillArtsServerPacket(names, buf.readUtf());
	}

	public static void handle(final KnownStillArtsServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}
			HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known -> {
				known.setKnownArtNames(msg.knownArtNames);
				known.setSelectedArt(StillArt.byName(msg.selectedArtName));
			});
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
