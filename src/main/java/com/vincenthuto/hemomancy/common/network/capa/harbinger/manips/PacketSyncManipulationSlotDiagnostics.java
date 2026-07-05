package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ManipulationSlotDiagnosticsClientData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncManipulationSlotDiagnostics implements CustomPacketPayload {
	public static final Type<PacketSyncManipulationSlotDiagnostics> TYPE =
			new Type<>(Hemomancy.rloc("packet_sync_manipulation_slot_diagnostics"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncManipulationSlotDiagnostics> STREAM_CODEC =
			StreamCodec.of(PacketSyncManipulationSlotDiagnostics::encode,
					PacketSyncManipulationSlotDiagnostics::decode);

	private final ManipulationSlotSnapshot snapshot;

	public PacketSyncManipulationSlotDiagnostics(ManipulationSlotSnapshot snapshot) {
		this.snapshot = snapshot == null ? ManipulationSlotSnapshot.EMPTY : snapshot;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncManipulationSlotDiagnostics msg) {
		ManipulationSlotSnapshot snapshot = msg.snapshot;
		buf.writeInt(snapshot.requestedSlots());
		buf.writeInt(snapshot.maxSlots());
		buf.writeInt(snapshot.appliedSlots());
		buf.writeInt(snapshot.cappedOffSlots());
		buf.writeInt(snapshot.contributions().size());
		for (ManipulationSlotContribution contribution : snapshot.contributions()) {
			buf.writeUtf(contribution.sourceId());
			buf.writeUtf(contribution.label());
			buf.writeEnum(contribution.category());
			buf.writeInt(contribution.amount());
			buf.writeUtf(contribution.condition());
		}
	}

	public static PacketSyncManipulationSlotDiagnostics decode(FriendlyByteBuf buf) {
		int requested = buf.readInt();
		int max = buf.readInt();
		int applied = buf.readInt();
		int capped = buf.readInt();
		int count = buf.readInt();
		List<ManipulationSlotContribution> contributions = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			contributions.add(new ManipulationSlotContribution(buf.readUtf(), buf.readUtf(),
					buf.readEnum(Category.class), buf.readInt(), buf.readUtf()));
		}
		return new PacketSyncManipulationSlotDiagnostics(new ManipulationSlotSnapshot(contributions,
				requested, max, applied, capped));
	}

	public static void handle(PacketSyncManipulationSlotDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ManipulationSlotDiagnosticsClientData.set(msg.snapshot));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
