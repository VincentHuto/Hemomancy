package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ManipulationCostDiagnosticsClientData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncManipulationCostDiagnostics implements CustomPacketPayload {
	public static final Type<PacketSyncManipulationCostDiagnostics> TYPE =
			new Type<>(Hemomancy.rloc("packet_sync_manipulation_cost_diagnostics"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncManipulationCostDiagnostics> STREAM_CODEC =
			StreamCodec.of(PacketSyncManipulationCostDiagnostics::encode,
					PacketSyncManipulationCostDiagnostics::decode);

	private final ManipulationCostSnapshot snapshot;

	public PacketSyncManipulationCostDiagnostics(ManipulationCostSnapshot snapshot) {
		this.snapshot = snapshot == null ? ManipulationCostSnapshot.EMPTY : snapshot;
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncManipulationCostDiagnostics msg) {
		ManipulationCostSnapshot snapshot = msg.snapshot;
		buf.writeDouble(snapshot.baseCost());
		buf.writeDouble(snapshot.discountMultiplier());
		buf.writeDouble(snapshot.surchargeMultiplier());
		buf.writeDouble(snapshot.totalMultiplier());
		buf.writeDouble(snapshot.effectiveCost());
		buf.writeInt(snapshot.contributions().size());
		for (ManipulationCostContribution contribution : snapshot.contributions()) {
			buf.writeUtf(contribution.sourceId());
			buf.writeUtf(contribution.label());
			buf.writeEnum(contribution.category());
			buf.writeDouble(contribution.multiplier());
			buf.writeUtf(contribution.condition());
		}
	}

	public static PacketSyncManipulationCostDiagnostics decode(FriendlyByteBuf buf) {
		double baseCost = buf.readDouble();
		double discount = buf.readDouble();
		double surcharge = buf.readDouble();
		double total = buf.readDouble();
		double effective = buf.readDouble();
		int count = buf.readInt();
		List<ManipulationCostContribution> contributions = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			contributions.add(new ManipulationCostContribution(buf.readUtf(), buf.readUtf(),
					buf.readEnum(Category.class), buf.readDouble(), buf.readUtf()));
		}
		return new PacketSyncManipulationCostDiagnostics(new ManipulationCostSnapshot(baseCost, contributions,
				discount, surcharge, total, effective));
	}

	public static void handle(PacketSyncManipulationCostDiagnostics msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> ManipulationCostDiagnosticsClientData.set(msg.snapshot));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
