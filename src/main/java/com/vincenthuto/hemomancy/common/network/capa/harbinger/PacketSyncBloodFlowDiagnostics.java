package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.BloodFlowDiagnosticsClientData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncBloodFlowDiagnostics implements CustomPacketPayload {
	public static final Type<PacketSyncBloodFlowDiagnostics> TYPE =
			new Type<>(Hemomancy.rloc("packet_sync_blood_flow_diagnostics"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncBloodFlowDiagnostics> STREAM_CODEC =
			StreamCodec.of(PacketSyncBloodFlowDiagnostics::encode, PacketSyncBloodFlowDiagnostics::decode);

	private final BloodFlowSnapshot snapshot;

	public PacketSyncBloodFlowDiagnostics(BloodFlowSnapshot snapshot) {
		this.snapshot = snapshot == null ? BloodFlowSnapshot.EMPTY : snapshot;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncBloodFlowDiagnostics msg) {
		BloodFlowSnapshot snapshot = msg.snapshot;
		buf.writeDouble(snapshot.positivePerTick());
		buf.writeDouble(snapshot.negativePerTick());
		buf.writeDouble(snapshot.netPerTick());
		buf.writeDouble(snapshot.circulationBandwidthPerWindow());
		buf.writeDouble(snapshot.circulationUsedInWindow());
		buf.writeDouble(snapshot.circulationRemainingInWindow());
		buf.writeInt(snapshot.circulationWindowTicks());
		buf.writeBoolean(snapshot.circulationEnabled());
		buf.writeInt(snapshot.contributions().size());
		for (BloodFlowContribution contribution : snapshot.contributions()) {
			buf.writeUtf(contribution.sourceId());
			buf.writeUtf(contribution.label());
			buf.writeEnum(contribution.category());
			buf.writeDouble(contribution.requestedPerTick());
			buf.writeDouble(contribution.appliedPerTick());
			buf.writeBoolean(contribution.circulationLimited());
			buf.writeUtf(contribution.condition());
		}
	}

	public static PacketSyncBloodFlowDiagnostics decode(FriendlyByteBuf buf) {
		double positive = buf.readDouble();
		double negative = buf.readDouble();
		double net = buf.readDouble();
		double bandwidth = buf.readDouble();
		double used = buf.readDouble();
		double remaining = buf.readDouble();
		int windowTicks = buf.readInt();
		boolean enabled = buf.readBoolean();
		int count = buf.readInt();
		List<BloodFlowContribution> contributions = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			contributions.add(new BloodFlowContribution(buf.readUtf(), buf.readUtf(), buf.readEnum(Category.class),
					buf.readDouble(), buf.readDouble(), buf.readBoolean(), buf.readUtf()));
		}
		return new PacketSyncBloodFlowDiagnostics(new BloodFlowSnapshot(contributions, positive, negative, net,
				bandwidth, used, remaining, windowTicks, enabled));
	}

	public static void handle(PacketSyncBloodFlowDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> BloodFlowDiagnosticsClientData.set(msg.snapshot));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
