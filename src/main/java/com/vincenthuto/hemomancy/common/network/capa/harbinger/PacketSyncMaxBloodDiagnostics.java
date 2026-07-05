package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.MaxBloodDiagnosticsClientData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncMaxBloodDiagnostics implements CustomPacketPayload {
	public static final Type<PacketSyncMaxBloodDiagnostics> TYPE =
			new Type<>(Hemomancy.rloc("packet_sync_max_blood_diagnostics"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncMaxBloodDiagnostics> STREAM_CODEC =
			StreamCodec.of(PacketSyncMaxBloodDiagnostics::encode, PacketSyncMaxBloodDiagnostics::decode);

	private final MaxBloodSnapshot snapshot;

	public PacketSyncMaxBloodDiagnostics(MaxBloodSnapshot snapshot) {
		this.snapshot = snapshot == null ? MaxBloodSnapshot.EMPTY : snapshot;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncMaxBloodDiagnostics msg) {
		MaxBloodSnapshot snapshot = msg.snapshot;
		buf.writeDouble(snapshot.baseMax());
		buf.writeDouble(snapshot.positiveBonus());
		buf.writeDouble(snapshot.negativePenalty());
		buf.writeDouble(snapshot.netBonus());
		buf.writeDouble(snapshot.totalMax());
		buf.writeInt(snapshot.contributions().size());
		for (MaxBloodContribution contribution : snapshot.contributions()) {
			buf.writeUtf(contribution.sourceId());
			buf.writeUtf(contribution.label());
			buf.writeEnum(contribution.category());
			buf.writeDouble(contribution.amount());
			buf.writeUtf(contribution.condition());
		}
	}

	public static PacketSyncMaxBloodDiagnostics decode(FriendlyByteBuf buf) {
		double baseMax = buf.readDouble();
		double positive = buf.readDouble();
		double negative = buf.readDouble();
		double net = buf.readDouble();
		double total = buf.readDouble();
		int count = buf.readInt();
		List<MaxBloodContribution> contributions = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			contributions.add(new MaxBloodContribution(buf.readUtf(), buf.readUtf(), buf.readEnum(Category.class),
					buf.readDouble(), buf.readUtf()));
		}
		return new PacketSyncMaxBloodDiagnostics(new MaxBloodSnapshot(baseMax, contributions, positive, negative,
				net, total));
	}

	public static void handle(PacketSyncMaxBloodDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> MaxBloodDiagnosticsClientData.set(msg.snapshot));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
